package io.github.md2backlog.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.md2backlog.converter.BacklogToMarkdownConverter
import io.github.md2backlog.converter.MarkdownToBacklogConverter

private val DirtyIndicatorColor = Color(0xFFFFA500)

enum class Direction(val label: String) {
    MD_TO_BL("MD → BL"),
    BL_TO_MD("BL → MD"),
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    val clipboardManager = LocalClipboardManager.current
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var isDirty by remember { mutableStateOf(false) }
    var direction by remember { mutableStateOf(Direction.MD_TO_BL) }
    var directionMenuExpanded by remember { mutableStateOf(false) }

    fun setInput(text: String) {
        if (text == inputText) return
        inputText = text
        isDirty = true
    }

    fun convert() {
        outputText = when (direction) {
            Direction.MD_TO_BL -> MarkdownToBacklogConverter.convert(inputText)
            Direction.BL_TO_MD -> BacklogToMarkdownConverter.convert(inputText)
        }
        isDirty = false
    }

    fun paste() {
        val text = clipboardManager.getText()?.text ?: return
        if (text.isNotEmpty()) setInput(text)
    }

    fun copyResult() {
        if (outputText.isNotEmpty()) {
            clipboardManager.setText(AnnotatedString(outputText))
        }
    }

    val convertButtonColors = if (isDirty)
        ButtonDefaults.buttonColors(backgroundColor = DirtyIndicatorColor)
    else
        ButtonDefaults.buttonColors()

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    val isMeta = event.isMetaPressed || event.isCtrlPressed
                    when {
                        isMeta && event.key == Key.Enter -> { convert(); true }
                        isMeta && event.isShiftPressed && event.key == Key.C -> { copyResult(); true }
                        else -> false
                    }
                },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                OutlinedButton(onClick = { directionMenuExpanded = true }) {
                    Text(direction.label)
                }
                DropdownMenu(
                    expanded = directionMenuExpanded,
                    onDismissRequest = { directionMenuExpanded = false },
                ) {
                    Direction.entries.forEach { d ->
                        DropdownMenuItem(onClick = {
                            if (d != direction) {
                                direction = d
                                if (outputText.isNotEmpty()) isDirty = true
                            }
                            directionMenuExpanded = false
                        }) {
                            Text(d.label)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = ::setInput,
                    label = { Text("入力") },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                OutlinedTextField(
                    value = outputText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("出力") },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Button(onClick = ::paste) {
                    Text("貼り付け")
                }
                Button(
                    onClick = ::convert,
                    colors = convertButtonColors,
                ) {
                    Text("変換実行")
                }
                Button(onClick = ::copyResult) {
                    Text("コピー")
                }
            }
        }
    }
}
