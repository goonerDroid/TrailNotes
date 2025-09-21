package com.sublime.trailnotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        NoteEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDb : RoomDatabase() {
    abstract fun notesDao(): NotesDao
}