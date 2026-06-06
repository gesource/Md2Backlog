package io.github.md2backlog.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Md2Backlog",
    ) {
        App()
    }
}
