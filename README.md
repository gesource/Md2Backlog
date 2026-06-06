# Md2Backlog

Markdown ↔ [Backlog 記法](https://backlog.com/ja/blog/how-to-write-in-backlog-wiki-markup/) の相互変換ツール。

Kotlin Multiplatform で実装されており、正規表現ではなく AST (抽象構文木) を使って変換するため、ネスト・複合要素も正確に処理します。

## 対応プラットフォーム

| プラットフォーム | 形態 |
|----------------|------|
| macOS / Windows | デスクトップアプリ（Compose Multiplatform GUI） |
| macOS (ARM64 / x64) | ネイティブ CLI バイナリ |
| Windows (x64) | ネイティブ CLI バイナリ |
| Node.js | JS モジュール（CLI として動作） |
| Web ブラウザ | JS ライブラリ（`Md2Backlog` グローバル API） |
| Chrome 拡張 | Content Script（選択テキストを変換） |

## 変換対応表

| 要素 | Markdown | Backlog 記法 |
|------|----------|-------------|
| 見出し 1〜6 | `# H1` 〜 `###### H6` | `* H1` 〜 `****** H6` |
| 太字 | `**text**` | `''text''` |
| 斜体 | `*text*` | `'''text'''` |
| 太字＋斜体 | `***text***` | `'''''text'''''` |
| 打消し線 | `~~text~~` | `%%text%%` |
| インラインコード | `` `code` `` | `` `code` `` |
| コードブロック | ` ```lang ` | `{code:lang}` / `{/code}` |
| リンク | `[text](url)` | `[[text:url]]` |
| 画像 | `![alt](src)` | `[[alt:src]]` |
| 番号なしリスト | `- item` | `- item` |
| 番号なしリスト（ネスト） | インデント + `- item` | `-- item` / `--- item` |
| 番号付きリスト | `1. item` | `+ item` |
| 番号付きリスト（ネスト） | インデント + `1. item` | `++ item` / `+++ item` |
| 引用 | `> text` | `{quote}...{/quote}` |
| 水平線 | `---` | `----` |
| テーブル | GFM 形式 | パイプ区切り（セパレーター行なし） |

## Backlog 記法の制約

Markdown では可能だが Backlog 記法では表現できない要素があります。変換時に以下の挙動になります。

| Markdown の記述 | 変換後の挙動 |
|----------------|-------------|
| リストアイテム内のコードブロック | コードブロックがリストの外に出力される。番号付きリストの場合、リストが分断されて番号がリセットされる |
| タスクリスト（`- [x]` / `- [ ]`） | `[x]` / `[ ]` がプレーンテキストとして出力される |
| ネストされた引用（`>>`） | Backlog の引用は1段のみ。多段は1段に統合される |
| リンク・画像のタイトル属性（`[text](url "title")`） | タイトル属性は変換時に失われる |
| 番号付きリストの開始番号（`3. item` など） | Backlog は開始番号を指定できないため、常に `1.` から始まる |
| HTML タグ（`<details>` など） | 変換時に消える |

## インストール・使い方

### デスクトップアプリ（macOS / Windows）

Compose Multiplatform 製の GUI アプリです。JDK 17 以上が必要です。

```bash
# 起動
./gradlew :desktop-app:run
```

起動後の操作：

| 操作 | マウス | キーボード |
|------|-------|----------|
| テキストを貼り付け | [貼り付け] ボタン | Cmd+V（Mac）/ Ctrl+V（Win） |
| 変換を実行 | [変換実行] ボタン | Cmd+Enter（Mac）/ Ctrl+Enter（Win） |
| 結果をコピー | [コピー] ボタン | Cmd+Shift+C（Mac）/ Ctrl+Shift+C（Win） |
| 変換方向を切替 | [MD→BL ▼] ドロップダウン | — |

変換方向はデフォルト MD→BL。入力を変更すると「変換実行」ボタンがオレンジ色になり、未変換状態を示します。

#### 配布用 macOS パッケージのビルド

`.pkg` インストーラを生成し、ダブルクリックで `/Applications/Md2Backlog.app` にインストールできます。

```bash
./gradlew :desktop-app:packagePkg
```

成果物: `desktop-app/build/compose/binaries/main/pkg/Md2Backlog-1.0.0.pkg`

> **macOS 26.4 系の DMG 不具合について**
> macOS 26.4 では `hdiutil` の HFS+ マウント不具合により `.dmg` のビルドが失敗します（Apple feedback ID 168672160）。`.pkg` は影響を受けないため、現状はこちらを採用しています。

未署名のため、初回起動時は **`.pkg` を右クリック → 「開く」** を選択してください（Gatekeeper 警告の回避）。

##### アイコンを変更する

`desktop-app/icons/AppIcon.svg` を編集後、以下で `.icns` を再生成します。

```bash
cd desktop-app/icons
mkdir -p AppIcon.iconset
for size in 16 32 64 128 256 512 1024; do
  rsvg-convert -w $size -h $size AppIcon.svg -o "AppIcon.iconset/_${size}.png"
done
cp AppIcon.iconset/_16.png   AppIcon.iconset/icon_16x16.png
cp AppIcon.iconset/_32.png   AppIcon.iconset/icon_16x16@2x.png
cp AppIcon.iconset/_32.png   AppIcon.iconset/icon_32x32.png
cp AppIcon.iconset/_64.png   AppIcon.iconset/icon_32x32@2x.png
cp AppIcon.iconset/_128.png  AppIcon.iconset/icon_128x128.png
cp AppIcon.iconset/_256.png  AppIcon.iconset/icon_128x128@2x.png
cp AppIcon.iconset/_256.png  AppIcon.iconset/icon_256x256.png
cp AppIcon.iconset/_512.png  AppIcon.iconset/icon_256x256@2x.png
cp AppIcon.iconset/_512.png  AppIcon.iconset/icon_512x512.png
cp AppIcon.iconset/_1024.png AppIcon.iconset/icon_512x512@2x.png
rm AppIcon.iconset/_*.png
iconutil -c icns AppIcon.iconset -o AppIcon.icns
```

`rsvg-convert` は `brew install librsvg` で導入できます。

### macOS / Linux ネイティブ CLI

```bash
# ビルド (macOS ARM64)
./gradlew :cli:linkReleaseExecutableMacosArm64

# 使用
./cli/build/bin/macosArm64/releaseExecutable/md2backlog --to-backlog input.md
./cli/build/bin/macosArm64/releaseExecutable/md2backlog --to-markdown input.backlog

# stdin からも読める
echo "# Hello" | md2backlog --to-backlog
cat input.md | md2backlog --to-backlog
```

### Windows ネイティブ CLI

```powershell
# ビルド (Windows x64) ※ Windows 環境または CI で実行
.\gradlew :cli:linkReleaseExecutableMingwX64

# 使用
.\cli\build\bin\mingwX64\releaseExecutable\md2backlog.exe --to-backlog input.md
```

### Node.js

```bash
# JS ビルド
./gradlew :js-app:compileDevelopmentExecutableKotlinJs

# 使用
node js-app/build/compileSync/js/main/developmentExecutable/kotlin/md2backlog-js-app.js \
  --to-backlog input.md

echo "# Hello" | node md2backlog-js-app.js --to-backlog
```

### ブラウザ（Web ライブラリ）

```bash
# ブラウザ向けバンドルをビルド
./gradlew :js-app:jsBrowserProductionWebpack
```

生成されたファイルは `js-app/build/kotlin-webpack/js/productionExecutable/js-app.js` に出力されます。
これを HTML に読み込むと `Md2Backlog` グローバルオブジェクトが利用できます。

```html
<script src="js-app.js"></script>
<script>
  const backlog = Md2Backlog.toBacklog("# Hello **World**");
  // → "* Hello ''World''"

  const markdown = Md2Backlog.toMarkdown("* Hello ''World''");
  // → "# Hello **World**"
</script>
```

### Chrome 拡張

`js-app.js` を Chrome 拡張の content script として登録します。

```jsonc
// manifest.json
{
  "manifest_version": 3,
  "name": "Md2Backlog",
  "version": "1.0",
  "content_scripts": [{
    "matches": ["<all_urls>"],
    "js": ["js-app.js"]
  }],
  "commands": {
    "convert-to-backlog":  { "suggested_key": { "default": "Ctrl+Shift+B" }, "description": "Convert selection to Backlog" },
    "convert-to-markdown": { "suggested_key": { "default": "Ctrl+Shift+M" }, "description": "Convert selection to Markdown" }
  }
}
```

ページ上でテキストを選択して `Ctrl+Shift+B` / `Ctrl+Shift+M` で変換 → クリップボードにコピーされます。

## ビルド要件

| ツール | バージョン |
|--------|-----------|
| JDK | 17 以上 |
| Gradle | 8.13（`gradlew` で自動取得） |
| Kotlin | 2.1.20 |
| XCode Command Line Tools | macOS ネイティブビルドに必要 |

## 開発・テスト

```bash
# コアライブラリのテスト（macOS ARM64 + JS）
./gradlew :core:macosArm64Test :core:jsTest

# 全ターゲットのテスト
./gradlew :core:allTests

# macOS ARM64 CLI をデバッグビルド
./gradlew :cli:linkDebugExecutableMacosArm64

# ブラウザバンドル（開発用）
./gradlew :js-app:jsBrowserDevelopmentWebpack
```

## アーキテクチャ

```
入力テキスト
    │
    ▼
┌─────────────────────┐
│  Parser             │  MarkdownParser (JetBrains Markdown ライブラリ)
│                     │  BacklogParser  (手書き再帰下降パーサー)
└──────────┬──────────┘
           │ Node ツリー（統一 AST）
    ▼
┌─────────────────────┐
│  Renderer           │  BacklogRenderer
│                     │  MarkdownRenderer
└──────────┬──────────┘
           │
    ▼
出力テキスト
```

変換はすべて Parser → 統一 AST → Renderer の2段階パイプラインで行われます。正規表現による直接変換は行いません。

### モジュール構成

| モジュール | 役割 | ターゲット |
|-----------|------|----------|
| `:core` | AST・パーサー・レンダラー・コンバーター | mingwX64, macosX64, macosArm64, js, jvm |
| `:cli` | ネイティブ CLI バイナリ | mingwX64, macosX64, macosArm64 |
| `:js-app` | JS エントリポイント（Node.js / Web / Chrome 拡張） | js |
| `:desktop-app` | Compose Multiplatform デスクトップアプリ | jvm |

## ライセンス

MIT
