# 产品配置迁移记录

## 代码生成器 Web 配置键

代码生成器中带发行版本含义的旧键 `webProConfig` 已迁移为描述技术栈的稳定键 `webVbenConfig`。

如果部署环境在 YAML 中显式覆盖了该配置，请同步修改键名；未显式配置时继续使用代码默认值，无需额外操作。

## 注册中心与事务命名空间

源码不再保存任何环境的 Nacos 或 Seata 命名空间 UUID。开发、生产、测试以及 Docker 部署都必须在各自的环境变量或密钥管理系统中显式配置以下变量：

- `NACOS_NAMESPACE`：当前环境使用的 Nacos 命名空间。
- `SEATA_NAMESPACE`：当前环境使用的 Seata 命名空间，应与该环境的 Seata 注册配置一致。

`dev`、`prod`、`test`、`docker` 四套 Maven 资源过滤配置继续保留空值，运行时由环境变量覆盖。执行 Docker Compose 前必须同时提供两个变量；历史私有 UUID 不再作为默认值回退。

```bash
# 示例值仅用于说明变量名称，请替换为当前部署环境的真实命名空间
export NACOS_NAMESPACE="NACOS_NAMESPACE_PLACEHOLDER"
export SEATA_NAMESPACE="SEATA_NAMESPACE_PLACEHOLDER"
docker compose -f docker/docker-compose.yml config
```
