package com.masteryi.yibokgua

import com.masteryi.yibokgua.ui.screen.reader.BlockKind
import com.masteryi.yibokgua.ui.screen.reader.TextDecoder
import org.junit.Assert.assertEquals
import org.junit.Test

class TextDecoderTest {

    @Test
    fun decodeText_純UTF8() {
        val bytes = "易經卜卦".toByteArray(Charsets.UTF_8)
        assertEquals("易經卜卦", TextDecoder.decodeText(bytes))
    }

    @Test
    fun decodeText_UTF8含BOM() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
            "風火家人".toByteArray(Charsets.UTF_8)
        assertEquals("風火家人", TextDecoder.decodeText(bytes))
    }

    @Test
    fun decodeText_UTF16LE含BOM() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte()) +
            "天地否".toByteArray(Charsets.UTF_16LE)
        assertEquals("天地否", TextDecoder.decodeText(bytes))
    }

    @Test
    fun decodeText_UTF16BE含BOM() {
        val bytes = byteArrayOf(0xFE.toByte(), 0xFF.toByte()) +
            "水火既濟".toByteArray(Charsets.UTF_16BE)
        assertEquals("水火既濟", TextDecoder.decodeText(bytes))
    }

    @Test
    fun toBlocks_去除空白行並分段() {
        val blocks = TextDecoder.toBlocks("第一行\n\n  第二行  \n\n\n第三行\n")
        assertEquals(3, blocks.size)
        assertEquals("第一行", blocks[0].text)
        assertEquals("第二行", blocks[1].text)
        assertEquals("第三行", blocks[2].text)
        assertEquals(BlockKind.PARAGRAPH, blocks[0].kind)
    }
}
