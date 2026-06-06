package io.github.md2backlog.converter

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Markdown → Backlog 記法 の変換テスト。
 *
 * 変換は MarkdownParser → Node ツリー → BacklogRenderer の2段階で行う。
 * テストは入力 Markdown 文字列と期待する Backlog 記法文字列を比較する。
 */
class MarkdownToBacklogTest {

    private fun convert(markdown: String): String =
        MarkdownToBacklogConverter.convert(markdown)

    // -------------------------------------------------------------------------
    // 見出し
    // -------------------------------------------------------------------------

    @Test
    fun `h1 converts to Backlog h1`() {
        assertEquals("* Heading 1", convert("# Heading 1").trim())
    }

    @Test
    fun `h2 converts to Backlog h2`() {
        assertEquals("** Heading 2", convert("## Heading 2").trim())
    }

    @Test
    fun `h3 converts to Backlog h3`() {
        assertEquals("*** Heading 3", convert("### Heading 3").trim())
    }

    @Test
    fun `h4 converts to Backlog h4`() {
        assertEquals("**** Heading 4", convert("#### Heading 4").trim())
    }

    @Test
    fun `h5 converts to Backlog h5`() {
        assertEquals("***** Heading 5", convert("##### Heading 5").trim())
    }

    @Test
    fun `h6 converts to Backlog h6`() {
        assertEquals("****** Heading 6", convert("###### Heading 6").trim())
    }

    // -------------------------------------------------------------------------
    // 太字・斜体・打消し線
    // -------------------------------------------------------------------------

    @Test
    fun `bold converts to Backlog bold`() {
        assertEquals("''bold text''", convert("**bold text**").trim())
    }

    @Test
    fun `italic converts to Backlog italic`() {
        assertEquals("'''italic text'''", convert("*italic text*").trim())
    }

    @Test
    fun `bold and italic combined converts to Backlog five quotes`() {
        assertEquals("'''''bold italic'''''", convert("***bold italic***").trim())
    }

    @Test
    fun `strikethrough converts to Backlog strikethrough`() {
        assertEquals("%%deleted%%", convert("~~deleted~~").trim())
    }

    // -------------------------------------------------------------------------
    // コード
    // -------------------------------------------------------------------------

    @Test
    fun `inline code converts to Backlog code tag`() {
        assertEquals("{code}code{/code}", convert("`code`").trim())
    }

