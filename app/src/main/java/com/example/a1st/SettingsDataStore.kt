package com.example.a1st
import com.example.a1st.dataStore
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")

object SettingsDataStore {
    fun isDarkModeEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }
    }

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
}
