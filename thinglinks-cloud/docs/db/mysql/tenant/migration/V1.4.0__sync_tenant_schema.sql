-- 版本 1.4.0：同步租户业务库表结构。
-- 前置条件：停止写入服务、完成目标租户库全量备份，并在 thinglinks_base_{tenantId} 中执行。
-- MySQL DDL 会隐式提交，不能由事务整体回滚；本脚本通过结构探测避免重复新增，并保留所有旧字段和旧业务表。
-- 若任一唯一键预检未通过，对应新唯一索引会暂缓创建；按文末查询处理数据后可安全重跑。

SET NAMES utf8mb4;

-- 重跑辅助过程：只在目标结构不存在时执行对应 DDL。
DROP PROCEDURE IF EXISTS `thinglinks_v140_exec_if`;
DELIMITER $$
CREATE PROCEDURE `thinglinks_v140_exec_if`(IN should_execute BOOLEAN, IN ddl_sql LONGTEXT)
BEGIN
  IF should_execute THEN
    SET @THINGLINKS_V140_DDL = ddl_sql;
    PREPARE thinglinks_v140_stmt FROM @THINGLINKS_V140_DDL;
    EXECUTE thinglinks_v140_stmt;
    DEALLOCATE PREPARE thinglinks_v140_stmt;
  END IF;
END$$
DELIMITER ;

CREATE TABLE IF NOT EXISTS `ca_cert_audit_log`  (
  `id` bigint NOT NULL COMMENT 'id',
  `ca_id` bigint NULL DEFAULT NULL COMMENT '关联 CA 证书 ID',
  `ca_serial_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'CA 证书序列号',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作类型: IMPORT/ISSUE/REVOKE/DOWNLOAD_PACK/SSL_TEST',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '详情(JSON 或自由文本)',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '最后修改时间',
  `deleted` int NULL DEFAULT 0 COMMENT '是否删除(0-未删除/1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ca_id`(`ca_id` ASC) USING BTREE COMMENT 'CA ID 索引',
  INDEX `idx_type_created`(`type` ASC, `created_time` ASC) USING BTREE COMMENT '类型+时间复合索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'CA 证书审计日志' ROW_FORMAT = DYNAMIC;

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'device' AND `column_name` = 'last_status_event_hlc'),
  'ALTER TABLE `device` ADD COLUMN `last_status_event_hlc` bigint NOT NULL DEFAULT 0 COMMENT ''最新状态事件因果时钟(HLC,64-bit)'' AFTER `connect_status`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'device' AND `column_name` = 'bound_product_version_no'),
  'ALTER TABLE `device` ADD COLUMN `bound_product_version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '''' COMMENT ''设备绑定的产品版本序号(快照标识,数据上报路径的物模型解析依据,灰度发布时可与产品当前版本不同)'' AFTER `product_identification`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'ota_upgrades' AND `column_name` = 'product_version_no'),
  'ALTER TABLE `ota_upgrades` ADD COLUMN `product_version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '''' COMMENT ''产品版本序号'' AFTER `product_identification`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'product' AND `column_name` = 'active_version_no'),
  'ALTER TABLE `product` ADD COLUMN `active_version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '''' COMMENT ''产品当前激活的版本序号(系统发布时生成的快照标识,16位短雪花字符串,非用户语义化版本号)'' AFTER `product_status`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'product' AND `column_name` = 'previous_full_version_no'),
  'ALTER TABLE `product` ADD COLUMN `previous_full_version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '''' COMMENT ''灰度切换前的全量版本序号(仅灰度态有值,灰度晋升/回滚后清空,供回滚定位与灰度路由用)'' AFTER `active_version_no`'
);

-- 旧 product_version 是唯一可证明的当前产品版本来源；只回填可无损容纳的值，旧列永久保留供核对和回退。
START TRANSACTION;
CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 1 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'product' AND `column_name` = 'product_version'),
  'UPDATE `product`
   SET `active_version_no` = `product_version`
   WHERE (`active_version_no` IS NULL OR `active_version_no` = '''')
     AND `product_version` IS NOT NULL
     AND `product_version` <> ''''
     AND CHAR_LENGTH(`product_version`) <= 64'
);
COMMIT;

