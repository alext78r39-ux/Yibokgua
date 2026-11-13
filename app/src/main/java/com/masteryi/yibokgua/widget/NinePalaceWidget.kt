package com.masteryi.yibokgua.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
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
import com.masteryi.yibokgua.ui.theme.YiGoldLight
import com.masteryi.yibokgua.ui.theme.YiOnPaper
import com.masteryi.yibokgua.ui.theme.YiOnSurfaceVariantLight
import com.masteryi.yibokgua.ui.theme.YiPaper
import com.masteryi.yibokgua.ui.theme.YiRedLight
import com.masteryi.yibokgua.ui.theme.ZenOnAccentLight
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

        provideContent {
            NinePalaceWidgetContent(
                result = result,
                dateLabel = dateLabel,
                hourLabel = hourLabel,
                openAction = openAction
            )
        }
    }
}

@Composable
private fun NinePalaceWidgetContent(
    result: NinePalaceResult,
    dateLabel: String,
    hourLabel: String,
    openAction: Action
) {
    val paper = ColorProvider(YiPaper)
    val onPaper = ColorProvider(YiOnPaper)
    val primary = ColorProvider(YiRedLight)
    val gold = ColorProvider(YiGoldLight)
    val variant = ColorProvider(YiOnSurfaceVariantLight)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(paper)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = "易卜卦 · 九宮六壬",
                style = TextStyle(color = gold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "$dateLabel · $hourLabel",
                style = TextStyle(color = variant, fontSize = 11.sp),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .clickable(openAction),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GuaPill("月・背景", result.monthGua, false, primary, onPaper, variant, GlanceModifier.defaultWeight())
            GuaPill("日・過程", result.dayGua, false, primary, onPaper, variant, GlanceModifier.defaultWeight())
            GuaPill("時・主卦", result.hourGua, true, primary, onPaper, variant, GlanceModifier.defaultWeight())
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Button(
            text = "開啟九宮六壬",
            onClick = openAction,
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = primary,
                contentColor = ColorProvider(ZenOnAccentLight)
            )
        )
    }
}

@Composable
private fun GuaPill(
    label: String,
    gua: NinePalaceGua,
    highlighted: Boolean,
    primary: ColorProvider,
    onPaper: ColorProvider,
    variant: ColorProvider,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = TextStyle(color = variant, fontSize = 10.sp),
            maxLines = 1
        )
        Text(
            text = gua.guaName,
            style = TextStyle(
                color = if (highlighted) primary else onPaper,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        Text(
            text = "${gua.number}・${gua.element}",
            style = TextStyle(color = variant, fontSize = 10.sp),
            maxLines = 1
        )
    }
}
