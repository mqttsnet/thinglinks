<div align="center">

<a href="https://mqttsnet.com"><img src="./docs/images/logo.png" alt="ThingLinks" width="200"></a>

# ThingLinks Cloud

**マルチテナント SaaS クラウド IoT プラットフォーム — 高性能 · 高スループット · 高拡張性**

[English](README.md) | [简体中文](README.zh-CN.md) | 日本語 | [한국어](README.ko.md)

[![JDK](https://img.shields.io/badge/JDK-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025-green?style=flat-square&logo=spring)](https://spring.io/projects/spring-cloud)
<br>

[![Website](https://img.shields.io/badge/公式サイト-mqttsnet.com-blue?style=for-the-badge)](https://mqttsnet.com)
[![Docs](https://img.shields.io/badge/ドキュメント-オンライン-green?style=for-the-badge)](https://mqttsnet.yuque.com/trgbro/thinglinks)

</div>

---

## 概要

ThingLinks Cloud は [ThingLinks](https://mqttsnet.com) マルチテナント SaaS クラウド IoT プラットフォームのバックエンドです。Spring Cloud マイクロサービスで構築され、**高性能・高スループット・高拡張性**のデバイス接続を提供します。

製品エディションとコンポーネントバージョンのメタデータは、ルートの [`.thinglinks-product.env`](.thinglinks-product.env) に記載されています。

## 🤖 Agent Skills(AI 支援開発)

本リポジトリには公式 **Agent Skill** が用意されており、AI エージェント(Claude Code · Codex · Cursor)が実コードに基づき必要な分だけ読み込んで理解できます。**[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** コレクションの一部です(1 リポジトリ = 1 skill)。

`-g` を付けるとグローバルにインストールされ、外すと現在のプロジェクトだけにインストールされます。

```bash
npx skills add mqttsnet/thinglinks-skills@thinglinks-cloud -g
```

ルールスクリプト、プロトコル封筒、上り/下り、モノモデル、ACL、または `broker` / `mqs` / `rule` / `link` モジュールを扱う際に自動的にトリガーされます。すべての skill(`thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin`)は **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)** を参照。

## お問い合わせ

- ビジネス：[mqttsnet@163.com](mailto:mqttsnet@163.com)
- Issues：[GitHub Issues](https://github.com/mqttsnet/thinglinks/issues)

> **注意：** Issue は [GitHub Issues](https://github.com/mqttsnet/thinglinks/issues) でのみ受け付けます。

## ライセンス

リポジトリルートの [LICENSE](../LICENSE) を確認してください。

---

<div align="center">

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com). All rights reserved.

</div>
