package io.github.md2backlog.jsapp

import io.github.md2backlog.converter.BacklogToMarkdownConverter
import io.github.md2backlog.converter.MarkdownToBacklogConverter

/**
 * Chrome 拡張向け Content Script。
 *
 * ページ上で選択されたテキストを変換し、クリップボードにコピーする。
 * manifest.json の content_scripts に登録して使用する。
 *
 * chrome.commands.onCommand は Manifest V3 のサービスワーカー専用 API のため、
 * コンテントスクリプトでは使用できない。background.js でコマンドを受け取り、
 * chrome.tabs.sendMessage でこのスクリプトに転送する構成が必要。
 *
 * Chrome 拡張の manifest.json 例:
 * {
 *   "manifest_version": 3,
 *   "name": "Md2Backlog",
 *   "version": "1.0",
 *   "background": {
 *     "service_worker": "background.js"
 *   },
 *   "content_scripts": [{
 *     "matches": ["<all_urls>"],
 *     "js": ["md2backlog-ext.js"]
 *   }],
 *   "permissions": ["clipboardWrite"],
 *   "commands": {
 *     "convert-to-backlog": { "suggested_key": { "default": "Ctrl+Shift+B" }, "description": "Convert selection to Backlog" },
 *     "convert-to-markdown": { "suggested_key": { "default": "Ctrl+Shift+M" }, "description": "Convert selection to Markdown" }
 *   }
 * }
 *
 * background.js 例:
 * chrome.commands.onCommand.addListener((command, tab) => {
 *   chrome.tabs.sendMessage(tab.id, { command });
 * });
 */
@Suppress("unused")
fun setupChromeExtension() {
    val chrome = js("typeof chrome !== 'undefined' ? chrome : null")
    if (chrome == null) return

    // background.js から chrome.tabs.sendMessage({ command: "..." }) で転送されたコマンドを処理する
    chrome.runtime.onMessage.addListener { message: dynamic, _: dynamic ->
        val command = (message.command as? String) ?: return@addListener

        val selected = getSelectedText()
        if (selected.isBlank()) return@addListener

        val converted = when (command) {
            "convert-to-backlog" -> MarkdownToBacklogConverter.convert(selected)
            "convert-to-markdown" -> BacklogToMarkdownConverter.convert(selected)
            else -> return@addListener
        }

        copyToClipboard(converted)
        showNotification("変換しました（${converted.length} 文字）")
    }
}

private fun getSelectedText(): String =
    js("window.getSelection().toString()") as? String ?: ""

private fun copyToClipboard(text: String) {
    js("navigator.clipboard.writeText(text).catch(function(e){console.error('Clipboard write failed:', e)})")
}

private fun showNotification(message: String) {
    // Kotlin/JS の js() は単一式のみ受け付けるため IIFE でラップ
    @Suppress("UNUSED_VARIABLE")
    val msg = message
    js("(function(m){var e=document.createElement('div');e.style.cssText='position:fixed;bottom:20px;right:20px;background:#333;color:#fff;padding:10px 16px;border-radius:6px;z-index:99999;font-size:14px;';e.textContent=m;document.body.appendChild(e);setTimeout(function(){e.parentNode&&e.parentNode.removeChild(e);},2500);})(msg)")
}
