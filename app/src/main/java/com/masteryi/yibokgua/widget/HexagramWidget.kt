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
import com.masteryi.yibokgua.MainActivity
import com.masteryi.yibokgua.ui.theme.OnYiGoldContainer
import com.masteryi.yibokgua.ui.theme.YiGoldContainer
import com.masteryi.yibokgua.ui.theme.YiGoldLight
import com.masteryi.yibokgua.ui.theme.YiOnPaper
import com.masteryi.yibokgua.ui.theme.YiOnSurfaceVariantLight
import com.masteryi.yibokgua.ui.theme.YiPaper
import com.masteryi.yibokgua.ui.theme.YiRedLight
import com.masteryi.yibokgua.ui.theme.ZenOnAccentLight
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 「易卜卦」桌面小工具：上半為每日一卦，下方提供「起卦」與「換一卦」。
 * 使用 Glance（Compose 風格）實作，無 XML 版面。
 */
class HexagramWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val epochDay = today.toEpochDay()
        val state = WidgetStateRepository(context).state()
        val hexagram = resolveHexagram(WidgetHexagram.resolveId(state, epochDay))
        val dateLabel = today.format(DateTimeFormatter.ofPattern("M月d日"))

        val detailAction = actionStartActivity(
            launchIntent(context) { putExtra(MainActivity.EXTRA_HEXAGRAM_ID, hexagram.id) }
        )
        val divinationAction = actionStartActivity(
            launchIntent(context) { putExtra(MainActivity.EXTRA_OPEN_DIVINATION, true) }
        )
        val refreshAction = actionRunCallback<RefreshWidgetAction>()

        provideContent {
            WidgetContent(
                hexagram = hexagram,
                dateLabel = dateLabel,
                detailAction = detailAction,
                divinationAction = divinationAction,
                refreshAction = refreshAction
            )
        }
    }

    private fun resolveHexagram(id: Int): Hexagram {
        val repository = HexagramRepository()
        return repository.getHexagramById(id) ?: repository.getAllHexagrams().first()
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

@Composable
private fun WidgetContent(
    hexagram: Hexagram,
    dateLabel: String,
    detailAction: Action,
    divinationAction: Action,
    refreshAction: Action
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
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "易卜卦 · 今日之卦",
                style = TextStyle(color = gold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = dateLabel,
                style = TextStyle(color = variant, fontSize = 11.sp)
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .clickable(detailAction),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = hexagram.symbol,
                style = TextStyle(color = primary, fontSize = 34.sp)
            )
            Spacer(modifier = GlanceModifier.width(10.dp))
            Column {
                Text(
                    text = hexagram.name,
                    style = TextStyle(color = onPaper, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "上${hexagram.upperTrigram.symbol}${hexagram.upperTrigram.nameZh} · " +
                        "下${hexagram.lowerTrigram.symbol}${hexagram.lowerTrigram.nameZh}",
                    style = TextStyle(color = variant, fontSize = 11.sp)
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                text = "起卦",
                onClick = divinationAction,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = primary,
                    contentColor = ColorProvider(ZenOnAccentLight)
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Button(
                text = "換一卦",
                onClick = refreshAction,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorProvider(YiGoldContainer),
                    contentColor = ColorProvider(OnYiGoldContainer)
                )
            )
        }
    }
}
