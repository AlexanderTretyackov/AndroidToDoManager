package ru.tretyackov.todo

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.Preferences
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.tretyackov.todo.data.THEME_MODE_KEY
import ru.tretyackov.todo.data.dataStore
import ru.tretyackov.todo.di.DaggerAppComponent
import ru.tretyackov.todo.utilities.SyncToDoListWorker
import java.util.concurrent.TimeUnit

private const val PERIOD_SYNC_HOURS = 8L

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    val appComponent = DaggerAppComponent.factory().create(this)
    override fun onCreate() {
        super.onCreate()
        instance = this
        runBlocking {
            val preferences = this@App.dataStore.data.first()
            updateThemeFromPreferences(preferences)
        }
        val applicationScope = CoroutineScope(Dispatchers.Main)
        applicationScope.launch {
            this@App.dataStore.data.collect { preferences ->
                updateThemeFromPreferences(preferences)
            }
        }
        enqueueSyncToDoList()
    }

    private fun updateThemeFromPreferences(preferences: Preferences) {
        val themeMode = preferences[THEME_MODE_KEY]
        if (themeMode == null || themeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            return
        }
        if (themeMode == AppCompatDelegate.MODE_NIGHT_NO)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    private fun enqueueSyncToDoList() {
        val syncToDoListWorkerRequest =
            PeriodicWorkRequestBuilder<SyncToDoListWorker>(PERIOD_SYNC_HOURS, TimeUnit.HOURS)
                .setInitialDelay(PERIOD_SYNC_HOURS, TimeUnit.HOURS)
                .build()
        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                "SyncToDoListWorker",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                syncToDoListWorkerRequest
            )
    }
}
