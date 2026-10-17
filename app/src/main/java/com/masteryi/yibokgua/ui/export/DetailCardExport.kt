package com.masteryi.yibokgua.ui.export

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.masteryi.core.model.iching.DivinationResult
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.core.model.iching.TiYongAnalysis
import com.masteryi.yibokgua.settings.ThemeMode
import com.masteryi.yibokgua.ui.theme.YibokguaTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 將起卦結果渲染成詳情卡片圖片並透過系統分享面板導出。
 */
fun shareDetailCardAsImage(
    context: Context,
    result: DivinationResult,
    tiYongAnalysis: TiYongAnalysis?
) {
    val activity = context.findActivity()
    val root = activity?.findViewById<ViewGroup>(android.R.id.content)
    if (activity == null || root == null) {
        Toast.makeText(context, "導出失敗：找不到可用的畫面", Toast.LENGTH_SHORT).show()
        return
    }

    renderDetailCardToBitmap(activity, root, result, tiYongAnalysis) { bitmap ->
        try {
            val dir = File(activity.cacheDir, "export").apply { mkdirs() }
            val file = File(dir, "divination_${result.timestamp}.png")
            file.outputStream().buffered().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity.startActivity(Intent.createChooser(sendIntent, "導出詳情卡片"))
        } catch (e: Exception) {
            Toast.makeText(activity, "導出失敗：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * 將內容以指定設計倍率離線渲染為點陣圖（寬 360dp × 密度 3 = 1080px，高度依內容自動延展）。
 *
 * Compose 需附加至視窗才能建立 composition，故先將透明的 ComposeView 掛到 Activity
 * 內容根檢視，待第一帧完成合成與佈局後，再以不受高度限制的規格重新量測並繪製，
 * 最後移除該檢視。結果透過 [onReady] 非同步回傳。
 */
private fun renderDetailCardToBitmap(
    context: Context,
    root: ViewGroup,
    result: DivinationResult,
    tiYongAnalysis: TiYongAnalysis?,
    onReady: (Bitmap) -> Unit
) {
    val density = 3f
    val cardWidthPx = (360 * density).toInt()

    val composeView = ComposeView(context).apply {
        setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density, fontScale = 1f),
                LocalLayoutDirection provides LayoutDirection.Ltr
            ) {
                YibokguaTheme(themeMode = ThemeMode.LIGHT) {
                    DivinationDetailCard(
                        result = result,
                        tiYongAnalysis = tiYongAnalysis
                    )
                }
            }
        }
        alpha = 0f
    }

    root.addView(
        composeView,
        ViewGroup.LayoutParams(cardWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
    )

    composeView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            // 等待 composition 完成並完成首次佈局
            if (composeView.height <= 0) return true
            composeView.viewTreeObserver.removeOnPreDrawListener(this)
            val bitmap = runCatching {
                composeView.measure(
                    View.MeasureSpec.makeMeasureSpec(cardWidthPx, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val heightPx = composeView.measuredHeight
                composeView.layout(0, 0, cardWidthPx, heightPx)
                Bitmap.createBitmap(cardWidthPx, heightPx, Bitmap.Config.ARGB_8888).also {
                    composeView.draw(Canvas(it))
                }
            }.getOrNull()
            root.removeView(composeView)
            if (bitmap != null) {
                onReady(bitmap)
            } else {
                Toast.makeText(context, "導出失敗：無法產生圖片", Toast.LENGTH_SHORT).show()
            }
            return true
        }
    })
}

/** 導出用詳情卡片版面（固定為淺色主題，確保輸出一致） */
@Composable
fun DivinationDetailCard(
    result: DivinationResult,
    tiYongAnalysis: TiYongAnalysis?
) {
    val timeStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        .format(Date(result.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text(
            text = "易卜・起卦詳情",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = result.divinationType,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        result.question.takeIf { it.isNotBlank() }?.let { question ->
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "所占之事：$question",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        result.divinationText.takeIf { it.isNotBlank() }?.let { text ->
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "占字：$text",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = timeStr,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExportHexagramBox(
                label = "本卦",
                hexagram = result.originalHexagram,
                modifier = Modifier.weight(1f)
            )
            result.mutatedHexagram?.let { mutated ->
                ExportHexagramBox(
                    label = "變卦",
                    hexagram = mutated,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        result.movingYaoIndex?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "動爻：第 $it 爻",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "【六爻】",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        for (i in 5 downTo 0) {
            val yao = result.yaos[i]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "爻 ${i + 1}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = yao.symbol,
                        fontSize = 17.sp,
                        fontWeight = if (yao.isMoving) FontWeight.Bold else FontWeight.Normal,
                        color = if (yao.isMoving) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${yao.code} ${yao.label}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        tiYongAnalysis?.let { analysis ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "【體用生克】",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            analysis.summary.split("\n").forEach { line ->
                Text(
                    text = "・$line",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "心誠則靈，一事一卦。",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** 導出卡片內的本卦 / 變卦展示塊 */
@Composable
private fun ExportHexagramBox(
    label: String,
    hexagram: Hexagram,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = hexagram.symbol,
            fontSize = 46.sp
        )
        Text(
            text = hexagram.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "上${hexagram.upperTrigram.symbol}${hexagram.upperTrigram.nameZh}・下${hexagram.lowerTrigram.symbol}${hexagram.lowerTrigram.nameZh}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}