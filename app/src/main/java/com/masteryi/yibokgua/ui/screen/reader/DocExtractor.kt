package com.masteryi.yibokgua.ui.screen.reader

import java.lang.Character.UnicodeScript

/**
 * 舊版二進位 .doc 的「盡力而為」文字擷取。
 *
 * .doc 為 OLE 複合檔案，格式複雜且非公開標準，無法可靠解析。此處不引入第三方函式庫，
 * 改以經驗法則擷取可讀文字：分別嘗試 UTF-16LE 與 8 位元編碼，取內容較「像文字」者。
 * 排版與部分內容可能失真，僅供概略閱讀。
 */
object DocExtractor {

    private const val MIN_RUN = 4

    fun extract(bytes: ByteArray): List<ReaderBlock> {
        val utf16 = extractRuns(String(bytes, Charsets.UTF_16LE))
        val singleByte = extractRuns(String(bytes, Charsets.ISO_8859_1))
        val chosen = if (score(utf16) >= score(singleByte)) utf16 else singleByte

        val blocks = mutableListOf<ReaderBlock>()
        for (run in chosen) {
            for (line in run.split('\n')) {
                val text = line.trim()
                if (text.isNotEmpty()) blocks += ReaderBlock(text, BlockKind.PARAGRAPH)
            }
        }
        return blocks
    }

    private fun extractRuns(text: String): List<String> {
        val runs = mutableListOf<String>()
        val sb = StringBuilder()
        for (c in text) {
            if (isMeaningful(c)) {
                sb.append(c)
            } else {
                if (sb.length >= MIN_RUN) runs += sb.toString().trim()
                sb.clear()
            }
        }
        if (sb.length >= MIN_RUN) runs += sb.toString().trim()
        return runs.filter { it.isNotBlank() }
    }

    private fun score(runs: List<String>): Int {
        var score = 0
        for (run in runs) {
            for (c in run) {
                score += when {
                    UnicodeScript.of(c.code) == UnicodeScript.HAN -> 3
                    Character.isLetterOrDigit(c) -> 1
                    else -> 0
                }
            }
        }
        return score
    }

    private fun isMeaningful(c: Char): Boolean =
        Character.isLetterOrDigit(c) ||
            Character.isWhitespace(c) ||
            c in "。，、；：！？「」『』（）〈〉《》【】…—·．・,.;:!?\"'()[]{}<>-–—/\\|@#%&+*=~^$"
}
