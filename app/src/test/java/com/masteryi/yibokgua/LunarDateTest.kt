package com.masteryi.yibokgua

import com.masteryi.core.model.iching.LunarDate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class LunarDateTest {

    @Test
    fun of_2025春節() {
        val lunar = LunarDate.of(LocalDate.of(2025, 1, 29))
        assertEquals(2025, lunar.year)
        assertEquals(1, lunar.month)
        assertEquals(false, lunar.isLeap)
        assertEquals(1, lunar.day)
        assertEquals("乙巳", lunar.ganZhiYear)
    }

    @Test
    fun of_2026春節() {
        val lunar = LunarDate.of(LocalDate.of(2026, 2, 17))
        assertEquals(2026, lunar.year)
        assertEquals(1, lunar.month)
        assertEquals(1, lunar.day)
        assertEquals("丙午", lunar.ganZhiYear)
    }

    @Test
    fun of_2026八月十五() {
        // 對應參考圖：2026-08-15 = 丙午年七月初三
        val lunar = LunarDate.of(LocalDate.of(2026, 8, 15))
        assertEquals("丙午年七月初三", lunar.text)
    }

    @Test
    fun of_2024春節() {
        val lunar = LunarDate.of(LocalDate.of(2024, 2, 10))
        assertEquals("甲辰年正月初一", lunar.text)
    }

    @Test
    fun of_2025閏六月存在() {
        // 2025 有閏六月
        val lunar = LunarDate.of(LocalDate.of(2025, 7, 25))
        assertEquals(6, lunar.month)
        assertEquals(true, lunar.isLeap)
    }

    @Test
    fun of_2025中秋() {
        val lunar = LunarDate.of(LocalDate.of(2025, 10, 6))
        assertEquals("乙巳年八月十五", lunar.text)
    }

    @Test
    fun yearBranchNumber_丙午年為午() {
        // 2026-02-17 = 丙午年正月初一，午為第 7 支
        val lunar = LunarDate.of(LocalDate.of(2026, 2, 17))
        assertEquals(7, lunar.yearBranchNumber)
    }

    @Test
    fun yearBranchNumber_乙巳年為巳() {
        // 2025-01-29 = 乙巳年正月初一，巳為第 6 支
        val lunar = LunarDate.of(LocalDate.of(2025, 1, 29))
        assertEquals(6, lunar.yearBranchNumber)
    }
}