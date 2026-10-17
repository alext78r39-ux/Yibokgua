package com.masteryi.yibokgua.ui.screen.reader

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 閱讀器顯示設定。
 *
 * 全部以 DataStore 儲存，未使用 SharedPreferences。
 */
data class ReaderSettings(
    val fontSizeSp: Float = 18f,
    val lineHeightMultiplier: Float = 1.7f,
    val paragraphSpacingDp: Float = 12f,
    val contentPaddingDp: Float = 20f
) {
    companion object {
        const val MIN_FONT_SIZE_SP = 12f
        const val MAX_FONT_SIZE_SP = 36f
        const val MIN_LINE_HEIGHT = 1.2f
        const val MAX_LINE_HEIGHT = 2.4f
    }
}

private val Context.readerDataStore by preferencesDataStore(name = "yibokgua_reader")

/** 閱讀器設定存取（DataStore，非 SharedPreferences） */
class ReaderSettingsRepository(private val context: Context) {

    private object Keys {
        val FONT_SIZE = floatPreferencesKey("reader_font_size")
        val LINE_HEIGHT = floatPreferencesKey("reader_line_height")
        val PARAGRAPH_SPACING = floatPreferencesKey("reader_paragraph_spacing")
        val CONTENT_PADDING = floatPreferencesKey("reader_content_padding")
    }

    val settings: Flow<ReaderSettings> = context.readerDataStore.data.map { prefs ->
        ReaderSettings(
            fontSizeSp = prefs[Keys.FONT_SIZE] ?: 18f,
            lineHeightMultiplier = prefs[Keys.LINE_HEIGHT] ?: 1.7f,
            paragraphSpacingDp = prefs[Keys.PARAGRAPH_SPACING] ?: 12f,
            contentPaddingDp = prefs[Keys.CONTENT_PADDING] ?: 20f
        )
    }

    suspend fun setFontSizeSp(value: Float) {
        context.readerDataStore.edit { it[Keys.FONT_SIZE] = value }
    }

    suspend fun setLineHeightMultiplier(value: Float) {
        context.readerDataStore.edit { it[Keys.LINE_HEIGHT] = value }
    }

    suspend fun setParagraphSpacingDp(value: Float) {
        context.readerDataStore.edit { it[Keys.PARAGRAPH_SPACING] = value }
    }

    suspend fun setContentPaddingDp(value: Float) {
        context.readerDataStore.edit { it[Keys.CONTENT_PADDING] = value }
    }

    suspend fun reset() {
        context.readerDataStore.edit {
            it.remove(Keys.FONT_SIZE)
            it.remove(Keys.LINE_HEIGHT)
            it.remove(Keys.PARAGRAPH_SPACING)
            it.remove(Keys.CONTENT_PADDING)
        }
    }
}
