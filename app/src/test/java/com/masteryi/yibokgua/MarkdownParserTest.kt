package com.masteryi.yibokgua

import com.masteryi.yibokgua.ui.screen.reader.BlockKind
import com.masteryi.yibokgua.ui.screen.reader.MarkdownParser
import com.masteryi.yibokgua.ui.screen.reader.ReaderBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {

    @Test
    fun parse_標題層級() {
        val blocks = MarkdownParser.parse("# 一\n## 二\n### 三\n#### 四")
        assertEquals(
            listOf(
                ReaderBlock("一", BlockKind.H1),
                ReaderBlock("二", BlockKind.H2),
                ReaderBlock("三", BlockKind.H3),
                ReaderBlock("四", BlockKind.H3)
            ),
            blocks
        )
    }

    @Test
    fun parse_清單與引用() {
        val blocks = MarkdownParser.parse("- 甲\n* 乙\n1. 丙\n> 引言")
        assertEquals(BlockKind.BULLET, blocks[0].kind)
        assertEquals(BlockKind.BULLET, blocks[1].kind)
        assertEquals(BlockKind.ORDERED, blocks[2].kind)
        assertEquals(BlockKind.QUOTE, blocks[3].kind)
        assertEquals("引言", blocks[3].text)
    }

    @Test
    fun parse_程式碼區塊() {
        val blocks = MarkdownParser.parse("前文\n```\nval a = 1\nval b = 2\n```\n後文")
        assertEquals(3, blocks.size)
        assertEquals(BlockKind.PARAGRAPH, blocks[0].kind)
        assertEquals(BlockKind.CODE, blocks[1].kind)
        assertEquals("val a = 1\nval b = 2", blocks[1].text)
        assertEquals(BlockKind.PARAGRAPH, blocks[2].kind)
    }

    @Test
    fun parse_水平線與一般段落() {
        val blocks = MarkdownParser.parse("---\n一段文字")
        assertEquals(BlockKind.RULE, blocks[0].kind)
        assertEquals(BlockKind.PARAGRAPH, blocks[1].kind)
        assertEquals("一段文字", blocks[1].text)
    }

    @Test
    fun parse_空行被忽略() {
        val blocks = MarkdownParser.parse("\n\n正文\n\n")
        assertEquals(1, blocks.size)
        assertTrue(blocks[0].kind == BlockKind.PARAGRAPH)
    }
}
