package com.masteryi.engine.iching

import com.masteryi.core.model.iching.DivinationResult
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.Yao
import java.lang.Character.UnicodeScript
import kotlin.random.Random

/** 統計字串中包含的中文字（漢字）字數 */
fun countChineseCharacters(text: String): Int =
    text.codePoints().filter { UnicodeScript.of(it) == UnicodeScript.HAN }.count().toInt()

/** 十二地支名稱（索引 0 = 子 … 11 = 亥） */
val EARTHLY_BRANCH_NAMES: List<String> =
    listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

/** 由 24 小時制時數求時支序（子1 丑2 … 亥12）；子時跨 23:00–00:59 */
fun hourBranchNumber(hour: Int): Int = ((hour + 1) / 2) % 12 + 1

class DivinationEngine(
    private val findHexagramByBinary: (String) -> Hexagram
) {

    /**
     * 1. 時間起卦（國曆）
     * 以國曆年月日時分取數（非傳統梅花曆法，保留供對照）：
     * - 上卦 = (年 + 月 + 日) mod 8
     * - 下卦 = (時 + 分) mod 8
     * - 動爻 = (年 + 月 + 日 + 時 + 分) mod 6
     */
    fun calculateTimeDivination(
        year: Int, month: Int, day: Int, hour: Int, minute: Int
    ): DivinationResult {
        val upperNum = mod8(year + month + day)
        val lowerNum = mod8(hour + minute)
        val movingYaoNum = mod6(year + month + day + hour + minute)
        return buildDivinationFromNums(upperNum, lowerNum, movingYaoNum, "時間起卦（國曆）")
    }

    /**
     * 1b. 時間起卦（農曆・梅花易數）
     * 傳統取數以「年支、農曆月、農曆日、時支」為準：
     * - 上卦 = (年支 + 農曆月 + 農曆日) mod 8
     * - 下卦 = (年支 + 農曆月 + 農曆日 + 時支) mod 8
     * - 動爻 = (年支 + 農曆月 + 農曆日 + 時支) mod 6
     * 地支序：子1 丑2 寅3 卯4 辰5 巳6 午7 未8 申9 酉10 戌11 亥12
     */
    fun calculateLunarTimeDivination(
        yearBranchNum: Int,
        lunarMonth: Int,
        lunarDay: Int,
        hourBranchNum: Int
    ): DivinationResult {
        require(yearBranchNum in 1..12) { "年支序必須在 1~12" }
        require(lunarMonth in 1..12) { "農曆月必須在 1~12" }
        require(lunarDay in 1..30) { "農曆日必須在 1~30" }
        require(hourBranchNum in 1..12) { "時支序必須在 1~12" }

        val base = yearBranchNum + lunarMonth + lunarDay
        val upperNum = mod8(base)
        val lowerNum = mod8(base + hourBranchNum)
        val movingYaoNum = mod6(base + hourBranchNum)
        return buildDivinationFromNums(upperNum, lowerNum, movingYaoNum, "時間起卦（農曆）")
    }

    /**
     * 2. 搖一爻（三錢法）
     * 依傳統三錢法：每枚銅錢擲得「陽面 3」或「陰面 2」，三枚之和決定該爻：
     * - 6（陰陰陰）老陰，機率 1/8 = 12.5%
     * - 7（陰陰陽）少陽，機率 3/8 = 37.5%
     * - 8（陰陽陽）少陰，機率 3/8 = 37.5%
     * - 9（陽陽陽）老陽，機率 1/8 = 12.5%
     */
    fun rollSingleYao(random: Random = Random.Default): Yao {
        var sum = 0
        repeat(3) { sum += if (random.nextBoolean()) 3 else 2 }
        return when (sum) {
            6 -> Yao.OLD_YIN        // 1/8
            7 -> Yao.YOUNG_YANG     // 3/8
            8 -> Yao.YOUNG_YIN      // 3/8
            else -> Yao.OLD_YANG    // 9 -> 1/8
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
     * 4. 根據手動/快速搖卦完成的 6 爻解析卦象
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

    /**
     * 5. 字占起卦（用戶輸入 1-10 個中文字成卦）
     * 依據多字分組法則（前少後多 / 天輕地重）：
     * - 偶數字：均分；奇數字：前少後多；11字以上亦採前少後多原則
     * - 上卦 = 前段文字總筆畫 mod 8（若為 0 則取 8）
     * - 下卦 = 後段文字總筆畫 mod 8（若為 0 則取 8）
     * - 動爻 = (全句文字總筆畫) mod 6（若為 0 則取 6）
     */
    fun calculateCharacterDivination(text: String, random: Random = Random.Default): DivinationResult {
        val codePoints = mutableListOf<Int>()
        text.codePoints().forEach { cp ->
            if (UnicodeScript.of(cp) == UnicodeScript.HAN) {
                codePoints.add(cp)
            }
        }
        val count = codePoints.size
        require(count in 1..10) { "字占限 1-10 個中文字" }

        if (count == 1) {
            return calculateSingleCharacterDivination(text)
        }

        // 前少後多
        val frontSz = if (count % 2 == 0) count / 2 else (count - 1) / 2

        val frontText = buildString {
            for (i in 0 until frontSz) {
                appendCodePoint(codePoints[i])
            }
        }
        val backText = buildString {
            for (i in frontSz until count) {
                appendCodePoint(codePoints[i])
            }
        }

        val frontStrokes = ChineseStrokeCounter.totalStrokes(frontText)
        val backStrokes = ChineseStrokeCounter.totalStrokes(backText)
        val totalStrokes = ChineseStrokeCounter.totalStrokes(text)

        val upperNum = mod8(frontStrokes)
        val lowerNum = mod8(backStrokes)
        val movingYaoNum = mod6(totalStrokes)

        return buildDivinationFromNums(upperNum, lowerNum, movingYaoNum, "字占起卦")
    }

    private fun buildDivinationFromNums(
        upperNum: Int,
        lowerNum: Int,
        movingYaoNum: Int,
        divinationTypeName: String = "起卦"
    ): DivinationResult {
        val upperTrigram = Trigram.fromNumber(upperNum)
        val lowerTrigram = Trigram.fromNumber(lowerNum)

        val originalBinary = lowerTrigram.binaryCode + upperTrigram.binaryCode
        val originalHexagram = findHexagramByBinary(originalBinary)

        val yaos = originalBinary.mapIndexed { index, char ->
            val isYang = char == '1'
            val yaoIndex1Based = index + 1
            if (yaoIndex1Based == movingYaoNum) {
                if (isYang) Yao.OLD_YANG else Yao.OLD_YIN
            } else {
                if (isYang) Yao.YOUNG_YANG else Yao.YOUNG_YIN
            }
        }

        val mutatedBinary = yaos.joinToString("") { if (it.mutate().isYang) "1" else "0" }
        val mutatedHexagram = findHexagramByBinary(mutatedBinary)

        return DivinationResult(
            originalHexagram = originalHexagram,
            mutatedHexagram = mutatedHexagram,
            yaos = yaos,
            movingYaoIndex = movingYaoNum,
            divinationType = divinationTypeName
        )
    }

    /**
     * 一字占：邵子一字拆筆法
     * - 可拆結構（左右/上下/包圍）：左/上/外筆畫為上卦，右/下/內筆畫為下卦，總筆畫 mod 6 為動爻
     * - 獨體字/不可拆結構：依筆畫數前少後多拆兩半
     */
    private fun calculateSingleCharacterDivination(text: String): DivinationResult {
        val codePoints = mutableListOf<Int>()
        text.codePoints().forEach { cp ->
            if (UnicodeScript.of(cp) == UnicodeScript.HAN) {
                codePoints.add(cp)
            }
        }
        require(codePoints.size == 1) { "單字占必須為 1 個中文字" }

        val codePoint = codePoints[0]
        val totalStrokes = ChineseStrokeCounter.getStrokes(codePoint)

        // 簡化拆分：基於常用拆字簡易規則（若左右結構傾向）
        // 常見左右結構字：例如「明」「林」「河」等 - 簡化為左右拆
        // 下方用前少後多作保守處理
        val leftStrokes = totalStrokes / 2
        val rightStrokes = totalStrokes - leftStrokes

        val upperNum = mod8(leftStrokes)
        val lowerNum = mod8(rightStrokes)
        val movingYaoNum = mod6(totalStrokes)

        return buildDivinationFromNums(upperNum, lowerNum, movingYaoNum, "字占起卦")
    }

    private fun mod8(n: Int): Int = valRem(n, 8)
    private fun mod6(n: Int): Int = valRem(n, 6)

    private fun valRem(n: Int, mod: Int): Int {
        val r = ((n % mod) + mod) % mod
        return if (r == 0) mod else r
    }
}
