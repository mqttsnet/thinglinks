-- 版本 1.4.0：清理历史基线误带入的演示数据、固定凭据和私有环境地址。
-- 前置条件：已按 README 顺序执行本目录前三个脚本，并已备份 thinglinks_ds_c_defaults。
-- 安全边界：只处理主键与历史业务身份同时匹配的记录；部署方修改过的记录保持不变。
-- 令牌、密码、应用密钥仅使用 SHA-256 摘要识别，脚本不保存原始敏感值。

SET NAMES utf8mb4;

DROP TEMPORARY TABLE IF EXISTS `_tl_140_seed_tenant`;
DROP TEMPORARY TABLE IF EXISTS `_tl_140_seed_user`;
DROP TEMPORARY TABLE IF EXISTS `_tl_140_seed_application`;
DROP TEMPORARY TABLE IF EXISTS `_tl_140_seed_doc_app`;
DROP TEMPORARY TABLE IF EXISTS `_tl_140_seed_isv`;
DROP TEMPORARY TABLE IF EXISTS `_tl_140_seed_gen_table`;
DROP TEMPORARY TABLE IF EXISTS `_tl_140_seed_resource`;
DROP TEMPORARY TABLE IF EXISTS `_tl_140_seed_interface`;

CREATE TEMPORARY TABLE `_tl_140_seed_tenant` (`id` BIGINT NOT NULL PRIMARY KEY) ENGINE = InnoDB;
CREATE TEMPORARY TABLE `_tl_140_seed_user` (`id` BIGINT NOT NULL PRIMARY KEY) ENGINE = InnoDB;
CREATE TEMPORARY TABLE `_tl_140_seed_application` (`id` BIGINT NOT NULL PRIMARY KEY) ENGINE = InnoDB;
CREATE TEMPORARY TABLE `_tl_140_seed_doc_app` (`id` BIGINT NOT NULL PRIMARY KEY) ENGINE = InnoDB;
CREATE TEMPORARY TABLE `_tl_140_seed_isv` (`id` BIGINT NOT NULL PRIMARY KEY) ENGINE = InnoDB;
CREATE TEMPORARY TABLE `_tl_140_seed_gen_table` (`id` BIGINT NOT NULL PRIMARY KEY) ENGINE = InnoDB;
CREATE TEMPORARY TABLE `_tl_140_seed_resource` (`id` BIGINT NOT NULL PRIMARY KEY) ENGINE = InnoDB;
CREATE TEMPORARY TABLE `_tl_140_seed_interface` (`id` BIGINT NOT NULL PRIMARY KEY) ENGINE = InnoDB;

-- 历史演示租户；名称或接入类型已修改时不纳入清理。
INSERT INTO `_tl_140_seed_tenant` (`id`)
SELECT `id`
FROM `def_tenant`
WHERE `id` = 483305815051076198
  AND `name` = 'MqttsNet演示企业'
  AND `abbreviation` = 'MqttsNet演示企业'
  AND `register_type` = 'CREATE'
  AND `connect_type` = 'SYSTEM';

-- 历史功能测试账号；摘要覆盖用户名、密码散列和盐，避免把原值继续提交到仓库。
INSERT INTO `_tl_140_seed_user` (`id`)
SELECT `id`
FROM `def_user`
WHERE `id` = 442312581298783074
  AND `work_describe` = '功能测试'
  AND SHA2(CONCAT_WS('|', `username`, `password`, `salt`), 256)
      = '3d25511b1019cc9a346ecbab871759db7e0246ad44917a04f15c3f86dc605246';

-- 历史测试应用；应用密钥只按摘要匹配。
INSERT INTO `_tl_140_seed_application` (`id`)
SELECT `id`
FROM `def_application`
WHERE `id` = 633773629138778488
  AND `app_key` = 'R9BNhpFsSQiT'
  AND `name` = '测试应用'
  AND SHA2(`app_secret`, 256) = '195e3367be2042991661f0d20eba76c8c2a3223f6fa4a1314bcb09609c40b408';

