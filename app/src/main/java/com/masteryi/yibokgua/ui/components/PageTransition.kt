package com.masteryi.yibokgua.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.masteryi.yibokgua.ui.theme.YiMotion

/**
 * 頁面切換動畫：左右滑動 + 淡入淡出。
 *
 * 滑動方向依頁面序號決定——序號變大（往後翻）由右側滑入、往小則由左側滑入；
 * 位移僅為畫面寬度的 8%，配合淡入淡出，感覺是「氣息流動」而非整頁推擠。
 *
 * @param currentPage 目前頁面序號，用來決定滑動方向
 * @param content 以頁面序號為參數的內容
 */
@Composable
fun ZenPageTransition(
    currentPage: Int,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    AnimatedContent(
        targetState = currentPage,
        modifier = modifier,
        transitionSpec = {
            val forward = targetState > initialState
            val offset = if (forward) YiMotion.pageSlideFraction else -YiMotion.pageSlideFraction
            val enter = slideInHorizontally(
                animationSpec = tween(YiMotion.long, easing = YiMotion.standard)
            ) { width -> (width * offset).toInt() } + fadeIn(
                animationSpec = tween(YiMotion.medium, easing = YiMotion.standard)
            )
            val exit = slideOutHorizontally(
                animationSpec = tween(YiMotion.long, easing = YiMotion.standard)
            ) { width -> (width * -offset).toInt() } + fadeOut(
                animationSpec = tween(YiMotion.medium, easing = YiMotion.standard)
            )
            enter togetherWith exit using SizeTransform(clip = false)
        },
        label = "zenPageTransition",
        content = { page -> content(page) }
    )
}
