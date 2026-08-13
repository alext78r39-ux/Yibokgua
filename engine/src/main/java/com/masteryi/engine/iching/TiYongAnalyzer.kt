package com.masteryi.engine.iching

import com.masteryi.core.model.iching.DivinationResult
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.core.model.iching.TiYongAnalysis
import com.masteryi.core.model.iching.TiYongRelation
import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.Yao

/**
 * 梅花易數體用生克分析器
 *
 * 內卦（下卦）為體，外卦（上卦）為用；有動爻之卦為用，無動爻之卦為體。
 * 互卦：去除主卦初、上兩爻後，二、三、四爻為下互，三、四、五爻為上互；
 * 體在下卦則下互為體互、上互為用互；體在上卦則上互為體互、下互為用互。
 * 體為主、用為事，互卦為事之中間，變卦為事之終。
 */
class TiYongAnalyzer(
    private val findHexagramByBinary: (String) -> Hexagram
) {

    fun analyze(result: DivinationResult): TiYongAnalysis {
        val yaos = result.yaos
        require(yaos.size == 6) { "六爻必須剛好包含 6 個爻" }

        val original = result.originalHexagram
        val lowerTrigram = original.lowerTrigram
        val upperTrigram = original.upperTrigram

        // 定體用：動爻所在之卦為用，無動爻之卦為體；無動爻（或兩卦皆有動爻）時，
        // 內卦下卦為體、外卦上卦為用。
        val movingInLower = yaos.take(3).any { it.isMoving }
        val movingInUpper = yaos.drop(3).any { it.isMoving }
        val bodyTrigram: Trigram
        val appTrigram: Trigram
        val bodyInLower: Boolean
        when {
            movingInLower && !movingInUpper -> {
                appTrigram = lowerTrigram
                bodyTrigram = upperTrigram
                bodyInLower = false
            }
            movingInUpper && !movingInLower -> {
                appTrigram = upperTrigram
                bodyTrigram = lowerTrigram
                bodyInLower = true
            }
            else -> {
                bodyTrigram = lowerTrigram
                appTrigram = upperTrigram
                bodyInLower = true
            }
        }

        // 互卦：二三四爻為下互，三四五爻為上互
        val lowerMutual = Trigram.fromBinary(bits(yaos[1]) + bits(yaos[2]) + bits(yaos[3]))
        val upperMutual = Trigram.fromBinary(bits(yaos[2]) + bits(yaos[3]) + bits(yaos[4]))
        val mutualHexagram = findHexagramByBinary(lowerMutual.binaryCode + upperMutual.binaryCode)

        // 體互 / 用互
        val bodyMutual = if (bodyInLower) lowerMutual else upperMutual
        val appMutual = if (bodyInLower) upperMutual else lowerMutual

        // 本卦體用生克
        val mainRelation = TiYongRelation.of(bodyTrigram, appTrigram)

        // 變卦中用位之卦（與本卦用位相同位置之經卦）
        val changedAppTrigram = result.mutatedHexagram?.let {
            if (bodyInLower) it.upperTrigram else it.lowerTrigram
        }
        val changedRelation = changedAppTrigram?.let { TiYongRelation.of(bodyTrigram, it) }

        // 體黨 / 用黨：互卦上下卦與變卦上下卦之五行比對
        val partyTrigrams = buildList {
            add(bodyMutual)
            add(appMutual)
            result.mutatedHexagram?.let {
                add(it.lowerTrigram)
                add(it.upperTrigram)
            }
        }
        val bodyPartyCount = partyTrigrams.count { it.wuXing == bodyTrigram.wuXing }
        val appPartyCount = partyTrigrams.count { it.wuXing == appTrigram.wuXing }

        return TiYongAnalysis(
            bodyTrigram = bodyTrigram,
            appTrigram = appTrigram,
            bodyInLower = bodyInLower,
            lowerMutual = lowerMutual,
            upperMutual = upperMutual,
            bodyMutual = bodyMutual,
            appMutual = appMutual,
            mutualHexagram = mutualHexagram,
            mainRelation = mainRelation,
            mutatedHexagram = result.mutatedHexagram,
            changedAppTrigram = changedAppTrigram,
            changedRelation = changedRelation,
            bodyPartyCount = bodyPartyCount,
            appPartyCount = appPartyCount
        )
    }

    private fun bits(yao: Yao): String = if (yao.isYang) "1" else "0"
}
