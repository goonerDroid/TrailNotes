package com.sublime.trailnotes.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SyncPreferencesDataSource(
    private val dataStore: DataStore<Preferences>
) {

    val lastSuccessfulSync: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[LAST_SUCCESSFUL_SYNC]
    }

    suspend fun updateLastSuccessfulSync(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SUCCESSFUL_SYNC] = timestamp
        }
    }

    private companion object {
        val LAST_SUCCESSFUL_SYNC = longPreferencesKey("last_successful_sync")
    }
}
