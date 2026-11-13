package com.masteryi.yibokgua.ui.screen.ninepalace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.masteryi.core.model.iching.LunarDate
import com.masteryi.engine.iching.hourBranchNumber
import com.masteryi.engine.ninepalace.NinePalaceCalculator
import com.masteryi.engine.ninepalace.NinePalaceGua
import com.masteryi.engine.ninepalace.NinePalaceInterpreter
import com.masteryi.engine.ninepalace.toCardText
import com.masteryi.yibokgua.ui.components.AdaptiveScreen
import com.masteryi.yibokgua.ui.components.ZenCard
import com.masteryi.yibokgua.ui.components.zenThinBorder
import java.time.LocalDate
import java.time.LocalTime

private val MONTH_NAMES = listOf(
    "正月", "二月", "三月", "四月", "五月", "六月",
    "七月", "八月", "九月", "十月", "十一月", "十二月"
)

private val HOUR_NAMES = listOf(
    "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
)

/** 九宮六壬：自動帶入當下農曆月／日／時支，可手動調整後起卦。 */
@Composable
fun NinePalaceScreen() {
    val todayLunar = remember { runCatching { LunarDate.of(LocalDate.now()) }.getOrNull() }
    var month by rememberSaveable { mutableIntStateOf(todayLunar?.month ?: 1) }
    var day by rememberSaveable { mutableIntStateOf(todayLunar?.day ?: 1) }
    var hourIndex by rememberSaveable {
        mutableIntStateOf(hourBranchNumber(LocalTime.now().hour))
    }
    var result by remember {
        mutableStateOf(NinePalaceCalculator.calculate(month, day, hourIndex))
    }
    var dialogGua by remember { mutableStateOf<NinePalaceGua?>(null) }

    fun loadNow() {
        val lunar = runCatching { LunarDate.of(LocalDate.now()) }.getOrNull()
        if (lunar != null) {
            month = lunar.month
            day = lunar.day
        }
        hourIndex = hourBranchNumber(LocalTime.now().hour)
        result = NinePalaceCalculator.calculate(month, day, hourIndex)
    }

    AdaptiveScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NinePalaceHeader(todayLunar = todayLunar)

            ZenCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "以農曆月、日、時支取數：月定基、日續推、時定局。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NinePalaceDropdown(
                        label = "農曆月",
                        value = MONTH_NAMES[month - 1],
                        options = MONTH_NAMES,
                        onSelect = { month = it + 1 }
                    )
                    NinePalaceDropdown(
                        label = "農曆日",
                        value = "$day 日",
                        options = (1..30).map { "$it 日" },
                        onSelect = { day = it + 1 }
                    )
                    NinePalaceDropdown(
                        label = "時辰",
                        value = "${HOUR_NAMES[hourIndex - 1]}時（$hourIndex）",
                        options = HOUR_NAMES.mapIndexed { index, name -> "${name}時（${index + 1}）" },
                        onSelect = { hourIndex = it + 1 }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { loadNow() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("帶入現在")
                        }
                        Button(
                            onClick = {
                                result = NinePalaceCalculator.calculate(month, day, hourIndex)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("起卦")
                        }
                    }
                }
            }

            Text(
                "所選：${MONTH_NAMES[month - 1]}・${day}日・${HOUR_NAMES[hourIndex - 1]}時",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GuaResultCard(
                    label = "月卦・背景",
                    gua = result.monthGua,
                    highlighted = false,
                    onClick = { dialogGua = result.monthGua },
                    modifier = Modifier.weight(1f)
                )
                GuaResultCard(
                    label = "日卦・過程",
                    gua = result.dayGua,
                    highlighted = false,
                    onClick = { dialogGua = result.dayGua },
                    modifier = Modifier.weight(1f)
                )
                GuaResultCard(
                    label = "時卦・主卦",
                    gua = result.hourGua,
                    highlighted = true,
                    onClick = { dialogGua = result.hourGua },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "九宮盤",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "時宮為主卦",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NinePalaceGua.palaceOrder.chunked(3).forEach { rowGua ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowGua.forEach { gua ->
                            NinePalaceCell(
                                gua = gua,
                                highlighted = gua == result.hourGua,
                                onClick = { dialogGua = gua },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }

            Text(
                "三層關聯",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ZenCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = NinePalaceInterpreter.describe(result),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.6f
                )
            }

            Text(
                "一事一卦，再占不靈；象意優先，五行輔助。",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }

    dialogGua?.let { gua ->
        AlertDialog(
            onDismissRequest = { dialogGua = null },
            title = { Text("${gua.guaName}（第${gua.number}卦）") },
            text = { Text(gua.toCardText()) },
            confirmButton = {
                TextButton(onClick = { dialogGua = null }) { Text("知道了") }
            }
        )
    }
}

@Composable
private fun NinePalaceHeader(todayLunar: LunarDate?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "九宮六壬",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "一事一卦，再占不靈。月定基、日續推、時定局，最終以時宮為主卦。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        todayLunar?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "今日農曆：${it.text}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun GuaResultCard(
    label: String,
    gua: NinePalaceGua,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ZenCard(
        modifier = modifier,
        containerColor = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = zenThinBorder(
            if (highlighted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                gua.guaName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlighted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${gua.number}・${gua.element}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                gua.direction,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NinePalaceCell(
    gua: NinePalaceGua,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ZenCard(
        modifier = modifier,
        containerColor = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = zenThinBorder(
            if (highlighted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                gua.guaName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (highlighted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${gua.number}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                gua.direction,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NinePalaceDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(value)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
