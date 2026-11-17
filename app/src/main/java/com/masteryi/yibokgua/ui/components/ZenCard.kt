package com.masteryi.yibokgua.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.masteryi.yibokgua.ui.theme.LocalBorderWidth
import com.masteryi.yibokgua.ui.theme.YiColors
import com.masteryi.yibokgua.ui.theme.YiMotion
import com.masteryi.yibokgua.ui.theme.YiShape
import com.masteryi.yibokgua.ui.theme.YiStroke

/** 全局統一細框線樣式（隨主題與設定自適應） */
@Composable
fun zenThinBorder(color: Color = MaterialTheme.colorScheme.outline): BorderStroke =
    BorderStroke(LocalBorderWidth.current.dp, color)

/**
 * 可重用的按壓縮放（預設 0.98）。
 * 因按下狀態需與點擊共用同一個 interactionSource，呼叫端寫法為：
 * ```
 * val press = remember { MutableInteractionSource() }
 * Box(Modifier.zenPressScale(press).clickable(interactionSource = press) { … })
 * ```
 */
@Composable
fun Modifier.zenPressScale(interactionSource: MutableInteractionSource): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) YiMotion.pressScale else 1f,
        animationSpec = tween(durationMillis = YiMotion.short, easing = YiMotion.press),
        label = "zenPressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * 全域紙張卡片：扁平無陰影、細框線、紙色底與頂部 1dp 內高光，內建按壓縮放微動效。
 *
 * 以 [YiShape.medium]（16dp）為預設圓角；傳入 onClick 時自動接上按壓回饋。
 * [selected] 為硃砂紅強調態（畫面中僅用於八卦卡選中框），覆寫底色與框線。
 */
@Composable
fun ZenCard(
    modifier: Modifier = Modifier,
    containerColor: Color = YiColors.paperElevated,
    contentColor: Color = contentColorFor(containerColor),
    shape: Shape = YiShape.medium,
    border: BorderStroke? = zenThinBorder(),
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null && enabled) YiMotion.pressScale else 1f,
        animationSpec = tween(durationMillis = YiMotion.short, easing = YiMotion.press),
        label = "zenCardScale"
    )
    val resolvedContainer = if (selected) YiColors.cinnabarSoft else containerColor
    val resolvedBorder = if (selected) {
        BorderStroke(LocalBorderWidth.current.dp, YiColors.cinnabar)
    } else {
        border
    }
    val scaledModifier = modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = scaledModifier,
            enabled = enabled,
            shape = shape,
            color = resolvedContainer,
            contentColor = contentColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = null,
            interactionSource = interactionSource
        ) {
            ZenCardBody(shape = shape, border = resolvedBorder, content = content)
        }
    } else {
        Surface(
            modifier = scaledModifier,
            shape = shape,
            color = resolvedContainer,
            contentColor = contentColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = null
        ) {
            ZenCardBody(shape = shape, border = resolvedBorder, content = content)
        }
    }
}

/** 卡片內容層：框線與頂部高光鋪在底色之上，並提供 [ColumnScope] 給呼叫端 */
@Composable
private fun ZenCardBody(
    shape: Shape,
    border: BorderStroke?,
    content: @Composable ColumnScope.() -> Unit
) {
    val highlightColor = YiColors.paperHighlight
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(border?.let { Modifier.border(it, shape) } ?: Modifier)
            .drawWithContent {
                drawContent()
                val y = YiStroke.hairlineWidth.toPx() / 2f
                drawLine(
                    color = highlightColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = YiStroke.hairlineWidth.toPx()
                )
            },
        content = content
    )
}
