package com.masteryi.yibokgua

import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.Yao
import com.masteryi.data.repository.HexagramRepository
import com.masteryi.engine.iching.DivinationEngine
import com.masteryi.engine.iching.hourBranchNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DivinationEngineTest {

    private val repository = HexagramRepository()
    private val engine = DivinationEngine(findHexagramByBinary = repository::getHexagramByBinary)

    @Test
    fun testTimeDivinationFirstYaoIsMoving() {
        // 上卦 = (1+1+1) % 8 = 3 離；下卦 = (0+0) % 8 -> 0 => 8 坤 => 火地晉
        val result = engine.calculateTimeDivination(year = 1, month = 1, day = 1, hour = 0, minute = 0)
        assertEquals("火地晉", result.originalHexagram.name)
        // 動爻 = mod6(1+1+1) = 3（第三爻）
        assertEquals(3, result.movingYaoIndex)
        assertTrue(result.yaos[2].isMoving)
        assertTrue(result.yaos[1].isMoving.not())
        assertNotNull(result.mutatedHexagram)
    }

    @Test
    fun testRollSingleYaoProbabilityDistribution() {
        val random = Random(42)
        val counts = mutableMapOf<Yao, Int>()
        repeat(100_000) { counts.merge(engine.rollSingleYao(random), 1, Int::plus) }
        val tolerance = 0.01
        // 傳統三錢法精確機率：老陰 1/8、少陽 3/8、少陰 3/8、老陽 1/8
        assertEquals(0.125, counts[Yao.OLD_YIN]!! / 100_000.0, tolerance)
        assertEquals(0.375, counts[Yao.YOUNG_YANG]!! / 100_000.0, tolerance)
        assertEquals(0.375, counts[Yao.YOUNG_YIN]!! / 100_000.0, tolerance)
        assertEquals(0.125, counts[Yao.OLD_YANG]!! / 100_000.0, tolerance)
    }

    @Test
    fun testOldYinMutatesToYoungYang() {
        val yaos = listOf(
            Yao.OLD_YIN, Yao.YOUNG_YANG, Yao.YOUNG_YIN,
            Yao.YOUNG_YANG, Yao.YOUNG_YIN, Yao.YOUNG_YANG
        )
        val result = engine.buildResultFrom6Yaos(yaos)
        assertEquals(1, result.movingYaoIndex)
        assertNotNull(result.mutatedHexagram)
        val original = result.originalHexagram.binaryCode
        val mutated = result.mutatedHexagram!!.binaryCode
        // 初爻 (index 0) 由陰變陽
        assertEquals("1" + original.substring(1), mutated)
    }

    @Test
    fun testNoMovingYaoHasNoMutatedHexagram() {
        val yaos = listOf(
            Yao.YOUNG_YANG, Yao.YOUNG_YIN, Yao.YOUNG_YANG,
            Yao.YOUNG_YIN, Yao.YOUNG_YANG, Yao.YOUNG_YIN
        )
        val result = engine.buildResultFrom6Yaos(yaos)
        assertNull(result.mutatedHexagram)
        assertNull(result.movingYaoIndex)
    }

    @Test
    fun testRepositoryHasAll64UniqueHexagrams() {
        assertEquals(64, repository.getAllHexagrams().size)
        val binaries = repository.getAllHexagrams().map { it.binaryCode }
        assertEquals(binaries.size, binaries.distinct().size)
    }

    @Test
    fun testTrigramBinaryAndNumberConversion() {
        assertEquals(Trigram.fromBinary("010"), Trigram.KAN)
        assertEquals(Trigram.fromNumber(8), Trigram.KUN)
        assertEquals(Trigram.fromNumber(16), Trigram.KUN)
    }

    @Test
    fun testLunarTimeDivinationUsesBranchAndLunarDate() {
        // 年支子(1)、農曆正月(1)、初一(1)、時支子(1)
        // 上卦 = mod8(1+1+1) = 3 離；下卦 = mod8(1+1+1+1) = 4 震 => 火雷噬嗑
        // 動爻 = mod6(1+1+1+1) = 4（第四爻）
        val result = engine.calculateLunarTimeDivination(
            yearBranchNum = 1, lunarMonth = 1, lunarDay = 1, hourBranchNum = 1
        )
        assertEquals("火雷噬嗑", result.originalHexagram.name)
        assertEquals(4, result.movingYaoIndex)
        assertTrue(result.yaos[3].isMoving)
        assertNotNull(result.mutatedHexagram)
        assertEquals("時間起卦（農曆）", result.divinationType)
    }

    @Test
    fun testLunarTimeDivinationZeroMapsToMax() {
        // 年支子(1)、正月(1)、初六(6)、時支子(1)：1+1+6 = 8 => 上卦 mod8 = 8 坤
        // 下卦 = mod8(8+1) = 1 乾 => 地天泰；動爻 = mod6(9) = 3（第三爻）
        val result = engine.calculateLunarTimeDivination(
            yearBranchNum = 1, lunarMonth = 1, lunarDay = 6, hourBranchNum = 1
        )
        assertEquals("地天泰", result.originalHexagram.name)
        assertEquals(3, result.movingYaoIndex)
    }

    @Test
    fun testLunarTimeDivinationAllTwelveBranchesValid() {
        // 年支亥(12)、農曆十二月(12)、三十(30)、時支亥(12)：54 => 上卦 mod8 = 6 坎
        // 下卦 = mod8(54+12) = mod8(66) = 2 兌 => 水澤節；動爻 = mod6(66) = 6（上爻）
        val result = engine.calculateLunarTimeDivination(
            yearBranchNum = 12, lunarMonth = 12, lunarDay = 30, hourBranchNum = 12
        )
        assertEquals("水澤節", result.originalHexagram.name)
        assertEquals(6, result.movingYaoIndex)
    }

    @Test
    fun testHourBranchNumberMapping() {
        assertEquals(1, hourBranchNumber(23))  // 子時 23:00–00:59
        assertEquals(1, hourBranchNumber(0))
        assertEquals(2, hourBranchNumber(1))   // 丑時
        assertEquals(2, hourBranchNumber(2))
        assertEquals(3, hourBranchNumber(3))   // 寅時
        assertEquals(7, hourBranchNumber(12))  // 午時
        assertEquals(12, hourBranchNumber(21)) // 亥時
        assertEquals(12, hourBranchNumber(22))
    }

    @Test
    fun testCategoryDirectionDivination() {
        val result = engine.calculateCategoryDirectionDivination(categoryNum = 1, directionNum = 1)
        // 上卦 乾(1)、下卦 乾(1) => 乾為天；動爻 = mod6(1+1) = 2（第二爻）
        assertEquals("乾為天", result.originalHexagram.name)
        assertEquals(2, result.movingYaoIndex)
        // 第二爻陽變陰 => 101111 = 天火同人
        assertEquals("天火同人", result.mutatedHexagram!!.name)
    }
}
