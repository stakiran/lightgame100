---
name: bump-version
description: バージョン番号を更新し、CHANGELOGにエントリを追加する
user_invocable: true
---

# バージョン更新スキル

以下の手順を実行してください。

## 1. 現在のバージョンを表示

`gradle.properties` の `mod_version` を読み取り、現在のバージョンをユーザーに表示してください。

## 2. 新しいバージョンを聞く

AskUserQuestion ツールを使って、更新後のバージョン番号をユーザーに尋ねてください。

## 3. gradle.properties を更新

`gradle.properties` 内の `mod_version=<旧バージョン>` を `mod_version=<新バージョン>` に書き換えてください。

## 4. CHANGELOG.md を更新

`CHANGELOG.md` を読み取り、**先頭に** 新しいエントリを追加してください（prepend）。フォーマット:

```
# v<新バージョン> <今日の日付 YYYY-MM-DD>
-

```

`-` の後ろは空欄にしてください（ユーザーが後で記入します）。既存の内容はその下にそのまま残してください。

## 5. 結果を表示

変更した2ファイルの差分を表示してください。
