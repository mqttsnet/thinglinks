<div align="center">

# bifromq-plugin — Community Edition

**ThingLinks IoT — BifroMQ プラグインライブラリ**

[English](README.md) | [简体中文](README.zh-CN.md) | 日本語 | [한국어](README.ko.md)

[![Edition](https://img.shields.io/badge/Edition-Community-blue?style=flat-square)](../LICENSE-COMMERCIAL)

</div>

---

BifroMQ MQTT Broker の [ThingLinks](https://mqttsnet.com) IoT プラットフォーム向けプラグインライブラリ。認証、イベント収集、リソース制御、動的設定の 4 プラグイン。

## 🤖 Agent Skills(AI 支援開発)

本リポジトリには公式 **Agent Skill** が用意されており、AI エージェント(Claude Code · Codex · Cursor)が実コードに基づき必要な分だけ読み込んで理解できます。**[ThingLinks Skills](https://github.com/mqttsnet/thinglinks-skills)** コレクションの一部です(1 リポジトリ = 1 skill)。

```bash
# グローバル(-g);-g を外すと現在のプロジェクトのみ
npx skills add mqttsnet/thinglinks-skills@bifromq-plugin -g
```

BifroMQ の認証/ACL、イベントコレクター(Kafka)、設定/リソース provider、`EventTypeEnum ↔ DeviceActionTypeEnum` マッピング、またはプラグインのデプロイを扱う際に自動的にトリガーされます。すべての skill(`thinglinks-cloud` · `thinglinks-util` · `thinglinks-web` · `bifromq-plugin`)は **[mqttsnet/thinglinks-skills](https://github.com/mqttsnet/thinglinks-skills)** を参照。

## ライセンス

[Apache License 2.0](../LICENSE) + [LICENSE-COMMERCIAL](../LICENSE-COMMERCIAL)

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
