package com.example.a1st

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ThemeViewModel(private val context: Context) : ViewModel() {

    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme

    init {
        // Load the dark theme preference
        SettingsDataStore.isDarkModeEnabled(context).onEach {
            _darkTheme.value = it
        }.launchIn(viewModelScope)
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            SettingsDataStore.setDarkMode(context, isDark)
        }
    }
}