CREATE TABLE IF NOT EXISTS `product_publish_record`  (
  `id` bigint NOT NULL COMMENT 'id',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `source_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '源版本号',
  `target_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '目标版本号',
  `intent` tinyint NOT NULL DEFAULT 0 COMMENT '操作意图[0-发布 1-回滚 2-历史清理]',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '执行状态[0-执行中 1-成功 2-失败]',
  `ddl_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'DDL列表JSON数组(已执行的DDL明细 + 重试元数据)',
  `canary_result_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '策略执行结果快照JSON',
  `failed_reason` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '失败原因(成功时为空)',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数(达上限不再重跑)',
  `max_retry_count` int NOT NULL DEFAULT 3 COMMENT '最大重试次数(用户可配,上限10)',
  `started_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `finished_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_identification`(`product_identification` ASC) USING BTREE COMMENT '产品标识',
  INDEX `idx_target_version`(`target_version` ASC) USING BTREE COMMENT '目标版本号'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '产品发布记录' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `product_version`  (
  `id` bigint NOT NULL COMMENT 'id',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版本序号(系统发布时生成的不可变快照标识,16位短雪花字符串)',
  `version_status` tinyint NOT NULL DEFAULT 0 COMMENT '版本状态[0-草稿 1-已发布 2-灰度中 3-影子 4-已回滚 5-已归档]',
  `product_snapshot_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '产品快照JSON(冻结整棵产品树)',
  `publish_strategy` tinyint NULL DEFAULT NULL COMMENT '发布策略[0-全量 1-灰度 2-影子]',
  `canary_config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '灰度配置JSON',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_version_no`(`version_no` ASC) USING BTREE COMMENT '版本序号',
  INDEX `idx_product_identification`(`product_identification` ASC) USING BTREE COMMENT '产品标识',
  INDEX `idx_version_status`(`version_status` ASC) USING BTREE COMMENT '版本状态'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '产品物模型版本快照' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `product_version_change_log`  (
  `id` bigint NOT NULL COMMENT 'id',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '版本序号:本批变更归属版本(草稿期累积,发布后固化,对应 product_version.version_no)',
  `change_type` tinyint NOT NULL DEFAULT 1 COMMENT '变更类型[0-新增 1-编辑 2-删除]',
  `target_type` tinyint NULL DEFAULT NULL COMMENT '变更维度[0-产品信息 1-服务 2-属性 3-命令]',
  `change_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '变更摘要',
  `change_detail_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '字段级变更明细JSON(覆盖产品所有字段)',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_identification`(`product_identification` ASC) USING BTREE COMMENT '产品标识',
  INDEX `idx_change_type`(`change_type` ASC) USING BTREE COMMENT '变更类型'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '产品物模型版本变更日志' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `rule_bridge_execution_step`  (
  `id` bigint NOT NULL COMMENT '主键',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '关联 trace（FK→rule_bridge_execution_trace.trace_id）',
  `bridge_rule_id` bigint NULL DEFAULT NULL COMMENT '关联桥接规则 ID(同 traceId 命中多条规则时区分 step 归属)',
  `step_no` int NOT NULL DEFAULT 0 COMMENT '步骤顺序号（从1起，前端按此排序）',
  `step_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '类型枚举：INGEST-数据接入 / RULE_MATCH-规则匹配 / RATE_LIMIT-限流 / TRANSFORM-脚本转换 / SINK_SEND-投递 / DEAD_LETTER-死信 / INBOUND_FORWARD-入站还原',
  `step_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '步骤可读名称（中文，前端卡片标题用）',
  `status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '00' COMMENT '00-成功 / 01-失败 / 02-跳过',
  `latency_ms` int NULL DEFAULT NULL COMMENT '本步骤耗时（毫秒）',
  `input_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '输入摘要 JSON（envelope payload 前 1KB / 命中条件 / 模板变量等）',
  `output_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '输出摘要 JSON（转换后 payload / sink 返回值 / 发送 messageId 等）',
  `error_msg` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '失败错误（status=01 时填；透传下游 raw 错误，对齐 ERROR_MSG_MAX_LENGTH=4000）',
  `started_at` datetime(3) NOT NULL COMMENT '步骤开始时间（毫秒精度）',
  `extend_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展参数（步骤特异协议数据 JSON：SINK_SEND 含 sinkType/partition/messageId；RULE_MATCH 含命中条件细节；RATE_LIMIT 含阈值/当前 QPS；TRANSFORM 含 scriptId/scriptVersion 等）',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_time`(`status` ASC, `started_at` ASC) USING BTREE COMMENT '按状态查异常步骤（监控告警用）',
  INDEX `idx_trace_rule_step`(`trace_id` ASC, `bridge_rule_id` ASC, `step_no` ASC) USING BTREE COMMENT '按 (traceId+ruleId) 查 step,详情抽屉用'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '桥接执行步骤明细（链路回放展示用）' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `rule_bridge_execution_trace`  (
  `id` bigint NOT NULL COMMENT '主键',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '全链路追踪ID（贯穿 mqs → RocketMQ → rule，可与设备 publish 日志串联）',
  `bridge_rule_id` bigint NULL DEFAULT NULL COMMENT '关联桥接规则 ID（出站必填；入站为订阅源拉取时为空）',
  `bridge_rule_id_key` varchar(22) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci GENERATED ALWAYS AS (CASE WHEN `bridge_rule_id` IS NULL THEN 'N:' ELSE CONCAT('V:', `bridge_rule_id`) END) STORED COMMENT '桥接规则ID唯一键归一值',
  `direction` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '10' COMMENT '桥接方向：10-出站 / 20-入站',
  `trigger_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '触发来源：DEVICE_DATA-设备数据 / SUBSCRIPTION-订阅源 / TEST_SINK-测试发送 / REPLAY-死信回放',
  `product_identification` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '产品标识（出站时来自设备事件）',
  `device_identification` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备标识（出站时来自设备事件）',
  `action_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '事件类型（PUBLISH/CONNECT/CLOSE/...，复用 LINK_DEVICE_ACTION_TYPE 字典）',
  `topic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备事件 topic',
  `data_source_id` bigint NULL DEFAULT NULL COMMENT '关联数据源 ID（出站=目标 sink；入站=来源 source）',
  `subscription_source_id` bigint NULL DEFAULT NULL COMMENT '关联订阅源 ID（仅入站）',
  `status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '00' COMMENT '整体状态：00-成功 / 01-失败 / 02-部分成功 / 03-死信',
  `step_count` int NOT NULL DEFAULT 0 COMMENT '执行的步骤总数（关联 rule_bridge_execution_step 计数）',
  `total_latency_ms` int NULL DEFAULT NULL COMMENT '总耗时毫秒（开始到结束）',
  `start_time` datetime(3) NOT NULL COMMENT '执行开始时间（毫秒精度）',
  `end_time` datetime(3) NULL DEFAULT NULL COMMENT '执行结束时间（毫秒精度）',
  `source_payload_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '源消息摘要（完整 envelope 报文；便于排查 + 死信回放）',
  `result_summary` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '结果摘要（成功的 sink / 失败原因等一句话；对齐 RESULT_SUMMARY_MAX_LENGTH=2000）',
  `error_msg` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '失败时的错误信息（透传 RocketMQ/Kafka/HTTP 等下游 raw 错误，含堆栈描述；对齐 ERROR_MSG_MAX_LENGTH=4000）',
  `extend_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展参数',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_trace_rule_key`(`trace_id` ASC, `bridge_rule_id_key` ASC) USING BTREE COMMENT '同一 envelope 与规则归一键唯一;RocketMQ 重投同 envelope 同规则被拦截',
  INDEX `idx_rule_status_time`(`bridge_rule_id` ASC, `status` ASC, `start_time` ASC) USING BTREE COMMENT '按规则+状态+时间查日志',
  INDEX `idx_device_time`(`device_identification` ASC, `start_time` ASC) USING BTREE COMMENT '按设备排查链路',
  INDEX `idx_rule_time`(`bridge_rule_id` ASC, `start_time` ASC) USING BTREE COMMENT '按规则+时间查日志 / 统计',
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE COMMENT '按执行时间倒序分页 / 时间范围统计',
  INDEX `idx_status_time`(`status` ASC, `start_time` ASC) USING BTREE COMMENT '按状态+执行时间筛选 trace',
  INDEX `idx_org_time`(`created_org_id` ASC, `start_time` ASC) USING BTREE COMMENT '按组织数据权限+执行时间查询'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '桥接执行trace主表（链路回放用）' ROW_FORMAT = DYNAMIC;

-- 入站订阅轨迹允许没有桥接规则；空值与真实规则 ID 使用不同前缀形成内部唯一键。
-- 预检包含逻辑删除记录，因为原索引与新索引均覆盖整张表；脚本不会自动删除或合并重复数据。
SELECT COUNT(*) AS `rule_trace_normalized_duplicate_group_count_should_be_0`
FROM (
  SELECT 1
  FROM `rule_bridge_execution_trace`
  GROUP BY `trace_id`,
    CASE WHEN `bridge_rule_id` IS NULL THEN 'N:' ELSE CONCAT('V:', `bridge_rule_id`) END
  HAVING COUNT(*) > 1
) AS `duplicate_rule_trace_identity`;

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_bridge_execution_trace'
     AND `column_name` = 'bridge_rule_id_key'),
  'ALTER TABLE `rule_bridge_execution_trace`
   ADD COLUMN `bridge_rule_id_key` varchar(22) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci
   GENERATED ALWAYS AS (CASE WHEN `bridge_rule_id` IS NULL
     THEN ''N:'' ELSE CONCAT(''V:'', `bridge_rule_id`) END) STORED
   COMMENT ''桥接规则ID唯一键归一值'' AFTER `bridge_rule_id`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_bridge_execution_trace'
     AND `index_name` = 'uk_trace_rule_key')
  AND EXISTS (
    SELECT 1 FROM `information_schema`.`columns`
    WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_bridge_execution_trace'
      AND `column_name` = 'bridge_rule_id_key'
      AND `data_type` = 'varchar' AND `character_maximum_length` = 22
      AND `character_set_name` = 'utf8mb4' AND `collation_name` = 'utf8mb4_general_ci'
      AND `extra` LIKE '%STORED GENERATED%'
      AND LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        `generation_expression`, '`', ''), ' ', ''), '(', ''), ')', ''), '_utf8mb4', '')) =
        'casewhenbridge_rule_idisnullthen''n:''elseconcat''v:'',bridge_rule_idend'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `rule_bridge_execution_trace`
    GROUP BY `trace_id`,
      CASE WHEN `bridge_rule_id` IS NULL THEN 'N:' ELSE CONCAT('V:', `bridge_rule_id`) END
    HAVING COUNT(*) > 1
  ),
  'ALTER TABLE `rule_bridge_execution_trace`
   ADD UNIQUE INDEX `uk_trace_rule_key` (`trace_id` ASC, `bridge_rule_id_key` ASC) USING BTREE
   COMMENT ''同一 envelope 与规则归一键唯一;RocketMQ 重投同 envelope 同规则被拦截'''
);

-- 仅在替代索引与生成列定义均符合预期时移除旧索引。
CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 2
          AND MIN(`non_unique`) = 0
          AND COUNT(`sub_part`) = 0
          AND GROUP_CONCAT(`column_name` ORDER BY `seq_in_index` SEPARATOR ',') =
            'trace_id,bridge_rule_id_key'
   FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_bridge_execution_trace'
     AND `index_name` = 'uk_trace_rule_key')
  AND EXISTS (
    SELECT 1 FROM `information_schema`.`columns`
    WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_bridge_execution_trace'
      AND `column_name` = 'bridge_rule_id_key'
      AND `data_type` = 'varchar' AND `character_maximum_length` = 22
      AND `character_set_name` = 'utf8mb4' AND `collation_name` = 'utf8mb4_general_ci'
      AND `extra` LIKE '%STORED GENERATED%'
      AND LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        `generation_expression`, '`', ''), ' ', ''), '(', ''), ')', ''), '_utf8mb4', '')) =
        'casewhenbridge_rule_idisnullthen''n:''elseconcat''v:'',bridge_rule_idend'
  )
  AND (SELECT COUNT(*) = 2
              AND MIN(`non_unique`) = 0
              AND COUNT(`sub_part`) = 0
              AND GROUP_CONCAT(`column_name` ORDER BY `seq_in_index` SEPARATOR ',') =
                'trace_id,bridge_rule_id'
       FROM `information_schema`.`statistics`
       WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_bridge_execution_trace'
         AND `index_name` = 'uk_trace_rule'),
  'ALTER TABLE `rule_bridge_execution_trace` DROP INDEX `uk_trace_rule`'
);

CREATE TABLE IF NOT EXISTS `rule_data_bridge`  (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `rule_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规则名称（列表页展示）',
  `rule_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规则业务唯一编码（snowflake）',
  `direction` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '10' COMMENT '桥接方向：10-出站(平台→第三方) / 20-入站(第三方→平台)',
  `data_source_id` bigint NOT NULL COMMENT '关联数据源 FK→rule_data_source.id',
  `match_config_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '匹配条件JSON。出站含productIdentifications/actionTypes/topicFilter/deviceFilter/payloadFilter/timeWindow；入站含subscriptionSourceIds/messageFilter',
  `action_config_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作配置JSON（含 sink 特异参数；EncryptTypeHandler 整体加密落盘，防内联 Bearer token 等泄漏）。出站含payloadTemplate/transformScript/sourceType特异参数；入站含targetHandler/fieldMapping',
  `qos` int NULL DEFAULT NULL COMMENT '规则级可靠性级别覆盖（NULL=用数据源默认）',
  `rate_limit_qps` int NULL DEFAULT NULL COMMENT '规则级 QPS 限流覆盖',
  `retry_max_times` int NULL DEFAULT NULL COMMENT '规则级最大重试次数覆盖',
  `retry_backoff_ms` int NULL DEFAULT NULL COMMENT '规则级初始退避时长覆盖（毫秒）',
  `timeout_ms` int NULL DEFAULT NULL COMMENT '规则级单次发送超时覆盖（毫秒）',
  `dead_letter_data_source_id` bigint NULL DEFAULT NULL COMMENT '规则级死信数据源覆盖',
  `enable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用：0-禁用 / 1-启用（必须测试发送成功后手动启用）',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级（数字越小越先匹配；同事件命中多条时按此排序）',
  `extend_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展参数（兜底，未来加加密/流量分级/A-B 灰度等 0 改表）',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rule_code`(`rule_code` ASC) USING BTREE COMMENT '规则编码全局唯一',
  INDEX `idx_app_direction_enable`(`app_id` ASC, `direction` ASC, `enable` ASC) USING BTREE COMMENT '按应用+方向+状态聚合（matcher 主索引）',
  INDEX `idx_data_source_id`(`data_source_id` ASC) USING BTREE COMMENT '按数据源反查关联规则'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据桥接-规则' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `rule_data_source`  (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `data_source_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '数据源名称（用户起的友好标识，列表页显示）',
  `data_source_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '业务唯一编码（snowflake，外部系统引用）',
  `direction` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '10' COMMENT '方向：10-出站sink / 20-入站source / 30-双向',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '协议类型：KAFKA/REDIS/ROCKETMQ/MYSQL/HTTP/WEBHOOK/MQTT；与 ConnectorType 1:1 对齐',
  `connection_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '连接参数JSON（host/port/topic/database/mode 等；EncryptTypeHandler 整体加密落盘）',
  `credential_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '凭证JSON（密码/密钥/token；EncryptTypeHandler 整体加密落盘）',
  `serialization` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'JSON' COMMENT '序列化策略：JSON/AVRO/STRING/BINARY（与 Serializer.name() 匹配）',
  `default_qos` int NOT NULL DEFAULT 1 COMMENT '默认可靠性级别：0-fire-forget / 1-at-least-once / 2-exactly-once',
  `default_rate_limit_qps` int NOT NULL DEFAULT 0 COMMENT '默认 QPS 限流（0=不限）',
  `default_retry_max_times` int NOT NULL DEFAULT 3 COMMENT '默认最大重试次数（不含首次发送）',
  `default_retry_backoff_ms` int NOT NULL DEFAULT 1000 COMMENT '默认初始退避时长 ms（指数倍增 1s/2s/4s/...）',
  `default_timeout_ms` int NOT NULL DEFAULT 5000 COMMENT '默认单次发送超时 ms',
  `default_dead_letter_data_source_id` bigint NULL DEFAULT NULL COMMENT '默认死信投递的数据源 FK（一般指向告警 Kafka）',
  `enable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用：0-禁用 / 1-启用（必须测试连接成功后手动启用）',
  `health_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'UNKNOWN' COMMENT '健康状态：HEALTHY/DEGRADED/DOWN/UNKNOWN（HealthCheckScheduler 5min 探活更新）',
  `last_health_check_time` datetime NULL DEFAULT NULL COMMENT '上次健康检查时间',
  `extend_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展参数（协议特异调参 JSON：acks/compression/timeout/poolSize 等）',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_data_source_code`(`data_source_code` ASC) USING BTREE COMMENT '业务编码全局唯一',
  INDEX `idx_app_id_direction`(`app_id` ASC, `direction` ASC) USING BTREE COMMENT '按应用+方向查询',
  INDEX `idx_enable`(`enable` ASC) USING BTREE COMMENT '按状态过滤启用项'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据桥接-数据源（出/入站共用）' ROW_FORMAT = DYNAMIC;

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_execution_log' AND `index_name` = 'idx_start_time'),
  'ALTER TABLE `rule_execution_log` ADD INDEX `idx_start_time`(`start_time` ASC) USING BTREE COMMENT ''按执行时间倒序分页 / 时间范围统计'''
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_execution_log' AND `index_name` = 'idx_status_start_time'),
  'ALTER TABLE `rule_execution_log` ADD INDEX `idx_status_start_time`(`status` ASC, `start_time` ASC) USING BTREE COMMENT ''按状态+执行时间筛选执行日志'''
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_execution_log' AND `index_name` = 'idx_rule_start_time'),
  'ALTER TABLE `rule_execution_log` ADD INDEX `idx_rule_start_time`(`rule_identification` ASC, `start_time` ASC) USING BTREE COMMENT ''按规则标识+执行时间筛选执行日志'''
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_execution_log' AND `index_name` = 'idx_org_start_time'),
  'ALTER TABLE `rule_execution_log` ADD INDEX `idx_org_start_time`(`created_org_id` ASC, `start_time` ASC) USING BTREE COMMENT ''按组织数据权限+执行时间查询'''
);

