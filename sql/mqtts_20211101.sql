/*
 Navicat Premium Data Transfer

 Source Server         : mqtts(开发环境)
 Source Server Type    : MySQL
 Source Server Version : 50734
 Source Host           : 49.235.122.136:3306
 Source Schema         : mqtts

 Target Server Type    : MySQL
 Target Server Version : 50734
 File Encoding         : 65001

 Date: 01/11/2021 11:54:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for QRTZ_BLOB_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
CREATE TABLE `QRTZ_BLOB_TRIGGERS`  (
                                       `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                                       `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
                                       `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                       `blob_data` blob NULL COMMENT '存放持久化Trigger对象',
                                       PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
                                       CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'Blob类型的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_BLOB_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_CALENDARS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
CREATE TABLE `QRTZ_CALENDARS`  (
                                   `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                                   `calendar_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '日历名称',
                                   `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
                                   PRIMARY KEY (`sched_name`, `calendar_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '日历信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_CALENDARS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_CRON_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
CREATE TABLE `QRTZ_CRON_TRIGGERS`  (
                                       `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                                       `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
                                       `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                       `cron_expression` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'cron表达式',
                                       `time_zone_id` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时区',
                                       PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
                                       CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'Cron类型的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_CRON_TRIGGERS
-- ----------------------------
INSERT INTO `QRTZ_CRON_TRIGGERS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME1', 'DEFAULT', '0/10 * * * * ?', 'Asia/Shanghai');
INSERT INTO `QRTZ_CRON_TRIGGERS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME2', 'DEFAULT', '0/15 * * * * ?', 'Asia/Shanghai');
INSERT INTO `QRTZ_CRON_TRIGGERS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME3', 'DEFAULT', '0/20 * * * * ?', 'Asia/Shanghai');
INSERT INTO `QRTZ_CRON_TRIGGERS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME4', 'SYSTEM', '* * * * * ?', 'Asia/Shanghai');

-- ----------------------------
-- Table structure for QRTZ_FIRED_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
CREATE TABLE `QRTZ_FIRED_TRIGGERS`  (
                                        `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                                        `entry_id` varchar(95) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度器实例id',
                                        `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
                                        `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                        `instance_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度器实例名',
                                        `fired_time` bigint(13) NOT NULL COMMENT '触发的时间',
                                        `sched_time` bigint(13) NOT NULL COMMENT '定时器制定的时间',
                                        `priority` int(11) NOT NULL COMMENT '优先级',
                                        `state` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '状态',
                                        `job_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务名称',
                                        `job_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务组名',
                                        `is_nonconcurrent` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否并发',
                                        `requests_recovery` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否接受恢复执行',
                                        PRIMARY KEY (`sched_name`, `entry_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '已触发的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_FIRED_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_JOB_DETAILS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
CREATE TABLE `QRTZ_JOB_DETAILS`  (
                                     `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                                     `job_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务名称',
                                     `job_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务组名',
                                     `description` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '相关介绍',
                                     `job_class_name` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '执行任务类名称',
                                     `is_durable` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '是否持久化',
                                     `is_nonconcurrent` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '是否并发',
                                     `is_update_data` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '是否更新数据',
                                     `requests_recovery` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '是否接受恢复执行',
                                     `job_data` blob NULL COMMENT '存放持久化job对象',
                                     PRIMARY KEY (`sched_name`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '任务详细信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_JOB_DETAILS
-- ----------------------------
INSERT INTO `QRTZ_JOB_DETAILS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME1', 'DEFAULT', NULL, 'net.mqtts.job.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001B6E65742E6D717474732E6A6F622E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E00097872002B6E65742E6D717474732E636F6D6D6F6E2E636F72652E7765622E646F6D61696E2E42617365456E7469747900000000000000010200074C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C787074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B597419030000787077080000017BF357AE9878707400007070707400013174000E302F3130202A202A202A202A203F74001172795461736B2E72794E6F506172616D7374000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000001740018E7B3BBE7BB9FE9BB98E8AEA4EFBC88E697A0E58F82EFBC8974000133740001317800);
INSERT INTO `QRTZ_JOB_DETAILS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME2', 'DEFAULT', NULL, 'net.mqtts.job.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001B6E65742E6D717474732E6A6F622E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E00097872002B6E65742E6D717474732E636F6D6D6F6E2E636F72652E7765622E646F6D61696E2E42617365456E7469747900000000000000010200074C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C787074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B597419030000787077080000017BF357AE9878707400007070707400013174000E302F3135202A202A202A202A203F74001572795461736B2E7279506172616D7328277279272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000002740018E7B3BBE7BB9FE9BB98E8AEA4EFBC88E69C89E58F82EFBC8974000133740001317800);
INSERT INTO `QRTZ_JOB_DETAILS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME3', 'DEFAULT', NULL, 'net.mqtts.job.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001B6E65742E6D717474732E6A6F622E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E00097872002B6E65742E6D717474732E636F6D6D6F6E2E636F72652E7765622E646F6D61696E2E42617365456E7469747900000000000000010200074C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C787074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B597419030000787077080000017BF357AE9878707400007070707400013174000E302F3230202A202A202A202A203F74003872795461736B2E72794D756C7469706C65506172616D7328277279272C20747275652C20323030304C2C203331362E3530442C203130302974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000003740018E7B3BBE7BB9FE9BB98E8AEA4EFBC88E5A49AE58F82EFBC8974000133740001317800);
INSERT INTO `QRTZ_JOB_DETAILS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME4', 'SYSTEM', NULL, 'net.mqtts.job.util.QuartzJobExecution', '0', '0', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001B6E65742E6D717474732E6A6F622E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E00097872002B6E65742E6D717474732E636F6D6D6F6E2E636F72652E7765622E646F6D61696E2E42617365456E7469747900000000000000010200074C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707400056D717474737372000E6A6176612E7574696C2E44617465686A81014B597419030000787077080000017CB6155D9078707400007070707400013074000B2A202A202A202A202A203F74000364646474000653595354454D7372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B020000787000000000000000047400016174000131740001317800);

-- ----------------------------
-- Table structure for QRTZ_LOCKS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS`  (
                               `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                               `lock_name` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '悲观锁名称',
                               PRIMARY KEY (`sched_name`, `lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '存储的悲观锁信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_LOCKS
-- ----------------------------
INSERT INTO `QRTZ_LOCKS` VALUES ('mqttsScheduler', 'STATE_ACCESS');
INSERT INTO `QRTZ_LOCKS` VALUES ('mqttsScheduler', 'TRIGGER_ACCESS');

-- ----------------------------
-- Table structure for QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS`  (
                                             `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                                             `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                             PRIMARY KEY (`sched_name`, `trigger_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '暂停的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SCHEDULER_STATE
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
CREATE TABLE `QRTZ_SCHEDULER_STATE`  (
                                         `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                                         `instance_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '实例名称',
                                         `last_checkin_time` bigint(13) NOT NULL COMMENT '上次检查时间',
                                         `checkin_interval` bigint(13) NOT NULL COMMENT '检查间隔时间',
                                         PRIMARY KEY (`sched_name`, `instance_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '调度器状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_SCHEDULER_STATE
-- ----------------------------
INSERT INTO `QRTZ_SCHEDULER_STATE` VALUES ('mqttsScheduler', 'xiaonan1635738752194', 1635738849530, 15000);

-- ----------------------------
-- Table structure for QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS`  (
                                         `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                                         `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
                                         `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                         `repeat_count` bigint(7) NOT NULL COMMENT '重复的次数统计',
                                         `repeat_interval` bigint(12) NOT NULL COMMENT '重复的间隔时间',
                                         `times_triggered` bigint(10) NOT NULL COMMENT '已经触发的次数',
                                         PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
                                         CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '简单触发器的信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_SIMPLE_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS`  (
                                          `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                                          `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
                                          `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
                                          `str_prop_1` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
                                          `str_prop_2` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
                                          `str_prop_3` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
                                          `int_prop_1` int(11) NULL DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
                                          `int_prop_2` int(11) NULL DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
                                          `long_prop_1` bigint(20) NULL DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
                                          `long_prop_2` bigint(20) NULL DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
                                          `dec_prop_1` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
                                          `dec_prop_2` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
                                          `bool_prop_1` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
                                          `bool_prop_2` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
                                          PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
                                          CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '同步机制的行锁表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_SIMPROP_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
CREATE TABLE `QRTZ_TRIGGERS`  (
                                  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
                                  `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '触发器的名字',
                                  `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '触发器所属组的名字',
                                  `job_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
                                  `job_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
                                  `description` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '相关介绍',
                                  `next_fire_time` bigint(13) NULL DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
                                  `prev_fire_time` bigint(13) NULL DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
                                  `priority` int(11) NULL DEFAULT NULL COMMENT '优先级',
                                  `trigger_state` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '触发器状态',
                                  `trigger_type` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '触发器的类型',
                                  `start_time` bigint(13) NOT NULL COMMENT '开始时间',
                                  `end_time` bigint(13) NULL DEFAULT NULL COMMENT '结束时间',
                                  `calendar_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '日程表名称',
                                  `misfire_instr` smallint(2) NULL DEFAULT NULL COMMENT '补偿执行的策略',
                                  `job_data` blob NULL COMMENT '存放持久化job对象',
                                  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
                                  INDEX `sched_name`(`sched_name`, `job_name`, `job_group`) USING BTREE,
                                  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `QRTZ_JOB_DETAILS` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '触发器详细信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of QRTZ_TRIGGERS
-- ----------------------------
INSERT INTO `QRTZ_TRIGGERS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME1', 'DEFAULT', 'TASK_CLASS_NAME1', 'DEFAULT', NULL, 1635738760000, -1, 5, 'PAUSED', 'CRON', 1635738755000, 0, NULL, 2, '');
INSERT INTO `QRTZ_TRIGGERS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME2', 'DEFAULT', 'TASK_CLASS_NAME2', 'DEFAULT', NULL, 1635738765000, -1, 5, 'PAUSED', 'CRON', 1635738756000, 0, NULL, 2, '');
INSERT INTO `QRTZ_TRIGGERS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME3', 'DEFAULT', 'TASK_CLASS_NAME3', 'DEFAULT', NULL, 1635738760000, -1, 5, 'PAUSED', 'CRON', 1635738757000, 0, NULL, 2, '');
INSERT INTO `QRTZ_TRIGGERS` VALUES ('mqttsScheduler', 'TASK_CLASS_NAME4', 'SYSTEM', 'TASK_CLASS_NAME4', 'SYSTEM', NULL, 1635738758000, -1, 5, 'PAUSED', 'CRON', 1635738758000, 0, NULL, -1, '');

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
                              `table_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
                              `table_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '表名称',
                              `table_comment` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '表描述',
                              `sub_table_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
                              `sub_table_fk_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
                              `class_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '实体类名称',
                              `tpl_category` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
                              `package_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '生成包路径',
                              `module_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '生成模块名',
                              `business_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '生成业务名',
                              `function_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '生成功能名',
                              `function_author` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '生成功能作者',
                              `gen_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
                              `gen_path` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
                              `options` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '其它生成选项',
                              `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                              `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                              `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                              `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                              `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                              PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '代码生成业务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table
-- ----------------------------
INSERT INTO `gen_table` VALUES (2, 'mqtts_device', '设备管理', '', '', 'MqttsDevice', 'crud', 'net.mqtts.link', 'link', 'device', '设备管理', 'mqtts', '0', '/', '{\"parentMenuId\":\"1061\"}', 'admin', '2021-10-21 17:28:39', '', '2021-10-22 17:03:57', NULL);

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
                                     `column_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
                                     `table_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '归属表编号',
                                     `column_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '列名称',
                                     `column_comment` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '列描述',
                                     `column_type` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '列类型',
                                     `java_type` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
                                     `java_field` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
                                     `is_pk` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
                                     `is_increment` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
                                     `is_required` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
                                     `is_insert` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
                                     `is_edit` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
                                     `is_list` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
                                     `is_query` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
                                     `query_type` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
                                     `html_type` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
                                     `dict_type` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '字典类型',
                                     `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
                                     `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                                     `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                     `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                                     `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                     PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------
INSERT INTO `gen_table_column` VALUES (25, '2', 'id', 'id', 'bigint(19)', 'Long', 'id', '1', '0', '1', '1', NULL, '1', NULL, 'EQ', 'input', '', 1, 'admin', '2021-10-21 17:28:39', '', '2021-10-22 17:03:57');
INSERT INTO `gen_table_column` VALUES (26, '2', 'client_id', '客户端标识', 'varchar(255)', 'String', 'clientId', '0', '0', '1', '1', NULL, '1', '1', 'LIKE', 'input', '', 2, 'admin', '2021-10-21 17:28:39', '', '2021-10-22 17:03:57');
INSERT INTO `gen_table_column` VALUES (27, '2', 'user_name', '用户名', 'varchar(255)', 'String', 'userName', '0', '0', '1', '1', '1', NULL, NULL, 'LIKE', 'input', '', 3, 'admin', '2021-10-21 17:28:39', '', '2021-10-22 17:03:57');
INSERT INTO `gen_table_column` VALUES (28, '2', 'password', '密码', 'varchar(255)', 'String', 'password', '0', '0', '1', '1', '1', NULL, NULL, 'EQ', 'input', '', 4, 'admin', '2021-10-21 17:28:39', '', '2021-10-22 17:03:57');
INSERT INTO `gen_table_column` VALUES (29, '2', 'auth_mode', '认证方式', 'varchar(255)', 'String', 'authMode', '0', '0', '1', '1', NULL, '1', NULL, 'EQ', 'select', 'link_device_auth_mode', 5, 'admin', '2021-10-21 17:28:39', '', '2021-10-22 17:03:57');
INSERT INTO `gen_table_column` VALUES (30, '2', 'device_id', '设备标识', 'varchar(255)', 'String', 'deviceId', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 6, 'admin', '2021-10-21 17:28:39', '', '2021-10-22 17:03:57');
INSERT INTO `gen_table_column` VALUES (31, '2', 'device_name', '设备名称', 'varchar(255)', 'String', 'deviceName', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 7, 'admin', '2021-10-21 17:28:40', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (32, '2', 'latitude', '纬度', 'decimal(10,7)', 'BigDecimal', 'latitude', '0', '0', '1', '1', '1', NULL, NULL, 'EQ', 'input', '', 8, 'admin', '2021-10-21 17:28:40', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (33, '2', 'longitude', '经度', 'decimal(10,7)', 'BigDecimal', 'longitude', '0', '0', '1', '1', '1', NULL, NULL, 'EQ', 'input', '', 9, 'admin', '2021-10-21 17:28:40', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (34, '2', 'connector', '连接实例', 'varchar(255)', 'String', 'connector', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'select', 'link_device_connector', 10, 'admin', '2021-10-21 17:28:40', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (35, '2', 'device_description', '设备描述', 'varchar(255)', 'String', 'deviceDescription', '0', '0', NULL, '1', '1', '1', NULL, 'EQ', 'input', '', 11, 'admin', '2021-10-21 17:28:40', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (36, '2', 'device_status', '设备状态', 'varchar(255)', 'String', 'deviceStatus', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'select', 'link_device_status', 12, 'admin', '2021-10-21 17:28:40', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (37, '2', 'connect_status', '连接状态', 'varchar(255)', 'String', 'connectStatus', '0', '0', '1', '1', NULL, '1', '1', 'EQ', 'select', 'link_device_connect_status', 13, 'admin', '2021-10-21 17:28:40', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (38, '2', 'is_will', '是否遗言', 'varchar(255)', 'String', 'isWill', '0', '0', NULL, '1', NULL, '1', NULL, 'EQ', 'select', 'link_device_is_will', 14, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (39, '2', 'device_tags', '设备标签', 'varchar(255)', 'String', 'deviceTags', '0', '0', NULL, '1', '1', '1', '1', 'LIKE', 'input', '', 15, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (40, '2', 'product_id', '产品型号', 'varchar(255)', 'String', 'productId', '0', '0', '1', '1', NULL, '1', '1', 'EQ', 'input', '', 16, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (41, '2', 'manufacturer_id', '厂商ID', 'varchar(255)', 'String', 'manufacturerId', '0', '0', '1', '1', NULL, '1', '1', 'EQ', 'input', '', 17, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (42, '2', 'protocol_type', '产品协议类型', 'varchar(255)', 'String', 'protocolType', '0', '0', '1', '1', NULL, '1', '1', 'EQ', 'select', 'link_device_protocol_type', 18, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (43, '2', 'device_type', '设备类型', 'varchar(255)', 'String', 'deviceType', '0', '0', '1', '1', NULL, '1', '1', 'EQ', 'select', 'link_device_device_type', 19, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (44, '2', 'create_by', '创建者', 'varchar(64)', 'String', 'createBy', '0', '0', '1', '1', NULL, '1', NULL, 'EQ', 'input', '', 20, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (45, '2', 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '1', '1', NULL, '1', NULL, 'EQ', 'datetime', '', 21, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (46, '2', 'update_by', '更新者', 'varchar(64)', 'String', 'updateBy', '0', '0', '1', '1', '1', '1', NULL, 'EQ', 'input', '', 22, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:58');
INSERT INTO `gen_table_column` VALUES (47, '2', 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '1', '1', '1', '1', NULL, 'EQ', 'datetime', '', 23, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:59');
INSERT INTO `gen_table_column` VALUES (48, '2', 'remark', '备注', 'varchar(500)', 'String', 'remark', '0', '0', NULL, '1', '1', '1', NULL, 'EQ', 'textarea', '', 24, 'admin', '2021-10-21 17:28:41', '', '2021-10-22 17:03:59');

-- ----------------------------
-- Table structure for mqtts_device
-- ----------------------------
DROP TABLE IF EXISTS `mqtts_device`;
CREATE TABLE `mqtts_device`  (
                                 `id` bigint(19) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                 `client_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '客户端标识',
                                 `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
                                 `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
                                 `auth_mode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '认证方式',
                                 `device_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备标识',
                                 `device_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备名称',
                                 `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
                                 `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
                                 `connector` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '连接实例',
                                 `device_description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备描述',
                                 `device_status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备状态： 启用 || 禁用',
                                 `connect_status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '连接状态 : 在线：online || 离线：offline || 未连接：ununited\r\n',
                                 `is_will` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否遗言',
                                 `device_tags` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备标签',
                                 `product_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品型号',
                                 `manufacturer_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '厂商ID',
                                 `protocol_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品协议类型 ：mqtt || coap || modbus || http',
                                 `device_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备类型',
                                 `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'ununited' COMMENT '创建者',
                                 `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                 `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                 `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '设备管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mqtts_device
-- ----------------------------
INSERT INTO `mqtts_device` VALUES (2, '123', '123', '123', 'default', '123', '123', 123.1200000, 123.1200000, '218.78.103.93:11883', '123', 'ENABLE', 'ONLINE', '0', '123', '123', '123', 'MQTT', 'GATEWAY', 'mqtts', '2021-10-25 17:52:16', 'mqtts', '2021-10-28 15:07:23', '123');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
                               `config_id` int(5) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
                               `config_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '参数名称',
                               `config_key` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '参数键名',
                               `config_value` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '参数键值',
                               `config_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
                               `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                               `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                               `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                               `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                               PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2021-09-17 18:40:14', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2021-09-17 18:40:14', 'admin', '2021-10-21 17:04:07', '初始化密码 123456');
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2021-09-17 18:40:14', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` VALUES (4, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2021-09-17 18:40:14', '', NULL, '是否开启注册用户功能（true开启，false关闭）');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
                             `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
                             `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父部门id',
                             `ancestors` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '祖级列表',
                             `dept_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '部门名称',
                             `order_num` int(4) NULL DEFAULT 0 COMMENT '显示顺序',
                             `leader` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '负责人',
                             `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系电话',
                             `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱',
                             `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
                             `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                             `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 110 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', 'mqtts科技', 0, 'mqtts', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2021-09-17 18:39:56', '', NULL);
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '深圳总公司', 1, 'mqtts', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2021-09-17 18:39:56', '', NULL);
INSERT INTO `sys_dept` VALUES (102, 100, '0,100', '长沙分公司', 2, 'mqtts', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2021-09-17 18:39:56', '', NULL);
INSERT INTO `sys_dept` VALUES (103, 101, '0,100,101', '研发部门', 1, 'mqtts', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2021-09-17 18:39:56', '', NULL);
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '市场部门', 2, 'mqtts', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2021-09-17 18:39:57', '', NULL);
INSERT INTO `sys_dept` VALUES (105, 101, '0,100,101', '测试部门', 3, 'mqtts', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2021-09-17 18:39:57', '', NULL);
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '财务部门', 4, 'mqtts', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2021-09-17 18:39:57', '', NULL);
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '运维部门', 5, 'mqtts', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2021-09-17 18:39:57', '', NULL);
INSERT INTO `sys_dept` VALUES (108, 102, '0,100,102', '市场部门', 1, 'mqtts', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2021-09-17 18:39:57', '', NULL);
INSERT INTO `sys_dept` VALUES (109, 102, '0,100,102', '财务部门', 2, 'mqtts', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2021-09-17 18:39:57', '', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
                                  `dict_code` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
                                  `dict_sort` int(4) NULL DEFAULT 0 COMMENT '字典排序',
                                  `dict_label` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '字典标签',
                                  `dict_value` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '字典键值',
                                  `dict_type` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '字典类型',
                                  `css_class` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
                                  `list_class` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
                                  `is_default` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
                                  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
                                  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                                  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2021-09-17 18:40:12', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (19, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (20, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (21, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (22, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2021-09-17 18:40:13', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (23, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2021-09-17 18:40:14', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (24, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2021-09-17 18:40:14', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (25, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2021-09-17 18:40:14', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (26, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2021-09-17 18:40:14', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (27, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2021-09-17 18:40:14', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (28, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2021-09-17 18:40:14', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (29, 0, '默认', 'default', 'link_device_auth_mode', NULL, 'default', 'N', '0', 'admin', '2021-10-21 17:56:52', '', NULL, '设备用户名+设备密码');
INSERT INTO `sys_dict_data` VALUES (30, 1, 'SSL/TLS', 'SSL/TLS', 'link_device_auth_mode', NULL, 'default', 'N', '0', 'admin', '2021-10-21 17:59:10', '', NULL, 'SSL/TLS认证');
INSERT INTO `sys_dict_data` VALUES (31, 0, '218.78.103.93:11883', '218.78.103.93:11883', 'link_device_connector', NULL, 'default', 'N', '0', 'admin', '2021-10-21 18:11:26', 'mqtts', '2021-10-27 13:02:19', '默认节点Node1');
INSERT INTO `sys_dict_data` VALUES (32, 0, '启用', 'ENABLE', 'link_device_status', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:28:13', 'mqtts', '2021-10-25 15:51:25', '设备启用');
INSERT INTO `sys_dict_data` VALUES (33, 1, '禁用', 'DISABLE', 'link_device_status', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:28:31', 'mqtts', '2021-10-25 15:52:16', '设备禁用');
INSERT INTO `sys_dict_data` VALUES (34, 1, '在线', 'ONLINE', 'link_device_connect_status', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:35:53', 'mqtts', '2021-10-25 15:56:05', '设备在线');
INSERT INTO `sys_dict_data` VALUES (35, 2, '离线', 'OFFLINE', 'link_device_connect_status', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:36:23', 'mqtts', '2021-10-25 15:56:12', '设备离线');
INSERT INTO `sys_dict_data` VALUES (36, 0, '未连接', 'INIT', 'link_device_connect_status', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:37:15', 'mqtts', '2021-10-25 15:55:45', '设备未连接');
INSERT INTO `sys_dict_data` VALUES (37, 0, '是', '0', 'link_device_is_will', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:40:55', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (38, 0, '否', '1', 'link_device_is_will', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:41:02', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (39, 0, 'mqtt', 'MQTT', 'link_device_protocol_type', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:43:48', 'mqtts', '2021-10-25 15:59:31', 'mqtt');
INSERT INTO `sys_dict_data` VALUES (40, 1, 'coap', 'COAP', 'link_device_protocol_type', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:44:02', 'mqtts', '2021-10-25 15:59:39', 'coap');
INSERT INTO `sys_dict_data` VALUES (41, 2, 'modbus', 'MODBUS', 'link_device_protocol_type', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:44:15', 'mqtts', '2021-10-25 15:59:53', 'modbus');
INSERT INTO `sys_dict_data` VALUES (42, 3, 'http', 'HTTP', 'link_device_protocol_type', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:44:35', 'mqtts', '2021-10-25 15:59:59', 'http');
INSERT INTO `sys_dict_data` VALUES (43, 0, '普通设备', 'COMMON', 'link_device_device_type', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:57:26', 'mqtts', '2021-10-25 16:20:15', '普通设备（无子设备也无父设备）');
INSERT INTO `sys_dict_data` VALUES (44, 1, '网关设备', 'GATEWAY', 'link_device_device_type', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:57:44', 'mqtts', '2021-10-25 16:20:47', '网关设备(可挂载子设备)');
INSERT INTO `sys_dict_data` VALUES (45, 2, '子设备', 'SUBSET', 'link_device_device_type', NULL, 'default', 'N', '0', 'admin', '2021-10-22 16:58:06', 'mqtts', '2021-10-25 16:18:25', '子设备(归属于某个网关设备)');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
                                  `dict_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
                                  `dict_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '字典名称',
                                  `dict_type` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '字典类型',
                                  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
                                  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                                  PRIMARY KEY (`dict_id`) USING BTREE,
                                  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2021-09-17 18:40:11', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2021-09-17 18:40:11', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2021-09-17 18:40:11', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2021-09-17 18:40:12', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2021-09-17 18:40:12', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2021-09-17 18:40:12', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2021-09-17 18:40:12', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2021-09-17 18:40:12', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2021-09-17 18:40:12', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2021-09-17 18:40:12', '', NULL, '登录状态列表');
INSERT INTO `sys_dict_type` VALUES (11, '设备认证方式', 'link_device_auth_mode', '0', 'admin', '2021-10-21 17:52:30', '', NULL, '设备管理鉴权方式');
INSERT INTO `sys_dict_type` VALUES (12, '设备连接实例', 'link_device_connector', '0', 'admin', '2021-10-21 18:10:18', '', NULL, '设备连接实例');
INSERT INTO `sys_dict_type` VALUES (13, '设备状态', 'link_device_status', '0', 'admin', '2021-10-22 16:27:28', 'mqtts', '2021-10-25 15:48:58', '设备状态');
INSERT INTO `sys_dict_type` VALUES (14, '连接状态', 'link_device_connect_status', '0', 'admin', '2021-10-22 16:35:11', '', NULL, '设备连接状态\n');
INSERT INTO `sys_dict_type` VALUES (15, '是否遗言', 'link_device_is_will', '0', 'admin', '2021-10-22 16:40:39', '', NULL, '设备是否有遗言');
INSERT INTO `sys_dict_type` VALUES (16, '产品协议类型', 'link_device_protocol_type', '0', 'admin', '2021-10-22 16:43:24', '', NULL, '产品协议类型 ：mqtt || coap || modbus || http');
INSERT INTO `sys_dict_type` VALUES (17, '设备类型', 'link_device_device_type', '0', 'admin', '2021-10-22 16:54:12', 'admin', '2021-10-22 16:56:47', '设备类型0-普通设备（无子设备也无父设备）1-网关设备(可挂载子设备)2-子设备(归属于某个网关设备)。');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
                            `job_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
                            `job_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
                            `job_group` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
                            `invoke_target` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调用目标字符串',
                            `cron_expression` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT 'cron执行表达式',
                            `misfire_policy` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
                            `concurrent` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
                            `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1暂停）',
                            `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                            `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                            `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                            `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '备注信息',
                            PRIMARY KEY (`job_id`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '定时任务调度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO `sys_job` VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2021-09-17 18:40:15', 'mqtts', '2021-10-25 03:09:23', '');
INSERT INTO `sys_job` VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2021-09-17 18:40:15', '', NULL, '');
INSERT INTO `sys_job` VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2021-09-17 18:40:15', '', NULL, '');
INSERT INTO `sys_job` VALUES (4, 'a', 'SYSTEM', 'ddd', '* * * * * ?', '1', '0', '1', 'mqtts', '2021-10-25 14:13:46', '', NULL, '');

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
                                `job_log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
                                `job_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务名称',
                                `job_group` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务组名',
                                `invoke_target` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调用目标字符串',
                                `job_message` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '日志信息',
                                `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
                                `exception_info` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '异常信息',
                                `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '定时任务调度日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
                                   `info_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '访问ID',
                                   `user_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '用户账号',
                                   `ipaddr` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
                                   `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
                                   `msg` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '提示信息',
                                   `access_time` datetime NULL DEFAULT NULL COMMENT '访问时间',
                                   PRIMARY KEY (`info_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 208 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统访问记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
INSERT INTO `sys_logininfor` VALUES (100, 'admin', '127.0.0.1', '0', '登录成功', '2021-09-25 18:55:16');
INSERT INTO `sys_logininfor` VALUES (101, 'admin', '127.0.0.1', '0', '退出成功', '2021-09-25 19:03:27');
INSERT INTO `sys_logininfor` VALUES (102, 'admin', '127.0.0.1', '0', '登录成功', '2021-09-26 09:26:22');
INSERT INTO `sys_logininfor` VALUES (103, 'admin', '127.0.0.1', '0', '登录成功', '2021-09-28 14:20:49');
INSERT INTO `sys_logininfor` VALUES (104, 'admin', '127.0.0.1', '0', '退出成功', '2021-09-28 14:30:37');
INSERT INTO `sys_logininfor` VALUES (105, 'admin', '127.0.0.1', '0', '登录成功', '2021-10-13 12:53:36');
INSERT INTO `sys_logininfor` VALUES (106, 'admin', '127.0.0.1', '1', '用户密码错误', '2021-10-21 17:02:32');
INSERT INTO `sys_logininfor` VALUES (107, 'admin', '127.0.0.1', '0', '登录成功', '2021-10-21 17:02:43');
INSERT INTO `sys_logininfor` VALUES (108, 'mqtts', '127.0.0.1', '0', '登录成功', '2021-10-22 15:25:40');
INSERT INTO `sys_logininfor` VALUES (109, 'mqtts', '127.0.0.1', '0', '退出成功', '2021-10-22 15:25:50');
INSERT INTO `sys_logininfor` VALUES (110, 'admin', '127.0.0.1', '0', '登录成功', '2021-10-22 15:26:06');
INSERT INTO `sys_logininfor` VALUES (151, 'mqtts', '58.211.55.186', '0', '退出成功', '2021-10-23 23:00:46');
INSERT INTO `sys_logininfor` VALUES (152, 'admin', '58.211.55.186', '0', '登录成功', '2021-10-23 23:00:56');
INSERT INTO `sys_logininfor` VALUES (153, 'admin', '58.211.55.186', '0', '退出成功', '2021-10-23 23:02:29');
INSERT INTO `sys_logininfor` VALUES (154, 'mqtts', '58.211.55.186', '0', '登录成功', '2021-10-23 23:03:50');
INSERT INTO `sys_logininfor` VALUES (155, 'mqtts', '117.147.39.226', '0', '登录成功', '2021-10-23 23:21:51');
INSERT INTO `sys_logininfor` VALUES (156, 'mqtts', '58.211.55.186', '0', '退出成功', '2021-10-24 00:10:30');
INSERT INTO `sys_logininfor` VALUES (157, 'mqtts', '58.211.55.186', '0', '登录成功', '2021-10-24 08:49:20');
INSERT INTO `sys_logininfor` VALUES (158, 'mqtts', '124.248.219.143', '0', '登录成功', '2021-10-24 11:44:20');
INSERT INTO `sys_logininfor` VALUES (159, 'mqtts', '112.48.5.244', '0', '登录成功', '2021-10-24 13:34:50');
INSERT INTO `sys_logininfor` VALUES (160, 'mqtts', '112.24.129.63', '0', '登录成功', '2021-10-24 14:05:12');
INSERT INTO `sys_logininfor` VALUES (161, 'mqtts', '223.104.175.169', '0', '登录成功', '2021-10-24 15:52:41');
INSERT INTO `sys_logininfor` VALUES (162, 'mqtts', '120.230.90.203', '0', '登录成功', '2021-10-24 17:01:47');
INSERT INTO `sys_logininfor` VALUES (163, 'mqtts', '39.88.214.126', '0', '登录成功', '2021-10-24 18:13:35');
INSERT INTO `sys_logininfor` VALUES (164, 'mqtts', '119.123.75.136', '0', '登录成功', '2021-10-24 20:30:04');
INSERT INTO `sys_logininfor` VALUES (165, 'mqtts', '116.228.158.146', '0', '登录成功', '2021-10-24 20:35:24');
INSERT INTO `sys_logininfor` VALUES (166, 'mqtts', '112.37.124.125', '0', '登录成功', '2021-10-24 20:47:30');
INSERT INTO `sys_logininfor` VALUES (167, 'mqtts', '113.65.128.39', '0', '登录成功', '2021-10-24 21:04:41');
INSERT INTO `sys_logininfor` VALUES (168, 'mqtts', '202.106.56.166', '0', '登录成功', '2021-10-24 21:31:47');
INSERT INTO `sys_logininfor` VALUES (169, 'mqtts', '222.90.64.190', '0', '登录成功', '2021-10-24 21:56:07');
INSERT INTO `sys_logininfor` VALUES (170, 'mqtts', '47.242.179.50', '0', '登录成功', '2021-10-25 03:07:58');
INSERT INTO `sys_logininfor` VALUES (171, 'mqtts', '223.166.106.245', '0', '登录成功', '2021-10-25 06:54:30');
INSERT INTO `sys_logininfor` VALUES (172, 'mqtts', '117.62.175.254', '0', '登录成功', '2021-10-25 08:43:38');
INSERT INTO `sys_logininfor` VALUES (173, 'mqtts', '111.59.236.54', '0', '登录成功', '2021-10-25 08:45:04');
INSERT INTO `sys_logininfor` VALUES (174, 'mqtts', '171.88.165.165', '0', '登录成功', '2021-10-25 09:02:30');
INSERT INTO `sys_logininfor` VALUES (175, 'mqtts', '218.94.111.38', '0', '登录成功', '2021-10-25 09:36:15');
INSERT INTO `sys_logininfor` VALUES (176, 'mqtts', '119.39.38.228', '0', '登录成功', '2021-10-25 09:39:03');
INSERT INTO `sys_logininfor` VALUES (177, 'mqtts', '111.46.51.63', '0', '登录成功', '2021-10-25 10:14:25');
INSERT INTO `sys_logininfor` VALUES (178, 'mqtts', '103.149.162.111', '0', '登录成功', '2021-10-25 10:32:39');
INSERT INTO `sys_logininfor` VALUES (179, 'mqtts', '222.240.96.3', '0', '登录成功', '2021-10-25 10:37:17');
INSERT INTO `sys_logininfor` VALUES (180, 'mqtts', '112.9.108.204', '0', '登录成功', '2021-10-25 10:40:43');
INSERT INTO `sys_logininfor` VALUES (181, 'mqtts', '103.121.165.206', '0', '登录成功', '2021-10-25 11:03:53');
INSERT INTO `sys_logininfor` VALUES (182, 'mqtts', '218.56.249.217', '0', '登录成功', '2021-10-25 13:15:39');
INSERT INTO `sys_logininfor` VALUES (183, 'mqtts', '104.207.156.155', '0', '登录成功', '2021-10-25 13:38:59');
INSERT INTO `sys_logininfor` VALUES (184, 'mqtts', '23.142.224.96', '0', '登录成功', '2021-10-25 13:49:48');
INSERT INTO `sys_logininfor` VALUES (185, 'mqtts', '36.7.87.91', '0', '登录成功', '2021-10-25 13:59:23');
INSERT INTO `sys_logininfor` VALUES (186, 'mqtts', '14.205.136.119', '0', '登录成功', '2021-10-25 14:11:46');
INSERT INTO `sys_logininfor` VALUES (187, 'mqtts', '14.205.136.119', '0', '登录成功', '2021-10-25 14:12:41');
INSERT INTO `sys_logininfor` VALUES (188, 'mqtts', '61.175.185.178', '0', '登录成功', '2021-10-25 14:19:28');
INSERT INTO `sys_logininfor` VALUES (189, 'mqtts', '113.110.134.105', '0', '登录成功', '2021-10-25 14:32:53');
INSERT INTO `sys_logininfor` VALUES (190, 'mqtts', '101.224.137.27', '0', '登录成功', '2021-10-25 14:36:27');
INSERT INTO `sys_logininfor` VALUES (191, 'mqtts', '153.3.160.63', '0', '登录成功', '2021-10-25 14:45:19');
INSERT INTO `sys_logininfor` VALUES (192, 'mqtts', '113.240.250.185', '0', '登录成功', '2021-10-25 15:15:52');
INSERT INTO `sys_logininfor` VALUES (193, 'mqtts', '49.7.47.132', '0', '登录成功', '2021-10-25 15:20:04');
INSERT INTO `sys_logininfor` VALUES (194, 'mqtts', '58.33.179.227', '0', '登录成功', '2021-10-25 15:33:20');
INSERT INTO `sys_logininfor` VALUES (195, 'mqtts', '36.154.102.227', '0', '登录成功', '2021-10-25 15:41:36');
INSERT INTO `sys_logininfor` VALUES (196, 'mqtts', '114.222.3.9', '0', '登录成功', '2021-10-25 15:43:06');
INSERT INTO `sys_logininfor` VALUES (197, 'mqtts', '222.129.38.226', '0', '登录成功', '2021-10-25 16:29:55');
INSERT INTO `sys_logininfor` VALUES (198, 'mqtts', '111.59.236.54', '0', '登录成功', '2021-10-25 16:34:00');
INSERT INTO `sys_logininfor` VALUES (199, 'mqtts', '119.139.198.100', '0', '登录成功', '2021-10-25 16:47:55');
INSERT INTO `sys_logininfor` VALUES (200, 'mqtts', '180.167.220.194', '0', '登录成功', '2021-10-25 16:54:02');
INSERT INTO `sys_logininfor` VALUES (201, 'mqtts', '123.139.19.144', '0', '登录成功', '2021-10-25 17:13:05');
INSERT INTO `sys_logininfor` VALUES (202, 'mqtts', '36.112.12.189', '0', '登录成功', '2021-10-25 17:23:17');
INSERT INTO `sys_logininfor` VALUES (203, 'mqtts', '127.0.0.1', '0', '登录成功', '2021-10-27 12:54:59');
INSERT INTO `sys_logininfor` VALUES (204, 'mqtts', '127.0.0.1', '0', '登录成功', '2021-10-28 15:05:42');
INSERT INTO `sys_logininfor` VALUES (205, 'mqtts', '127.0.0.1', '0', '退出成功', '2021-10-28 15:17:31');
INSERT INTO `sys_logininfor` VALUES (206, 'mqtts', '127.0.0.1', '0', '登录成功', '2021-10-31 18:15:09');
INSERT INTO `sys_logininfor` VALUES (207, 'mqtts', '127.0.0.1', '0', '登录成功', '2021-11-01 11:18:59');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
                             `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
                             `menu_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '菜单名称',
                             `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID',
                             `order_num` int(4) NULL DEFAULT 0 COMMENT '显示顺序',
                             `path` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '路由地址',
                             `component` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '组件路径',
                             `query` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路由参数',
                             `is_frame` int(1) NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
                             `is_cache` int(1) NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
                             `menu_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
                             `visible` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
                             `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
                             `perms` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限标识',
                             `icon` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
                             `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '备注',
                             PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1068 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, 'system', NULL, '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2021-09-17 18:39:58', '', NULL, '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 2, 'monitor', NULL, '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2021-09-17 18:39:58', '', NULL, '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, '系统工具', 0, 3, 'tool', NULL, '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2021-09-17 18:39:58', '', NULL, '系统工具目录');
INSERT INTO `sys_menu` VALUES (4, 'mqtts官网', 0, 5, 'http://mqtts.net', NULL, '', 0, 0, 'M', '0', '0', '', 'guide', 'admin', '2021-09-17 18:39:59', 'admin', '2021-10-21 17:14:49', 'mqtts官网地址');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2021-09-17 18:39:59', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2021-09-17 18:39:59', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2021-09-17 18:39:59', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2021-09-17 18:39:59', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2021-09-17 18:39:59', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2021-09-17 18:39:59', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2021-09-17 18:39:59', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2021-09-17 18:39:59', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` VALUES (108, '日志管理', 1, 9, 'log', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2021-09-17 18:39:59', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2021-09-17 18:39:59', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2021-09-17 18:39:59', '', NULL, '定时任务菜单');
INSERT INTO `sys_menu` VALUES (111, 'Sentinel控制台', 2, 3, 'http://localhost:8718', '', '', 0, 0, 'C', '0', '0', 'monitor:sentinel:list', 'sentinel', 'admin', '2021-09-17 18:39:59', '', NULL, '流量控制菜单');
INSERT INTO `sys_menu` VALUES (112, 'Nacos控制台', 2, 4, 'http://49.235.122.136:8848/nacos/index.html', '', '', 0, 0, 'C', '0', '0', 'monitor:nacos:list', 'nacos', 'admin', '2021-09-17 18:39:59', 'admin', '2021-09-26 09:42:08', '服务治理菜单');
INSERT INTO `sys_menu` VALUES (113, 'Admin控制台', 2, 5, 'http://localhost:9100/login', '', '', 0, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2021-09-17 18:39:59', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` VALUES (114, '表单构建', 3, 1, 'build', 'tool/build/index', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2021-09-17 18:39:59', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` VALUES (115, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2021-09-17 18:40:00', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` VALUES (116, '系统接口', 3, 3, 'http://localhost:8080/swagger-ui/index.html', '', '', 0, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2021-09-17 18:40:00', '', NULL, '系统接口菜单');
INSERT INTO `sys_menu` VALUES (500, '操作日志', 108, 1, 'operlog', 'system/operlog/index', '', 1, 0, 'C', '0', '0', 'system:operlog:list', 'form', 'admin', '2021-09-17 18:40:00', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` VALUES (501, '登录日志', 108, 2, 'logininfor', 'system/logininfor/index', '', 1, 0, 'C', '0', '0', 'system:logininfor:list', 'logininfor', 'admin', '2021-09-17 18:40:00', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` VALUES (1001, '用户查询', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2021-09-17 18:40:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1002, '用户新增', 100, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2021-09-17 18:40:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1003, '用户修改', 100, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2021-09-17 18:40:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1004, '用户删除', 100, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2021-09-17 18:40:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1005, '用户导出', 100, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2021-09-17 18:40:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1006, '用户导入', 100, 6, '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2021-09-17 18:40:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1007, '重置密码', 100, 7, '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2021-09-17 18:40:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1008, '角色查询', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2021-09-17 18:40:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1009, '角色新增', 101, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2021-09-17 18:40:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1010, '角色修改', 101, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1011, '角色删除', 101, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1012, '角色导出', 101, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1013, '菜单查询', 102, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1014, '菜单新增', 102, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1015, '菜单修改', 102, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1016, '菜单删除', 102, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1017, '部门查询', 103, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1018, '部门新增', 103, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1019, '部门修改', 103, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1020, '部门删除', 103, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1021, '岗位查询', 104, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1022, '岗位新增', 104, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1023, '岗位修改', 104, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1024, '岗位删除', 104, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1025, '岗位导出', 104, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2021-09-17 18:40:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1026, '字典查询', 105, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1027, '字典新增', 105, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1028, '字典修改', 105, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1029, '字典删除', 105, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1030, '字典导出', 105, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1031, '参数查询', 106, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1032, '参数新增', 106, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1033, '参数修改', 106, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1034, '参数删除', 106, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1035, '参数导出', 106, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1036, '公告查询', 107, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1037, '公告新增', 107, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1038, '公告修改', 107, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1039, '公告删除', 107, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1040, '操作查询', 500, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:operlog:query', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1041, '操作删除', 500, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:operlog:remove', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1042, '日志导出', 500, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:operlog:export', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1043, '登录查询', 501, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:logininfor:query', '#', 'admin', '2021-09-17 18:40:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1044, '登录删除', 501, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:logininfor:remove', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1045, '日志导出', 501, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:logininfor:export', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1046, '在线查询', 109, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1047, '批量强退', 109, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1048, '单条强退', 109, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1049, '任务查询', 110, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1050, '任务新增', 110, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1051, '任务修改', 110, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1052, '任务删除', 110, 4, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1053, '状态修改', 110, 5, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1054, '任务导出', 110, 7, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2021-09-17 18:40:03', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1055, '生成查询', 115, 1, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2021-09-17 18:40:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1056, '生成修改', 115, 2, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2021-09-17 18:40:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1057, '生成删除', 115, 3, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2021-09-17 18:40:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1058, '导入代码', 115, 2, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2021-09-17 18:40:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1059, '预览代码', 115, 4, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2021-09-17 18:40:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1060, '生成代码', 115, 5, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2021-09-17 18:40:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1061, '设备集成', 0, 4, 'link', NULL, NULL, 1, 0, 'M', '0', '0', '', 'client', 'admin', '2021-10-21 17:14:32', 'admin', '2021-10-21 17:14:54', '');
INSERT INTO `sys_menu` VALUES (1062, '设备管理', 1061, 1, 'device', 'link/device/index', NULL, 1, 0, 'C', '0', '0', 'link:device:list', 'nested', 'admin', '2021-10-21 17:27:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1063, '设备查询', 1062, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'link:device:query', '#', 'admin', '2021-10-21 17:35:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1064, '设备新增', 1062, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'link:device:add', '#', 'admin', '2021-10-21 17:35:39', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1065, '设备修改', 1062, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'link:device:edit', '#', 'admin', '2021-10-21 17:36:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1066, '设备删除', 1062, 4, '', NULL, NULL, 1, 0, 'F', '0', '0', 'link:device:remove', '#', 'admin', '2021-10-21 17:36:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1067, '设备导出', 1062, 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'link:device:export', '#', 'admin', '2021-10-21 17:36:41', '', NULL, '');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
                               `notice_id` int(4) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
                               `notice_title` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公告标题',
                               `notice_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
                               `notice_content` longblob NULL COMMENT '公告内容',
                               `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
                               `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                               `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                               `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                               `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                               PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 mqtts新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2021-09-17 18:40:16', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 mqtts系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2021-09-17 18:40:16', '', NULL, '管理员');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
                                 `oper_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
                                 `title` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '模块标题',
                                 `business_type` int(2) NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
                                 `method` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '方法名称',
                                 `request_method` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '请求方式',
                                 `operator_type` int(1) NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
                                 `oper_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '操作人员',
                                 `dept_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '部门名称',
                                 `oper_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '请求URL',
                                 `oper_ip` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '主机地址',
                                 `oper_location` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '操作地点',
                                 `oper_param` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '请求参数',
                                 `json_result` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '返回参数',
                                 `status` int(1) NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
                                 `error_msg` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '错误消息',
                                 `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
                                 PRIMARY KEY (`oper_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 195 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (100, '菜单管理', 2, 'net.mqtts.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"query\":\"\",\"icon\":\"nacos\",\"orderNum\":\"4\",\"menuName\":\"Nacos控制台\",\"params\":{},\"parentId\":2,\"isCache\":\"0\",\"path\":\"http://49.235.122.136:8848/nacos/index.html\",\"component\":\"\",\"children\":[],\"createTime\":1631875199000,\"updateBy\":\"admin\",\"isFrame\":\"0\",\"menuId\":112,\"menuType\":\"C\",\"perms\":\"monitor:nacos:list\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-09-26 09:42:09');
INSERT INTO `sys_oper_log` VALUES (101, '个人信息', 2, 'net.mqtts.system.controller.SysProfileController.updateProfile()', 'PUT', 1, 'admin', NULL, '/user/profile', '127.0.0.1', '', '{\"roles\":[{\"flag\":false,\"roleId\":1,\"admin\":true,\"dataScope\":\"1\",\"params\":{},\"roleSort\":\"1\",\"deptCheckStrictly\":false,\"menuCheckStrictly\":false,\"roleKey\":\"admin\",\"roleName\":\"超级管理员\",\"status\":\"0\"}],\"phonenumber\":\"15888888888\",\"admin\":true,\"loginDate\":1631875197000,\"remark\":\"管理员\",\"delFlag\":\"0\",\"loginIp\":\"127.0.0.1\",\"email\":\"mqtts@163.com\",\"nickName\":\"mqtts\",\"sex\":\"0\",\"deptId\":103,\"avatar\":\"\",\"dept\":{\"deptName\":\"研发部门\",\"leader\":\"mqtts\",\"deptId\":103,\"orderNum\":\"1\",\"params\":{},\"parentId\":101,\"children\":[],\"status\":\"0\"},\"params\":{},\"userName\":\"admin\",\"userId\":1,\"createBy\":\"admin\",\"createTime\":1631875197000,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-09-28 14:21:31');
INSERT INTO `sys_oper_log` VALUES (102, '用户头像', 2, 'net.mqtts.system.controller.SysProfileController.avatar()', 'POST', 1, 'admin', NULL, '/user/profile/avatar', '127.0.0.1', '', '', '{\"msg\":\"操作成功\",\"imgUrl\":\"http://127.0.0.1:9300/statics/2021/09/28/3371452e-9ace-40cb-bd50-7d066fc034f5.jpeg\",\"code\":200}', 0, NULL, '2021-09-28 14:21:53');
INSERT INTO `sys_oper_log` VALUES (103, '菜单管理', 2, 'net.mqtts.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"query\":\"\",\"icon\":\"guide\",\"orderNum\":\"4\",\"menuName\":\"mqtts官网\",\"params\":{},\"parentId\":0,\"isCache\":\"0\",\"path\":\"http://mqtts.net\",\"children\":[],\"createTime\":1631875199000,\"updateBy\":\"admin\",\"isFrame\":\"0\",\"menuId\":4,\"menuType\":\"M\",\"perms\":\"\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-13 12:54:09');
INSERT INTO `sys_oper_log` VALUES (104, '字典类型', 9, 'net.mqtts.system.controller.SysDictTypeController.refreshCache()', 'DELETE', 1, 'admin', NULL, '/dict/type/refreshCache', '127.0.0.1', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-13 12:54:33');
INSERT INTO `sys_oper_log` VALUES (105, '字典类型', 9, 'net.mqtts.system.controller.SysDictTypeController.refreshCache()', 'DELETE', 1, 'admin', NULL, '/dict/type/refreshCache', '127.0.0.1', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-13 12:54:33');
INSERT INTO `sys_oper_log` VALUES (106, '字典类型', 9, 'net.mqtts.system.controller.SysDictTypeController.refreshCache()', 'DELETE', 1, 'admin', NULL, '/dict/type/refreshCache', '127.0.0.1', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-13 12:54:34');
INSERT INTO `sys_oper_log` VALUES (107, '字典类型', 9, 'net.mqtts.system.controller.SysDictTypeController.refreshCache()', 'DELETE', 1, 'admin', NULL, '/dict/type/refreshCache', '127.0.0.1', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-13 12:54:34');
INSERT INTO `sys_oper_log` VALUES (108, '字典类型', 9, 'net.mqtts.system.controller.SysDictTypeController.refreshCache()', 'DELETE', 1, 'admin', NULL, '/dict/type/refreshCache', '127.0.0.1', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-13 12:54:35');
INSERT INTO `sys_oper_log` VALUES (109, '用户管理', 1, 'net.mqtts.system.controller.SysUserController.add()', 'POST', 1, 'admin', NULL, '/user', '127.0.0.1', '', '{\"phonenumber\":\"\",\"admin\":false,\"password\":\"$2a$10$oeiU9tYwmguNECk.RbslvuNz0s1bCEcVf5Hj/I1Fok8p0YWUthB3y\",\"postIds\":[2],\"email\":\"mqttsnet@163.com\",\"nickName\":\"mqtts\",\"sex\":\"2\",\"deptId\":100,\"params\":{},\"userName\":\"mqtts\",\"userId\":3,\"createBy\":\"admin\",\"roleIds\":[2],\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:03:53');
INSERT INTO `sys_oper_log` VALUES (110, '用户管理', 1, 'net.mqtts.system.controller.SysUserController.add()', 'POST', 1, 'admin', NULL, '/user', '127.0.0.1', '', '{\"phonenumber\":\"\",\"admin\":false,\"password\":\"123456\",\"postIds\":[2],\"email\":\"mqttsnet@163.com\",\"nickName\":\"mqtts\",\"sex\":\"2\",\"deptId\":100,\"params\":{},\"userName\":\"mqtts\",\"roleIds\":[2],\"status\":\"0\"}', '{\"msg\":\"新增用户\'mqtts\'失败，登录账号已存在\",\"code\":500}', 0, NULL, '2021-10-21 17:03:53');
INSERT INTO `sys_oper_log` VALUES (111, '参数管理', 2, 'net.mqtts.system.controller.SysConfigController.edit()', 'PUT', 1, 'admin', NULL, '/config', '127.0.0.1', '', '{\"configName\":\"用户管理-账号初始密码\",\"configKey\":\"sys.user.initPassword\",\"createBy\":\"admin\",\"createTime\":1631875214000,\"updateBy\":\"admin\",\"configId\":2,\"remark\":\"初始化密码 123456\",\"configType\":\"Y\",\"configValue\":\"123456\",\"params\":{}}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:04:07');
INSERT INTO `sys_oper_log` VALUES (112, '菜单管理', 1, 'net.mqtts.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"icon\":\"client\",\"orderNum\":\"4\",\"menuName\":\"设备集成\",\"params\":{},\"parentId\":0,\"isCache\":\"0\",\"path\":\"link\",\"createBy\":\"admin\",\"children\":[],\"isFrame\":\"1\",\"menuType\":\"M\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:14:32');
INSERT INTO `sys_oper_log` VALUES (113, '菜单管理', 2, 'net.mqtts.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"icon\":\"client\",\"orderNum\":\"5\",\"menuName\":\"设备集成\",\"params\":{},\"parentId\":0,\"isCache\":\"0\",\"path\":\"link\",\"children\":[],\"createTime\":1634807672000,\"updateBy\":\"admin\",\"isFrame\":\"1\",\"menuId\":1061,\"menuType\":\"M\",\"perms\":\"\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:14:39');
INSERT INTO `sys_oper_log` VALUES (114, '菜单管理', 2, 'net.mqtts.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"query\":\"\",\"icon\":\"guide\",\"orderNum\":\"5\",\"menuName\":\"mqtts官网\",\"params\":{},\"parentId\":0,\"isCache\":\"0\",\"path\":\"http://mqtts.net\",\"children\":[],\"createTime\":1631875199000,\"updateBy\":\"admin\",\"isFrame\":\"0\",\"menuId\":4,\"menuType\":\"M\",\"perms\":\"\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:14:49');
INSERT INTO `sys_oper_log` VALUES (115, '菜单管理', 2, 'net.mqtts.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"icon\":\"client\",\"orderNum\":\"4\",\"menuName\":\"设备集成\",\"params\":{},\"parentId\":0,\"isCache\":\"0\",\"path\":\"link\",\"children\":[],\"createTime\":1634807672000,\"updateBy\":\"admin\",\"isFrame\":\"1\",\"menuId\":1061,\"menuType\":\"M\",\"perms\":\"\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:14:54');
INSERT INTO `sys_oper_log` VALUES (116, '菜单管理', 1, 'net.mqtts.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"icon\":\"nested\",\"orderNum\":\"1\",\"menuName\":\"设备管理\",\"params\":{},\"parentId\":1061,\"isCache\":\"0\",\"path\":\"device\",\"component\":\"link/device/index\",\"createBy\":\"admin\",\"children\":[],\"isFrame\":\"1\",\"menuType\":\"C\",\"perms\":\"link:device:list\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:27:48');
INSERT INTO `sys_oper_log` VALUES (117, '代码生成', 6, 'net.mqtts.gen.controller.GenController.importTableSave()', 'POST', 1, 'admin', NULL, '/gen/importTable', '127.0.0.1', '', 'mqtts_device', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:28:38');
INSERT INTO `sys_oper_log` VALUES (118, '代码生成', 6, 'net.mqtts.gen.controller.GenController.importTableSave()', 'POST', 1, 'admin', NULL, '/gen/importTable', '127.0.0.1', '', 'mqtts_device', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:28:41');
INSERT INTO `sys_oper_log` VALUES (119, '代码生成', 3, 'net.mqtts.gen.controller.GenController.remove()', 'DELETE', 1, 'admin', NULL, '/gen/1', '127.0.0.1', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:28:46');
INSERT INTO `sys_oper_log` VALUES (120, '代码生成', 8, 'net.mqtts.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', NULL, NULL, 0, NULL, '2021-10-21 17:30:42');
INSERT INTO `sys_oper_log` VALUES (121, '菜单管理', 1, 'net.mqtts.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"orderNum\":\"1\",\"menuName\":\"设备查询\",\"params\":{},\"parentId\":1062,\"isCache\":\"0\",\"createBy\":\"admin\",\"children\":[],\"isFrame\":\"1\",\"menuType\":\"F\",\"perms\":\"link:device:query\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:35:09');
INSERT INTO `sys_oper_log` VALUES (122, '菜单管理', 1, 'net.mqtts.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"orderNum\":\"2\",\"menuName\":\"设备新增\",\"params\":{},\"parentId\":1062,\"isCache\":\"0\",\"createBy\":\"admin\",\"children\":[],\"isFrame\":\"1\",\"menuType\":\"F\",\"perms\":\"link:device:add\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:35:39');
INSERT INTO `sys_oper_log` VALUES (123, '菜单管理', 1, 'net.mqtts.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"orderNum\":\"3\",\"menuName\":\"设备修改\",\"params\":{},\"parentId\":1062,\"isCache\":\"0\",\"createBy\":\"admin\",\"children\":[],\"isFrame\":\"1\",\"menuType\":\"F\",\"perms\":\"link:device:edit\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:36:00');
INSERT INTO `sys_oper_log` VALUES (124, '菜单管理', 1, 'net.mqtts.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"orderNum\":\"4\",\"menuName\":\"设备删除\",\"params\":{},\"parentId\":1062,\"isCache\":\"0\",\"createBy\":\"admin\",\"children\":[],\"isFrame\":\"1\",\"menuType\":\"F\",\"perms\":\"link:device:remove\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:36:21');
INSERT INTO `sys_oper_log` VALUES (125, '菜单管理', 1, 'net.mqtts.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"visible\":\"0\",\"orderNum\":\"5\",\"menuName\":\"设备导出\",\"params\":{},\"parentId\":1062,\"isCache\":\"0\",\"createBy\":\"admin\",\"children\":[],\"isFrame\":\"1\",\"menuType\":\"F\",\"perms\":\"link:device:export\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:36:41');
INSERT INTO `sys_oper_log` VALUES (126, '字典类型', 1, 'net.mqtts.system.controller.SysDictTypeController.add()', 'POST', 1, 'admin', NULL, '/dict/type', '127.0.0.1', '', '{\"createBy\":\"admin\",\"dictName\":\"设备认证方式\",\"remark\":\"设备管理鉴权方式\",\"params\":{},\"dictType\":\"link_device_auth_mode\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:52:30');
INSERT INTO `sys_oper_log` VALUES (127, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"default\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"设备用户名+设备密码\",\"params\":{},\"dictType\":\"link_device_auth_mode\",\"dictLabel\":\"默认\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:56:52');
INSERT INTO `sys_oper_log` VALUES (128, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"SSL/TLS\",\"listClass\":\"default\",\"dictSort\":1,\"remark\":\"SSL/TLS认证\",\"params\":{},\"dictType\":\"link_device_auth_mode\",\"dictLabel\":\"SSL/TLS\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 17:59:10');
INSERT INTO `sys_oper_log` VALUES (129, '字典类型', 1, 'net.mqtts.system.controller.SysDictTypeController.add()', 'POST', 1, 'admin', NULL, '/dict/type', '127.0.0.1', '', '{\"createBy\":\"admin\",\"dictName\":\"设备连接实例\",\"remark\":\"设备连接实例\",\"params\":{},\"dictType\":\"link_device_connector\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 18:10:18');
INSERT INTO `sys_oper_log` VALUES (130, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"127.0.0.1:11883\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"默认节点Node1\",\"params\":{},\"dictType\":\"link_device_connector\",\"dictLabel\":\"默认节点Node1\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-21 18:11:26');
INSERT INTO `sys_oper_log` VALUES (131, '字典类型', 1, 'net.mqtts.system.controller.SysDictTypeController.add()', 'POST', 1, 'admin', NULL, '/dict/type', '127.0.0.1', '', '{\"createBy\":\"admin\",\"dictName\":\"设备状态\",\"remark\":\"设备状态 0启用 1禁用，不填时默认为0启用\",\"params\":{},\"dictType\":\"link_device_status\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:27:29');
INSERT INTO `sys_oper_log` VALUES (132, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"0\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"设备启用\",\"params\":{},\"dictType\":\"link_device_status\",\"dictLabel\":\"启用\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:28:13');
INSERT INTO `sys_oper_log` VALUES (133, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"1\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"设备禁用\",\"params\":{},\"dictType\":\"link_device_status\",\"dictLabel\":\"禁用\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:28:32');
INSERT INTO `sys_oper_log` VALUES (134, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"1\",\"listClass\":\"default\",\"dictSort\":1,\"remark\":\"设备禁用\",\"params\":{},\"dictType\":\"link_device_status\",\"dictLabel\":\"禁用\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634891311000,\"dictCode\":33,\"updateBy\":\"admin\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:28:39');
INSERT INTO `sys_oper_log` VALUES (135, '字典类型', 1, 'net.mqtts.system.controller.SysDictTypeController.add()', 'POST', 1, 'admin', NULL, '/dict/type', '127.0.0.1', '', '{\"createBy\":\"admin\",\"dictName\":\"连接状态\",\"remark\":\"设备连接状态\\n\",\"params\":{},\"dictType\":\"link_device_connect_status\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:35:11');
INSERT INTO `sys_oper_log` VALUES (136, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"online\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"设备在线\",\"params\":{},\"dictType\":\"link_device_connect_status\",\"dictLabel\":\"在线\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:35:53');
INSERT INTO `sys_oper_log` VALUES (137, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"offline\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"设备离线\",\"params\":{},\"dictType\":\"link_device_connect_status\",\"dictLabel\":\"离线\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:36:24');
INSERT INTO `sys_oper_log` VALUES (138, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"offline\",\"listClass\":\"default\",\"dictSort\":1,\"remark\":\"设备离线\",\"params\":{},\"dictType\":\"link_device_connect_status\",\"dictLabel\":\"离线\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634891783000,\"dictCode\":35,\"updateBy\":\"admin\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:36:34');
INSERT INTO `sys_oper_log` VALUES (139, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"offline\",\"listClass\":\"default\",\"dictSort\":2,\"remark\":\"设备离线\",\"params\":{},\"dictType\":\"link_device_connect_status\",\"dictLabel\":\"离线\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634891783000,\"dictCode\":35,\"updateBy\":\"admin\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:36:44');
INSERT INTO `sys_oper_log` VALUES (140, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"online\",\"listClass\":\"default\",\"dictSort\":1,\"remark\":\"设备在线\",\"params\":{},\"dictType\":\"link_device_connect_status\",\"dictLabel\":\"在线\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634891753000,\"dictCode\":34,\"updateBy\":\"admin\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:36:48');
INSERT INTO `sys_oper_log` VALUES (141, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"ununited\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"设备未连接\",\"params\":{},\"dictType\":\"link_device_connect_status\",\"dictLabel\":\"未连接\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:37:15');
INSERT INTO `sys_oper_log` VALUES (142, '字典类型', 1, 'net.mqtts.system.controller.SysDictTypeController.add()', 'POST', 1, 'admin', NULL, '/dict/type', '127.0.0.1', '', '{\"createBy\":\"admin\",\"dictName\":\"是否遗言\",\"remark\":\"设备是否有遗言\",\"params\":{},\"dictType\":\"link_device_is_will\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:40:40');
INSERT INTO `sys_oper_log` VALUES (143, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"0\",\"listClass\":\"default\",\"dictSort\":0,\"params\":{},\"dictType\":\"link_device_is_will\",\"dictLabel\":\"是\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:40:55');
INSERT INTO `sys_oper_log` VALUES (144, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"1\",\"listClass\":\"default\",\"dictSort\":0,\"params\":{},\"dictType\":\"link_device_is_will\",\"dictLabel\":\"否\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:41:03');
INSERT INTO `sys_oper_log` VALUES (145, '字典类型', 1, 'net.mqtts.system.controller.SysDictTypeController.add()', 'POST', 1, 'admin', NULL, '/dict/type', '127.0.0.1', '', '{\"createBy\":\"admin\",\"dictName\":\"产品协议类型\",\"remark\":\"产品协议类型 ：mqtt || coap || modbus || http\",\"params\":{},\"dictType\":\"link_device_protocol_type\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:43:24');
INSERT INTO `sys_oper_log` VALUES (146, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"0\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"mqtt\",\"params\":{},\"dictType\":\"link_device_protocol_type\",\"dictLabel\":\"mqtt\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:43:48');
INSERT INTO `sys_oper_log` VALUES (147, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"1\",\"listClass\":\"default\",\"dictSort\":1,\"remark\":\"coap\",\"params\":{},\"dictType\":\"link_device_protocol_type\",\"dictLabel\":\"coap\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:44:02');
INSERT INTO `sys_oper_log` VALUES (148, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"2\",\"listClass\":\"default\",\"dictSort\":2,\"remark\":\"modbus\",\"params\":{},\"dictType\":\"link_device_protocol_type\",\"dictLabel\":\"modbus\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:44:15');
INSERT INTO `sys_oper_log` VALUES (149, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"3\",\"listClass\":\"default\",\"dictSort\":3,\"remark\":\"http\",\"params\":{},\"dictType\":\"link_device_protocol_type\",\"dictLabel\":\"http\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:44:35');
INSERT INTO `sys_oper_log` VALUES (150, '字典类型', 1, 'net.mqtts.system.controller.SysDictTypeController.add()', 'POST', 1, 'admin', NULL, '/dict/type', '127.0.0.1', '', '{\"createBy\":\"admin\",\"dictName\":\"设备类型\",\"remark\":\"设备类型\",\"params\":{},\"dictType\":\"link_device_device_type\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:54:12');
INSERT INTO `sys_oper_log` VALUES (151, '字典类型', 2, 'net.mqtts.system.controller.SysDictTypeController.edit()', 'PUT', 1, 'admin', NULL, '/dict/type', '127.0.0.1', '', '{\"createBy\":\"admin\",\"createTime\":1634892852000,\"updateBy\":\"admin\",\"dictName\":\"设备类型\",\"remark\":\"设备类型0-普通设备（无子设备也无父设备）1-网关设备(可挂载子设备)2-子设备(归属于某个网关设备)。\",\"dictId\":17,\"params\":{},\"dictType\":\"link_device_device_type\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:56:47');
INSERT INTO `sys_oper_log` VALUES (152, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"0\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"普通设备（无子设备也无父设备）\",\"params\":{},\"dictType\":\"link_device_device_type\",\"dictLabel\":\"普通设备\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:57:26');
INSERT INTO `sys_oper_log` VALUES (153, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"1\",\"listClass\":\"default\",\"dictSort\":1,\"remark\":\"网关设备(可挂载子设备)\",\"params\":{},\"dictType\":\"link_device_device_type\",\"dictLabel\":\"网关设备\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:57:44');
INSERT INTO `sys_oper_log` VALUES (154, '字典数据', 1, 'net.mqtts.system.controller.SysDictDataController.add()', 'POST', 1, 'admin', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"2\",\"listClass\":\"default\",\"dictSort\":2,\"remark\":\"子设备(归属于某个网关设备)\",\"params\":{},\"dictType\":\"link_device_device_type\",\"dictLabel\":\"子设备\",\"createBy\":\"admin\",\"default\":false,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 16:58:07');
INSERT INTO `sys_oper_log` VALUES (155, '代码生成', 8, 'net.mqtts.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', NULL, NULL, 0, NULL, '2021-10-22 17:04:34');
INSERT INTO `sys_oper_log` VALUES (156, '设备管理', 1, 'net.mqtts.link.controller.device.MqttsDeviceController.add()', 'POST', 1, 'admin', NULL, '/device', '127.0.0.1', '', '{\"deviceTags\":\"11\",\"latitude\":11,\"remark\":\"11\",\"deviceId\":\"11\",\"deviceName\":\"11\",\"deviceStatus\":\"0\",\"password\":\"1\",\"longitude\":1,\"deviceType\":\"1\",\"clientId\":\"1\",\"connectStatus\":\"ununited\",\"deviceDescription\":\"11\",\"productId\":\"1\",\"manufacturerId\":\"11\",\"protocolType\":\"0\",\"params\":{},\"userName\":\"11\",\"authMode\":\"default\",\"connector\":\"127.0.0.1:11883\",\"createTime\":1634895034632,\"isWill\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-22 17:30:38');
INSERT INTO `sys_oper_log` VALUES (157, '角色管理', 2, 'net.mqtts.system.controller.SysRoleController.edit()', 'PUT', 1, 'admin', NULL, '/role', '58.211.55.186', '', '{\"flag\":false,\"roleId\":2,\"admin\":false,\"remark\":\"普通角色\",\"dataScope\":\"2\",\"delFlag\":\"0\",\"params\":{},\"roleSort\":\"2\",\"deptCheckStrictly\":true,\"createTime\":1631875198000,\"updateBy\":\"admin\",\"menuCheckStrictly\":true,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"menuIds\":[1,100,1001,1002,1003,1004,1005,1006,1007,101,1008,1009,1010,1011,1012,102,1013,1014,1015,1016,103,1017,1018,1019,1020,104,1021,1022,1023,1024,1025,105,1026,1027,1028,1029,1030,106,1031,1032,1033,1034,1035,107,1036,1037,1038,1039,108,500,1040,1041,1042,501,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,3,114,115,1055,1056,1058,1057,1059,1060,116,1061,1062,1063,1064,1065,1066,1067,4],\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-23 23:01:13');
INSERT INTO `sys_oper_log` VALUES (158, '用户头像', 2, 'net.mqtts.system.controller.SysProfileController.avatar()', 'POST', 1, 'admin', NULL, '/user/profile/avatar', '58.211.55.186', '', '', '{\"msg\":\"操作成功\",\"imgUrl\":\"http://218.78.103.93:19300/statics/2021/10/23/32599cc0-ccce-4590-94a4-fa2e1ced3e62.jpeg\",\"code\":200}', 0, NULL, '2021-10-23 23:01:52');
INSERT INTO `sys_oper_log` VALUES (159, '用户头像', 2, 'net.mqtts.system.controller.SysProfileController.avatar()', 'POST', 1, 'admin', NULL, '/user/profile/avatar', '58.211.55.186', '', '', '{\"msg\":\"操作成功\",\"imgUrl\":\"http://218.78.103.93:19300/statics/2021/10/23/fb0b1b4c-42e3-4b1b-966d-32efdae5e2e9.jpeg\",\"code\":200}', 0, NULL, '2021-10-23 23:02:08');
INSERT INTO `sys_oper_log` VALUES (160, '定时任务', 2, 'net.mqtts.job.controller.SysJobController.edit()', 'PUT', 1, 'mqtts', NULL, '/job', '47.242.179.50', '', '{\"jobName\":\"系统默认（无参）\",\"concurrent\":\"1\",\"remark\":\"\",\"jobGroup\":\"DEFAULT\",\"params\":{},\"cronExpression\":\"0/10 * * * * ?\",\"jobId\":1,\"createBy\":\"admin\",\"nextValidTime\":1635102570000,\"createTime\":1631875215000,\"updateBy\":\"mqtts\",\"invokeTarget\":\"ryTask.ryNoParams\",\"misfirePolicy\":\"3\",\"status\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 03:09:23');
INSERT INTO `sys_oper_log` VALUES (161, '代码生成', 8, 'net.mqtts.gen.controller.GenController.batchGenCode()', 'GET', 1, 'mqtts', NULL, '/gen/batchGenCode', '119.39.38.228', '', NULL, NULL, 0, NULL, '2021-10-25 09:55:26');
INSERT INTO `sys_oper_log` VALUES (162, '用户管理', 2, 'net.mqtts.system.controller.SysUserController.changeStatus()', 'PUT', 1, 'mqtts', NULL, '/user/changeStatus', '103.121.165.206', '', '{\"admin\":false,\"updateBy\":\"mqtts\",\"params\":{},\"userId\":2,\"status\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 11:04:31');
INSERT INTO `sys_oper_log` VALUES (163, '用户管理', 2, 'net.mqtts.system.controller.SysUserController.changeStatus()', 'PUT', 1, 'mqtts', NULL, '/user/changeStatus', '103.121.165.206', '', '{\"admin\":false,\"updateBy\":\"mqtts\",\"params\":{},\"userId\":2,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 11:04:34');
INSERT INTO `sys_oper_log` VALUES (164, '用户管理', 2, 'net.mqtts.system.controller.SysUserController.edit()', 'PUT', 1, 'mqtts', NULL, '/user', '103.121.165.206', '', '{\"roles\":[{\"flag\":false,\"roleId\":2,\"admin\":false,\"dataScope\":\"2\",\"params\":{},\"roleSort\":\"2\",\"deptCheckStrictly\":false,\"menuCheckStrictly\":false,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"status\":\"0\"}],\"phonenumber\":\"15666666666\",\"admin\":false,\"loginDate\":1631875198000,\"remark\":\"测试员\",\"delFlag\":\"0\",\"password\":\"\",\"updateBy\":\"mqtts\",\"postIds\":[2],\"loginIp\":\"127.0.0.1\",\"email\":\"ry@qq.com\",\"nickName\":\"mqtts\",\"sex\":\"1\",\"deptId\":105,\"avatar\":\"\",\"dept\":{\"deptName\":\"测试部门\",\"leader\":\"mqtts\",\"deptId\":105,\"orderNum\":\"3\",\"params\":{},\"parentId\":101,\"children\":[],\"status\":\"0\"},\"params\":{},\"userName\":\"ry\",\"userId\":2,\"createBy\":\"admin\",\"roleIds\":[2],\"createTime\":1631875198000,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 11:04:47');
INSERT INTO `sys_oper_log` VALUES (165, '用户管理', 5, 'net.mqtts.system.controller.SysUserController.export()', 'POST', 1, 'mqtts', NULL, '/user/export', '103.121.165.206', '', '{\"admin\":false,\"deptId\":105,\"params\":{\"dataScope\":\" AND (d.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = 2 ) )\"}}', NULL, 0, NULL, '2021-10-25 11:05:05');
INSERT INTO `sys_oper_log` VALUES (166, '用户管理', 5, 'net.mqtts.system.controller.SysUserController.export()', 'POST', 1, 'mqtts', NULL, '/user/export', '103.121.165.206', '', '{\"admin\":false,\"deptId\":105,\"params\":{\"dataScope\":\" AND (d.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = 2 ) )\"}}', NULL, 0, NULL, '2021-10-25 11:05:05');
INSERT INTO `sys_oper_log` VALUES (167, '用户管理', 5, 'net.mqtts.system.controller.SysUserController.export()', 'POST', 1, 'mqtts', NULL, '/user/export', '103.121.165.206', '', '{\"admin\":false,\"deptId\":105,\"params\":{\"dataScope\":\" AND (d.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = 2 ) )\"}}', NULL, 0, NULL, '2021-10-25 11:05:05');
INSERT INTO `sys_oper_log` VALUES (168, '用户管理', 5, 'net.mqtts.system.controller.SysUserController.export()', 'POST', 1, 'mqtts', NULL, '/user/export', '103.121.165.206', '', '{\"admin\":false,\"deptId\":105,\"params\":{\"dataScope\":\" AND (d.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = 2 ) )\"}}', NULL, 0, NULL, '2021-10-25 11:05:05');
INSERT INTO `sys_oper_log` VALUES (169, '代码生成', 8, 'net.mqtts.gen.controller.GenController.batchGenCode()', 'GET', 1, 'mqtts', NULL, '/gen/batchGenCode', '117.62.175.254', '', NULL, NULL, 0, NULL, '2021-10-25 13:06:49');
INSERT INTO `sys_oper_log` VALUES (170, '在线用户', 7, 'net.mqtts.system.controller.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'mqtts', NULL, '/online/0b933c63-88f3-44d8-a2f3-c67b19de7043', '14.205.136.119', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 14:12:23');
INSERT INTO `sys_oper_log` VALUES (171, '在线用户', 7, 'net.mqtts.system.controller.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'mqtts', NULL, '/online/34b9a1cb-8be4-43b1-a376-7e4d87a8c2bd', '14.205.136.119', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 14:12:27');
INSERT INTO `sys_oper_log` VALUES (172, '定时任务', 1, 'net.mqtts.job.controller.SysJobController.add()', 'POST', 1, 'mqtts', NULL, '/job', '14.205.136.119', '', '{\"jobName\":\"a\",\"concurrent\":\"0\",\"jobGroup\":\"SYSTEM\",\"params\":{},\"cronExpression\":\"* * * * * ?\",\"jobId\":4,\"createBy\":\"mqtts\",\"nextValidTime\":1635142428000,\"invokeTarget\":\"ddd\",\"misfirePolicy\":\"1\",\"status\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 14:13:47');
INSERT INTO `sys_oper_log` VALUES (173, '定时任务', 2, 'net.mqtts.job.controller.SysJobController.changeStatus()', 'PUT', 1, 'mqtts', NULL, '/job/changeStatus', '14.205.136.119', '', '{\"params\":{},\"jobId\":1,\"misfirePolicy\":\"0\",\"status\":\"0\"}', NULL, 1, 'Couldn\'t update misfired trigger \'DEFAULT.TASK_CLASS_NAME1\': Couldn\'t store trigger \'DEFAULT.TASK_CLASS_NAME1\' for \'DEFAULT.TASK_CLASS_NAME1\' job:Couldn\'t retrieve job because a required class was not found: net.mqtts.job.domain.SysJob', '2021-10-25 14:14:08');
INSERT INTO `sys_oper_log` VALUES (174, '参数管理', 9, 'net.mqtts.system.controller.SysConfigController.refreshCache()', 'DELETE', 1, 'mqtts', NULL, '/config/refreshCache', '61.175.185.178', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 14:40:13');
INSERT INTO `sys_oper_log` VALUES (175, '字典类型', 2, 'net.mqtts.system.controller.SysDictTypeController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/type', '36.154.102.227', '', '{\"createBy\":\"admin\",\"createTime\":1634891248000,\"updateBy\":\"mqtts\",\"dictName\":\"设备状态\",\"remark\":\"设备状态\",\"dictId\":13,\"params\":{},\"dictType\":\"link_device_status\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 15:48:58');
INSERT INTO `sys_oper_log` VALUES (176, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"ENABLE\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"设备启用\",\"params\":{},\"dictType\":\"link_device_status\",\"dictLabel\":\"启用\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634891293000,\"dictCode\":32,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 15:51:25');
INSERT INTO `sys_oper_log` VALUES (177, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"DISABLE\",\"listClass\":\"default\",\"dictSort\":1,\"remark\":\"设备禁用\",\"params\":{},\"dictType\":\"link_device_status\",\"dictLabel\":\"禁用\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634891311000,\"dictCode\":33,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 15:52:16');
INSERT INTO `sys_oper_log` VALUES (178, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"INIT\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"设备未连接\",\"params\":{},\"dictType\":\"link_device_connect_status\",\"dictLabel\":\"未连接\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634891835000,\"dictCode\":36,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 15:55:45');
INSERT INTO `sys_oper_log` VALUES (179, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"ONLINE\",\"listClass\":\"default\",\"dictSort\":1,\"remark\":\"设备在线\",\"params\":{},\"dictType\":\"link_device_connect_status\",\"dictLabel\":\"在线\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634891753000,\"dictCode\":34,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 15:56:05');
INSERT INTO `sys_oper_log` VALUES (180, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"OFFLINE\",\"listClass\":\"default\",\"dictSort\":2,\"remark\":\"设备离线\",\"params\":{},\"dictType\":\"link_device_connect_status\",\"dictLabel\":\"离线\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634891783000,\"dictCode\":35,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 15:56:12');
INSERT INTO `sys_oper_log` VALUES (181, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"MQTT\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"mqtt\",\"params\":{},\"dictType\":\"link_device_protocol_type\",\"dictLabel\":\"mqtt\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634892228000,\"dictCode\":39,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 15:59:31');
INSERT INTO `sys_oper_log` VALUES (182, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"COAP\",\"listClass\":\"default\",\"dictSort\":1,\"remark\":\"coap\",\"params\":{},\"dictType\":\"link_device_protocol_type\",\"dictLabel\":\"coap\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634892242000,\"dictCode\":40,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 15:59:39');
INSERT INTO `sys_oper_log` VALUES (183, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"MODBUS\",\"listClass\":\"default\",\"dictSort\":2,\"remark\":\"modbus\",\"params\":{},\"dictType\":\"link_device_protocol_type\",\"dictLabel\":\"modbus\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634892255000,\"dictCode\":41,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 15:59:53');
INSERT INTO `sys_oper_log` VALUES (184, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"HTTP\",\"listClass\":\"default\",\"dictSort\":3,\"remark\":\"http\",\"params\":{},\"dictType\":\"link_device_protocol_type\",\"dictLabel\":\"http\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634892275000,\"dictCode\":42,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 15:59:59');
INSERT INTO `sys_oper_log` VALUES (185, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"SUBSET\",\"listClass\":\"default\",\"dictSort\":2,\"remark\":\"子设备(归属于某个网关设备)\",\"params\":{},\"dictType\":\"link_device_device_type\",\"dictLabel\":\"子设备\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634893086000,\"dictCode\":45,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 16:18:25');
INSERT INTO `sys_oper_log` VALUES (186, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"COMMON\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"普通设备（无子设备也无父设备）\",\"params\":{},\"dictType\":\"link_device_device_type\",\"dictLabel\":\"普通设备\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634893046000,\"dictCode\":43,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 16:20:15');
INSERT INTO `sys_oper_log` VALUES (187, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '36.154.102.227', '', '{\"dictValue\":\"GATEWAY\",\"listClass\":\"default\",\"dictSort\":1,\"remark\":\"网关设备(可挂载子设备)\",\"params\":{},\"dictType\":\"link_device_device_type\",\"dictLabel\":\"网关设备\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634893064000,\"dictCode\":44,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 16:20:47');
INSERT INTO `sys_oper_log` VALUES (188, '代码生成', 2, 'net.mqtts.gen.controller.GenController.synchDb()', 'GET', 1, 'mqtts', NULL, '/gen/synchDb/mqtts_device', '222.129.38.226', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 16:31:39');
INSERT INTO `sys_oper_log` VALUES (189, '代码生成', 8, 'net.mqtts.gen.controller.GenController.batchGenCode()', 'GET', 1, 'mqtts', NULL, '/gen/batchGenCode', '222.129.38.226', '', NULL, NULL, 0, NULL, '2021-10-25 16:31:41');
INSERT INTO `sys_oper_log` VALUES (190, '在线用户', 7, 'net.mqtts.system.controller.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'mqtts', NULL, '/online/a9f486b4-03f7-4354-b863-480a163724e0', '36.112.12.189', '', NULL, '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 17:24:49');
INSERT INTO `sys_oper_log` VALUES (191, '设备管理', 1, 'net.mqtts.link.controller.device.MqttsDeviceController.add()', 'POST', 1, 'mqtts', NULL, '/device', '36.154.102.227', '', '{\"deviceTags\":\"123\",\"latitude\":123.12,\"remark\":\"123\",\"deviceId\":\"123\",\"deviceName\":\"123\",\"deviceStatus\":\"ENABLE\",\"password\":\"123\",\"longitude\":123.12,\"deviceType\":\"GATEWAY\",\"clientId\":\"123\",\"connectStatus\":\"INIT\",\"deviceDescription\":\"123\",\"productId\":\"123\",\"manufacturerId\":\"123\",\"protocolType\":\"MQTT\",\"params\":{},\"userName\":\"123\",\"authMode\":\"default\",\"createBy\":\"mqtts\",\"connector\":\"127.0.0.1:11883\",\"createTime\":1635155535612,\"isWill\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-25 17:52:16');
INSERT INTO `sys_oper_log` VALUES (192, '字典数据', 2, 'net.mqtts.system.controller.SysDictDataController.edit()', 'PUT', 1, 'mqtts', NULL, '/dict/data', '127.0.0.1', '', '{\"dictValue\":\"218.78.103.93:11883\",\"listClass\":\"default\",\"dictSort\":0,\"remark\":\"默认节点Node1\",\"params\":{},\"dictType\":\"link_device_connector\",\"dictLabel\":\"218.78.103.93:11883\",\"createBy\":\"admin\",\"default\":false,\"isDefault\":\"N\",\"createTime\":1634811086000,\"dictCode\":31,\"updateBy\":\"mqtts\",\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-27 13:02:20');
INSERT INTO `sys_oper_log` VALUES (193, '设备管理', 2, 'net.mqtts.link.controller.device.MqttsDeviceController.edit()', 'PUT', 1, 'mqtts', NULL, '/device', '127.0.0.1', '', '{\"deviceTags\":\"123\",\"latitude\":123.12,\"remark\":\"123\",\"deviceId\":\"123\",\"deviceName\":\"123\",\"deviceStatus\":\"ENABLE\",\"password\":\"123\",\"updateBy\":\"mqtts\",\"id\":2,\"longitude\":123.12,\"deviceType\":\"GATEWAY\",\"clientId\":\"123\",\"connectStatus\":\"ONLINE\",\"deviceDescription\":\"123\",\"productId\":\"123\",\"manufacturerId\":\"123\",\"updateTime\":1635404837302,\"protocolType\":\"MQTT\",\"params\":{},\"userName\":\"123\",\"authMode\":\"default\",\"createBy\":\"mqtts\",\"connector\":\"218.78.103.93:11883\",\"createTime\":1635155536000,\"isWill\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-28 15:07:18');
INSERT INTO `sys_oper_log` VALUES (194, '设备管理', 2, 'net.mqtts.link.controller.device.MqttsDeviceController.edit()', 'PUT', 1, 'mqtts', NULL, '/device', '127.0.0.1', '', '{\"deviceTags\":\"123\",\"latitude\":123.12,\"remark\":\"123\",\"deviceId\":\"123\",\"deviceName\":\"123\",\"deviceStatus\":\"ENABLE\",\"password\":\"123\",\"updateBy\":\"mqtts\",\"id\":2,\"longitude\":123.12,\"deviceType\":\"GATEWAY\",\"clientId\":\"123\",\"connectStatus\":\"ONLINE\",\"deviceDescription\":\"123\",\"productId\":\"123\",\"manufacturerId\":\"123\",\"updateTime\":1635404842510,\"protocolType\":\"MQTT\",\"params\":{},\"userName\":\"123\",\"authMode\":\"default\",\"createBy\":\"mqtts\",\"connector\":\"218.78.103.93:11883\",\"createTime\":1635155536000,\"isWill\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2021-10-28 15:07:22');

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
                             `post_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
                             `post_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '岗位编码',
                             `post_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '岗位名称',
                             `post_sort` int(4) NOT NULL COMMENT '显示顺序',
                             `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
                             `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                             PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '岗位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2021-09-17 18:39:58', '', NULL, '');
INSERT INTO `sys_post` VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2021-09-17 18:39:58', '', NULL, '');
INSERT INTO `sys_post` VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2021-09-17 18:39:58', '', NULL, '');
INSERT INTO `sys_post` VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2021-09-17 18:39:58', '', NULL, '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
                             `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                             `role_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名称',
                             `role_key` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色权限字符串',
                             `role_sort` int(4) NOT NULL COMMENT '显示顺序',
                             `data_scope` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
                             `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
                             `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
                             `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
                             `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                             `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                             PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2021-09-17 18:39:58', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '0', 'admin', '2021-09-17 18:39:58', 'admin', '2021-10-23 23:01:13', '普通角色');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
                                  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
                                  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色和部门关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept` VALUES (2, 100);
INSERT INTO `sys_role_dept` VALUES (2, 101);
INSERT INTO `sys_role_dept` VALUES (2, 105);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
                                  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
                                  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (2, 1);
INSERT INTO `sys_role_menu` VALUES (2, 2);
INSERT INTO `sys_role_menu` VALUES (2, 3);
INSERT INTO `sys_role_menu` VALUES (2, 4);
INSERT INTO `sys_role_menu` VALUES (2, 100);
INSERT INTO `sys_role_menu` VALUES (2, 101);
INSERT INTO `sys_role_menu` VALUES (2, 102);
INSERT INTO `sys_role_menu` VALUES (2, 103);
INSERT INTO `sys_role_menu` VALUES (2, 104);
INSERT INTO `sys_role_menu` VALUES (2, 105);
INSERT INTO `sys_role_menu` VALUES (2, 106);
INSERT INTO `sys_role_menu` VALUES (2, 107);
INSERT INTO `sys_role_menu` VALUES (2, 108);
INSERT INTO `sys_role_menu` VALUES (2, 109);
INSERT INTO `sys_role_menu` VALUES (2, 110);
INSERT INTO `sys_role_menu` VALUES (2, 111);
INSERT INTO `sys_role_menu` VALUES (2, 112);
INSERT INTO `sys_role_menu` VALUES (2, 113);
INSERT INTO `sys_role_menu` VALUES (2, 114);
INSERT INTO `sys_role_menu` VALUES (2, 115);
INSERT INTO `sys_role_menu` VALUES (2, 116);
INSERT INTO `sys_role_menu` VALUES (2, 500);
INSERT INTO `sys_role_menu` VALUES (2, 501);
INSERT INTO `sys_role_menu` VALUES (2, 1001);
INSERT INTO `sys_role_menu` VALUES (2, 1002);
INSERT INTO `sys_role_menu` VALUES (2, 1003);
INSERT INTO `sys_role_menu` VALUES (2, 1004);
INSERT INTO `sys_role_menu` VALUES (2, 1005);
INSERT INTO `sys_role_menu` VALUES (2, 1006);
INSERT INTO `sys_role_menu` VALUES (2, 1007);
INSERT INTO `sys_role_menu` VALUES (2, 1008);
INSERT INTO `sys_role_menu` VALUES (2, 1009);
INSERT INTO `sys_role_menu` VALUES (2, 1010);
INSERT INTO `sys_role_menu` VALUES (2, 1011);
INSERT INTO `sys_role_menu` VALUES (2, 1012);
INSERT INTO `sys_role_menu` VALUES (2, 1013);
INSERT INTO `sys_role_menu` VALUES (2, 1014);
INSERT INTO `sys_role_menu` VALUES (2, 1015);
INSERT INTO `sys_role_menu` VALUES (2, 1016);
INSERT INTO `sys_role_menu` VALUES (2, 1017);
INSERT INTO `sys_role_menu` VALUES (2, 1018);
INSERT INTO `sys_role_menu` VALUES (2, 1019);
INSERT INTO `sys_role_menu` VALUES (2, 1020);
INSERT INTO `sys_role_menu` VALUES (2, 1021);
INSERT INTO `sys_role_menu` VALUES (2, 1022);
INSERT INTO `sys_role_menu` VALUES (2, 1023);
INSERT INTO `sys_role_menu` VALUES (2, 1024);
INSERT INTO `sys_role_menu` VALUES (2, 1025);
INSERT INTO `sys_role_menu` VALUES (2, 1026);
INSERT INTO `sys_role_menu` VALUES (2, 1027);
INSERT INTO `sys_role_menu` VALUES (2, 1028);
INSERT INTO `sys_role_menu` VALUES (2, 1029);
INSERT INTO `sys_role_menu` VALUES (2, 1030);
INSERT INTO `sys_role_menu` VALUES (2, 1031);
INSERT INTO `sys_role_menu` VALUES (2, 1032);
INSERT INTO `sys_role_menu` VALUES (2, 1033);
INSERT INTO `sys_role_menu` VALUES (2, 1034);
INSERT INTO `sys_role_menu` VALUES (2, 1035);
INSERT INTO `sys_role_menu` VALUES (2, 1036);
INSERT INTO `sys_role_menu` VALUES (2, 1037);
INSERT INTO `sys_role_menu` VALUES (2, 1038);
INSERT INTO `sys_role_menu` VALUES (2, 1039);
INSERT INTO `sys_role_menu` VALUES (2, 1040);
INSERT INTO `sys_role_menu` VALUES (2, 1041);
INSERT INTO `sys_role_menu` VALUES (2, 1042);
INSERT INTO `sys_role_menu` VALUES (2, 1043);
INSERT INTO `sys_role_menu` VALUES (2, 1044);
INSERT INTO `sys_role_menu` VALUES (2, 1045);
INSERT INTO `sys_role_menu` VALUES (2, 1046);
INSERT INTO `sys_role_menu` VALUES (2, 1047);
INSERT INTO `sys_role_menu` VALUES (2, 1048);
INSERT INTO `sys_role_menu` VALUES (2, 1049);
INSERT INTO `sys_role_menu` VALUES (2, 1050);
INSERT INTO `sys_role_menu` VALUES (2, 1051);
INSERT INTO `sys_role_menu` VALUES (2, 1052);
INSERT INTO `sys_role_menu` VALUES (2, 1053);
INSERT INTO `sys_role_menu` VALUES (2, 1054);
INSERT INTO `sys_role_menu` VALUES (2, 1055);
INSERT INTO `sys_role_menu` VALUES (2, 1056);
INSERT INTO `sys_role_menu` VALUES (2, 1057);
INSERT INTO `sys_role_menu` VALUES (2, 1058);
INSERT INTO `sys_role_menu` VALUES (2, 1059);
INSERT INTO `sys_role_menu` VALUES (2, 1060);
INSERT INTO `sys_role_menu` VALUES (2, 1061);
INSERT INTO `sys_role_menu` VALUES (2, 1062);
INSERT INTO `sys_role_menu` VALUES (2, 1063);
INSERT INTO `sys_role_menu` VALUES (2, 1064);
INSERT INTO `sys_role_menu` VALUES (2, 1065);
INSERT INTO `sys_role_menu` VALUES (2, 1066);
INSERT INTO `sys_role_menu` VALUES (2, 1067);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
                             `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                             `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID',
                             `user_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户账号',
                             `nick_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户昵称',
                             `user_type` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '00' COMMENT '用户类型（00系统用户）',
                             `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '用户邮箱',
                             `phonenumber` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '手机号码',
                             `sex` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
                             `avatar` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '头像地址',
                             `password` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '密码',
                             `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
                             `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                             `login_ip` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '最后登录IP',
                             `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
                             `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                             PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', 'mqtts', '00', 'mqtts@163.com', '15888888888', '0', 'http://218.78.103.93:19300/statics/2021/10/23/fb0b1b4c-42e3-4b1b-966d-32efdae5e2e9.jpeg', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2021-09-17 18:39:57', 'admin', '2021-09-17 18:39:57', '', '2021-09-28 14:21:29', '管理员');
INSERT INTO `sys_user` VALUES (2, 105, 'ry', 'mqtts', '00', 'ry@qq.com', '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2021-09-17 18:39:58', 'admin', '2021-09-17 18:39:58', 'mqtts', '2021-10-25 11:04:47', '测试员');
INSERT INTO `sys_user` VALUES (3, 100, 'mqtts', 'mqtts', '00', 'mqttsnet@163.com', '', '2', '', '$2a$10$oeiU9tYwmguNECk.RbslvuNz0s1bCEcVf5Hj/I1Fok8p0YWUthB3y', '0', '0', '', NULL, 'admin', '2021-10-21 17:03:52', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
                                  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                  `post_id` bigint(20) NOT NULL COMMENT '岗位ID',
                                  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1);
INSERT INTO `sys_user_post` VALUES (2, 2);
INSERT INTO `sys_user_post` VALUES (3, 2);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
                                  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户和角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);
INSERT INTO `sys_user_role` VALUES (3, 2);

SET FOREIGN_KEY_CHECKS = 1;