-- 历史 Torna 同步应用；真实 token 只按摘要匹配。
INSERT INTO `_tl_140_seed_doc_app` (`id`)
SELECT `id`
FROM `sop_doc_app`
WHERE `id` = 705368977342462102
  AND `app_name` = 'ThingLinks-开放平台测试环境'
  AND SHA2(`token`, 256) = '9e86521d44c7534ad405ac9156fba087190b4b958688617ea112840b75f16434';

-- 历史内置/测试 ISV；仅按完整旧身份纳入清理，其密钥随后按外键身份删除。
INSERT INTO `_tl_140_seed_isv` (`id`)
SELECT `id` FROM `sop_isv_info`
WHERE `id` = 1
  AND `name` = '内置ISV'
  AND `app_id` = '2019032617262200001'
  AND `remark` = '内置测试使用，请勿删除'
UNION ALL
SELECT `id` FROM `sop_isv_info`
WHERE `id` = 646351801644697601
  AND `name` = '测试'
  AND `app_id` = '20250622646351801644697600'
UNION ALL
SELECT `id` FROM `sop_isv_info`
WHERE `id` = 646351801644697603
  AND `name` = 'tes1'
  AND `app_id` = '20250622646351801644697602';

-- 代码生成器的两套运行时样例。
INSERT INTO `_tl_140_seed_gen_table` (`id`)
SELECT `id` FROM `def_gen_table`
WHERE `id` = 291271427745644580
  AND `name` = 'def_gen_test_simple'
  AND `entity_name` = 'DefGenTestSimple'
  AND `comment_` = '测试树结构'
UNION ALL
SELECT `id` FROM `def_gen_table`
WHERE `id` = 291271427745644606
  AND `name` = 'def_gen_test_tree'
  AND `entity_name` = 'DefGenTestTree'
  AND `comment_` = '测试树结构';

-- 运行时演示菜单与两条固定测试数据权限。
INSERT INTO `_tl_140_seed_resource` (`id`)
SELECT `id` FROM `def_resource`
WHERE `id` = 207209017863307264 AND `application_id` = 2
  AND `code` = 'tenant:developer:demo' AND `parent_id` = 160833820721938437
UNION ALL
SELECT `id` FROM `def_resource`
WHERE `id` = 207209017863307265 AND `application_id` = 2
  AND `code` = 'tenant:developer:demo:simple' AND `parent_id` = 207209017863307264
UNION ALL
SELECT `id` FROM `def_resource`
WHERE `id` = 207209017863307266 AND `application_id` = 2
  AND `code` = 'tenant:developer:demo:tree' AND `parent_id` = 207209017863307264
UNION ALL
SELECT `id` FROM `def_resource`
WHERE `id` = 207209017863307267 AND `application_id` = 2
  AND `code` = 'tenant:developer:demo:m_s' AND `parent_id` = 207209017863307264
UNION ALL
SELECT `id` FROM `def_resource`
WHERE `id` = 179582070228516870 AND `application_id` = 1
  AND `code` = 'basic:msg:msg:custom' AND `custom_class` = 'DATA_SCOPE_TEST'
UNION ALL
SELECT `id` FROM `def_resource`
WHERE `id` = 524352044412200344 AND `application_id` = 3
  AND `code` = 'link:device:device:custom' AND `custom_class` = 'DATA_SCOPE_TEST';

-- 固定启用的脚本测试接口。
INSERT INTO `_tl_140_seed_interface` (`id`)
SELECT `id`
FROM `def_interface`
WHERE `id` = 251763346439667712
  AND `code` = 'TEST'
  AND `name` = '测试通过脚本执行接口逻辑';

START TRANSACTION;

-- 删除已确认的 Torna 运行时快照和暴露 token 的应用。
DELETE `content`
FROM `sop_doc_content` AS `content`
INNER JOIN `sop_doc_info` AS `info` ON `info`.`id` = `content`.`doc_info_id`
INNER JOIN `_tl_140_seed_doc_app` AS `target` ON `target`.`id` = `info`.`doc_app_id`;

DELETE `info`
FROM `sop_doc_info` AS `info`
INNER JOIN `_tl_140_seed_doc_app` AS `target` ON `target`.`id` = `info`.`doc_app_id`;

