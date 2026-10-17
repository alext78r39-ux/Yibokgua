package com.masteryi.yibokgua.ui.screen.reader

/**
 * 輕量 Markdown 解析（區塊層級）。
 *
 * 支援標題（#、##、### 以上收斂為 H3）、無序清單、有序清單、引用、程式碼區塊與水平線。
 * 行內樣式（粗體／斜體／等寬）於 UI 層以 [inlineMarkdown] 處理。
 */
object MarkdownParser {

    private val HORIZONTAL_RULE = Regex("^(?:-{3,}|\\*{3,}|_{3,})$")
    private val BULLET = Regex("^[-*+]\\s+.*")
    private val ORDERED = Regex("^\\d+[.)]\\s+.*")

    fun parse(markdown: String): List<ReaderBlock> {
        val blocks = mutableListOf<ReaderBlock>()
        var inCode = false
        val code = StringBuilder()

        for (rawLine in markdown.split('\n')) {
            val line = rawLine.trimEnd()

            if (line.trimStart().startsWith("```")) {
                if (inCode) {
                    blocks += ReaderBlock(code.toString().trimEnd('\n'), BlockKind.CODE)
                    code.clear()
                    inCode = false
                } else {
                    inCode = true
                }
                continue
            }
            if (inCode) {
                code.append(line).append('\n')
                continue
            }

            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> Unit
                HORIZONTAL_RULE.matches(trimmed) -> blocks += ReaderBlock("", BlockKind.RULE)
                trimmed.startsWith("###### ") -> blocks += heading(trimmed, 6)
                trimmed.startsWith("##### ") -> blocks += heading(trimmed, 5)
                trimmed.startsWith("#### ") -> blocks += heading(trimmed, 4)
                trimmed.startsWith("### ") -> blocks += heading(trimmed, 3)
                trimmed.startsWith("## ") -> blocks += heading(trimmed, 2)
                trimmed.startsWith("# ") -> blocks += heading(trimmed, 1)
                trimmed == ">" -> blocks += ReaderBlock("", BlockKind.QUOTE)
                trimmed.startsWith("> ") -> blocks += ReaderBlock(trimmed.removePrefix("> ").trim(), BlockKind.QUOTE)
                BULLET.matches(trimmed) -> blocks += ReaderBlock(trimmed, BlockKind.BULLET)
                ORDERED.matches(trimmed) -> blocks += ReaderBlock(trimmed, BlockKind.ORDERED)
                else -> blocks += ReaderBlock(trimmed, BlockKind.PARAGRAPH)
            }
        }

        if (inCode && code.isNotEmpty()) {
            blocks += ReaderBlock(code.toString().trimEnd('\n'), BlockKind.CODE)
        }
        return blocks
    }

    private fun heading(line: String, level: Int): ReaderBlock {
        val text = line.dropWhile { it == '#' }.trim()
        val kind = when (level) {
            1 -> BlockKind.H1
            2 -> BlockKind.H2
            else -> BlockKind.H3
        }
        return ReaderBlock(text, kind)
    }
}
