package com.masteryi.yibokgua.ui.screen.query

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.TrigramInfo
import com.masteryi.yibokgua.ui.components.AdaptiveScreen
import com.masteryi.yibokgua.ui.components.TrigramInfoDialog
import com.masteryi.yibokgua.ui.components.TrigramPairSection
import com.masteryi.yibokgua.ui.components.ZenCard
import com.masteryi.yibokgua.ui.components.zenThinBorder
import com.masteryi.yibokgua.ui.theme.YiType
import com.masteryi.yibokgua.ui.theme.systemFontTypography
import com.masteryi.yibokgua.ui.theme.YibokguaTheme
import com.masteryi.yibokgua.viewmodel.DivinationViewModel

/** 全局統一細框線樣式（隨主題與設定自適應） */
private val ThinBorder: BorderStroke
    @Composable get() = zenThinBorder()

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

    MaterialTheme(typography = systemFontTypography()) {
        AdaptiveScreen {
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
                    ZenCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedHexagram = hexagram }
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
                                    fontFamily = YiType.symbolFamily,
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
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = YiType.symbolFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        selectedHexagram?.let { hexagram ->
            HexagramDetailDialog(
                hexagram = hexagram,
                getTrigramInfo = viewModel::getTrigramInfo,
                onDismiss = { selectedHexagram = null },
                onHexagramClick = { selectedHexagram = it }
            )
        }
    }
}

/** 卦象詳情彈窗：完整六爻卦象、錯卦綜卦互卦變卦、梅花易數屬性、上下卦八卦資料、卦辭、彖傳、大象傳、六爻爻辭、分爻小象傳、歷代注解 */
@Composable
fun HexagramDetailDialog(
    hexagram: Hexagram,
    getTrigramInfo: (Trigram) -> TrigramInfo,
    onDismiss: () -> Unit,
    onHexagramClick: (Hexagram) -> Unit = {}
) {
    var dialogInfo by remember { mutableStateOf<TrigramInfo?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                // 全局自適應：大螢幕限制對話窗最大寬度，避免內容過度拉寬
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = MaterialTheme.shapes.extraLarge,
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
                        fontFamily = YiType.symbolFamily,
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
                        fontFamily = YiType.symbolFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
					
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
					Spacer(modifier = Modifier.height(10.dp))
					
					// 錯卦、綜卦、互卦（橫向滾動卡片）
                    RelatedHexagramSection(
                        title = "錯卦・綜卦・互卦",
                        items = listOfNotNull(
                            hexagram.cuoHexagram?.let { "錯卦" to it },
                            hexagram.zongHexagram?.let { "綜卦" to it },
                            hexagram.huHexagram?.let { "互卦" to it }
                        ),
                        onHexagramClick = { onDismiss(); onHexagramClick(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 六爻變卦（橫向滾動卡片）
                    if (hexagram.bianHexagrams.isNotEmpty()) {
                        RelatedHexagramSection(
                            title = "六爻變卦",
                            items = hexagram.bianHexagrams.mapIndexed { i, h -> "第${i + 1}爻變" to h },
                            onHexagramClick = { onDismiss(); onHexagramClick(it) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(
                        text = hexagram.meiHuaAttribute,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 上下卦八卦資料區塊（點擊小卡可看單卦詳情）
                    TrigramPairSection(
                        title = "上下卦・八卦資料",
                        lower = getTrigramInfo(hexagram.lowerTrigram),
                        upper = getTrigramInfo(hexagram.upperTrigram),
                        onTrigramClick = { dialogInfo = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
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

    // 單卦詳情彈窗（點擊上下卦小卡時顯示）
    dialogInfo?.let { info ->
        TrigramInfoDialog(info = info, onDismiss = { dialogInfo = null })
    }
}

/** 關聯卦橫向滾動區塊 */
@Composable
private fun RelatedHexagramSection(
    title: String,
    items: List<Pair<String, Hexagram>>,
    onHexagramClick: (Hexagram) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { (label, hex) ->
                RelatedHexagramCard(label = label, hexagram = hex, onClick = { onHexagramClick(hex) })
            }
        }
    }
}

/** 單張關聯卦卡片：卦象符號 + 卦名 + 卦序，點擊跳轉 */
@Composable
private fun RelatedHexagramCard(
    label: String,
    hexagram: Hexagram,
    onClick: () -> Unit
) {
    ZenCard(
        modifier = Modifier.width(100.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = hexagram.symbol,
                fontSize = 28.sp,
                fontFamily = YiType.symbolFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = hexagram.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = "第${hexagram.id}卦",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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

/** 64 卦庫全螢幕預覽 */
@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 891)
@Composable
private fun HexagramQueryScreenPreview() {
    YibokguaTheme {
        HexagramQueryScreen(viewModel = DivinationViewModel())
    }
}
