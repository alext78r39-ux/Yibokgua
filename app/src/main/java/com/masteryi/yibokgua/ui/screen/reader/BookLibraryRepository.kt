package com.masteryi.yibokgua.ui.screen.reader

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID

private val Context.libraryDataStore by preferencesDataStore(name = "yibokgua_library")

/**
 * 書架資料存取。
 *
 * 匯入時將檔案內容複製到 App 內部儲存（`filesDir/books/<id>.<ext>`），
 * 書本中繼資料以 DataStore 儲存（拒絕 SharedPreferences）。
 */
class BookLibraryRepository(private val context: Context) {

    private val booksKey = stringPreferencesKey("books")

    val books: Flow<List<Book>> = context.libraryDataStore.data.map { prefs ->
        Book.decode(prefs[booksKey]).sortedByDescending { it.importedAt }
    }

    private val booksDir: File
        get() = File(context.filesDir, "books").apply { if (!exists()) mkdirs() }

    private suspend fun currentBooks(): List<Book> =
        Book.decode(context.libraryDataStore.data.first()[booksKey])

    /** 匯入單一檔案（複製至內部儲存）並回傳 [Book]；格式不支援或讀取失敗時丟出例外。 */
    suspend fun import(uri: Uri): Book {
        val name = queryDisplayName(uri) ?: "未命名文件"
        val format = BookFormat.fromFileName(name)
            ?: BookFormat.fromMimeType(context.contentResolver.getType(uri))
            ?: error("不支援的檔案格式：$name")

        val id = UUID.randomUUID().toString()
        val target = File(booksDir, "$id.${format.extension}")
        var size = 0L
        val input = context.contentResolver.openInputStream(uri) ?: error("無法開啟檔案：$name")
        input.use { source ->
            target.outputStream().use { output ->
                size = source.copyTo(output)
            }
        }
        if (size == 0L) {
            target.delete()
            error("檔案為空：$name")
        }
        return Book(
            id = id,
            title = name.substringBeforeLast('.').ifBlank { name },
            fileName = name,
            format = format,
            path = target.absolutePath,
            sizeBytes = size,
            importedAt = System.currentTimeMillis()
        )
    }

    suspend fun add(book: Book) {
        val list = currentBooks() + book
        context.libraryDataStore.edit { it[booksKey] = Book.encode(list) }
    }

    suspend fun addAll(additional: List<Book>) {
        if (additional.isEmpty()) return
        val list = currentBooks() + additional
        context.libraryDataStore.edit { it[booksKey] = Book.encode(list) }
    }

    suspend fun delete(book: Book) {
        runCatching { File(book.path).delete() }
        val list = currentBooks().filterNot { it.id == book.id }
        context.libraryDataStore.edit { it[booksKey] = Book.encode(list) }
    }

    suspend fun updateProgress(id: String, progress: Float, lastReadAt: Long) {
        val safe = progress.coerceIn(0f, 1f)
        val list = currentBooks().map {
            if (it.id == id) it.copy(progress = safe, lastReadAt = lastReadAt) else it
        }
        context.libraryDataStore.edit { it[booksKey] = Book.encode(list) }
    }

    private fun queryDisplayName(uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }.getOrNull()
}