    @Test
    fun `fenced code block with java lang converts to Backlog code java`() {
        val md = "```java\nSystem.out.println(\"hello\");\n```"
        val expected = "{code:java}\nSystem.out.println(\"hello\");\n{/code}"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `fenced code block with cs lang converts to Backlog code cs`() {
        val md = "```cs\nConsole.WriteLine(\"hello\");\n```"
        val expected = "{code:cs}\nConsole.WriteLine(\"hello\");\n{/code}"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `fenced code block with csharp lang converts to Backlog code cs`() {
        val md = "```csharp\nConsole.WriteLine(\"hello\");\n```"
        val expected = "{code:cs}\nConsole.WriteLine(\"hello\");\n{/code}"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `fenced code block with other lang converts to Backlog code without lang`() {
        val md = "```kotlin\nprintln(\"hello\")\n```"
        val expected = "{code}\nprintln(\"hello\")\n{/code}"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `fenced code block without lang converts to Backlog code`() {
        val md = "```\nplain code\n```"
        val expected = "{code}\nplain code\n{/code}"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `fenced code block with multiple lines preserves newlines`() {
        val md = "```sql\n-- line 1\n-- line 2\nSELECT *\nFROM t\n```"
        val expected = "{code}\n-- line 1\n-- line 2\nSELECT *\nFROM t\n{/code}"
        assertEquals(expected, convert(md).trim())
    }

    // -------------------------------------------------------------------------
    // リンク・画像
    // -------------------------------------------------------------------------

    @Test
    fun `link converts to Backlog link`() {
        assertEquals("[[click here:https://example.com]]", convert("[click here](https://example.com)").trim())
    }

    @Test
    fun `image converts to Backlog image`() {
        // Backlog記法では画像はリンクで代替: [[alt:src]]
        assertEquals("[[a cat:image.png]]", convert("![a cat](image.png)").trim())
    }

    // -------------------------------------------------------------------------
    // リスト
    // -------------------------------------------------------------------------

    @Test
    fun `bullet list converts to Backlog bullet list`() {
        val md = "- item 1\n- item 2"
        val expected = "- item 1\n- item 2"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `ordered list converts to Backlog ordered list`() {
        val md = "1. first\n2. second"
        val expected = "+ first\n+ second"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `nested bullet list converts to Backlog double dash`() {
        val md = "- a\n  - a1\n  - a2\n- b"
        val expected = "- a\n-- a1\n-- a2\n- b"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `three level bullet list converts to Backlog triple dash`() {
        val md = "- a\n  - a1\n    - a1a\n    - a1b\n  - a2\n- b"
        val expected = "- a\n-- a1\n--- a1a\n--- a1b\n-- a2\n- b"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `nested ordered list converts to Backlog double plus`() {
        val md = "1. first\n   1. sub1\n   2. sub2\n2. second"
        val expected = "+ first\n++ sub1\n++ sub2\n+ second"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `mixed nested list bullet with ordered sub converts correctly`() {
        val md = "- a\n  1. a1\n     - a1a\n     - a2a\n  2. a2\n     - a2a\n- b\n  1. b1\n     - b1b\n  2. b2"
        val expected = "- a\n++ a1\n--- a1a\n--- a2a\n++ a2\n--- a2a\n- b\n++ b1\n--- b1b\n++ b2"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `code block inside list item renders without list prefix`() {
        val md = "1. aaa\n\n   ```\n   code\n   ```\n\n2. bbb"
        val expected = "+ aaa\n{code}\ncode\n{/code}\n+ bbb"
        assertEquals(expected, convert(md).trim())
    }

    // -------------------------------------------------------------------------
    // 引用・水平線
    // -------------------------------------------------------------------------

    @Test
    fun `blockquote converts to Backlog quote block`() {
        val expected = "{quote}\nquoted text\n{/quote}"
        assertEquals(expected, convert("> quoted text").trim())
    }

    @Test
    fun `blockquote with multiple block elements converts correctly`() {
        val md = "> **重要なメモ:**\n>\n> - item 1\n> - item 2"
        val expected = "{quote}\n''重要なメモ:''\n\n- item 1\n- item 2\n{/quote}"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `multiline blockquote without blank lines does not include gt markers`() {
        val md = "> line 1\n> line 2\n> line 3"
        val expected = "{quote}\nline 1 line 2 line 3\n{/quote}"
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `horizontal rule is removed in Backlog output`() {
        assertEquals("", convert("---").trim())
    }

    // -------------------------------------------------------------------------
    // テーブル
    // -------------------------------------------------------------------------

    @Test
    fun `table converts to Backlog table`() {
        val md = """
            | Col1 | Col2 |
            |------|------|
            | A    | B    |
        """.trimIndent()
        val expected = "| Col1 | Col2 |\n| A | B |"
        assertEquals(expected, convert(md).trim())
    }

    // -------------------------------------------------------------------------
    // 見出し後の空行削除
    // -------------------------------------------------------------------------

    @Test
    fun `blank line after heading is removed in Backlog output`() {
        val md = "# Title\n\nParagraph text."
        assertEquals("* Title\nParagraph text.", convert(md).trim())
    }

    @Test
    fun `blank line between headings is removed in Backlog output`() {
        val md = "# H1\n\n## H2\n\nParagraph text."
        assertEquals("* H1\n** H2\nParagraph text.", convert(md).trim())
    }

    @Test
    fun `blank line after heading before list is removed in Backlog output`() {
        val md = "## Section\n\n- item 1\n- item 2"
        assertEquals("** Section\n- item 1\n- item 2", convert(md).trim())
    }

    @Test
    fun `blank line before heading is preserved in Backlog output`() {
        val md = "Paragraph text.\n\n# Title"
        assertEquals("Paragraph text.\n\n* Title", convert(md).trim())
    }

    // -------------------------------------------------------------------------
    // 複合テスト
    // -------------------------------------------------------------------------

    @Test
    fun `mixed content converts correctly`() {
        val md = """
            # Title

            Some **bold** and *italic* text.

            - item 1
            - item 2
        """.trimIndent()
        val expected = """
            * Title
            Some ''bold'' and '''italic''' text.

            - item 1
            - item 2
        """.trimIndent()
        assertEquals(expected, convert(md).trim())
    }

    @Test
    fun `complex document with nested lists inline formatting table and code`() {
        val md = """
            # Project Overview

            ## Features

            Some **bold** and *italic* and ~~strikethrough~~ text with `inline code`.

            - bullet level 1
              1. ordered level 2
                 - bullet level 3
                 - bullet level 3
              2. ordered level 2
            - bullet level 1

            ## Setup

            1. Install dependencies

               ```bash
               npm install
               ```

            2. Run the app

            ## API

            | Method | Path | Description |
            |--------|------|-------------|
            | **GET** | `/users` | List *all* users |
            | POST | `/users` | Create user |

            ---

            See [docs](https://example.com) for details.
        """.trimIndent()
        val expected = """
            * Project Overview
            ** Features
            Some ''bold'' and '''italic''' and %%strikethrough%% text with {code}inline code{/code}.

            - bullet level 1
            ++ ordered level 2
            --- bullet level 3
            --- bullet level 3
            ++ ordered level 2
            - bullet level 1

            ** Setup
            + Install dependencies
            {code}
            npm install
            {/code}
            + Run the app

            ** API
            | Method | Path | Description |
            | ''GET'' | {code}/users{/code} | List '''all''' users |
            | POST | {code}/users{/code} | Create user |

            See [[docs:https://example.com]] for details.
        """.trimIndent()
        assertEquals(expected, convert(md).trim())
    }
}
