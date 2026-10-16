package com.masteryi.yibokgua

import com.masteryi.engine.iching.ChineseStrokeCounter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChineseStrokeCounterTest {

    @Test
    fun totalStrokes_卜卦為十字() {
        assertEquals(10, ChineseStrokeCounter.totalStrokes("卜卦"))
    }

    @Test
    fun totalStrokes_繁體與簡體碼位各自正確() {
        // 繁體採用該字本身筆畫，不套用簡體字碼位之筆畫
        assertEquals(15, ChineseStrokeCounter.totalStrokes("請"))
        assertEquals(10, ChineseStrokeCounter.totalStrokes("请"))
        assertEquals(9, ChineseStrokeCounter.totalStrokes("為"))
        assertEquals(4, ChineseStrokeCounter.totalStrokes("为"))
    }

    @Test
    fun getStrokes_非漢字回傳零() {
        assertEquals(0, ChineseStrokeCounter.getStrokes('。'.code))
        assertEquals(0, ChineseStrokeCounter.getStrokes(' '.code))
    }

    @Test
    fun totalStrokes_忽略標點與空白() {
        assertEquals(
            ChineseStrokeCounter.totalStrokes("卜卦"),
            ChineseStrokeCounter.totalStrokes("卜 卦。")
        )
    }

    @Test
    fun hasData_常用漢字為真() {
        assertTrue(ChineseStrokeCounter.hasData("卜".codePointAt(0)))
        assertTrue(ChineseStrokeCounter.hasData("請".codePointAt(0)))
    }

    @Test
    fun getStrokes_未涵蓋之罕用字回傳保守值() {
        val extB = 0x20000 // 𠀀，擴充 B 區，不在 BMP 資料內
        assertFalse(ChineseStrokeCounter.hasData(extB))
        assertEquals(ChineseStrokeCounter.FALLBACK_STROKES, ChineseStrokeCounter.getStrokes(extB))
    }
}
