# 目录说明

## cloud
- 基础业务数据库脚本
- **thinglinks_ds_c_defaults** 系统默认数据库;
- **thinglinks_base_{tenantId}** 系统租户数据库,{tenantId}为租户ID，每个租户为单独数据库;
- 租户的数据库表结构基本一致，如果有新版本更新的 **base** 表结构变更脚本，需要更新到对应的每个租户数据库中。


## job
- thinglinks-job 任务调度相关数据库脚本



