package com.sublime.trailnotes.di

import androidx.room.Room.databaseBuilder
import com.sublime.trailnotes.data.local.AppDb
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dbModule = module {
    single {
        databaseBuilder(
            androidContext(),
            AppDb::class.java,
            "trail-notes.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDb>().notesDao() }
}
