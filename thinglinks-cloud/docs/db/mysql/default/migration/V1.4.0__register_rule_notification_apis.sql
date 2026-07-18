-- 版本 1.4.0：为场景联动菜单注册规则通知模板接口。
-- 前置条件：在 thinglinks_ds_c_defaults 数据库中执行，并先完成备份。
-- 同方法、同 URI 已存在时仅同步接口元数据并保留原主键；重复端点或主键占用会中止迁移。
-- 执行后调用 POST /system/defResource/clearCache?applicationId=3 清理资源缓存，
-- 再等待网关定时刷新（最长 60 秒），或重启网关触发刷新。

DROP TEMPORARY TABLE IF EXISTS `_tl_140_rule_api_stage`;
DROP TEMPORARY TABLE IF EXISTS `_tl_140_rule_api_gate`;

CREATE TEMPORARY TABLE `_tl_140_rule_api_stage` LIKE `def_resource_api`;
CREATE TEMPORARY TABLE `_tl_140_rule_api_gate` (
  `id` TINYINT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB;

INSERT INTO `_tl_140_rule_api_stage` (
    `id`, `resource_id`, `controller`, `spring_application_name`,
    `request_method`, `name`, `uri`, `is_input`,
    `created_by`, `created_time`, `updated_by`, `updated_time`
) VALUES
  (
    790260718000000001, 379966883274686466, 'RuleNotificationController',
    'thinglinks-rule-server', 'GET', '规则通知模板-查询变量',
    '/rule/ruleNotification/variables', b'0',
    1452186486253289472, CURRENT_TIMESTAMP, 1452186486253289472, CURRENT_TIMESTAMP
  ),
  (
    790260718000000002, 379966883274686466, 'RuleNotificationController',
    'thinglinks-rule-server', 'POST', '规则通知模板-预览',
    '/rule/ruleNotification/preview', b'0',
    1452186486253289472, CURRENT_TIMESTAMP, 1452186486253289472, CURRENT_TIMESTAMP
  );

INSERT INTO `_tl_140_rule_api_gate` (`id`) VALUES (1);

START TRANSACTION;

-- 失败门禁：目标资源必须存在。
INSERT INTO `_tl_140_rule_api_gate` (`id`)
SELECT 1
WHERE NOT EXISTS (
  SELECT 1
  FROM `def_resource`
  WHERE `id` = 379966883274686466
);

-- 失败门禁：预留主键不得被其他接口占用。
INSERT INTO `_tl_140_rule_api_gate` (`id`)
SELECT 1
FROM `def_resource_api` AS `current`
INNER JOIN `_tl_140_rule_api_stage` AS `incoming`
  ON `incoming`.`id` = `current`.`id`
WHERE `current`.`request_method` <> `incoming`.`request_method`
   OR `current`.`uri` <> `incoming`.`uri`
LIMIT 1;

-- 失败门禁：同方法、同 URI 不得已有多条记录。
INSERT INTO `_tl_140_rule_api_gate` (`id`)
SELECT 1
FROM `_tl_140_rule_api_stage` AS `incoming`
INNER JOIN `def_resource_api` AS `current`
  ON `current`.`request_method` = `incoming`.`request_method`
 AND `current`.`uri` = `incoming`.`uri`
GROUP BY `incoming`.`id`
HAVING COUNT(*) > 1
LIMIT 1;

-- 已有端点只同步受管元数据，保留原主键和创建信息。
UPDATE `def_resource_api` AS `current`
INNER JOIN `_tl_140_rule_api_stage` AS `incoming`
  ON `current`.`request_method` = `incoming`.`request_method`
 AND `current`.`uri` = `incoming`.`uri`
SET `current`.`resource_id` = `incoming`.`resource_id`,
    `current`.`controller` = `incoming`.`controller`,
    `current`.`spring_application_name` = `incoming`.`spring_application_name`,
    `current`.`name` = `incoming`.`name`,
    `current`.`is_input` = `incoming`.`is_input`,
    `current`.`updated_by` = `incoming`.`updated_by`,
    `current`.`updated_time` = CURRENT_TIMESTAMP;

-- 不存在的端点使用预留主键新增。
INSERT INTO `def_resource_api` (
    `id`, `resource_id`, `controller`, `spring_application_name`,
    `request_method`, `name`, `uri`, `is_input`,
    `created_by`, `created_time`, `updated_by`, `updated_time`
)
SELECT
    `incoming`.`id`, `incoming`.`resource_id`, `incoming`.`controller`,
    `incoming`.`spring_application_name`, `incoming`.`request_method`,
    `incoming`.`name`, `incoming`.`uri`, `incoming`.`is_input`,
    `incoming`.`created_by`, `incoming`.`created_time`,
    `incoming`.`updated_by`, `incoming`.`updated_time`
FROM `_tl_140_rule_api_stage` AS `incoming`
WHERE NOT EXISTS (
  SELECT 1
  FROM `def_resource_api` AS `current`
  WHERE `current`.`request_method` = `incoming`.`request_method`
    AND `current`.`uri` = `incoming`.`uri`
);

-- 提交前门禁：两个受管端点都必须且只能存在一条。
INSERT INTO `_tl_140_rule_api_gate` (`id`)
SELECT 1
FROM `_tl_140_rule_api_stage` AS `incoming`
LEFT JOIN `def_resource_api` AS `current`
  ON `current`.`request_method` = `incoming`.`request_method`
 AND `current`.`uri` = `incoming`.`uri`
GROUP BY `incoming`.`id`
HAVING COUNT(`current`.`id`) <> 1
LIMIT 1;

COMMIT;

DROP TEMPORARY TABLE IF EXISTS `_tl_140_rule_api_stage`;
DROP TEMPORARY TABLE IF EXISTS `_tl_140_rule_api_gate`;

-- 验证：结果应为 2。
SELECT COUNT(*) AS `rule_notification_api_count_should_be_2`
FROM `def_resource_api`
WHERE (`request_method` = 'GET' AND `uri` = '/rule/ruleNotification/variables')
   OR (`request_method` = 'POST' AND `uri` = '/rule/ruleNotification/preview');
