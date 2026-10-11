# Light Game 100
[![松明マイクラデモ動画](https://img.youtube.com/vi/kamlpgmELvw/hqdefault.jpg)](https://www.youtube.com/watch?v=kamlpgmELvw)

100ブロックのワールドボーダー内で、地下の空気ブロックをどれだけ照らせるかを競うミニゲームMOD（Fabric 1.21.11）

解説ウェブサイトはこちら: <https://stakiran.github.io/lightgame100/>

## 導入方法

### 前提

- Minecraft Java Edition 1.21.11
- Fabric Loader 0.19.0 以上
- Fabric API 0.141.6+1.21.11

### インストール

1. [Fabric Loader](https://fabricmc.net/use/installer/) をインストール
2. [Fabric API](https://modrinth.com/mod/fabric-api) を `mods` フォルダに配置
3. `lightgame100-x.x.x.jar` を `mods` フォルダに配置
    - [Releasesページからもダウンロードできます](https://github.com/stakiran/lightgame100/releases)
4. Minecraft 1.21.11（Fabric）で起動

## 遊び方

### コマンド

`/lightgame` の一つだけです。実行すると以下をまとめて行います:

1. 現在地を中心にワールドボーダー(100ブロック)を設定
2. 持ち物をクリアし、ゲーム用アイテム一式(シュルカーボックス)を支給
3. プレビューフェーズを開始

ゲーム中や終了後にもう一度 `/lightgame` を実行すると、最初からやり直せます。

### ゲームの流れ

1. **開始** — `/lightgame` を実行
2. **プレビュー（30秒）** — スペクテイターモードで地下を確認
3. **ゲーム本番（10分）** — サバイバルモードで地下に松明等を設置して照らす
4. **終了** — 時間切れ、またはプレイヤー死亡でゲーム終了

### スコアについて

- 対象: ワールドボーダー内、y=64未満の全空気ブロック
- スコア = プレイヤーが照らしたブロック数（自然光源分はベースラインとして差し引き）
- ゲーム開始時に存在していた空気ブロックのみが対象。掘って新しくできた空間はカウントされない
- スコアは画面右のサイドバーに表示（2秒ごと更新）
- 残り時間は画面上部のボスバー（ゲージ）に表示。残り3分で黄色、残り1分で赤に変わる

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

`build/libs/lightgame100-x.x.x.jar` が生成される（`x.x.x` は `gradle.properties` の `mod_version`）。

### 配布

1. `./gradlew build` を実行
2. `build/libs/lightgame100-x.x.x.jar` を配布（`-sources.jar` は不要）
3. バージョン変更は `gradle.properties` の `mod_version` を編集

## ライセンス

MIT
