#更新了 action_commands 表
-- ----------------------------
-- Table structure for action_commands
-- ----------------------------
DROP TABLE IF EXISTS `action_commands`;
CREATE TABLE `action_commands` (
  `id` int(19) unsigned NOT NULL AUTO_INCREMENT,
  `business_type` int(11) NOT NULL COMMENT '业务类型:0规则生效1产品生效2设备生效',
  `rule_identification` varchar(100) DEFAULT NULL COMMENT '规则标识',
  `product_identification` varchar(100) DEFAULT NULL COMMENT '产品标识',
  `device_identification` varchar(100) DEFAULT NULL COMMENT '设备标识',
  `service_id` int(19) DEFAULT NULL COMMENT '服务id',
  `service_name` varchar(255) DEFAULT NULL COMMENT '服务名称',
  `command_id` int(19) DEFAULT NULL COMMENT '命令id',
  `command_body` text COMMENT 'json命令参数及参数值{"key":"value","key1":"value1"}',
  `ctreate_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `command_name` varchar(255) DEFAULT NULL COMMENT '命令名称\n',
  `ctreate_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='动作命令信息表';

SET FOREIGN_KEY_CHECKS = 1;
