# release-desktop-app-artifacts

## Purpose

TBD: GitHub Release へのデスクトップアプリ成果物（macOS `.pkg` / Windows `.msi`）の添付に関する仕様。

## Requirements

### Requirement: macOS デスクトップアプリのリリース添付
CI は `v*` タグ push 時に macOS デスクトップアプリ（`.pkg`）をビルドし、GitHub Release に添付しなければならない（SHALL）。

#### Scenario: macOS .pkg のビルドと添付
- **WHEN** `v*` タグを push して CI が実行される
- **THEN** `macos-latest` ランナーで `.pkg` がビルドされ、GitHub Release のアセットとして添付される

#### Scenario: macOS .pkg の成果物パス
- **WHEN** `./gradlew :desktop-app:packagePkg` が成功する
- **THEN** `desktop-app/build/compose/binaries/main/pkg/Md2Backlog-1.0.0.pkg` が生成される

### Requirement: Windows デスクトップアプリのリリース添付
CI は `v*` タグ push 時に Windows デスクトップアプリ（`.msi`）をビルドし、GitHub Release に添付しなければならない（SHALL）。

#### Scenario: Windows .msi のビルドと添付
- **WHEN** `v*` タグを push して CI が実行される
- **THEN** `windows-latest` ランナーで `.msi` がビルドされ、GitHub Release のアセットとして添付される

#### Scenario: Windows .msi の成果物パス
- **WHEN** `.\gradlew.bat :desktop-app:packageMsi` が成功する
- **THEN** `desktop-app\build\compose\binaries\main\msi\Md2Backlog-1.0.0.msi` が生成される

### Requirement: リリースアセット一覧
GitHub Release には 4 種類のアセットが添付されなければならない（SHALL）：macOS CLI バイナリ、Windows CLI バイナリ、macOS `.pkg`、Windows `.msi`。

#### Scenario: 全アセットの確認
- **WHEN** リリースが完了する
- **THEN** GitHub Release のアセット一覧に `md2backlog-macos-arm64`、`md2backlog-windows-x64.exe`、`Md2Backlog-macos.pkg`、`Md2Backlog-windows.msi` の 4 ファイルが存在する
