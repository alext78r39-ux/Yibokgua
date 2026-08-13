package com.masteryi.yibokgua.ui.screen.query

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.core.model.iching.TiYongInfo
import com.masteryi.yibokgua.viewmodel.DivinationViewModel

/** 全局統一細框線樣式（隨主題自適應） */
private val ThinBorder: BorderStroke
    @Composable get() = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)

@Composable
fun HexagramQueryScreen(
    viewModel: DivinationViewModel,
    autoOpenId: Int? = null,
    onAutoOpenConsumed: () -> Unit = {}
) {
    val hexagrams = remember { viewModel.getAll64Hexagrams() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedHexagram by remember { mutableStateOf<Hexagram?>(null) }

    val filteredList = hexagrams.filter {
        it.name.contains(searchQuery) || it.id.toString() == searchQuery
    }

    // 從起卦結果「查看 64 卦詳情」自動開啟對應卦象
    LaunchedEffect(autoOpenId) {
        if (autoOpenId != null) {
            selectedHexagram = viewModel.getHexagramById(autoOpenId) ?: hexagrams.firstOrNull()
            onAutoOpenConsumed()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("搜尋 64 卦 (例如：乾、坤、1)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 全局自適應：以 360dp 為基準自動分欄（窄螢幕單欄、平板雙欄）
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredList) { hexagram ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedHexagram = hexagram },
                    border = ThinBorder,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 完整的六爻卦象
                            Text(
                                text = hexagram.symbol,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "第 ${hexagram.id} 卦",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = hexagram.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = hexagram.wuXingRelation,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${hexagram.upperTrigram.nameZh}${hexagram.upperTrigram.symbol} " +
                                    "${hexagram.lowerTrigram.symbol}${hexagram.lowerTrigram.nameZh}",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }

    selectedHexagram?.let { hexagram ->
        HexagramDetailDialog(hexagram = hexagram, onDismiss = { selectedHexagram = null })
    }
}

/** 卦象詳情彈窗：完整六爻卦象、梅花易數屬性、卦辭、彖傳、大象傳、六爻爻辭、分爻小象傳、歷代注解 */
@Composable
fun HexagramDetailDialog(
    hexagram: Hexagram,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                // 全局自適應：大螢幕限制對話窗最大寬度，避免內容過度拉寬
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = ThinBorder
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // 可捲動內容區：完整顯示所有區塊
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 放大顯示的完整六爻卦象
                    Text(
                        text = hexagram.symbol,
                        fontSize = 84.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "第 ${hexagram.id} 卦：${hexagram.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${hexagram.upperTrigram.nameZh}${hexagram.upperTrigram.symbol} " +
                            "${hexagram.lowerTrigram.symbol}${hexagram.lowerTrigram.nameZh}（上${hexagram.upperTrigram.nameZh}下${hexagram.lowerTrigram.nameZh}）",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = ThinBorder,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("【梅花易數屬性】", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(hexagram.meiHuaAttribute)
                            Text(
                                text = "下卦：${hexagram.lowerTrigram.nameZh}（內卦/地卦）・上卦：${hexagram.upperTrigram.nameZh}（外卦/天卦）",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = ThinBorder,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("【體用生克】", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "體為自身、用為所應之事：動爻所在之卦為用，無動爻之卦為體；無動爻時內卦下卦為體、外卦上卦為用。",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TiYongScenarioRow(title = "無動爻（默認）", info = hexagram.tiYong())
                            TiYongScenarioRow(title = "動爻在下卦（初・二・三爻）", info = hexagram.tiYong(1))
                            TiYongScenarioRow(title = "動爻在上卦（四・五・六爻）", info = hexagram.tiYong(4))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    SectionHeader("周易本經・卦辭")
                    Text(hexagram.judgement)
                    Spacer(modifier = Modifier.height(8.dp))

                    SectionHeader("彖傳（釋卦總義）")
                    Text(hexagram.tuanZhuan.ifEmpty { "資料建置中..." })
                    Spacer(modifier = Modifier.height(8.dp))

                    SectionHeader("大象傳（全卦總象）")
                    Text(hexagram.imageText)
                    Spacer(modifier = Modifier.height(8.dp))

                    SectionHeader("周易本經・六爻爻辭")
                    hexagram.lineTexts.forEach { line ->
                        Text(line, modifier = Modifier.padding(vertical = 2.dp))
                    }

                    if (hexagram.yaoXiangZhuan.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader("分爻小象傳")
                        hexagram.yaoXiangZhuan.forEach { line ->
                            Text(line, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }

                    if (hexagram.interpretation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader("淺釋（白話解說）")
                        Text(hexagram.interpretation)
                    }

                    if (hexagram.commentary.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader("歷代注解")
                        Text(hexagram.commentary)
                    }
                }

                // 固定底部的關閉按鈕
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("關閉") }
                }
            }
        }
    }
}

/** 體用生克情境行 */
@Composable
private fun TiYongScenarioRow(title: String, info: TiYongInfo) {
    Text(
        text = "$title：${info.text}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

/** 詳情彈窗小節標題（高對比主題色） */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = "【$text】",
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}
