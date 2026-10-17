package com.masteryi.yibokgua

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.masteryi.yibokgua.ui.screen.reader.inlineMarkdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownInlineTest {

    @Test
    fun inlineMarkdown_粗體與等寬() {
        val result = inlineMarkdown("這是**粗體**與`程式碼`")

        assertEquals("這是粗體與程式碼", result.text)
        assertTrue(result.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        assertTrue(result.spanStyles.any { it.item.fontFamily == FontFamily.Monospace })
    }

    @Test
    fun inlineMarkdown_未閉合語法原樣輸出() {
        val result = inlineMarkdown("a ** b")
        assertEquals("a ** b", result.text)
    }
}
