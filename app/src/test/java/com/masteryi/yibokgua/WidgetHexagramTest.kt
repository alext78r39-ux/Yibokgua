package com.masteryi.yibokgua

import com.masteryi.yibokgua.widget.WidgetHexagram
import com.masteryi.yibokgua.widget.WidgetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetHexagramTest {

    @Test
    fun dailyId_isStableAndWithinRange() {
        for (day in -100L..1000L) {
            val id = WidgetHexagram.dailyId(day)
            assertTrue("day=$day id=$id", id in 1..64)
        }
        assertEquals(WidgetHexagram.dailyId(20_000L), WidgetHexagram.dailyId(20_000L))
    }

    @Test
    fun dailyId_mapsSequentiallyAndWraps() {
        assertEquals(1, WidgetHexagram.dailyId(0L))
        assertEquals(2, WidgetHexagram.dailyId(1L))
        assertEquals(64, WidgetHexagram.dailyId(63L))
        assertEquals(1, WidgetHexagram.dailyId(64L))
    }

    @Test
    fun dailyId_handlesNegativeEpochDay() {
        assertEquals(64, WidgetHexagram.dailyId(-1L))
        assertEquals(1, WidgetHexagram.dailyId(-64L))
    }

    @Test
    fun resolveId_usesSameDayOverride() {
        val state = WidgetState(overrideId = 7, overrideEpochDay = 100L)
        assertEquals(7, WidgetHexagram.resolveId(state, 100L))
    }

    @Test
    fun resolveId_ignoresStaleOverride() {
        val state = WidgetState(overrideId = 7, overrideEpochDay = 100L)
        assertEquals(WidgetHexagram.dailyId(101L), WidgetHexagram.resolveId(state, 101L))
    }

    @Test
    fun resolveId_ignoresOutOfRangeOverride() {
        val state = WidgetState(overrideId = 99, overrideEpochDay = 100L)
        assertEquals(WidgetHexagram.dailyId(100L), WidgetHexagram.resolveId(state, 100L))
    }

    @Test
    fun resolveId_withoutOverrideUsesDaily() {
        assertEquals(WidgetHexagram.dailyId(50L), WidgetHexagram.resolveId(WidgetState(), 50L))
    }
}
