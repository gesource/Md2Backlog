## Why

CLI と JS アプリは既にあるが、GUI なしでは「貼り付け→変換→コピー」のワークフローに摩擦がある。Compose Multiplatform を使ったデスクトップアプリを追加することで、Mac / Windows 両対応の GUI ツールを単一コードベースで実現する。

## What Changes

- `desktop-app/` モジュールを新規追加（Compose Multiplatform デスクトップアプリ）
- `core/` モジュールに `jvm()` ターゲットを追加（Compose デスクトップは JVM 上で動作するため）
- `settings.gradle.kts` に `:desktop-app` を追加

## Capabilities

### New Capabilities

- `desktop-app-ui`: Markdown ↔ Backlog 変換の 2 ペイン GUI。入力ペイン・出力ペインを左右に配置し、変換実行・貼り付け・コピーをボタンおよびキーボードショートカットで操作できる。
- `core-jvm-target`: `core` モジュールへの JVM ターゲット追加。既存の commonMain コードはそのまま流用し、ビルド設定のみ変更する。

### Modified Capabilities

（なし）

## Impact

- **新規モジュール**: `desktop-app/`（Compose Multiplatform、JVM）
- **既存モジュール変更**: `core/build.gradle.kts`（`jvm()` 追加）、`settings.gradle.kts`（`:desktop-app` 追加）
- **新規依存**: `org.jetbrains.compose`（Compose Multiplatform Gradle プラグイン）、`androidx.compose.*`
- **既存モジュールへの影響**: `cli/`・`js-app/` への変更なし
- **配布**: 当初は `./gradlew :desktop-app:run` でローカル実行のみ。将来的に `packageDmg` / `packageMsi` で JRE バンドル済みインストーラを生成可能
