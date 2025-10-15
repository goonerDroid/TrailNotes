package com.sublime.trailnotes

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.sublime.trailnotes.di.appModule
import com.sublime.trailnotes.di.dbModule

class TrailNotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TrailNotesApp)
            modules(dbModule, appModule)
        }
    }
}