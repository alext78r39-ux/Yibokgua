package com.masteryi.yibokgua

import com.masteryi.yibokgua.ui.screen.reader.Book
import com.masteryi.yibokgua.ui.screen.reader.BookFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookTest {

    @Test
    fun fromFileName_辨識支援格式() {
        assertEquals(BookFormat.TXT, BookFormat.fromFileName("notes.txt"))
        assertEquals(BookFormat.TXT, BookFormat.fromFileName("NOTES.TEXT"))
        assertEquals(BookFormat.MD, BookFormat.fromFileName("readme.md"))
        assertEquals(BookFormat.MD, BookFormat.fromFileName("readme.markdown"))
        assertEquals(BookFormat.PDF, BookFormat.fromFileName("周易.pdf"))
        assertEquals(BookFormat.DOC, BookFormat.fromFileName("舊稿.doc"))
        assertEquals(BookFormat.DOCX, BookFormat.fromFileName("新稿.docx"))
    }

    @Test
    fun fromFileName_不支援格式回傳null() {
        assertNull(BookFormat.fromFileName("image.png"))
        assertNull(BookFormat.fromFileName("noextension"))
    }

    @Test
    fun fromMimeType_辨識支援格式() {
        assertEquals(BookFormat.TXT, BookFormat.fromMimeType("text/plain"))
        assertEquals(BookFormat.MD, BookFormat.fromMimeType("text/markdown"))
        assertEquals(BookFormat.PDF, BookFormat.fromMimeType("application/pdf"))
        assertEquals(BookFormat.DOC, BookFormat.fromMimeType("application/msword"))
        assertEquals(
            BookFormat.DOCX,
            BookFormat.fromMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        )
        assertNull(BookFormat.fromMimeType(null))
    }

    @Test
    fun encodeDecode_來回一致() {
        val books = listOf(
            Book(
                id = "id-1",
                title = "含\t定位與\n換行\\反斜線",
                fileName = "weird.txt",
                format = BookFormat.TXT,
                path = "/data/books/id-1.txt",
                sizeBytes = 1234L,
                importedAt = 1000L,
                lastReadAt = 2000L,
                progress = 0.42f
            ),
            Book(
                id = "id-2",
                title = "周易",
                fileName = "周易.pdf",
                format = BookFormat.PDF,
                path = "/data/books/id-2.pdf",
                sizeBytes = 987654L,
                importedAt = 3000L
            )
        )

        val decoded = Book.decode(Book.encode(books))

        assertEquals(books, decoded)
    }

    @Test
    fun decode_空白或null回傳空清單() {
        assertEquals(emptyList<Book>(), Book.decode(null))
        assertEquals(emptyList<Book>(), Book.decode(""))
        assertEquals(emptyList<Book>(), Book.decode("   \n  "))
    }

    @Test
    fun decode_欄位不足之壞資料被忽略() {
        assertEquals(emptyList<Book>(), Book.decode("only\ttwo\tfields"))
    }
}
