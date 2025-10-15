package com.sublime.trailnotes.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.sublime.trailnotes.data.local.SyncState
import com.sublime.trailnotes.domain.Note
import com.sublime.trailnotes.ui.theme.TrailNotesTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {
    val pagingItems = viewModel.notes.collectAsLazyPagingItems()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    NotesScreen(
        notes = pagingItems,
        uiState = uiState.value,
        onAddNote = viewModel::addSampleNote,
        onDeleteNote = viewModel::deleteNote,
        onSyncNow = viewModel::syncNow,
        modifier = modifier
    )

    LaunchedEffect(Unit) {
        viewModel.schedulePeriodicSync()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: LazyPagingItems<Note>,
    uiState: NotesUiState,
    onAddNote: () -> Unit,
    onDeleteNote: (String) -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Trail Notes")
                        val lastSync = uiState.lastSuccessfulSync
                        val subtitle = if (lastSync != null) {
                            "Last synced ${formatTimestamp(lastSync)}"
                        } else {
                            "Waiting for first sync"
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSyncNow) {
                        Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Sync now")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add note")
            }
        }
    ) { contentPadding ->
        if (notes.itemCount == 0) {
            EmptyState(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            )
        } else {
            LazyColumn(
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = notes.itemCount,
                    key = { index ->
                        notes.peek(index)?.id ?: "placeholder-$index"
                    }
                ) { index ->
                    val note = notes[index]
                    if (note != null) {
                        NoteRow(
                            note = note,
                            onDelete = { onDeleteNote(note.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteRow(
    note: Note,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = note.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = note.body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp)
            )
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = when (note.syncStatus) {
                            SyncState.PENDING -> "Pending sync"
                            SyncState.SYNCED -> "Synced"
                            SyncState.FAILED -> "Retrying"
                            SyncState.CONFLICT -> "Conflict"
                            else -> "Unknown"
                        }
                    )
                },
                modifier = Modifier.padding(top = 8.dp),
                colors = AssistChipDefaults.assistChipColors()
            )
            Text(
                text = "Updated ${formatTimestamp(note.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.End)) {
                Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete note")
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your offline trail journal is empty",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tap + to capture a note even without signal",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String =
    DateTimeFormatter.ofPattern("MMM d • HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(timestamp))

@Preview
@Composable
private fun EmptyStatePreview() {
    TrailNotesTheme {
        EmptyState(modifier = Modifier.fillMaxSize())
    }
}
