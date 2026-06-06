## 1. Windows 用アイコンの準備

- [x] 1.1 ImageMagick などを使って `desktop-app/icons/AppIcon.ico` を生成する（既存 PNG から変換: `icon_16x16.png`、`icon_32x32.png`、`icon_128x128.png`、`icon_256x256.png`）
- [x] 1.2 生成した `AppIcon.ico` をリポジトリにコミットする

## 2. build.gradle.kts の更新

- [x] 2.1 `desktop-app/build.gradle.kts` の `targetFormats(...)` に `TargetFormat.Msi` を追加する
- [x] 2.2 `nativeDistributions` ブロックに `windows {}` ブロックを追加し、`iconFile` を `AppIcon.ico` に設定する
- [x] 2.3 `windows {}` ブロックに `upgradeUuid = "D7B4F1E2-3C5A-4B8D-9F0E-1A2B3C4D5E6F"` を設定する
- [x] 2.4 ローカル（macOS）で `./gradlew :desktop-app:packagePkg` が成功することを確認し、成果物パス（`desktop-app/build/compose/binaries/main/pkg/Md2Backlog-1.0.0.pkg`）を検証する

## 3. GitHub Actions ワークフローの更新

- [x] 3.1 `build-macos-arm64` ジョブに `./gradlew :desktop-app:packagePkg` ステップを追加する
- [x] 3.2 `build-macos-arm64` ジョブに `.pkg` を `Md2Backlog-macos.pkg` としてコピーするステップを追加する
- [x] 3.3 `build-macos-arm64` ジョブの `upload-artifact` の `path` に `Md2Backlog-macos.pkg` を追加する
- [x] 3.4 `build-windows` ジョブに `.\gradlew.bat :desktop-app:packageMsi` ステップを追加する
- [x] 3.5 `build-windows` ジョブに `.msi` を `Md2Backlog-windows.msi` としてコピーするステップを追加する
- [x] 3.6 `build-windows` ジョブの `upload-artifact` の `path` に `Md2Backlog-windows.msi` を追加する
- [x] 3.7 `release` ジョブの `softprops/action-gh-release` の `files` に `artifacts/Md2Backlog-macos.pkg` と `artifacts/Md2Backlog-windows.msi` を追加する

## 4. 動作確認

- [x] 4.1 テスト用タグ（例：`v0.0.1-test`）を push して CI を実行する
- [x] 4.2 `build-macos-arm64` ジョブが `.pkg` のビルドとアップロードに成功することを確認する
- [x] 4.3 `build-windows` ジョブが `.msi` のビルドとアップロードに成功することを確認する
- [x] 4.4 GitHub Release のアセット一覧に 4 ファイル（`md2backlog-macos-arm64`、`md2backlog-windows-x64.exe`、`Md2Backlog-macos.pkg`、`Md2Backlog-windows.msi`）が揃っていることを確認する
