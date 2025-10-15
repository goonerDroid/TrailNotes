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
import java.time.ZoneId

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
            val nowMillis = clock.millis()
            val noteTitle = randomTitle()
            val noteBody = buildString {
                append(randomBody())
                append("\n\nCaptured offline at ")
                append(formatTimestamp(nowMillis))
            }
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

    private fun formatTimestamp(timestamp: Long): String =
        captureFormatter.format(Instant.ofEpochMilli(timestamp))

    private companion object {
        private val captureFormatter = DateTimeFormatter.ofPattern("MMM d • HH:mm")
            .withZone(ZoneId.systemDefault())

        private val sampleTitles = listOf(
            "Sunrise ridge sketch",
            "Trailside reflections",
            "Campsite checklist",
            "Forest floor finds",
            "Summit weather log"
        )

        private val sampleBodies = listOf(
            "Jotted observations about the switchbacks and how the light shifted near the overlook.",
            "Captured quick trail markers so the group can compare routes back at camp.",
            "Logged notes on the ridge so we can plan a safe descent tomorrow.",
            "Tracked the wildflowers clustered near the creek crossing for later identification.",
            "Sketched the valley panorama to remember where the shortcut drops back onto the main path."
        )

        private fun randomTitle(): String = sampleTitles.random()

        private fun randomBody(): String = sampleBodies.random()
    }
}

data class NotesUiState(
    val lastSuccessfulSync: Long? = null
)
