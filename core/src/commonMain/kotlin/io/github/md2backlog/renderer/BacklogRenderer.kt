package io.github.md2backlog.renderer

import io.github.md2backlog.ast.Node

/**
 * Node ツリーを Backlog 記法テキストに変換するレンダラー。
 */
class BacklogRenderer {

    fun render(document: Node.Document): String =
        renderBlocks(document.children)

    // ------------------------------------------------------------------
    // Block rendering
    // ------------------------------------------------------------------

    private fun renderBlocks(nodes: List<Node>): String {
        val filtered = nodes.filter { it !is Node.HorizontalRule }
        return buildString {
            filtered.forEachIndexed { index, node ->
                if (index > 0) {
                    append(if (filtered[index - 1] is Node.Heading) "\n" else "\n\n")
                }
                append(renderBlock(node))
            }
        }
    }

    private fun renderBlock(node: Node): String = when (node) {
        is Node.Document -> renderBlocks(node.children)
        is Node.Heading -> "${"*".repeat(node.level)} ${renderInlineChildren(node.children)}"
        is Node.Paragraph -> renderInlineChildren(node.children)
        is Node.BlockQuote -> "{quote}\n${renderBlocks(node.children)}\n{/quote}"
        is Node.CodeBlock -> renderCodeBlock(node)
        is Node.BulletList -> renderBulletList(node)
        is Node.OrderedList -> renderOrderedList(node)
        is Node.ListItem -> renderListItem(node, "- ", 1)
        is Node.Table -> renderTable(node)
        is Node.HorizontalRule -> "----"
        else -> renderInline(node)
    }

    private fun renderCodeBlock(node: Node.CodeBlock): String {
        val backlogLang = when (node.lang?.lowercase()) {
            "java" -> "java"
            "cs", "csharp", "c#" -> "cs"
            else -> null
        }
        val header = if (backlogLang != null) "{code:$backlogLang}" else "{code}"
        return "$header\n${node.code}\n{/code}"
    }

    private fun renderBulletList(node: Node.BulletList, level: Int = 1): String {
        val prefix = "-".repeat(level) + " "
        return node.items.joinToString("\n") { renderListItem(it, prefix, level) }
    }

    private fun renderOrderedList(node: Node.OrderedList, level: Int = 1): String {
        val prefix = "+".repeat(level) + " "
        return node.items.joinToString("\n") { renderListItem(it, prefix, level) }
    }

    private fun renderListItem(item: Node.ListItem, prefix: String, level: Int): String =
        item.children.joinToString("\n") { child ->
            when (child) {
                is Node.Paragraph -> prefix + renderInlineChildren(child.children)
                is Node.BulletList -> renderBulletList(child, level + 1)
                is Node.OrderedList -> renderOrderedList(child, level + 1)
                is Node.CodeBlock -> renderCodeBlock(child)
                else -> prefix + renderBlock(child)
            }
        }

    private fun renderTable(table: Node.Table): String {
        val rows = buildList {
            add(renderTableRow(table.header))
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
        is Node.Bold -> "''${renderInlineChildren(node.children)}''"
        is Node.Italic -> "'''${renderInlineChildren(node.children)}'''"
        is Node.Strikethrough -> "%%${renderInlineChildren(node.children)}%%"
        is Node.InlineCode -> "{code}${node.code}{/code}"
        is Node.Link -> {
            val text = renderInlineChildren(node.children)
            // リンクテキストと URL が同じ場合は [[url]] 形式で出力（二重 URL を避ける）
            if (text == node.href) "[[${node.href}]]" else "[[$text:${node.href}]]"
        }
        is Node.Image -> "[[${node.alt}:${node.src}]]"
        is Node.LineBreak -> "\n"
        else -> ""
    }
}
