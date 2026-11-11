package com.masteryi.yibokgua.widget

/** 桌面小工具狀態：使用者按「換一卦」時的覆寫卦象（僅當日有效）。 */
data class WidgetState(
    val overrideId: Int? = null,
    val overrideEpochDay: Long? = null
)

/** 桌面小工具顯示哪一卦的純邏輯（方便單元測試）。 */
object WidgetHexagram {

    /** 易經六十四卦，id 為 1..64。 */
    const val HEXAGRAM_COUNT = 64

    /** 依日期（epochDay）穩定對應 1..64 的卦序，同一天固定不變。 */
    fun dailyId(epochDay: Long): Int {
        val mod = ((epochDay % HEXAGRAM_COUNT) + HEXAGRAM_COUNT) % HEXAGRAM_COUNT
        return mod.toInt() + 1
    }

    /** 當日覆寫優先（且須在合法範圍），否則使用每日卦。 */
    fun resolveId(state: WidgetState, todayEpochDay: Long): Int {
        val override = state.overrideId
        return if (override != null &&
            state.overrideEpochDay == todayEpochDay &&
            override in 1..HEXAGRAM_COUNT
        ) {
            override
        } else {
            dailyId(todayEpochDay)
        }
    }
}