-- 保留旧字段和 idx_only_key，便于核对原始脚本身份；新字段只追加，不覆盖已有人工整理结果。
CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_groovy_script' AND `column_name` = 'script_type'),
  'ALTER TABLE `rule_groovy_script` ADD COLUMN `script_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '''' COMMENT ''脚本类型'' AFTER `app_id`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_groovy_script' AND `column_name` = 'product_identification'),
  'ALTER TABLE `rule_groovy_script` ADD COLUMN `product_identification` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '''' COMMENT ''产品标识'' AFTER `channel_code`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_groovy_script' AND `column_name` = 'topic_pattern'),
  'ALTER TABLE `rule_groovy_script` ADD COLUMN `topic_pattern` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '''' COMMENT ''主题模式'' AFTER `product_identification`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_groovy_script' AND `column_name` = 'deleted'),
  'ALTER TABLE `rule_groovy_script` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0=正常/1=删除)'' AFTER `remark`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 1 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE()
     AND `table_name` = 'rule_groovy_script'
     AND `column_name` = 'object_version'
     AND (`column_type` <> 'varchar(100)' OR `is_nullable` <> 'NO'
       OR `column_default` IS NULL OR `column_default` <> '')),
  'ALTER TABLE `rule_groovy_script` MODIFY COLUMN `object_version` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '''' COMMENT ''版本号'' AFTER `topic_pattern`'
);

-- 可证明映射：旧内置 MQTT/WS 数据协议脚本的 business_identification 在旧代码中明确表示产品标识。
-- topic_pattern 在旧结构中没有等价字段，不能猜测；保持空值并由下面的预检阻止误建唯一索引。
START TRANSACTION;
CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 5 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE()
     AND `table_name` = 'rule_groovy_script'
     AND `column_name` IN ('namespace', 'platform_code', 'product_code', 'business_code', 'business_identification')),
  'UPDATE `rule_groovy_script`
   SET `script_type` = CASE
         WHEN `script_type` = '''' THEN ''topicInboundTransform''
         ELSE `script_type`
       END,
       `product_identification` = CASE
         WHEN `product_identification` = '''' THEN `business_identification`
         ELSE `product_identification`
       END
   WHERE `namespace` = ''system''
     AND `platform_code` = ''iot''
     AND `product_code` = ''dataProtocol''
     AND `business_code` = ''datas''
     AND `channel_code` IN (''mqtt'', ''webSocket'')
     AND `business_identification` <> '''''
);
COMMIT;

-- 预检结果必须都为 0 才创建新唯一索引；不自动删除或合并任何脚本。
SELECT COUNT(*) AS `rule_script_identity_blank_count_should_be_0`
FROM `rule_groovy_script`
WHERE `script_type` = ''
   OR `channel_code` = ''
   OR `product_identification` = ''
   OR `topic_pattern` = ''
   OR `object_version` = '';

SELECT COUNT(*) AS `rule_script_duplicate_group_count_should_be_0`
FROM (
  SELECT 1
  FROM `rule_groovy_script`
  GROUP BY `script_type`, `channel_code`, `product_identification`, `topic_pattern`, `object_version`
  HAVING COUNT(*) > 1
) AS `duplicate_rule_script_identity`;

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'rule_groovy_script'
     AND `index_name` = 'uk_rule_groovy_script_identity')
  AND NOT EXISTS (
    SELECT 1 FROM `rule_groovy_script`
    WHERE `script_type` = '' OR `channel_code` = '' OR `product_identification` = ''
       OR `topic_pattern` = '' OR `object_version` = ''
  )
  AND NOT EXISTS (
    SELECT 1 FROM `rule_groovy_script`
    GROUP BY `script_type`, `channel_code`, `product_identification`, `topic_pattern`, `object_version`
    HAVING COUNT(*) > 1
  ),
  'ALTER TABLE `rule_groovy_script` ADD UNIQUE INDEX `uk_rule_groovy_script_identity`(`script_type` ASC, `channel_code` ASC, `product_identification` ASC, `topic_pattern` ASC, `object_version` ASC) USING BTREE COMMENT ''脚本类型、渠道、产品、主题和版本唯一'''
);

