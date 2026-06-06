## ADDED Requirements

### Requirement: JVM ターゲットのサポート
`core` モジュールは JVM ターゲットをサポートし、`MarkdownToBacklogConverter` および `BacklogToMarkdownConverter` を JVM 環境から利用できること。

#### Scenario: JVM からの変換呼び出し
- **WHEN** JVM アプリケーションが `MarkdownToBacklogConverter.convert(text)` を呼び出す
- **THEN** Backlog 記法に変換されたテキストが返る

### Requirement: 既存ターゲットへの影響がないこと
`jvm()` ターゲットの追加は、既存の `macosArm64`・`mingwX64`・`js` ターゲットのビルドおよびテストに影響を与えないこと。

#### Scenario: 既存ターゲットのテストが通ること
- **WHEN** `./gradlew :core:macosArm64Test` を実行する
- **THEN** 既存のすべてのテストが成功する

#### Scenario: JS ターゲットのテストが通ること
- **WHEN** `./gradlew :core:jsTest` を実行する
- **THEN** 既存のすべてのテストが成功する
