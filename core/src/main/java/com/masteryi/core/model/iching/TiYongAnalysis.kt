package com.masteryi.core.model.iching

/**
 * 體用生克五種關係（梅花易數判斷吉凶的核心）
 */
enum class TiYongRelation(
    val label: String,           // 關係名稱，如「體克用」
    val verdict: String,         // 吉凶斷語
    val isAuspicious: Boolean    // 是否屬吉
) {
    TI_KE_YONG("體克用", "體卦克制用卦，主諸事吉利", true),
    YONG_KE_TI("用克體", "用卦克制體卦，主諸事兇險", false),
    TI_SHENG_YONG("體生用", "體卦生扶用卦，主有損耗財物的憂患", false),
    YONG_SHENG_TI("用生體", "用卦生扶體卦，主有進收利益之喜", true),
    BI_HE("體用比和", "體卦用卦兩相比和，主百事稱心如意", true);

    companion object {
        /** 由體卦、用卦之五行推出生克關係 */
        fun of(body: Trigram, app: Trigram): TiYongRelation = when {
            body.wuXing.controls(app.wuXing) -> TI_KE_YONG
            app.wuXing.controls(body.wuXing) -> YONG_KE_TI
            body.wuXing.generates(app.wuXing) -> TI_SHENG_YONG
            app.wuXing.generates(body.wuXing) -> YONG_SHENG_TI
            else -> BI_HE
        }
    }
}

/**
 * 單卦體用生克查詢結果（64卦庫靜態查詢用）
 *
 * 體為自身、用為所應之事；動爻所在之卦為用，無動爻之卦為體。
 */
data class TiYongInfo(
    val bodyTrigram: Trigram,      // 體卦
    val appTrigram: Trigram,       // 用卦
    val relation: TiYongRelation   // 體用生克關係
) {
    val text: String
        get() = "體卦${bodyTrigram.nameZh}（${bodyTrigram.wuXing.label}）" +
            "對用卦${appTrigram.nameZh}（${appTrigram.wuXing.label}）為「${relation.label}」，${relation.verdict}"
}

/**
 * 梅花易數體用生克分析結果
 *
 * 體為主、用為事，互卦為事之中間，變卦為事之終。
 * 體卦為起卦之人物，用卦為相關聯的內容，互卦為事物的中間對應關係，變卦為事物的結果。
 */
data class TiYongAnalysis(
    val bodyTrigram: Trigram,         // 體卦（無動爻之卦；無動爻時以內卦下卦為體）
    val appTrigram: Trigram,          // 用卦（有動爻之卦；無動爻時以外卦上卦為用）
    val bodyInLower: Boolean,         // 體卦是否位於下卦（內卦）
    val lowerMutual: Trigram,         // 下互（二、三、四爻）
    val upperMutual: Trigram,         // 上互（三、四、五爻）
    val bodyMutual: Trigram,          // 體互（與體卦同側之互卦）
    val appMutual: Trigram,           // 用互（與用卦同側之互卦）
    val mutualHexagram: Hexagram,     // 互卦（下互為下卦、上互為上卦所組成的六爻卦）
    val mainRelation: TiYongRelation, // 本卦體用生克關係
    val mutatedHexagram: Hexagram?,   // 變卦（事之終）
    val changedAppTrigram: Trigram?,  // 變卦中用位之卦（與本卦用位相同之經卦）
    val changedRelation: TiYongRelation?, // 變卦體用生克關係
    val bodyPartyCount: Int,          // 互變兩卦中與體卦同五行之體黨數
    val appPartyCount: Int            // 互變兩卦中與用卦同五行之用黨數
) {

    /** 本卦體用生克斷語 */
    val mainText: String
        get() = "本卦：體卦${bodyTrigram.nameZh}（${bodyTrigram.wuXing.label}），" +
            "用卦${appTrigram.nameZh}（${appTrigram.wuXing.label}）。${mainRelation.label}，${mainRelation.verdict}。"

    /** 互卦（事之中間）描述 */
    val mutualText: String
        get() = "互卦（${mutualHexagram.name}）：下互${lowerMutual.nameZh}${lowerMutual.wuXing.label}、" +
            "上互${upperMutual.nameZh}${upperMutual.wuXing.label}；" +
            "體互為${bodyMutual.nameZh}${bodyMutual.wuXing.label}，用互為${appMutual.nameZh}${appMutual.wuXing.label}。"

    /** 變卦（事之終）描述 */
    val changedText: String
        get() {
            val app = changedAppTrigram ?: return "本卦無動爻，不成變卦。"
            val relation = changedRelation ?: return "本卦無動爻，不成變卦。"
            return "變卦（${mutatedHexagram?.name}）：用位${app.nameZh}（${app.wuXing.label}）" +
                "與體卦${bodyTrigram.nameZh}為${relation.label}，${relation.verdict}。$trendText"
        }

    /** 用卦走勢：互卦為中間之應、變卦為末後之期 */
    val trendText: String
        get() {
            val changed = changedRelation ?: return ""
            return when {
                mainRelation.isAuspicious && changed.isAuspicious -> "用卦先吉後亦吉，主先後皆吉。"
                mainRelation.isAuspicious && !changed.isAuspicious -> "用卦由吉變凶，主先順利而後坎坷。"
                !mainRelation.isAuspicious && changed.isAuspicious -> "用卦由凶變吉，主先坎坷而後順利。"
                else -> "用卦先凶後亦凶，主先後皆凶。"
            }
        }

    /** 體勢 / 用勢（互變兩卦中之體黨與用黨） */
    val partyText: String
        get() = when {
            bodyPartyCount > appPartyCount -> "互變兩卦中體黨多而體勢盛，主吉。"
            appPartyCount > bodyPartyCount -> "互變兩卦中用黨多而用勢盛，主不吉。"
            else -> "互變兩卦中體用黨勢相當。"
        }

    /** 綜合斷語 */
    val summary: String
        get() = listOf(mainText, mutualText, changedText, partyText).joinToString("\n")
}
