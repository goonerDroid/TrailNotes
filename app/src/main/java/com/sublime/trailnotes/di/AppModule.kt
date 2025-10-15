package com.sublime.trailnotes.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.work.WorkManager
import com.sublime.trailnotes.data.NotesRepository
import com.sublime.trailnotes.data.preferences.SyncPreferencesDataSource
import com.sublime.trailnotes.data.remote.FakeTrailNotesApi
import com.sublime.trailnotes.data.remote.TrailNotesApi
import com.sublime.trailnotes.sync.SyncScheduler
import com.sublime.trailnotes.ui.notes.NotesViewModel
import java.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private const val SYNC_DATA_STORE_FILE = "sync.preferences_pb"

val appModule = module {
    single<CoroutineDispatcher> { Dispatchers.IO }
    single { Clock.systemUTC() }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile(SYNC_DATA_STORE_FILE) }
        )
    }

    single { SyncPreferencesDataSource(get()) }

    single<TrailNotesApi> { FakeTrailNotesApi(get()) }

    single { NotesRepository(get(), get(), get(), get(), get()) }

    single { WorkManager.getInstance(androidContext()) }

    single { SyncScheduler(get()) }

    viewModel { NotesViewModel(get(), get(), get()) }
}
