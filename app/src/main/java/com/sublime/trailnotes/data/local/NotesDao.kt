package com.sublime.trailnotes.data.local

import androidx.paging.PagingSource
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

interface NotesDao {

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun pagingSource(): PagingSource<Int, NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun get(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Query("UPDATE notes SET deletedAt = :now, syncStatus = :status WHERE id = :id")
    suspend fun softDelete(id: String, now: Long, status: Int)
}