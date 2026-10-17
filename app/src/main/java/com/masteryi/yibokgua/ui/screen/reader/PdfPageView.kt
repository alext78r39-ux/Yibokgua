package com.masteryi.yibokgua.ui.screen.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 單一 PDF 頁面：依可用寬度延遲渲染為點陣圖。 */
@Composable
fun PdfPageView(
    filePath: String,
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val targetWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }.coerceAtLeast(1)
        val bitmap by produceState<ImageBitmap?>(
            initialValue = null,
            filePath,
            pageIndex,
            targetWidthPx
        ) {
            value = withContext(Dispatchers.IO) {
                PdfHelper.renderPage(filePath, pageIndex, targetWidthPx)
            }
        }

        val current = bitmap
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7071f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        } else {
            Image(
                bitmap = current,
                contentDescription = "第 ${pageIndex + 1} 頁",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
