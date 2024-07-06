package ru.tretyackov.todo.utilities

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import ru.tretyackov.todo.App
import ru.tretyackov.todo.data.DataResult
import ru.tretyackov.todo.data.TodoItemsRepository
import ru.tretyackov.todo.di.AppComponent
import javax.inject.Inject

class SyncToDoListWorker (private val appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val repository = (appContext as App).appComponent.todoItemsRepository()
        val flowDataResult = repository.getAll()
        val dataResult = flowDataResult.first{ it is DataResult.Success ||  it is DataResult.Error }
        if(dataResult is DataResult.Success)
        {
            Log.i("SyncToDoListWorker", "Success sync ToDo list")
        }
        return Result.success()
    }
}