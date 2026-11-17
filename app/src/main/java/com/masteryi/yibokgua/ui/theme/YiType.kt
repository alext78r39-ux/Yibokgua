package com.masteryi.yibokgua.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.masteryi.yibokgua.R

/**
 * 字級模組 Tokens。
 *
 * 所有尺寸與行高皆使用 sp，隨系統 fontScale 縮放，避免大字級裁切。
 * 標題類使用內嵌 Noto Serif TC Medium 子集字體；缺字時由系統字體 fallback。
 */
private val YiSerif = FontFamily(
    Font(R.font.noto_serif_tc_medium, FontWeight.Medium)
)
private val YiSans = FontFamily.Default

object YiType {
    /** 內嵌 Noto Serif TC 字族，供全域標題使用 */
    val serifFamily = YiSerif

    /** 系統黑體字族，供正文與標籤使用 */
    val sansFamily = YiSans

    /** 卦象與八卦符號字族；子集字型不含此類 Unicode 符號，須走系統字型 */
    val symbolFamily = FontFamily.Default

    /** 「易卜」 */
    val display = TextStyle(
        fontFamily = YiSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 4.sp
    )

    /** 區塊標題 */
    val title = TextStyle(
        fontFamily = YiSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 2.sp
    )

    /** 卦名、彈窗標題 */
    val heading = TextStyle(
        fontFamily = YiSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 1.sp
    )

    /** 次要標題 */
    val subtitle = TextStyle(
        fontFamily = YiSans,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    )

    /** 正文、輸入框 */
    val body = TextStyle(
        fontFamily = YiSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    /** 農曆日期、提示 */
    val caption = TextStyle(
        fontFamily = YiSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )

    /** 極小印鑒風文字 */
    val seal = TextStyle(
        fontFamily = YiSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 2.sp
    )
}

/**
 * 隨設定字級倍率縮放的一組文字樣式。
 *
 * 基底樣式仍定義於 [YiType]；此處僅套用設定倍率。sp 本身亦隨系統字型大小縮放，
 * 故 [fontSize] 與 [lineHeight] 同步乘上倍率，避免大字級行距不足而裁切。
 */
@Immutable
data class YiTypeStyles(
    val display: TextStyle,
    val title: TextStyle,
    val heading: TextStyle,
    val subtitle: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
    val seal: TextStyle
)

/** 依設定倍率產生字級組；倍率為 1 時直接回用基底樣式。 */
internal fun yiTypeStyles(scale: Float): YiTypeStyles {
    fun TextStyle.scaled(): TextStyle = if (scale == 1f) {
        this
    } else {
        copy(fontSize = fontSize * scale, lineHeight = lineHeight * scale)
    }
    return YiTypeStyles(
        display = YiType.display.scaled(),
        title = YiType.title.scaled(),
        heading = YiType.heading.scaled(),
        subtitle = YiType.subtitle.scaled(),
        body = YiType.body.scaled(),
        caption = YiType.caption.scaled(),
        seal = YiType.seal.scaled()
    )
}

/** 目前主題的字級組；由 [YibokguaTheme] 依設定倍率提供。 */
val LocalYiType = staticCompositionLocalOf { yiTypeStyles(1f) }
