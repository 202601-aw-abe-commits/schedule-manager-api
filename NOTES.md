# Working Notes

Use this file for ongoing task memos.

## Entry Template
- Date/Time (JST):
- Task:
- Decision:
- Files changed:
- Verify:

- Date/Time (JST): 2026-05-11 14:39 JST
- Task: 毎回データが初期化される問題の修正
- Decision: `spring.sql.init.mode` を `always` から環境変数対応の `embedded` 既定へ変更。DB URLも環境変数化し、Renderで永続ディスク/外部DBに切替可能にした。
- Files changed: `src/main/resources/application.properties`
- Verify: `./mvnw -q -DskipTests compile` 成功
