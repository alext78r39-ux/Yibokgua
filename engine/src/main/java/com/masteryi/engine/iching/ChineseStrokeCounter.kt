package com.masteryi.engine.iching

import java.lang.Character.UnicodeScript

/**
 * 中文字筆畫數計算（繁體為準）
 *
 * 資料來源：Unicode Han Database（Unihan）之 `kTotalStrokes` 屬性，
 * 取自 unicode-org/unicodetools 專案（`unicodetools/data/ucd/dev/Unihan/kTotalStrokes.txt`），
 * 已內建於 `src/main/resources/unihan_stroke_counts.txt`。
 *
 * 涵蓋 BMP 內之漢字：CJK 基本區（U+4E00–U+9FFF）、擴充 A 區（U+3400–U+4DBF）
 * 與相容表意文字（U+F900–U+FAFF），漢字覆蓋率約 100%
 * （U+4DC0–U+4DFF 為易經六十四卦符號，非漢字，故不在資料內）。
 *
 * 每個碼位採該字本身之筆畫數，因此繁體碼位與簡體碼位各自正確、不互相混淆：
 * 例「請」U+8ACB = 15、「请」U+8BF7 = 10；「為」U+70BA = 9、「为」U+4E3A = 4。
 */
object ChineseStrokeCounter {

    private const val RESOURCE = "/unihan_stroke_counts.txt"

    /**
     * 資料未涵蓋之罕用字（多為擴充 B 區以上，如 U+20000 起）時採用之保守估計筆畫數。
     * 因 BMP 內漢字覆蓋率近 100%，實務上僅極罕用字會落到此值。
     */
    const val FALLBACK_STROKES = 6

    private class Table(val starts: IntArray, val ends: IntArray, val strokes: IntArray)

    private val table: Table by lazy { load() }

    private fun load(): Table {
        val starts = ArrayList<Int>()
        val ends = ArrayList<Int>()
        val values = ArrayList<Int>()

        val stream = ChineseStrokeCounter::class.java.getResourceAsStream(RESOURCE)
            ?: error("缺少筆畫資料資源：$RESOURCE")
        stream.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.forEachLine { raw ->
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#")) return@forEachLine
                val tab = line.indexOf('\t')
                if (tab <= 0) return@forEachLine
                val rangePart = line.substring(0, tab)
                val value = line.substring(tab + 1).toIntOrNull() ?: return@forEachLine
                val dot = rangePart.indexOf("..")
                val start: Int
                val end: Int
                if (dot >= 0) {
                    start = rangePart.substring(0, dot).toIntOrNull(16) ?: return@forEachLine
                    end = rangePart.substring(dot + 2).toIntOrNull(16) ?: return@forEachLine
                } else {
                    start = rangePart.toIntOrNull(16) ?: return@forEachLine
                    end = start
                }
                starts.add(start)
                ends.add(end)
                values.add(value)
            }
        }

        // 資料檔應以碼位遞增排列；此處保險起見再排序一次以確保二分搜尋正確。
        val order = starts.indices.sortedBy { starts[it] }
        return Table(
            IntArray(order.size) { starts[order[it]] },
            IntArray(order.size) { ends[order[it]] },
            IntArray(order.size) { values[order[it]] }
        )
    }

    private fun lookup(codePoint: Int): Int? {
        val t = table
        var lo = 0
        var hi = t.starts.size - 1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            when {
                codePoint < t.starts[mid] -> hi = mid - 1
                codePoint > t.ends[mid] -> lo = mid + 1
                else -> return t.strokes[mid]
            }
        }
        return null
    }

    /** 該碼位是否為漢字且已查得權威筆畫資料。 */
    fun hasData(codePoint: Int): Boolean {
        if (UnicodeScript.of(codePoint) != UnicodeScript.HAN) return false
        return lookup(codePoint) != null
    }

    /**
     * 取得單一碼位之筆畫數。
     * - 非漢字（標點、空白、emoji 等）回傳 0。
     * - 漢字但資料未涵蓋時回傳 [FALLBACK_STROKES]（6）。
     */
    fun getStrokes(codePoint: Int): Int {
        if (UnicodeScript.of(codePoint) != UnicodeScript.HAN) return 0
        return lookup(codePoint) ?: FALLBACK_STROKES
    }

    /** 累計字串中所有漢字之筆畫數（依各字本身之繁簡筆畫，非漢字不計）。 */
    fun totalStrokes(text: String): Int =
        text.codePoints()
            .map { getStrokes(it) }
            .sum()
}