CREATE TABLE IF NOT EXISTS `rule_subscription_source`  (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `source_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '订阅源名称（用户可读）',
  `source_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '业务唯一编码（snowflake；HTTP 入站 endpoint URL 用此值）',
  `data_source_id` bigint NOT NULL COMMENT '复用数据源 FK→rule_data_source.id（direction 须为 20-入站 或 30-双向）',
  `target_handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MQTT_FORWARD' COMMENT '入站后处理方式：MQTT_FORWARD-伪装设备 publish / RAW_INSERT-直接写 DeviceAction / RULE_TRIGGER-触发场景联动',
  `mapping_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字段映射 JSON（如 [{\"sourceField\":\"device_id\",\"targetField\":\"deviceIdentification\"}]）',
  `target_product_identification` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'target_handler=MQTT_FORWARD 时的目标产品标识',
  `target_topic_template` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标 topic 模板（含 ${} 占位符，如 $thing/up/property/${productId}/${deviceId}）',
  `enable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用：0-禁用 / 1-启用（必须测试连接成功后手动启用）',
  `last_consume_offset` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上次消费位点（Kafka offset / MQTT messageId / HTTP 时间戳；重启后接续消费）',
  `extend_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展参数',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_source_code`(`source_code` ASC) USING BTREE COMMENT '编码全局唯一',
  INDEX `idx_app_id_enable`(`app_id` ASC, `enable` ASC) USING BTREE COMMENT '按应用+状态查启用订阅源（启动时扫描用）'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据桥接-订阅源' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_channel`  (
  `id` bigint NOT NULL COMMENT '主键',
  `device_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属设备标识',
  `channel_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通道标识',
  `channel_no` int NULL DEFAULT NULL COMMENT '逻辑通道号',
  `channel_type` smallint NULL DEFAULT NULL COMMENT '通道类型(GB28181行业编码131~143)',
  `channel_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通道名称',
  `stream_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流标识',
  `stream_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流类型',
  `manufacturer` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '厂商',
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '型号',
  `online_status` tinyint(1) NULL DEFAULT 0 COMMENT '在线状态(0=离线/1=在线)',
  `host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通道地址(IP/域名)',
  `port` int NULL DEFAULT NULL COMMENT '端口',
  `password` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '设备口令（EncryptTypeHandler 加密落盘）',
  `longitude` decimal(12, 8) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(12, 8) NULL DEFAULT NULL COMMENT '纬度',
  `full_address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '安装地址',
  `province_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '省级编码',
  `city_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '市级编码',
  `region_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '行政区划编码',
  `has_audio` tinyint(1) NULL DEFAULT 0 COMMENT '支持音频(0=否/1=是)',
  `ptz_type` tinyint NULL DEFAULT NULL COMMENT '云台类型(0=未知/1=球机/2=半球/3=固定枪机/4=遥控枪机)',
  `ptz_capability` tinyint(1) NULL DEFAULT 0 COMMENT '支持云台控制(0=否/1=是)',
  `talk_capability` tinyint(1) NULL DEFAULT 0 COMMENT '支持对讲(0=否/1=是)',
  `secrecy` tinyint NULL DEFAULT 0 COMMENT '保密属性(0=不涉密/1=涉密)',
  `channel_config` json NULL COMMENT '通道专属配置(JSON)',
  `extend_params` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展参数',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '修改人',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_channel_identification`(`channel_identification` ASC) USING BTREE,
  INDEX `idx_device_identification`(`device_identification` ASC) USING BTREE,
  INDEX `idx_online_status`(`online_status` ASC) USING BTREE,
  INDEX `idx_stream_identification`(`stream_identification` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统一通道表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_device`  (
  `id` bigint NOT NULL COMMENT '主键',
  `device_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备标识',
  `access_protocol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备接入协议(GB28181/ONVIF/ISUP/JT1078/SIP/PELCO_D/PELCO_P)',
  `device_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备名称',
  `custom_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自定义名称',
  `manufacturer` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '厂商',
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '型号',
  `firmware` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '固件版本',
  `host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备地址(IP/域名)',
  `port` int NULL DEFAULT NULL COMMENT '端口',
  `wan_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公网地址(IP/域名)',
  `lan_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '局域网地址(IP/域名)',
  `access_endpoint` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '完整访问端点(host:port)',
  `sdp_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收流地址(IP/域名)',
  `local_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '本地SIP交互地址(IP/域名)',
  `transport` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '传输协议(UDP/TCP)',
  `stream_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据流传输模式',
  `online_status` tinyint(1) NULL DEFAULT 0 COMMENT '是否在线(0=离线/1=在线)',
  `register_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '注册时间',
  `last_keepalive_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后心跳时间',
  `expires` int NULL DEFAULT NULL COMMENT '注册有效期(秒)',
  `keepalive_interval` int NULL DEFAULT NULL COMMENT '心跳间隔(秒)',
  `keepalive_timeout_count` int NULL DEFAULT NULL COMMENT '心跳超时次数',
  `auth_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '认证方式(PASSWORD/VALIDATE_CODE/AUTH_TOKEN/CERTIFICATE/DIGEST/NONE)',
  `auth_secret` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '认证密钥(加密存储)',
  `media_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '媒体服务唯一标识',
  `channel_count` int NULL DEFAULT 0 COMMENT '通道数量',
  `ability` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备能力集描述',
  `protocol_config` json NULL COMMENT '协议专属配置(JSON)',
  `extend_params` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展参数',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '修改人',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_identification`(`device_identification` ASC) USING BTREE,
  INDEX `idx_access_protocol`(`access_protocol` ASC) USING BTREE,
  INDEX `idx_online_status`(`online_status` ASC) USING BTREE,
  INDEX `idx_media_identification`(`media_identification` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统一设备表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_device_alarm`  (
  `id` bigint NOT NULL COMMENT '主键',
  `device_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备国标编号',
  `channel_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通道国标编号',
  `alarm_priority` tinyint NULL DEFAULT NULL COMMENT '告警级别(1=一级警情/2=二级警情/3=三级警情/4=四级警情)',
  `alarm_method` tinyint NULL DEFAULT NULL COMMENT '告警方式',
  `alarm_time` datetime NULL DEFAULT NULL COMMENT '告警时间（设备上报时间）',
  `alarm_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警描述',
  `alarm_type` tinyint NULL DEFAULT NULL COMMENT '告警类型',
  `alarm_type_param` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警类型参数(JSON)',
  `longitude` double NULL DEFAULT NULL COMMENT '经度',
  `latitude` double NULL DEFAULT NULL COMMENT '纬度',
  `handle_status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态(0=未处理/1=已确认/2=已消除/3=已忽略)',
  `handle_user_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `handle_result` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理结果/备注',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_identification`(`device_identification` ASC) USING BTREE,
  INDEX `idx_alarm_time`(`alarm_time` ASC) USING BTREE,
  INDEX `idx_handle_status`(`handle_status` ASC) USING BTREE,
  INDEX `idx_alarm_priority`(`alarm_priority` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频设备告警表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_device_group`  (
  `id` bigint NOT NULL COMMENT '主键',
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组名称',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '上级分组ID(顶层为空)',
  `group_type` tinyint NOT NULL DEFAULT 0 COMMENT '分组类型(0=自定义分组/1=行政区划/2=业务分组)',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `group_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '层级路径(如: /1/2/3，便于快速查子孙)',
  `group_level` int NOT NULL DEFAULT 1 COMMENT '层级深度(从1开始)',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标标识',
  `enable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '启用状态(0=禁用/1=启用)',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '扩展参数(JSON)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_group_path`(`group_path`(191) ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_enable`(`enable` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频设备分组表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_device_group_relation`  (
  `id` bigint NOT NULL COMMENT '主键',
  `group_id` bigint NOT NULL COMMENT '分组ID',
  `device_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备国标编号',
  `channel_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通道国标编号(空表示设备级别关联)',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '分组内排序序号',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '扩展参数(JSON)',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `channel_identification_key` varchar(52) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci GENERATED ALWAYS AS (CASE WHEN `deleted` <> 0 THEN CONCAT('D:', `id`) WHEN NULLIF(TRIM(`channel_identification`), '') IS NULL THEN 'N:' ELSE CONCAT('V:', TRIM(`channel_identification`)) END) STORED COMMENT '关联唯一键归一值',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_device_channel_key`(`group_id` ASC, `device_identification` ASC, `channel_identification_key` ASC) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_device_identification`(`device_identification` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频设备分组关联表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_device_mobile_position`  (
  `id` bigint NOT NULL COMMENT '主键',
  `device_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备国标编号',
  `channel_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通道国标编号',
  `longitude` double NULL DEFAULT NULL COMMENT '经度',
  `latitude` double NULL DEFAULT NULL COMMENT '纬度',
  `altitude` double NULL DEFAULT NULL COMMENT '海拔高度(米)',
  `speed` double NULL DEFAULT NULL COMMENT '速度(km/h)',
  `direction` double NULL DEFAULT NULL COMMENT '方向角(度，正北为0，顺时针)',
  `report_time` datetime NULL DEFAULT NULL COMMENT '位置上报时间',
  `geo_coord_sys` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'WGS84' COMMENT '坐标系(WGS84/GCJ02/BD09)',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_identification`(`device_identification` ASC) USING BTREE,
  INDEX `idx_report_time`(`report_time` ASC) USING BTREE,
  INDEX `idx_device_time`(`device_identification` ASC, `report_time` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频设备移动位置表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_gateway_mapping`  (
  `id` bigint NOT NULL COMMENT '主键',
  `src_protocol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '源协议(JT1078/ISUP等)',
  `src_device_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '源设备标识',
  `src_channel_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '源通道标识',
  `gb_device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '映射GB28181设备编号',
  `gb_channel_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '映射GB28181通道编号',
  `gb_platform_id` bigint NULL DEFAULT NULL COMMENT '目标上级平台ID',
  `enable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用(0=禁用/1=启用)',
  `auto_push` tinyint(1) NOT NULL DEFAULT 0 COMMENT '自动推流(0=否/1=是)',
  `mapping_config` json NULL COMMENT '映射配置(JSON)',
  `register_status` tinyint(1) NULL DEFAULT 0 COMMENT '注册状态(0=未注册/1=已注册)',
  `last_register_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后注册时间',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `src_channel_identification_key` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci GENERATED ALWAYS AS (CASE WHEN `deleted` <> 0 THEN CONCAT('D:', `id`) WHEN NULLIF(TRIM(`src_channel_identification`), '') IS NULL THEN 'N:' ELSE CONCAT('V:', TRIM(`src_channel_identification`)) END) STORED COMMENT '映射唯一键归一值',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '修改人',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_src_device_channel_key`(`src_protocol` ASC, `src_device_identification` ASC, `src_channel_identification_key` ASC) USING BTREE,
  INDEX `idx_gb_device_id`(`gb_device_id` ASC) USING BTREE,
  INDEX `idx_gb_channel_id`(`gb_channel_id` ASC) USING BTREE,
  INDEX `idx_gb_platform_id`(`gb_platform_id` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '网关协议映射表' ROW_FORMAT = DYNAMIC;

-- 活动记录的可空通道标识先归一成非空内部键再参与唯一约束；删除记录使用主键隔离，允许解绑后重绑。
-- 预检只统计活动记录，脚本不会自动删除或合并重复数据。
SELECT COUNT(*) AS `video_group_relation_normalized_duplicate_group_count_should_be_0`
FROM (
  SELECT 1
  FROM `video_device_group_relation`
  WHERE `deleted` = 0
  GROUP BY `group_id`, `device_identification`,
    CASE
      WHEN NULLIF(TRIM(`channel_identification`), '') IS NULL THEN 'N:'
      ELSE CONCAT('V:', TRIM(`channel_identification`))
    END
  HAVING COUNT(*) > 1
) AS `duplicate_video_group_relation_identity`;

SELECT COUNT(*) AS `video_gateway_mapping_normalized_duplicate_group_count_should_be_0`
FROM (
  SELECT 1
  FROM `video_gateway_mapping`
  WHERE `deleted` = 0
  GROUP BY `src_protocol`, `src_device_identification`,
    CASE
      WHEN NULLIF(TRIM(`src_channel_identification`), '') IS NULL THEN 'N:'
      ELSE CONCAT('V:', TRIM(`src_channel_identification`))
    END
  HAVING COUNT(*) > 1
) AS `duplicate_video_gateway_mapping_identity`;

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_device_group_relation'
     AND `column_name` = 'channel_identification_key'),
  'ALTER TABLE `video_device_group_relation`
   ADD COLUMN `channel_identification_key` varchar(52) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci
   GENERATED ALWAYS AS (CASE WHEN `deleted` <> 0 THEN CONCAT(''D:'', `id`)
     WHEN NULLIF(TRIM(`channel_identification`), '''') IS NULL
     THEN ''N:'' ELSE CONCAT(''V:'', TRIM(`channel_identification`)) END) STORED
   COMMENT ''关联唯一键归一值'' AFTER `deleted`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_gateway_mapping'
     AND `column_name` = 'src_channel_identification_key'),
  'ALTER TABLE `video_gateway_mapping`
   ADD COLUMN `src_channel_identification_key` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci
   GENERATED ALWAYS AS (CASE WHEN `deleted` <> 0 THEN CONCAT(''D:'', `id`)
     WHEN NULLIF(TRIM(`src_channel_identification`), '''') IS NULL
     THEN ''N:'' ELSE CONCAT(''V:'', TRIM(`src_channel_identification`)) END) STORED
   COMMENT ''映射唯一键归一值'' AFTER `deleted`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_device_group_relation'
     AND `index_name` = 'uk_group_device_channel_key')
  AND EXISTS (
    SELECT 1 FROM `information_schema`.`columns`
    WHERE `table_schema` = DATABASE() AND `table_name` = 'video_device_group_relation'
      AND `column_name` = 'channel_identification_key'
      AND `data_type` = 'varchar' AND `character_maximum_length` = 52
      AND `character_set_name` = 'utf8mb4' AND `collation_name` = 'utf8mb4_general_ci'
      AND `extra` LIKE '%STORED GENERATED%'
      AND LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        `generation_expression`, '`', ''), ' ', ''), '(', ''), ')', ''), '_utf8mb4', '')) =
        'casewhendeleted<>0thenconcat''d:'',idwhennulliftrimchannel_identification,''''isnullthen''n:''elseconcat''v:'',trimchannel_identificationend'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `video_device_group_relation`
    WHERE `deleted` = 0
    GROUP BY `group_id`, `device_identification`,
      CASE
        WHEN NULLIF(TRIM(`channel_identification`), '') IS NULL THEN 'N:'
        ELSE CONCAT('V:', TRIM(`channel_identification`))
      END
    HAVING COUNT(*) > 1
  ),
  'ALTER TABLE `video_device_group_relation`
   ADD UNIQUE INDEX `uk_group_device_channel_key`
   (`group_id` ASC, `device_identification` ASC, `channel_identification_key` ASC) USING BTREE'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_gateway_mapping'
     AND `index_name` = 'uk_src_device_channel_key')
  AND EXISTS (
    SELECT 1 FROM `information_schema`.`columns`
    WHERE `table_schema` = DATABASE() AND `table_name` = 'video_gateway_mapping'
      AND `column_name` = 'src_channel_identification_key'
      AND `data_type` = 'varchar' AND `character_maximum_length` = 66
      AND `character_set_name` = 'utf8mb4' AND `collation_name` = 'utf8mb4_general_ci'
      AND `extra` LIKE '%STORED GENERATED%'
      AND LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        `generation_expression`, '`', ''), ' ', ''), '(', ''), ')', ''), '_utf8mb4', '')) =
        'casewhendeleted<>0thenconcat''d:'',idwhennulliftrimsrc_channel_identification,''''isnullthen''n:''elseconcat''v:'',trimsrc_channel_identificationend'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `video_gateway_mapping`
    WHERE `deleted` = 0
    GROUP BY `src_protocol`, `src_device_identification`,
      CASE
        WHEN NULLIF(TRIM(`src_channel_identification`), '') IS NULL THEN 'N:'
        ELSE CONCAT('V:', TRIM(`src_channel_identification`))
      END
    HAVING COUNT(*) > 1
  ),
  'ALTER TABLE `video_gateway_mapping`
   ADD UNIQUE INDEX `uk_src_device_channel_key`
   (`src_protocol` ASC, `src_device_identification` ASC, `src_channel_identification_key` ASC) USING BTREE'
);

-- 仅在替代索引已经存在时移除旧索引，预检未通过时继续保留旧约束。
CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 3
          AND MIN(`non_unique`) = 0
          AND COUNT(`sub_part`) = 0
          AND GROUP_CONCAT(`column_name` ORDER BY `seq_in_index` SEPARATOR ',') =
            'group_id,device_identification,channel_identification_key'
   FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_device_group_relation'
     AND `index_name` = 'uk_group_device_channel_key')
  AND EXISTS (
    SELECT 1 FROM `information_schema`.`columns`
    WHERE `table_schema` = DATABASE() AND `table_name` = 'video_device_group_relation'
      AND `column_name` = 'channel_identification_key'
      AND `data_type` = 'varchar' AND `character_maximum_length` = 52
      AND `character_set_name` = 'utf8mb4' AND `collation_name` = 'utf8mb4_general_ci'
      AND `extra` LIKE '%STORED GENERATED%'
      AND LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        `generation_expression`, '`', ''), ' ', ''), '(', ''), ')', ''), '_utf8mb4', '')) =
        'casewhendeleted<>0thenconcat''d:'',idwhennulliftrimchannel_identification,''''isnullthen''n:''elseconcat''v:'',trimchannel_identificationend'
  )
  AND (SELECT COUNT(*) = 3
              AND MIN(`non_unique`) = 0
              AND COUNT(`sub_part`) = 0
              AND GROUP_CONCAT(`column_name` ORDER BY `seq_in_index` SEPARATOR ',') =
                'group_id,device_identification,channel_identification'
       FROM `information_schema`.`statistics`
       WHERE `table_schema` = DATABASE() AND `table_name` = 'video_device_group_relation'
         AND `index_name` = 'uk_group_device_channel'),
  'ALTER TABLE `video_device_group_relation` DROP INDEX `uk_group_device_channel`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 3
          AND MIN(`non_unique`) = 0
          AND COUNT(`sub_part`) = 0
          AND GROUP_CONCAT(`column_name` ORDER BY `seq_in_index` SEPARATOR ',') =
            'src_protocol,src_device_identification,src_channel_identification_key'
   FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_gateway_mapping'
     AND `index_name` = 'uk_src_device_channel_key')
  AND EXISTS (
    SELECT 1 FROM `information_schema`.`columns`
    WHERE `table_schema` = DATABASE() AND `table_name` = 'video_gateway_mapping'
      AND `column_name` = 'src_channel_identification_key'
      AND `data_type` = 'varchar' AND `character_maximum_length` = 66
      AND `character_set_name` = 'utf8mb4' AND `collation_name` = 'utf8mb4_general_ci'
      AND `extra` LIKE '%STORED GENERATED%'
      AND LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        `generation_expression`, '`', ''), ' ', ''), '(', ''), ')', ''), '_utf8mb4', '')) =
        'casewhendeleted<>0thenconcat''d:'',idwhennulliftrimsrc_channel_identification,''''isnullthen''n:''elseconcat''v:'',trimsrc_channel_identificationend'
  )
  AND (SELECT COUNT(*) = 3
              AND MIN(`non_unique`) = 0
              AND COUNT(`sub_part`) = 0
              AND GROUP_CONCAT(`column_name` ORDER BY `seq_in_index` SEPARATOR ',') =
                'src_protocol,src_device_identification,src_channel_identification'
       FROM `information_schema`.`statistics`
       WHERE `table_schema` = DATABASE() AND `table_name` = 'video_gateway_mapping'
         AND `index_name` = 'uk_src_device_channel'),
  'ALTER TABLE `video_gateway_mapping` DROP INDEX `uk_src_device_channel`'
);

