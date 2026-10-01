# Claude Code 自動レビュー

PRの作成時、およびPRブランチへのpush時に、GitHub Actions(`.github/workflows/claude-code-review.yml`)が
Claude Codeによるレビューをコメントとして投稿します。

- 対象外: forkからのPR、Draft PR
- 認証: リポジトリSecret `ANTHROPIC_API_KEY`
- レビュー結果は同一コメントが更新されます
