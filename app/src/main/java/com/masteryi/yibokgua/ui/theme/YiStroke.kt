package com.masteryi.yibokgua.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * 框線 Tokens。
 */
object YiStroke {
    val hairlineWidth = 0.75.dp
    val dashedWidth = 0.5.dp
    val doubleWidth = 1.dp
    val doubleGap = 4.dp
    val doubleInnerWidth = 0.75.dp

    /** 0.75dp 實線，墨黑 40% */
    @Composable
    fun hairline(): BorderStroke = BorderStroke(hairlineWidth, LocalYiColors.current.hairline)

    /** 0.5dp 虛線，8/6 間距，墨黑 15% */
    @Composable
    fun dashed(): Stroke {
        val density = LocalDensity.current
        return Stroke(
            width = with(density) { dashedWidth.toPx() },
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(
                    with(density) { 8.dp.toPx() },
                    with(density) { 6.dp.toPx() }
                )
            ),
            cap = StrokeCap.Round
        )
    }

    /** 1dp ＋ 4dp 間距 ＋ 0.75dp 雙線（預留，本次不用） */
    @Composable
    fun double(): DoubleStroke {
        val hairlineColor = LocalYiColors.current.hairline
        return DoubleStroke(
            outer = BorderStroke(doubleWidth, hairlineColor),
            gap = doubleGap,
            inner = BorderStroke(doubleInnerWidth, hairlineColor)
        )
    }

    data class DoubleStroke(
        val outer: BorderStroke,
        val gap: androidx.compose.ui.unit.Dp,
        val inner: BorderStroke
    )
}