-- 新地址列与旧 IP 列并行保留；重跑只补缺列，不改变已人工维护的新值。
CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'host'),
  'ALTER TABLE `video_media_server` ADD COLUMN `host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''服务器地址(IP/域名)'' AFTER `media_identification`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'hook_host'),
  'ALTER TABLE `video_media_server` ADD COLUMN `hook_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''Hook回调地址(IP/域名)'' AFTER `host`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'sdp_host'),
  'ALTER TABLE `video_media_server` ADD COLUMN `sdp_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''SDP地址(IP/域名)'' AFTER `hook_host`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'stream_host'),
  'ALTER TABLE `video_media_server` ADD COLUMN `stream_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''流播放地址(IP/域名)'' AFTER `sdp_host`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'version'),
  'ALTER TABLE `video_media_server` ADD COLUMN `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''服务器版本号'' AFTER `remark`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'capabilities'),
  'ALTER TABLE `video_media_server` ADD COLUMN `capabilities` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT ''服务器能力集(JSON，标记支持哪些API)'' AFTER `version`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'max_streams'),
  'ALTER TABLE `video_media_server` ADD COLUMN `max_streams` int NULL DEFAULT NULL COMMENT ''最大承载流数量(用于负载均衡)'' AFTER `capabilities`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'current_streams'),
  'ALTER TABLE `video_media_server` ADD COLUMN `current_streams` int NULL DEFAULT 0 COMMENT ''当前流数量'' AFTER `max_streams`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'cpu_usage'),
  'ALTER TABLE `video_media_server` ADD COLUMN `cpu_usage` decimal(5, 2) NULL DEFAULT NULL COMMENT ''CPU使用率(心跳上报)'' AFTER `current_streams`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'memory_usage'),
  'ALTER TABLE `video_media_server` ADD COLUMN `memory_usage` decimal(5, 2) NULL DEFAULT NULL COMMENT ''内存使用率(心跳上报)'' AFTER `cpu_usage`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'network_in_speed'),
  'ALTER TABLE `video_media_server` ADD COLUMN `network_in_speed` bigint NULL DEFAULT NULL COMMENT ''入网速率bytes/s(心跳上报)'' AFTER `memory_usage`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'network_out_speed'),
  'ALTER TABLE `video_media_server` ADD COLUMN `network_out_speed` bigint NULL DEFAULT NULL COMMENT ''出网速率bytes/s(心跳上报)'' AFTER `network_in_speed`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server' AND `column_name` = 'deleted'),
  'ALTER TABLE `video_media_server` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0=正常/1=删除)'' AFTER `network_out_speed`'
);

