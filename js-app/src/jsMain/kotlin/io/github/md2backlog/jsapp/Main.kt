package io.github.md2backlog.jsapp

/**
 * JS モジュールのエントリポイント。
 *
 * - Node.js 環境: NodeMain.kt の runNodeCli() を呼び出す（引数処理・stdin・ファイル読み込み）
 * - ブラウザ / Chrome 拡張環境: ChromeExt.kt の setupChromeExtension() を呼び出す
 *   さらに WebApi.kt の Md2Backlog オブジェクトが @JsExport で公開される
 */
fun main() {
    val isNode = js(
        "typeof process !== 'undefined' && process.versions != null && process.versions.node != null"
    ) as Boolean

    if (isNode) {
        runNodeCli()
    } else {
        setupChromeExtension()
    }
}
