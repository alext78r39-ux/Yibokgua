package com.masteryi.yibokgua.ui.screen.reader

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import kotlin.math.roundToInt

/** 以 Android 內建 [PdfRenderer] 讀取 PDF 頁數與渲染單頁。 */
object PdfHelper {

    fun pageCount(filePath: String): Int {
        val file = File(filePath)
        if (!file.exists()) error("找不到 PDF 檔案")
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { it.pageCount }
        }
    }

    /**
     * 將第 [pageIndex] 頁渲染為寬度 [targetWidthPx] 的點陣圖；失敗時回傳 null。
     * 每次呼叫獨立開啟 [PdfRenderer]，避免跨執行緒共用造成的問題。
     */
    fun renderPage(filePath: String, pageIndex: Int, targetWidthPx: Int): ImageBitmap? {
        val file = File(filePath)
        if (!file.exists() || targetWidthPx <= 0) return null
        return runCatching {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    if (pageIndex !in 0 until renderer.pageCount) return null
                    renderer.openPage(pageIndex).use { page ->
                        val scale = targetWidthPx.toFloat() / page.width
                        val height = (page.height * scale).roundToInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(targetWidthPx, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap.asImageBitmap()
                    }
                }
            }
        }.getOrNull()
    }
}
