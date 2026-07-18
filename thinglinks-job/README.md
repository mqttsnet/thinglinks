<div align="center">

<a href="https://mqttsnet.com"><img src="https://avatars.githubusercontent.com/u/94173946?s=200&v=4" alt="ThingLinks" width="160"></a>

# thinglinks-job

**ThingLinks IoT Platform — Distributed Task Scheduling**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | [한국어](README.ko.md)

[![JDK](https://img.shields.io/badge/JDK-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)

</div>

---

## About

`thinglinks-job` is the distributed task scheduling service for the [ThingLinks](https://mqttsnet.com) IoT platform. It is based on XXL-Job and includes adaptations for IoT workloads.

Product identity, component version, dependency versions and release metadata are maintained in [`.thinglinks-product.env`](.thinglinks-product.env).

## Build Order

Install the ThingLinks Util version declared by `.thinglinks-product.env` from its standalone repository into the local Maven repository first. Then run these commands from any directory inside the `thinglinks` monorepo:

```bash
MONOREPO_ROOT="$(git rev-parse --show-toplevel)"
mvn --batch-mode -f "$MONOREPO_ROOT/thinglinks-cloud/pom.xml" -DskipTests install
mvn --batch-mode -f "$MONOREPO_ROOT/thinglinks-job/pom.xml" -DskipTests package
```

The database initialization script is located at [`docs/db/mysql/baseline/thinglinks_job.sql`](docs/db/mysql/baseline/thinglinks_job.sql). Incremental migration notes are documented in the [database script guide](docs/db/mysql/README.md).

## Product Configuration

See [Product Configuration Quick Operations](docs/产品配置快捷操作.md).

## License

This project is based on [XXL-Job](https://github.com/xuxueli/xxl-job) and retains the original [GPL v3 License](LICENSE). Release-specific terms are declared by the product manifest and its referenced license file.

---

<div align="center">

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com). All rights reserved.

</div>
