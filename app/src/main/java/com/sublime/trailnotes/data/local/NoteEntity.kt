package com.sublime.trailnotes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val version: Long,
    val syncStatus: Int = 0
)

object SyncState {
    const val PENDING = 0
    const val SYNCED = 1
    const val FAILED = 2
    const val CONFLICT = 3
}