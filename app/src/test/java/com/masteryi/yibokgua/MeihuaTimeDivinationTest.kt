package com.masteryi.yibokgua

import com.masteryi.core.model.iching.LunarDate
import com.masteryi.core.model.iching.Trigram
import com.masteryi.data.repository.HexagramRepository
import com.masteryi.engine.iching.DivinationEngine
import com.masteryi.engine.iching.hourBranchNumber
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MeihuaTimeDivinationTest {

    private val repository = HexagramRepository()
    private val engine = DivinationEngine(findHexagramByBinary = repository::getHexagramByBinary)

    @Test
    fun calculateMeihuaTimeDivination_knownDateAndHour() {
        // 2026-08-15 = 丙午年七月初三；12 時 = 午時（年支午7、月7、日3、時7）
        // S1 = 17 => 上卦 mod8 = 1 乾；S2 = 24 => 下卦 mod8 = 8 坤；動爻 mod6 = 6
        val calculation = engine.calculateMeihuaTimeCalculation(LocalDate.of(2026, 8, 15), hour = 12)
        val result = calculation.result

        assertEquals(7, calculation.yearBranchNum)
        assertEquals(7, calculation.lunarMonth)
        assertEquals(3, calculation.lunarDay)
        assertEquals(7, calculation.hourBranchNum)
        assertEquals(17, calculation.sumBase)
        assertEquals(1, calculation.upperNum)
        assertEquals(24, calculation.sumTotal)
        assertEquals(8, calculation.lowerNum)
        assertEquals(6, calculation.movingYaoNum)

        assertEquals(Trigram.QIAN, result.originalHexagram.upperTrigram)
        assertEquals(Trigram.KUN, result.originalHexagram.lowerTrigram)
        assertEquals("天地否", result.originalHexagram.name)
        assertEquals(6, result.movingYaoIndex)
        assertEquals("澤地萃", result.mutatedHexagram?.name)
    }

    @Test
    fun calculateMeihuaTimeDivination_23時取次日子時() {
        val result = engine.calculateMeihuaTimeDivination(LocalDate.of(2026, 8, 15), hour = 23)
        val expected = engine.calculateLunarTimeDivination(
            yearBranchNum = 7, lunarMonth = 7, lunarDay = 4, hourBranchNum = 1
        )

        assertEquals(expected.originalHexagram.name, result.originalHexagram.name)
        assertEquals(expected.movingYaoIndex, result.movingYaoIndex)
        assertEquals(expected.mutatedHexagram?.name, result.mutatedHexagram?.name)
    }

    @Test
    fun calculateMeihuaTimeDivination_22時不跨日() {
        val result = engine.calculateMeihuaTimeDivination(LocalDate.of(2026, 8, 15), hour = 22)
        val expected = engine.calculateLunarTimeDivination(
            yearBranchNum = 7, lunarMonth = 7, lunarDay = 3, hourBranchNum = 12
        )

        assertEquals(expected.originalHexagram.name, result.originalHexagram.name)
        assertEquals(expected.movingYaoIndex, result.movingYaoIndex)
    }

    @Test
    fun calculateMeihuaTimeDivination_閏月沿用原月序() {
        val date = LocalDate.of(2025, 7, 25)
        val lunar = LunarDate.of(date)
        assertEquals(true, lunar.isLeap)

        val result = engine.calculateMeihuaTimeDivination(date, hour = 12)
        val expected = engine.calculateLunarTimeDivination(
            yearBranchNum = lunar.yearBranchNumber,
            lunarMonth = lunar.month,
            lunarDay = lunar.day,
            hourBranchNum = hourBranchNumber(12)
        )

        assertEquals(expected.originalHexagram.name, result.originalHexagram.name)
        assertEquals(expected.movingYaoIndex, result.movingYaoIndex)
    }

    @Test
    fun calculateMeihuaTimeDivination_23時遇到月末進到次月() {
        val monthEnd = generateSequence(LocalDate.of(2026, 1, 1)) { it.plusDays(1) }
            .first { LunarDate.of(it).day == 30 }
        val nextLunar = LunarDate.of(monthEnd.plusDays(1))
        assertEquals(1, nextLunar.day)

        val result = engine.calculateMeihuaTimeDivination(monthEnd, hour = 23)
        val expected = engine.calculateLunarTimeDivination(
            yearBranchNum = nextLunar.yearBranchNumber,
            lunarMonth = nextLunar.month,
            lunarDay = nextLunar.day,
            hourBranchNum = 1
        )

        assertEquals(expected.originalHexagram.name, result.originalHexagram.name)
        assertEquals(expected.movingYaoIndex, result.movingYaoIndex)
        assertEquals(expected.mutatedHexagram?.name, result.mutatedHexagram?.name)
    }
}
