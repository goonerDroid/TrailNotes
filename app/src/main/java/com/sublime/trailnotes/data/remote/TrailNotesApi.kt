package com.sublime.trailnotes.data.remote

interface TrailNotesApi {
    suspend fun fetchNotes(updatedAfter: Long?): List<NoteDto>
    suspend fun upsertNotes(notes: List<NoteDto>): List<NoteDto>
}
