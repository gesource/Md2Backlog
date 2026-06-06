package io.github.md2backlog.ast

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Node の基本的な構造テスト。
 * パーサー・レンダラーが実装される前に AST の定義が正しいことを確認する。
 */
class NodeTest {

    @Test
    fun `Document contains children`() {
        val doc = Node.Document(
            children = listOf(
                Node.Heading(level = 1, children = listOf(Node.Text("Hello"))),
                Node.Paragraph(children = listOf(Node.Text("World"))),
            )
        )
        assertEquals(2, doc.children.size)
    }

    @Test
    fun `Heading level range`() {
        for (level in 1..6) {
            val heading = Node.Heading(level = level, children = listOf(Node.Text("h$level")))
            assertEquals(level, heading.level)
        }
    }

    @Test
    fun `Bold wraps inline nodes`() {
        val bold = Node.Bold(children = listOf(Node.Text("bold text")))
        assertEquals(1, bold.children.size)
        assertEquals(Node.Text("bold text"), bold.children.first())
    }

    @Test
    fun `BulletList contains ListItems`() {
        val list = Node.BulletList(
            items = listOf(
                Node.ListItem(listOf(Node.Text("item 1"))),
                Node.ListItem(listOf(Node.Text("item 2"))),
            )
        )
        assertEquals(2, list.items.size)
    }

    @Test
    fun `OrderedList has start index`() {
        val list = Node.OrderedList(
            start = 1,
            items = listOf(Node.ListItem(listOf(Node.Text("first"))))
        )
        assertEquals(1, list.start)
    }

    @Test
    fun `CodeBlock preserves lang and code`() {
        val block = Node.CodeBlock(lang = "kotlin", code = "println(\"hello\")")
        assertEquals("kotlin", block.lang)
        assertEquals("println(\"hello\")", block.code)
    }

    @Test
    fun `CodeBlock with no lang`() {
        val block = Node.CodeBlock(lang = null, code = "plain code")
        assertEquals(null, block.lang)
    }

    @Test
    fun `Link has href and optional title`() {
        val link = Node.Link(
            href = "https://example.com",
            title = "Example",
            children = listOf(Node.Text("click here"))
        )
        assertEquals("https://example.com", link.href)
        assertEquals("Example", link.title)
        assertEquals(Node.Text("click here"), link.children.first())
    }

    @Test
    fun `Link with no title`() {
        val link = Node.Link(href = "https://example.com", title = null, children = emptyList())
        assertEquals(null, link.title)
    }

    @Test
    fun `Table structure`() {
        val table = Node.Table(
            header = Node.TableRow(listOf(Node.TableCell(listOf(Node.Text("Col1"))))),
            rows = listOf(
                Node.TableRow(listOf(Node.TableCell(listOf(Node.Text("Value1")))))
            )
        )
        assertEquals(1, table.header.cells.size)
        assertEquals(1, table.rows.size)
    }

    @Test
    fun `HorizontalRule is singleton object`() {
        val a = Node.HorizontalRule
        val b = Node.HorizontalRule
        assertEquals(a, b)
    }

    @Test
    fun `LineBreak is singleton object`() {
        val a = Node.LineBreak
        val b = Node.LineBreak
        assertEquals(a, b)
    }

    @Test
    fun `Image has src and alt`() {
        val img = Node.Image(src = "image.png", alt = "a cat")
        assertEquals("image.png", img.src)
        assertEquals("a cat", img.alt)
    }

    @Test
    fun `Strikethrough wraps children`() {
        val strike = Node.Strikethrough(children = listOf(Node.Text("deleted")))
        assertEquals("deleted", (strike.children.first() as Node.Text).value)
    }

    @Test
    fun `InlineCode preserves code string`() {
        val code = Node.InlineCode("val x = 1")
        assertEquals("val x = 1", code.code)
    }
}