DELETE `app`
FROM `sop_doc_app` AS `app`
INNER JOIN `_tl_140_seed_doc_app` AS `target` ON `target`.`id` = `app`.`id`;

-- 删除历史示例帮助文档；修改过标题的同主键文档保持不变。
DELETE FROM `sop_help_doc`
WHERE (`id` = 712712237514301095 AND `label` = '示例1： Java基础语法入门')
   OR (`id` = 712712237514301098 AND `label` = '文档二：Java面向对象基础')
   OR (`id` = 712712237514301101 AND `label` = '三篇随机主题Markdown文章')
   OR (`id` = 712712237514301114 AND `label` = '文档三：Java常用类与工具类入门');

-- 先删除 ISV 关联与密钥，再删除身份记录。
DELETE `relation`
FROM `sop_perm_isv_group` AS `relation`
INNER JOIN `_tl_140_seed_isv` AS `target` ON `target`.`id` = `relation`.`isv_id`;

DELETE `keys_`
FROM `sop_isv_keys` AS `keys_`
INNER JOIN `_tl_140_seed_isv` AS `target` ON `target`.`id` = `keys_`.`isv_id`;

DELETE `isv`
FROM `sop_isv_info` AS `isv`
INNER JOIN `_tl_140_seed_isv` AS `target` ON `target`.`id` = `isv`.`id`;

-- 删除代码生成样例及其列元数据。
DELETE `column_`
FROM `def_gen_table_column` AS `column_`
INNER JOIN `_tl_140_seed_gen_table` AS `target` ON `target`.`id` = `column_`.`table_id`;

DELETE `table_`
FROM `def_gen_table` AS `table_`
INNER JOIN `_tl_140_seed_gen_table` AS `target` ON `target`.`id` = `table_`.`id`;

-- 清除菜单引用后删除演示资源及关联。
UPDATE `def_gen_table`
SET `menu_parent_id` = NULL
WHERE `menu_parent_id` IN (SELECT `id` FROM `_tl_140_seed_resource`);

DELETE `api`
FROM `def_resource_api` AS `api`
INNER JOIN `_tl_140_seed_resource` AS `target` ON `target`.`id` = `api`.`resource_id`;

DELETE `relation`
FROM `def_tenant_resource_rel` AS `relation`
INNER JOIN `_tl_140_seed_resource` AS `target` ON `target`.`id` = `relation`.`resource_id`;

DELETE `resource_`
FROM `def_resource` AS `resource_`
INNER JOIN `_tl_140_seed_resource` AS `target` ON `target`.`id` = `resource_`.`id`;

-- 删除脚本测试接口和属性。
DELETE `property_`
FROM `def_interface_property` AS `property_`
INNER JOIN `_tl_140_seed_interface` AS `target` ON `target`.`id` = `property_`.`interface_id`;

DELETE `interface_`
FROM `def_interface` AS `interface_`
INNER JOIN `_tl_140_seed_interface` AS `target` ON `target`.`id` = `interface_`.`id`;

-- 删除只服务于生成样例的字典和物联卡测试项。
DELETE FROM `def_dict`
WHERE (`id` = 216686795209834626 AND `parent_id` = 0 AND `key_` = 'TEST_ADD_DICT')
   OR (`id` = 216686795209834627 AND `parent_id` = 216686795209834626 AND `parent_key` = 'TEST_ADD_DICT' AND `key_` = '1')
   OR (`id` = 216686795209834628 AND `parent_id` = 216686795209834626 AND `parent_key` = 'TEST_ADD_DICT' AND `key_` = '2')
   OR (`id` = 216686795209834629 AND `parent_id` = 216686795209834626 AND `parent_key` = 'TEST_ADD_DICT' AND `key_` = 'aad')
   OR (`id` = 515431758114395244 AND `parent_id` = 515431758114395136 AND `parent_key` = 'CARD_APPLICATION_SCENARIO' AND `key_` = 'test1')
   OR (`id` = 680387613591506003 AND `parent_id` = 0 AND `key_` = 'DefGenTestTreeType2Enum')
   OR (`id` = 680387613591506004 AND `parent_id` = 680387613591506003 AND `parent_key` = 'DefGenTestTreeType2Enum' AND `key_` = 'ORDINARY')
   OR (`id` = 680387613591506005 AND `parent_id` = 680387613591506003 AND `parent_key` = 'DefGenTestTreeType2Enum' AND `key_` = 'GIFT')
   OR (`id` = 680387613591506006 AND `parent_id` = 0 AND `key_` = 'DefGenTestSimpleType2Enum')
   OR (`id` = 680387613591506007 AND `parent_id` = 680387613591506006 AND `parent_key` = 'DefGenTestSimpleType2Enum' AND `key_` = 'ORDINARY')
   OR (`id` = 680387613591506008 AND `parent_id` = 680387613591506006 AND `parent_key` = 'DefGenTestSimpleType2Enum' AND `key_` = 'GIFT');

