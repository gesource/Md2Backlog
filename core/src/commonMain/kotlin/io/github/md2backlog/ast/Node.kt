package io.github.md2backlog.ast

/**
 * Markdown / Backlog 記法 共通の統一 AST ノード定義。
 *
 * パーサーは入力テキストを Node ツリーに変換し、
 * レンダラーは Node ツリーを各記法のテキストに変換する。
 */
sealed class Node {

    // -------------------------------------------------------------------------
    // Block nodes
    // -------------------------------------------------------------------------

    /** ドキュメントルート */
    data class Document(val children: List<Node>) : Node()

    /** 見出し（level: 1〜6） */
    data class Heading(val level: Int, val children: List<Node>) : Node()

    /** 段落 */
    data class Paragraph(val children: List<Node>) : Node()

    /** 引用ブロック */
    data class BlockQuote(val children: List<Node>) : Node()

    /** フェンスコードブロック */
    data class CodeBlock(val lang: String?, val code: String) : Node()

    /** 番号なしリスト */
    data class BulletList(val items: List<ListItem>) : Node()

    /** 番号付きリスト */
    data class OrderedList(val start: Int, val items: List<ListItem>) : Node()

    /** リストアイテム */
    data class ListItem(val children: List<Node>) : Node()

    /** テーブル */
    data class Table(val header: TableRow, val rows: List<TableRow>) : Node()

    /** テーブル行 */
    data class TableRow(val cells: List<TableCell>) : Node()

    /** テーブルセル */
    data class TableCell(val children: List<Node>) : Node()

    /** 水平線 */
    data object HorizontalRule : Node()

    // -------------------------------------------------------------------------
    // Inline nodes
    // -------------------------------------------------------------------------

    /** プレーンテキスト */
    data class Text(val value: String) : Node()

    /** 太字 */
    data class Bold(val children: List<Node>) : Node()

    /** 斜体 */
    data class Italic(val children: List<Node>) : Node()

    /** 打消し線 */
    data class Strikethrough(val children: List<Node>) : Node()

    /** インラインコード */
    data class InlineCode(val code: String) : Node()

    /** リンク */
    data class Link(val href: String, val title: String?, val children: List<Node>) : Node()

    /** 画像 */
    data class Image(val src: String, val alt: String) : Node()

    /** 強制改行 */
    data object LineBreak : Node()
}
