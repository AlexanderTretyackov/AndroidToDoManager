package ru.tretyackov.todo

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ru.tretyackov.todo.di.DaggerAppComponent
import ru.tretyackov.todo.utilities.SyncToDoListWorker
import java.util.concurrent.TimeUnit

class App : Application() {
    val appComponent = DaggerAppComponent.factory().create(this)
    override fun onCreate() {
        super.onCreate()
        enqueueSyncToDoList()
    }

    private fun enqueueSyncToDoList(){
        val syncToDoListWorkerRequest = PeriodicWorkRequestBuilder<SyncToDoListWorker>(8, TimeUnit.HOURS)
            .setInitialDelay(8, TimeUnit.HOURS)
            .build()
        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork("SyncToDoListWorker", ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, syncToDoListWorkerRequest)
    }
}