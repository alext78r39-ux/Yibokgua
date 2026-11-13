package com.masteryi.engine.ninepalace

/**
 * 九宮六壬的九卦資料。
 *
 * 數、卦名、方位、五行、象意皆依傳統九宮六壬整理；
 * 起卦時使用 1–9 編號循環順數，方位僅供展示與查閱。
 */
enum class NinePalaceGua(
    val number: Int,
    val guaName: String,
    val direction: String,
    val element: String,
    val meaning: String
) {
    DA_AN(1, "大安", "左方", "木", "穩定、宜靜、可成"),
    LIU_LIAN(2, "留連", "左上方", "木", "停滯、反覆、遲滯"),
    SU_XI(3, "速喜", "上方", "火", "快速、驚喜、喜事近"),
    CHI_KOU(4, "赤口", "右方", "金", "口舌、爭鬥、是非"),
    XIAO_JI(5, "小吉", "下方", "水", "起步、小成、需努力"),
    KONG_WANG(6, "空亡", "中央", "土", "落空、虛妄、事難成"),
    BING_FU(7, "病符", "右上", "土", "病態、異常、防健康"),
    TAO_HUA(8, "桃花", "左下", "土", "人緣、感情、牽絆"),
    TIAN_DE(9, "天德", "右下", "金", "貴人、上司、高遠");

    companion object {
        fun of(number: Int): NinePalaceGua =
            entries.first { it.number == number }

        /** 九宮格展示順序：留連、速喜、病符／大安、空亡、赤口／桃花、小吉、天德。 */
        val palaceOrder: List<NinePalaceGua> = listOf(
            LIU_LIAN, SU_XI, BING_FU,
            DA_AN, KONG_WANG, CHI_KOU,
            TAO_HUA, XIAO_JI, TIAN_DE
        )
    }
}

/** 九宮六壬輸入：農曆月（1–12）、農曆日（1–30）、時辰序（子1…亥12）。 */
data class NinePalaceInput(
    val month: Int,
    val day: Int,
    val hourIndex: Int
) {
    init {
        require(month in 1..12) { "農曆月必須在 1–12" }
        require(day in 1..30) { "農曆日必須在 1–30" }
        require(hourIndex in 1..12) { "時辰序必須在 1–12（子=1…亥=12）" }
    }
}

/** 三層結果：月卦為背景、日卦為過程、時卦為落點（主卦）。 */
data class NinePalaceResult(
    val monthGua: NinePalaceGua,
    val dayGua: NinePalaceGua,
    val hourGua: NinePalaceGua
) {
    /** 最終主卦：時宮。 */
    val mainGua: NinePalaceGua get() = hourGua
}

/** 九宮六壬核心算法：只負責取數與落點，不判斷吉凶。 */
object NinePalaceCalculator {

    fun calculate(input: NinePalaceInput): NinePalaceResult {
        val monthNumber = ((input.month - 1) % 9) + 1
        val monthGua = NinePalaceGua.of(monthNumber)

        val dayNumber = ((monthGua.number - 1 + input.day - 1) % 9) + 1
        val dayGua = NinePalaceGua.of(dayNumber)

        val hourNumber = ((dayGua.number - 1 + input.hourIndex - 1) % 9) + 1
        val hourGua = NinePalaceGua.of(hourNumber)

        return NinePalaceResult(
            monthGua = monthGua,
            dayGua = dayGua,
            hourGua = hourGua
        )
    }

    fun calculate(month: Int, day: Int, hourIndex: Int): NinePalaceResult =
        calculate(NinePalaceInput(month, day, hourIndex))
}

/** 卦卡展示文字。 */
fun NinePalaceGua.toCardText(): String = buildString {
    appendLine("第${number}卦 · ${guaName}")
    appendLine("${direction} · ${element}")
    append(meaning)
}

/**
 * 三層關聯解讀：僅做結構化拼接與提示，不自動斷吉凶。
 * 吉凶仍需結合所問之事、卦名象意與三層關係判斷。
 */
object NinePalaceInterpreter {

    fun describe(result: NinePalaceResult): String = buildString {
        appendLine("【月卦·背景／緣起】")
        appendLine(result.monthGua.toRelationLine())

        appendLine()
        appendLine("【日卦·過程／中間狀態】")
        appendLine(result.dayGua.toRelationLine())

        appendLine()
        appendLine("【時卦·當下落點／主卦】")
        appendLine(result.hourGua.toRelationLine())

        appendLine()
        appendLine("【三層關聯提示】")
        append(relationHint(result))
    }

    private fun NinePalaceGua.toRelationLine(): String =
        "${guaName}（${direction}，${element}）：${meaning}"

    private fun relationHint(result: NinePalaceResult): String {
        val month = result.monthGua
        val day = result.dayGua
        val hour = result.hourGua

        return when {
            month == day && day == hour ->
                "三卦同象，主題集中，該象意在此事中反覆出現。宜圍繞「${hour.meaning}」審慎判斷，不宜只看單一吉凶字面。"

            hour == month ->
                "時卦與月卦同象，落點回到事情的外部底色或初心。事情容易受環境、緣起影響，宜先看背景再定行動。"

            hour == day ->
                "時卦與日卦同象，過程與結果同象。推進方式會直接決定落點，宜檢查中間環節是否與當下主卦相應。"

            else ->
                "三卦各異，按「月為背景、日為過程、時為定局」分層讀：先看時卦定當下主軸，再用月卦解釋來路，用日卦解釋過程中的阻力或助力。"
        }
    }
}
