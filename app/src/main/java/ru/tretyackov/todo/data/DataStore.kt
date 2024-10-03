package ru.tretyackov.todo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val THEME_MODE_KEY = intPreferencesKey("theme_mode")
private val YANDEX_AUTH_TOKEN_KEY = stringPreferencesKey("YANDEX_AUTH_TOKEN")

suspend fun Context.getYandexAuthToken(): Token? {
    return dataStore.data.firstOrNull()?.get(YANDEX_AUTH_TOKEN_KEY)?.let { tokenString ->
        try {
            return Json.decodeFromString<Token>(tokenString)
        } catch (ex: Exception) {
            return null
        }
    }
}

suspend fun Context.updateYandexAuthToken(token: Token?) {
    dataStore.edit { preferences ->
        preferences[YANDEX_AUTH_TOKEN_KEY] = if (token == null) "" else Json.encodeToString(token)
    }
}
