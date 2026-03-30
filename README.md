# Light Game 100

100ブロックのワールドボーダー内で、地下の空気ブロックをどれだけ照らせるかを競うミニゲームMOD（Fabric 1.21.4）

解説ウェブサイトはこちら: <https://stakiran.github.io/lightgame100/>

## 導入方法

### 前提

- Minecraft Java Edition 1.21.4
- Fabric Loader 0.16.0 以上
- Fabric API 0.110.5+1.21.4

### インストール

1. [Fabric Loader](https://fabricmc.net/use/installer/) をインストール
2. [Fabric API](https://modrinth.com/mod/fabric-api) を `mods` フォルダに配置
3. `lightgame100-x.x.x.jar` を `mods` フォルダに配置
    - [Releasesページからもダウンロードできます](https://github.com/stakiran/lightgame100/releases)
4. Minecraft 1.21.4（Fabric）で起動

## 遊び方

### コマンド一覧

| コマンド | 説明 |
|---|---|
| `/lightgame setup` | 現在地を中心にワールドボーダー(100ブロック)を設定 |
| `/lightgame kit` | ゲーム用アイテム一式(シュルカーボックス)を取得 |
| `/lightgame start` | ゲームを開始 |
| `/lightgame stop` | ゲームを手動停止 |
| `/lightgame score` | 現在のスコアを表示 |

### ゲームの流れ

1. **セットアップ** — `/lightgame setup` でワールドボーダーを設定
2. **アイテム準備** — `/lightgame kit` でアイテムを取得（または自分で用意）
3. **ゲーム開始** — `/lightgame start` で開始
4. **プレビュー（30秒）** — スペクテイターモードで地下を確認
5. **ゲーム本番（10分）** — サバイバルモードで地下に松明等を設置して照らす
6. **終了** — 時間切れ、手動停止、またはプレイヤー死亡でゲーム終了

### スコアについて

- 対象: ワールドボーダー内、y=64未満の全空気ブロック
- スコア = プレイヤーが照らしたブロック数（自然光源分はベースラインとして差し引き）
- ゲーム開始時に存在していた空気ブロックのみが対象。掘って新しくできた空間はカウントされない
- サイドバーにリアルタイム表示（2秒ごと更新）

## 開発者向け

### Requirements

- Java 21（Eclipse Temurin 推奨）
- Gradle（Wrapper 同梱）
- VSCode + Java 関連拡張機能

### デバッグ

```bash
./gradlew vscode    # VSCode プロジェクトファイル生成
```

VSCode の「Run and Debug」から **Minecraft Client** を選択して実行。

### ビルド

```bash
./gradlew build
```

`build/libs/lightgame100-1.0.0.jar` が生成される。

### 配布

1. `./gradlew build` を実行
2. `build/libs/lightgame100-1.0.0.jar` を配布（`-sources.jar` は不要）
3. バージョン変更は `gradle.properties` の `mod_version` を編集

## ライセンス

MIT
