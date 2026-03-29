# CLAUDE.md

Claude Code (claude.ai/code) がこのリポジトリで作業する際のガイドです。

## プロジェクト概要

Minecraft Fabric MOD（1.21.4）。100ブロックのワールドボーダー内で地下の暗い空気ブロックをどれだけ照らせるかを競うミニゲーム。Java 21で記述。

## ビルド・実行

```bash
./gradlew build          # ビルド → build/libs/lightgame100-*.jar
./gradlew vscode         # VSCode プロジェクトファイル生成
```

デバッグ: VSCode の「実行とデバッグ」→「Minecraft Client」

テストスイートはなし。

## アーキテクチャ

`src/main/java/com/stakiran/lightgame/` に5クラス:

- **LightGameMod** — エントリポイント（`onInitialize`）。コマンドとティックイベントを登録。
- **LightGameCommand** — `/lightgame {setup,start,stop,score,kit}` のBrigadierコマンドツリー。ゲーム状態のバリデーションを行い、GameManagerに委譲。
- **GameManager** — コアゲームロジック。フェーズ状態機械（IDLE → PREVIEW → GAME → ENDED）、ワールドボーダー設定、空気ブロックのスナップショット（y < 64、ブロック光レベル0のもの）、光レベルによるスコア計算、ティックベースのタイマー（30秒プレビュー、10分ゲーム）、ビーコン設置を管理。全状態はstaticフィールド。
- **KitManager** — スターターアイテムキットの管理。ネザライト装備・松明・バケツ・エリトラ等をエンチャント済みシュルカーボックスとして提供。
- **ScoreboardManager** — ゲームフェーズ中のサイドバースコアボード表示。残り時間、照らしたブロック数、合計、割合を表示。

ゲームフロー: `setup`でワールドボーダーと中心位置を設定 → `start`でy=64以下のブロック光レベル0の空気ブロックをスナップショットし、30秒のスペクテイターモードプレビュー → 10分のサバイバルゲームフェーズ → `stop`（手動またはタイムアウト）で最終スコア表示。

スコア = スナップショットした空気ブロックのうちブロック光レベル ≥ 1 になったものの数。

## 重要事項

- 全ゲーム状態はGameManagerのstaticフィールド。シングルゲームインスタンスのみ。
- バージョン定義は `gradle.properties`（Minecraft、Yarnマッピング、Fabric loader/API）。
- MODメタデータは `src/main/resources/fabric.mod.json`。
- UIメッセージは日本語。

## 禁止事項

- **`memo/` ディレクトリは読まないこと。** ユーザーから明示的に指示された場合のみ参照可。
