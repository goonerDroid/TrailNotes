package com.sublime.trailnotes.data.local

import androidx.paging.PagingSource
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

interface NotesDao {

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun pagingSource(): PagingSource<Int, NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun get(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(notes: List<NoteEntity>)

    @Query("UPDATE notes SET deletedAt = :now, syncStatus = :status WHERE id = :id")
    suspend fun softDelete(id: String, now: Long, status: Int)

    @Query("SELECT * FROM notes WHERE syncStatus != :syncedStatus")
    suspend fun getPendingSync(syncedStatus: Int = SyncState.SYNCED): List<NoteEntity>

    @Query("UPDATE notes SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<String>, status: Int)

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}