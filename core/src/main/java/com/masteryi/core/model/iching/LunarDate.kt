package com.masteryi.core.model.iching

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 農曆日期轉換（國曆 → 農曆），支援 1900-01-31 ~ 2100-12-31。
 *
 * 採用「農曆編碼表」：每年以一個 Int 編碼，
 * - bit16（0x10000）：閏月為 30 天；否則 29 天
 * - bit15 ~ bit4：正月至十二月大小月（1 = 30 天）
 * - bit3 ~ bit0（& 0xf）：閏月月份（0 = 無閏月）
 */
data class LunarDate(
    val year: Int,
    val month: Int,
    val isLeap: Boolean,
    val day: Int
) {
    /** 干支年（例：丙午年） */
    val ganZhiYear: String
        get() = GAN[((year - 4) % 10 + 10) % 10] + ZHI[((year - 4) % 12 + 12) % 12]

    /** 年支序（子1 丑2 寅3 卯4 辰5 巳6 午7 未8 申9 酉10 戌11 亥12） */
    val yearBranchNumber: Int
        get() = ((year - 4) % 12 + 12) % 12 + 1

    /** 月名（含閏字，例：正月、閏六月） */
    val monthLabel: String
        get() = (if (isLeap) "閏" else "") + MONTH_NAMES[month - 1]

    /** 日名（例：初三） */
    val dayLabel: String
        get() = DAY_NAMES[day - 1]

    /** 完整農曆日期（例：丙午年七月初三） */
    val text: String
        get() = "${ganZhiYear}年${monthLabel}${dayLabel}"

    companion object {
        private val GAN = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
        private val ZHI = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
        private val MONTH_NAMES = arrayOf("正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月")
        private val DAY_NAMES = arrayOf(
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
        )

        /** 農曆基準日：1900-01-31 = 清光緒二十六年正月初一 */
        private const val BASE_YEAR = 1900
        private const val BASE_SOLAR = "1900-01-31"

        // 農曆編碼表（1900 ~ 2100）
        private val lunarInfo = intArrayOf(
            0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, // 1900-1909
            0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, // 1910-1919
            0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, // 1920-1929
            0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, // 1930-1939
            0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, // 1940-1949
            0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, // 1950-1959
            0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, // 1960-1969
            0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6, // 1970-1979
            0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, // 1980-1989
            0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, // 1990-1999
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, // 2000-2009
            0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, // 2010-2019
            0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, // 2020-2029
            0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, // 2030-2039
            0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, // 2040-2049
            0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0, // 2050-2059
            0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, // 2060-2069
            0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, // 2070-2079
            0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, // 2080-2089
            0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252, // 2090-2099
            0x0d520 // 2100
        )

        private const val MONTH_BIT_MASK = 0x10000
        private const val LEAP_MONTH_MASK = 0xf

        /** 農曆 y 年閏月總天數（無閏月回傳 0） */
        private fun leapDays(y: Int): Int =
            if (leapMonth(y) != 0) if (lunarInfo[y - BASE_YEAR] and MONTH_BIT_MASK != 0) 30 else 29 else 0

        /** 農曆 y 年閏哪個月（1~12，0 = 無閏月） */
        fun leapMonth(y: Int): Int = lunarInfo[y - BASE_YEAR] and LEAP_MONTH_MASK

        /** 農曆 y 年 m 月的總天數 */
        private fun monthDays(y: Int, m: Int): Int =
            if (lunarInfo[y - BASE_YEAR] and (MONTH_BIT_MASK shr m) != 0) 30 else 29

        /** 農曆 y 年總天數 */
        private fun lunarYearDays(y: Int): Int {
            var sum = 348
            var i = 0x8000
            while (i > 0x8) {
                sum += if (lunarInfo[y - BASE_YEAR] and i != 0) 1 else 0
                i = i shr 1
            }
            return sum + leapDays(y)
        }

        /** 國曆轉農曆 */
        fun of(solarDate: LocalDate): LunarDate {
            val base = LocalDate.parse(BASE_SOLAR)
            var offset = ChronoUnit.DAYS.between(base, solarDate).toInt()
            require(offset >= 0) { "僅支援 ${BASE_SOLAR} 之後的日期" }

            // 定位農曆年份
            var year = BASE_YEAR
            var yearDays = 0
            while (year <= 2100 && offset > 0) {
                yearDays = lunarYearDays(year)
                offset -= yearDays
                year++
            }
            if (offset < 0) {
                offset += yearDays
                year--
            }
            require(year <= 2100) { "僅支援 2100 年（含）之前的日期" }

            // 定位農曆月份（含閏月）與日期
            val leapM = leapMonth(year)
            var month = 1
            var isLeap = false
            var monthDays = 0
            while (month <= 12 && offset > 0) {
                if (leapM > 0 && month == leapM + 1 && !isLeap) {
                    month--
                    isLeap = true
                    monthDays = leapDays(year)
                } else {
                    monthDays = monthDays(year, month)
                    if (isLeap && month == leapM + 1) isLeap = false
                }
                offset -= monthDays
                month++
            }
            // 邊界校正：偏移恰好落於月首時，需還原所屬月份（含閏月與其後一個月）
            if (offset == 0 && leapM > 0 && month == leapM + 1) {
                if (isLeap) {
                    isLeap = false
                } else {
                    month--
                    isLeap = true
                }
            }
            if (offset < 0) {
                offset += monthDays
                month--
            }
            return LunarDate(year, month, isLeap, offset + 1)
        }
    }
}