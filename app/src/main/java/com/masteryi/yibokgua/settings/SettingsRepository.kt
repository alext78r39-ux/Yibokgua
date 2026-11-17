package com.masteryi.yibokgua.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val storeValue: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2)
}

private val Context.settingsDataStore by preferencesDataStore(name = "yibokgua_settings")

data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontSizeScale: Float = 1.0f,
    val borderWidth: Float = 0.5f,
    val keyboardScale: Float = 1.0f
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val FONT_SIZE_SCALE = floatPreferencesKey("font_size_scale")
        val BORDER_WIDTH = floatPreferencesKey("border_width")
        val KEYBOARD_SCALE = floatPreferencesKey("keyboard_scale")
    }

    val settings: Flow<Settings> = context.settingsDataStore.data.map { prefs ->
        Settings(
            themeMode = ThemeMode.entries.firstOrNull {
                it.storeValue == (prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.storeValue)
            } ?: ThemeMode.SYSTEM,
            fontSizeScale = prefs[Keys.FONT_SIZE_SCALE] ?: 1.0f,
            borderWidth = prefs[Keys.BORDER_WIDTH] ?: 0.5f,
            keyboardScale = prefs[Keys.KEYBOARD_SCALE] ?: 1.0f
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.storeValue
        }
    }

    suspend fun setFontSizeScale(scale: Float) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.FONT_SIZE_SCALE] = scale
        }
    }

    suspend fun setBorderWidth(width: Float) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.BORDER_WIDTH] = width
        }
    }

    suspend fun setKeyboardScale(scale: Float) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.KEYBOARD_SCALE] = scale
        }
    }

    suspend fun reset() {
        context.settingsDataStore.edit { it.clear() }
    }
}
