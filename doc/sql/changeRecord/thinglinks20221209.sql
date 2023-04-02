SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for action_commands
-- ----------------------------
DROP TABLE IF EXISTS `action_commands`;
CREATE TABLE `action_commands`
(
    `id`                     int(19) unsigned NOT NULL AUTO_INCREMENT,
    `business_type`          int(11)          NOT NULL COMMENT '业务类型:0规则生效1产品生效2设备生效',
    `rule_identification`    varchar(100) DEFAULT NULL COMMENT '规则标识',
    `product_identification` varchar(100) DEFAULT NULL COMMENT '产品标识',
    `device_identificaiton`  varchar(100) DEFAULT NULL COMMENT '设备标识',
    `service_id`             int(19)      DEFAULT NULL COMMENT '服务id',
    `command_id`             int(19)      DEFAULT NULL COMMENT '命令id',
    `command_body`           text COMMENT 'json命令参数及参数值{"key":"value","key1":"value1"}',
    `ctreate_by`             varchar(255) DEFAULT NULL COMMENT '创建者',
    `ctreate_time`           datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`              varchar(255) DEFAULT NULL COMMENT '更新者',
    `update_time`            datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='动作命令信息表';

SET FOREIGN_KEY_CHECKS = 1;


ALTER TABLE `device_datas`
    ADD COLUMN `protocol_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '协议类型 ：mqtt || coap || modbus || http' AFTER `device_identification`,
    ADD COLUMN `report_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间' AFTER `status`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`id`) USING BTREE,
    DROP INDEX `message_id`;