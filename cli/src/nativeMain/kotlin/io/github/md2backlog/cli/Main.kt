@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.md2backlog.cli

import io.github.md2backlog.converter.BacklogToMarkdownConverter
import io.github.md2backlog.converter.MarkdownToBacklogConverter
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlin.system.exitProcess
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.stderr

fun main(args: Array<String>) {
    val toBacklog = args.contains("--to-backlog")
    val toMarkdown = args.contains("--to-markdown")

    if (toBacklog && toMarkdown) {
        fputs("Error: --to-backlog and --to-markdown are mutually exclusive\n", stderr)
        exitProcess(1)
    }

    if (!toBacklog && !toMarkdown) {
        printUsage()
        exitProcess(1)
    }

    val fileArg = args.firstOrNull { !it.startsWith("-") }
    val text = if (fileArg != null) {
        readFile(fileArg) ?: exitProcess(1)
    } else {
        readStdin()
    }

    val result = if (toBacklog) {
        MarkdownToBacklogConverter.convert(text)
    } else {
        BacklogToMarkdownConverter.convert(text)
    }

    print(result)
}

private fun printUsage() {
    println(
        """
        Usage: md2backlog [--to-backlog|--to-markdown] [file]

        Convert between Markdown and Backlog wiki notation.

        Options:
          --to-backlog    Convert Markdown → Backlog format
          --to-markdown   Convert Backlog format → Markdown

        Examples:
          md2backlog --to-backlog input.md
          md2backlog --to-markdown input.backlog
          echo "# Hello" | md2backlog --to-backlog
        """.trimIndent()
    )
}

private fun readStdin(): String = buildString {
    var line = readlnOrNull()
    while (line != null) {
        appendLine(line)
        line = readlnOrNull()
    }
}

private fun readFile(path: String): String? = memScoped {
    val file = fopen(path, "r")
    if (file == null) {
        fputs("Error: Cannot open file: $path\n", stderr)
        return@memScoped null
    }
    try {
        buildString {
            val buffer = allocArray<ByteVar>(8192)
            while (fgets(buffer, 8192, file) != null) {
                append(buffer.toKString())
            }
        }
    } finally {
        fclose(file)
    }
}
