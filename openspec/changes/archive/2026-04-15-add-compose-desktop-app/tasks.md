## 1. ビルド設定

- [x] 1.1 Kotlin 2.1.20 と互換性のある Compose Multiplatform の最新安定バージョンを確認する
- [x] 1.2 `core/build.gradle.kts` の `kotlin { }` ブロックに `jvm()` を追加する
- [x] 1.3 `./gradlew :core:macosArm64Test` が成功することを確認する
- [x] 1.4 `./gradlew :core:jsTest` が成功することを確認する
- [x] 1.5 `desktop-app/` ディレクトリと `build.gradle.kts` を作成する（Compose Multiplatform Gradle プラグイン設定、`jvm()` ターゲット、`compose.desktop.currentOs` 依存）
- [x] 1.6 `settings.gradle.kts` に `include(":desktop-app")` を追加する
- [x] 1.7 `dependencyResolutionManagement` の `repositories` に `google()` を追加する（Compose 依存の解決に必要）

## 2. アプリ起動の骨格

- [x] 2.1 `desktop-app/src/jvmMain/kotlin/` に `main.kt` を作成し、`application { Window(...) {} }` で空のウィンドウを表示する
- [ ] 2.2 `./gradlew :desktop-app:run` でウィンドウが開くことを確認する

## 3. UI 実装

- [x] 3.1 変換方向セレクタ（MD→BL / BL→MD のドロップダウンまたはトグルボタン）を実装する。デフォルトは MD→BL
- [x] 3.2 左ペインに編集可能な入力 `TextField` を実装する（スクロール対応）
- [x] 3.3 右ペインに読み取り専用の出力 `TextField` を実装する（スクロール対応）
- [x] 3.4 「貼り付け」ボタンを実装する（`LocalClipboardManager.getText()` の内容を入力ペインにセット）
- [x] 3.5 「変換実行」ボタンを実装する（変換方向に応じて `MarkdownToBacklogConverter` または `BacklogToMarkdownConverter` を呼び出し、結果を出力ペインに反映）
- [x] 3.6 「コピー」ボタンを実装する（`LocalClipboardManager.setText()` で出力ペインの全テキストをクリップボードへ）
- [x] 3.7 未変換状態インジケーターを実装する（入力ペイン変更後に「変換実行」ボタンのラベルまたは色でアラート、変換実行後にリセット）

## 4. キーボードショートカット

- [x] 4.1 `Cmd+Enter`（Mac）/ `Ctrl+Enter`（Windows）で変換実行を起動する
- [x] 4.2 `Cmd+Shift+C`（Mac）/ `Ctrl+Shift+C`（Windows）でコピーを起動する

## 5. 動作確認

- [x] 5.1 空入力で変換実行しても出力ペインが空になることを確認する（エラーなし）
- [x] 5.2 クリップボードが空の状態で「貼り付け」ボタンを押しても入力ペインが変化しないことを確認する
- [x] 5.3 出力ペインが空の状態で「コピー」ボタンを押してもクリップボードが変化しないことを確認する
- [ ] 5.4 Mac で MD→BL・BL→MD の両方向の変換をエンドツーエンドで確認する
