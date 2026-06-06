package io.github.md2backlog.converter

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Backlog 記法 → Markdown の変換テスト。
 *
 * 変換は BacklogParser → Node ツリー → MarkdownRenderer の2段階で行う。
 */
class BacklogToMarkdownTest {

    private fun convert(backlog: String): String =
        BacklogToMarkdownConverter.convert(backlog)

    // -------------------------------------------------------------------------
    // 見出し
    // -------------------------------------------------------------------------

    @Test
    fun `Backlog h1 converts to md h1`() {
        assertEquals("# Heading 1", convert("* Heading 1").trim())
    }

    @Test
    fun `Backlog h2 converts to md h2`() {
        assertEquals("## Heading 2", convert("** Heading 2").trim())
    }

    @Test
    fun `Backlog h3 converts to md h3`() {
        assertEquals("### Heading 3", convert("*** Heading 3").trim())
    }

    @Test
    fun `Backlog h4 converts to md h4`() {
        assertEquals("#### Heading 4", convert("**** Heading 4").trim())
    }

    @Test
    fun `Backlog h5 converts to md h5`() {
        assertEquals("##### Heading 5", convert("***** Heading 5").trim())
    }

    @Test
    fun `Backlog h6 converts to md h6`() {
        assertEquals("###### Heading 6", convert("****** Heading 6").trim())
    }

    // -------------------------------------------------------------------------
    // 太字・斜体・打消し線
    // -------------------------------------------------------------------------

    @Test
    fun `Backlog bold converts to md bold`() {
        assertEquals("**bold text**", convert("''bold text''").trim())
    }

    @Test
    fun `Backlog italic converts to md italic`() {
        assertEquals("*italic text*", convert("'''italic text'''").trim())
    }

    @Test
    fun `Backlog strikethrough converts to md strikethrough`() {
        assertEquals("~~deleted~~", convert("%%deleted%%").trim())
    }

    // -------------------------------------------------------------------------
    // コード
    // -------------------------------------------------------------------------

    @Test
    fun `Backlog inline code tag converts to md inline code`() {
        assertEquals("`code`", convert("{code}code{/code}").trim())
    }

    @Test
    fun `Backlog code block with lang converts to fenced code`() {
        val backlog = "{code:kotlin}\nprintln(\"hello\")\n{/code}"
        val expected = "```kotlin\nprintln(\"hello\")\n```"
        assertEquals(expected, convert(backlog).trim())
    }

    @Test
    fun `Backlog code block without lang converts to fenced code`() {
        val backlog = "{code}\nplain code\n{/code}"
        val expected = "```\nplain code\n```"
        assertEquals(expected, convert(backlog).trim())
    }

    // -------------------------------------------------------------------------
    // リンク
    // -------------------------------------------------------------------------

    @Test
    fun `Backlog link converts to md link`() {
        assertEquals("[click here](https://example.com)", convert("[[click here:https://example.com]]").trim())
    }

    // -------------------------------------------------------------------------
    // リスト
    // -------------------------------------------------------------------------

    @Test
    fun `Backlog bullet list is unchanged`() {
        val backlog = "- item 1\n- item 2"
        val expected = "- item 1\n- item 2"
        assertEquals(expected, convert(backlog).trim())
    }

    @Test
    fun `Backlog ordered list converts to md ordered list`() {
        val backlog = "+ first\n+ second"
        val expected = "1. first\n2. second"
        assertEquals(expected, convert(backlog).trim())
    }

    // -------------------------------------------------------------------------
    // 引用・水平線
    // -------------------------------------------------------------------------

    @Test
    fun `blockquote is unchanged`() {
        assertEquals("> quoted text", convert("> quoted text").trim())
    }

    @Test
    fun `Backlog hr converts to md hr`() {
        assertEquals("---", convert("----").trim())
    }

    // -------------------------------------------------------------------------
    // テーブル
    // -------------------------------------------------------------------------

    @Test
    fun `Backlog table converts to md table`() {
        val backlog = "| Col1 | Col2 |\n| A | B |"
        val expected = "| Col1 | Col2 |\n|------|------|\n| A | B |"
        assertEquals(expected, convert(backlog).trim())
    }

    // -------------------------------------------------------------------------
    // 複合テスト
    // -------------------------------------------------------------------------

    @Test
    fun `mixed content converts correctly`() {
        val backlog = """
            * Title

            Some ''bold'' and '''italic''' text.

            - item 1
            - item 2
        """.trimIndent()
        val expected = """
            # Title

            Some **bold** and *italic* text.

            - item 1
            - item 2
        """.trimIndent()
        assertEquals(expected, convert(backlog).trim())
    }

    // -------------------------------------------------------------------------
    // ラウンドトリップテスト (Backlog → MD → Backlog)
    // -------------------------------------------------------------------------

    @Test
    fun `round trip Backlog to MD to Backlog for heading`() {
        val original = "* Hello World"
        val md = BacklogToMarkdownConverter.convert(original)
        val backlog = MarkdownToBacklogConverter.convert(md)
        assertEquals(original.trim(), backlog.trim())
    }

    @Test
    fun `round trip MD to Backlog to MD for heading`() {
        val original = "# Hello World"
        val backlog = MarkdownToBacklogConverter.convert(original)
        val md = BacklogToMarkdownConverter.convert(backlog)
        assertEquals(original.trim(), md.trim())
    }
}
