package com.masteryi.engine.iching

import com.masteryi.core.model.iching.DivinationResult
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.Yao
import kotlin.random.Random

class DivinationEngine(
    private val findHexagramByBinary: (String) -> Hexagram
) {

    /**
     * 1. 時間起卦（梅花易數）
     */
    fun calculateTimeDivination(
        year: Int, month: Int, day: Int, hour: Int, minute: Int
    ): DivinationResult {
        val upperNum = mod8(year + month + day)
        val lowerNum = mod8(hour + minute)
        val movingYaoNum = mod6(year + month + day + hour + minute)

        val upperTrigram = Trigram.fromNumber(upperNum)
        val lowerTrigram = Trigram.fromNumber(lowerNum)

        // 組合 binary (下卦 3 位 + 上卦 3 位)
        val originalBinary = lowerTrigram.binaryCode + upperTrigram.binaryCode
        val originalHexagram = findHexagramByBinary(originalBinary)

        // 建構 6 爻列表
        val yaos = originalBinary.mapIndexed { index, char ->
            val isYang = char == '1'
            val yaoIndex1Based = index + 1
            if (yaoIndex1Based == movingYaoNum) {
                if (isYang) Yao.OLD_YANG else Yao.OLD_YIN
            } else {
                if (isYang) Yao.YOUNG_YANG else Yao.YOUNG_YIN
            }
        }

        // 變卦 binary
        val mutatedBinary = yaos.joinToString("") { if (it.mutate().isYang) "1" else "0" }
        val mutatedHexagram = findHexagramByBinary(mutatedBinary)

        return DivinationResult(
            originalHexagram = originalHexagram,
            mutatedHexagram = mutatedHexagram,
            yaos = yaos,
            movingYaoIndex = movingYaoNum,
            divinationType = "時間起卦（梅花易數）"
        )
    }

    /**
     * 2. 自動搖卦（單爻機率引擎）
     * 機率：老陰 12%、老陽 13%、少陽 37%、少陰 38%
     */
    fun rollSingleYaoAuto(random: Random = Random.Default): Yao {
        val rand = random.nextInt(100) // 0 .. 99
        return when {
            rand < 12 -> Yao.OLD_YIN        // 0..11 (12%)
            rand < 25 -> Yao.OLD_YANG       // 12..24 (13%)
            rand < 62 -> Yao.YOUNG_YANG     // 25..61 (37%)
            else -> Yao.YOUNG_YIN           // 62..99 (38%)
        }
    }

    /**
     * 3. 類象加方位起卦
     */
    fun calculateCategoryDirectionDivination(
        categoryNum: Int,
        directionNum: Int
    ): DivinationResult {
        val upperNum = mod8(categoryNum)
        val lowerNum = mod8(directionNum)
        val movingYaoNum = mod6(categoryNum + directionNum)

        val upperTrigram = Trigram.fromNumber(upperNum)
        val lowerTrigram = Trigram.fromNumber(lowerNum)

        val binary = lowerTrigram.binaryCode + upperTrigram.binaryCode
        val originalHexagram = findHexagramByBinary(binary)

        val yaos = binary.mapIndexed { index, char ->
            val isYang = char == '1'
            if (index + 1 == movingYaoNum) {
                if (isYang) Yao.OLD_YANG else Yao.OLD_YIN
            } else {
                if (isYang) Yao.YOUNG_YANG else Yao.YOUNG_YIN
            }
        }

        val mutatedBinary = yaos.joinToString("") { if (it.mutate().isYang) "1" else "0" }

        return DivinationResult(
            originalHexagram = originalHexagram,
            mutatedHexagram = findHexagramByBinary(mutatedBinary),
            yaos = yaos,
            movingYaoIndex = movingYaoNum,
            divinationType = "類象加方位起卦"
        )
    }

    /**
     * 4. 根據手動/搖卦完成的 6 爻解析卦象
     */
    fun buildResultFrom6Yaos(yaos: List<Yao>, typeName: String = "搖卦起卦"): DivinationResult {
        require(yaos.size == 6) { "六爻必須剛好包含 6 個爻" }

        val originalBinary = yaos.joinToString("") { if (it.isYang) "1" else "0" }
        val mutatedBinary = yaos.joinToString("") { if (it.mutate().isYang) "1" else "0" }

        val hasMoving = yaos.any { it.isMoving }

        return DivinationResult(
            originalHexagram = findHexagramByBinary(originalBinary),
            mutatedHexagram = if (hasMoving) findHexagramByBinary(mutatedBinary) else null,
            yaos = yaos,
            movingYaoIndex = yaos.indexOfFirst { it.isMoving }.takeIf { it != -1 }?.plus(1),
            divinationType = typeName
        )
    }

    private fun mod8(n: Int): Int = valRem(n, 8)
    private fun mod6(n: Int): Int = valRem(n, 6)

    private fun valRem(n: Int, mod: Int): Int {
        val r = n % mod
        return if (r == 0) mod else r
    }
}