-- 地址字段是同义扩容，可直接无损回填；新列已有值时不覆盖，旧列继续保留。
START TRANSACTION;
CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 4 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server'
     AND `column_name` IN ('ip', 'hook_ip', 'sdp_ip', 'stream_ip')),
  'UPDATE `video_media_server`
   SET `host` = CASE WHEN `host` IS NULL OR `host` = '''' THEN `ip` ELSE `host` END,
       `hook_host` = CASE WHEN `hook_host` IS NULL OR `hook_host` = '''' THEN `hook_ip` ELSE `hook_host` END,
       `sdp_host` = CASE WHEN `sdp_host` IS NULL OR `sdp_host` = '''' THEN `sdp_ip` ELSE `sdp_host` END,
       `stream_host` = CASE WHEN `stream_host` IS NULL OR `stream_host` = '''' THEN `stream_ip` ELSE `stream_host` END'
);
COMMIT;

-- 预检结果必须都为 0 才创建 host + http_port 唯一索引；旧 ip 唯一索引仍保留。
SELECT COUNT(*) AS `media_server_empty_unique_key_count_should_be_0`
FROM `video_media_server`
WHERE `host` IS NULL OR TRIM(`host`) = '' OR `http_port` IS NULL;

SELECT COUNT(*) AS `media_server_duplicate_group_count_should_be_0`
FROM (
  SELECT 1
  FROM `video_media_server`
  WHERE `host` IS NOT NULL AND TRIM(`host`) <> '' AND `http_port` IS NOT NULL
  GROUP BY `host`, `http_port`
  HAVING COUNT(*) > 1
) AS `duplicate_media_server_identity`;

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_media_server'
     AND `index_name` = 'uk_media_server_unique_host_http_port')
  AND NOT EXISTS (
    SELECT 1 FROM `video_media_server`
    WHERE `host` IS NULL OR TRIM(`host`) = '' OR `http_port` IS NULL
  )
  AND NOT EXISTS (
    SELECT 1 FROM `video_media_server`
    WHERE `host` IS NOT NULL AND TRIM(`host`) <> '' AND `http_port` IS NOT NULL
    GROUP BY `host`, `http_port`
    HAVING COUNT(*) > 1
  ),
  'ALTER TABLE `video_media_server` ADD UNIQUE INDEX `uk_media_server_unique_host_http_port`(`host` ASC, `http_port` ASC) USING BTREE COMMENT ''唯一地址和HTTP端口组合'''
);

