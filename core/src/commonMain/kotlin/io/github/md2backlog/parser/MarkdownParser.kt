package io.github.md2backlog.parser

import io.github.md2backlog.ast.Node
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.MarkdownParser as JBParser

class MarkdownParser {

    private val flavour = GFMFlavourDescriptor()

    fun parse(src: String): Node.Document {
        val tree = JBParser(flavour).buildMarkdownTreeFromString(src)
        val converter = AstConverter(src)
        return Node.Document(converter.convertBlockChildren(tree))
    }
}

private class AstConverter(private val src: String) {

    fun convertBlockChildren(node: ASTNode): List<Node> =
        node.children.mapNotNull { convertBlock(it) }

    private fun convertInlineChildren(node: ASTNode): List<Node> =
        node.children.mapNotNull { convertInline(it) }

    // ------------------------------------------------------------------
    // Block conversion
    // ------------------------------------------------------------------

    private fun convertBlock(node: ASTNode): Node? = when (node.type) {
        MarkdownElementTypes.ATX_1 -> convertAtxHeading(node, 1)
        MarkdownElementTypes.ATX_2 -> convertAtxHeading(node, 2)
        MarkdownElementTypes.ATX_3 -> convertAtxHeading(node, 3)
        MarkdownElementTypes.ATX_4 -> convertAtxHeading(node, 4)
        MarkdownElementTypes.ATX_5 -> convertAtxHeading(node, 5)
        MarkdownElementTypes.ATX_6 -> convertAtxHeading(node, 6)
        MarkdownElementTypes.SETEXT_1 -> convertSetextHeading(node, 1)
        MarkdownElementTypes.SETEXT_2 -> convertSetextHeading(node, 2)
        MarkdownElementTypes.PARAGRAPH -> Node.Paragraph(convertInlineChildren(node))
        MarkdownElementTypes.BLOCK_QUOTE -> convertBlockQuote(node)
        MarkdownElementTypes.CODE_FENCE -> convertCodeFence(node)
        MarkdownElementTypes.CODE_BLOCK -> convertIndentedCodeBlock(node)
        MarkdownElementTypes.UNORDERED_LIST -> convertBulletList(node)
        MarkdownElementTypes.ORDERED_LIST -> convertOrderedList(node)
        MarkdownTokenTypes.HORIZONTAL_RULE -> Node.HorizontalRule
        GFMElementTypes.TABLE -> convertTable(node)
        MarkdownTokenTypes.EOL,
        MarkdownTokenTypes.WHITE_SPACE -> null
        else -> null
    }

    private fun convertAtxHeading(node: ASTNode, level: Int): Node.Heading {
        val contentNode = node.children.find { it.type == MarkdownTokenTypes.ATX_CONTENT }
        val children = if (contentNode != null) {
            // ATX_CONTENT may include a leading whitespace token – skip it
            contentNode.children
                .dropWhile { it.type == MarkdownTokenTypes.WHITE_SPACE }
                .mapNotNull { convertInline(it) }
        } else {
            // fallback: skip ATX_HEADER tokens, then skip whitespace
            node.children
                .filter { it.type != MarkdownTokenTypes.ATX_HEADER && it.type != MarkdownTokenTypes.EOL }
                .dropWhile { it.type == MarkdownTokenTypes.WHITE_SPACE }
                .mapNotNull { convertInline(it) }
        }
        return Node.Heading(level, children)
    }

    private fun convertSetextHeading(node: ASTNode, level: Int): Node.Heading {
        val contentNode = node.children.find { it.type == MarkdownTokenTypes.SETEXT_CONTENT }
        val children = if (contentNode != null) {
            convertInlineChildren(contentNode)
        } else {
            node.children
                .filter {
                    it.type != MarkdownTokenTypes.SETEXT_1 &&
                    it.type != MarkdownTokenTypes.SETEXT_2 &&
                    it.type != MarkdownTokenTypes.EOL
                }
                .mapNotNull { convertInline(it) }
        }
        return Node.Heading(level, children)
    }

