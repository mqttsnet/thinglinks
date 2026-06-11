# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.4.0] - 2026-06-11

### Added
- Event processors reorganized by BifroMQ package structure (`mqttbroker/clientconnected`, `clientdisconnect`, `channelclosed`, `disthandling`, `subhandling`, `session`), with 18 client-disconnect sub-type processors fully covered
- HLC/UTC causal-clock semantics separation for device events, plus Kafka partition key by client for per-device ordering
- `maxTopicFiltersPerSub` / `maxTopicFiltersPerInbox` setting-provider parameters

### Changed
- Auth HTTP channel rebuilt on truly-async OkHttp with per-host concurrency unlocked and smart retry interceptor
- ACL verdict caching switched to Caffeine `AsyncCache` to prevent cache breakdown under burst
- Event TaskQueue workers 8 -> 64 and Kafka producer defaults tuned for high-throughput brokers
- ACL topic-pattern matching encapsulated (replaces `AclMatcherUtil`)
- `kafka-clients` upgraded 3.9.0 -> 4.2.0
- `thinglinks-util` dependency aligned to 1.0.8.1

### Fixed
- BifroMQ documentation links updated to bifromq.apache.org

## [1.3.0] - 2026-03-23

### Added
- Multi-language README (English, Chinese, Japanese, Korean)
- Dual licensing: Apache 2.0 (LICENSE) + Commercial Terms (LICENSE-COMMERCIAL)
- GitHub Issue/PR templates, Security policy, Support guide
- Root-level files (.editorconfig, .gitattributes, CODE_OF_CONDUCT, CONTRIBUTING, SUPPORT)

### Changed
- Plugin modules version unified to 1.3.0 (aligned with the ThingLinks community release line)
- LICENSE updated from custom agreement to Apache 2.0
- pom.xml: added licenses section, URL standardized to https://mqttsnet.com

---

*For earlier versions, see [git log](https://github.com/mqttsnet/thinglinks/commits/main).*
