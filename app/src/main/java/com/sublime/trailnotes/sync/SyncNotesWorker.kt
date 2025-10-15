package com.sublime.trailnotes.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sublime.trailnotes.data.NotesRepository
import com.sublime.trailnotes.data.NotesRepository.SyncResult
import org.koin.java.KoinJavaComponent.inject

class SyncNotesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val repository: NotesRepository by inject(NotesRepository::class.java)

    override suspend fun doWork(): Result {
        return when (val result = repository.runSync()) {
            is SyncResult.Success -> Result.success()
            is SyncResult.Error -> Result.retry()
        }
    }

    companion object {
        const val ON_DEMAND_WORK_NAME = "trail_notes_on_demand_sync"
        const val PERIODIC_WORK_NAME = "trail_notes_periodic_sync"
    }
}
