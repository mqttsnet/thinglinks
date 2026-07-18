-- 为场景联动菜单注册规则通知模板接口。
-- 已存在的同方法、同 URI 记录会修正到标准资源绑定，脚本可重复执行。
-- 执行后调用 POST /system/defResource/clearCache?applicationId=3 清理资源缓存，
-- 再等待网关定时刷新（最长 60 秒），或重启网关触发刷新。
START TRANSACTION;

INSERT INTO `def_resource_api` (
    `id`, `resource_id`, `controller`, `spring_application_name`,
    `request_method`, `name`, `uri`, `is_input`,
    `created_by`, `created_time`, `updated_by`, `updated_time`
)
SELECT
    790260718000000001, 379966883274686466, 'RuleNotificationController',
    'thinglinks-rule-server', 'GET', '规则通知模板-查询变量',
    '/rule/ruleNotification/variables', b'0',
    1452186486253289472, CURRENT_TIMESTAMP, 1452186486253289472, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM `def_resource_api`
    WHERE `request_method` = 'GET'
      AND `uri` = '/rule/ruleNotification/variables'
);

UPDATE `def_resource_api`
SET `resource_id` = 379966883274686466,
    `controller` = 'RuleNotificationController',
    `spring_application_name` = 'thinglinks-rule-server',
    `name` = '规则通知模板-查询变量',
    `is_input` = b'0',
    `updated_by` = 1452186486253289472,
    `updated_time` = CURRENT_TIMESTAMP
WHERE `request_method` = 'GET'
  AND `uri` = '/rule/ruleNotification/variables';

INSERT INTO `def_resource_api` (
    `id`, `resource_id`, `controller`, `spring_application_name`,
    `request_method`, `name`, `uri`, `is_input`,
    `created_by`, `created_time`, `updated_by`, `updated_time`
)
SELECT
    790260718000000002, 379966883274686466, 'RuleNotificationController',
    'thinglinks-rule-server', 'POST', '规则通知模板-预览',
    '/rule/ruleNotification/preview', b'0',
    1452186486253289472, CURRENT_TIMESTAMP, 1452186486253289472, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM `def_resource_api`
    WHERE `request_method` = 'POST'
      AND `uri` = '/rule/ruleNotification/preview'
);

UPDATE `def_resource_api`
SET `resource_id` = 379966883274686466,
    `controller` = 'RuleNotificationController',
    `spring_application_name` = 'thinglinks-rule-server',
    `name` = '规则通知模板-预览',
    `is_input` = b'0',
    `updated_by` = 1452186486253289472,
    `updated_time` = CURRENT_TIMESTAMP
WHERE `request_method` = 'POST'
  AND `uri` = '/rule/ruleNotification/preview';

COMMIT;