CREATE TABLE IF NOT EXISTS `video_notify_subscription`  (
  `id` bigint NOT NULL COMMENT '主键',
  `subscription_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订阅名称',
  `channel_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '渠道类型(字典 NOTIFY_CHANNEL_TYPE)',
  `channel_config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '渠道凭证JSON（EncryptTypeHandler 整体加密落盘）',
  `template_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息模板编码(ExtendMsgTemplate.code)',
  `event_types` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订阅事件类型(逗号分隔)',
  `priority_filter` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警级别过滤(逗号分隔,空=全部)',
  `recipient_scope` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SELF' COMMENT '接收范围: SELF/ORG/CUSTOM',
  `recipient_ids` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接收人用户ID(逗号分隔)',
  `at_all` tinyint NOT NULL DEFAULT 0 COMMENT '@所有人(0=否/1=是)',
  `jump_url_template` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跳转链接模板',
  `msg_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容模板(支持${变量})',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0=禁用/1=启用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_event_types`(`event_types`(100) ASC) USING BTREE,
  INDEX `idx_channel_type`(`channel_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频事件通知订阅配置' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_platform`  (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '平台名称',
  `enable` tinyint(1) NULL DEFAULT 1 COMMENT '启用状态(0=禁用/1=启用)',
  `server_gb_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '平台国标编号',
  `server_gb_domain` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '平台国标域',
  `server_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '上级SIP服务IP/域名',
  `server_port` int NULL DEFAULT NULL COMMENT '平台SIP端口',
  `device_gb_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '本平台在上级的设备编号',
  `device_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '本机设备IP/域名',
  `device_port` int NULL DEFAULT NULL COMMENT '本平台SIP端口',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '认证用户名',
  `password` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '认证密码（EncryptTypeHandler 加密落盘）',
  `expires` int NULL DEFAULT 3600 COMMENT '注册有效期(秒)',
  `keep_timeout` int NULL DEFAULT 60 COMMENT '心跳超时(秒)',
  `transport` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'UDP' COMMENT '传输协议(UDP/TCP)',
  `character_set` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'GB2312' COMMENT '字符集(GB2312/UTF-8)',
  `ptz` tinyint(1) NULL DEFAULT 0 COMMENT '是否支持PTZ',
  `rtcp` tinyint(1) NULL DEFAULT 0 COMMENT '是否支持RTCP',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '注册状态(0=未注册/1=已注册)',
  `catalog_subscribe` tinyint(1) NULL DEFAULT 0 COMMENT '订阅目录变化',
  `alarm_subscribe` tinyint(1) NULL DEFAULT 0 COMMENT '订阅告警',
  `mobile_position_subscribe` tinyint(1) NULL DEFAULT 0 COMMENT '订阅位置',
  `catalog_group` int NULL DEFAULT 1 COMMENT '目录分组大小',
  `as_message_channel` tinyint(1) NULL DEFAULT 0 COMMENT '作为消息通道',
  `send_stream_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '推流IP/域名',
  `auto_push_channel` tinyint(1) NULL DEFAULT 0 COMMENT '自动推送通道',
  `catalog_with_platform` int NULL DEFAULT 0 COMMENT '目录包含平台',
  `catalog_with_group` int NULL DEFAULT 0 COMMENT '目录包含分组',
  `catalog_with_region` int NULL DEFAULT 0 COMMENT '目录包含区域',
  `civil_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '行政区划编码',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '厂商',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '型号',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `register_way` tinyint NULL DEFAULT 1 COMMENT '注册方式(1=符合标准)',
  `secrecy` tinyint NULL DEFAULT 0 COMMENT '保密属性(0=不涉密/1=涉密)',
  `server_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务器ID',
  `cascade_type` tinyint NULL DEFAULT 0 COMMENT '级联类型(0=作为下级/1=作为上级)',
  `gb_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'GB28181协议版本',
  `online_status` tinyint(1) NULL DEFAULT 0 COMMENT '在线状态(0=离线/1=在线)',
  `register_expires` int NULL DEFAULT 3600 COMMENT '注册有效期(秒)',
  `keepalive_interval` int NULL DEFAULT 60 COMMENT '心跳间隔(秒)',
  `keepalive_timeout_count` int NULL DEFAULT 3 COMMENT '心跳超时次数',
  `last_register_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最近注册时间',
  `last_keepalive_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最近心跳时间',
  `start_offline_push` tinyint(1) NULL DEFAULT 0 COMMENT '推送离线通道',
  `sip_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT 'SIP服务IP/域名',
  `sip_port` int NULL DEFAULT NULL COMMENT 'SIP监听端口',
  `hook_url_prefix` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Hook URL前缀',
  `service_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务实例ID',
  `cascade_sdp_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '级联SDP IP/域名',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_server_gb_id`(`server_gb_id` ASC) USING BTREE,
  INDEX `idx_online_status`(`online_status` ASC) USING BTREE,
  INDEX `idx_enable`(`enable` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频级联平台表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_platform_catalog`  (
  `id` bigint NOT NULL COMMENT '主键',
  `platform_id` bigint NOT NULL COMMENT '级联平台ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目录名称',
  `gb_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目录国标编号',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '上级目录ID(顶层为空)',
  `catalog_type` tinyint NULL DEFAULT 0 COMMENT '目录类型(0=行政区划/1=业务分组/2=虚拟组织)',
  `civil_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '行政区划编码',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_platform_id`(`platform_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频级联目录表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_platform_channel`  (
  `id` bigint NOT NULL COMMENT '主键',
  `platform_id` bigint NOT NULL COMMENT '级联平台ID',
  `device_channel_id` bigint NULL DEFAULT NULL COMMENT '设备通道表ID',
  `catalog_id` bigint NULL DEFAULT NULL COMMENT '所属目录ID',
  `device_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备国标编号',
  `channel_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通道国标编号',
  `custom_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自定义名称',
  `custom_gb_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自定义国标编号',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_platform_id`(`platform_id` ASC) USING BTREE,
  INDEX `idx_device_identification`(`device_identification` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频级联平台通道关联表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_record_file`  (
  `id` bigint NOT NULL COMMENT '主键',
  `plan_id` bigint NULL DEFAULT NULL COMMENT '关联录像计划ID(手动录制时为空)',
  `device_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备国标编号',
  `channel_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通道国标编号',
  `stream_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流标识',
  `app` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '应用名',
  `media_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流媒体服务器标识',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名',
  `file_id` bigint NULL DEFAULT NULL COMMENT '文件ID(关联base服务File表)',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  `file_format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'mp4' COMMENT '文件格式(mp4/flv/ts)',
  `duration` int NOT NULL DEFAULT 0 COMMENT '时长(秒)',
  `start_time` datetime NULL DEFAULT NULL COMMENT '录像开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '录像结束时间',
  `thumbnail_file_id` bigint NULL DEFAULT NULL COMMENT '缩略图文件ID(关联base服务File表)',
  `file_status` tinyint NOT NULL DEFAULT 0 COMMENT '文件状态(0=录制中/1=已完成/2=已过期/3=已删除)',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '扩展参数(JSON)',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE,
  INDEX `idx_device_channel`(`device_identification` ASC, `channel_identification` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_end_time`(`end_time` ASC) USING BTREE,
  INDEX `idx_file_id`(`file_id` ASC) USING BTREE,
  INDEX `idx_file_status`(`file_status` ASC) USING BTREE,
  INDEX `idx_media_identification`(`media_identification` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频录像文件表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_record_plan`  (
  `id` bigint NOT NULL COMMENT '主键',
  `plan_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '计划名称',
  `plan_type` tinyint NOT NULL DEFAULT 0 COMMENT '计划类型(0=设备录像/1=云端录像)',
  `media_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '指定流媒体服务器标识(空则自动分配)',
  `record_format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'mp4' COMMENT '录像格式(mp4/flv/ts)',
  `segment_duration` int NOT NULL DEFAULT 3600 COMMENT '分段时长(秒，默认1小时)',
  `retention_days` int NOT NULL DEFAULT 30 COMMENT '保留天数(超期自动清理)',
  `storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '存储路径(空则用默认路径)',
  `plan_status` tinyint NOT NULL DEFAULT 0 COMMENT '计划状态(0=停用/1=启用)',
  `schedule_rule` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '调度规则(JSON，支持周期/CRON/一次性)',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '扩展参数(JSON)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_status`(`plan_status` ASC) USING BTREE,
  INDEX `idx_plan_type`(`plan_type` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频录像计划表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `video_sip_config`  (
  `id` bigint NOT NULL COMMENT '主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `sip_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '国标编号(SIP ID)',
  `sip_domain` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'SIP域',
  `sip_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'SIP认证密码(AES加密)',
  `sip_server_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'SIP接入地址(域名或IP)',
  `bind_ip` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定IP(多网卡隔离,逗号分隔)',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认(1=是)',
  `register_interval` int NULL DEFAULT NULL COMMENT '注册有效期(秒)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0=禁用/1=启用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint NULL DEFAULT NULL COMMENT '创建组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sip_id`(`sip_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_org_id`(`created_org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '租户SIP服务配置' ROW_FORMAT = DYNAMIC;

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_stream_proxy' AND `column_name` = 'pull_retry_count'),
  'ALTER TABLE `video_stream_proxy` ADD COLUMN `pull_retry_count` int NULL DEFAULT 0 COMMENT ''拉流重试次数'' AFTER `remark`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_stream_proxy' AND `column_name` = 'max_retry_count'),
  'ALTER TABLE `video_stream_proxy` ADD COLUMN `max_retry_count` int NULL DEFAULT 3 COMMENT ''最大重试次数'' AFTER `pull_retry_count`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_stream_proxy' AND `column_name` = 'last_pull_time'),
  'ALTER TABLE `video_stream_proxy` ADD COLUMN `last_pull_time` datetime NULL DEFAULT NULL COMMENT ''最近拉流时间'' AFTER `max_retry_count`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_stream_proxy' AND `column_name` = 'last_error'),
  'ALTER TABLE `video_stream_proxy` ADD COLUMN `last_error` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''最近错误信息'' AFTER `last_pull_time`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_stream_proxy' AND `column_name` = 'deleted'),
  'ALTER TABLE `video_stream_proxy` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0=正常/1=删除)'' AFTER `last_error`'
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 0 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_stream_push' AND `column_name` = 'deleted'),
  'ALTER TABLE `video_stream_push` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0=正常/1=删除)'' AFTER `remark`'
);

-- 加密后的字符串会比原文更长；以下修改只扩容并补充字段语义，不改写已有凭据值。
CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 1 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_channel' AND `column_name` = 'password'
     AND (`data_type` <> 'text' OR `column_comment` <> '设备口令（EncryptTypeHandler 加密落盘）')),
  'ALTER TABLE `video_channel` MODIFY COLUMN `password` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT ''设备口令（EncryptTypeHandler 加密落盘）'''
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 1 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_notify_subscription' AND `column_name` = 'channel_config'
     AND (`data_type` <> 'longtext' OR `column_comment` <> '渠道凭证JSON（EncryptTypeHandler 整体加密落盘）')),
  'ALTER TABLE `video_notify_subscription` MODIFY COLUMN `channel_config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT ''渠道凭证JSON（EncryptTypeHandler 整体加密落盘）'''
);

CALL `thinglinks_v140_exec_if`(
  (SELECT COUNT(*) = 1 FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE() AND `table_name` = 'video_platform' AND `column_name` = 'password'
     AND (`data_type` <> 'text' OR `column_comment` <> '认证密码（EncryptTypeHandler 加密落盘）')),
  'ALTER TABLE `video_platform` MODIFY COLUMN `password` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT ''认证密码（EncryptTypeHandler 加密落盘）'''
);

-- empowerment_record、video_device_channel、video_device_info 保存历史业务数据。
-- 新旧视频模型没有完整、可证明的一一映射，本版本不删除、不搬迁，也不改写其中的删除状态或定制数据。

-- 验证：两个结果应分别为 23 和 2。
SELECT COUNT(*) AS `new_table_count_should_be_23`
FROM `information_schema`.`tables`
WHERE `table_schema` = DATABASE()
  AND `table_name` IN (
    'ca_cert_audit_log',
    'product_publish_record',
    'product_version',
    'product_version_change_log',
    'rule_bridge_execution_step',
    'rule_bridge_execution_trace',
    'rule_data_bridge',
    'rule_data_source',
    'rule_subscription_source',
    'video_channel',
    'video_device',
    'video_device_alarm',
    'video_device_group',
    'video_device_group_relation',
    'video_device_mobile_position',
    'video_gateway_mapping',
    'video_notify_subscription',
    'video_platform',
    'video_platform_catalog',
    'video_platform_channel',
    'video_record_file',
    'video_record_plan',
    'video_sip_config'
  );

SELECT COUNT(*) AS `device_version_column_count_should_be_2`
FROM `information_schema`.`columns`
WHERE `table_schema` = DATABASE()
  AND `table_name` = 'device'
  AND `column_name` IN ('last_status_event_hlc', 'bound_product_version_no');

-- 产品版本回填：首项应为 0；次项保留所有超长旧值，待人工确定新编码后再处理。
SELECT COUNT(*) AS `product_version_backfill_mismatch_count_should_be_0`
FROM `product`
WHERE `product_version` IS NOT NULL
  AND `product_version` <> ''
  AND CHAR_LENGTH(`product_version`) <= 64
  AND (`active_version_no` IS NULL OR `active_version_no` <> `product_version`);

SELECT COUNT(*) AS `product_version_too_long_manual_review_count`
FROM `product`
WHERE `product_version` IS NOT NULL
  AND CHAR_LENGTH(`product_version`) > 64;

-- 旧字段与旧表均应继续存在；结果分别应为 10 和 3。
SELECT COUNT(*) AS `legacy_column_count_should_be_10`
FROM `information_schema`.`columns`
WHERE `table_schema` = DATABASE()
  AND (
    (`table_name` = 'product' AND `column_name` = 'product_version')
    OR (`table_name` = 'rule_groovy_script' AND `column_name` IN
      ('namespace', 'platform_code', 'product_code', 'business_code', 'business_identification'))
    OR (`table_name` = 'video_media_server' AND `column_name` IN
      ('ip', 'hook_ip', 'sdp_ip', 'stream_ip'))
  );

SELECT COUNT(*) AS `legacy_table_count_should_be_3`
FROM `information_schema`.`tables`
WHERE `table_schema` = DATABASE()
  AND `table_name` IN ('empowerment_record', 'video_device_channel', 'video_device_info');

-- 三个凭据字段的容量与类型；结果应为 3。
SELECT COUNT(*) AS `video_encrypted_credential_column_count_should_be_3`
FROM `information_schema`.`columns`
WHERE `table_schema` = DATABASE()
  AND (
    (`table_name` = 'video_channel' AND `column_name` = 'password' AND `data_type` = 'text')
    OR (`table_name` = 'video_notify_subscription' AND `column_name` = 'channel_config' AND `data_type` = 'longtext')
    OR (`table_name` = 'video_platform' AND `column_name` = 'password' AND `data_type` = 'text')
  );

-- 旧明文凭据兼容读取，但仍需通过新版接口重新保存或重置；完成后结果应为 0。
SELECT
  (SELECT COUNT(*) FROM `video_channel`
   WHERE `password` IS NOT NULL AND TRIM(`password`) <> ''
     AND `password` NOT REGEXP '^ENC@[A-Za-z0-9+/]+={0,2}$')
  +
  (SELECT COUNT(*) FROM `video_notify_subscription`
   WHERE `channel_config` IS NOT NULL AND TRIM(`channel_config`) <> ''
     AND `channel_config` NOT REGEXP '^ENC@[A-Za-z0-9+/]+={0,2}$')
  +
  (SELECT COUNT(*) FROM `video_platform`
   WHERE `password` IS NOT NULL AND TRIM(`password`) <> ''
     AND `password` NOT REGEXP '^ENC@[A-Za-z0-9+/]+={0,2}$')
  AS `video_plaintext_credential_count_manual_migration`;

-- 回填一致性；两项都应为 0。
SELECT COUNT(*) AS `media_server_address_backfill_mismatch_count_should_be_0`
FROM `video_media_server`
WHERE (`ip` IS NOT NULL AND `ip` <> '' AND (`host` IS NULL OR `host` = ''))
   OR (`hook_ip` IS NOT NULL AND `hook_ip` <> '' AND (`hook_host` IS NULL OR `hook_host` = ''))
   OR (`sdp_ip` IS NOT NULL AND `sdp_ip` <> '' AND (`sdp_host` IS NULL OR `sdp_host` = ''))
   OR (`stream_ip` IS NOT NULL AND `stream_ip` <> '' AND (`stream_host` IS NULL OR `stream_host` = ''));

SELECT COUNT(*) AS `rule_script_known_mapping_mismatch_count_should_be_0`
FROM `rule_groovy_script`
WHERE `namespace` = 'system'
  AND `platform_code` = 'iot'
  AND `product_code` = 'dataProtocol'
  AND `business_code` = 'datas'
  AND `channel_code` IN ('mqtt', 'webSocket')
  AND `business_identification` <> ''
  AND (`script_type` <> 'topicInboundTransform'
       OR `product_identification` <> `business_identification`);

-- 视频可空业务标识归一键；生成列结果应为 2。
SELECT COUNT(*) AS `video_nullable_identity_key_column_count_should_be_2`
FROM `information_schema`.`columns`
WHERE `table_schema` = DATABASE()
  AND (
    (`table_name` = 'video_device_group_relation'
      AND `column_name` = 'channel_identification_key'
      AND `data_type` = 'varchar' AND `character_maximum_length` = 52
      AND `character_set_name` = 'utf8mb4' AND `collation_name` = 'utf8mb4_general_ci'
      AND `extra` LIKE '%STORED GENERATED%'
      AND LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        `generation_expression`, '`', ''), ' ', ''), '(', ''), ')', ''), '_utf8mb4', '')) =
        'casewhendeleted<>0thenconcat''d:'',idwhennulliftrimchannel_identification,''''isnullthen''n:''elseconcat''v:'',trimchannel_identificationend')
    OR (`table_name` = 'video_gateway_mapping'
      AND `column_name` = 'src_channel_identification_key'
      AND `data_type` = 'varchar' AND `character_maximum_length` = 66
      AND `character_set_name` = 'utf8mb4' AND `collation_name` = 'utf8mb4_general_ci'
      AND `extra` LIKE '%STORED GENERATED%'
      AND LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        `generation_expression`, '`', ''), ' ', ''), '(', ''), ')', ''), '_utf8mb4', '')) =
        'casewhendeleted<>0thenconcat''d:'',idwhennulliftrimsrc_channel_identification,''''isnullthen''n:''elseconcat''v:'',trimsrc_channel_identificationend')
  );

