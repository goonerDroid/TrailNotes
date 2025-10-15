package com.sublime.trailnotes.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.sublime.trailnotes.data.local.NoteEntity
import com.sublime.trailnotes.data.local.NotesDao
import com.sublime.trailnotes.data.local.SyncState
import com.sublime.trailnotes.data.preferences.SyncPreferencesDataSource
import com.sublime.trailnotes.data.remote.TrailNotesApi
import com.sublime.trailnotes.domain.Note
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class NotesRepository(
    private val dao: NotesDao,
    private val api: TrailNotesApi,
    private val syncPreferences: SyncPreferencesDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    private val clock: Clock
) {

    fun pagedNotes(): Flow<PagingData<Note>> =
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { dao.pagingSource() }
        )
            .flow
            .map { pagingData -> pagingData.map { it.toDomain() } }

    suspend fun createNote(title: String, body: String) {
        withContext(ioDispatcher) {
            val now = clock.millis()
            val entity = NoteEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                body = body,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
                version = 0L,
                syncStatus = SyncState.PENDING
            )
            dao.upsert(entity)
        }
    }

    suspend fun deleteNote(id: String) {
        withContext(ioDispatcher) {
            val now = clock.millis()
            dao.softDelete(id, now, SyncState.PENDING)
        }
    }

    suspend fun runSync(): SyncResult = withContext(ioDispatcher) {
        val pending = dao.getPendingSync()
        try {
            val pushed = if (pending.isNotEmpty()) {
                api.upsertNotes(pending.map { it.toDto() })
            } else {
                emptyList()
            }

            if (pending.isNotEmpty()) {
                val updatedEntities = pushed.map { it.toEntity(SyncState.SYNCED) }
                if (updatedEntities.isNotEmpty()) {
                    dao.upsert(updatedEntities)
                }
            }

            val lastSync = syncPreferences.lastSuccessfulSync
                .map { it ?: 0L }
                .first()
            val pulled = api.fetchNotes(lastSync)
            if (pulled.isNotEmpty()) {
                val notes = pulled.map { it.toEntity(SyncState.SYNCED) }
                dao.upsert(notes)
            }

            val syncedAt = clock.millis()
            syncPreferences.updateLastSuccessfulSync(syncedAt)
            SyncResult.Success(syncedAt, pending.size, pushed.size, pulled.size)
        } catch (error: Exception) {
            if (pending.isNotEmpty()) {
                dao.updateSyncStatus(pending.map { it.id }, SyncState.FAILED)
            }
            SyncResult.Error(error)
        }
    }

    fun lastSuccessfulSync(): Flow<Long?> = syncPreferences.lastSuccessfulSync

    sealed interface SyncResult {
        data class Success(
            val syncedAt: Long,
            val pendingCount: Int,
            val pushedCount: Int,
            val pulledCount: Int
        ) : SyncResult

        data class Error(val throwable: Throwable) : SyncResult
    }
}