-- 稳定业务键保持不变，只中性化历史展示文案。
UPDATE `def_dict`
SET `name` = 'ThingLinks内置场景',
    `remark` = '平台内置应用场景',
    `i18n_json` = '{\"zh-CN\": \"ThingLinks内置场景\", \"en-US\": \"ThingLinks Built-in Scenario\", \"ja\": \"ThingLinks組み込みシナリオ\"}'
WHERE `id` = 380145455129952257
  AND `parent_id` = 380145455129952256
  AND `parent_key` = 'LINK_APPLICATION_SCENARIO'
  AND `key_` = 'thinglinks-test'
  AND `name` = 'ThingLinks测试场景';

UPDATE `def_dict`
SET `name` = 'ThingLinks内置应用',
    `remark` = '平台内置物联卡应用',
    `i18n_json` = '{\"zh-CN\": \"ThingLinks内置应用\", \"en-US\": \"ThingLinks Built-in Application\", \"ja\": \"ThingLinks組み込みアプリケーション\"}'
WHERE `id` = 515431758114395245
  AND `parent_id` = 515431758114395136
  AND `parent_key` = 'CARD_APPLICATION_SCENARIO'
  AND `key_` = 'thinglinks-test'
  AND `name` = '测试应用';

UPDATE `def_dict`
SET `name` = 'ThingLinks内置场景',
    `remark` = '平台内置视频应用场景',
    `i18n_json` = '{\"zh-CN\": \"ThingLinks内置场景\", \"en-US\": \"ThingLinks Built-in Scenario\", \"ja\": \"ThingLinks組み込みシナリオ\"}'
WHERE `id` = 515431758114395246
  AND `parent_id` = 515431758114395137
  AND `parent_key` = 'VIDEO_APPLICATION_SCENARIO'
  AND `key_` = 'thinglinks'
  AND `name` = '测试场景(thinglinks)';

-- 仅将确认的历史私有 Broker 地址替换为本地初始化地址。
UPDATE `def_dict`
SET `key_` = '127.0.0.1:11883', `name` = '本地节点(mqtt://)', `remark` = '127.0.0.1:11883',
    `i18n_json` = '{\"zh-CN\": \"本地节点(mqtt://)\", \"en-US\": \"Local node (mqtt://)\", \"ja\": \"ローカルノード(mqtt://)\"}'
WHERE `id` = 343221986358460424 AND `parent_key` = 'LINK_DEVICE_CONNECTOR'
  AND `key_` = 'broker.thinglinks.mqttsnet.com:11883';

UPDATE `def_dict`
SET `key_` = '127.0.0.1:18883', `name` = '本地节点(ws://)', `remark` = '127.0.0.1:18883,\nPath:/mqtt',
    `i18n_json` = '{\"zh-CN\": \"本地节点(ws://)\", \"en-US\": \"Local node (ws://)\", \"ja\": \"ローカルノード(ws://)\"}'
WHERE `id` = 603366042365234176 AND `parent_key` = 'LINK_DEVICE_CONNECTOR'
  AND `key_` = 'broker.thinglinks.mqttsnet.com:18883';

