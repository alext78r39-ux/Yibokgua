package com.masteryi.core.model.iching

/**
 * 爻類型定義
 */
enum class Yao(
    val code: Int,
    val isYang: Boolean,
    val isMoving: Boolean,
    val symbol: String,
    val label: String
) {
    OLD_YIN(6, isYang = false, isMoving = true, symbol = "⚋ ✕", label = "老陰"),
    YOUNG_YANG(7, isYang = true, isMoving = false, symbol = "⚊", label = "少陽"),
    YOUNG_YIN(8, isYang = false, isMoving = false, symbol = "⚋", label = "少陰"),
    OLD_YANG(9, isYang = true, isMoving = true, symbol = "⚊ ◯", label = "老陽");

    /** 變爻後的陰陽狀態 */
    fun mutate(): Yao = when (this) {
        OLD_YIN -> YOUNG_YANG
        OLD_YANG -> YOUNG_YIN
        else -> this
    }
}

/**
 * 五行
 */
enum class WuXing(val label: String) {
    JIN("金"), MU("木"), SHUI("水"), HUO("火"), TU("土");

    /** 五行相生：此行者生 other */
    fun generates(other: WuXing): Boolean = when (this) {
        JIN -> other == SHUI
        SHUI -> other == MU
        MU -> other == HUO
        HUO -> other == TU
        TU -> other == JIN
    }

    /** 五行相剋：此行者剋 other */
    fun controls(other: WuXing): Boolean = when (this) {
        JIN -> other == MU
        MU -> other == TU
        TU -> other == SHUI
        SHUI -> other == HUO
        HUO -> other == JIN
    }
}

/**
 * 八卦（單卦 / 三爻）
 * 先天八卦數：乾 1、兌 2、離 3、震 4、巽 5、坎 6、艮 7、坤 8
 * 八卦五行：乾、兌屬金；震、巽屬木；坎屬水；離屬火；艮、坤屬土
 */
enum class Trigram(
    val number: Int,
    val nameZh: String,
    val symbol: String,
    val binaryCode: String, // 從初爻到上爻（下至上）：1為陽，0為陰
    val wuXing: WuXing
) {
    QIAN(1, "乾", "☰", "111", WuXing.JIN),
    DUI(2, "兌", "☱", "110", WuXing.JIN),
    LI(3, "離", "☲", "101", WuXing.HUO),
    ZHEN(4, "震", "☳", "100", WuXing.MU),
    XUN(5, "巽", "☴", "011", WuXing.MU),
    KAN(6, "坎", "☵", "010", WuXing.SHUI),
    GEN(7, "艮", "☶", "001", WuXing.TU),
    KUN(8, "坤", "☷", "000", WuXing.TU);

    companion object {
        fun fromNumber(num: Int): Trigram {
            val normalized = if (num % 8 == 0) 8 else num % 8
            return entries.first { it.number == normalized }
        }

        fun fromBinary(binary: String): Trigram {
            return entries.first { it.binaryCode == binary }
        }
    }
}

/**
 * 64 卦資料模型（重卦 / 六爻）
 * 上下卦定義：下卦 = 內卦、地卦；上卦 = 外卦、天卦。
 */
data class Hexagram(
    val id: Int,
    val name: String,
    val binaryCode: String, // 6位元 binary (初爻至上爻)
    val upperTrigram: Trigram,
    val lowerTrigram: Trigram,
    val judgement: String,   // 卦辭（周易本經）
    val imageText: String,   // 大象傳（全卦總象）
    val lineTexts: List<String>, // 六爻爻辭 (0:初爻 ... 5:上爻，乾坤另含用九/用六)
    val tuanZhuan: String = "",           // 彖傳（釋卦總義）
    val yaoXiangZhuan: List<String> = emptyList(), // 分爻小象傳
    val commentary: String = "",           // 歷代注解
    val interpretation: String = ""        // 淺釋（白話解說）
) {
    /**
     * 完整的六爻卦象（Unicode 易經六十四卦符號，一字符即全卦），依文王卦序排列。
     * U+4DC0 ~ U+4DFF 依序對應第 1 ~ 64 卦。
     */
    val symbol: String
        get() = (0x4DC0 + id - 1).toChar().toString()

    /**
     * 梅花易數體用五行關係：體用比和 / 體生用 / 用生體 / 體克用 / 用克體
     * 無動爻時以內卦下卦為體、外卦上卦為用。
     */
    val wuXingRelation: String
        get() = tiYong().relation.label

    /**
     * 梅花易數屬性描述：下卦、上卦之卦名、先天數、五行及體用關係
     */
    val meiHuaAttribute: String
        get() = "下卦：${lowerTrigram.nameZh}，數 ${lowerTrigram.number}，五行${lowerTrigram.wuXing.label}；" +
            "上卦：${upperTrigram.nameZh}，數 ${upperTrigram.number}，五行${upperTrigram.wuXing.label}。$wuXingRelation。"

    /**
     * 依梅花易數定體用（靜態查詢，供 64 卦庫參考）：
     * 動爻所在之卦為用、無動爻之卦為體；無動爻時內卦下卦為體、外卦上卦為用。
     *
     * @param movingYaoIndex 動爻位置 1~6（初爻至上爻）；null 表示無動爻
     */
    fun tiYong(movingYaoIndex: Int? = null): TiYongInfo {
        require(movingYaoIndex == null || movingYaoIndex in 1..6) { "動爻位置必須在 1~6 之間" }
        val movingInLower = movingYaoIndex != null && movingYaoIndex in 1..3
        val (body, app) = if (movingInLower) upperTrigram to lowerTrigram else lowerTrigram to upperTrigram
        return TiYongInfo(bodyTrigram = body, appTrigram = app, relation = TiYongRelation.of(body, app))
    }
}

/**
 * 起卦結果模型
 */
data class DivinationResult(
    val originalHexagram: Hexagram,
    val mutatedHexagram: Hexagram?,
    val yaos: List<Yao>,           // 長度 6，[0] 為初爻，[5] 為上爻
    val movingYaoIndex: Int?,      // 動爻位置 (1~6)，若為梅花易數則必定有 1 動爻
    val divinationType: String,
    val timestamp: Long = System.currentTimeMillis()
)
