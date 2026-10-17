package com.masteryi.yibokgua

import com.masteryi.yibokgua.ui.screen.reader.DocExtractor
import org.junit.Assert.assertTrue
import org.junit.Test

class DocExtractorTest {

    @Test
    fun extract_UTF16LE可讀文字被保留() {
        val text = "這是一段測試文字內容，用於驗證擷取。"
        val bytes = text.toByteArray(Charsets.UTF_16LE)

        val blocks = DocExtractor.extract(bytes)

        assertTrue(blocks.isNotEmpty())
        assertTrue(blocks.any { it.text.contains("測試文字") })
    }

    @Test
    fun extract_空資料回傳空清單() {
        assertTrue(DocExtractor.extract(ByteArray(0)).isEmpty())
    }

    @Test
    fun extract_二進位雜訊仍可回傳清單不崩潰() {
        val noise = ByteArray(512) { (it * 31 + 7).toByte() }
        // 盡力而為：不保證內容正確，但不得拋出例外
        DocExtractor.extract(noise)
    }
}
