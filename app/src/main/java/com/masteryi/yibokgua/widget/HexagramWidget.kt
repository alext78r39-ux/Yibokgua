package com.masteryi.yibokgua.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.data.repository.HexagramRepository
import com.masteryi.engine.iching.DivinationEngine
import com.masteryi.engine.iching.EARTHLY_BRANCH_NAMES
import com.masteryi.engine.iching.MeihuaTimeCalculation
import com.masteryi.engine.iching.hourBranchNumber
import com.masteryi.yibokgua.MainActivity
import com.masteryi.yibokgua.ui.theme.YiDarkPalette
import com.masteryi.yibokgua.ui.theme.YiLightPalette
import com.masteryi.yibokgua.ui.theme.YiPalette
import java.time.LocalDateTime

/**
 * 「易卜卦」桌面小工具：以梅花易數取當下農曆年支、月、日、時支成卦。
 * 23:00 起算次日子時；顯示取數計算過程、本卦、動爻與變卦。
 */
class HexagramWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val now = LocalDateTime.now()
        val repository = HexagramRepository()
        val engine = DivinationEngine(repository::getHexagramByBinary)

        val calculation = runCatching {
            engine.calculateMeihuaTimeCalculation(now.toLocalDate(), now.hour)
        }.getOrNull()
        val hourBranchNum = calculation?.hourBranchNum ?: hourBranchNumber(now.hour)
        val hourName = EARTHLY_BRANCH_NAMES[hourBranchNum - 1]
        val dateLabel = calculation?.lunar?.let {
            "${it.ganZhiYear}年${it.monthLabel}${it.dayLabel} ${hourName}時"
        } ?: "農曆換算失敗"

        val hexagram = calculation?.result?.originalHexagram
            ?: repository.getAllHexagrams().first()
        val mutatedHexagram = calculation?.result?.mutatedHexagram
        val processText = calculation?.let(::buildProcessText)

        val detailAction = actionStartActivity(
            launchIntent(context) { putExtra(MainActivity.EXTRA_HEXAGRAM_ID, hexagram.id) }
        )
        val divinationAction = actionStartActivity(
            launchIntent(context) { putExtra(MainActivity.EXTRA_OPEN_DIVINATION, true) }
        )
        val refreshAction = actionRunCallback<RefreshWidgetAction>()
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val palette = if (uiMode == Configuration.UI_MODE_NIGHT_YES) YiDarkPalette else YiLightPalette

        provideContent {
            WidgetContent(
                hexagram = hexagram,
                mutatedHexagram = mutatedHexagram,
                processText = processText,
                dateLabel = dateLabel,
                detailAction = detailAction,
                divinationAction = divinationAction,
                refreshAction = refreshAction,
                palette = palette
            )
        }
    }

    /** 重用既有 MainActivity（透過 onNewIntent 導覽），避免堆疊重複頁面。 */
    private fun launchIntent(context: Context, configure: Intent.() -> Unit): Intent =
        Intent(context, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            configure()
        }
}

/** 產生「7+10+9=26 除 8 餘 2，上卦：兌。…」的完整計算式。 */
private fun buildProcessText(c: MeihuaTimeCalculation): String {
    fun remainder(total: Int, divisor: Int): String {
        val r = total % divisor
        return if (r == 0) "整除，記 $divisor" else "餘 $r"
    }

    val upper = c.result.originalHexagram.upperTrigram.nameZh
    val lower = c.result.originalHexagram.lowerTrigram.nameZh
    return buildString {
        append(
            "${c.yearBranchNum}+${c.lunarMonth}+${c.lunarDay}=${c.sumBase} " +
                "除 8 ${remainder(c.sumBase, 8)}，上卦：$upper。"
        )
        append(
            "${c.sumBase}+${c.hourBranchNum}=${c.sumTotal} " +
                "除 8 ${remainder(c.sumTotal, 8)}，下卦：$lower。"
        )
        append("${c.sumTotal} 除 6 ${remainder(c.sumTotal, 6)}，動爻：第 ${c.movingYaoNum} 爻")
    }
}

