<div align="center">

<a href="https://mqttsnet.com"><img src="https://avatars.githubusercontent.com/u/94173946?s=200&v=4" alt="ThingLinks" width="160"></a>

# thinglinks-web-visualize

**ThingLinks IoT プラットフォーム — 可視化ダッシュボード**

[English](README.md) | [简体中文](README.zh-CN.md) | 日本語 | [한국어](README.ko.md)

</div>

---

## 概要

`thinglinks-web-visualize` は [ThingLinks](https://github.com/mqttsnet/thinglinks) マルチテナント SaaS クラウド IoT プラットフォームのデータ可視化・大画面ダッシュボードフロントエンドです。Vue 3 + TypeScript + Vite で構築され、多彩なチャート、3D 可視化、地図表示をサポートします。

製品情報、エディション、コンポーネントバージョンは [`.thinglinks-product.env`](.thinglinks-product.env) で一元管理します。更新と検証の手順は[製品設定クイック操作](docs/产品配置快捷操作.md)を参照してください。

## 技術スタック

![Vue 3](https://img.shields.io/badge/Vue-3.2-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-4.6-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-4.3-646CFF?style=flat-square&logo=vite&logoColor=white)
![Pinia](https://img.shields.io/badge/Pinia-2.0-FFD859?style=flat-square)
![Naive UI](https://img.shields.io/badge/Naive%20UI-2.34-18A058?style=flat-square)
![ECharts](https://img.shields.io/badge/ECharts-5.3-AA344D?style=flat-square&logo=apacheecharts&logoColor=white)
![Three.js](https://img.shields.io/badge/Three.js-0.145-000000?style=flat-square&logo=threedotjs&logoColor=white)

## 特徴

- ECharts ベースの豊富なチャートコンポーネント
- 高徳地図による地図可視化（ヒートマップ、散布図）
- Three.js による 3D シーンレンダリング
- ドラッグ＆ドロップのダッシュボードエディタ
- レスポンシブレイアウト（マルチ解像度対応）
- テーマカスタマイズと国際化（i18n）

## クイックスタート

### 環境要件

- **Node.js** >= 18.0.0
- **pnpm**（インストール：`npm install -g pnpm`）

### インストールと実行

```bash
# 依存関係のインストール
pnpm install

# 開発サーバーの起動
pnpm dev

# 本番ビルド
pnpm build
```

## ライセンス

[Apache License 2.0](LICENSE) の下で公開されています。

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
