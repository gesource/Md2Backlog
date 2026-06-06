# Claude Code スキルとして md2backlog を使う

Claude Code から `md2backlog` コマンドを呼び出して、Markdown ↔ Backlog 記法 の相互変換を自動化するための手順をまとめます。

## 構成

| ドキュメント | 内容 |
|-------------|------|
| [01-setup.md](./01-setup.md) | CLI バイナリをビルドし、`~/bin/` に配置して PATH を通すまでのセットアップ手順 |
| [02-create-skill.md](./02-create-skill.md) | `md2backlog` を呼び出す Claude Code エージェントスキルの作成方法 |

## 前提

- macOS (Apple Silicon / Intel) または Windows
- JDK 17 以上
- Claude Code がインストール済み

## 全体の流れ

```
1. CLI をビルド
        │
        ▼
2. ~/bin/ に配置 + PATH を通す
        │
        ▼
3. ~/.claude/skills/md2backlog/SKILL.md を作成
        │
        ▼
4. Claude Code でスキルが自動発動することを確認
```
