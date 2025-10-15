package com.sublime.trailnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sublime.trailnotes.ui.theme.TrailNotesTheme
import com.sublime.trailnotes.ui.notes.NotesScreen
import com.sublime.trailnotes.ui.notes.NotesViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrailNotesTheme {
                val viewModel: NotesViewModel = koinViewModel()
                NotesScreen(viewModel = viewModel)
            }
        }
    }
}