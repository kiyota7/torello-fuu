# タスクボード フロントエンド

React(Vite, JavaScript)製のフロントエンド。詳細は[ルートのREADME](../README.md)と
[ローカル開発環境セットアップ](../docs/ローカル開発環境セットアップ.md)を参照。

## 起動方法

```bash
npm install
npm run dev
```

`http://localhost:5173` でボード画面を確認できる。`vite.config.js`で`/api`宛のリクエストを
バックエンド(`http://localhost:8080`)へプロキシしているため、バックエンドを別途起動しておくこと。
