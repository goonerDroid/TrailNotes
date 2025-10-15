package com.sublime.trailnotes.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sublime.trailnotes.data.NotesRepository
import com.sublime.trailnotes.domain.Note
import com.sublime.trailnotes.sync.SyncScheduler
import java.time.Clock
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NotesRepository,
    private val syncScheduler: SyncScheduler,
    private val clock: Clock
) : ViewModel() {

    val notes: StateFlow<PagingData<Note>> =
        repository.pagedNotes()
            .cachedIn(viewModelScope)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PagingData.empty())

    val uiState: StateFlow<NotesUiState> = repository
        .lastSuccessfulSync()
        .map { timestamp -> NotesUiState(lastSuccessfulSync = timestamp) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun addSampleNote() {
        viewModelScope.launch {
            val timestamp = clock.instant()
            val noteTitle = "Trail report ${timestamp.atZone(clock.zone).toLocalTime()}"
            val noteBody = "Captured offline at ${formatInstant(timestamp)}"
            repository.createNote(noteTitle, noteBody)
            syncScheduler.triggerOneTimeSync()
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
            syncScheduler.triggerOneTimeSync()
        }
    }

    fun syncNow() {
        syncScheduler.triggerOneTimeSync()
    }

    fun schedulePeriodicSync() {
        syncScheduler.schedulePeriodicSync()
    }

    private fun formatInstant(instant: Instant): String =
        DateTimeFormatter.ofPattern("MMM d • HH:mm")
            .withZone(clock.zone)
            .format(instant)
}

data class NotesUiState(
    val lastSuccessfulSync: Long? = null
)
