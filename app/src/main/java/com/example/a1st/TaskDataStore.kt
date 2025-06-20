package com.example.a1st

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.a1st.dataStore


private val TASK_LIST_KEY = stringPreferencesKey("task_list")

object TaskDataStore {
    private val gson = Gson()

    suspend fun saveTasks(context: Context, tasks: List<Task>) {
        val json = gson.toJson(tasks)
        context.dataStore.edit { preferences ->
            preferences[TASK_LIST_KEY] = json
        }
    }

    fun getTasks(context: Context): Flow<List<Task>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[TASK_LIST_KEY] ?: "[]"
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson(json, type)
        }
    }
}
