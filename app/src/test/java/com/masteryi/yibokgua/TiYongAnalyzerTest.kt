package com.masteryi.yibokgua

import com.masteryi.core.model.iching.TiYongRelation
import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.Yao
import com.masteryi.data.repository.HexagramRepository
import com.masteryi.engine.iching.DivinationEngine
import com.masteryi.engine.iching.TiYongAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TiYongAnalyzerTest {

    private val repository = HexagramRepository()
    private val engine = DivinationEngine(findHexagramByBinary = repository::getHexagramByBinary)
    private val analyzer = TiYongAnalyzer(findHexagramByBinary = repository::getHexagramByBinary)

    private fun yaos(moving: List<Int> = emptyList(), binary: String): List<Yao> =
        binary.mapIndexed { index, char ->
            val yang = char == '1'
            when {
                index in moving && yang -> Yao.OLD_YANG
                index in moving && !yang -> Yao.OLD_YIN
                yang -> Yao.YOUNG_YANG
                else -> Yao.YOUNG_YIN
            }
        }

    /** 山火賁：九三動變為山雷頤，用卦離火生體卦艮為吉，變爻震木剋制體土，故先順利後坎坷 */
    @Test
    fun testBenYaNineThreeMutatesToShanLeiYi() {
        val result = engine.buildResultFrom6Yaos(yaos(moving = listOf(2), binary = "101001"))

        assertEquals("山火賁", result.originalHexagram.name)
        assertEquals("山雷頤", result.mutatedHexagram!!.name)

        val analysis = analyzer.analyze(result)

        // 動爻在下卦，故體為上卦艮、用為下卦離
        assertEquals(Trigram.GEN, analysis.bodyTrigram)
        assertEquals(Trigram.LI, analysis.appTrigram)
        assertFalse(analysis.bodyInLower)

        // 用卦離火生體卦艮土 => 用生體，吉
        assertEquals(TiYongRelation.YONG_SHENG_TI, analysis.mainRelation)
        assertTrue(analysis.mainRelation.isAuspicious)

        // 互卦：二三四爻為坎（下互），三四五爻為震（上互）=> 雷水解
        assertEquals(Trigram.KAN, analysis.lowerMutual)
        assertEquals(Trigram.ZHEN, analysis.upperMutual)
        assertEquals("雷水解", analysis.mutualHexagram.name)
        // 體在上卦，故上互為體互、下互為用互
        assertEquals(Trigram.ZHEN, analysis.bodyMutual)
        assertEquals(Trigram.KAN, analysis.appMutual)

        // 變卦用位（下卦）為震木，剋制體卦艮土 => 用克體，凶
        assertEquals(Trigram.ZHEN, analysis.changedAppTrigram)
        assertEquals(TiYongRelation.YONG_KE_TI, analysis.changedRelation)
        assertFalse(analysis.changedRelation!!.isAuspicious)

        // 用卦由吉變凶，主先順利而後坎坷
        assertTrue(analysis.trendText.contains("由吉變凶"))
    }

    /** 水火既濟：上爻動，本卦用坎水剋體離火為凶，變卦用位巽木生體離火為吉，故先兇後吉 */
    @Test
    fun testJiJiUpperYaoMutatesToFengHuoJiaRen() {
        val result = engine.buildResultFrom6Yaos(yaos(moving = listOf(5), binary = "101010"))

        assertEquals("水火既濟", result.originalHexagram.name)
        assertEquals("風火家人", result.mutatedHexagram!!.name)

        val analysis = analyzer.analyze(result)

        // 動爻在上卦，故體為下卦離、用為上卦坎
        assertEquals(Trigram.LI, analysis.bodyTrigram)
        assertEquals(Trigram.KAN, analysis.appTrigram)
        assertTrue(analysis.bodyInLower)

        // 用卦坎水剋體卦離火 => 用克體，凶
        assertEquals(TiYongRelation.YONG_KE_TI, analysis.mainRelation)
        assertFalse(analysis.mainRelation.isAuspicious)

        // 互卦：下互坎、上互離 => 火水未濟；體在下卦，故下互坎為體互、上互離為用互
        assertEquals(Trigram.KAN, analysis.lowerMutual)
        assertEquals(Trigram.LI, analysis.upperMutual)
        assertEquals("火水未濟", analysis.mutualHexagram.name)
        assertEquals(Trigram.KAN, analysis.bodyMutual)
        assertEquals(Trigram.LI, analysis.appMutual)

        // 變卦用位（上卦）為巽木，生體卦離火 => 用生體，吉
        assertEquals(Trigram.XUN, analysis.changedAppTrigram)
        assertEquals(TiYongRelation.YONG_SHENG_TI, analysis.changedRelation)
        assertTrue(analysis.changedRelation!!.isAuspicious)

        // 用卦由凶變吉，主先坎坷而後順利
        assertTrue(analysis.trendText.contains("由凶變吉"))
    }

    /** 無動爻時：內卦下卦為體、外卦上卦為用，無變卦 */
    @Test
    fun testNoMovingYaoDefaultsLowerAsBody() {
        val result = engine.buildResultFrom6Yaos(yaos(binary = "101001"))

        assertNull(result.mutatedHexagram)
        assertNull(result.movingYaoIndex)

        val analysis = analyzer.analyze(result)

        assertEquals(Trigram.LI, analysis.bodyTrigram)
        assertEquals(Trigram.GEN, analysis.appTrigram)
        assertTrue(analysis.bodyInLower)

        // 體離火生用艮土 => 體生用，有耗失之患
        assertEquals(TiYongRelation.TI_SHENG_YONG, analysis.mainRelation)
        assertFalse(analysis.mainRelation.isAuspicious)
        assertNull(analysis.changedRelation)
        assertTrue(analysis.changedText.contains("不成變卦"))
    }

    /** 體用比和：乾為天九二動，體用皆乾金，比和主百事順遂 */
    @Test
    fun testBiHeRelation() {
        val result = engine.buildResultFrom6Yaos(yaos(moving = listOf(1), binary = "111111"))

        val analysis = analyzer.analyze(result)

        assertEquals(Trigram.QIAN, analysis.bodyTrigram)
        assertEquals(Trigram.QIAN, analysis.appTrigram)
        assertEquals(TiYongRelation.BI_HE, analysis.mainRelation)
        assertTrue(analysis.mainRelation.isAuspicious)
        // 變卦天火同人，用位（下卦）離火剋體卦乾金
        assertEquals(Trigram.LI, analysis.changedAppTrigram)
        assertEquals(TiYongRelation.YONG_KE_TI, analysis.changedRelation)
    }

    /** 體黨用黨統計：賁九三動，體艮土、用離火，互變中體黨為變卦上卦艮土 */
    @Test
    fun testPartyCountBen() {
        val result = engine.buildResultFrom6Yaos(yaos(moving = listOf(2), binary = "101001"))
        val analysis = analyzer.analyze(result)

        // 候選卦：體互震木、用互坎水、變卦下震木、變卦上艮土
        assertEquals(1, analysis.bodyPartyCount)
        assertEquals(0, analysis.appPartyCount)
        assertTrue(analysis.partyText.contains("體黨多而體勢盛"))
    }

    /** 64卦庫靜態查詢 tiYong() 與動態分析器結果一致 */
    @Test
    fun testStaticTiYongConsistentWithAnalyzer() {
        val moving = engine.buildResultFrom6Yaos(yaos(moving = listOf(2), binary = "101001"))
        val movingAnalysis = analyzer.analyze(moving)
        val movingStatic = moving.originalHexagram.tiYong(moving.movingYaoIndex!!)
        assertEquals(movingAnalysis.bodyTrigram, movingStatic.bodyTrigram)
        assertEquals(movingAnalysis.appTrigram, movingStatic.appTrigram)
        assertEquals(movingAnalysis.mainRelation, movingStatic.relation)

        val noMoving = engine.buildResultFrom6Yaos(yaos(binary = "101001"))
        val noMovingAnalysis = analyzer.analyze(noMoving)
        val noMovingStatic = noMoving.originalHexagram.tiYong(null)
        assertEquals(noMovingAnalysis.bodyTrigram, noMovingStatic.bodyTrigram)
        assertEquals(noMovingAnalysis.appTrigram, noMovingStatic.appTrigram)
        assertEquals(noMovingAnalysis.mainRelation, noMovingStatic.relation)
    }
}
