package com.masteryi.yibokgua

import com.masteryi.yibokgua.ui.screen.reader.BlockKind
import com.masteryi.yibokgua.ui.screen.reader.DocxExtractor
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class DocxExtractorTest {

    private fun buildDocx(documentXml: String): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry("word/document.xml"))
            zip.write(documentXml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        return out.toByteArray()
    }

    @Test
    fun extract_段落與標題() {
        val xml = """
            <w:document><w:body>
            <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t>第一章</w:t></w:r></w:p>
            <w:p><w:r><w:t>Hello</w:t></w:r><w:r><w:t xml:space="preserve"> &amp; world</w:t></w:r></w:p>
            </w:body></w:document>
        """.trimIndent()

        val blocks = DocxExtractor.extract(buildDocx(xml))

        assertEquals(2, blocks.size)
        assertEquals(BlockKind.H1, blocks[0].kind)
        assertEquals("第一章", blocks[0].text)
        assertEquals(BlockKind.PARAGRAPH, blocks[1].kind)
        assertEquals("Hello & world", blocks[1].text)
    }

    @Test
    fun extract_還原數字實體與跳脫字元() {
        assertEquals("A<B>C&D", DocxExtractor.decodeEntities("A&lt;B&gt;C&amp;D"))
        assertEquals("中", DocxExtractor.decodeEntities("&#x4E2D;"))
        assertEquals("文", DocxExtractor.decodeEntities("&#25991;"))
    }

    @Test
    fun extract_標題樣式判讀() {
        val xml = """
            <w:document><w:body>
            <w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t>第二節</w:t></w:r></w:p>
            <w:p><w:pPr><w:pStyle w:val="Title"/></w:pPr><w:r><w:t>大標題</w:t></w:r></w:p>
            </w:body></w:document>
        """.trimIndent()

        val blocks = DocxExtractor.extract(buildDocx(xml))

        assertEquals(BlockKind.H2, blocks[0].kind)
        assertEquals(BlockKind.H1, blocks[1].kind)
    }
}
