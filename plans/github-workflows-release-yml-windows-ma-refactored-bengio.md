# リリースワークフロー拡張計画：デスクトップアプリの追加

## Context

現在の release.yml は macOS CLI と Windows CLI の 2 アーティファクトのみをリリースに添付している。  
要件として、以下 4 種類をすべて GitHub Release からダウンロードできるようにしたい：

1. macOS CLI バイナリ → ファイル名 `md2backlog`（既存・ファイル名変更）
2. Windows CLI バイナリ → ファイル名 `md2backlog.exe`（既存・ファイル名変更）
3. macOS デスクトップアプリ（`.pkg`）← 追加
4. Windows デスクトップアプリ（`.msi`）← 追加

`desktop-app` モジュールはすでに実装済みだが、build.gradle.kts は macOS のみ設定されており、Windows 向けの `TargetFormat.Msi` と `windows {}` ブロックが未追加。

## 変更ファイル一覧

| ファイル | 変更内容 |
|---------|---------|
| `desktop-app/build.gradle.kts` | `TargetFormat.Msi` と `windows {}` ブロックを追加 |
| `desktop-app/icons/AppIcon.ico` | Windows 用アイコンを新規追加（PNG から変換） |
| `.github/workflows/release.yml` | 各ジョブにデスクトップアプリのビルドステップを追加 |

---

## Step 1: desktop-app/build.gradle.kts の変更

```kotlin
nativeDistributions {
    targetFormats(TargetFormat.Pkg, TargetFormat.Msi)   // Msi を追加
    packageName = "Md2Backlog"
    packageVersion = "1.0.0"
    description = "Markdown ↔ Backlog 記法 相互変換ツール"
    vendor = "Sprix"

    macOS {
        bundleID = "io.github.md2backlog.desktop"
        iconFile.set(project.file("icons/AppIcon.icns"))
    }

    windows {
        iconFile.set(project.file("icons/AppIcon.ico"))
        upgradeUuid = "D7B4F1E2-3C5A-4B8D-9F0E-1A2B3C4D5E6F"  // ランダムGUID（アップグレード対応用）
    }
}
```

- `upgradeUuid` は一度決めたら変更しない（MSI アップグレード時に必要）
- Windows ビルドは `windows-latest` ランナー上でのみ実行可能

## Step 2: Windows 用アイコンファイルの追加

`icons/AppIcon.ico` をリポジトリに追加する。

既存の `icons/AppIcon.iconset/` に PNG が揃っているため、ローカルで ImageMagick を使って変換：

```bash
convert icons/AppIcon.iconset/icon_16x16.png \
        icons/AppIcon.iconset/icon_32x32.png \
        icons/AppIcon.iconset/icon_128x128.png \
        icons/AppIcon.iconset/icon_256x256.png \
        desktop-app/icons/AppIcon.ico
```

または macOS の sips + iconutil の代わりに、他のツールで変換してリポジトリにコミットしておく。  
**CI 上で動的生成はしない**（キャッシュ・再現性のため、バイナリをリポジトリに含める）。

## Step 3: .github/workflows/release.yml の変更

### build-macos-arm64 ジョブ（macOS CLI + macOS Desktop App）

```yaml
build-macos-arm64:
  runs-on: macos-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: 17
    - name: Build macOS ARM64 CLI
      run: ./gradlew :cli:renameReleaseExecutableMacosArm64
    - name: Copy CLI binary
      run: cp cli/build/bin/macosArm64/releaseExecutable/md2backlog md2backlog
    - name: Build macOS Desktop App
      run: ./gradlew :desktop-app:packagePkg
    - name: Copy Desktop App
      run: cp desktop-app/build/compose/binaries/main/pkg/Md2Backlog-1.0.0.pkg Md2Backlog-macos.pkg
    - uses: actions/upload-artifact@v4
      with:
        name: macos-arm64
        path: |
          md2backlog
          Md2Backlog-macos.pkg
```

### build-windows ジョブ（Windows CLI + Windows Desktop App）

```yaml
build-windows:
  runs-on: windows-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: 17
    - name: Build Windows x64 CLI
      run: .\gradlew.bat :cli:linkReleaseExecutableMingwX64
    - name: Copy CLI binary
      run: cp cli\build\bin\mingwX64\releaseExecutable\md2backlog.exe md2backlog.exe
    - name: Build Windows Desktop App
      run: .\gradlew.bat :desktop-app:packageMsi
    - name: Copy Desktop App
      run: cp desktop-app\build\compose\binaries\main\msi\Md2Backlog-1.0.0.msi Md2Backlog-windows.msi
    - uses: actions/upload-artifact@v4
      with:
        name: windows-x64
        path: |
          md2backlog.exe
          Md2Backlog-windows.msi
```

### release ジョブ（変更箇所のみ）

```yaml
- name: Create Release
  uses: softprops/action-gh-release@v2
  with:
    files: |
      artifacts/md2backlog
      artifacts/Md2Backlog-macos.pkg
      artifacts/md2backlog.exe
      artifacts/Md2Backlog-windows.msi
    generate_release_notes: true
```

---

## 注意事項

### Windows MSI ビルド要件
`jpackage`（JDK 17 に同梱）が MSI 生成に WiX Toolset を使用する。  
`windows-latest` ランナーには WiX v3 がプリインストールされているため追加インストール不要。

### macOS .pkg の署名
CI 環境では署名なし（unsigned）でビルドされる。  
ユーザーが起動時に Gatekeeper の警告が出る可能性があるが、今回のスコープでは署名対応は含めない。

### packageVersion のハードコード
`build.gradle.kts` に `packageVersion = "1.0.0"` がハードコードされている。  
ファイル名（`Md2Backlog-1.0.0.pkg` 等）はこの値に依存するため、  
タグバージョンと一致させたい場合は別途対応が必要（今回のスコープ外）。

---

## 確認方法

1. ローカルで各コマンドを実行して成果物パスを確認する：
   - `./gradlew :desktop-app:packagePkg` → `desktop-app/build/compose/binaries/main/pkg/`
   - `.\gradlew.bat :desktop-app:packageMsi` → `desktop-app\build\compose\binaries\main\msi\`
2. `v0.0.1-test` などのテストタグを push して CI を走らせる
3. GitHub Release のアセット一覧に 4 ファイルが添付されていることを確認する
