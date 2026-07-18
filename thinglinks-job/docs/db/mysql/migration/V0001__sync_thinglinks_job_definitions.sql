-- ThingLinks Job 任务定义同步脚本
-- 适用范围：已经使用旧版 thinglinks_job 基线创建的数据库
-- 执行效果：补齐新增任务、修正已知默认配置，并保留已有任务的自定义启停状态与调度表达式

START TRANSACTION;

-- IoT 执行器不存在时创建为自动注册模式。
INSERT INTO `xxl_job_group` (`app_name`, `title`, `address_type`, `address_list`, `update_time`)
SELECT 'thinglinks-iot-executor', '物联网业务系统执行器', 0, NULL, CURRENT_TIMESTAMP
FROM (SELECT 1 AS `seed`) desired
LEFT JOIN `xxl_job_group` existing
  ON existing.`app_name` = 'thinglinks-iot-executor'
WHERE existing.`id` IS NULL;

-- 仅清理旧基线中的本机固定地址，不覆盖运维人员已经设置的其他地址。
UPDATE `xxl_job_group`
SET `address_type` = 0,
    `address_list` = NULL,
    `update_time` = CURRENT_TIMESTAMP
WHERE `app_name` = 'thinglinks-iot-executor'
  AND `address_type` = 1
  AND `address_list` = 'http://127.0.0.1:18911/';

SET @thinglinks_iot_job_group_id = (
  SELECT MIN(`id`)
  FROM `xxl_job_group`
  WHERE `app_name` = 'thinglinks-iot-executor'
);

-- 临时表集中保存需要补齐的任务定义；新增任务统一保持停止状态。
DROP TEMPORARY TABLE IF EXISTS `tmp_thinglinks_job_definitions`;
CREATE TEMPORARY TABLE `tmp_thinglinks_job_definitions` (
  `job_desc` varchar(255) NOT NULL,
  `schedule_conf` varchar(128) NOT NULL,
  `executor_route_strategy` varchar(50) NOT NULL,
  `executor_handler` varchar(255) NOT NULL,
  PRIMARY KEY (`executor_handler`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `tmp_thinglinks_job_definitions`
  (`job_desc`, `schedule_conf`, `executor_route_strategy`, `executor_handler`)
VALUES
  ('全租户产品版本发布重试兜底调度任务', '0 0/5 * * * ?', 'FIRST', 'flushProductVersionPublishRetryJobHandler'),
  ('ZLM流媒体心跳检测', '0 0/1 * * * ?', 'FIRST', 'zlmMediaServerHeartbeatJobHandler'),
  ('ABL流媒体心跳检测', '0 0/1 * * * ?', 'FIRST', 'ablMediaServerHeartbeatJobHandler'),
  ('录像计划调度', '0/30 * * * * ?', 'FIRST', 'executeRecordPlanScheduleJobHandler'),
  ('过期录像文件清理', '0 0 2 * * ?', 'FIRST', 'cleanExpiredRecordFilesJobHandler'),
  ('流媒体服务器缓存刷新', '0 0/5 * * * ?', 'FIRST', 'refreshMediaServerCacheJobHandler'),
  ('刷新全租户SIP配置缓存', '0 0/10 * * * ?', 'FIRST', 'refreshSipTenantConfigCacheJobHandler'),
  ('设备心跳超时检测', '*/30 * * * * ?', 'FIRST', 'deviceKeepaliveTimeoutCheckJobHandler'),
  ('SRC池孤儿对账', '0 0/1 * * * ?', 'FIRST', 'ssrcPoolReconcileJobHandler');

INSERT INTO `xxl_job_info` (
  `job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`,
  `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`,
  `executor_handler`, `executor_param`, `executor_block_strategy`, `executor_timeout`,
  `executor_fail_retry_count`, `glue_type`, `glue_source`, `glue_remark`,
  `glue_updatetime`, `child_jobid`, `trigger_status`, `trigger_last_time`, `trigger_next_time`
)
SELECT
  @thinglinks_iot_job_group_id,
  definition.`job_desc`,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  'mqttsnet',
  '',
  'CRON',
  definition.`schedule_conf`,
  'DO_NOTHING',
  definition.`executor_route_strategy`,
  definition.`executor_handler`,
  '',
  'SERIAL_EXECUTION',
  0,
  0,
  'BEAN',
  '',
  'GLUE代码初始化',
  CURRENT_TIMESTAMP,
  '',
  0,
  0,
  0
FROM `tmp_thinglinks_job_definitions` definition
LEFT JOIN `xxl_job_info` existing
  ON existing.`executor_handler` = definition.`executor_handler`
WHERE @thinglinks_iot_job_group_id IS NOT NULL
  AND existing.`id` IS NULL
ORDER BY definition.`executor_handler`;

-- 仅修正旧定义中会每秒触发的 ZLM 表达式，不覆盖其他自定义表达式。
UPDATE `xxl_job_info`
SET `schedule_conf` = '0 0/1 * * * ?',
    `update_time` = CURRENT_TIMESTAMP
WHERE `executor_handler` = 'zlmMediaServerHeartbeatJobHandler'
  AND `schedule_type` = 'CRON'
  AND `schedule_conf` = '* 0/1 * * * ?';

-- 已删除的 ACL 任务和示例任务只停止调度，保留历史配置与执行记录。
UPDATE `xxl_job_info`
SET `trigger_status` = 0,
    `trigger_next_time` = 0,
    `update_time` = CURRENT_TIMESTAMP
WHERE `executor_handler` IN ('flushAllTenantDeviceAclRuleCacheJobHandler', 'demoJobHandler')
  AND (`trigger_status` <> 0 OR `trigger_next_time` <> 0);

DROP TEMPORARY TABLE IF EXISTS `tmp_thinglinks_job_definitions`;

COMMIT;

-- 执行后核对执行器注册方式和任务定义；新增任务应保持停止状态。
SELECT `id`, `app_name`, `address_type`, `address_list`
FROM `xxl_job_group`
WHERE `app_name` = 'thinglinks-iot-executor';

SELECT `id`, `executor_handler`, `schedule_conf`, `trigger_status`
FROM `xxl_job_info`
WHERE `executor_handler` IN (
  'flushProductVersionPublishRetryJobHandler',
  'zlmMediaServerHeartbeatJobHandler',
  'ablMediaServerHeartbeatJobHandler',
  'executeRecordPlanScheduleJobHandler',
  'cleanExpiredRecordFilesJobHandler',
  'refreshMediaServerCacheJobHandler',
  'refreshSipTenantConfigCacheJobHandler',
  'deviceKeepaliveTimeoutCheckJobHandler',
  'ssrcPoolReconcileJobHandler',
  'flushAllTenantDeviceAclRuleCacheJobHandler',
  'demoJobHandler'
)
ORDER BY `executor_handler`, `id`;

-- 返回 1 表示仍在使用旧基线管理员口令，应立即登录后台修改。
SELECT COUNT(*) AS `legacy_default_admin_password_count`
FROM `xxl_job_user`
WHERE `username` = 'admin'
  AND `password` = 'df56d07f329ae243a7c30d58e21ac5b3';
