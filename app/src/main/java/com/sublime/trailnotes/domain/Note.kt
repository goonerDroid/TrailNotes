package com.sublime.trailnotes.domain

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val version: Long,
    val syncStatus: Int
)
