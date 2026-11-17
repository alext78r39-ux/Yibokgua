package com.masteryi.yibokgua.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.masteryi.core.model.iching.TrigramInfo
import com.masteryi.yibokgua.ui.theme.YiMotion
import com.masteryi.yibokgua.ui.theme.YiType
import kotlinx.coroutines.launch

/**
 * 八卦資料詳情彈窗：卦象、先天數五行、萬類象全覽。
 * 首頁八卦選擇區點擊按鈕時顯示，亦可由上下卦小卡點入。
 */
@Composable
fun TrigramInfoDialog(
    info: TrigramInfo,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var closing by remember { mutableStateOf(false) }
    val requestDismiss: () -> Unit = {
        if (!closing) {
            closing = true
            scope.launch {
                progress.animateTo(0f, tween(YiMotion.medium, easing = YiMotion.emphasized))
                onDismiss()
            }
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(YiMotion.medium, easing = YiMotion.emphasized))
    }

    Dialog(onDismissRequest = requestDismiss) {
        Surface(
            modifier = Modifier
                .width(380.dp)
                .fillMaxHeight(0.8f)
                .graphicsLayer {
                    val p = progress.value
                    scaleX = 0.96f + 0.04f * p
                    scaleY = 0.96f + 0.04f * p
                    alpha = p
                },
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            border = zenThinBorder()
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = info.symbol,
                        fontSize = 60.sp,
                        fontFamily = YiType.symbolFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${info.nameZh}卦",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "先天數 ${info.trigram.number}・五行${info.trigram.wuXing.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ZenCard(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            info.attributeEntries().forEachIndexed { index, (label, value) ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 18.sp
                                )
                            }
                            if (info.attributeEntries().isEmpty()) {
                                Text(TrigramInfo.PENDING_TEXT, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = requestDismiss) { Text("關閉") }
                }
            }
        }
    }
}

/**
 * 上下卦八卦資料區塊：以兩張並排小卡分別顯示下卦（內卦）、上卦（外卦）的八卦資料摘要。
 * 用於占卜結果與 64 卦庫詳情彈窗；點擊小卡可開啟單卦完整詳情彈窗。
 */
@Composable
fun TrigramPairSection(
    lower: TrigramInfo,
    upper: TrigramInfo,
    title: String = "上下卦・八卦資料",
    modifier: Modifier = Modifier,
    maxEntries: Int = 4,
    onTrigramClick: ((TrigramInfo) -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitleText("【$title】")
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompactTrigramCard(
                label = "下卦（內卦・地卦）",
                info = lower,
                maxEntries = maxEntries,
                onClick = onTrigramClick?.let { callback -> { callback(lower) } },
                modifier = Modifier.weight(1f)
            )
            CompactTrigramCard(
                label = "上卦（外卦・天卦）",
                info = upper,
                maxEntries = maxEntries,
                onClick = onTrigramClick?.let { callback -> { callback(upper) } },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** 單一八卦緊湊資料卡：符號＋名稱＋屬性摘要（超出 maxEntries 以提示取代），可選擇性支援點擊開啟詳情彈窗 */
@Composable
fun CompactTrigramCard(
    label: String,
    info: TrigramInfo,
    modifier: Modifier = Modifier,
    maxEntries: Int = Int.MAX_VALUE,
    onClick: (() -> Unit)? = null
) {
    val entries = info.attributeEntries()
    val visibleEntries = if (maxEntries >= 0) entries.take(maxEntries) else entries
    val hiddenCount = entries.size - visibleEntries.size

    ZenCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(info.symbol, fontSize = 24.sp, fontFamily = YiType.symbolFamily)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "${info.nameZh}卦",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "數${info.trigram.number}・五行${info.trigram.wuXing.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 6.dp)
            )
            visibleEntries.forEach { (attrLabel, value) ->
                Text(
                    text = attrLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            if (hiddenCount > 0 && onClick != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "⋯等共 ${entries.size} 類・點擊查看完整",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else if (entries.isEmpty()) {
                Text(TrigramInfo.PENDING_TEXT, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** 區塊標題（高對比主題色） */
@Composable
private fun SectionTitleText(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}
