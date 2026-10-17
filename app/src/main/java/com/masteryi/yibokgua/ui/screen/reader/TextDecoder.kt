package com.masteryi.yibokgua.ui.screen.reader

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

/** 純文字解碼工具（依 BOM 與編碼自動判讀）。 */
object TextDecoder {

    /** 依 BOM 與編碼自動判讀文字，優先 UTF-8，其次 UTF-16，最後以 GB18030 相容中文舊檔。 */
    fun decodeText(bytes: ByteArray): String {
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()
        ) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }

        val strictUtf8 = runCatching {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        }.getOrNull()
        if (strictUtf8 != null) return strictUtf8

        return runCatching {
            String(bytes, Charset.forName("GB18030"))
        }.getOrElse {
            String(bytes, Charsets.UTF_8)
        }
    }

    /** 以空行分段，將純文字轉為段落區塊。 */
    fun toBlocks(text: String): List<ReaderBlock> =
        text.split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { ReaderBlock(it, BlockKind.PARAGRAPH) }
}