UPDATE `def_dict`
SET `key_` = '127.0.0.1:11884', `name` = '本地节点(mqtts://)', `remark` = '127.0.0.1:11884',
    `i18n_json` = '{\"zh-CN\": \"本地节点(mqtts://)\", \"en-US\": \"Local node (mqtts://)\", \"ja\": \"ローカルノード(mqtts://)\"}'
WHERE `id` = 603366042365234177 AND `parent_key` = 'LINK_DEVICE_CONNECTOR'
  AND `key_` = 'broker.thinglinks.mqttsnet.com:11884';

UPDATE `def_dict`
SET `key_` = '127.0.0.1:18443', `name` = '本地节点(wss://)', `remark` = '127.0.0.1:18443,\nPath:/mqtt',
    `i18n_json` = '{\"zh-CN\": \"本地节点(wss://)\", \"en-US\": \"Local node (wss://)\", \"ja\": \"ローカルノード(wss://)\"}'
WHERE `id` = 603366042365234178 AND `parent_key` = 'LINK_DEVICE_CONNECTOR'
  AND `key_` = 'broker.thinglinks.mqttsnet.com:18443';

-- 私有规则实例不写入开源默认数据；FUXA 保留可配置占位项。
DELETE FROM `def_dict`
WHERE `id` = 475671003876294679
  AND `parent_id` = 475671003876294678
  AND `parent_key` = 'RULE_INSTANCE_ADDRESS'
  AND `key_` = 'https://r1.nodered.thinglinks.mqttsnet.com';

UPDATE `def_dict`
SET `key_` = '__FUXA_URL__', `name` = '组态实例', `state` = b'0', `remark` = '部署后配置访问地址',
    `i18n_json` = '{\"zh-CN\": \"组态实例\", \"en-US\": \"SCADA instance\", \"ja\": \"構成インスタンス\"}'
WHERE `id` = 571418731291661584
  AND `parent_id` = 475671003876294678
  AND `parent_key` = 'RULE_INSTANCE_ADDRESS'
  AND `key_` = 'https://fuxa.mqttsnet.com';

-- 外部控制台只在命中历史绝对地址时改成禁用占位项。
UPDATE `def_resource` SET `link` = '__NACOS_CONSOLE_URL__', `state` = b'0'
WHERE `id` = 160833820721938439 AND `code` = 'tenant:developer:nacos'
  AND `link` = 'https://nacos.thinglinks.mqttsnet.com';
UPDATE `def_resource` SET `link` = '__SKYWALKING_CONSOLE_URL__', `state` = b'0'
WHERE `id` = 160833820721938440 AND `code` = 'tenant:developer:skyWalking'
  AND `link` = 'http://sky.thinglinks.mqttsnet.com';
UPDATE `def_resource` SET `link` = '__MINIO_CONSOLE_URL__', `state` = b'0'
WHERE `id` = 242152648445263888 AND `code` = 'tenant:developer:minio'
  AND `link` = 'https://minio.mqttsnet.com/minio/ui/';
UPDATE `def_resource` SET `link` = '__JOB_ADMIN_URL__', `state` = b'0'
WHERE `id` = 242152648445263893 AND `code` = 'tenant:developer:job'
  AND `link` = 'https://job.thinglinks.mqttsnet.com/thinglinks-job-admin';
UPDATE `def_resource` SET `link` = '__FILE_PREVIEW_URL__', `state` = b'0'
WHERE `id` = 242152648445263898 AND `code` = 'tenant:developer:file'
  AND `link` = 'https://file.kkview.cn/';
UPDATE `def_resource` SET `link` = '__JENKINS_URL__', `state` = b'0'
WHERE `id` = 242300348075606021 AND `code` = 'tenant:developer:jenkins'
  AND `link` = 'http://jenkins.mqttsnet.com/';
UPDATE `def_resource` SET `link` = '__SCADA_URL__', `state` = b'0'
WHERE `id` = 571418731291661586 AND `code` = 'link:scada'
  AND `link` = 'https://scada.mqttsnet.com/';
UPDATE `def_resource` SET `link` = '__NODERED_MANAGER_URL__', `state` = b'0'
WHERE `id` = 673403416947298304 AND `code` = 'tenant:developer:noderedManager'
  AND `link` = 'http://manager.nodered.thinglinks.mqttsnet.com/';

