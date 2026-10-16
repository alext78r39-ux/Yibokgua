package com.masteryi.yibokgua.ui.screen.home

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.masteryi.yibokgua.settings.ColorPreset
import com.masteryi.yibokgua.settings.SettingsViewModel
import com.masteryi.yibokgua.settings.ThemeMode
import com.masteryi.yibokgua.ui.components.AdaptiveScreen
import com.masteryi.yibokgua.ui.components.ZenCard
import com.masteryi.yibokgua.ui.components.zenThinBorder
import com.masteryi.yibokgua.ui.screen.reader.ReaderSettings
import com.masteryi.yibokgua.ui.screen.reader.ReaderViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    readerViewModel: ReaderViewModel
) {
    val settings by settingsViewModel.settings.collectAsState()
    val readerSettings by readerViewModel.settings.collectAsState()
    var showUsage by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showLegal by rememberSaveable { mutableStateOf(false) }

    AdaptiveScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        Text(
            "設定",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SectionLabel("外觀")
        SettingCard {
            Column(Modifier.padding(vertical = 6.dp)) {
                SettingSectionTitle("主題模式", "跟隨系統、淺色或深色顯示")
                Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        ThemeOptionChip(
                            mode = mode,
                            selected = settings.themeMode == mode,
                            onClick = { settingsViewModel.setThemeMode(mode) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                SwitchRow(
                    title = "動態取色",
                    subtitle = "使用 Android 12+ 系統色彩",
                    checked = settings.dynamicColor,
                    onCheckedChange = settingsViewModel::setDynamicColor
                )
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 配色方案
                SettingSectionTitle("配色方案", "選擇應用主色調")
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ColorPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = settings.colorPreset == preset,
                            onClick = { settingsViewModel.setColorPreset(preset) },
                            label = { Text(preset.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 字級大小
                SliderSettingRow(
                    title = "字級大小",
                    subtitle = "調整全局文字大小",
                    value = settings.fontSizeScale,
                    valueRange = 0.8f..1.4f,
                    steps = 5,
                    valueLabel = { "${(it * 100).roundToInt()}%" },
                    onValueChange = settingsViewModel::setFontSizeScale
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 邊框粗細
                SliderSettingRow(
                    title = "邊框粗細",
                    subtitle = "調整卡片與分隔線邊框",
                    value = settings.borderWidth,
                    valueRange = 0f..2f,
                    steps = 7,
                    valueLabel = { "${String.format("%.1f", it)}dp" },
                    onValueChange = settingsViewModel::setBorderWidth
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 鍵盤大小
                SliderSettingRow(
                    title = "鍵盤大小",
                    subtitle = "調整起卦頁按鈕尺寸",
                    value = settings.keyboardScale,
                    valueRange = 0.8f..1.4f,
                    steps = 5,
                    valueLabel = { "${(it * 100).roundToInt()}%" },
                    onValueChange = settingsViewModel::setKeyboardScale
                )
            }
        }

        SectionLabel("閱讀")
        SettingCard {
            Column(Modifier.padding(vertical = 6.dp)) {
                SliderSettingRow(
                    title = "閱讀字級",
                    subtitle = "調整書籍內文字大小",
                    value = readerSettings.fontSizeSp,
                    valueRange = ReaderSettings.MIN_FONT_SIZE_SP..ReaderSettings.MAX_FONT_SIZE_SP,
                    steps = 23,
                    valueLabel = { "${it.roundToInt()}sp" },
                    onValueChange = readerViewModel::setFontSizeSp
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SliderSettingRow(
                    title = "閱讀行距",
                    subtitle = "調整行與行之間的距離",
                    value = readerSettings.lineHeightMultiplier,
                    valueRange = ReaderSettings.MIN_LINE_HEIGHT..ReaderSettings.MAX_LINE_HEIGHT,
                    steps = 11,
                    valueLabel = { String.format("%.1f", it) },
                    onValueChange = readerViewModel::setLineHeightMultiplier
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SliderSettingRow(
                    title = "段落間距",
                    subtitle = "調整段落之間的距離",
                    value = readerSettings.paragraphSpacingDp,
                    valueRange = 0f..32f,
                    steps = 15,
                    valueLabel = { "${it.roundToInt()}dp" },
                    onValueChange = readerViewModel::setParagraphSpacingDp
                )
            }
        }

        SectionLabel("資料")
        SettingCard {
            SettingRow(
                title = "重置設定",
                subtitle = "將所有設定還原為預設值",
                onClick = { settingsViewModel.reset() }
            )
        }

        SectionLabel("說明")
        SettingCard {
            SettingRow(
                title = "使用說明",
                subtitle = "起卦方法與各功能操作指引",
                onClick = { showUsage = true }
            )
        }

        SectionLabel("關於")
        SettingCard {
            Column(Modifier.padding(vertical = 6.dp)) {
                SettingRow(
                    title = "關於易卜",
                    subtitle = "版本 ${versionName()}",
                    onClick = { showAbout = true }
                )
                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingRow(
                    title = "版權條款",
                    subtitle = "授權與免責聲明",
                    onClick = { showLegal = true }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "天行健，君子以自強不息",
            fontFamily = FontFamily.Serif,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
    }

    if (showUsage) {
        ScrollableInfoDialog(
            title = "使用說明",
            onDismiss = { showUsage = false },
            content = USAGE_TEXT
        )
    }
    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }
    if (showLegal) {
        ScrollableInfoDialog(
            title = "版權與條款",
            onDismiss = { showLegal = false },
            content = LEGAL_TEXT
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingCard(content: @Composable ColumnScope.() -> Unit) {
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            content = content
        )
    }
}

@Composable
private fun SettingSectionTitle(title: String, subtitle: String) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeOptionChip(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (mode) {
        ThemeMode.SYSTEM -> "跟隨系統"
        ThemeMode.LIGHT -> "淺色"
        ThemeMode.DARK -> "深色"
    }
    Surface(
        modifier = modifier
            .height(40.dp)
            .selectable(selected = selected, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderSettingRow(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueLabel: (Float) -> String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = valueLabel(value),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun versionName(): String {
    val context = LocalContext.current
    return remember {
        try {
            val pkg = context.packageManager.getPackageInfo(context.packageName, 0)
            pkg.versionName ?: "未知"
        } catch (e: PackageManager.NameNotFoundException) {
            "未知"
        }
    }
}

/** 可捲動的資訊對話框（使用說明 / 版權條款） */
@Composable
private fun ScrollableInfoDialog(
    title: String,
    onDismiss: () -> Unit,
    content: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            border = zenThinBorder()
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content.split("\n").forEach { line ->
                        when {
                            line.startsWith("【") -> Text(
                                line,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            line.isBlank() -> Spacer(Modifier.height(2.dp))
                            else -> Text(line, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("關閉") }
            }
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("確定") }
        },
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = { Text("關於易卜") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "易卜是一款以《周易》為核心的起卦占卜工具，",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "提供手動搖卦、快速起卦、時間起卦、類象加方位起卦與字占起卦，",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "收錄六十四卦經文與體用生克分析，供參研易理之用。",
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Text(
                    "版本 ${versionName()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "占卜結果僅供參考，凡事以自身理性判斷為準。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

private val USAGE_TEXT = """
【起卦】
底部或側欄「起卦」頁提供五種起卦方式：

・手動搖卦：點「開始」後，長按太極圖 2 秒生成一爻，依序搖滿六爻成卦。中途放開可重新計時。

・快速起卦：點「開始」後，直接生成六個爻，一鍵成卦。

・時間起卦：可切換「農曆（梅花易數，預設）」與「國曆」。
　農曆：輸入年支、農曆月、農曆日、時支，依傳統梅花易數取數（年支／時支為地支序：子1〜亥12）。
　國曆：選擇日期與時間（24 小時制），以年月日時分取數。

・類象：輸入「物象／觀察數」與「方位數」，按下「生成卦象」取數起卦。

・字占：輸入 1-10 個中文字，確認送出後依字取數成卦。

【卦象解讀】
成卦後顯示本卦與變卦、動爻位置，以及「體用生克」分析（體為自身、用為所應之事）。
點擊本卦或變卦可跳轉至「64卦庫」查看完整卦辭、爻辭、彖傳、象傳與歷代注解。

【64卦庫】
收錄《周易》六十四卦。可以卦名或卦序（如：乾、坤、1）搜尋，點任一卦即展開詳情：
卦象、梅花易數屬性、體用生克三種情境、卦辭、彖傳、大象傳、六爻爻辭、分爻小象傳、白話淺釋與歷代注解。

【歷史】
查看本次應用內搖卦的卦象紀錄。

【書籍】
內建純文字閱讀器，點「匯入」選擇 .txt 檔案即可閱讀。閱讀時點擊畫面中央可顯示或隱藏工具列，並可即時調整字級與行距。

【設定】
可切換主題模式（跟隨系統／淺色／深色）、動態取色，並可調整閱讀字級、行距與段落間距，亦可重置所有設定。

【貼士】
心誠則靈，一事一卦。同一件事短期內不宜重複占問。
""".trimIndent()

private val LEGAL_TEXT = """
【版權】
・《周易》經文與王弼、孔穎達、朱熹等歷代注疏皆屬公共領域文獻，本應用以繁體中文整理校訂。
・應用程式「易卜」之程式碼、版面設計與圖示，著作權歸原作者所有，未經授權不得商用或改作發布。

【授權】
・本應用以個人學習與研究為目的免費發布。
・著作權相關標示依發布版本隨附之 LICENSE 檔為準。

【免責聲明】
・占卜結果僅供易理研究與娛樂參考，不構成醫療、法律、投資或其他專業建議。
・使用者應自行承擔使用本應用之風險與判斷，一切決策請以理性與事實為準。
・本應用不蒐集、不傳送任何個人資料，所有資料皆儲存於本機。
""".trimIndent()