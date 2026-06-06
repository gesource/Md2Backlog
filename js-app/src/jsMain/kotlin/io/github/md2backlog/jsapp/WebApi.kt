package io.github.md2backlog.jsapp

import io.github.md2backlog.converter.BacklogToMarkdownConverter
import io.github.md2backlog.converter.MarkdownToBacklogConverter

/**
 * ブラウザ向けグローバル API。
 *
 * ビルド後、以下のように使用できる:
 *   Md2Backlog.toBacklog("# Hello")   // → "* Hello"
 *   Md2Backlog.toMarkdown("* Hello")  // → "# Hello"
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Suppress("unused")
object Md2Backlog {

    /** Markdown テキストを Backlog 記法に変換する */
    fun toBacklog(markdown: String): String =
        MarkdownToBacklogConverter.convert(markdown)

    /** Backlog 記法テキストを Markdown に変換する */
    fun toMarkdown(backlog: String): String =
        BacklogToMarkdownConverter.convert(backlog)
}
