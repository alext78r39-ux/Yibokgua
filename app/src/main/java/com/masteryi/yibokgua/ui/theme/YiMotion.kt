package com.masteryi.yibokgua.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

/**
 * 全域動效 Tokens（單一來源）。
 *
 * standard 用於一般轉場與狀態切換；emphasized 用於太極起卦等重點動畫；
 * press 用於按壓回饋。時長分短／中／長三級，位移與縮放幅度刻意維持細微。
 */
object YiMotion {
    /** 一般轉場與狀態切換 */
    val standard: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    /** 重點動畫（太極起卦、八卦卡選中） */
    val emphasized: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    /** 按壓回饋：回彈快、收尾靜 */
    val press: Easing = FastOutSlowInEasing

    /** 極短回饋（按壓縮放） */
    const val short = 150

    /** 一般狀態切換（淡入淡出） */
    const val medium = 300

    /** 頁面進出等較長動畫（滑動） */
    const val long = 450

    /** 卡片按壓縮放倍率（0.98 = 內縮 2%） */
    const val pressScale = 0.98f

    /** 頁面切換橫向位移，占畫面寬度比例 */
    const val pageSlideFraction = 0.08f

    /** 卡片色暈透明度（2%） */
    const val cardGradientAlpha = 0.02f
}
