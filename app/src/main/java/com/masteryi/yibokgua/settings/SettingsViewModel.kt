package com.masteryi.yibokgua.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val settings: StateFlow<Settings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Settings())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setFontSizeScale(scale: Float) {
        viewModelScope.launch { repository.setFontSizeScale(scale) }
    }

    fun setBorderWidth(width: Float) {
        viewModelScope.launch { repository.setBorderWidth(width) }
    }

    fun setKeyboardScale(scale: Float) {
        viewModelScope.launch { repository.setKeyboardScale(scale) }
    }

    fun setOnboardingSeen(seen: Boolean) {
        viewModelScope.launch { repository.setOnboardingSeen(seen) }
    }

    fun reset() {
        viewModelScope.launch { repository.reset() }
    }
}
