package com.masteryi.yibokgua

import com.masteryi.core.model.iching.TiYongRelation
import com.masteryi.core.model.iching.Trigram
import com.masteryi.data.repository.HexagramRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HexagramDataTest {

    private val repository = HexagramRepository()

    @Test
    fun testAll64HexagramsHaveCompleteData() {
        val hexagrams = repository.getAllHexagrams()
        assertEquals(64, hexagrams.size)
        assertEquals(64, hexagrams.map { it.binaryCode }.distinct().size)

        hexagrams.forEach { hexagram ->
            assertTrue("第${hexagram.id}卦 tuanZhuan 不可為空", hexagram.tuanZhuan.isNotBlank())
            val xiangCount = if (hexagram.id == 1 || hexagram.id == 2) 7 else 6
            assertEquals("第${hexagram.id}卦 分爻小象傳數量應為 $xiangCount", xiangCount, hexagram.yaoXiangZhuan.size)
            assertTrue("第${hexagram.id}卦 爻辭數量應為 6", hexagram.lineTexts.size >= 6)
            if (hexagram.id <= 50) {
                assertTrue("第${hexagram.id}卦 歷代注解不可為空", hexagram.commentary.isNotBlank())
            } else {
                assertTrue("第${hexagram.id}卦 淺釋不可為空", hexagram.interpretation.isNotBlank())
            }
        }
    }

    @Test
    fun testWuXingRelationMatchesPlumBlossomRules() {
        val expected = mapOf(
            1 to "體用比和", 2 to "體用比和", 3 to "用生體", 4 to "用克體",
            5 to "體生用", 6 to "用生體", 7 to "用克體", 8 to "體克用",
            9 to "體克用", 10 to "體用比和", 11 to "用生體", 12 to "體生用",
            13 to "體克用", 14 to "用克體", 15 to "體用比和", 16 to "用克體",
            17 to "用克體", 18 to "體克用", 19 to "用生體", 20 to "用克體",
            21 to "體生用", 22 to "體生用", 23 to "體用比和", 24 to "體克用",
            25 to "用克體", 26 to "用生體", 27 to "體克用", 28 to "用克體",
            29 to "體用比和", 30 to "體用比和", 31 to "體生用", 32 to "體用比和",
            33 to "體生用", 34 to "體克用", 35 to "用生體", 36 to "體生用",
            37 to "用生體", 38 to "用克體", 39 to "體克用", 40 to "體生用",
            41 to "用生體", 42 to "體用比和", 43 to "體用比和", 44 to "用克體",
            45 to "體生用", 46 to "體克用", 47 to "用生體", 48 to "用生體",
            49 to "體克用", 50 to "體生用", 51 to "體用比和", 52 to "體用比和",
            53 to "用克體", 54 to "體克用", 55 to "用生體", 56 to "用生體",
            57 to "體用比和", 58 to "體用比和", 59 to "體生用", 60 to "體生用",
            61 to "體克用", 62 to "用克體", 63 to "用克體", 64 to "體克用"
        )
        expected.forEach { (hexId, relation) ->
            val hexagram = repository.getHexagramById(hexId)!!
            assertEquals("第${hexId}卦 五行關係錯誤", relation, hexagram.wuXingRelation)
        }
    }

    @Test
    fun testHexagramSymbolFollowsKingWenUnicodeOrder() {
        val hexagrams = repository.getAllHexagrams()
        hexagrams.forEach { hexagram ->
            val expected = (0x4DC0 + hexagram.id - 1).toChar()
            assertEquals("第${hexagram.id}卦 六爻卦象符號錯誤", expected, hexagram.symbol.first())
        }
    }

    @Test
    fun testTiYongLogicMatchesPlumBlossomRules() {
        // 山火賁：下卦離火、上卦艮土
        val ben = repository.getHexagramById(22)!!

        // 無動爻（默認）/ 動爻在上卦：內卦下卦為體、外卦上卦為用 => 體生用（火生土）
        val default = ben.tiYong()
        assertEquals(Trigram.LI, default.bodyTrigram)
        assertEquals(Trigram.GEN, default.appTrigram)
        assertEquals(TiYongRelation.TI_SHENG_YONG, default.relation)
        // 動爻在上卦（四・五・六爻）與無動爻結果一致
        assertEquals(default, ben.tiYong(4))
        assertEquals(ben.tiYong(4), ben.tiYong(5))
        assertEquals(ben.tiYong(4), ben.tiYong(6))

        // 動爻在下卦（初・二・三爻）：外卦上卦為體、內卦下卦為用 => 用生體（火生土）
        val lowerMoving = ben.tiYong(2)
        assertEquals(Trigram.GEN, lowerMoving.bodyTrigram)
        assertEquals(Trigram.LI, lowerMoving.appTrigram)
        assertEquals(TiYongRelation.YONG_SHENG_TI, lowerMoving.relation)
        assertEquals(ben.tiYong(1), lowerMoving)
        assertEquals(ben.tiYong(3), lowerMoving)

        // 乾為天：體用皆乾金 => 比和
        val qian = repository.getHexagramById(1)!!
        assertEquals(TiYongRelation.BI_HE, qian.tiYong().relation)
        assertEquals(TiYongRelation.BI_HE, qian.tiYong(2).relation)

        // 水火既濟：下離火、上坎水，動爻在上卦 => 用克體（坎水剋離火）
        val jiji = repository.getHexagramById(63)!!
        val upperMoving = jiji.tiYong(5)
        assertEquals(Trigram.LI, upperMoving.bodyTrigram)
        assertEquals(Trigram.KAN, upperMoving.appTrigram)
        assertEquals(TiYongRelation.YONG_KE_TI, upperMoving.relation)
    }

    @Test
    fun testKeyHexagramSymbols() {
        val expected = mapOf(
            1 to "䷀", 2 to "䷁", 11 to "䷊", 29 to "䷜", 30 to "䷝",
            51 to "䷲", 57 to "䷸", 58 to "䷹", 63 to "䷾", 64 to "䷿"
        )
        expected.forEach { (id, symbol) ->
            val hexagram = repository.getHexagramById(id)!!
            assertEquals("第${id}卦 六爻卦象符號錯誤", symbol, hexagram.symbol)
        }
    }
}