-- 两项重复预检均为 0 时，新归一唯一索引结果应为 2，旧可空唯一索引结果应为 0。
SELECT COUNT(*) AS `video_normalized_unique_index_count_should_be_2_when_prechecks_pass`
FROM (
  SELECT `table_name`, `index_name`
  FROM `information_schema`.`statistics`
  WHERE `table_schema` = DATABASE()
    AND (
      (`table_name` = 'video_device_group_relation' AND `index_name` = 'uk_group_device_channel_key')
      OR (`table_name` = 'video_gateway_mapping' AND `index_name` = 'uk_src_device_channel_key')
    )
  GROUP BY `table_name`, `index_name`
  HAVING MIN(`non_unique`) = 0
    AND COUNT(`sub_part`) = 0
    AND (
      (`table_name` = 'video_device_group_relation'
        AND COUNT(*) = 3
        AND GROUP_CONCAT(`column_name` ORDER BY `seq_in_index` SEPARATOR ',') =
          'group_id,device_identification,channel_identification_key')
      OR (`table_name` = 'video_gateway_mapping'
        AND COUNT(*) = 3
        AND GROUP_CONCAT(`column_name` ORDER BY `seq_in_index` SEPARATOR ',') =
          'src_protocol,src_device_identification,src_channel_identification_key')
    )
) AS `valid_video_normalized_unique_index`;

SELECT COUNT(DISTINCT CONCAT(`table_name`, '.', `index_name`)) AS `video_legacy_nullable_unique_index_count_should_be_0_when_prechecks_pass`
FROM `information_schema`.`statistics`
WHERE `table_schema` = DATABASE()
  AND (
    (`table_name` = 'video_device_group_relation' AND `index_name` = 'uk_group_device_channel')
    OR (`table_name` = 'video_gateway_mapping' AND `index_name` = 'uk_src_device_channel')
  );

-- 规则轨迹可空规则 ID 归一键与索引；重复预检为 0 时结果应依次为 1、1、0。
SELECT COUNT(*) AS `rule_trace_identity_key_column_count_should_be_1`
FROM `information_schema`.`columns`
WHERE `table_schema` = DATABASE()
  AND `table_name` = 'rule_bridge_execution_trace'
  AND `column_name` = 'bridge_rule_id_key'
  AND `data_type` = 'varchar'
  AND `character_maximum_length` = 22
  AND `character_set_name` = 'utf8mb4'
  AND `collation_name` = 'utf8mb4_general_ci'
  AND `extra` LIKE '%STORED GENERATED%'
  AND LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    `generation_expression`, '`', ''), ' ', ''), '(', ''), ')', ''), '_utf8mb4', '')) =
    'casewhenbridge_rule_idisnullthen''n:''elseconcat''v:'',bridge_rule_idend';

SELECT COUNT(*) AS `rule_trace_normalized_unique_index_count_should_be_1_when_precheck_passes`
FROM (
  SELECT `table_name`, `index_name`
  FROM `information_schema`.`statistics`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'rule_bridge_execution_trace'
    AND `index_name` = 'uk_trace_rule_key'
  GROUP BY `table_name`, `index_name`
  HAVING MIN(`non_unique`) = 0
    AND COUNT(`sub_part`) = 0
    AND COUNT(*) = 2
    AND GROUP_CONCAT(`column_name` ORDER BY `seq_in_index` SEPARATOR ',') =
      'trace_id,bridge_rule_id_key'
) AS `valid_rule_trace_normalized_unique_index`;

SELECT COUNT(DISTINCT CONCAT(`table_name`, '.', `index_name`)) AS `rule_trace_legacy_nullable_unique_index_count_should_be_0_when_precheck_passes`
FROM `information_schema`.`statistics`
WHERE `table_schema` = DATABASE()
  AND `table_name` = 'rule_bridge_execution_trace'
  AND `index_name` = 'uk_trace_rule';

-- 索引状态；无待处理空值/重复组时结果应为 2，否则按 README 修正后重跑。
SELECT COUNT(DISTINCT CONCAT(`table_name`, '.', `index_name`)) AS `new_unique_index_count_should_be_2_when_prechecks_pass`
FROM `information_schema`.`statistics`
WHERE `table_schema` = DATABASE()
  AND (
    (`table_name` = 'rule_groovy_script' AND `index_name` = 'uk_rule_groovy_script_identity')
    OR (`table_name` = 'video_media_server' AND `index_name` = 'uk_media_server_unique_host_http_port')
  );

DROP PROCEDURE IF EXISTS `thinglinks_v140_exec_if`;
SET @THINGLINKS_V140_DDL = NULL;
