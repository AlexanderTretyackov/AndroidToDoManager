package ru.tretyackov.todo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val THEME_MODE_KEY = intPreferencesKey("theme_mode")

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")