UPDATE `def_resource` SET `link` = 'https://github.com/mqttsnet/thinglinks'
WHERE `id` = 160833820721938443 AND `code` = 'tenant:about:doc'
  AND `link` = 'https://mqttsnet.yuque.com/gt6zkc/thinglinks';
UPDATE `def_resource` SET `link` = 'https://github.com/mqttsnet/thinglinks'
WHERE `id` = 160833820721938444 AND `code` = 'tenant:about:vip'
  AND `link` = 'https://www.mqttsnet.com';

-- 大屏应用和开放平台地址只清理确定的历史私有值。
UPDATE `def_application` SET `url` = ''
WHERE `id` = 343406236160491521
  AND `app_key` = 'visualizationSystem'
  AND `url` = 'https://pro-test-view.thinglinks.mqttsnet.com/';

UPDATE `sop_sys_config` SET `config_value` = '__SOP_OPEN_PRODUCTION_URL__'
WHERE `id` = 4 AND `config_key` = 'admin.open-prod-url'
  AND `config_value` = 'https://openapi.thinglinks.mqttsnet.com/api';
UPDATE `sop_sys_config` SET `config_value` = '__SOP_OPEN_SANDBOX_URL__'
WHERE `id` = 5 AND `config_key` = 'admin.open-sandbox-url'
  AND `config_value` = 'https://openapi.thinglinks.mqttsnet.com/api';
UPDATE `sop_sys_config` SET `config_value` = '__TORNA_SERVER_URL__'
WHERE `id` = 6 AND `config_key` = 'admin.torna-server-addr'
  AND `config_value` = 'https://torna.thinglinks.mqttsnet.com';

-- 删除历史演示租户、测试账号和测试应用的默认库关联。
DELETE `row_` FROM `def_login_log` AS `row_`
LEFT JOIN `_tl_140_seed_tenant` AS `tenant_` ON `tenant_`.`id` = `row_`.`tenant_id`
LEFT JOIN `_tl_140_seed_user` AS `user_` ON `user_`.`id` = `row_`.`user_id`
WHERE `tenant_`.`id` IS NOT NULL OR `user_`.`id` IS NOT NULL;

DELETE `row_` FROM `def_user_application` AS `row_`
LEFT JOIN `_tl_140_seed_user` AS `user_` ON `user_`.`id` = `row_`.`user_id`
LEFT JOIN `_tl_140_seed_application` AS `app_` ON `app_`.`id` = `row_`.`application_id`
WHERE `user_`.`id` IS NOT NULL OR `app_`.`id` IS NOT NULL;

DELETE `row_` FROM `def_user_tenant_rel` AS `row_`
LEFT JOIN `_tl_140_seed_user` AS `user_` ON `user_`.`id` = `row_`.`user_id`
LEFT JOIN `_tl_140_seed_tenant` AS `tenant_` ON `tenant_`.`id` = `row_`.`tenant_id`
WHERE `user_`.`id` IS NOT NULL OR `tenant_`.`id` IS NOT NULL;

DELETE `row_` FROM `def_tenant_resource_rel` AS `row_`
LEFT JOIN `_tl_140_seed_tenant` AS `tenant_` ON `tenant_`.`id` = `row_`.`tenant_id`
LEFT JOIN `_tl_140_seed_application` AS `app_` ON `app_`.`id` = `row_`.`application_id`
WHERE `tenant_`.`id` IS NOT NULL OR `app_`.`id` IS NOT NULL;

DELETE `row_` FROM `def_tenant_application_record` AS `row_`
LEFT JOIN `_tl_140_seed_tenant` AS `tenant_` ON `tenant_`.`id` = `row_`.`tenant_id`
LEFT JOIN `_tl_140_seed_application` AS `app_` ON `app_`.`id` = `row_`.`application_id`
WHERE `tenant_`.`id` IS NOT NULL OR `app_`.`id` IS NOT NULL;

