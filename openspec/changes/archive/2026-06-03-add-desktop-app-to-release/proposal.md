## Why

GitHub Release に CLI バイナリ（macOS・Windows）しか添付されておらず、デスクトップアプリ（`.pkg` / `.msi`）をダウンロードする手段がない。`desktop-app` モジュールは実装済みのため、CI ビルドとリリース添付を追加することで、ユーザーが GitHub Release から直接インストーラを入手できるようにする。

## What Changes

- `desktop-app/build.gradle.kts` に `TargetFormat.Msi` と `windows {}` ブロックを追加（Windows MSI ビルドを有効化）
- Windows 用アイコンファイル `desktop-app/icons/AppIcon.ico` をリポジトリに追加
- `.github/workflows/release.yml` の `build-macos-arm64` ジョブに macOS デスクトップアプリ（`.pkg`）のビルド・アップロードステップを追加
- `.github/workflows/release.yml` の `build-windows` ジョブに Windows デスクトップアプリ（`.msi`）のビルド・アップロードステップを追加
- `release` ジョブの GitHub Release 添付ファイル一覧に `.pkg` と `.msi` を追加

## Capabilities

### New Capabilities

- `release-desktop-app-artifacts`: GitHub Release に macOS `.pkg` と Windows `.msi` のインストーラを添付するビルド＆リリースフロー

### Modified Capabilities

- `desktop-app-ui`: Windows 向けビルド設定（`TargetFormat.Msi`・`windows {}`）と `.ico` アイコンを追加し、Windows MSI ビルドを可能にする

## Impact

- **ファイル**: `desktop-app/build.gradle.kts`、`desktop-app/icons/AppIcon.ico`（新規）、`.github/workflows/release.yml`
- **CI**: `windows-latest` ランナーで WiX Toolset（プリインストール済み）を使用した `jpackage` による MSI 生成が走る
- **リリース成果物**: GitHub Release の添付ファイルが 2 件（CLI のみ）から 4 件（CLI + デスクトップアプリ）に増加
- **依存**: JDK 17 付属の `jpackage`、WiX Toolset v3（`windows-latest` に同梱）
