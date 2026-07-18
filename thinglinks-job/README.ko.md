<div align="center">

# thinglinks-job

**ThingLinks IoT — 분산 태스크 스케줄링**

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md) | 한국어

</div>

---

`thinglinks-job`은 [ThingLinks](https://mqttsnet.com) IoT 플랫폼의 분산 태스크 스케줄링 서비스입니다. 제품 정보와 버전은 [`.thinglinks-product.env`](.thinglinks-product.env)에서 통합 관리합니다.

데이터베이스 초기화 스크립트는 [`docs/db/mysql/baseline/thinglinks_job.sql`](docs/db/mysql/baseline/thinglinks_job.sql)에 있습니다. 증분 마이그레이션 주의 사항은 [데이터베이스 스크립트 안내서](docs/db/mysql/README.md)를 참고하십시오.

## 라이선스

[GPL v3](LICENSE). 현재 릴리스의 추가 조건은 제품 매니페스트가 참조하는 라이선스 파일에 정의됩니다.

Copyright &copy; 2019-present [MqttsNet](https://mqttsnet.com)
