package io.github.md2backlog.renderer

import io.github.md2backlog.ast.Node

/**
 * Node ツリーを Markdown テキストに変換するレンダラー。
 */
class MarkdownRenderer {

    fun render(document: Node.Document): String =
        renderBlocks(document.children)

    // ------------------------------------------------------------------
    // Block rendering
    // ------------------------------------------------------------------

    private fun renderBlocks(nodes: List<Node>): String =
        nodes.joinToString("\n\n") { renderBlock(it) }

    private fun renderBlock(node: Node): String = when (node) {
        is Node.Document -> renderBlocks(node.children)
        is Node.Heading -> "${"#".repeat(node.level)} ${renderInlineChildren(node.children)}"
        is Node.Paragraph -> renderInlineChildren(node.children)
        is Node.BlockQuote -> node.children.joinToString("\n") { "> ${renderBlock(it)}" }
        is Node.CodeBlock -> renderCodeBlock(node)
        is Node.BulletList -> node.items.joinToString("\n") { "- ${renderListContent(it)}" }
        is Node.OrderedList -> renderOrderedList(node)
        is Node.ListItem -> renderListContent(node)
        is Node.Table -> renderTable(node)
        is Node.HorizontalRule -> "---"
        else -> renderInline(node)
    }

    private fun renderCodeBlock(node: Node.CodeBlock): String {
        val header = if (node.lang != null) "```${node.lang}" else "```"
        return "$header\n${node.code}\n```"
    }

    private fun renderOrderedList(node: Node.OrderedList): String =
        node.items.mapIndexed { idx, item ->
            "${node.start + idx}. ${renderListContent(item)}"
        }.joinToString("\n")

    private fun renderListContent(item: Node.ListItem): String =
        item.children.joinToString("\n") { child ->
            when (child) {
                is Node.Paragraph -> renderInlineChildren(child.children)
                else -> renderBlock(child)
            }
        }

    private fun renderTable(table: Node.Table): String {
        val rows = buildList {
            add(renderTableRow(table.header))
            add("|${table.header.cells.joinToString("|") { "------" }}|")
            table.rows.forEach { add(renderTableRow(it)) }
        }
        return rows.joinToString("\n")
    }

    private fun renderTableRow(row: Node.TableRow): String =
        "| ${row.cells.joinToString(" | ") { renderInlineChildren(it.children) }} |"

    // ------------------------------------------------------------------
    // Inline rendering
    // ------------------------------------------------------------------

    private fun renderInlineChildren(nodes: List<Node>): String =
        nodes.joinToString("") { renderInline(it) }

    private fun renderInline(node: Node): String = when (node) {
        is Node.Text -> node.value
        is Node.Bold -> "**${renderInlineChildren(node.children)}**"
        is Node.Italic -> "*${renderInlineChildren(node.children)}*"
        is Node.Strikethrough -> "~~${renderInlineChildren(node.children)}~~"
        is Node.InlineCode -> "`${node.code}`"
        is Node.Link -> "[${renderInlineChildren(node.children)}](${node.href})"
        is Node.Image -> "![${node.alt}](${node.src})"
        is Node.LineBreak -> "\n"
        else -> ""
    }
}
