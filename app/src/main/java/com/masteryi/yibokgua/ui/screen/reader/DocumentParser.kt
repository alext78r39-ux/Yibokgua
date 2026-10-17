package com.masteryi.yibokgua.ui.screen.reader

import java.io.File

/**
 * 依書本格式載入內容。文字格式解析為段落區塊；PDF 僅計頁數，逐頁延遲渲染。
 *
 * 此類別會使用 Android 平台 API（PDF），故不屬於純邏輯；純文字解析見
 * [TextDecoder]、[MarkdownParser]、[DocxExtractor]、[DocExtractor]。
 */
object DocumentParser {

    fun parse(book: Book): BookContent {
        val file = File(book.path)
        if (!file.exists()) error("找不到檔案，可能已被刪除")

        return when (book.format) {
            BookFormat.PDF -> BookContent.Pdf(book.path, PdfHelper.pageCount(book.path))
            BookFormat.TXT -> BookContent.Text(TextDecoder.toBlocks(TextDecoder.decodeText(file.readBytes())))
            BookFormat.MD -> BookContent.Text(MarkdownParser.parse(TextDecoder.decodeText(file.readBytes())))
            BookFormat.DOCX -> BookContent.Text(DocxExtractor.extract(file.readBytes()))
            BookFormat.DOC -> BookContent.Text(DocExtractor.extract(file.readBytes()))
        }
    }
}
