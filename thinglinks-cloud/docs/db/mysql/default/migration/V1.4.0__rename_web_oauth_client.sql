-- 版本 1.4.0：将 Web OAuth 客户端标识与产品清单保持一致。
-- 前置条件：在 thinglinks_ds_c_defaults 数据库中执行，并先完成备份。
-- 仅迁移历史初始化值 thinglinks_pro；已调整或用户自定义的客户端不会修改。
DROP TEMPORARY TABLE IF EXISTS `_tl_140_oauth_conflict_gate`;

-- 冲突门禁使用固定主键；重复写入会中止当前迁移。
CREATE TEMPORARY TABLE `_tl_140_oauth_conflict_gate` (
    `gate_key` VARCHAR(64) NOT NULL,
    PRIMARY KEY (`gate_key`)
) ENGINE = InnoDB;

INSERT INTO `_tl_140_oauth_conflict_gate` (`gate_key`)
VALUES ('oauth-migration-ready');

START TRANSACTION;

-- 历史记录仍待迁移时，目标客户端标识不得由其他记录占用。
INSERT INTO `_tl_140_oauth_conflict_gate` (`gate_key`)
SELECT 'oauth-migration-ready'
FROM `def_client` AS `source_client`
INNER JOIN `def_client` AS `target_client`
        ON `target_client`.`id` <> `source_client`.`id`
       AND `target_client`.`client_id` = 'thinglinks_web'
WHERE `source_client`.`id` = 1448881860003233792
  AND `source_client`.`client_id` = 'thinglinks_pro'
  AND `source_client`.`client_secret` = 'thinglinks_pro_secret'
  AND `source_client`.`name` = 'thinglinks_pro';

UPDATE `def_client`
SET `client_id` = 'thinglinks_web',
    `client_secret` = 'thinglinks_web_secret',
    `name` = 'thinglinks_web',
    `updated_time` = CURRENT_TIMESTAMP
WHERE `id` = 1448881860003233792
  AND `client_id` = 'thinglinks_pro'
  AND `client_secret` = 'thinglinks_pro_secret'
  AND `name` = 'thinglinks_pro';

-- 严格匹配的历史记录完成迁移后不应继续存在。
INSERT INTO `_tl_140_oauth_conflict_gate` (`gate_key`)
SELECT 'oauth-migration-ready'
FROM `def_client`
WHERE `id` = 1448881860003233792
  AND `client_id` = 'thinglinks_pro'
  AND `client_secret` = 'thinglinks_pro_secret'
  AND `name` = 'thinglinks_pro';

COMMIT;

DROP TEMPORARY TABLE IF EXISTS `_tl_140_oauth_conflict_gate`;

-- 验证：标准初始化记录应为 thinglinks_web；自定义记录应保持原值。
SELECT `client_id`, `name`
FROM `def_client`
WHERE `id` = 1448881860003233792;
