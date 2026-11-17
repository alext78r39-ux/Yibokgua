package com.masteryi.yibokgua.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.LocalContext
import androidx.glance.LocalSize

internal data class WidgetLayout(
    val width: Dp,
    val height: Dp,
    val fontScale: Float
) {
    val isLargeText: Boolean get() = fontScale >= 1.3f
    val isNarrow: Boolean get() = width < 300.dp
    val isShort: Boolean get() = height < 190.dp
    val isCompact: Boolean get() = isNarrow || isShort || isLargeText
}

@Composable
internal fun currentWidgetLayout(fallbackWidth: Dp, fallbackHeight: Dp): WidgetLayout {
    val size = LocalSize.current
    return WidgetLayout(
        width = size.width.orFallback(fallbackWidth),
        height = size.height.orFallback(fallbackHeight),
        fontScale = LocalContext.current.resources.configuration.fontScale
    )
}

private fun Dp.orFallback(fallback: Dp): Dp =
    if (value.isFinite() && value > 0f) this else fallback
