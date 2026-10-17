package com.masteryi.yibokgua.ui.screen.reader

import java.util.UUID

/** 支援的文件格式。 */
enum class BookFormat(val label: String, val extension: String) {
    TXT("TXT", "txt"),
    MD("MD", "md"),
    PDF("PDF", "pdf"),
    DOC("DOC", "doc"),
    DOCX("DOCX", "docx");

    companion object {
        /** 由檔名副檔名判斷格式；不支援時回傳 null。 */
        fun fromFileName(fileName: String): BookFormat? {
            val ext = fileName.substringAfterLast('.', "").lowercase()
            return when (ext) {
                "txt", "text" -> TXT
                "md", "markdown", "mdown", "mkd" -> MD
                "pdf" -> PDF
                "doc" -> DOC
                "docx" -> DOCX
                else -> null
            }
        }

        /** 由 MIME type 判斷格式；不支援時回傳 null。 */
        fun fromMimeType(mime: String?): BookFormat? = when (mime?.lowercase()) {
            "text/plain" -> TXT
            "text/markdown", "text/x-markdown" -> MD
            "application/pdf" -> PDF
            "application/msword" -> DOC
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> DOCX
            else -> null
        }
    }
}

/**
 * 書架中的一本書。
 *
 * 檔案內容於匯入時複製到 App 內部儲存（[path]），因此原檔刪除或重開機後仍可閱讀。
 * 以 DataStore 儲存的中繼資料（見 [encode] / [decode]），未使用 SharedPreferences。
 */
data class Book(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val fileName: String,
    val format: BookFormat,
    val path: String,
    val sizeBytes: Long,
    val importedAt: Long,
    val lastReadAt: Long = 0L,
    val progress: Float = 0f
) {
    fun serialize(): String = listOf(
        id,
        escape(title),
        escape(fileName),
        format.name,
        escape(path),
        sizeBytes.toString(),
        importedAt.toString(),
        lastReadAt.toString(),
        progress.toString()
    ).joinToString(FIELD_SEP)

    companion object {
        private const val FIELD_SEP = "\t"
        private const val RECORD_SEP = "\n"

        fun encode(books: List<Book>): String = books.joinToString(RECORD_SEP) { it.serialize() }

        fun decode(raw: String?): List<Book> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split(RECORD_SEP).mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null
                val f = line.split(FIELD_SEP)
                if (f.size < 9) return@mapNotNull null
                runCatching {
                    Book(
                        id = f[0],
                        title = unescape(f[1]),
                        fileName = unescape(f[2]),
                        format = BookFormat.valueOf(f[3]),
                        path = unescape(f[4]),
                        sizeBytes = f[5].toLong(),
                        importedAt = f[6].toLong(),
                        lastReadAt = f[7].toLong(),
                        progress = f[8].toFloat().coerceIn(0f, 1f)
                    )
                }.getOrNull()
            }
        }

        private fun escape(s: String): String = buildString(s.length) {
            for (c in s) when (c) {
                '\\' -> append("\\\\")
                '\t' -> append("\\t")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(c)
            }
        }

        private fun unescape(s: String): String = buildString(s.length) {
            var i = 0
            while (i < s.length) {
                val c = s[i]
                if (c == '\\' && i + 1 < s.length) {
                    when (s[i + 1]) {
                        '\\' -> { append('\\'); i += 2 }
                        't' -> { append('\t'); i += 2 }
                        'n' -> { append('\n'); i += 2 }
                        'r' -> { append('\r'); i += 2 }
                        else -> { append(c); i++ }
                    }
                } else {
                    append(c)
                    i++
                }
            }
        }
    }
}
