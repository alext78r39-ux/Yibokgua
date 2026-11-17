package com.masteryi.yibokgua.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.action.Action
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.masteryi.core.model.iching.LunarDate
import com.masteryi.engine.iching.hourBranchNumber
import com.masteryi.engine.ninepalace.NinePalaceCalculator
import com.masteryi.engine.ninepalace.NinePalaceGua
import com.masteryi.engine.ninepalace.NinePalaceResult
import com.masteryi.yibokgua.MainActivity
import com.masteryi.yibokgua.ui.theme.YiDarkPalette
import com.masteryi.yibokgua.ui.theme.YiLightPalette
import com.masteryi.yibokgua.ui.theme.YiPalette
import java.time.LocalDate
import java.time.LocalTime

private val WIDGET_HOUR_NAMES = listOf(
    "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
)

/**
 * 「易卜卦」九宮六壬桌面小工具：依當下農曆月、日、時支顯示三卦，
 * 時卦（主卦）以主色標示，點擊可開啟完整九宮六壬頁面。
 */
class NinePalaceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val lunar = runCatching { LunarDate.of(today) }.getOrNull()
        val month = lunar?.month ?: 1
        val day = lunar?.day ?: 1
        val hourIndex = hourBranchNumber(LocalTime.now().hour)
        val result = NinePalaceCalculator.calculate(month, day, hourIndex)

        val dateLabel = lunar?.let { "${it.monthLabel}${it.dayLabel}" } ?: today.toString()
        val hourLabel = "${WIDGET_HOUR_NAMES[hourIndex - 1]}時"
        val openAction = actionStartActivity(
            Intent(context, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                putExtra(MainActivity.EXTRA_OPEN_NINE_PALACE, true)
            }
        )

        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val palette = if (uiMode == Configuration.UI_MODE_NIGHT_YES) YiDarkPalette else YiLightPalette

        provideContent {
            NinePalaceWidgetContent(
                result = result,
                dateLabel = dateLabel,
                hourLabel = hourLabel,
                openAction = openAction,
                palette = palette
            )
        }
    }
}

@Composable
private fun NinePalaceWidgetContent(
    result: NinePalaceResult,
    dateLabel: String,
    hourLabel: String,
    openAction: Action,
    palette: YiPalette
) {
    val layout = currentWidgetLayout(fallbackWidth = 250.dp, fallbackHeight = 150.dp)
    val largeText = layout.isLargeText
    val compact = layout.isCompact
    val wide = layout.width >= 320.dp && !compact
    val padding = when {
        layout.isShort -> 10.dp
        wide -> 14.dp
        else -> 12.dp
    }
    val titleSize = when {
        largeText -> 12.sp
        wide -> 14.sp
        compact -> 12.sp
        else -> 13.sp
    }
    val dateSize = when {
        largeText -> 10.sp
        wide -> 12.sp
        else -> 11.sp
    }
    val guaLabelSize = when {
        largeText -> 10.sp
        wide -> 12.sp
        compact -> 10.sp
        else -> 11.sp
    }
    val guaNameSize = when {
        largeText -> 18.sp
        wide -> 24.sp
        compact -> 19.sp
        else -> 21.sp
    }
    val guaMetaSize = when {
        largeText -> 10.sp
        wide -> 12.sp
        compact -> 10.sp
        else -> 11.sp
    }
    val buttonSize = when {
        largeText -> 13.sp
        wide -> 15.sp
        compact -> 13.sp
        else -> 14.sp
    }
    val showDate = !largeText || layout.height >= 190.dp
    val showGuaLabel = !largeText || layout.height >= 190.dp
    val showGuaMeta = !largeText || layout.height >= 190.dp
    val stackHeader = (largeText && showDate) || (!largeText && layout.width < 280.dp)
    val gap = if (layout.isShort) 4.dp else 6.dp

    val paper = ColorProvider(palette.paper)
    val onPaper = ColorProvider(palette.ink)
    val primary = ColorProvider(palette.inkSecondary)
    val buttonBackground = ColorProvider(palette.ink)
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
                    text = "易卜卦 · 九宮六壬",
                    style = TextStyle(color = label, fontSize = titleSize, fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
                if (showDate) {
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "$dateLabel · $hourLabel",
                        style = TextStyle(color = variant, fontSize = dateSize),
                        maxLines = 1
                    )
                }
            }
        } else {
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = "易卜卦 · 九宮六壬",
                    style = TextStyle(color = label, fontSize = titleSize, fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                if (showDate) {
                    Text(
                        text = "$dateLabel · $hourLabel",
                        style = TextStyle(color = variant, fontSize = dateSize),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(gap))

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .clickable(openAction),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GuaPill(
                label = if (compact) "月" else "月・背景",
                gua = result.monthGua,
                highlighted = false,
                showLabel = showGuaLabel,
                showMeta = showGuaMeta,
                labelSize = guaLabelSize,
                nameSize = guaNameSize,
                metaSize = guaMetaSize,
                primary = primary,
                onPaper = onPaper,
                variant = variant,
                modifier = GlanceModifier.defaultWeight()
            )
            GuaPill(
                label = if (compact) "日" else "日・過程",
                gua = result.dayGua,
                highlighted = false,
                showLabel = showGuaLabel,
                showMeta = showGuaMeta,
                labelSize = guaLabelSize,
                nameSize = guaNameSize,
                metaSize = guaMetaSize,
                primary = primary,
                onPaper = onPaper,
                variant = variant,
                modifier = GlanceModifier.defaultWeight()
            )
            GuaPill(
                label = if (compact) "時" else "時・主卦",
                gua = result.hourGua,
                highlighted = true,
                showLabel = showGuaLabel,
                showMeta = showGuaMeta,
                labelSize = guaLabelSize,
                nameSize = guaNameSize,
                metaSize = guaMetaSize,
                primary = primary,
                onPaper = onPaper,
                variant = variant,
                modifier = GlanceModifier.defaultWeight()
            )
        }

        Spacer(modifier = GlanceModifier.height(gap))

        Button(
            text = if (largeText || layout.width < 280.dp) "開啟" else "開啟九宮六壬",
            onClick = openAction,
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(fontSize = buttonSize, fontWeight = FontWeight.Medium),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = buttonBackground,
                contentColor = paper
            )
        )
    }
}

@Composable
private fun GuaPill(
    label: String,
    gua: NinePalaceGua,
    highlighted: Boolean,
    showLabel: Boolean,
    showMeta: Boolean,
    labelSize: TextUnit,
    nameSize: TextUnit,
    metaSize: TextUnit,
    primary: ColorProvider,
    onPaper: ColorProvider,
    variant: ColorProvider,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLabel) {
            Text(
                text = label,
                style = TextStyle(color = variant, fontSize = labelSize),
                maxLines = 1
            )
        }
        Text(
            text = gua.guaName,
            style = TextStyle(
                color = if (highlighted) primary else onPaper,
                fontSize = nameSize,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        if (showMeta) {
            Text(
                text = "${gua.number}・${gua.element}",
                style = TextStyle(color = variant, fontSize = metaSize),
                maxLines = 1
            )
        }
    }
}
