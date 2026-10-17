package com.masteryi.yibokgua.ui.screen.reader

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

/**
 * .docx 文字擷取。
 *
 * .docx 本質為 ZIP，內含 `word/document.xml`。此處以輕量方式取出段落與標題，
 * 不引入第三方函式庫。表格內文字亦會被取出。
 */
object DocxExtractor {

    private val STYLE = Regex("<w:pStyle\\b[^>]*w:val=\"([^\"]*)\"")
    private val TOKEN = Regex(
        "<w:t\\b[^>]*>(.*?)</w:t>|<w:tab\\b[^>]*/?>|<w:br\\b[^>]*/?>",
        RegexOption.DOT_MATCHES_ALL
    )

    fun extract(bytes: ByteArray): List<ReaderBlock> {
        val xml = readEntry(bytes, "word/document.xml")
            ?.toString(Charsets.UTF_8)
            ?: error("無效的 .docx（缺少 word/document.xml）")

        val blocks = mutableListOf<ReaderBlock>()
        for (chunk in xml.split("</w:p>")) {
            val text = extractText(chunk).trim()
            if (text.isEmpty()) continue
            val kind = headingKind(STYLE.find(chunk)?.groupValues?.get(1))
            blocks += ReaderBlock(text, kind ?: BlockKind.PARAGRAPH)
        }
        return blocks
    }

    private fun extractText(chunk: String): String = buildString {
        for (m in TOKEN.findAll(chunk)) {
            when {
                m.value.startsWith("<w:t") -> append(decodeEntities(m.groupValues[1]))
                m.value.startsWith("<w:tab") -> append(' ')
                m.value.startsWith("<w:br") -> append('\n')
            }
        }
    }

    private fun headingKind(styleId: String?): BlockKind? {
        if (styleId.isNullOrBlank()) return null
        val lower = styleId.lowercase()
        if (lower == "title") return BlockKind.H1
        val level = when {
            lower.contains("heading") -> lower.substringAfter("heading").trim().toIntOrNull()
            styleId.contains("標題") -> styleId.substringAfter("標題").trim().toIntOrNull()
            else -> null
        } ?: return null
        return when (level) {
            1 -> BlockKind.H1
            2 -> BlockKind.H2
            else -> BlockKind.H3
        }
    }

    private fun readEntry(bytes: ByteArray, name: String): ByteArray? {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name.equals(name, ignoreCase = true)) return zip.readBytes()
                entry = zip.nextEntry
            }
        }
        return null
    }

    /** 還原 XML 實體（含數字實體）。 */
    fun decodeEntities(s: String): String {
        if ('&' !in s) return s
        return buildString(s.length) {
            var i = 0
            while (i < s.length) {
                val c = s[i]
                if (c == '&') {
                    val end = s.indexOf(';', i + 1)
                    if (end > i) {
                        val entity = s.substring(i + 1, end)
                        val replaced = when {
                            entity == "amp" -> "&"
                            entity == "lt" -> "<"
                            entity == "gt" -> ">"
                            entity == "quot" -> "\""
                            entity == "apos" -> "'"
                            entity.startsWith("#x") || entity.startsWith("#X") ->
                                entity.substring(2).toIntOrNull(16)?.let { String(Character.toChars(it)) }
                            entity.startsWith("#") ->
                                entity.substring(1).toIntOrNull()?.let { String(Character.toChars(it)) }
                            else -> null
                        }
                        if (replaced != null) {
                            append(replaced)
                            i = end + 1
                            continue
                        }
                    }
                }
                append(c)
                i++
            }
        }
    }
}
