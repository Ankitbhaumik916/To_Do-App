package com.example.a1st

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "todo_prefs")
