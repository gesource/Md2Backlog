# 02. md2backlog エージェントスキルの作成

`md2backlog` コマンドを Claude Code から呼び出すためのエージェントスキルを作成する手順です。

> **前提**: [01-setup.md](./01-setup.md) を完了し、ターミナルから `md2backlog` が実行できる状態になっていること。

## 1. スキルとは

Claude Code の **スキル (Skill)** は、特定の状況で自動的に発動する手順書です。`SKILL.md` の `description` フィールドに「いつ発動するか」を書いておくと、ユーザーの依頼内容に応じて Claude Code が自動で呼び出します。

スキルは以下のいずれかの場所に配置できます。

| スコープ | パス | 適用範囲 |
|---------|------|---------|
| ユーザー (グローバル) | `~/.claude/skills/<skill-name>/SKILL.md` | 全プロジェクト |
| プロジェクト | `<project-root>/.claude/skills/<skill-name>/SKILL.md` | そのプロジェクトのみ |

`md2backlog` は変換ツールでありプロジェクト横断で使うため、**ユーザースコープ** に配置するのを推奨します。

## 2. スキルファイルの作成

### ディレクトリを作成

```bash
mkdir -p ~/.claude/skills/md2backlog
```

### SKILL.md を作成

`~/.claude/skills/md2backlog/SKILL.md` として以下を保存します。

````markdown
---
name: md2backlog
description: Markdown 形式のドキュメントを Backlog 記法へ、または Backlog 記法を Markdown へ変換する。ユーザーが「Backlog 記法に変換」「Backlog Wiki に貼り付ける形にして」「md → backlog」「Markdown に戻して」などと依頼したとき、または .md / .backlog ファイルを相互変換する必要があるときに発動する。正規表現での変換ではなく `md2backlog` コマンド (AST ベース) を必ず使用する。
---

# md2backlog 変換スキル

## 目的

Markdown ↔ Backlog 記法 の変換は、必ず `md2backlog` CLI を経由して行う。Claude が手動で正規表現や逐次置換による変換をしないこと。

理由:

- Backlog 記法は `''bold''`、`'''italic'''`、`{code}`、`{quote}`、`[[text:url]]` など独自の記号体系を持ち、ネスト・複合要素を正規表現で正しく扱うのは困難。
- `md2backlog` は AST (抽象構文木) で変換するため、ネストされたリスト・テーブル内の装飾・コードブロック内の特殊文字なども安全に処理できる。

## 発動条件

以下のいずれかに該当したら、このスキルを使う。

1. ユーザーが Markdown 文字列を「Backlog 記法に変換して」「Backlog Wiki に貼れる形式にして」と依頼
2. ユーザーが Backlog 記法を「Markdown に戻して」「GitHub に貼れる形にして」と依頼
3. `.md` ファイルから `.backlog` ファイル (またはその逆) を生成する依頼
4. Backlog の課題説明・Wiki ページ用にドキュメントを整形する依頼

## コマンドの使い方

### Markdown → Backlog

```bash
# ファイルから
md2backlog --to-backlog input.md

# stdin から (短いテキスト向け)
echo "# Hello **World**" | md2backlog --to-backlog

# ヒアドキュメントから (複数行テキスト向け)
md2backlog --to-backlog <<'EOF'
# 見出し
- リスト1
- リスト2
EOF
```

### Backlog → Markdown

```bash
md2backlog --to-markdown input.backlog
echo "* Hello ''World''" | md2backlog --to-markdown
```

### ファイル出力

CLI は標準出力に書き出すため、リダイレクトでファイル化する。

```bash
md2backlog --to-backlog input.md > output.backlog
```

## ワークフロー

1. ユーザーから変換対象のテキストまたはファイルを受け取る
2. 変換方向 (`--to-backlog` / `--to-markdown`) を判断する
3. `Bash` ツールで `md2backlog` を実行する
4. 変換結果をユーザーに提示、または指定されたファイルに保存する

## 注意事項

- 変換結果に含まれる `'`、`*`、`[`、`{` などの記号は Backlog 記法の一部なので、勝手に整形・修正しない。
- `md2backlog` がインストールされていない (`command not found`) 場合は、`docs/skill/01-setup.md` のセットアップ手順をユーザーに案内する。
- 一部要素は変換時にロスがある (Backlog はネストされた引用・タスクリスト・リンクタイトル等に未対応)。詳細は `README.md` の「Backlog 記法の制約」を参照。

## 例

### 入力 (Markdown)

```markdown
# 障害報告

**発生日時**: 2026-05-07

## 影響範囲
- 管理画面の `/admin/master/user-log/sent_mail/show`
- ~~廃止済み API~~
```

### コマンド

```bash
md2backlog --to-backlog <<'EOF'
# 障害報告

**発生日時**: 2026-05-07

## 影響範囲
- 管理画面の `/admin/master/user-log/sent_mail/show`
- ~~廃止済み API~~
EOF
```

### 出力 (Backlog 記法)

```
* 障害報告

''発生日時'': 2026-05-07

** 影響範囲
- 管理画面の `/admin/master/user-log/sent_mail/show`
- %%廃止済み API%%
```
````

## 3. スキルが認識されているか確認

Claude Code を起動し、以下のように質問してみます。

```
利用可能なスキルを教えて
```

または、変換を依頼してスキルが自動発動することを確認します。

```
以下を Backlog 記法に変換して

# Hello
- World
```

このとき Claude Code が `md2backlog --to-backlog` を `Bash` ツール経由で実行すれば成功です。

## 4. 発動しない場合のチューニング

スキルの `description` が曖昧だと Claude Code が呼び出さないことがあります。改善ポイント:

- 「いつ発動するか」を具体的なフレーズで列挙する (「Backlog Wiki に貼れる形」「md → backlog」など)
- 競合する別スキル (例: 汎用テキスト整形系) がないか確認する
- 「正規表現での変換ではなく **必ずコマンドを使う**」と明示し、Claude が手動変換に逃げるのを防ぐ

## 5. プロジェクトスコープに配置する場合

特定プロジェクトの中だけで使いたい場合は、以下の場所に配置します。

```
<project-root>/.claude/skills/md2backlog/SKILL.md
```

例えば Backlog にチケット説明を貼ることが多いプロジェクトでは、プロジェクト直下に配置すると、そのリポジトリで作業中のときだけ自動発動します。

## 6. 権限設定 (任意)

毎回 `Bash(md2backlog ...)` の許可ダイアログが出るのを避けたい場合、`~/.claude/settings.json` に以下を追加します。

```json
{
  "permissions": {
    "allow": [
      "Bash(md2backlog --to-backlog *)",
      "Bash(md2backlog --to-markdown *)"
    ]
  }
}
```

## 参考

- スキルの公式ドキュメント: Claude Code の `/help` 内 "Skills" セクション
- `md2backlog` 自体の使い方: プロジェクト直下の [`README.md`](../../README.md)
- 変換アルゴリズムの詳細: [`CLAUDE.md`](../../CLAUDE.md) の「アーキテクチャ上の注意点」
