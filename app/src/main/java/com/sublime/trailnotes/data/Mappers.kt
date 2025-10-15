package com.sublime.trailnotes.data

import com.sublime.trailnotes.data.local.NoteEntity
import com.sublime.trailnotes.data.remote.NoteDto
import com.sublime.trailnotes.domain.Note

fun NoteEntity.toDomain(): Note =
    Note(
        id = id,
        title = title,
        body = body,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        version = version,
        syncStatus = syncStatus
    )

fun NoteEntity.toDto(): NoteDto =
    NoteDto(
        id = id,
        title = title,
        body = body,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        version = version
    )

fun NoteDto.toEntity(syncStatus: Int): NoteEntity =
    NoteEntity(
        id = id,
        title = title,
        body = body,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        version = version,
        syncStatus = syncStatus
    )

fun NoteDto.toDomain(syncStatus: Int): Note =
    Note(
        id = id,
        title = title,
        body = body,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        version = version,
        syncStatus = syncStatus
    )
