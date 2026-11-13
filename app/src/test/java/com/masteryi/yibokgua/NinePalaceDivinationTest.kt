package com.masteryi.yibokgua

import com.masteryi.engine.ninepalace.NinePalaceCalculator
import com.masteryi.engine.ninepalace.NinePalaceGua
import com.masteryi.engine.ninepalace.NinePalaceInput
import com.masteryi.engine.ninepalace.NinePalaceInterpreter
import com.masteryi.engine.ninepalace.toCardText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NinePalaceDivinationTest {

    @Test
    fun `三月十四日卯時應為速喜、病符、大安`() {
        val result = NinePalaceCalculator.calculate(month = 3, day = 14, hourIndex = 4)

        assertEquals(NinePalaceGua.SU_XI, result.monthGua)
        assertEquals(NinePalaceGua.BING_FU, result.dayGua)
        assertEquals(NinePalaceGua.DA_AN, result.hourGua)
        assertEquals(NinePalaceGua.DA_AN, result.mainGua)
    }

    @Test
    fun `正月初一子時三卦皆為大安`() {
        val result = NinePalaceCalculator.calculate(month = 1, day = 1, hourIndex = 1)

        assertEquals(NinePalaceGua.DA_AN, result.monthGua)
        assertEquals(NinePalaceGua.DA_AN, result.dayGua)
        assertEquals(NinePalaceGua.DA_AN, result.hourGua)
    }

    @Test
    fun `月份編號循環滿九回到一`() {
        val monthByTen = NinePalaceCalculator.calculate(month = 10, day = 1, hourIndex = 1)
        val monthByOne = NinePalaceCalculator.calculate(month = 1, day = 1, hourIndex = 1)
        assertEquals(monthByOne.monthGua, monthByTen.monthGua)

        val monthByTwelve = NinePalaceCalculator.calculate(month = 12, day = 1, hourIndex = 1)
        assertEquals(NinePalaceGua.SU_XI, monthByTwelve.monthGua)
    }

    @Test
    fun `輸入超出範圍會拋出例外`() {
        val invalidInputs = listOf(
            Triple(0, 1, 1),
            Triple(13, 1, 1),
            Triple(1, 0, 1),
            Triple(1, 31, 1),
            Triple(1, 1, 0),
            Triple(1, 1, 13)
        )
        invalidInputs.forEach { (month, day, hour) ->
            var thrown = false
            try {
                NinePalaceInput(month, day, hour)
            } catch (e: IllegalArgumentException) {
                thrown = true
            }
            assertTrue("應拒絕 month=$month day=$day hour=$hour", thrown)
        }
    }

    @Test
    fun `九宮展示順序涵蓋九卦且不重複`() {
        val order = NinePalaceGua.palaceOrder
        assertEquals(9, order.size)
        assertEquals(9, order.map { it.number }.toSet().size)
        assertEquals(NinePalaceGua.LIU_LIAN, order.first())
        assertEquals(NinePalaceGua.TIAN_DE, order.last())
    }

    @Test
    fun `卦卡文字包含卦名方位五行象意`() {
        val text = NinePalaceGua.DA_AN.toCardText()
        assertTrue(text.contains("大安"))
        assertTrue(text.contains("左方"))
        assertTrue(text.contains("木"))
        assertTrue(text.contains("穩定"))
    }

    @Test
    fun `三卦同象提示主題集中`() {
        val result = NinePalaceCalculator.calculate(month = 1, day = 1, hourIndex = 1)
        assertTrue(NinePalaceInterpreter.describe(result).contains("三卦同象"))
    }

    @Test
    fun `時月同象與時日同象有對應提示`() {
        val monthEqualsHour = NinePalaceCalculator.calculate(month = 3, day = 5, hourIndex = 6)
        assertEquals(monthEqualsHour.monthGua, monthEqualsHour.hourGua)
        assertTrue(NinePalaceInterpreter.describe(monthEqualsHour).contains("時卦與月卦同象"))

        val dayEqualsHour = NinePalaceCalculator.calculate(month = 1, day = 3, hourIndex = 1)
        assertEquals(dayEqualsHour.dayGua, dayEqualsHour.hourGua)
        assertTrue(NinePalaceInterpreter.describe(dayEqualsHour).contains("時卦與日卦同象"))
    }

    @Test
    fun `三卦各異時給出分層讀法`() {
        val result = NinePalaceCalculator.calculate(month = 3, day = 14, hourIndex = 4)
        assertTrue(NinePalaceInterpreter.describe(result).contains("三卦各異"))
    }
}
