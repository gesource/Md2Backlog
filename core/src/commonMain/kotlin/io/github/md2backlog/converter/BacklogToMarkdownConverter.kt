package io.github.md2backlog.converter

import io.github.md2backlog.parser.BacklogParser
import io.github.md2backlog.renderer.MarkdownRenderer

/**
 * Backlog 記法 → Markdown 変換エントリポイント。
 *
 * 内部処理: BacklogParser → Node ツリー → MarkdownRenderer
 */
object BacklogToMarkdownConverter {

    private val parser = BacklogParser()
    private val renderer = MarkdownRenderer()

    fun convert(backlog: String): String {
        val document = parser.parse(backlog)
        return renderer.render(document)
    }
}
