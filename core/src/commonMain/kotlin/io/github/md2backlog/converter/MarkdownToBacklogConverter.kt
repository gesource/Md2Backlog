package io.github.md2backlog.converter

import io.github.md2backlog.parser.MarkdownParser
import io.github.md2backlog.renderer.BacklogRenderer

/**
 * Markdown → Backlog 記法 変換エントリポイント。
 *
 * 内部処理: MarkdownParser → Node ツリー → BacklogRenderer
 */
object MarkdownToBacklogConverter {

    private val parser = MarkdownParser()
    private val renderer = BacklogRenderer()

    fun convert(markdown: String): String {
        val document = parser.parse(markdown)
        return renderer.render(document)
    }
}
