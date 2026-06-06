## Context

既存の `core/` モジュールは KMP で実装済みだが、JVM ターゲットがない。  
Compose Multiplatform のデスクトップは JVM 上で動作するため、`core` への JVM ターゲット追加が前提となる。  
ユーザーの主なユースケースは「Markdown テキストを貼り付け → Backlog 記法に変換 → コピー」であり、GUI は最小限のシンプルな構成を目指す。

## Goals / Non-Goals

**Goals:**
- Mac / Windows 両対応のデスクトップアプリを単一コードベースで実装
- 2 ペイン UI（左: 入力、右: 出力）でテキストを視認しながら編集可能
- ボタンとキーボードショートカット両方で「貼り付け・変換・コピー」を操作可能
- `./gradlew :desktop-app:run` でローカル起動できること

**Non-Goals:**
- ファイル保存 / 読み込み（今回のスコープ外）
- 自動アップデート機能
- インストーラ生成（`packageDmg` / `packageMsi`）は将来フェーズ
- Windows ARM 対応（JVM なので実行環境次第で動くが、明示的にサポートしない）

## Decisions

### 1. Compose Multiplatform（JVM）を採用する

**採用理由:**
- 既存コードが Kotlin であり、同じ言語・エコシステムで UI を記述できる
- Mac / Windows を単一コードベースでカバーできる
- JVM ターゲットは `core` への軽微な設定追加のみで対応可能

**検討した代替案:**
- Swing / JavaFX: Kotlin とのエルゴノミクスが悪く、UI 記述が冗長
- Kotlin/Native GUI（macOS ネイティブ）: Windows 非対応、ビルド複雑
- Electron（既存 js-app を流用）: バイナリが重く、ネイティブ感が薄い

### 2. `desktop-app/` モジュールを新規追加する

既存の `cli/` `js-app/` と命名規則を揃える。  
`core` への依存は `implementation(project(":core"))` で解決する。

```
settings.gradle.kts に include(":desktop-app") を追加
desktop-app/build.gradle.kts で Compose Multiplatform を設定
desktop-app/src/jvmMain/kotlin/ にソースを配置
```

### 3. `core` に `jvm()` ターゲットを追加する

`core/build.gradle.kts` の `kotlin { }` ブロックに `jvm()` を1行追加するだけ。  
`commonMain` のコードはそのまま流用できる。依存の `org.jetbrains:markdown:0.7.3` は JVM 対応済み。

### 4. 変換は手動実行（Cmd+Enter / ボタン）

**採用理由:**
- リアルタイム変換だと入力中に右ペインが変わり集中を乱す
- debounce を入れても実装が複雑になる
- 「編集→確認→コピー」のサイクルに手動実行が自然に合う

入力ペイン変更後・未変換の状態はインジケーター（テキスト色の変化など）で伝える。

### 5. 変換方向はデフォルト MD→BL、ドロップダウンで切替

MD→BL がメインユースケース。BL→MD は補助として提供。  
アプリ再起動時にデフォルト（MD→BL）に戻る（設定の永続化は今回スコープ外）。

### 6. クリップボード操作に Compose の `ClipboardManager` を使用

Compose Multiplatform が提供する `LocalClipboardManager` でテキストの読み書きを行う。  
- 「貼り付け」ボタン: `clipboardManager.getText()` → 入力ペインにセット
- 「コピー」ボタン: `clipboardManager.setText(...)` → 出力ペインの内容をコピー

## Risks / Trade-offs

- **JRE バンドルなし**: `./gradlew run` は JRE が必要。配布時は `packageDmg` 等で JRE をバンドルする（将来フェーズ）。
- **Mac 署名なし**: 初期は自分用のため GateKeeper の警告が出る。一般配布時に Apple Developer Program が必要。
- **Windows テスト環境**: Mac のみで開発する場合、Windows 動作確認は CI（GitHub Actions Windows runner）に依存する。
- **Compose バージョン管理**: Kotlin と Compose Multiplatform のバージョン互換性を維持する必要がある（Kotlin 2.1.20 対応バージョンを選定）。

## Migration Plan

1. `core/build.gradle.kts` に `jvm()` を追加し、既存テストが通ることを確認
2. `desktop-app/` モジュールを作成し、`./gradlew :desktop-app:run` で起動確認
3. UI 実装（2 ペイン、変換ボタン、コピーボタン）
4. キーボードショートカット実装

ロールバック戦略: モジュール追加のみのため、`:desktop-app` を削除するだけで元の状態に戻る。`core` への `jvm()` 追加は既存ビルドに影響しない。

## Open Questions

- Compose Multiplatform の採用バージョン（Kotlin 2.1.20 対応の最新安定版を確認）
- 入力ペインが未変換状態のインジケーターをどう表示するか（ラベル変更 vs ボーダー色変更）
