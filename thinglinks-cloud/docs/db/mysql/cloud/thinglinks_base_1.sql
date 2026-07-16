/*
 Navicat Premium Dump SQL

 Source Server         : 开源版本
 Source Server Type    : MySQL
 Source Server Version : 80408 (8.4.8)
 Source Host           : 127.0.0.1:13306
 Source Schema         : thinglinks_base_1

 Target Server Type    : MySQL
 Target Server Version : 80408 (8.4.8)
 File Encoding         : 65001

 Date: 27/03/2026 09:45:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for base_dict
-- ----------------------------
DROP TABLE IF EXISTS `base_dict`;
CREATE TABLE `base_dict` (
  `id` bigint NOT NULL COMMENT 'ID',
  `parent_id` bigint NOT NULL COMMENT '所属字典',
  `parent_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '所属字典标识',
  `dict_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典分组',
  `key_` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '标识',
  `classify` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '20' COMMENT '分类;[10-系统字典 20-业务字典]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.DICT_CLASSIFY)',
  `data_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '数据类型\n[1-字符串 2-整型 3-布尔]',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '名称',
  `state` bit(1) DEFAULT b'1' COMMENT '状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `sort_value` int DEFAULT '1' COMMENT '排序',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '图标',
  `css_style` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'css样式',
  `css_class` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'css类元素',
  `prop_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '组件属性\n用于Tag时，用于配置color属性\n用于Button时，用于配置type属性',
  `i18n_json` varchar(5120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '国际化配置',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典';

-- ----------------------------
-- Records of base_dict
-- ----------------------------
BEGIN;
INSERT INTO `base_dict` (`id`, `parent_id`, `parent_key`, `dict_group`, `key_`, `classify`, `data_type`, `name`, `state`, `remark`, `sort_value`, `icon`, `css_style`, `css_class`, `prop_type`, `i18n_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (475671003876294678, 0, '', NULL, 'RULE_INSTANCE_ADDRESS', '10', '1', '规则实例地址', b'1', '规则实例地址', 1, '', '', '', '', '{\"zh-CN\": \"规则实例地址\", \"en-US\": \"Rule Instance Address\", \"ja\": \"ルールインスタンスアドレス\"}', 1452186486253289472, '2024-04-04 22:43:52', 1452186486253289472, '2024-04-04 22:43:52', NULL);
INSERT INTO `base_dict` (`id`, `parent_id`, `parent_key`, `dict_group`, `key_`, `classify`, `data_type`, `name`, `state`, `remark`, `sort_value`, `icon`, `css_style`, `css_class`, `prop_type`, `i18n_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (497973033877635275, 475671003876294678, 'RULE_INSTANCE_ADDRESS', NULL, 'http://125.122.29.104:18803', '20', '1', '官方实例(IP模式)', b'1', '', 2, '', '', '', '', '{\"zh-CN\": \"官方实例(IP模式)\", \"en-US\": \"Official Instance (IP Mode)\", \"ja\": \"公式インスタンス(IPモード)\"}', 1452186486253289472, '2024-05-21 16:26:22', 1452186486253289472, '2025-09-19 14:44:02', NULL);
INSERT INTO `base_dict` (`id`, `parent_id`, `parent_key`, `dict_group`, `key_`, `classify`, `data_type`, `name`, `state`, `remark`, `sort_value`, `icon`, `css_style`, `css_class`, `prop_type`, `i18n_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (502678509857846274, 0, '', NULL, 'TENANT_AMAP__AAPPLICATION_JS_API_KEY', '10', '1', '高德应用JS开放APIKEY', b'1', '高德地图应用Web端(JS API)相关参数', 1, '', '', '', '', '{\"zh-CN\": \"高德应用JS开放APIKEY\", \"en-US\": \"AMap JS API Key\", \"ja\": \"AMap JS APIキー\"}', 1452186486253289472, '2024-06-05 15:58:27', 1452186486253289472, '2024-06-05 15:58:27', NULL);
INSERT INTO `base_dict` (`id`, `parent_id`, `parent_key`, `dict_group`, `key_`, `classify`, `data_type`, `name`, `state`, `remark`, `sort_value`, `icon`, `css_style`, `css_class`, `prop_type`, `i18n_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (502678509857846275, 502678509857846274, 'TENANT_AMAP__AAPPLICATION_JS_API_KEY', NULL, 'key', '10', '1', 'xxx', b'1', 'KEY', 1, '', '', '', '', '{\"zh-CN\": \"your_key\", \"en-US\": \"your_key\", \"ja\": \"your_key\"}', 1452186486253289472, '2024-06-05 16:03:18', 1452186486253289472, '2024-06-11 22:04:39', NULL);
INSERT INTO `base_dict` (`id`, `parent_id`, `parent_key`, `dict_group`, `key_`, `classify`, `data_type`, `name`, `state`, `remark`, `sort_value`, `icon`, `css_style`, `css_class`, `prop_type`, `i18n_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (502678509857846276, 502678509857846274, 'TENANT_AMAP__AAPPLICATION_JS_API_KEY', NULL, 'securityJsCode', '10', '1', 'xxx', b'1', '安全密钥', 1, '', '', '', '', '{\"zh-CN\": \"your_securityJsCode\", \"en-US\": \"your_securityJsCode\", \"ja\": \"your_securityJsCode\"}', 1452186486253289472, '2024-06-05 16:04:03', 1452186486253289472, '2024-06-11 22:04:47', NULL);
INSERT INTO `base_dict` (`id`, `parent_id`, `parent_key`, `dict_group`, `key_`, `classify`, `data_type`, `name`, `state`, `remark`, `sort_value`, `icon`, `css_style`, `css_class`, `prop_type`, `i18n_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (674535346988355585, 475671003876294678, 'RULE_INSTANCE_ADDRESS', NULL, 'https://r1.nodered.thinglinks.mqttsnet.com', '20', '1', 'R1测试实例节点', b'1', 'https://r1.nodered.thinglinks.mqttsnet.com', 1, '', '', '', '', '{\"zh-CN\": \"R1测试实例节点\", \"en-US\": \"R1 Test Instance Node\", \"ja\": \"R1テストインスタンスノード\"}', 1452186486253289472, '2025-09-07 01:48:27', 1452186486253289472, '2025-09-19 14:43:16', NULL);
COMMIT;

-- ----------------------------
-- Table structure for base_employee
-- ----------------------------
DROP TABLE IF EXISTS `base_employee`;
CREATE TABLE `base_employee` (
  `id` bigint NOT NULL COMMENT 'ID',
  `is_default` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否默认员工;[0-否 1-是]',
  `position_id` bigint DEFAULT NULL COMMENT '所属岗位',
  `user_id` bigint NOT NULL COMMENT '用户',
  `last_company_id` bigint DEFAULT NULL COMMENT '上一次登录单位ID',
  `last_dept_id` bigint DEFAULT NULL COMMENT '上一次登录部门ID',
  `real_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '真实姓名',
  `active_status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '20' COMMENT '激活状态;[10-未激活 20-已激活]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Global.ACTIVE_STATUS)',
  `position_status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '10' COMMENT '职位状态;[10-在职 20-离职]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.POSITION_STATUS)',
  `state` bit(1) DEFAULT b'1' COMMENT '状态;[0-禁用 1-启用]',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_emp_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='员工';

-- ----------------------------
-- Records of base_employee
-- ----------------------------
BEGIN;
INSERT INTO `base_employee` (`id`, `is_default`, `position_id`, `user_id`, `last_company_id`, `last_dept_id`, `real_name`, `active_status`, `position_status`, `state`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (339843960220418056, b'0', NULL, 1452186486253289472, NULL, NULL, '哈哈啊', '20', '10', b'1', 1452186486253289472, '2023-03-19 19:40:47', 1452186486253289472, '2023-03-19 19:40:47', 1);
INSERT INTO `base_employee` (`id`, `is_default`, `position_id`, `user_id`, `last_company_id`, `last_dept_id`, `real_name`, `active_status`, `position_status`, `state`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (543809234108564219, b'0', NULL, 442312581298783074, NULL, NULL, 'ThingLinks测试', '20', '10', b'1', 1452186486253289472, '2024-10-09 12:51:31', 1452186486253289472, '2024-10-09 12:51:31', NULL);
COMMIT;

-- ----------------------------
-- Table structure for base_employee_org_rel
-- ----------------------------
DROP TABLE IF EXISTS `base_employee_org_rel`;
CREATE TABLE `base_employee_org_rel` (
  `id` bigint NOT NULL COMMENT 'ID',
  `org_id` bigint NOT NULL COMMENT '关联机构',
  `employee_id` bigint NOT NULL COMMENT '关联员工',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_employee_org` (`org_id`,`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='员工所在部门';

-- ----------------------------
-- Records of base_employee_org_rel
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for base_employee_role_rel
-- ----------------------------
DROP TABLE IF EXISTS `base_employee_role_rel`;
CREATE TABLE `base_employee_role_rel` (
  `id` bigint NOT NULL COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '拥有角色;#base_role',
  `employee_id` bigint NOT NULL COMMENT '所属员工;#base_employee',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_err_role_employee` (`role_id`,`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='员工的角色';

-- ----------------------------
-- Records of base_employee_role_rel
-- ----------------------------
BEGIN;
INSERT INTO `base_employee_role_rel` (`id`, `role_id`, `employee_id`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (339843960220418058, 1452496398934081536, 339843960220418056, 1452186486253289472, '2023-03-19 19:40:48', 1452186486253289472, '2023-03-19 19:40:48', NULL);
INSERT INTO `base_employee_role_rel` (`id`, `role_id`, `employee_id`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (739397474948784136, 739397474948784128, 543809234108564219, 1452186486253289472, '2026-03-04 18:13:09', 1452186486253289472, '2026-03-04 18:13:09', NULL);
COMMIT;

-- ----------------------------
-- Table structure for base_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `base_operation_log`;
CREATE TABLE `base_operation_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `request_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作IP',
  `type` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'OPT' COMMENT '日志类型;#LogType{OPT:操作类型;EX:异常类型}',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作人',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作描述',
  `class_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '类路径',
  `action_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求方法',
  `request_uri` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求地址',
  `http_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'GET' COMMENT '请求类型;#HttpMethod{GET:GET请求;POST:POST请求;PUT:PUT请求;DELETE:DELETE请求;PATCH:PATCH请求;TRACE:TRACE请求;HEAD:HEAD请求;OPTIONS:OPTIONS请求;}',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `finish_time` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `consuming_time` bigint DEFAULT '0' COMMENT '消耗时间',
  `ua` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '浏览器',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后更新人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志';

-- ----------------------------
-- Records of base_operation_log
-- ----------------------------
BEGIN;
INSERT INTO `base_operation_log` (`id`, `request_ip`, `type`, `user_name`, `description`, `class_path`, `action_method`, `request_uri`, `http_method`, `start_time`, `finish_time`, `consuming_time`, `ua`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (749122831390229504, '127.0.0.1', 'OPT', '', '文件上传-根据文件id，获取文件临时的访问路径', 'com.mqttsnet.thinglinks.file.controller.FileAnyoneController', 'findUrlById', '/anyone/file/findUrlById', 'POST', '2026-03-26 17:39:41', '2026-03-26 17:39:42', 294, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '2026-03-26 17:39:42', 1452186486253289472, '2026-03-26 17:39:42', 1452186486253289472, 0);
INSERT INTO `base_operation_log` (`id`, `request_ip`, `type`, `user_name`, `description`, `class_path`, `action_method`, `request_uri`, `http_method`, `start_time`, `finish_time`, `consuming_time`, `ua`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (749122831390229505, '127.0.0.1', 'OPT', '', '需要登录但无需验证uri权限的接口-查询我的应用', 'com.mqttsnet.thinglinks.system.controller.anyone.TenantAnyoneController', 'findMyApplication', '/anyone/findMyApplication', 'GET', '2026-03-26 17:39:41', '2026-03-26 17:39:42', 819, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '2026-03-26 17:39:42', 1452186486253289472, '2026-03-26 17:39:42', 1452186486253289472, 0);
INSERT INTO `base_operation_log` (`id`, `request_ip`, `type`, `user_name`, `description`, `class_path`, `action_method`, `request_uri`, `http_method`, `start_time`, `finish_time`, `consuming_time`, `ua`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (749122831390229506, '127.0.0.1', 'OPT', '', '文件上传-根据文件id，获取文件临时的访问路径', 'com.mqttsnet.thinglinks.file.controller.FileAnyoneController', 'findUrlById', '/anyone/file/findUrlById', 'POST', '2026-03-26 17:39:43', '2026-03-26 17:39:43', 214, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '2026-03-26 17:39:43', 1452186486253289472, '2026-03-26 17:39:43', 1452186486253289472, 0);
INSERT INTO `base_operation_log` (`id`, `request_ip`, `type`, `user_name`, `description`, `class_path`, `action_method`, `request_uri`, `http_method`, `start_time`, `finish_time`, `consuming_time`, `ua`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (749122831390229507, '127.0.0.1', 'OPT', '', '文件上传-根据文件id，获取文件临时的访问路径', 'com.mqttsnet.thinglinks.file.controller.FileAnyoneController', 'findUrlById', '/anyone/file/findUrlById', 'POST', '2026-03-26 17:39:44', '2026-03-26 17:39:44', 218, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '2026-03-26 17:39:44', 1452186486253289472, '2026-03-26 17:39:44', 1452186486253289472, 0);
INSERT INTO `base_operation_log` (`id`, `request_ip`, `type`, `user_name`, `description`, `class_path`, `action_method`, `request_uri`, `http_method`, `start_time`, `finish_time`, `consuming_time`, `ua`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (749122831390229508, '127.0.0.1', 'OPT', '', '文件上传-根据文件id，获取文件临时的访问路径', 'com.mqttsnet.thinglinks.file.controller.FileAnyoneController', 'findUrlById', '/anyone/file/findUrlById', 'POST', '2026-03-26 17:39:46', '2026-03-26 17:39:46', 212, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '2026-03-26 17:39:46', 1452186486253289472, '2026-03-26 17:39:46', 1452186486253289472, 0);
COMMIT;

-- ----------------------------
-- Table structure for base_operation_log_ext
-- ----------------------------
DROP TABLE IF EXISTS `base_operation_log_ext`;
CREATE TABLE `base_operation_log_ext` (
  `id` bigint NOT NULL COMMENT '主键',
  `params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '请求参数',
  `result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '返回值',
  `ex_detail` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '异常描述',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后更新人ID',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志扩展';

-- ----------------------------
-- Records of base_operation_log_ext
-- ----------------------------
BEGIN;
INSERT INTO `base_operation_log_ext` (`id`, `params`, `result`, `ex_detail`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (749122831390229504, '[[\"670471852653070336\"]]', '{\"code\":0,\"data\":{},\"msg\":\"ok\",\"path\":null,\"extra\":null,\"timestamp\":\"1774517981719\",\"errorMsg\":\"\",\"trace\":\"abf3b19fff37419491818f6d77f5de08\",\"isSuccess\":true}', NULL, '2026-03-26 17:39:42', 1452186486253289472, '2026-03-26 17:39:42', 1452186486253289472, NULL);
INSERT INTO `base_operation_log_ext` (`id`, `params`, `result`, `ex_detail`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (749122831390229505, '[null, null]', '{\"code\":0,\"data\":[{\"id\":\"1\",\"createdTime\":\"2021-12-12 12:12:12\",\"createdBy\":\"2\",\"updatedTime\":\"2024-10-22 00:24:08\",\"updatedBy\":\"1452186486253289472\",\"echoMap\":{\"DEF__APPLICATION__LOGO\":[],\"type\":\"自建应用\"},\"appKey\":\"basicPlatform\",\"appSecret\":\"uhpe70w9rw0qjyp1hd6rae58ioa7anycc00p\",\"name\":\"基础平台\",\"version\":\"v1.0\",\"type\":\"10\",\"introduce\":\"租户的工作台，最基础的功能。\",\"redirect\":null,\"remark\":\"基础平台是整个平台最基础，最核心的功能，所有租户都拥有。可以理解为用户的工作台，跳转其他业务系统的控制台等。\",\"url\":\"\",\"isVisible\":true,\"sortValue\":1,\"isGeneral\":null,\"expirationTime\":null,\"state\":\"1\"},{\"id\":\"2\",\"createdTime\":\"2021-12-12 12:12:12\",\"createdBy\":\"2\",\"updatedTime\":\"2024-10-22 00:24:13\",\"updatedBy\":\"1452186486253289472\",\"echoMap\":{\"DEF__APPLICATION__LOGO\":[],\"type\":\"自建应用\"},\"appKey\":\"devOperation\",\"appSecret\":\"ymyqj01qvmz7bpkne5li81cvxma2bebrzb57\",\"name\":\"开发运营系统\",\"version\":\"v1.0\",\"type\":\"10\",\"introduce\":\"开发者或运营者使用，系统级功能，不能分配给租户。\",\"redirect\":null,\"remark\":\"开发运营系统是给 开发者和运营者公司的用户使用的，主要维护一些系统级的配置和数据，不能分配给普通租户使用。\",\"url\":\"\",\"isVisible\":true,\"sortValue\":2,\"isGeneral\":null,\"expirationTime\":null,\"state\":\"1\"},{\"id\":\"3\",\"createdTime\":\"2021-12-12 12:12:12\",\"createdBy\":\"2\",\"updatedTime\":\"2024-10-22 00:24:19\",\"updatedBy\":\"1452186486253289472\",\"echoMap\":{\"DEF__APPLICATION__LOGO\":[],\"type\":\"自建应用\"},\"appKey\":\"iotSystem\",\"appSecret\":\"c2mn7qb9i194mcypuletfxv8qe182e61awut\",\"name\":\"物联网业务系统\",\"version\":\"v1.0\",\"type\":\"10\",\"introduce\":\"物联网业务系统，租户需要购买方可使用。\",\"redirect\":null,\"remark\":\"设备管理、产品管理、规则引擎、协议管理等功能\",\"url\":\"\",\"isVisible\":true,\"sortValue\":3,\"isGeneral\":null,\"expirationTime\":null,\"state\":\"1\"},{\"id\":\"343406236160491520\",\"createdTime\":\"2023-03-29 13:03:55\",\"createdBy\":\"1452186486253289472\",\"updatedTime\":\"2024-10-22 00:24:27\",\"updatedBy\":\"1452186486253289472\",\"echoMap\":{\"DEF__APPLICATION__LOGO\":[],\"type\":\"自建应用\"},\"appKey\":\"iotCardSystem\",\"appSecret\":\"63xu88xz5eahdmn9959mj247dz35tlk7rd85\",\"name\":\"物联卡业务系统\",\"version\":\"v1.0\",\"type\":\"10\",\"introduce\":\"用于实现物联卡信息查询充值的管理平台\",\"redirect\":null,\"remark\":\"用于实现物联卡信息查询充值的管理平台，物联卡业务系统对接了中国联通，中国电信，中国移动物联网平台，充值流量实时到账，卡状态，流量实时同步，接入流量池，短信发送等\",\"url\":\"\",\"isVisible\":true,\"sortValue\":4,\"isGeneral\":null,\"expirationTime\":null,\"state\":\"1\"},{\"id\":\"343406236160491521\",\"createdTime\":\"2023-03-29 13:09:17\",\"createdBy\":\"1452186486253289472\",\"updatedTime\":\"2024-10-22 00:24:32\",\"updatedBy\":\"1452186486253289472\",\"echoMap\":{\"DEF__APPLICATION__LOGO\":[],\"type\":\"自建应用\"},\"appKey\":\"visualizationSystem\",\"appSecret\":\"kzfhf3t8kzyma81g9qdv185wur6xqfa67ioz\",\"name\":\"大屏可视化系统\",\"version\":\"v1.0\",\"type\":\"10\",\"introduce\":\"多源实时数据、BI报表与大屏一体化、智能预警，及时查看业务变化\",\"redirect\":null,\"remark\":\"支持零编码拖拽式设计大屏，多种图文组件、内外部资源、TAB页组件等控件任意摆放，随心发挥创意；支持多种布局方式，灵活易用，大大降低了大屏制作门槛。\",\"url\":\"https://pro-test-view.thinglinks.mqttsnet.com/\",\"isVisible\":true,\"sortValue\":5,\"isGeneral\":null,\"expirationTime\":null,\"state\":\"1\"},{\"id\":\"403369958207127552\",\"createdTime\":\"2023-09-07 00:44:08\",\"createdBy\":\"1452186486253289472\",\"updatedTime\":\"2024-10-22 00:24:38\",\"updatedBy\":\"1452186486253289472\",\"echoMap\":{\"DEF__APPLICATION__LOGO\":[{\"bizId\":\"403369958207127552\",\"bizType\":\"DEF__APPLICATION__LOGO\",\"id\":\"543809225518628952\"}],\"type\":\"自建应用\"},\"appKey\":\"videoSystem\",\"appSecret\":\"uc91d45ofyo8xpq1nwp0j2q6vf57ow5emggf\",\"name\":\"流媒体业务系统\",\"version\":\"v1.0\",\"type\":\"10\",\"introduce\":\"视频监控的未来，高效、全面、智能，助您实时掌握一切。\",\"redirect\":null,\"remark\":\"流媒体业务系统是一款创新性的视频监控和管理平台，为用户提供卓越的视频接入和实时监控体验。多摄像头支持，智能分析，直观报警，一应俱全。\",\"url\":\"\",\"isVisible\":true,\"sortValue\":6,\"isGeneral\":null,\"expirationTime\":null,\"state\":\"1\"}],\"msg\":\"ok\",\"path\":null,\"extra\":null,\"timestamp\":\"1774517982135\",\"errorMsg\":\"\",\"trace\":\"99747e29409f4100b239c5b7aae9ccbb\",\"isSuccess\":true}', NULL, '2026-03-26 17:39:42', 1452186486253289472, '2026-03-26 17:39:42', 1452186486253289472, NULL);
INSERT INTO `base_operation_log_ext` (`id`, `params`, `result`, `ex_detail`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (749122831390229506, '[[\"543809225518628952\"]]', '{\"code\":0,\"data\":{},\"msg\":\"ok\",\"path\":null,\"extra\":null,\"timestamp\":\"1774517983251\",\"errorMsg\":\"\",\"trace\":\"db935133e06d4241a27dc32f683acdde\",\"isSuccess\":true}', NULL, '2026-03-26 17:39:43', 1452186486253289472, '2026-03-26 17:39:43', 1452186486253289472, NULL);
INSERT INTO `base_operation_log_ext` (`id`, `params`, `result`, `ex_detail`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (749122831390229507, '[[\"670471852653070336\"]]', '{\"code\":0,\"data\":{},\"msg\":\"ok\",\"path\":null,\"extra\":null,\"timestamp\":\"1774517984177\",\"errorMsg\":\"\",\"trace\":\"8b059cfb51904c3e9e49822e9879d1d9\",\"isSuccess\":true}', NULL, '2026-03-26 17:39:44', 1452186486253289472, '2026-03-26 17:39:44', 1452186486253289472, NULL);
INSERT INTO `base_operation_log_ext` (`id`, `params`, `result`, `ex_detail`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (749122831390229508, '[[\"670471852653070336\",\"670471852653070336\"]]', '{\"code\":0,\"data\":{},\"msg\":\"ok\",\"path\":null,\"extra\":null,\"timestamp\":\"1774517986336\",\"errorMsg\":\"\",\"trace\":\"859a0f6ba7e0437fa081c4227f74a1a1\",\"isSuccess\":true}', NULL, '2026-03-26 17:39:46', 1452186486253289472, '2026-03-26 17:39:46', 1452186486253289472, NULL);
COMMIT;

-- ----------------------------
-- Table structure for base_org
-- ----------------------------
DROP TABLE IF EXISTS `base_org`;
CREATE TABLE `base_org` (
  `id` bigint NOT NULL COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '名称',
  `type_` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '10' COMMENT '类型;[10-单位 20-部门]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.ORG_TYPE)',
  `short_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '简称',
  `parent_id` bigint DEFAULT NULL COMMENT '父组织',
  `tree_grade` int DEFAULT '0' COMMENT '树层级',
  `tree_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '树路径;用id拼接树结构',
  `sort_value` int DEFAULT '1' COMMENT '排序',
  `state` bit(1) DEFAULT b'1' COMMENT '状态;[0-禁用 1-启用]',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime DEFAULT NULL COMMENT '修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_org_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='组织';

-- ----------------------------
-- Records of base_org
-- ----------------------------
BEGIN;
INSERT INTO `base_org` (`id`, `name`, `type_`, `short_name`, `parent_id`, `tree_grade`, `tree_path`, `sort_value`, `state`, `remarks`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (470449818292977807, '研发部', '10', 'Research and Development', 0, 0, '/', 100, b'1', NULL, '2024-03-05 23:05:46', 1452186486253289472, '2024-03-05 23:05:46', 1452186486253289472, NULL);
INSERT INTO `base_org` (`id`, `name`, `type_`, `short_name`, `parent_id`, `tree_grade`, `tree_path`, `sort_value`, `state`, `remarks`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (470449818292977812, '硬件开发团队', '20', '硬件开发', 470449818292977807, 1, '/470449818292977807/', 100, b'1', '负责物联网设备的硬件设计、开发和测试', '2024-03-05 23:06:14', 1452186486253289472, '2024-03-05 23:06:14', 1452186486253289472, NULL);
COMMIT;

-- ----------------------------
-- Table structure for base_org_role_rel
-- ----------------------------
DROP TABLE IF EXISTS `base_org_role_rel`;
CREATE TABLE `base_org_role_rel` (
  `id` bigint NOT NULL COMMENT 'ID',
  `org_id` bigint NOT NULL COMMENT '所属部门;#base_org',
  `role_id` bigint NOT NULL COMMENT '拥有角色;#base_role',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后更新人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_org_role` (`org_id`,`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='组织的角色';

-- ----------------------------
-- Records of base_org_role_rel
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for base_parameter
-- ----------------------------
DROP TABLE IF EXISTS `base_parameter`;
CREATE TABLE `base_parameter` (
  `id` bigint NOT NULL COMMENT 'ID',
  `key_` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '参数键',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '参数值',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '参数名称',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `state` bit(1) DEFAULT b'1' COMMENT '状态',
  `param_type` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '20' COMMENT '类型;[10-系统参数 20-业务参数]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.PARAMETER_TYPE)',
  `created_by` bigint DEFAULT NULL COMMENT '创建人id',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人id',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个性参数';

-- ----------------------------
-- Records of base_parameter
-- ----------------------------
BEGIN;
INSERT INTO `base_parameter` (`id`, `key_`, `value`, `name`, `remarks`, `state`, `param_type`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (600026945810652162, '开启', '1', '开启', '', b'1', '10', 1452186486253289472, '2025-02-18 16:26:10', 1452186486253289472, '2025-02-18 16:26:10', NULL);
COMMIT;

-- ----------------------------
-- Table structure for base_position
-- ----------------------------
DROP TABLE IF EXISTS `base_position`;
CREATE TABLE `base_position` (
  `id` bigint NOT NULL COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '名称',
  `org_id` bigint DEFAULT NULL COMMENT '所属组织;#base_org@Echo(api = EchoApi.ORG_ID_CLASS)',
  `state` bit(1) DEFAULT b'1' COMMENT '状态;0-禁用 1-启用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime DEFAULT NULL COMMENT '修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='岗位';

-- ----------------------------
-- Records of base_position
-- ----------------------------
BEGIN;
INSERT INTO `base_position` (`id`, `name`, `org_id`, `state`, `remarks`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (490233618774360161, '12', 470449818292977807, b'1', '', '2024-04-28 10:03:15', 1452186486253289472, '2026-01-04 09:57:16', 1452186486253289472, NULL);
INSERT INTO `base_position` (`id`, `name`, `org_id`, `state`, `remarks`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (600026945810652161, '前端开发', 470449818292977807, b'1', '', '2025-02-18 15:54:36', 1452186486253289472, '2025-02-18 15:54:36', 1452186486253289472, NULL);
COMMIT;

-- ----------------------------
-- Table structure for base_role
-- ----------------------------
DROP TABLE IF EXISTS `base_role`;
CREATE TABLE `base_role` (
  `id` bigint NOT NULL COMMENT 'ID',
  `category` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '10' COMMENT '角色类别;[10-功能角色 20-桌面角色 30-数据角色]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.ROLE_CATEGORY)',
  `type_` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '20' COMMENT '角色类型;[10-系统角色 20-自定义角色]; \n@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Global.DATA_TYPE)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '名称',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '编码',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `state` bit(1) DEFAULT b'1' COMMENT '状态',
  `readonly_` bit(1) DEFAULT b'0' COMMENT '内置角色',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_code` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色';

-- ----------------------------
-- Records of base_role
-- ----------------------------
BEGIN;
INSERT INTO `base_role` (`id`, `category`, `type_`, `name`, `code`, `remarks`, `state`, `readonly_`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (739397474948784128, '10', '20', '测试租户管理员', 'TEST_TENANT_ADMIN', '', b'1', b'0', 1452186486253289472, '2026-03-04 17:50:37', 1452186486253289472, '2026-03-04 17:50:37', NULL);
INSERT INTO `base_role` (`id`, `category`, `type_`, `name`, `code`, `remarks`, `state`, `readonly_`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (1452496398934081536, '10', '10', '租户管理员', 'TENANT_ADMIN', '租户管理员', b'1', b'0', 2, '2021-10-25 12:45:02', 2, '2021-10-26 18:25:50', 0);
INSERT INTO `base_role` (`id`, `category`, `type_`, `name`, `code`, `remarks`, `state`, `readonly_`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (1452944729753780224, '30', '20', '机构管理员', 'ORG_ADMIN', '单位(门店)管理员', b'1', b'0', 2, '2021-10-26 18:26:33', 1452186486253289472, '2024-04-28 11:16:41', 0);
COMMIT;

-- ----------------------------
-- Table structure for base_role_resource_rel
-- ----------------------------
DROP TABLE IF EXISTS `base_role_resource_rel`;
CREATE TABLE `base_role_resource_rel` (
  `id` bigint NOT NULL COMMENT '主键',
  `resource_id` bigint NOT NULL COMMENT '拥有资源;#def_resource',
  `application_id` bigint NOT NULL COMMENT '所属应用;#def_application',
  `role_id` bigint NOT NULL COMMENT '所属角色;#base_role',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后更新人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_resource` (`resource_id`,`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色的资源';

-- ----------------------------
-- Records of base_role_resource_rel
-- ----------------------------
BEGIN;
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666880, 337596429604225200, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666881, 337596429604225074, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666882, 337596429604225075, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666883, 337596429604225077, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666884, 337596429604225076, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666885, 337596429604225078, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666886, 337596429604225079, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666887, 509472280537072640, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666888, 510523886100056064, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666889, 532687944574317568, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666890, 363894235184758784, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666891, 617259552898906114, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666892, 617259552898906115, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666893, 617259552898906116, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666894, 617259552898906117, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666895, 617259552898906118, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666896, 617259552898906119, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666897, 644799291226149084, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666898, 337596429604225201, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666899, 337596429604225203, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666900, 337596429604225202, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666901, 337596429604225204, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666902, 337596429604225205, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666903, 725326225784065024, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666904, 725326225784065025, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666905, 367331110964690944, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666906, 680740870356661374, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666907, 680740870356661375, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666908, 680740870356661376, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666909, 680740870356661377, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666910, 376804833862090752, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666911, 644799291226148865, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666912, 642256043521245289, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666913, 642256043521245290, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666914, 642256043521245292, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666915, 642256043521245291, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666916, 642256043521245293, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666917, 642256043521245294, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666918, 644799291226149085, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666919, 646606914112176192, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666920, 646606914112176193, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666921, 646606914112176194, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666922, 646606914112176195, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666923, 646606914112176196, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666924, 646606914112176197, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666925, 679376681369208832, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666926, 379966883274686464, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666927, 379966883274686465, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666928, 475671003876294668, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666929, 475671003876294670, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666930, 475671003876294672, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666931, 475671003876294674, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666932, 475671003876294676, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666933, 483305815051075584, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666934, 610601430171725915, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666935, 610601430171725916, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666936, 610601430171725918, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666937, 610601430171725917, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666938, 610601430171725919, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666939, 610601430171725920, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666940, 620099630088797631, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666941, 622932126660965695, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666942, 379966883274686466, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666943, 410198067149537280, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666944, 571418731291661315, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666945, 404111325396992012, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666946, 404111325396992013, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666947, 404111325396992014, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666948, 404111325396992015, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666949, 520054637574501376, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666950, 449227912437039104, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666951, 450279685503320082, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666952, 450279685503320083, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666953, 450279685503320084, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666954, 450279685503320085, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666955, 450279685503320086, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666956, 450279685503320087, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666957, 450279685503320136, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666958, 450279685503320137, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666959, 450279685503320138, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666960, 450279685503320139, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666961, 450279685503320140, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666962, 450279685503320141, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666963, 497972668805415203, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666964, 719446819282378754, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666965, 719446819282378755, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666966, 719446819282378752, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666967, 588459714119084182, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666968, 534637872611628032, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666969, 532798733255731280, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666970, 532798733255731281, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666971, 532798733255731282, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666972, 532798733255731283, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666973, 532798733255731284, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666974, 532798733255731285, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666975, 534644774624075860, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666976, 534644774624075861, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666977, 534644774624075862, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666978, 534644774624075863, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666979, 534644774624075864, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666980, 534644774624075865, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666981, 743171810604162048, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666982, 743171810604162185, 3, 739397474948784128, '2026-03-11 15:02:04', 1452186486253289472, '2026-03-11 15:02:04', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666993, 524352044412200338, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666994, 337596429604225074, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666995, 524352044412200343, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666996, 524352044412200342, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666997, 524352044412200341, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666998, 524352044412200340, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666999, 524352044412200339, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
COMMIT;

-- ----------------------------
-- Table structure for ca_cert_license
-- ----------------------------
DROP TABLE IF EXISTS `ca_cert_license`;
CREATE TABLE `ca_cert_license` (
  `id` bigint NOT NULL COMMENT 'id',
  `cert_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '证书名称',
  `issuer_common_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '颁发者通用名称',
  `serial_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '证书序列号',
  `common_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '通用名称',
  `organization` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '组织名称',
  `organizational_unit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '组织单位名称',
  `country_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '国家',
  `province_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '省份/州',
  `locality_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '城市',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '邮箱',
  `license_base64` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'License文件内容(Base64编码)',
  `business_license_fileid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '营业执照文件ID',
  `authorization_cert_fileid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '授权证书文件ID',
  `ca_cert_pem` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'CA证书(PEM格式)',
  `cert_fileid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '证书文件ID',
  `algorithm` tinyint NOT NULL DEFAULT '0' COMMENT '算法(0-RSA、1-EC)',
  `sign_algorithm` tinyint NOT NULL DEFAULT '0' COMMENT '签名算法(0-SHA256withRSA)',
  `param1` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'RSA公钥n或ECC Point x',
  `param2` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'RSA公钥e或ECC Point y',
  `extend_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展信息',
  `not_before` datetime DEFAULT NULL COMMENT '证书颁发时间',
  `not_after` datetime DEFAULT NULL COMMENT '证书过期时间',
  `revoke_time` datetime DEFAULT NULL COMMENT '证书撤销时间',
  `revoke_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '撤销原因',
  `state` tinyint NOT NULL DEFAULT '0' COMMENT '证书状态(0-待完善、1-已颁发、2-已撤销)',
  `thumbprint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '证书指纹(SHA-256)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_serial_number` (`serial_number`) USING BTREE COMMENT '证书序列号索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CA许可证证书表';

-- ----------------------------
-- Records of ca_cert_license
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for card_channel_info
-- ----------------------------
DROP TABLE IF EXISTS `card_channel_info`;
CREATE TABLE `card_channel_info` (
  `id` bigint NOT NULL COMMENT '渠道id',
  `channel_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '渠道商名称（如:中国移动）',
  `key_parameter` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '密钥集合',
  `official_flag` tinyint DEFAULT '0' COMMENT '是否直属官方平台(如直接对接是移动onelink  0不是  1是)',
  `refresh_flag` tinyint DEFAULT '0' COMMENT 'token是否需要重复刷新 true是 false否 默认是: false',
  `operator_type` tinyint DEFAULT '1' COMMENT '所属运营商(1移动、2电信 、3联通）',
  `province_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '省份名称',
  `province_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '省份编码',
  `appKey` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '公共应用键',
  `secret` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '公共密钥',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '公共code',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '客户appid',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '密匙',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '渠道状态(0启用、1停用)',
  `channel_type` tinyint DEFAULT NULL COMMENT '渠道类别(如:山东移动)',
  `extend_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数',
  `remark` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物联卡渠道表';

-- ----------------------------
-- Records of card_channel_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for card_channel_info_config
-- ----------------------------
DROP TABLE IF EXISTS `card_channel_info_config`;
CREATE TABLE `card_channel_info_config` (
  `id` bigint NOT NULL,
  `channel_id` bigint DEFAULT NULL COMMENT '渠道id',
  `request_type_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '请求类型编码',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '供应商接口地址',
  `extend_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物联卡渠道信息配置表';

-- ----------------------------
-- Records of card_channel_info_config
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for card_sim_device
-- ----------------------------
DROP TABLE IF EXISTS `card_sim_device`;
CREATE TABLE `card_sim_device` (
  `id` bigint NOT NULL COMMENT '主键',
  `card_id` bigint NOT NULL COMMENT '物联卡ID',
  `device_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备标识',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_card_id` (`card_id`) USING BTREE,
  KEY `idx_device_id` (`device_identification`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物联网卡与设备关系表';

-- ----------------------------
-- Records of card_sim_device
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for card_sim_info
-- ----------------------------
DROP TABLE IF EXISTS `card_sim_info`;
CREATE TABLE `card_sim_info` (
  `id` bigint NOT NULL COMMENT '主键',
  `imsi` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '国际移动用户识别码',
  `iccid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'SIM卡唯一识别码',
  `card_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '卡号',
  `card_type` tinyint DEFAULT NULL COMMENT '卡类型 0 插拔卡 1 贴片IC卡',
  `channel_id` bigint DEFAULT NULL COMMENT '渠道ID',
  `flows_used` decimal(17,2) DEFAULT '0.00' COMMENT '已用流量',
  `flows_total` decimal(17,2) DEFAULT '0.00' COMMENT '总流量',
  `flows_rest` decimal(17,2) DEFAULT '0.00' COMMENT '本月剩余流量',
  `virtual_flows_used` decimal(17,2) DEFAULT '0.00' COMMENT '虚拟已用流量',
  `virtual_flows_rest` decimal(17,2) DEFAULT '0.00' COMMENT '虚拟剩余流量',
  `virtual_flows_total` decimal(17,2) DEFAULT '0.00' COMMENT '虚拟总流量',
  `sms_flag` tinyint DEFAULT '0' COMMENT '是否有短信 0 无 1 有',
  `gprs_flag` tinyint DEFAULT '0' COMMENT 'GPRS开关 0 关 1 开',
  `open_time` datetime DEFAULT NULL COMMENT '开卡时间',
  `last_open_time` datetime DEFAULT NULL COMMENT '最晚激活时间',
  `start_time` datetime DEFAULT NULL COMMENT '激活时间',
  `end_time` datetime DEFAULT NULL COMMENT '失效时间',
  `flows_end_time` datetime DEFAULT NULL COMMENT '流量到期时间',
  `carrier_type` tinyint DEFAULT '1' COMMENT '运营商类型 1 移动 2 电信 3 联通',
  `sms_count` tinyint DEFAULT '0' COMMENT '已发送短信数量',
  `status` tinyint DEFAULT '1' COMMENT 'SIM卡状态 1 待激活 2 已激活 3 停机',
  `use_type` tinyint DEFAULT '1' COMMENT '使用类型 1 普卡 2 共享池 3 流量池',
  `apn_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'CMIOT' COMMENT 'APN名称',
  `ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'IP地址',
  `gain_time` datetime DEFAULT NULL COMMENT '获取时间',
  `online_flag` tinyint DEFAULT NULL COMMENT '在线状态 0 不在线 1 在线',
  `stop_card_type` tinyint NOT NULL DEFAULT '0' COMMENT '停卡类型 1 系统停卡 2 人工停卡 0 正常',
  `monthly_warning` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '当月流量预警记录',
  `imei` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '关联设备IMEI',
  `limit_flow` double(13,2) DEFAULT '0.00' COMMENT '流量限制阀值',
  `limit_flag` tinyint DEFAULT '0' COMMENT '流量阀值状态 0 未开启 1 开启',
  `limit_status` tinyint DEFAULT '0' COMMENT '流量限制状态 0 未限制 1 已限制',
  `offer_id` bigint DEFAULT NULL COMMENT '事务ID',
  `searchable_status` tinyint DEFAULT '1' COMMENT 'API是否可查 0 不可查 1 可查',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_channel_id` (`channel_id`) USING BTREE,
  KEY `idx_card_type` (`card_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_card_number` (`card_number`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物联网卡信息表';

-- ----------------------------
-- Records of card_sim_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for card_trigger_log
-- ----------------------------
DROP TABLE IF EXISTS `card_trigger_log`;
CREATE TABLE `card_trigger_log` (
  `id` bigint NOT NULL,
  `msisdn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '卡号',
  `trigger_type` smallint DEFAULT NULL COMMENT '触发类型 1: 卡号关停 2 数据服务关停, 3预警记录 ',
  `trigger_cause` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '触发原因',
  `trigger_mode` smallint DEFAULT NULL COMMENT '触发类型 1: 系统触发  2: 人工触发',
  `status` smallint DEFAULT '0' COMMENT '执行状态: 0处理中 1 失败  2 成功',
  `trigger_time` datetime DEFAULT NULL COMMENT '触发时间',
  `value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '变量值',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物联卡异常触发表';

-- ----------------------------
-- Records of card_trigger_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for card_warning
-- ----------------------------
DROP TABLE IF EXISTS `card_warning`;
CREATE TABLE `card_warning` (
  `id` bigint NOT NULL COMMENT 'id',
  `warning_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '预警名称',
  `warning_threshold` smallint DEFAULT NULL COMMENT '预警阈值',
  `alarm_id` bigint NOT NULL COMMENT '告警渠道',
  `warning_type` smallint DEFAULT NULL COMMENT '1 卡级 2流量池',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物联卡告警表';

-- ----------------------------
-- Records of card_warning
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for device
-- ----------------------------
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
  `id` bigint NOT NULL COMMENT 'id',
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端标识',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '密码',
  `cert_serial_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '证书序列号',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `auth_mode` tinyint NOT NULL DEFAULT '0' COMMENT '认证方式',
  `encrypt_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '加密秘钥',
  `encrypt_vector` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '加密向量',
  `sign_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '签名密钥',
  `encrypt_method` tinyint NOT NULL DEFAULT '0' COMMENT '协议加密方式',
  `device_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备标识',
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备名称',
  `connector` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '连接实例',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '设备描述',
  `device_status` tinyint NOT NULL DEFAULT '0' COMMENT '设备状态',
  `connect_status` tinyint NOT NULL DEFAULT '0' COMMENT '连接状态',
  `last_heartbeat_time` datetime DEFAULT NULL COMMENT '最新心跳时间',
  `device_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '设备标签',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `sw_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '软件版本',
  `fw_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '固件版本',
  `device_sdk_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'v1' COMMENT 'sdk版本',
  `gateway_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '网关设备id',
  `node_type` tinyint NOT NULL DEFAULT '0' COMMENT '设备类型',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_device_identification` (`device_identification`) USING BTREE COMMENT '设备标识',
  UNIQUE KEY `idx_client_id` (`client_id`) USING BTREE COMMENT '客户端标识',
  KEY `idx_product_identification` (`product_identification`) USING BTREE COMMENT '产品标识',
  KEY `idx_gateway_node_type` (`gateway_id`,`node_type`) USING BTREE COMMENT '网关设备ID,设备类型',
  KEY `idx_app_id` (`app_id`) USING BTREE COMMENT '应用ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备档案信息表';

-- ----------------------------
-- Records of device
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for device_acl_rule
-- ----------------------------
DROP TABLE IF EXISTS `device_acl_rule`;
CREATE TABLE `device_acl_rule` (
  `id` bigint NOT NULL COMMENT 'id',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '规则名称',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `device_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '设备标识',
  `rule_level` tinyint NOT NULL DEFAULT '0' COMMENT '规则级别(0:产品级、1:设备级)',
  `action_type` tinyint NOT NULL DEFAULT '0' COMMENT '动作类型(0:全部、1:发布、2:订阅、3:取消订阅)',
  `priority` int NOT NULL DEFAULT '500' COMMENT '规则优先级(0-1000,值越小优先级越高)',
  `topic_pattern` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'MQTT主题模式(支持通配符)',
  `ip_whitelist` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'IP白名单地址(多个用逗号分隔)',
  `decision` tinyint(1) NOT NULL DEFAULT '1' COMMENT '决策(0:拒绝、1:允许)',
  `enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device_identification` (`device_identification`) USING BTREE COMMENT '设备标识索引',
  KEY `idx_product_identification` (`product_identification`) USING BTREE COMMENT '产品标识索引',
  CONSTRAINT `device_acl_rule_chk_1` CHECK (((`topic_pattern` <> _utf8mb4'') or (`ip_whitelist` <> _utf8mb4''))),
  CONSTRAINT `device_acl_rule_chk_2` CHECK ((`priority` between 0 and 1000)),
  CONSTRAINT `device_acl_rule_chk_3` CHECK ((((`rule_level` = 0) and ((`device_identification` = _utf8mb4'') or (`device_identification` is null))) or ((`rule_level` = 1) and (`device_identification` <> _utf8mb4'') and (`device_identification` is not null))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备访问控制(ACL)规则表';

-- ----------------------------
-- Records of device_acl_rule
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for device_action
-- ----------------------------
DROP TABLE IF EXISTS `device_action`;
CREATE TABLE `device_action` (
  `id` bigint NOT NULL COMMENT 'id',
  `device_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '设备标识',
  `action_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '动作类型',
  `message` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '内容信息',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0:成功、1:失败)',
  `remark` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device_identification` (`device_identification`) USING BTREE COMMENT '设备标识索引',
  KEY `idx_action_type` (`action_type`) USING BTREE COMMENT '动作类型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备动作数据';

-- ----------------------------
-- Records of device_action
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for device_command
-- ----------------------------
DROP TABLE IF EXISTS `device_command`;
CREATE TABLE `device_command` (
  `id` bigint NOT NULL COMMENT 'id',
  `device_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备标识',
  `command_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '命令标识',
  `command_type` tinyint NOT NULL DEFAULT '0' COMMENT '命令类型(0:命名下发、1:命令响应、2:OTA命令下发)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '内容',
  `remark` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device_identification` (`device_identification`) USING BTREE COMMENT '设备标识',
  KEY `idx_device_cmdtype_ctime` (`device_identification`,`command_type`,`created_time` DESC) USING BTREE COMMENT '设备标识+命令类型+时间联合索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备命令下发及响应表';

-- ----------------------------
-- Records of device_command
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for device_group
-- ----------------------------
DROP TABLE IF EXISTS `device_group`;
CREATE TABLE `device_group` (
  `id` bigint NOT NULL COMMENT 'id',
  `parent_id` bigint NOT NULL COMMENT '父级ID',
  `sort_value` int DEFAULT '1' COMMENT '排序;默认升序',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '分组名称',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '分组类型',
  `state` bit(1) NOT NULL DEFAULT b'1' COMMENT '状态;[0-禁用 1-启用]',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '分组描述',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备分组表';

-- ----------------------------
-- Records of device_group
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for device_group_rel
-- ----------------------------
DROP TABLE IF EXISTS `device_group_rel`;
CREATE TABLE `device_group_rel` (
  `id` bigint NOT NULL COMMENT 'id',
  `group_id` bigint NOT NULL COMMENT '分组ID;#device_group',
  `device_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备标识',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_group_device` (`group_id`,`device_identification`) USING BTREE,
  KEY `idx_group_id` (`group_id`) USING BTREE,
  KEY `idx_device_identification` (`device_identification`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备分组资源关系表';

-- ----------------------------
-- Records of device_group_rel
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for device_location
-- ----------------------------
DROP TABLE IF EXISTS `device_location`;
CREATE TABLE `device_location` (
  `id` bigint NOT NULL COMMENT '主键',
  `device_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备标识',
  `latitude` decimal(10,7) NOT NULL COMMENT '纬度',
  `longitude` decimal(10,7) NOT NULL COMMENT '经度',
  `full_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '位置名称',
  `province_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '省,直辖市编码',
  `city_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '市编码',
  `region_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '区县',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备位置表';

-- ----------------------------
-- Records of device_location
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for empowerment_record
-- ----------------------------
DROP TABLE IF EXISTS `empowerment_record`;
CREATE TABLE `empowerment_record` (
  `id` bigint NOT NULL COMMENT 'id',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `empowerment_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '赋能标识',
  `empowerment_type` tinyint NOT NULL DEFAULT '0' COMMENT '赋能类型',
  `startTime` datetime DEFAULT NULL COMMENT '开始时间',
  `endTime` datetime DEFAULT NULL COMMENT '结束时间',
  `outcome` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '赋能结果',
  `feedback` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '赋能反馈',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版本',
  `dependencies` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '依赖关系',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='赋能记录表';

-- ----------------------------
-- Records of empowerment_record
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for extend_interface_log
-- ----------------------------
DROP TABLE IF EXISTS `extend_interface_log`;
CREATE TABLE `extend_interface_log` (
  `id` bigint NOT NULL,
  `interface_id` bigint NOT NULL COMMENT '接口ID;\n#extend_interface',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '接口名称',
  `success_count` int DEFAULT '0' COMMENT '成功次数',
  `fail_count` int DEFAULT '0' COMMENT '失败次数',
  `last_exec_time` datetime DEFAULT NULL COMMENT '最后执行时间',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime DEFAULT NULL COMMENT '修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `UK_EIL_INTERFACE_ID` (`interface_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='接口执行日志';

-- ----------------------------
-- Records of extend_interface_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for extend_interface_logging
-- ----------------------------
DROP TABLE IF EXISTS `extend_interface_logging`;
CREATE TABLE `extend_interface_logging` (
  `id` bigint NOT NULL,
  `log_id` bigint NOT NULL COMMENT '接口日志ID;\n#extend_interface_log',
  `exec_time` datetime NOT NULL COMMENT '执行时间',
  `status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '01' COMMENT '执行状态;[01-初始化 02-成功 03-失败]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.MSG_INTERFACE_LOGGING_STATUS)',
  `params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '请求参数',
  `result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '接口返回',
  `error_msg` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '异常信息',
  `biz_id` bigint DEFAULT NULL COMMENT '业务ID',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime DEFAULT NULL COMMENT '修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='接口执行日志记录';

-- ----------------------------
-- Records of extend_interface_logging
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for extend_msg
-- ----------------------------
DROP TABLE IF EXISTS `extend_msg`;
CREATE TABLE `extend_msg` (
  `id` bigint NOT NULL COMMENT '短信记录ID',
  `template_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '消息模板;\n#extend_msg_template',
  `type` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '消息类型;[01-短信 02-邮件 03-站内信];@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.MSG_TEMPLATE_TYPE)',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '执行状态;\n#TaskStatus{DRAFT:草稿;WAITING:等待执行;SUCCESS:执行成功;FAIL:执行失败}',
  `channel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '发送渠道;\n#SourceType{APP:应用;SERVICE:服务}',
  `param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '参数;需要封装为[{‘key’:‘‘,;’value’:‘‘}, {’key2’:‘‘, ’value2’:‘‘}]格式',
  `config_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '消息配置参数',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '发送内容',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `biz_id` bigint DEFAULT NULL COMMENT '业务ID',
  `biz_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '业务类型',
  `remind_mode` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提醒方式;@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_REMIND_MODE)[01-待办 02-预警 03-提醒]',
  `author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '发布人姓名',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人所属机构',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `tempate_id_topic_content` (`template_code`,`title`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息';

-- ----------------------------
-- Records of extend_msg
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for extend_msg_recipient
-- ----------------------------
DROP TABLE IF EXISTS `extend_msg_recipient`;
CREATE TABLE `extend_msg_recipient` (
  `id` bigint NOT NULL COMMENT 'ID',
  `msg_id` bigint NOT NULL COMMENT '任务ID;\n#extend_msg',
  `recipient` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '接收人;\n可能是手机号、邮箱、用户ID等',
  `ext` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '扩展信息',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `task_id_tel_num` (`msg_id`,`recipient`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息接收人';

-- ----------------------------
-- Records of extend_msg_recipient
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for extend_msg_template
-- ----------------------------
DROP TABLE IF EXISTS `extend_msg_template`;
CREATE TABLE `extend_msg_template` (
  `id` bigint NOT NULL COMMENT '模板ID',
  `interface_id` bigint NOT NULL COMMENT '接口ID',
  `type` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '消息类型;[01-短信 02-邮件 03-站内信];@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.MSG_TEMPLATE_TYPE)',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '模板标识',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '模板名称',
  `state` bit(1) DEFAULT NULL COMMENT '状态',
  `template_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '模板编码',
  `sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '签名',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板内容',
  `script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '脚本',
  `param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '模板参数',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `target_` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '打开方式;@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_TARGET)[01-页面 02-弹窗 03-新开窗口]',
  `auto_read` bit(1) DEFAULT b'1' COMMENT '自动已读',
  `remind_mode` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提醒方式;@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_REMIND_MODE)[01-待办 02-预警 03-提醒]',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '跳转地址',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `UK_EX_MSG_TEMPLATE_CODE` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息模板';

-- ----------------------------
-- Records of extend_msg_template
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for extend_notice
-- ----------------------------
DROP TABLE IF EXISTS `extend_notice`;
CREATE TABLE `extend_notice` (
  `id` bigint NOT NULL COMMENT 'ID',
  `msg_id` bigint DEFAULT NULL COMMENT '消息ID',
  `biz_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '业务ID',
  `biz_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '业务类型',
  `recipient_id` bigint NOT NULL COMMENT '接收人',
  `remind_mode` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '提醒方式;@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_REMIND_MODE)[01-待办 02-预警 03-提醒]',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '标题',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '内容',
  `author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '发布人',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '处理地址',
  `target_` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '打开方式;@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_TARGET)[01-页面 02-弹窗 03-新开窗口]',
  `auto_read` bit(1) DEFAULT NULL COMMENT '自动已读',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `read_time` datetime DEFAULT NULL COMMENT '读取时间',
  `is_read` bit(1) DEFAULT b'0' COMMENT '是否已读',
  `is_handle` bit(1) DEFAULT b'0' COMMENT '是否处理',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人id',
  `updated_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '所属组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知表';

-- ----------------------------
-- Records of extend_notice
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for mobile_space
-- ----------------------------
DROP TABLE IF EXISTS `mobile_space`;
CREATE TABLE `mobile_space` (
  `id` bigint NOT NULL COMMENT 'id',
  `space_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '空间名称',
  `full_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '位置名称',
  `latitude` decimal(10,7) NOT NULL COMMENT '纬度',
  `longitude` decimal(10,7) NOT NULL COMMENT '经度',
  `province_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '省,直辖市编码',
  `city_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '市编码',
  `region_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '区县',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='移动端-空间表';

-- ----------------------------
-- Records of mobile_space
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for mobile_space_device
-- ----------------------------
DROP TABLE IF EXISTS `mobile_space_device`;
CREATE TABLE `mobile_space_device` (
  `id` bigint NOT NULL COMMENT 'id',
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `device_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备标识',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_space_product_device` (`space_id`,`product_identification`,`device_identification`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='空间设备绑定表';

-- ----------------------------
-- Records of mobile_space_device
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for mobile_space_member
-- ----------------------------
DROP TABLE IF EXISTS `mobile_space_member`;
CREATE TABLE `mobile_space_member` (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `member_id` bigint NOT NULL COMMENT '人员ID',
  `member_type` tinyint NOT NULL DEFAULT '0' COMMENT '人员类型:( 0:成员、1:管理员、 2:所有者)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_uniq_space_member` (`space_id`,`member_id`) USING BTREE COMMENT '空间人员唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='空间人员绑定关系表';

-- ----------------------------
-- Records of mobile_space_member
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ota_upgrade_records
-- ----------------------------
DROP TABLE IF EXISTS `ota_upgrade_records`;
CREATE TABLE `ota_upgrade_records` (
  `id` bigint NOT NULL COMMENT '主键',
  `upgrade_id` bigint NOT NULL COMMENT '升级包ID，关联ota_upgrades表',
  `task_id` bigint NOT NULL COMMENT '任务ID，关联ota_upgrade_tasks表',
  `device_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备标识',
  `upgrade_status` smallint NOT NULL DEFAULT '0' COMMENT '升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)',
  `source_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '待升级的源版本号',
  `target_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '目标版本号',
  `app_confirmation_status` smallint NOT NULL DEFAULT '-1' COMMENT 'APP确认状态(-1:无需确认、0:待确认、1:已确认、2:已拒绝)',
  `app_confirmation_time` datetime DEFAULT NULL COMMENT 'APP确认时间',
  `command_send_status` smallint NOT NULL DEFAULT '0' COMMENT '指令下发状态(0:未下发、1:下发中、2:下发成功、3:下发失败)',
  `last_command_send_time` datetime DEFAULT NULL COMMENT '最新指令下发时间',
  `command_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'OTA指令内容',
  `progress` smallint NOT NULL DEFAULT '0' COMMENT '升级进度（百分比）',
  `error_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '错误代码',
  `error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '错误信息',
  `start_time` datetime DEFAULT NULL COMMENT '升级开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '升级结束时间',
  `success_details` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '升级成功详细信息',
  `failure_details` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '升级失败详细信息',
  `log_details` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '升级过程日志',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_task_id_device_identification` (`task_id`,`device_identification`) USING BTREE COMMENT '任务ID+设备标识唯一索引',
  KEY `idx_task_id` (`task_id` DESC) USING BTREE COMMENT '任务ID索引',
  KEY `idx_device_identification` (`device_identification` DESC) USING BTREE COMMENT '设备标识索引',
  KEY `idx_upgrade_id` (`upgrade_id` DESC) USING BTREE COMMENT '升级包ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OTA升级记录表';

-- ----------------------------
-- Records of ota_upgrade_records
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ota_upgrade_targets
-- ----------------------------
DROP TABLE IF EXISTS `ota_upgrade_targets`;
CREATE TABLE `ota_upgrade_targets` (
  `id` bigint NOT NULL COMMENT '主键',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `target_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '目标值(产品标识/设备标识/分组ID/省市区域编码)',
  `target_status` smallint NOT NULL DEFAULT '0' COMMENT '目标状态(0:待推送,1:推送中,2:推送成功,3:推送失败)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`) USING BTREE COMMENT '任务ID索引',
  KEY `idx_target_value` (`target_value`) USING BTREE COMMENT '目标值索引',
  KEY `idx_target_status` (`target_status`) USING BTREE COMMENT '目标状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OTA升级目标表';

-- ----------------------------
-- Records of ota_upgrade_targets
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ota_upgrade_tasks
-- ----------------------------
DROP TABLE IF EXISTS `ota_upgrade_tasks`;
CREATE TABLE `ota_upgrade_tasks` (
  `id` bigint NOT NULL COMMENT '主键',
  `upgrade_id` bigint NOT NULL COMMENT '升级包ID，关联ota_upgrades表',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `upgrade_method` smallint NOT NULL DEFAULT '0' COMMENT '升级方式(0:静态升级、1:动态升级)',
  `upgrade_scope` smallint NOT NULL DEFAULT '0' COMMENT '升级范围(0:全部设备、1:定向升级、2:分组升级、3:区域升级)',
  `task_status` smallint NOT NULL DEFAULT '0' COMMENT '任务状态(0:待发布、1:进行中、2:已完成、3:已取消、4:失败)',
  `scheduled_start_time` datetime NOT NULL COMMENT '计划执行开始时间',
  `scheduled_end_time` datetime DEFAULT NULL COMMENT '计划执行结束时间',
  `max_retry_count` int NOT NULL DEFAULT '3' COMMENT '最大重试次数',
  `current_retry_count` int NOT NULL DEFAULT '0' COMMENT '当前重试次数',
  `source_versions` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '待升级的源版本号',
  `app_confirmation_required` tinyint NOT NULL DEFAULT '0' COMMENT 'APP确认升级(0:否、1:是)',
  `upgrade_rate` int NOT NULL DEFAULT '10' COMMENT '升级速率(恒定速率升级，10-1000)',
  `retry_interval_minutes` int NOT NULL DEFAULT '10' COMMENT '重试间隔分钟数',
  `device_upgrade_timeout` int DEFAULT NULL COMMENT '设备升级超时时间(分钟，5-1440)',
  `last_retry_time` datetime DEFAULT NULL COMMENT '最后重试时间',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '任务描述',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_upgrade_id` (`upgrade_id`) USING BTREE COMMENT '升级包ID索引',
  KEY `idx_upgrade_method` (`upgrade_method`) USING BTREE COMMENT '升级模式索引',
  KEY `idx_upgrade_scope` (`upgrade_scope`) USING BTREE COMMENT '升级范围索引',
  KEY `idx_task_status` (`task_status`) USING BTREE COMMENT '升级状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OTA升级任务表';

-- ----------------------------
-- Records of ota_upgrade_tasks
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ota_upgrades
-- ----------------------------
DROP TABLE IF EXISTS `ota_upgrades`;
CREATE TABLE `ota_upgrades` (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '包名称',
  `package_type` smallint NOT NULL DEFAULT '0' COMMENT '升级包类型(0:软件包、1:固件包)',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '升级包版本号',
  `file_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '升级包的位置',
  `sign_method` smallint NOT NULL DEFAULT '0' COMMENT '签名方法(0-MD5、1-SHA256)',
  `status` smallint NOT NULL DEFAULT '0' COMMENT '状态',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '升级包功能描述',
  `custom_info` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '自定义信息',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_app_id` (`app_id`) USING BTREE COMMENT '应用ID',
  KEY `idx_product_identification` (`product_identification`) USING BTREE COMMENT '产品标识',
  KEY `idx_version` (`version`) USING BTREE COMMENT '升级包版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OTA升级包';

-- ----------------------------
-- Records of ota_upgrades
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for plugin_info
-- ----------------------------
DROP TABLE IF EXISTS `plugin_info`;
CREATE TABLE `plugin_info` (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID，所属应用场景',
  `plugin_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '插件唯一标识，自动生成：plugin_code + version',
  `plugin_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '插件代码标识，取自 pluginMeta.properties',
  `plugin_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '插件名称，文件名',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '插件版本，取自 pluginMeta.properties',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '插件描述，取自 pluginMeta.properties',
  `file_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文件在服务器上的唯一标识，用于查询文件临时路径',
  `file_size` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '文件大小（MB）',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态',
  `level` tinyint(1) NOT NULL DEFAULT '0' COMMENT '插件级别：0-系统级，1-用户级',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '插件类型：0-设备协议插件，1-业务插件',
  `run_mode` tinyint(1) NOT NULL DEFAULT '0' COMMENT '运行模式：0-单节点，1-集群',
  `license_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '许可证类型（如GPL, MIT, 商业等）',
  `license_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '许可证密钥或证书',
  `valid_until` date DEFAULT NULL COMMENT '许可证有效期',
  `file_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '文件的哈希值，用于验证文件的完整性（如 SHA-256）',
  `scan_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'PENDING' COMMENT '扫描状态：PENDING, SUCCESS, FAILED',
  `scan_report_file_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '扫描报告的文件ID',
  `scan_date` datetime DEFAULT NULL COMMENT '最后一次扫描的日期',
  `scan_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扫描摘要（如发现的漏洞数目等）',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数（预留）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='插件信息表';

-- ----------------------------
-- Records of plugin_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for plugin_instance
-- ----------------------------
DROP TABLE IF EXISTS `plugin_instance`;
CREATE TABLE `plugin_instance` (
  `id` bigint NOT NULL COMMENT '主键',
  `instance_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '实例唯一标识',
  `instance_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '实例名称',
  `application_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用名称，SpringBoot应用名称',
  `weight` int NOT NULL DEFAULT '0' COMMENT '实例的权重',
  `healthy` tinyint(1) NOT NULL DEFAULT '0' COMMENT '实例的健康状态',
  `enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '实例是否启用',
  `ephemeral` tinyint(1) NOT NULL DEFAULT '0' COMMENT '实例是否为临时实例',
  `cluster_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '实例所在集群名称',
  `heart_beat_interval` bigint DEFAULT NULL COMMENT '实例心跳间隔时间(毫秒)',
  `heart_beat_time_out` bigint DEFAULT NULL COMMENT '实例心跳超时时间(毫秒)',
  `ip_delete_time_out` bigint DEFAULT NULL COMMENT '实例IP删除超时时间(毫秒)',
  `machine_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '实例机器IP地址',
  `machine_port` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '实例机器端口',
  `port_range_start` int NOT NULL COMMENT '实例可用端口范围起始值',
  `port_range_end` int NOT NULL COMMENT '实例可用端口范围结束值',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数（预留）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_instance_identification` (`instance_identification`) USING BTREE COMMENT '实例唯一标识索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='插件实例信息表';

-- ----------------------------
-- Records of plugin_instance
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for plugin_instance_heartbeat
-- ----------------------------
DROP TABLE IF EXISTS `plugin_instance_heartbeat`;
CREATE TABLE `plugin_instance_heartbeat` (
  `id` bigint NOT NULL COMMENT '主键',
  `instance_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '实例唯一标识，关联 plugin_instance 表的 instance_identification',
  `machine_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '插件运行所在的机器 IP 地址',
  `last_heartbeat_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次心跳时间',
  `heartbeat_interval` int NOT NULL DEFAULT '60' COMMENT '心跳间隔时间（秒）',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '心跳状态：0-正常，1-异常',
  `heartbeat_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '心跳详细信息',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数（预留）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='插件实例心跳表';

-- ----------------------------
-- Records of plugin_instance_heartbeat
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for plugin_instance_mapping
-- ----------------------------
DROP TABLE IF EXISTS `plugin_instance_mapping`;
CREATE TABLE `plugin_instance_mapping` (
  `id` bigint NOT NULL COMMENT '主键',
  `plugin_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '插件唯一标识，关联 plugin_info 表的 plugin_identification',
  `instance_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '实例唯一标识，关联 plugin_instance 表的 instance_identification',
  `port` int NOT NULL COMMENT '插件在该实例上使用的端口号',
  `port_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '端口类型或用途（如 HTTP, HTTPS, 管理端口等）',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '端口：0-正常，1-异常',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_plugin_instance_port` (`plugin_identification`,`instance_identification`,`port`) USING BTREE COMMENT '插件、实例和端口的唯一关联'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='插件与实例及端口管理表';

-- ----------------------------
-- Records of plugin_instance_mapping
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for plugin_log
-- ----------------------------
DROP TABLE IF EXISTS `plugin_log`;
CREATE TABLE `plugin_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `plugin_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '插件唯一标识，关联 plugin_info 表的 plugin_identification',
  `instance_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '实例唯一标识，关联 plugin_instance 表的 instance_identification',
  `log_level` tinyint(1) NOT NULL DEFAULT '0' COMMENT '日志级别：0-DEBUG，1-INFO，2-WARN，3-ERROR',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '日志消息内容',
  `log_timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志记录时间',
  `node_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '运行节点标识（集群模式下用以区分具体节点）',
  `thread_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '运行时线程名称',
  `exception_stacktrace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '异常堆栈信息（如有异常）',
  `context_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '上下文信息，可能包括请求ID、用户ID等',
  `error_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '错误代码（如果适用）',
  `execution_time` decimal(10,2) DEFAULT NULL COMMENT '执行时间（毫秒）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_plugin_identification` (`plugin_identification`) USING BTREE COMMENT '索引插件标识',
  KEY `idx_instance_identification` (`instance_identification`) USING BTREE COMMENT '索引实例标识',
  KEY `idx_node_id` (`node_id`) USING BTREE COMMENT '索引运行节点'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='插件运行日志表';

-- ----------------------------
-- Records of plugin_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint NOT NULL COMMENT 'id',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `template_id` bigint DEFAULT NULL COMMENT '产品模板ID',
  `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品名称',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `product_type` tinyint NOT NULL DEFAULT '0' COMMENT '产品类型',
  `manufacturer_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '厂商ID',
  `manufacturer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '厂商名称',
  `model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品型号',
  `data_format` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '数据格式',
  `device_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备类型',
  `protocol_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '接入协议',
  `product_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `product_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '产品版本',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '图标',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '产品描述',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_product_identification` (`product_identification`) USING BTREE COMMENT '产品标识',
  KEY `idx_manufacturer_id` (`manufacturer_id`) USING BTREE COMMENT '厂商ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品模型';

-- ----------------------------
-- Records of product
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_command
-- ----------------------------
DROP TABLE IF EXISTS `product_command`;
CREATE TABLE `product_command` (
  `id` bigint NOT NULL COMMENT '命令id',
  `service_id` bigint NOT NULL COMMENT '服务ID',
  `command_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '命令编码',
  `command_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '命令名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '命令描述。',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_service_id` (`service_id`) USING BTREE COMMENT '服务ID',
  KEY `idx_command_code` (`command_code`) USING BTREE COMMENT '命令编码'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品模型设备服务命令表';

-- ----------------------------
-- Records of product_command
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_command_request
-- ----------------------------
DROP TABLE IF EXISTS `product_command_request`;
CREATE TABLE `product_command_request` (
  `id` bigint NOT NULL COMMENT 'id',
  `service_id` bigint NOT NULL DEFAULT '0' COMMENT '服务ID',
  `command_id` bigint NOT NULL DEFAULT '0' COMMENT '命令ID',
  `parameter_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数编码',
  `parameter_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数名称',
  `parameter_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数描述',
  `datatype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '数据类型',
  `enumlist` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '枚举值',
  `max` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最大值',
  `maxlength` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字符串长度',
  `min` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最小值',
  `required` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '是否必填',
  `step` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '步长',
  `unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '单位',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_command_id` (`command_id`) USING BTREE COMMENT '命令ID',
  KEY `idx_service_id` (`service_id`) USING BTREE COMMENT '服务ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品模型设备下发服务命令属性表';

-- ----------------------------
-- Records of product_command_request
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_command_response
-- ----------------------------
DROP TABLE IF EXISTS `product_command_response`;
CREATE TABLE `product_command_response` (
  `id` bigint NOT NULL COMMENT 'id',
  `command_id` bigint NOT NULL DEFAULT '0' COMMENT '命令ID',
  `service_id` bigint DEFAULT '0' COMMENT '服务ID',
  `datatype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '数据类型',
  `enumlist` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '枚举值',
  `max` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最大值',
  `maxlength` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字符串长度',
  `min` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最小值',
  `parameter_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数描述',
  `parameter_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数名字。',
  `parameter_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数字段值',
  `required` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '是否必填',
  `step` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '步长',
  `unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '单位',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_command_id` (`command_id`) USING BTREE COMMENT '命令ID',
  KEY `idx_service_id` (`service_id`) USING BTREE COMMENT '服务ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品模型设备响应服务命令属性表';

-- ----------------------------
-- Records of product_command_response
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_property
-- ----------------------------
DROP TABLE IF EXISTS `product_property`;
CREATE TABLE `product_property` (
  `id` bigint NOT NULL COMMENT '属性id',
  `service_id` bigint NOT NULL COMMENT '服务ID',
  `property_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '属性编码',
  `property_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '属性名称',
  `datatype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '数据类型',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '属性描述',
  `enumlist` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '枚举值',
  `max` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最大值',
  `maxlength` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字符串长度',
  `method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '访问模式',
  `min` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最小值',
  `required` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '是否必填',
  `step` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '步长',
  `unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '单位',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '图标',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_service_id` (`service_id`) USING BTREE COMMENT '服务ID',
  KEY `idx_property_code` (`property_code`) USING BTREE COMMENT '属性编码'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品模型服务属性表';

-- ----------------------------
-- Records of product_property
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_service
-- ----------------------------
DROP TABLE IF EXISTS `product_service`;
CREATE TABLE `product_service` (
  `id` bigint NOT NULL COMMENT '服务id',
  `product_id` bigint DEFAULT NULL COMMENT '产品ID',
  `service_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '服务编码',
  `service_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '服务名称',
  `service_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '服务类型',
  `service_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '服务描述',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_product_id` (`product_id`) USING BTREE COMMENT '产品ID',
  KEY `idx_service_code` (`service_code`) USING BTREE COMMENT '服务编码'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品模型服务表';

-- ----------------------------
-- Records of product_service
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_topic
-- ----------------------------
DROP TABLE IF EXISTS `product_topic`;
CREATE TABLE `product_topic` (
  `id` bigint NOT NULL COMMENT 'id',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品标识',
  `topic_type` tinyint NOT NULL DEFAULT '0' COMMENT 'Topic类型(0:基础Topic,1:自定义Topic)',
  `function_type` tinyint NOT NULL DEFAULT '0' COMMENT '功能类型',
  `topic` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'topic',
  `publisher` tinyint NOT NULL DEFAULT '0' COMMENT '发布者(0:物联网平台、1:边缘设备、2:终端设备)',
  `subscriber` tinyint NOT NULL DEFAULT '0' COMMENT '订阅者(0:物联网平台、1:边缘设备、2:终端设备)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_product_identification` (`product_identification`) USING BTREE COMMENT '产品标识索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品Topic信息表';

-- ----------------------------
-- Records of product_topic
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule
-- ----------------------------
DROP TABLE IF EXISTS `rule`;
CREATE TABLE `rule` (
  `id` bigint NOT NULL COMMENT '主键',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规则名称',
  `rule_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规则标识',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `effective_type` smallint NOT NULL COMMENT '生效类型',
  `appoint_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '指定内容',
  `status` smallint NOT NULL DEFAULT '0' COMMENT '启用状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_rule_identification` (`rule_identification`) USING BTREE COMMENT '规则标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='规则信息表';

-- ----------------------------
-- Records of rule
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_action_execution_log
-- ----------------------------
DROP TABLE IF EXISTS `rule_action_execution_log`;
CREATE TABLE `rule_action_execution_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `rule_execution_id` bigint NOT NULL COMMENT '规则执行日志ID（外键）',
  `action_type` smallint NOT NULL COMMENT '动作类型：0-命令下发，1-触发告警，2-数据转发',
  `action_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作内容',
  `result` tinyint(1) NOT NULL COMMENT '动作是否执行成功',
  `start_time` datetime NOT NULL COMMENT '动作开始执行时间',
  `end_time` datetime DEFAULT NULL COMMENT '动作结束执行时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数（文本格式）',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_rule_execution_id` (`rule_execution_id`) USING BTREE COMMENT '规则执行日志ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='规则动作执行日志表';

-- ----------------------------
-- Records of rule_action_execution_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_alarm
-- ----------------------------
DROP TABLE IF EXISTS `rule_alarm`;
CREATE TABLE `rule_alarm` (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `alarm_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '告警名称',
  `alarm_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '告警编码',
  `alarm_scene` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '告警场景',
  `alarm_channel_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '告警渠道ID集合',
  `level` smallint NOT NULL DEFAULT '0' COMMENT '告警级别',
  `status` smallint NOT NULL DEFAULT '0' COMMENT '启用状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='告警规则表';

-- ----------------------------
-- Records of rule_alarm
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_alarm_channel
-- ----------------------------
DROP TABLE IF EXISTS `rule_alarm_channel`;
CREATE TABLE `rule_alarm_channel` (
  `id` bigint NOT NULL COMMENT '主键',
  `channel_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '渠道名称',
  `channel_type` smallint NOT NULL COMMENT '渠道类型',
  `channel_config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '告警配置',
  `status` smallint NOT NULL DEFAULT '0' COMMENT '启用状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='告警规则渠道表';

-- ----------------------------
-- Records of rule_alarm_channel
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_alarm_record
-- ----------------------------
DROP TABLE IF EXISTS `rule_alarm_record`;
CREATE TABLE `rule_alarm_record` (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `alarm_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '告警编码',
  `occurred_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  `handled_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handling_notes` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '处理记录',
  `resolved_time` datetime DEFAULT NULL COMMENT '解决时间',
  `resolution_notes` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '解决记录',
  `content_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '告警具体内容信息',
  `handled_status` smallint NOT NULL DEFAULT '0' COMMENT '处理状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='告警规则记录表';

-- ----------------------------
-- Records of rule_alarm_record
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_condition
-- ----------------------------
DROP TABLE IF EXISTS `rule_condition`;
CREATE TABLE `rule_condition` (
  `id` bigint NOT NULL COMMENT '主键',
  `rule_id` bigint NOT NULL COMMENT '规则',
  `condition_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '条件编码',
  `condition_type` smallint NOT NULL DEFAULT '0' COMMENT '条件类型',
  `condition_scheme` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '条件内容',
  `status` smallint NOT NULL DEFAULT '0' COMMENT '启用状态',
  `anti_shake` smallint NOT NULL DEFAULT '0' COMMENT '防抖状态',
  `anti_shake_scheme` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '防抖策略',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_rule_id` (`rule_id`) USING BTREE COMMENT '规则ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='规则条件表';

-- ----------------------------
-- Records of rule_condition
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_condition_action
-- ----------------------------
DROP TABLE IF EXISTS `rule_condition_action`;
CREATE TABLE `rule_condition_action` (
  `id` bigint NOT NULL COMMENT '主键',
  `rule_condition_id` bigint NOT NULL COMMENT '规则条件ID',
  `action_type` smallint NOT NULL DEFAULT '0' COMMENT '执行动作',
  `action_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作内容',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_rule_condition_id` (`rule_condition_id`) USING BTREE COMMENT '规则条件ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='规则条件动作表';

-- ----------------------------
-- Records of rule_condition_action
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_condition_execution_log
-- ----------------------------
DROP TABLE IF EXISTS `rule_condition_execution_log`;
CREATE TABLE `rule_condition_execution_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `rule_execution_id` bigint NOT NULL COMMENT '规则执行日志ID',
  `condition_uuid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '条件唯一标识',
  `condition_type` smallint NOT NULL COMMENT '条件类型：0-设备属性触发，1-定时触发，2-设备动作触发等',
  `evaluation_result` tinyint(1) NOT NULL COMMENT '条件是否成立',
  `start_time` datetime NOT NULL COMMENT '条件评估开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '条件评估结束时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数（文本格式）',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_condition_uuid` (`condition_uuid`) USING BTREE COMMENT '条件UUID索引',
  KEY `idx_rule_execution_id` (`rule_execution_id`) USING BTREE COMMENT '规则执行日志ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='规则条件执行日志表';

-- ----------------------------
-- Records of rule_condition_execution_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_execution_log
-- ----------------------------
DROP TABLE IF EXISTS `rule_execution_log`;
CREATE TABLE `rule_execution_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `rule_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规则标识',
  `rule_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规则名称',
  `status` smallint NOT NULL DEFAULT '0' COMMENT '规则执行状态：0-未执行，1-执行中，2-已完成',
  `start_time` datetime NOT NULL COMMENT '规则执行开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '规则执行结束时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '描述',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数（文本格式）',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_rule_identification` (`rule_identification`) USING BTREE COMMENT '规则唯一标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='规则执行日志表';

-- ----------------------------
-- Records of rule_execution_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_groovy_script
-- ----------------------------
DROP TABLE IF EXISTS `rule_groovy_script`;
CREATE TABLE `rule_groovy_script` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '名称',
  `app_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `namespace` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '命名空间',
  `platform_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '平台编码',
  `product_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品编码',
  `channel_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '渠道编码',
  `business_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '业务编码',
  `business_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '业务标识',
  `enable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用',
  `script_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '脚本内容',
  `extend_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展信息',
  `object_version` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'v1.0.0' COMMENT '版本号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_only_key` (`namespace`,`platform_code`,`product_code`,`channel_code`,`business_code`,`business_identification`) USING BTREE COMMENT '唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='规则脚本表';

-- ----------------------------
-- Records of rule_groovy_script
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_instance
-- ----------------------------
DROP TABLE IF EXISTS `rule_instance`;
CREATE TABLE `rule_instance` (
  `id` bigint NOT NULL COMMENT 'id',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `rule_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '规则实例名称',
  `flow_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '流程ID， 规则实例类型为“规则编排”时，该项为对应的NedRed流程',
  `flow_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '流程数据',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '规则实例类型(0规则编排、1设备告警、2数据转发）',
  `instance_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '实例地址',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_flow_id` (`flow_id`) USING BTREE COMMENT '流程ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='规则实例表';

-- ----------------------------
-- Records of rule_instance
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'increment id',
  `branch_id` bigint NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'global transaction id',
  `context` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AT transaction mode undo table';

-- ----------------------------
-- Records of undo_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_device_channel
-- ----------------------------
DROP TABLE IF EXISTS `video_device_channel`;
CREATE TABLE `video_device_channel` (
  `id` bigint NOT NULL COMMENT '唯一标识符',
  `device_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备标识',
  `channel_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '通道标识',
  `stream_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '码流标识',
  `channel_type` tinyint NOT NULL DEFAULT '0' COMMENT '通道类型',
  `stream_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '码流类型',
  `channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '通道名称',
  `manufacturer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '厂商',
  `model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '型号',
  `block` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '警区',
  `province_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '省,直辖市编码',
  `city_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '市编码',
  `region_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '行政区划编码',
  `full_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '位置',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `safety_way` tinyint DEFAULT NULL COMMENT '信令安全模式',
  `register_way` tinyint DEFAULT NULL COMMENT '注册方式',
  `cert_num` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '证书序列号',
  `cert_status` tinyint DEFAULT NULL COMMENT '证书有效标识(0=有效,1=无效,2=过期,3=吊销)',
  `cert_invalid_reason_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '证书无效原因码',
  `cert_expiry_time` datetime DEFAULT NULL COMMENT '证书有效期截止时间',
  `secrecy` tinyint DEFAULT '0' COMMENT '保密属性(0-不涉密,1-涉密)',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '设备/系统IPv4/IPv6地址',
  `port` int DEFAULT NULL COMMENT '设备/系统端口',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '设备口令',
  `online_status` tinyint(1) DEFAULT '0' COMMENT '是否在线',
  `has_audio` tinyint(1) DEFAULT '0' COMMENT '是否含有音频',
  `ptz_type` tinyint DEFAULT NULL COMMENT '摄像机结构类型',
  `position_type` tinyint DEFAULT NULL COMMENT '摄像机位置类型扩展',
  `room_type` tinyint DEFAULT NULL COMMENT '摄像机安装位置类型',
  `use_type` tinyint DEFAULT NULL COMMENT '用途属性类型',
  `supply_light_type` tinyint DEFAULT NULL COMMENT '摄像机补光属性类型',
  `direction_type` tinyint DEFAULT NULL COMMENT '摄像机监视方位(光轴方向)属性类型',
  `resolution` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '分辨率',
  `download_speed` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '下载倍速',
  `svc_space_support_mod` tinyint DEFAULT NULL COMMENT '空域编码能力',
  `svc_time_support_mode` tinyint DEFAULT NULL COMMENT '时域编码能力',
  `extend_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device_identification` (`device_identification`) USING BTREE COMMENT '设备标识',
  KEY `idx_channel_identification` (`channel_identification`) USING BTREE COMMENT '通道标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流媒体设备通道表';

-- ----------------------------
-- Records of video_device_channel
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_device_info
-- ----------------------------
DROP TABLE IF EXISTS `video_device_info`;
CREATE TABLE `video_device_info` (
  `id` bigint NOT NULL COMMENT '唯一标识符',
  `device_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备标识',
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备名称',
  `custom_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '自定义名称',
  `media_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '媒体唯一标识',
  `manufacturer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '厂商',
  `model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '型号',
  `firmware` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '固件版本',
  `transport` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '传输协议（UDP/TCP）',
  `stream_mode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '数据流传输模式',
  `online_status` tinyint(1) DEFAULT '0' COMMENT '是否在线',
  `register_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '注册时间',
  `keepalive_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '心跳时间',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'IP',
  `port` int DEFAULT NULL COMMENT '端口',
  `wan_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '公网IP',
  `lan_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '局域网IP ',
  `host_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '访问地址',
  `expires` int DEFAULT NULL COMMENT '注册有效期',
  `subscribe_cycle_for_catalog` tinyint(1) DEFAULT '0' COMMENT '目录订阅',
  `subscribe_cycle_for_mobile_position` tinyint(1) DEFAULT '0' COMMENT '移动设备位置订阅',
  `subscribe_cycle_for_alarm` tinyint(1) DEFAULT '0' COMMENT '报警订阅',
  `mobile_position_submission_interval` int DEFAULT '5' COMMENT '移动设备位置信息上报时间间隔,单位:秒,默认值5',
  `charset` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字符集',
  `ssrc_check` tinyint(1) DEFAULT '0' COMMENT 'ssrc校验',
  `geo_coord_sys` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '地理坐标系',
  `sdp_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '收流IP',
  `local_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'SIP交互IP（设备访问平台的IP）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备密码',
  `as_message_channel` tinyint(1) DEFAULT '0' COMMENT '是否作为消息通道',
  `keepalive_interval_time` int DEFAULT NULL COMMENT '心跳间隔',
  `keepalive_timeout_count` int DEFAULT NULL COMMENT '心跳超时次数',
  `position_capability` int DEFAULT '0' COMMENT '定位功能支持情况(0-不支持;1-支持GPS定位;2-支持北斗定位)',
  `broadcast_push_after_ack` tinyint(1) DEFAULT '0' COMMENT '控制语音对讲流程，释放收到ACK后发流',
  `ability` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '能力',
  `extend_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_device_identification` (`device_identification`) USING BTREE COMMENT '设备标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流媒体设备信息表';

-- ----------------------------
-- Records of video_device_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_media_server
-- ----------------------------
DROP TABLE IF EXISTS `video_media_server`;
CREATE TABLE `video_media_server` (
  `id` bigint NOT NULL COMMENT '唯一标识符',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '名称',
  `media_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '媒体唯一标识',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '服务器IP地址',
  `hook_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'hook使用的IP（zlm访问客户端使用的IP）',
  `sdp_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'SDP IP地址',
  `stream_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '流IP地址',
  `http_port` int DEFAULT NULL COMMENT 'HTTP端口',
  `http_ssl_port` int DEFAULT NULL COMMENT 'HTTPS端口',
  `rtmp_port` int DEFAULT NULL COMMENT 'RTMP端口',
  `rtmp_ssl_port` int DEFAULT NULL COMMENT 'RTMP SSL端口',
  `rtp_proxy_port` int DEFAULT NULL COMMENT 'RTP代理端口（单端口模式）',
  `rtsp_port` int DEFAULT NULL COMMENT 'RTSP端口',
  `rtsp_ssl_port` int DEFAULT NULL COMMENT 'RTSP SSL端口',
  `flv_port` int DEFAULT NULL COMMENT 'FLV端口',
  `flv_ssl_port` int DEFAULT NULL COMMENT 'FLV SSL端口',
  `ws_flv_port` int DEFAULT NULL COMMENT 'WebSocket FLV端口',
  `ws_flv_ssl_port` int DEFAULT NULL COMMENT 'WebSocket FLV SSL端口',
  `auto_config` tinyint(1) DEFAULT '0' COMMENT '是否开启自动配置ZLM',
  `secret` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'ZLM鉴权参数',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'zlm' COMMENT '类型（zlm/abl）',
  `rtp_enable` tinyint(1) DEFAULT '0' COMMENT '是否启用多端口模式',
  `rtp_port_range` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '多端口RTP收流端口范围',
  `send_rtp_port_range` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'RTP发流端口范围',
  `record_assist_port` int DEFAULT NULL COMMENT '录制辅助服务端口',
  `default_server` tinyint(1) DEFAULT '0' COMMENT '是否是默认ZLM服务器',
  `last_alive_time` datetime DEFAULT NULL COMMENT '上次心跳时间',
  `hook_alive_interval` int DEFAULT NULL COMMENT 'keepalive hook触发间隔，单位秒',
  `record_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '录像存储路径',
  `record_day` int DEFAULT '7' COMMENT '录像存储时长（天）',
  `transcode_suffix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '转码的前缀',
  `online_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '在线状态(0:离线、1:在线)',
  `extend_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_media_server_unique_ip_http_port` (`ip`,`http_port`) USING BTREE COMMENT '唯一IP和HTTP端口组合',
  KEY `idx_type` (`type`) USING BTREE COMMENT '类型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流媒体服务器信息表';

-- ----------------------------
-- Records of video_media_server
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_stream_proxy
-- ----------------------------
DROP TABLE IF EXISTS `video_stream_proxy`;
CREATE TABLE `video_stream_proxy` (
  `id` bigint NOT NULL COMMENT '唯一标识符',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `proxy_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '代理类型',
  `proxy_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '代理名称',
  `stream_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '流唯一标识',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '拉流地址',
  `src_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '源地址',
  `dst_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '目标地址',
  `timeout_ms` int DEFAULT NULL COMMENT '超时时间（毫秒）',
  `ffmpeg_cmd_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'FFmpeg模板KEY',
  `rtp_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'RTSP拉流时的拉流方式（0：TCP，1：UDP，2：组播）',
  `gb_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '国标唯一标识',
  `media_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '媒体唯一标识',
  `enable_audio` tinyint(1) DEFAULT '0' COMMENT '是否启用音频',
  `enable_mp4` tinyint(1) DEFAULT '0' COMMENT '是否启用MP4',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态',
  `enable_remove_none_reader` tinyint(1) DEFAULT '0' COMMENT '无人观看时是否删除',
  `stream_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '拉流代理时ZLM返回的KEY，用于停止拉流代理',
  `enable_disable_none_reader` tinyint(1) DEFAULT '0' COMMENT '无人观看时是否自动停用',
  `extend_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_stream_proxy_app_id_stream_identification` (`app_id`,`stream_identification`) USING BTREE COMMENT '唯一应用和流标识组合'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='视频拉流代理信息表';

-- ----------------------------
-- Records of video_stream_proxy
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_stream_push
-- ----------------------------
DROP TABLE IF EXISTS `video_stream_push`;
CREATE TABLE `video_stream_push` (
  `id` bigint NOT NULL COMMENT 'id',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `stream_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '流唯一标识',
  `total_reader_count` int DEFAULT NULL COMMENT '观看总人数',
  `origin_type` tinyint DEFAULT NULL COMMENT '产生源类型',
  `origin_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '产生源的url',
  `vhost` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '音视频轨道',
  `bytes_speed` double(10,2) DEFAULT NULL COMMENT '数据产生速度，单位byte/s',
  `alive_second` bigint DEFAULT NULL COMMENT '存活时间，单位秒',
  `media_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '媒体唯一标识',
  `server_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '使用的服务ID',
  `push_time` datetime DEFAULT NULL COMMENT '推流时间',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态',
  `push_ing` tinyint(1) DEFAULT '0' COMMENT '是否正在推流',
  `self` tinyint(1) DEFAULT '0' COMMENT '是否自己平台的推流',
  `extend_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_stream_proxy_app_id_stream_identification` (`app_id`,`stream_identification`) USING BTREE COMMENT '唯一应用和流标识组合'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='视频推流信息表';

-- ----------------------------
-- Records of video_stream_push
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for view_project
-- ----------------------------
DROP TABLE IF EXISTS `view_project`;
CREATE TABLE `view_project` (
  `id` bigint NOT NULL COMMENT 'id',
  `project_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '项目标识',
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '项目名称',
  `status` tinyint NOT NULL DEFAULT '-1' COMMENT '项目状态[1-发布,-1-未发布]',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '存储数据',
  `index_image_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '首页图片',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='可视化项目表';

-- ----------------------------
-- Records of view_project
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for view_project_template
-- ----------------------------
DROP TABLE IF EXISTS `view_project_template`;
CREATE TABLE `view_project_template` (
  `id` bigint NOT NULL COMMENT 'id',
  `template_identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '模版标识',
  `template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '模版名称',
  `status` tinyint NOT NULL DEFAULT '-1' COMMENT '项目状态[1-发布,-1-未发布]',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '存储数据',
  `index_image_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '首页图片',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='可视化项目模板表';

-- ----------------------------
-- Records of view_project_template
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
