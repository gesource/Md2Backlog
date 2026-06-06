package io.github.md2backlog.parser

import io.github.md2backlog.ast.Node

/**
 * Backlog 記法テキストを Node ツリーに変換するパーサー。
 *
 * ブロック要素を行単位で検出し、インライン要素を文字単位でスキャンする。
 */
class BacklogParser {

    fun parse(text: String): Node.Document {
        val lines = text.lines()
        return Node.Document(parseBlocks(lines))
    }

    // ------------------------------------------------------------------
    // Block parsing
    // ------------------------------------------------------------------

    private fun parseBlocks(lines: List<String>): List<Node> {
        val result = mutableListOf<Node>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            when {
                // 見出し: "* text" 〜 "****** text"
                HEADING_REGEX.matches(line) -> {
                    val level = line.indexOfFirst { it != '*' }
                    val content = line.drop(level + 1) // skip "*** "
                    result.add(Node.Heading(level, parseInline(content)))
                    i++
                }

                // 番号付きリスト: "+ item"
                line.startsWith("+ ") -> {
                    val items = mutableListOf<Node.ListItem>()
                    while (i < lines.size && lines[i].startsWith("+ ")) {
                        items.add(Node.ListItem(listOf(Node.Paragraph(parseInline(lines[i].drop(2))))))
                        i++
                    }
                    result.add(Node.OrderedList(start = 1, items = items))
                }

                // 番号なしリスト: "- item"
                line.startsWith("- ") -> {
                    val items = mutableListOf<Node.ListItem>()
                    while (i < lines.size && lines[i].startsWith("- ")) {
                        items.add(Node.ListItem(listOf(Node.Paragraph(parseInline(lines[i].drop(2))))))
                        i++
                    }
                    result.add(Node.BulletList(items))
                }

                // 引用ブロック: {quote}...{/quote}
                line == "{quote}" -> {
                    val quoteLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && lines[i] != "{/quote}") {
                        quoteLines.add(lines[i])
                        i++
                    }
                    if (i < lines.size) i++ // skip {/quote}
                    result.add(Node.BlockQuote(parseBlocks(quoteLines)))
                }

                // 引用: "> text"（連続行をまとめて1つの BlockQuote にする）
                line.startsWith("> ") -> {
                    val quoteLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].startsWith("> ")) {
                        quoteLines.add(lines[i].drop(2))
                        i++
                    }
                    result.add(Node.BlockQuote(parseBlocks(quoteLines)))
                }

                // 水平線: "----" (4文字以上のハイフン)
                HR_REGEX.matches(line) -> {
                    result.add(Node.HorizontalRule)
                    i++
                }

                // コードブロック: {code} または {code:lang}（行全体がタグのみの場合）
                CODE_BLOCK_START_REGEX.matches(line) -> {
                    val langMatch = CODE_LANG_REGEX.find(line)
                    val lang = langMatch?.groupValues?.getOrNull(1)?.takeIf { it.isNotEmpty() }
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && lines[i] != "{/code}") {
                        codeLines.add(lines[i])
                        i++
                    }
                    if (i < lines.size) i++ // skip {/code}
                    result.add(Node.CodeBlock(lang, codeLines.joinToString("\n")))
                }

                // テーブル: "| ... |"
                line.startsWith("|") -> {
                    val tableLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].startsWith("|")) {
                        tableLines.add(lines[i])
                        i++
                    }
                    result.add(parseTable(tableLines))
                }

                // 空行はスキップ
                line.isBlank() -> i++

                // 段落: それ以外の行
                else -> {
                    val paraLines = mutableListOf<String>()
                    while (i < lines.size && !lines[i].isBlank() && !isBlockStart(lines[i])) {
                        paraLines.add(lines[i])
                        i++
                    }
                    result.add(Node.Paragraph(parseInline(paraLines.joinToString("\n"))))
                }
            }
        }

        return result
    }

    private fun isBlockStart(line: String): Boolean =
        HEADING_REGEX.matches(line) ||
        line.startsWith("- ") ||
        line.startsWith("+ ") ||
        line.startsWith("> ") ||
        HR_REGEX.matches(line) ||
        CODE_BLOCK_START_REGEX.matches(line) ||
        line.startsWith("|")

    private fun parseTable(lines: List<String>): Node.Table {
        val rows = lines.map { parseTableRow(it) }
        val header = rows.firstOrNull() ?: Node.TableRow(emptyList())
        val bodyRows = rows.drop(1)
        return Node.Table(header, bodyRows)
    }

    private fun parseTableRow(line: String): Node.TableRow {
        val cells = line.trim().removePrefix("|").removeSuffix("|")
            .split("|")
            .map { cellContent ->
                Node.TableCell(parseInline(cellContent.trim()))
            }
        return Node.TableRow(cells)
    }

    // ------------------------------------------------------------------
    // Inline parsing
    // ------------------------------------------------------------------

    private fun parseInline(text: String): List<Node> {
        val nodes = mutableListOf<Node>()
        val sb = StringBuilder()
        var i = 0

        fun flushText() {
            if (sb.isNotEmpty()) {
                nodes.add(Node.Text(sb.toString()))
                sb.clear()
            }
        }

        while (i < text.length) {
            when {
                // 斜体: '''text''' (''' を '' より先にチェック)
                text.startsWith("'''", i) -> {
                    flushText()
                    val end = text.indexOf("'''", i + 3)
                    if (end != -1) {
                        nodes.add(Node.Italic(parseInline(text.substring(i + 3, end))))
                        i = end + 3
                    } else {
                        sb.append("'''")
                        i += 3
                    }
                }

                // 太字: ''text''
                // closing '' が ''' の一部（斜体デリミタ）にならないよう、
                // 前後に ' がある位置はスキップする
                text.startsWith("''", i) -> {
                    flushText()
                    var end = text.indexOf("''", i + 2)
                    while (end != -1) {
                        val followedByQuote = end + 2 < text.length && text[end + 2] == '\''
                        val precededByQuote = end > i + 2 && text[end - 1] == '\''
                        if (!followedByQuote && !precededByQuote) break
                        end = text.indexOf("''", end + 1)
                    }
                    if (end != -1) {
                        nodes.add(Node.Bold(parseInline(text.substring(i + 2, end))))
                        i = end + 2
                    } else {
                        sb.append("''")
                        i += 2
                    }
                }

                // 打消し線: %%text%%
                text.startsWith("%%", i) -> {
                    flushText()
                    val end = text.indexOf("%%", i + 2)
                    if (end != -1) {
                        nodes.add(Node.Strikethrough(parseInline(text.substring(i + 2, end))))
                        i = end + 2
                    } else {
                        sb.append("%%")
                        i += 2
                    }
                }

                // インラインコード: {code}code{/code}
                text.startsWith("{code}", i) -> {
                    flushText()
                    val end = text.indexOf("{/code}", i + 6)
                    if (end != -1) {
                        nodes.add(Node.InlineCode(text.substring(i + 6, end)))
                        i = end + 7
                    } else {
                        sb.append("{code}")
                        i += 6
                    }
                }

                // リンク: [[text:url]]
                text.startsWith("[[", i) -> {
                    flushText()
                    val end = text.indexOf("]]", i + 2)
                    if (end != -1) {
                        nodes.add(parseLinkContent(text.substring(i + 2, end)))
                        i = end + 2
                    } else {
                        sb.append("[[")
                        i += 2
                    }
                }

                else -> {
                    sb.append(text[i])
                    i++
                }
            }
        }

        flushText()
        return nodes
    }

    /**
     * "[[" と "]]" の間の内容を解析してリンクノードを返す。
     *
     * フォーマット: "text:url" または "url"
     * URL プロトコル ("://") がある場合はその手前の ":" を区切りとして使う。
     */
    private fun parseLinkContent(content: String): Node.Link {
        val protocolIdx = content.indexOf("://")
        val separatorIdx = if (protocolIdx > 0) {
            // "click here:https://..." → ":" は "https" の前にある
            var schemeStart = protocolIdx
            while (schemeStart > 0 && content[schemeStart - 1].isLetter()) schemeStart--
            if (schemeStart > 0) schemeStart - 1 else -1
        } else {
            content.indexOf(':')
        }

        return if (separatorIdx > 0) {
            val linkText = content.substring(0, separatorIdx)
            val url = content.substring(separatorIdx + 1)
            Node.Link(url, null, listOf(Node.Text(linkText)))
        } else {
            Node.Link(content, null, listOf(Node.Text(content)))
        }
    }

    companion object {
        private val HEADING_REGEX = Regex("^\\*{1,6} .+")
        private val HR_REGEX = Regex("^-{4,}$")
        private val CODE_LANG_REGEX = Regex("\\{code(?::([^}]*))?\\}")
        private val CODE_BLOCK_START_REGEX = Regex("^\\{code(?::[^}]*)?\\}$")
    }
}
