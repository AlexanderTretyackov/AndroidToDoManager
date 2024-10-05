package ru.tretyackov.todo.utilities

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.tretyackov.todo.App

class SyncToDoListWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val repository = (appContext as App).appComponent.todoItemsRepository()
        repository.refresh()
        Log.i("SyncToDoListWorker", "Success sync ToDo list")
        return Result.success()
    }
}