    private fun convertBlockQuote(node: ASTNode): Node.BlockQuote {
        val children = node.children
            .filter { it.type != MarkdownTokenTypes.BLOCK_QUOTE && it.type != MarkdownTokenTypes.EOL }
            .mapNotNull { convertBlock(it) }
        return Node.BlockQuote(children)
    }

    private fun convertCodeFence(node: ASTNode): Node.CodeBlock {
        val langNode = node.children.find { it.type == MarkdownTokenTypes.FENCE_LANG }
        val lang = langNode?.getTextInNode(src)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val code = node.children
            .filter { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
            .joinToString("\n") { it.getTextInNode(src).toString() }
            .trimEnd('\n')
        return Node.CodeBlock(lang, code)
    }

    private fun convertIndentedCodeBlock(node: ASTNode): Node.CodeBlock {
        val code = node.getTextInNode(src).toString()
            .lines()
            .map { it.removePrefix("    ") }
            .joinToString("\n")
            .trimEnd('\n')
        return Node.CodeBlock(null, code)
    }

    private fun convertBulletList(node: ASTNode): Node.BulletList {
        val items = node.children
            .filter { it.type == MarkdownElementTypes.LIST_ITEM }
            .map { convertListItem(it) }
        return Node.BulletList(items)
    }

    private fun convertOrderedList(node: ASTNode): Node.OrderedList {
        val items = node.children
            .filter { it.type == MarkdownElementTypes.LIST_ITEM }
            .map { convertListItem(it) }
        return Node.OrderedList(start = 1, items = items)
    }

    private fun convertListItem(node: ASTNode): Node.ListItem {
        val children = node.children
            .filter {
                it.type != MarkdownTokenTypes.LIST_BULLET &&
                it.type != MarkdownTokenTypes.LIST_NUMBER &&
                it.type != MarkdownTokenTypes.WHITE_SPACE &&
                it.type != MarkdownTokenTypes.EOL
            }
            .mapNotNull { convertBlock(it) }
        return Node.ListItem(children)
    }

    private fun convertTable(node: ASTNode): Node.Table {
        val headerRow = node.children.find { it.type == GFMElementTypes.HEADER }
        val bodyRows = node.children.filter { it.type == GFMElementTypes.ROW }
        val header = if (headerRow != null) convertTableRow(headerRow) else Node.TableRow(emptyList())
        val rows = bodyRows.map { convertTableRow(it) }
        return Node.Table(header, rows)
    }

    private fun convertTableRow(node: ASTNode): Node.TableRow {
        val cells = node.children
            .filter { it.type == GFMTokenTypes.CELL }
            .map { cellNode ->
                // Cell content includes surrounding whitespace – trim via text nodes
                val children = convertInlineChildren(cellNode).trimTextEdges()
                Node.TableCell(children)
            }
        return Node.TableRow(cells)
    }

    // ------------------------------------------------------------------
    // Inline conversion
    // ------------------------------------------------------------------

    private fun convertInline(node: ASTNode): Node? = when (node.type) {
        MarkdownElementTypes.STRONG -> convertStrong(node)
        MarkdownElementTypes.EMPH -> convertEmph(node)
        GFMElementTypes.STRIKETHROUGH -> convertStrikethrough(node)
        MarkdownElementTypes.CODE_SPAN -> convertCodeSpan(node)
        MarkdownElementTypes.INLINE_LINK -> convertInlineLink(node)
        MarkdownElementTypes.IMAGE -> convertImage(node)
        MarkdownTokenTypes.BLOCK_QUOTE -> null
        MarkdownTokenTypes.EOL -> null
        MarkdownTokenTypes.HARD_LINE_BREAK -> Node.LineBreak
        else -> {
            val text = node.getTextInNode(src).toString()
            if (text.isEmpty()) null else Node.Text(text)
        }
    }

    /**
     * STRONG: `**bold**` → JB splits `**` into two EMPH(*) tokens.
     * Skip all leading/trailing EMPH tokens to extract inner content.
     */
    private fun convertStrong(node: ASTNode): Node.Bold {
        val content = node.children
            .dropWhile { it.type == MarkdownTokenTypes.EMPH }
            .dropLastWhile { it.type == MarkdownTokenTypes.EMPH }
            .mapNotNull { convertInline(it) }
        return Node.Bold(content)
    }

    /** EMPH: `*italic*` → same pattern as STRONG, single `*` EMPH tokens */
    private fun convertEmph(node: ASTNode): Node.Italic {
        val content = node.children
            .dropWhile { it.type == MarkdownTokenTypes.EMPH }
            .dropLastWhile { it.type == MarkdownTokenTypes.EMPH }
            .mapNotNull { convertInline(it) }
        return Node.Italic(content)
    }

    /** STRIKETHROUGH: `~~text~~` → JB splits `~~` into two GFMTokenTypes.TILDE tokens */
    private fun convertStrikethrough(node: ASTNode): Node.Strikethrough {
        val content = node.children
            .dropWhile { it.type == GFMTokenTypes.TILDE }
            .dropLastWhile { it.type == GFMTokenTypes.TILDE }
            .mapNotNull { convertInline(it) }
        return Node.Strikethrough(content)
    }

    /** CODE_SPAN: strip surrounding backticks from full text */
    private fun convertCodeSpan(node: ASTNode): Node.InlineCode {
        val full = node.getTextInNode(src).toString()
        val backticks = full.takeWhile { it == '`' }
        val code = full.removePrefix(backticks).removeSuffix(backticks).trim()
        return Node.InlineCode(code)
    }

    private fun convertInlineLink(node: ASTNode): Node.Link {
        val linkTextNode = node.children.find { it.type == MarkdownElementTypes.LINK_TEXT }
        val linkDestNode = node.children.find { it.type == MarkdownElementTypes.LINK_DESTINATION }
        val linkTitleNode = node.children.find { it.type == MarkdownElementTypes.LINK_TITLE }

        val href = linkDestNode?.getTextInNode(src)?.toString() ?: ""
        val title = linkTitleNode?.getTextInNode(src)?.toString()
            ?.trim('"', '\'', '(', ')')
            ?.takeIf { it.isNotEmpty() }
        val children = if (linkTextNode != null) {
            linkTextNode.children
                .filter { it.type != MarkdownTokenTypes.LBRACKET && it.type != MarkdownTokenTypes.RBRACKET }
                .mapNotNull { convertInline(it) }
        } else emptyList()

        return Node.Link(href, title, children)
    }

    /**
     * IMAGE: `![alt](src)` — the IMAGE element may nest LINK_TEXT / LINK_DESTINATION
     * inside an inner INLINE_LINK node, so we search recursively.
     */
    private fun convertImage(node: ASTNode): Node.Image {
        val linkTextNode = findDescendant(node, MarkdownElementTypes.LINK_TEXT)
        val linkDestNode = findDescendant(node, MarkdownElementTypes.LINK_DESTINATION)

        val imgSrc = linkDestNode?.getTextInNode(src)?.toString() ?: ""
        val alt = linkTextNode?.getTextInNode(src)?.toString()?.trim('[', ']') ?: ""

        return Node.Image(imgSrc, alt)
    }

    // ------------------------------------------------------------------
    // Utilities
    // ------------------------------------------------------------------

    /** Depth-first search for the first descendant with the given type */
    private fun findDescendant(node: ASTNode, type: IElementType): ASTNode? {
        if (node.type == type) return node
        return node.children.firstNotNullOfOrNull { findDescendant(it, type) }
    }

    /**
     * Trim leading/trailing whitespace from the edge Text nodes of an inline list.
     * Used to clean up table cell content.
     */
    private fun List<Node>.trimTextEdges(): List<Node> {
        if (isEmpty()) return this
        val mutable = toMutableList()
        val first = mutable.first()
        if (first is Node.Text) mutable[0] = Node.Text(first.value.trimStart())
        val last = mutable.last()
        if (last is Node.Text) mutable[mutable.lastIndex] = Node.Text(last.value.trimEnd())
        return mutable.filter { it !is Node.Text || it.value.isNotEmpty() }
    }
}
