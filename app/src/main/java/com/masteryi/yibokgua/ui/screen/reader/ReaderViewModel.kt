package com.masteryi.yibokgua.ui.screen.reader

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 書籍畫面狀態：書架清單與目前開啟的文件。 */
data class ReaderUiState(
    val books: List<Book> = emptyList(),
    val currentBook: Book? = null,
    val content: BookContent? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isImporting: Boolean = false,
    val importSummary: String? = null
)

/**
 * 書籍閱讀器 ViewModel。
 *
 * 管理書架（匯入／刪除／閱讀進度）與目前開啟文件的內容載入，並提供顯示設定。
 */
class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = ReaderSettingsRepository(application)
    private val library = BookLibraryRepository(application)

    private val _settings = MutableStateFlow(ReaderSettings())
    val settings: StateFlow<ReaderSettings> = _settings.asStateFlow()

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var settingsInitialized = false

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { stored ->
                // 只在初次載入時覆蓋，避免非同步回寫蓋掉使用者的即時調整
                if (!settingsInitialized) {
                    _settings.value = stored
                    settingsInitialized = true
                }
            }
        }
        viewModelScope.launch {
            library.books.collect { books ->
                _uiState.update { it.copy(books = books) }
            }
        }
    }

    // ---- 顯示設定（即時生效，背景持久化） ----

    fun setFontSizeSp(value: Float) {
        _settings.update { it.copy(fontSizeSp = value.coerceIn(
            ReaderSettings.MIN_FONT_SIZE_SP, ReaderSettings.MAX_FONT_SIZE_SP)) }
        viewModelScope.launch { settingsRepository.setFontSizeSp(_settings.value.fontSizeSp) }
    }

    fun setLineHeightMultiplier(value: Float) {
        _settings.update { it.copy(lineHeightMultiplier = value.coerceIn(
            ReaderSettings.MIN_LINE_HEIGHT, ReaderSettings.MAX_LINE_HEIGHT)) }
        viewModelScope.launch { settingsRepository.setLineHeightMultiplier(_settings.value.lineHeightMultiplier) }
    }

    fun setParagraphSpacingDp(value: Float) {
        _settings.update { it.copy(paragraphSpacingDp = value) }
        viewModelScope.launch { settingsRepository.setParagraphSpacingDp(value) }
    }

    fun setContentPaddingDp(value: Float) {
        _settings.update { it.copy(contentPaddingDp = value) }
        viewModelScope.launch { settingsRepository.setContentPaddingDp(value) }
    }

    /** 還原閱讀器顯示設定為預設值。 */
    fun resetSettings() {
        _settings.value = ReaderSettings()
        viewModelScope.launch { settingsRepository.reset() }
    }

    // ---- 書架 ----

    /** 批次匯入多個 URI；逐一處理，彙整成功與失敗數量。 */
    fun importUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importSummary = null) }
            val imported = mutableListOf<Book>()
            val failures = mutableListOf<String>()
            for (uri in uris) {
                runCatching { library.import(uri) }
                    .onSuccess { imported += it }
                    .onFailure { failures += (it.message ?: "匯入失敗") }
            }
            if (imported.isNotEmpty()) library.addAll(imported)
            _uiState.update {
                it.copy(isImporting = false, importSummary = buildImportSummary(imported.size, failures))
            }
        }
    }

    fun consumeImportSummary() {
        _uiState.update { it.copy(importSummary = null) }
    }

    fun openBook(book: Book) {
        viewModelScope.launch {
            _uiState.update { it.copy(currentBook = book, content = null, isLoading = true, error = null) }
            val result = withContext(Dispatchers.IO) { runCatching { DocumentParser.parse(book) } }
            result
                .onSuccess { content ->
                    _uiState.update { it.copy(content = content, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "無法開啟文件") }
                }
            library.updateProgress(book.id, book.progress, System.currentTimeMillis())
        }
    }

    fun closeBook() {
        _uiState.update { it.copy(currentBook = null, content = null, error = null) }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch { library.delete(book) }
    }

    /** 儲存目前書本閱讀進度（0..1）。 */
    fun saveProgress(progress: Float) {
        val book = _uiState.value.currentBook ?: return
        val safe = progress.coerceIn(0f, 1f)
        if (kotlin.math.abs(book.progress - safe) < 0.001f) return
        _uiState.update { it.copy(currentBook = it.currentBook?.copy(progress = safe)) }
        viewModelScope.launch { library.updateProgress(book.id, safe, System.currentTimeMillis()) }
    }

    private fun buildImportSummary(success: Int, failures: List<String>): String {
        val parts = mutableListOf<String>()
        if (success > 0) parts += "已匯入 $success 本"
        if (failures.isNotEmpty()) parts += "${failures.size} 本失敗"
        return if (parts.isEmpty()) "未匯入任何檔案" else parts.joinToString("，")
    }
}
