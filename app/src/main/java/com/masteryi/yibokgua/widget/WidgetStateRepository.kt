package com.masteryi.yibokgua.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.widgetDataStore by preferencesDataStore(name = "yibokgua_widget")

/** 桌面小工具狀態存取（DataStore，非 SharedPreferences）。 */
class WidgetStateRepository(private val context: Context) {

    private object Keys {
        val OVERRIDE_ID = intPreferencesKey("widget_override_id")
        val OVERRIDE_EPOCH_DAY = longPreferencesKey("widget_override_epoch_day")
    }

    suspend fun state(): WidgetState {
        val prefs = context.widgetDataStore.data.first()
        return WidgetState(
            overrideId = prefs[Keys.OVERRIDE_ID],
            overrideEpochDay = prefs[Keys.OVERRIDE_EPOCH_DAY]
        )
    }

    suspend fun setOverride(id: Int, epochDay: Long) {
        context.widgetDataStore.edit {
            it[Keys.OVERRIDE_ID] = id
            it[Keys.OVERRIDE_EPOCH_DAY] = epochDay
        }
    }

    suspend fun clearOverride() {
        context.widgetDataStore.edit {
            it.remove(Keys.OVERRIDE_ID)
            it.remove(Keys.OVERRIDE_EPOCH_DAY)
        }
    }
}