DELETE `row_` FROM `def_tenant_application_rel` AS `row_`
LEFT JOIN `_tl_140_seed_tenant` AS `tenant_` ON `tenant_`.`id` = `row_`.`tenant_id`
LEFT JOIN `_tl_140_seed_application` AS `app_` ON `app_`.`id` = `row_`.`application_id`
WHERE `tenant_`.`id` IS NOT NULL OR `app_`.`id` IS NOT NULL;

DELETE `row_` FROM `def_tenant_ds_c_rel` AS `row_`
INNER JOIN `_tl_140_seed_tenant` AS `tenant_` ON `tenant_`.`id` = `row_`.`tenant_id`;

DELETE FROM `def_tenant_ds_c_rel`
WHERE (`id` = 361661316047306753 AND `tenant_id` = 361661316047306752
       AND `datasource_config_id` = 347271908590354432 AND `db_prefix` = 'thinglinks_base')
   OR (`id` = 361661316047306754 AND `tenant_id` = 361661316047306752
       AND `datasource_config_id` = 347271908590354432 AND `db_prefix` = 'thinglinks_extend');

DELETE `row_` FROM `sop_notify_info` AS `row_`
INNER JOIN `_tl_140_seed_tenant` AS `tenant_` ON `tenant_`.`id` = `row_`.`tenant_id`;

DELETE `row_` FROM `com_appendix` AS `row_`
LEFT JOIN `_tl_140_seed_tenant` AS `tenant_` ON `tenant_`.`id` = `row_`.`tenant_id` OR `tenant_`.`id` = `row_`.`biz_id`
LEFT JOIN `_tl_140_seed_user` AS `user_` ON `user_`.`id` = `row_`.`biz_id`
LEFT JOIN `_tl_140_seed_application` AS `app_` ON `app_`.`id` = `row_`.`biz_id`
WHERE `tenant_`.`id` IS NOT NULL OR `user_`.`id` IS NOT NULL OR `app_`.`id` IS NOT NULL;

DELETE `row_` FROM `com_file` AS `row_`
LEFT JOIN `_tl_140_seed_tenant` AS `tenant_` ON `tenant_`.`id` = `row_`.`tenant_id`
LEFT JOIN `_tl_140_seed_user` AS `user_` ON `user_`.`id` = `row_`.`created_by`
WHERE `tenant_`.`id` IS NOT NULL OR `user_`.`id` IS NOT NULL;

DELETE `row_` FROM `def_user` AS `row_`
INNER JOIN `_tl_140_seed_user` AS `target` ON `target`.`id` = `row_`.`id`;
DELETE `row_` FROM `def_application` AS `row_`
INNER JOIN `_tl_140_seed_application` AS `target` ON `target`.`id` = `row_`.`id`;
DELETE `row_` FROM `def_tenant` AS `row_`
INNER JOIN `_tl_140_seed_tenant` AS `target` ON `target`.`id` = `row_`.`id`;

COMMIT;

-- 验证：所有结果均应为 0。非 0 表示记录已被修改，脚本为保护现场而跳过。
SELECT COUNT(*) AS `legacy_demo_tenant_count_should_be_0`
FROM `def_tenant` WHERE `id` = 483305815051076198 AND `name` = 'MqttsNet演示企业';
SELECT COUNT(*) AS `legacy_test_user_count_should_be_0`
FROM `def_user` WHERE `id` = 442312581298783074 AND `work_describe` = '功能测试';
SELECT COUNT(*) AS `legacy_test_application_count_should_be_0`
FROM `def_application` WHERE `id` = 633773629138778488 AND `name` = '测试应用';
SELECT COUNT(*) AS `legacy_torna_token_count_should_be_0`
FROM `sop_doc_app`
WHERE `id` = 705368977342462102
  AND SHA2(`token`, 256) = '9e86521d44c7534ad405ac9156fba087190b4b958688617ea112840b75f16434';
SELECT COUNT(*) AS `legacy_runtime_test_asset_count_should_be_0`
FROM `def_resource`
WHERE `id` IN (207209017863307264, 207209017863307265, 207209017863307266,
               207209017863307267, 179582070228516870, 524352044412200344);
