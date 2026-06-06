## ADDED Requirements

### Requirement: Windows MSI パッケージング
デスクトップアプリは Windows 向けに MSI インストーラ形式でビルドできなければならない（SHALL）。`build.gradle.kts` に `TargetFormat.Msi` と `windows {}` ブロックを設定し、`AppIcon.ico` を参照すること。

#### Scenario: MSI ビルドの実行
- **WHEN** `windows-latest` 環境で `.\gradlew.bat :desktop-app:packageMsi` を実行する
- **THEN** MSI ファイルが `desktop-app/build/compose/binaries/main/msi/` に生成される

#### Scenario: upgradeUuid の固定
- **WHEN** MSI インストーラをビルドする
- **THEN** `upgradeUuid` は固定値（`D7B4F1E2-3C5A-4B8D-9F0E-1A2B3C4D5E6F`）を使用し、バージョン間でアップグレードインストールが機能する

### Requirement: Windows 用アイコン
Windows MSI パッケージングに使用する `.ico` 形式のアイコンファイル（`desktop-app/icons/AppIcon.ico`）がリポジトリに含まれていなければならない（SHALL）。

#### Scenario: ICO ファイルの存在確認
- **WHEN** `desktop-app/build.gradle.kts` が `windows { iconFile.set(...AppIcon.ico) }` を参照する
- **THEN** `desktop-app/icons/AppIcon.ico` がリポジトリに存在し、ビルドが成功する
