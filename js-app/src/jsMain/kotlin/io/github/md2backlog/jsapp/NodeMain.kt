package io.github.md2backlog.jsapp

import io.github.md2backlog.converter.BacklogToMarkdownConverter
import io.github.md2backlog.converter.MarkdownToBacklogConverter

/**
 * Node.js 向け CLI エントリポイント。
 *
 * 使用例:
 *   node md2backlog.js --to-backlog input.md
 *   echo "# Hello" | node md2backlog.js --to-backlog
 */
fun runNodeCli() {
    val process = js("require('process')")
    val args = (process.argv as Array<String>).drop(2)  // 0=node, 1=script

    val toBacklog = args.contains("--to-backlog")
    val toMarkdown = args.contains("--to-markdown")

    if (!toBacklog && !toMarkdown) {
        printUsage()
        return
    }

    val fileArg = args.firstOrNull { !it.startsWith("-") }

    if (fileArg != null) {
        // ファイル引数あり: 同期読み込み
        val fs = js("require('fs')")
        val text = fs.readFileSync(fileArg, "utf8") as String
        val result = convert(text, toBacklog)
        println(result)
    } else {
        // stdin から読み込む（非同期）
        readStdin { text ->
            val result = convert(text, toBacklog)
            println(result)
        }
    }
}

private fun convert(text: String, toBacklog: Boolean): String =
    if (toBacklog) MarkdownToBacklogConverter.convert(text)
    else BacklogToMarkdownConverter.convert(text)

private fun printUsage() {
    println(
        """
        Usage: node md2backlog.js [--to-backlog|--to-markdown] [file]

        Convert between Markdown and Backlog wiki notation.

        Options:
          --to-backlog    Convert Markdown → Backlog format
          --to-markdown   Convert Backlog format → Markdown

        Examples:
          node md2backlog.js --to-backlog input.md
          echo "# Hello" | node md2backlog.js --to-backlog
        """.trimIndent()
    )
}

private fun readStdin(callback: (String) -> Unit) {
    val process = js("require('process')")
    val chunks = mutableListOf<String>()
    process.stdin.setEncoding("utf8")
    process.stdin.on("data") { chunk: String -> chunks.add(chunk) }
    process.stdin.on("end") { callback(chunks.joinToString("")) }
}
