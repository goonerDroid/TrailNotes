package com.sublime.trailnotes.data.remote

import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeTrailNotesApi(
    private val clock: Clock
) : TrailNotesApi {

    private val mutex = Mutex()
    private val remoteNotes = mutableMapOf<String, NoteDto>()

    override suspend fun fetchNotes(updatedAfter: Long?): List<NoteDto> {
        delay(API_DELAY_MS)
        val threshold = updatedAfter ?: 0L
        return mutex.withLock {
            remoteNotes.values
                .filter { it.updatedAt > threshold }
                .map { it.copy() }
        }
    }

    override suspend fun upsertNotes(notes: List<NoteDto>): List<NoteDto> {
        delay(API_DELAY_MS)
        if (notes.isEmpty()) return emptyList()
        val now = clock.millis()
        return mutex.withLock {
            notes.map { incoming ->
                val existing = remoteNotes[incoming.id]
                val assignedId = incoming.id.ifEmpty { UUID.randomUUID().toString() }
                val nextVersion = (existing?.version ?: incoming.version).coerceAtLeast(incoming.version) + 1
                val updatedNote = incoming.copy(
                    id = assignedId,
                    version = nextVersion,
                    updatedAt = maxOf(incoming.updatedAt, now)
                )
                remoteNotes[assignedId] = updatedNote
                updatedNote
            }
        }
    }

    companion object {
        private const val API_DELAY_MS = 250L
    }
}
