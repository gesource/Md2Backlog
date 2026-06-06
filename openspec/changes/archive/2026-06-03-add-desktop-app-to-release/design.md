## Context

現在の `.github/workflows/release.yml` は 2 ジョブ構成：

- `build-macos-arm64`: macOS CLI バイナリ（`md2backlog-macos-arm64`）をビルドしてアップロード
- `build-windows`: Windows CLI バイナリ（`md2backlog-windows-x64.exe`）をビルドしてアップロード
- `release`: 上記 2 ファイルを GitHub Release に添付

`desktop-app/build.gradle.kts` は `TargetFormat.Pkg`（macOS のみ）が設定済み。Windows MSI に必要な `TargetFormat.Msi` と `windows {}` ブロックは未追加。

## Goals / Non-Goals

**Goals:**

- macOS `.pkg` と Windows `.msi` のデスクトップアプリインストーラを GitHub Release に添付
- Windows MSI ビルドを `windows-latest` ランナー上で動作させる
- アップグレード対応のため `upgradeUuid` を固定する

**Non-Goals:**

- macOS / Windows コード署名・公証（Notarization）への対応
- `packageVersion` の動的設定（タグバージョンとの連動）
- x86 など ARM64 / x64 以外のアーキテクチャへの対応

## Decisions

### 1. CLIバイナリのファイル名変更（`-macos-arm64` / `-windows-x64` サフィックスを除去）

**決定**: 今回のスコープ外とし、既存の名前（`md2backlog-macos-arm64`、`md2backlog-windows-x64.exe`）を維持する。

**理由**: 既存のダウンロードリンクや自動化スクリプトを壊さないため。名前変更は別の変更として扱う。

### 2. ICO ファイルのリポジトリへの事前コミット

**決定**: `desktop-app/icons/AppIcon.ico` をローカルで生成してリポジトリにコミットする。CI 上での動的変換は行わない。

**理由**:
- ImageMagick など追加ツールのインストールステップが不要
- CI の再現性とキャッシュ効率が高い
- ICO ファイルは静的リソースであり、変更頻度が極めて低い

**代替案**: CI で `sips` / `convert` を実行して動的生成 → ツール依存と実行時間増加のため却下。

### 3. upgradeUuid の固定値

**決定**: `D7B4F1E2-3C5A-4B8D-9F0E-1A2B3C4D5E6F` を `build.gradle.kts` にハードコードし、変更しない。

**理由**: MSI のアップグレード（上書きインストール）は `upgradeUuid` が同一であることを要求する。変更すると新規インストールとして扱われ、旧バージョンが残存する。

### 4. packageVersion のハードコード維持

**決定**: `packageVersion = "1.0.0"` をそのまま維持し、コピー先ファイル名を固定（`Md2Backlog-1.0.0.pkg` / `Md2Backlog-1.0.0.msi`）でリリースに添付する。

**理由**: 今回のスコープはリリース添付の実現であり、バージョン連動は別タスクとして切り出す。

### 5. デスクトップアプリビルドを既存ジョブに統合（ジョブ追加なし）

**決定**: 既存の `build-macos-arm64` / `build-windows` ジョブにデスクトップアプリビルドステップを追加する。新規ジョブは作らない。

**理由**: ランナーは OS に縛られているため、macOS ビルドは `macos-latest`、Windows ビルドは `windows-latest` で行う必要がある。CI 時間・コストを最小化するため統合を選択。

## Risks / Trade-offs

- **WiX Toolset バージョン**: `windows-latest` に同梱の WiX v3 に依存。GitHub Actions の `windows-latest` イメージが更新されて WiX が除去・バージョン変更された場合、ビルドが壊れる。→ CI ログで WiX バージョンを確認し、必要であれば明示インストールに切り替える。
- **packageVersion 固定によるファイル名不整合**: タグ `v0.2.0` のリリースでも成果物名が `Md2Backlog-1.0.0.pkg` になる。→ ユーザー向けリリースノートに注記するか、別タスクでバージョン連動を実装する。
- **Gatekeeper 警告（macOS）**: 署名なし `.pkg` はユーザーが初回起動時に警告を受ける。→ 今回スコープ外。ドキュメントに手順を記載することで対応。