@Composable
private fun WidgetContent(
    hexagram: Hexagram,
    mutatedHexagram: Hexagram?,
    processText: String?,
    dateLabel: String,
    detailAction: Action,
    divinationAction: Action,
    refreshAction: Action,
    palette: YiPalette
) {
    val layout = currentWidgetLayout(fallbackWidth = 250.dp, fallbackHeight = 190.dp)
    val largeText = layout.isLargeText
    val compact = layout.isCompact
    val wide = layout.width >= 320.dp && !compact
    val padding = when {
        layout.isShort -> 10.dp
        wide -> 14.dp
        else -> 12.dp
    }
    val titleSize = when {
        largeText -> 11.sp
        wide -> 13.sp
        compact -> 12.sp
        else -> 13.sp
    }
    val dateSize = when {
        largeText -> 10.sp
        wide -> 12.sp
        else -> 11.sp
    }
    val processSize = if (wide) 11.sp else 10.sp
    val symbolSize = when {
        largeText -> 32.sp
        wide -> 44.sp
        compact -> 36.sp
        else -> 42.sp
    }
    val nameSize = when {
        largeText -> 16.sp
        wide -> 24.sp
        compact -> 19.sp
        else -> 22.sp
    }
    val metaSize = when {
        largeText -> 10.sp
        wide -> 14.sp
        else -> 12.sp
    }
    val buttonSize = when {
        largeText -> 13.sp
        wide -> 15.sp
        else -> 14.sp
    }
    val showDate = !largeText || layout.height >= 220.dp
    val showProcess = !largeText && processText != null
    val showMutated = !largeText || layout.height >= 220.dp
    val stackHeader = (largeText && showDate) || (!largeText && layout.width < 280.dp)
    val gap = if (layout.isShort) 4.dp else 6.dp

    val paper = ColorProvider(palette.paper)
    val onPaper = ColorProvider(palette.ink)
    val primary = ColorProvider(palette.ink)
    val label = ColorProvider(palette.inkSecondary)
    val variant = ColorProvider(palette.inkTertiary)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(paper)
            .padding(padding),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        if (stackHeader) {
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = "易卜卦 · 此刻之卦",
                    style = TextStyle(color = label, fontSize = titleSize, fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
                if (showDate) {
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = dateLabel,
                        style = TextStyle(color = variant, fontSize = dateSize),
                        maxLines = 1
                    )
                }
            }
        } else {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "易卜卦 · 此刻之卦",
                    style = TextStyle(color = label, fontSize = titleSize, fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                if (showDate) {
                    Text(
                        text = dateLabel,
                        style = TextStyle(color = variant, fontSize = dateSize),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(gap))

        if (showProcess) {
            Text(
                text = processText,
                style = TextStyle(color = variant, fontSize = processSize),
                maxLines = if (layout.height >= 230.dp) 4 else 2
            )
            Spacer(modifier = GlanceModifier.height(gap))
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .clickable(detailAction),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = hexagram.symbol,
                style = TextStyle(color = primary, fontSize = symbolSize)
            )
            Spacer(modifier = GlanceModifier.width(if (compact) 8.dp else 12.dp))
            Column(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = hexagram.name,
                    style = TextStyle(color = onPaper, fontSize = nameSize, fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                if (showMutated) {
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "變卦：${mutatedHexagram?.name ?: "—"}",
                        style = TextStyle(color = variant, fontSize = metaSize),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(gap))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                text = "起卦",
                onClick = divinationAction,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(fontSize = buttonSize, fontWeight = FontWeight.Medium),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = primary,
                    contentColor = paper
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Button(
                text = if (largeText || layout.width < 280.dp) "整理" else "重新整理",
                onClick = refreshAction,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(fontSize = buttonSize, fontWeight = FontWeight.Medium),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorProvider(palette.paperMuted),
                    contentColor = onPaper
                )
            )
        }
    }
}
