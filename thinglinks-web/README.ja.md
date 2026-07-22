<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="160"></a>

# thinglinks-web

**ThingLinks IoT — 管理コンソール**

[English](README.md) | [简体中文](README.zh-CN.md) | 日本語 | [한국어](README.ko.md)

</div>

---

## 概要

`thinglinks-web` は [ThingLinks](https://mqttsnet.com) マルチテナント SaaS クラウド IoT プラットフォームの管理コンソールです。

製品エディションとコンポーネントバージョンのメタデータは、ルートの [`.thinglinks-product.env`](.thinglinks-product.env) にあります。

## 🤖 Agent Skills(AI 支援開発)

本リポジトリには公式 **Agent Skill** が用意されており、AI エージェント(Claude Code · Codex · Cursor)が実コードに基づき必要な分だけ読み込んで理解できます。**[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** コレクションの一部です(1 リポジトリ = 1 skill)。

```bash
# グローバル(-g);-g を外すと現在のプロジェクトのみ
npx skills add mqttsnet/thinglinks-skills@thinglinks-web -g
```

IoT ページ、API 呼び出し、共有コンポーネント / ルールスクリプトデバッグパネル、またはファイル配置と **Flexy** デザイン規約を扱う際に自動的にトリガーされます。すべての skill(`thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin`)は **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)** を参照。

## ライセンス

[LICENSE](LICENSE) と製品メタデータで参照される追加条件を確認してください。

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
