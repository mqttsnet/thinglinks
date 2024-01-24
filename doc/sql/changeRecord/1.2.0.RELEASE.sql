CREATE TABLE `ota_upgrades`
(
    `id`                     bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `app_id`                 varchar(64)  NOT NULL DEFAULT '' COMMENT '应用ID',
    `package_name`           varchar(100) NOT NULL DEFAULT '' COMMENT '包名称',
    `package_type`           smallint(1) NOT NULL DEFAULT '0' COMMENT '升级包类型(0:软件包、1:固件包)',
    `product_identification` varchar(100) NOT NULL DEFAULT '' COMMENT '产品标识',
    `version`                varchar(255) NOT NULL DEFAULT '' COMMENT '升级包版本号',
    `file_location`          varchar(255) NOT NULL DEFAULT '' COMMENT '升级包的位置',
    `status`                 smallint(1) NOT NULL DEFAULT '0' COMMENT '状态',
    `description`            varchar(255)          DEFAULT '' COMMENT '升级包功能描述',
    `custom_info`            longtext COMMENT '自定义信息',
    `remark`                 varchar(255)          DEFAULT '' COMMENT '描述',
    `created_by`             bigint(20) DEFAULT NULL COMMENT '创建人',
    `created_time`           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`             bigint(20) DEFAULT NULL COMMENT '更新人',
    `updated_time`           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_org_id`         bigint(20) DEFAULT NULL COMMENT '创建人组织',
    PRIMARY KEY (`id`) USING BTREE,
    KEY                      `idx_app_id` (`app_id`) USING BTREE COMMENT '应用ID',
    KEY                      `idx_product_identification` (`product_identification`) USING BTREE COMMENT '产品标识',
    KEY                      `idx_version` (`version`) USING BTREE COMMENT '升级包版本号'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='OTA升级包';

CREATE TABLE `ota_upgrade_tasks`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `upgrade_id`     bigint(20) NOT NULL COMMENT '升级包ID，关联ota_upgrades表',
    `task_name`      varchar(100) NOT NULL DEFAULT '' COMMENT '任务名称',
    `task_status`    smallint(1) NOT NULL DEFAULT '0' COMMENT '任务状态(0:待发布、1:进行中、2:已完成、3:已取消)',
    `scheduled_time` datetime              DEFAULT NULL COMMENT '计划执行时间',
    `description`    varchar(255)          DEFAULT '' COMMENT '任务描述',
    `remark`         varchar(255)          DEFAULT '' COMMENT '描述',
    `created_by`     bigint(20) DEFAULT NULL COMMENT '创建人',
    `created_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`     bigint(20) DEFAULT NULL COMMENT '更新人',
    `updated_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_org_id` bigint(20) DEFAULT NULL COMMENT '创建人组织',
    PRIMARY KEY (`id`) USING BTREE,
    KEY              `idx_upgrade_id` (`upgrade_id`) USING BTREE COMMENT '升级包ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OTA升级任务表';

CREATE TABLE `ota_upgrade_records`
(
    `id`                    bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`               bigint(20) NOT NULL COMMENT '任务ID，关联ota_upgrade_tasks表',
    `device_identification` varchar(100) NOT NULL DEFAULT '' COMMENT '设备标识',
    `upgrade_status`        smallint(1) NOT NULL DEFAULT '0' COMMENT '升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)',
    `progress`              smallint(3) NOT NULL DEFAULT '0' COMMENT '升级进度（百分比）',
    `error_code`            varchar(100)          DEFAULT NULL COMMENT '错误代码',
    `error_message`         varchar(255)          DEFAULT NULL COMMENT '错误信息',
    `start_time`            datetime              DEFAULT NULL COMMENT '升级开始时间',
    `end_time`              datetime              DEFAULT NULL COMMENT '升级结束时间',
    `success_details`       longtext COMMENT '升级成功详细信息',
    `failure_details`       longtext COMMENT '升级失败详细信息',
    `log_details`           longtext COMMENT '升级过程日志',
    `remark`                varchar(255)          DEFAULT '' COMMENT '描述',
    `created_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `created_org_id`        bigint(20) DEFAULT NULL COMMENT '创建人组织',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_task_id_device_identification` (`task_id`,`device_identification`) USING BTREE,
    KEY                     `idx_task_id` (`task_id`),
    KEY                     `idx_device_identification` (`device_identification`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OTA升级记录表';

