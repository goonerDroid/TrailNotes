package com.sublime.trailnotes.data.remote

data class NoteDto(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val version: Long
)
