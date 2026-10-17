package com.masteryi.yibokgua.ui.screen.reader

/** 文字文件的段落樣式。 */
enum class BlockKind { H1, H2, H3, PARAGRAPH, BULLET, ORDERED, QUOTE, CODE, RULE }

/** 一段可渲染的文字內容。 */
data class ReaderBlock(val text: String, val kind: BlockKind)

/** 已載入的文件內容。 */
sealed interface BookContent {
    /** txt / md / docx / doc：已解析為段落。 */
    data class Text(val blocks: List<ReaderBlock>) : BookContent

    /** pdf：保留檔案路徑與頁數，逐頁延遲渲染。 */
    data class Pdf(val filePath: String, val pageCount: Int) : BookContent
}
