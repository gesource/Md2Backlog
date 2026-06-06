# 01. md2backlog CLI のセットアップ

`md2backlog` ネイティブ CLI をビルドし、`~/bin/` に配置して、ターミナルから `md2backlog` コマンドとして実行できるようにする手順です。

## 1. ビルド要件

| ツール | バージョン | 確認コマンド |
|--------|-----------|-------------|
| JDK | 17 以上 | `java -version` |
| Xcode Command Line Tools | 最新 (macOS のみ) | `xcode-select -p` |

JDK が入っていない場合:

```bash
brew install --cask temurin@17
```

## 2. リポジトリのクローン

```bash
git clone https://github.com/<your-org>/Md2Backlog.git
cd Md2Backlog
```

## 3. CLI バイナリをビルド

自分のアーキテクチャに合わせてターゲットを選びます。

### macOS Apple Silicon (M1/M2/M3/M4)

```bash
./gradlew :cli:linkReleaseExecutableMacosArm64
```

成果物: `cli/build/bin/macosArm64/releaseExecutable/md2backlog.kexe`

### macOS Intel

```bash
./gradlew :cli:linkReleaseExecutableMacosX64
```

成果物: `cli/build/bin/macosX64/releaseExecutable/md2backlog.kexe`

### Windows

```powershell
.\gradlew :cli:linkReleaseExecutableMingwX64
```

成果物: `cli\build\bin\mingwX64\releaseExecutable\md2backlog.exe`

> **アーキテクチャ確認方法 (macOS)**: `uname -m`
> - `arm64` → Apple Silicon (`MacosArm64`)
> - `x86_64` → Intel (`MacosX64`)

## 4. ~/bin/ に配置する

### macOS / Linux

```bash
# ~/bin が存在しない場合は作成
mkdir -p ~/bin

# ビルド成果物をコピー (拡張子 .kexe を外して md2backlog にリネーム)
cp cli/build/bin/macosArm64/releaseExecutable/md2backlog.kexe ~/bin/md2backlog

# 実行権限を付与
chmod +x ~/bin/md2backlog
```

### Windows (PowerShell)

```powershell
# ~/bin に相当するフォルダを作成
New-Item -ItemType Directory -Force -Path "$HOME\bin"

# 配置
Copy-Item "cli\build\bin\mingwX64\releaseExecutable\md2backlog.exe" "$HOME\bin\md2backlog.exe"
```

## 5. PATH を通す

`~/bin` が PATH に含まれていない場合は、シェルの設定ファイルに追記します。

### zsh (macOS デフォルト)

```bash
echo 'export PATH="$HOME/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### bash

```bash
echo 'export PATH="$HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

### Windows (PowerShell)

```powershell
[Environment]::SetEnvironmentVariable(
  "Path",
  [Environment]::GetEnvironmentVariable("Path", "User") + ";$HOME\bin",
  "User"
)
```

設定後、PowerShell を再起動してください。

## 6. 動作確認

```bash
# どこから実行できるか
which md2backlog
# → /Users/<you>/bin/md2backlog

# ヘルプ表示
md2backlog --help

# Markdown → Backlog 変換 (stdin)
echo "# Hello **World**" | md2backlog --to-backlog
# → * Hello ''World''

# Backlog → Markdown 変換 (file)
md2backlog --to-markdown sample.backlog
```

## 7. アップデート手順

リポジトリを更新したら、以下を再実行するだけです。

```bash
cd Md2Backlog
git pull
./gradlew :cli:linkReleaseExecutableMacosArm64
cp cli/build/bin/macosArm64/releaseExecutable/md2backlog.kexe ~/bin/md2backlog
```

## トラブルシューティング

### `command not found: md2backlog`

PATH が通っていません。`echo $PATH` に `~/bin` (または絶対パス) が含まれているか確認してください。

### `cannot execute binary file`

アーキテクチャが合っていません。`uname -m` の結果に合わせて再ビルドします。

### 「開発元を確認できないため開けません」(macOS)

未署名バイナリのため Gatekeeper に弾かれます。一度だけ以下を実行してください。

```bash
xattr -d com.apple.quarantine ~/bin/md2backlog
```

## 次のステップ

CLI が動くようになったら、[02-create-skill.md](./02-create-skill.md) で Claude Code スキルを作成します。
