package com.masteryi.yibokgua

import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.Yao
import com.masteryi.data.repository.HexagramRepository
import com.masteryi.engine.iching.DivinationEngine
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
    fun testRollSingleYaoAutoProbabilityDistribution() {
        val random = Random(42)
        val counts = mutableMapOf<Yao, Int>()
        repeat(100_000) { counts.merge(engine.rollSingleYaoAuto(random), 1, Int::plus) }
        val tolerance = 0.01
        assertEquals(0.12, counts[Yao.OLD_YIN]!! / 100_000.0, tolerance)
        assertEquals(0.13, counts[Yao.OLD_YANG]!! / 100_000.0, tolerance)
        assertEquals(0.37, counts[Yao.YOUNG_YANG]!! / 100_000.0, tolerance)
        assertEquals(0.38, counts[Yao.YOUNG_YIN]!! / 100_000.0, tolerance)
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
    fun testCategoryDirectionDivination() {
        val result = engine.calculateCategoryDirectionDivination(categoryNum = 1, directionNum = 1)
        // 上卦 乾(1)、下卦 乾(1) => 乾為天；動爻 = mod6(1+1) = 2（第二爻）
        assertEquals("乾為天", result.originalHexagram.name)
        assertEquals(2, result.movingYaoIndex)
        // 第二爻陽變陰 => 101111 = 天火同人
        assertEquals("天火同人", result.mutatedHexagram!!.name)
    }
}
