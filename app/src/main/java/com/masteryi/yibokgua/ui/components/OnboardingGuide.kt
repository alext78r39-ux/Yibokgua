package com.masteryi.yibokgua.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.masteryi.yibokgua.ui.theme.LocalYiType
import com.masteryi.yibokgua.ui.theme.YiColors
import com.masteryi.yibokgua.ui.theme.YiMotion

/** 首次使用導覽的單一步驟；tab 對應起卦頁的分頁標題。 */
private data class GuideStep(
    val tab: String,
    val hint: String,
    val detail: String
)

/** 五種起卦方式的引導文案，順序與 DivinationInputScreen 的 tabs 一致。 */
private val GUIDE_STEPS = listOf(
    GuideStep(
        tab = "手動",
        hint = "長按太極圖，一爻一爻搖",
        detail = "點「開始」後長按太極圖約 2 秒生成一爻，中途放開可重新計時，依序搖滿六爻成卦。"
    ),
    GuideStep(
        tab = "快速",
        hint = "一鍵成卦",
        detail = "點「開始」即一次生成六爻，適合快速對照卦象與體用分析。"
    ),
    GuideStep(
        tab = "日期",
        hint = "以時間取數",
        detail = "預設採農曆梅花易數：由年支、農曆月、日、時支取數；亦可切換國曆日期與時間對照。"
    ),
    GuideStep(
        tab = "類象",
        hint = "所見即卦",
        detail = "輸入物象／觀察數與方位數取數，把當下的觀察轉成卦象。"
    ),
    GuideStep(
        tab = "字占",
        hint = "以字起卦",
        detail = "輸入 1–10 個中文字，依字數與繁簡筆畫分組取數成卦。"
    )
)

/**
 * 首次使用導覽：以五個步驟對應起卦頁五種方式，降低初次上手的認知負擔。
 *
 * 目前步驟以「脈動」微互動（scale 0.8→1，[YiMotion.emphasized] / [YiMotion.medium]）標示，
 * 顏色沿用全域 token，不引入新樣式。
 *
 * 不論走完流程、跳過或按返回鍵，皆呼叫 [onComplete] 寫入「已看過」，
 * 避免每次啟動重複出現；日後可至「設定 → 使用說明」再次查看完整說明。
 */
@Composable
fun OnboardingGuide(onComplete: () -> Unit) {
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val pulseTransition = rememberInfiniteTransition(label = "onboardingPulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = YiMotion.medium, easing = YiMotion.emphasized),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboardingPulseScale"
    )

    val step = GUIDE_STEPS[stepIndex]
    val type = LocalYiType.current

    Dialog(
        onDismissRequest = onComplete,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        ZenCard(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = "第一次使用？", style = type.heading)

                Text(
                    text = "花 30 秒認識五種起卦方式。之後可隨時在「設定 → 使用說明」再次查看。",
                    style = type.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 五個步驟指示器，當前步驟以脈動微互動提示（TechCrunch UX 文章的 pulsing circles 概念）
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GUIDE_STEPS.forEachIndexed { index, _ ->
                        val active = index == stepIndex
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .graphicsLayer {
                                    if (active) {
                                        scaleX = pulse
                                        scaleY = pulse
                                    }
                                }
                                .background(
                                    color = if (active) {
                                        YiColors.cinnabar
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Text(
                    text = "${stepIndex + 1} / ${GUIDE_STEPS.size}　${step.tab}",
                    style = type.seal,
                    color = YiColors.cinnabar
                )
                Text(text = step.hint, style = type.subtitle)
                Text(
                    text = step.detail,
                    style = type.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f)
                    ) { Text("跳過") }

                    Button(
                        onClick = {
                            if (stepIndex == GUIDE_STEPS.lastIndex) {
                                onComplete()
                            } else {
                                stepIndex++
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (stepIndex == GUIDE_STEPS.lastIndex) "開始使用" else "下一步")
                    }
                }
            }
        }
    }
}
