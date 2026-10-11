# CLAUDE.md

Claude Code (claude.ai/code) がこのリポジトリで作業する際のガイドです。

## プロジェクト概要

Minecraft Fabric MOD（1.21.11）。100ブロックのワールドボーダー内で地下の暗い空気ブロックをどれだけ照らせるかを競うミニゲーム。Java 21で記述。

## ビルド・実行

```bash
./gradlew build          # ビルド → build/libs/lightgame100-*.jar
./gradlew vscode         # VSCode プロジェクトファイル生成
```

デバッグ: VSCode の「実行とデバッグ」→「Minecraft Client」

テストスイートはなし。

## アーキテクチャ

`src/main/java/com/stakiran/lightgame/` に6クラス:

- **LightGameMod** — エントリポイント（`onInitialize`）。コマンドとティックイベントを登録。
- **LightGameCommand** — `/lightgame` 単一コマンド。GameManager.setup に委譲（ゲーム中・終了後でも呼べ、リセットを兼ねる）。
- **GameManager** — コアゲームロジック。フェーズ状態機械（IDLE → PREVIEW → GAME → ENDED）、ワールドボーダー設定、空気ブロックのスナップショット（y < 64、ブロック光レベル0のもの）、光レベルによるスコア計算、ティックベースのタイマー（30秒プレビュー、10分ゲーム）、ビーコン設置を管理。全状態はstaticフィールド。
- **KitManager** — スターターアイテムキットの管理。ネザライト装備・松明・バケツ・エリトラ等をエンチャント済みシュルカーボックスとして提供。
- **ScoreboardManager** — ゲームフェーズ中のサイドバースコアボード表示。照らしたブロック数、合計、割合を表示（2秒ごと更新）。
- **TimerBar** — 画面上部のボスバーに残り時間を表示。プレビュー中は青、本番は緑→残り3分で黄→残り1分で赤。毎ティック更新。

ゲームフロー: `/lightgame` でワールドボーダーと中心位置を設定し、全プレイヤーの持ち物をクリアしてキットを支給 → 30秒のスペクテイターモードプレビュー → プレビュー終了時にy=64未満のブロック光レベル0の空気ブロックをスナップショット → 10分のサバイバルゲームフェーズ → タイムアウトまたは死亡で最終スコア表示。手動停止コマンドはなく、`/lightgame` の再実行がリセットになる。

スコア = スナップショットした空気ブロックのうちブロック光レベル ≥ 1 になったものの数。

## 重要事項

- 全ゲーム状態はGameManagerのstaticフィールド。シングルゲームインスタンスのみ。
- バージョン定義は `gradle.properties`（Minecraft、Yarnマッピング、Fabric loader/API）。
- MODメタデータは `src/main/resources/fabric.mod.json`。
- UIメッセージは日本語。

## 禁止事項

- **`memo/` ディレクトリは読まないこと。** ユーザーから明示的に指示された場合のみ参照可。
