-- ThingLinks 租户库全量基线。
-- 基线版本：1.4.0；版本来源：thinglinks-cloud/.thinglinks-product.env。
-- 目标数据库：thinglinks_base_1；其他租户库应使用对应 tenantId 创建并初始化。
-- 适用于新建数据库；脚本包含 DROP TABLE IF EXISTS，不得用于增量升级。
-- 字符集：utf8mb4；排序规则：utf8mb4_general_ci。

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
INSERT INTO `base_dict` (`id`, `parent_id`, `parent_key`, `dict_group`, `key_`, `classify`, `data_type`, `name`, `state`, `remark`, `sort_value`, `icon`, `css_style`, `css_class`, `prop_type`, `i18n_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (497973033877635275, 475671003876294678, 'RULE_INSTANCE_ADDRESS', NULL, 'http://127.0.0.1:1880', '20', '1', '官方实例(IP模式)', b'1', '', 2, '', '', '', '', '{\"zh-CN\": \"官方实例(IP模式)\", \"en-US\": \"Official Instance (IP Mode)\", \"ja\": \"公式インスタンス(IPモード)\"}', 1452186486253289472, '2024-05-21 16:26:22', 1452186486253289472, '2025-09-19 14:44:02', NULL);
INSERT INTO `base_dict` (`id`, `parent_id`, `parent_key`, `dict_group`, `key_`, `classify`, `data_type`, `name`, `state`, `remark`, `sort_value`, `icon`, `css_style`, `css_class`, `prop_type`, `i18n_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (502678509857846274, 0, '', NULL, 'TENANT_AMAP__AAPPLICATION_JS_API_KEY', '10', '1', '高德应用JS开放APIKEY', b'1', '高德地图应用Web端(JS API)相关参数', 1, '', '', '', '', '{\"zh-CN\": \"高德应用JS开放APIKEY\", \"en-US\": \"AMap JS API Key\", \"ja\": \"AMap JS APIキー\"}', 1452186486253289472, '2024-06-05 15:58:27', 1452186486253289472, '2024-06-05 15:58:27', NULL);
INSERT INTO `base_dict` (`id`, `parent_id`, `parent_key`, `dict_group`, `key_`, `classify`, `data_type`, `name`, `state`, `remark`, `sort_value`, `icon`, `css_style`, `css_class`, `prop_type`, `i18n_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (502678509857846275, 502678509857846274, 'TENANT_AMAP__AAPPLICATION_JS_API_KEY', NULL, 'key', '10', '1', '__AMAP_JS_API_KEY__', b'1', 'KEY', 1, '', '', '', '', '{\"zh-CN\": \"__AMAP_JS_API_KEY__\", \"en-US\": \"__AMAP_JS_API_KEY__\", \"ja\": \"__AMAP_JS_API_KEY__\"}', 1452186486253289472, '2024-06-05 16:03:18', 1452186486253289472, '2024-06-11 22:04:39', NULL);
INSERT INTO `base_dict` (`id`, `parent_id`, `parent_key`, `dict_group`, `key_`, `classify`, `data_type`, `name`, `state`, `remark`, `sort_value`, `icon`, `css_style`, `css_class`, `prop_type`, `i18n_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (502678509857846276, 502678509857846274, 'TENANT_AMAP__AAPPLICATION_JS_API_KEY', NULL, 'securityJsCode', '10', '1', '__AMAP_SECURITY_JS_CODE__', b'1', '安全密钥', 1, '', '', '', '', '{\"zh-CN\": \"__AMAP_SECURITY_JS_CODE__\", \"en-US\": \"__AMAP_SECURITY_JS_CODE__\", \"ja\": \"__AMAP_SECURITY_JS_CODE__\"}', 1452186486253289472, '2024-06-05 16:04:03', 1452186486253289472, '2024-06-11 22:04:47', NULL);
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
INSERT INTO `base_employee` (`id`, `is_default`, `position_id`, `user_id`, `last_company_id`, `last_dept_id`, `real_name`, `active_status`, `position_status`, `state`, `created_by`, `created_time`, `updated_by`, `updated_time`, `created_org_id`) VALUES (339843960220418056, b'1', NULL, 1452186486253289472, NULL, NULL, 'ThingLinks管理员', '20', '10', b'1', 1452186486253289472, '2023-03-19 19:40:47', 1452186486253289472, '2023-03-19 19:40:47', NULL);
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
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666993, 524352044412200338, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666994, 337596429604225074, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666995, 524352044412200343, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666996, 524352044412200342, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666997, 524352044412200341, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666998, 524352044412200340, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
INSERT INTO `base_role_resource_rel` (`id`, `resource_id`, `application_id`, `role_id`, `created_time`, `created_by`, `updated_time`, `updated_by`, `created_org_id`) VALUES (743457911260666999, 524352044412200339, 3, 1452944729753780224, '2026-03-17 16:30:30', 1452186486253289472, '2026-03-17 16:30:30', 1452186486253289472, NULL);
COMMIT;

-- ----------------------------
-- Table structure for ca_cert_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `ca_cert_audit_log`;
CREATE TABLE `ca_cert_audit_log` (
  `id` bigint NOT NULL COMMENT 'id',
  `ca_id` bigint DEFAULT NULL COMMENT '关联 CA 证书 ID',
  `ca_serial_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'CA 证书序列号',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作类型: IMPORT/ISSUE/REVOKE/DOWNLOAD_PACK/SSL_TEST',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '详情(JSON 或自由文本)',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `updated_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除(0-未删除/1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ca_id` (`ca_id`) USING BTREE COMMENT 'CA ID 索引',
  KEY `idx_type_created` (`type`,`created_time`) USING BTREE COMMENT '类型+时间复合索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='CA 证书审计日志';

-- ----------------------------
-- Records of ca_cert_audit_log
-- ----------------------------
BEGIN;
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
  `last_status_event_hlc` bigint NOT NULL DEFAULT '0' COMMENT '最新状态事件因果时钟(HLC,64-bit)',
  `last_heartbeat_time` datetime DEFAULT NULL COMMENT '最新心跳时间',
  `device_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '设备标签',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `bound_product_version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '设备绑定的产品版本序号(快照标识,数据上报路径的物模型解析依据,灰度发布时可与产品当前版本不同)',
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
  `product_version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品版本序号',
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
  `active_version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '产品当前激活的版本序号(系统发布时生成的快照标识,16位短雪花字符串,非用户语义化版本号)',
  `previous_full_version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '灰度切换前的全量版本序号(仅灰度态有值,灰度晋升/回滚后清空,供回滚定位与灰度路由用)',
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
-- Table structure for product_publish_record
-- ----------------------------
DROP TABLE IF EXISTS `product_publish_record`;
CREATE TABLE `product_publish_record` (
  `id` bigint NOT NULL COMMENT 'id',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `source_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '源版本号',
  `target_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '目标版本号',
  `intent` tinyint NOT NULL DEFAULT '0' COMMENT '操作意图[0-发布 1-回滚 2-历史清理]',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '执行状态[0-执行中 1-成功 2-失败]',
  `ddl_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'DDL列表JSON数组(已执行的DDL明细 + 重试元数据)',
  `canary_result_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '策略执行结果快照JSON',
  `failed_reason` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '失败原因(成功时为空)',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数(达上限不再重跑)',
  `max_retry_count` int NOT NULL DEFAULT '3' COMMENT '最大重试次数(用户可配,上限10)',
  `started_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finished_time` datetime DEFAULT NULL COMMENT '结束时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_product_identification` (`product_identification`) USING BTREE COMMENT '产品标识',
  KEY `idx_target_version` (`target_version`) USING BTREE COMMENT '目标版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='产品发布记录';

-- ----------------------------
-- Records of product_publish_record
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
-- Table structure for product_version
-- ----------------------------
DROP TABLE IF EXISTS `product_version`;
CREATE TABLE `product_version` (
  `id` bigint NOT NULL COMMENT 'id',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版本序号(系统发布时生成的不可变快照标识,16位短雪花字符串)',
  `version_status` tinyint NOT NULL DEFAULT '0' COMMENT '版本状态[0-草稿 1-已发布 2-灰度中 3-影子 4-已回滚 5-已归档]',
  `product_snapshot_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '产品快照JSON(冻结整棵产品树)',
  `publish_strategy` tinyint DEFAULT NULL COMMENT '发布策略[0-全量 1-灰度 2-影子]',
  `canary_config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '灰度配置JSON',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_version_no` (`version_no`) USING BTREE COMMENT '版本序号',
  KEY `idx_product_identification` (`product_identification`) USING BTREE COMMENT '产品标识',
  KEY `idx_version_status` (`version_status`) USING BTREE COMMENT '版本状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='产品物模型版本快照';

-- ----------------------------
-- Records of product_version
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_version_change_log
-- ----------------------------
DROP TABLE IF EXISTS `product_version_change_log`;
CREATE TABLE `product_version_change_log` (
  `id` bigint NOT NULL COMMENT 'id',
  `product_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '版本序号:本批变更归属版本(草稿期累积,发布后固化,对应 product_version.version_no)',
  `change_type` tinyint NOT NULL DEFAULT '1' COMMENT '变更类型[0-新增 1-编辑 2-删除]',
  `target_type` tinyint DEFAULT NULL COMMENT '变更维度[0-产品信息 1-服务 2-属性 3-命令]',
  `change_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '变更摘要',
  `change_detail_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '字段级变更明细JSON(覆盖产品所有字段)',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除、1-已删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_product_identification` (`product_identification`) USING BTREE COMMENT '产品标识',
  KEY `idx_change_type` (`change_type`) USING BTREE COMMENT '变更类型'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='产品物模型版本变更日志';

-- ----------------------------
-- Records of product_version_change_log
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
-- Table structure for rule_bridge_execution_step
-- ----------------------------
DROP TABLE IF EXISTS `rule_bridge_execution_step`;
CREATE TABLE `rule_bridge_execution_step` (
  `id` bigint NOT NULL COMMENT '主键',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '关联 trace（FK→rule_bridge_execution_trace.trace_id）',
  `bridge_rule_id` bigint DEFAULT NULL COMMENT '关联桥接规则 ID(同 traceId 命中多条规则时区分 step 归属)',
  `step_no` int NOT NULL DEFAULT '0' COMMENT '步骤顺序号（从1起，前端按此排序）',
  `step_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '类型枚举：INGEST-数据接入 / RULE_MATCH-规则匹配 / RATE_LIMIT-限流 / TRANSFORM-脚本转换 / SINK_SEND-投递 / DEAD_LETTER-死信 / INBOUND_FORWARD-入站还原',
  `step_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '步骤可读名称（中文，前端卡片标题用）',
  `status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '00' COMMENT '00-成功 / 01-失败 / 02-跳过',
  `latency_ms` int DEFAULT NULL COMMENT '本步骤耗时（毫秒）',
  `input_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '输入摘要 JSON（envelope payload 前 1KB / 命中条件 / 模板变量等）',
  `output_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '输出摘要 JSON（转换后 payload / sink 返回值 / 发送 messageId 等）',
  `error_msg` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '失败错误（status=01 时填；透传下游 raw 错误，对齐 ERROR_MSG_MAX_LENGTH=4000）',
  `started_at` datetime(3) NOT NULL COMMENT '步骤开始时间（毫秒精度）',
  `extend_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '扩展参数（步骤特异协议数据 JSON：SINK_SEND 含 sinkType/partition/messageId；RULE_MATCH 含命中条件细节；RATE_LIMIT 含阈值/当前 QPS；TRANSFORM 含 scriptId/scriptVersion 等）',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status_time` (`status`,`started_at`) USING BTREE COMMENT '按状态查异常步骤（监控告警用）',
  KEY `idx_trace_rule_step` (`trace_id`,`bridge_rule_id`,`step_no`) USING BTREE COMMENT '按 (traceId+ruleId) 查 step,详情抽屉用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='桥接执行步骤明细（链路回放展示用）';

-- ----------------------------
-- Records of rule_bridge_execution_step
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_bridge_execution_trace
-- ----------------------------
DROP TABLE IF EXISTS `rule_bridge_execution_trace`;
CREATE TABLE `rule_bridge_execution_trace` (
  `id` bigint NOT NULL COMMENT '主键',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '全链路追踪ID（贯穿 mqs → RocketMQ → rule，可与设备 publish 日志串联）',
  `bridge_rule_id` bigint DEFAULT NULL COMMENT '关联桥接规则 ID（出站必填；入站为订阅源拉取时为空）',
  `bridge_rule_id_key` varchar(22) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci GENERATED ALWAYS AS ((case when (`bridge_rule_id` is null) then _utf8mb4'N:' else concat(_utf8mb4'V:',`bridge_rule_id`) end)) STORED COMMENT '桥接规则ID唯一键归一值',
  `direction` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '10' COMMENT '桥接方向：10-出站 / 20-入站',
  `trigger_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '触发来源：DEVICE_DATA-设备数据 / SUBSCRIPTION-订阅源 / TEST_SINK-测试发送 / REPLAY-死信回放',
  `product_identification` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '产品标识（出站时来自设备事件）',
  `device_identification` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备标识（出站时来自设备事件）',
  `action_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '事件类型（PUBLISH/CONNECT/CLOSE/...，复用 LINK_DEVICE_ACTION_TYPE 字典）',
  `topic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备事件 topic',
  `data_source_id` bigint DEFAULT NULL COMMENT '关联数据源 ID（出站=目标 sink；入站=来源 source）',
  `subscription_source_id` bigint DEFAULT NULL COMMENT '关联订阅源 ID（仅入站）',
  `status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '00' COMMENT '整体状态：00-成功 / 01-失败 / 02-部分成功 / 03-死信',
  `step_count` int NOT NULL DEFAULT '0' COMMENT '执行的步骤总数（关联 rule_bridge_execution_step 计数）',
  `total_latency_ms` int DEFAULT NULL COMMENT '总耗时毫秒（开始到结束）',
  `start_time` datetime(3) NOT NULL COMMENT '执行开始时间（毫秒精度）',
  `end_time` datetime(3) DEFAULT NULL COMMENT '执行结束时间（毫秒精度）',
  `source_payload_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '源消息摘要（完整 envelope 报文；便于排查 + 死信回放）',
  `result_summary` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结果摘要（成功的 sink / 失败原因等一句话；对齐 RESULT_SUMMARY_MAX_LENGTH=2000）',
  `error_msg` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '失败时的错误信息（透传 RocketMQ/Kafka/HTTP 等下游 raw 错误，含堆栈描述；对齐 ERROR_MSG_MAX_LENGTH=4000）',
  `extend_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '扩展参数',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_trace_rule_key` (`trace_id`,`bridge_rule_id_key`) USING BTREE COMMENT '同一 envelope 与规则归一键唯一;RocketMQ 重投同 envelope 同规则被拦截',
  KEY `idx_rule_status_time` (`bridge_rule_id`,`status`,`start_time`) USING BTREE COMMENT '按规则+状态+时间查日志',
  KEY `idx_device_time` (`device_identification`,`start_time`) USING BTREE COMMENT '按设备排查链路',
  KEY `idx_rule_time` (`bridge_rule_id`,`start_time`) USING BTREE COMMENT '按规则+时间查日志 / 统计',
  KEY `idx_start_time` (`start_time`) USING BTREE COMMENT '按执行时间倒序分页 / 时间范围统计',
  KEY `idx_status_time` (`status`,`start_time`) USING BTREE COMMENT '按状态+执行时间筛选 trace',
  KEY `idx_org_time` (`created_org_id`,`start_time`) USING BTREE COMMENT '按组织数据权限+执行时间查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='桥接执行trace主表（链路回放用）';

-- ----------------------------
-- Records of rule_bridge_execution_trace
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
-- Table structure for rule_data_bridge
-- ----------------------------
DROP TABLE IF EXISTS `rule_data_bridge`;
CREATE TABLE `rule_data_bridge` (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `rule_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规则名称（列表页展示）',
  `rule_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规则业务唯一编码（snowflake）',
  `direction` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '10' COMMENT '桥接方向：10-出站(平台→第三方) / 20-入站(第三方→平台)',
  `data_source_id` bigint NOT NULL COMMENT '关联数据源 FK→rule_data_source.id',
  `match_config_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '匹配条件JSON。出站含productIdentifications/actionTypes/topicFilter/deviceFilter/payloadFilter/timeWindow；入站含subscriptionSourceIds/messageFilter',
  `action_config_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作配置JSON（含 sink 特异参数；EncryptTypeHandler 整体加密落盘，防内联 Bearer token 等泄漏）。出站含payloadTemplate/transformScript/sourceType特异参数；入站含targetHandler/fieldMapping',
  `qos` int DEFAULT NULL COMMENT '规则级可靠性级别覆盖（NULL=用数据源默认）',
  `rate_limit_qps` int DEFAULT NULL COMMENT '规则级 QPS 限流覆盖',
  `retry_max_times` int DEFAULT NULL COMMENT '规则级最大重试次数覆盖',
  `retry_backoff_ms` int DEFAULT NULL COMMENT '规则级初始退避时长覆盖（毫秒）',
  `timeout_ms` int DEFAULT NULL COMMENT '规则级单次发送超时覆盖（毫秒）',
  `dead_letter_data_source_id` bigint DEFAULT NULL COMMENT '规则级死信数据源覆盖',
  `enable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用：0-禁用 / 1-启用（必须测试发送成功后手动启用）',
  `priority` int NOT NULL DEFAULT '100' COMMENT '优先级（数字越小越先匹配；同事件命中多条时按此排序）',
  `extend_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '扩展参数（兜底，未来加加密/流量分级/A-B 灰度等 0 改表）',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_rule_code` (`rule_code`) USING BTREE COMMENT '规则编码全局唯一',
  KEY `idx_app_direction_enable` (`app_id`,`direction`,`enable`) USING BTREE COMMENT '按应用+方向+状态聚合（matcher 主索引）',
  KEY `idx_data_source_id` (`data_source_id`) USING BTREE COMMENT '按数据源反查关联规则'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据桥接-规则';

-- ----------------------------
-- Records of rule_data_bridge
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rule_data_source
-- ----------------------------
DROP TABLE IF EXISTS `rule_data_source`;
CREATE TABLE `rule_data_source` (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `data_source_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '数据源名称（用户起的友好标识，列表页显示）',
  `data_source_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '业务唯一编码（snowflake，外部系统引用）',
  `direction` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '10' COMMENT '方向：10-出站sink / 20-入站source / 30-双向',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '协议类型：KAFKA/REDIS/ROCKETMQ/MYSQL/HTTP/WEBHOOK/MQTT；与 ConnectorType 1:1 对齐',
  `connection_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '连接参数JSON（host/port/topic/database/mode 等；EncryptTypeHandler 整体加密落盘）',
  `credential_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '凭证JSON（密码/密钥/token；EncryptTypeHandler 整体加密落盘）',
  `serialization` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'JSON' COMMENT '序列化策略：JSON/AVRO/STRING/BINARY（与 Serializer.name() 匹配）',
  `default_qos` int NOT NULL DEFAULT '1' COMMENT '默认可靠性级别：0-fire-forget / 1-at-least-once / 2-exactly-once',
  `default_rate_limit_qps` int NOT NULL DEFAULT '0' COMMENT '默认 QPS 限流（0=不限）',
  `default_retry_max_times` int NOT NULL DEFAULT '3' COMMENT '默认最大重试次数（不含首次发送）',
  `default_retry_backoff_ms` int NOT NULL DEFAULT '1000' COMMENT '默认初始退避时长 ms（指数倍增 1s/2s/4s/...）',
  `default_timeout_ms` int NOT NULL DEFAULT '5000' COMMENT '默认单次发送超时 ms',
  `default_dead_letter_data_source_id` bigint DEFAULT NULL COMMENT '默认死信投递的数据源 FK（一般指向告警 Kafka）',
  `enable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用：0-禁用 / 1-启用（必须测试连接成功后手动启用）',
  `health_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'UNKNOWN' COMMENT '健康状态：HEALTHY/DEGRADED/DOWN/UNKNOWN（HealthCheckScheduler 5min 探活更新）',
  `last_health_check_time` datetime DEFAULT NULL COMMENT '上次健康检查时间',
  `extend_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '扩展参数（协议特异调参 JSON：acks/compression/timeout/poolSize 等）',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_data_source_code` (`data_source_code`) USING BTREE COMMENT '业务编码全局唯一',
  KEY `idx_app_id_direction` (`app_id`,`direction`) USING BTREE COMMENT '按应用+方向查询',
  KEY `idx_enable` (`enable`) USING BTREE COMMENT '按状态过滤启用项'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据桥接-数据源（出/入站共用）';

-- ----------------------------
-- Records of rule_data_source
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
  KEY `idx_rule_identification` (`rule_identification`) USING BTREE COMMENT '规则唯一标识',
  KEY `idx_start_time` (`start_time`) USING BTREE COMMENT '按执行时间倒序分页 / 时间范围统计',
  KEY `idx_status_start_time` (`status`,`start_time`) USING BTREE COMMENT '按状态+执行时间筛选执行日志',
  KEY `idx_rule_start_time` (`rule_identification`,`start_time`) USING BTREE COMMENT '按规则标识+执行时间筛选执行日志',
  KEY `idx_org_start_time` (`created_org_id`,`start_time`) USING BTREE COMMENT '按组织数据权限+执行时间查询'
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
  `script_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '脚本类型',
  `channel_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '渠道编码',
  `product_identification` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品标识',
  `topic_pattern` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '主题模式',
  `object_version` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版本号',
  `enable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用',
  `script_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '脚本内容',
  `extend_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展信息',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_only_key` (`script_type`,`channel_code`,`product_identification`,`topic_pattern`,`object_version`) USING BTREE
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
-- Table structure for rule_subscription_source
-- ----------------------------
DROP TABLE IF EXISTS `rule_subscription_source`;
CREATE TABLE `rule_subscription_source` (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '应用ID',
  `source_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '订阅源名称（用户可读）',
  `source_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '业务唯一编码（snowflake；HTTP 入站 endpoint URL 用此值）',
  `data_source_id` bigint NOT NULL COMMENT '复用数据源 FK→rule_data_source.id（direction 须为 20-入站 或 30-双向）',
  `target_handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MQTT_FORWARD' COMMENT '入站后处理方式：MQTT_FORWARD-伪装设备 publish / RAW_INSERT-直接写 DeviceAction / RULE_TRIGGER-触发场景联动',
  `mapping_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字段映射 JSON（如 [{"sourceField":"device_id","targetField":"deviceIdentification"}]）',
  `target_product_identification` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'target_handler=MQTT_FORWARD 时的目标产品标识',
  `target_topic_template` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '目标 topic 模板（含 ${} 占位符，如 $thing/up/property/${productId}/${deviceId}）',
  `enable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用：0-禁用 / 1-启用（必须测试连接成功后手动启用）',
  `last_consume_offset` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上次消费位点（Kafka offset / MQTT messageId / HTTP 时间戳；重启后接续消费）',
  `extend_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '扩展参数',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_source_code` (`source_code`) USING BTREE COMMENT '编码全局唯一',
  KEY `idx_app_id_enable` (`app_id`,`enable`) USING BTREE COMMENT '按应用+状态查启用订阅源（启动时扫描用）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据桥接-订阅源';

-- ----------------------------
-- Records of rule_subscription_source
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
-- Table structure for video_channel
-- ----------------------------
DROP TABLE IF EXISTS `video_channel`;
CREATE TABLE `video_channel` (
  `id` bigint NOT NULL COMMENT '主键',
  `device_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属设备标识',
  `channel_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通道标识',
  `channel_no` int DEFAULT NULL COMMENT '逻辑通道号',
  `channel_type` smallint DEFAULT NULL COMMENT '通道类型(GB28181行业编码131~143)',
  `channel_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '通道名称',
  `stream_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '流标识',
  `stream_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '流类型',
  `manufacturer` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '厂商',
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '型号',
  `online_status` tinyint(1) DEFAULT '0' COMMENT '在线状态(0=离线/1=在线)',
  `host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '通道地址(IP/域名)',
  `port` int DEFAULT NULL COMMENT '端口',
  `password` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '设备口令（EncryptTypeHandler 加密落盘）',
  `longitude` decimal(12,8) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(12,8) DEFAULT NULL COMMENT '纬度',
  `full_address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '安装地址',
  `province_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '省级编码',
  `city_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '市级编码',
  `region_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '行政区划编码',
  `has_audio` tinyint(1) DEFAULT '0' COMMENT '支持音频(0=否/1=是)',
  `ptz_type` tinyint DEFAULT NULL COMMENT '云台类型(0=未知/1=球机/2=半球/3=固定枪机/4=遥控枪机)',
  `ptz_capability` tinyint(1) DEFAULT '0' COMMENT '支持云台控制(0=否/1=是)',
  `talk_capability` tinyint(1) DEFAULT '0' COMMENT '支持对讲(0=否/1=是)',
  `secrecy` tinyint DEFAULT '0' COMMENT '保密属性(0=不涉密/1=涉密)',
  `channel_config` json DEFAULT NULL COMMENT '通道专属配置(JSON)',
  `extend_params` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '扩展参数',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_channel_identification` (`channel_identification`) USING BTREE,
  KEY `idx_device_identification` (`device_identification`) USING BTREE,
  KEY `idx_online_status` (`online_status`) USING BTREE,
  KEY `idx_stream_identification` (`stream_identification`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='统一通道表';

-- ----------------------------
-- Records of video_channel
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_device
-- ----------------------------
DROP TABLE IF EXISTS `video_device`;
CREATE TABLE `video_device` (
  `id` bigint NOT NULL COMMENT '主键',
  `device_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备标识',
  `access_protocol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备接入协议(GB28181/ONVIF/ISUP/JT1078/SIP/PELCO_D/PELCO_P)',
  `device_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备名称',
  `custom_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '自定义名称',
  `manufacturer` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '厂商',
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '型号',
  `firmware` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '固件版本',
  `host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备地址(IP/域名)',
  `port` int DEFAULT NULL COMMENT '端口',
  `wan_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '公网地址(IP/域名)',
  `lan_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '局域网地址(IP/域名)',
  `access_endpoint` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '完整访问端点(host:port)',
  `sdp_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收流地址(IP/域名)',
  `local_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '本地SIP交互地址(IP/域名)',
  `transport` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '传输协议(UDP/TCP)',
  `stream_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数据流传输模式',
  `online_status` tinyint(1) DEFAULT '0' COMMENT '是否在线(0=离线/1=在线)',
  `register_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '注册时间',
  `last_keepalive_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后心跳时间',
  `expires` int DEFAULT NULL COMMENT '注册有效期(秒)',
  `keepalive_interval` int DEFAULT NULL COMMENT '心跳间隔(秒)',
  `keepalive_timeout_count` int DEFAULT NULL COMMENT '心跳超时次数',
  `auth_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '认证方式(PASSWORD/VALIDATE_CODE/AUTH_TOKEN/CERTIFICATE/DIGEST/NONE)',
  `auth_secret` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '认证密钥(加密存储)',
  `media_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '媒体服务唯一标识',
  `channel_count` int DEFAULT '0' COMMENT '通道数量',
  `ability` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备能力集描述',
  `protocol_config` json DEFAULT NULL COMMENT '协议专属配置(JSON)',
  `extend_params` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '扩展参数',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_device_identification` (`device_identification`) USING BTREE,
  KEY `idx_access_protocol` (`access_protocol`) USING BTREE,
  KEY `idx_online_status` (`online_status`) USING BTREE,
  KEY `idx_media_identification` (`media_identification`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='统一设备表';

-- ----------------------------
-- Records of video_device
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_device_alarm
-- ----------------------------
DROP TABLE IF EXISTS `video_device_alarm`;
CREATE TABLE `video_device_alarm` (
  `id` bigint NOT NULL COMMENT '主键',
  `device_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备国标编号',
  `channel_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '通道国标编号',
  `alarm_priority` tinyint DEFAULT NULL COMMENT '告警级别(1=一级警情/2=二级警情/3=三级警情/4=四级警情)',
  `alarm_method` tinyint DEFAULT NULL COMMENT '告警方式',
  `alarm_time` datetime DEFAULT NULL COMMENT '告警时间（设备上报时间）',
  `alarm_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '告警描述',
  `alarm_type` tinyint DEFAULT NULL COMMENT '告警类型',
  `alarm_type_param` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '告警类型参数(JSON)',
  `longitude` double DEFAULT NULL COMMENT '经度',
  `latitude` double DEFAULT NULL COMMENT '纬度',
  `handle_status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态(0=未处理/1=已确认/2=已消除/3=已忽略)',
  `handle_user_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_result` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '处理结果/备注',
  `created_org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device_identification` (`device_identification`) USING BTREE,
  KEY `idx_alarm_time` (`alarm_time`) USING BTREE,
  KEY `idx_handle_status` (`handle_status`) USING BTREE,
  KEY `idx_alarm_priority` (`alarm_priority`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='视频设备告警表';

-- ----------------------------
-- Records of video_device_alarm
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_device_group
-- ----------------------------
DROP TABLE IF EXISTS `video_device_group`;
CREATE TABLE `video_device_group` (
  `id` bigint NOT NULL COMMENT '主键',
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组名称',
  `parent_id` bigint DEFAULT NULL COMMENT '上级分组ID(顶层为空)',
  `group_type` tinyint NOT NULL DEFAULT '0' COMMENT '分组类型(0=自定义分组/1=行政区划/2=业务分组)',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序序号',
  `group_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '层级路径(如: /1/2/3，便于快速查子孙)',
  `group_level` int NOT NULL DEFAULT '1' COMMENT '层级深度(从1开始)',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图标标识',
  `enable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '启用状态(0=禁用/1=启用)',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数(JSON)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `created_org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_group_path` (`group_path`(191)) USING BTREE,
  KEY `idx_sort_order` (`sort_order`) USING BTREE,
  KEY `idx_enable` (`enable`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='视频设备分组表';

-- ----------------------------
-- Records of video_device_group
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_device_group_relation
-- ----------------------------
DROP TABLE IF EXISTS `video_device_group_relation`;
CREATE TABLE `video_device_group_relation` (
  `id` bigint NOT NULL COMMENT '主键',
  `group_id` bigint NOT NULL COMMENT '分组ID',
  `device_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备国标编号',
  `channel_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '通道国标编号(空表示设备级别关联)',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '分组内排序序号',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数(JSON)',
  `created_org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `channel_identification_key` varchar(52) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci GENERATED ALWAYS AS ((case when (`deleted` <> 0) then concat(_utf8mb4'D:',`id`) when (nullif(trim(`channel_identification`),_utf8mb4'') is null) then _utf8mb4'N:' else concat(_utf8mb4'V:',trim(`channel_identification`)) end)) STORED COMMENT '关联唯一键归一值',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_group_device_channel_key` (`group_id`,`device_identification`,`channel_identification_key`) USING BTREE,
  KEY `idx_group_id` (`group_id`) USING BTREE,
  KEY `idx_device_identification` (`device_identification`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='视频设备分组关联表';

-- ----------------------------
-- Records of video_device_group_relation
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_device_mobile_position
-- ----------------------------
DROP TABLE IF EXISTS `video_device_mobile_position`;
CREATE TABLE `video_device_mobile_position` (
  `id` bigint NOT NULL COMMENT '主键',
  `device_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备国标编号',
  `channel_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '通道国标编号',
  `longitude` double DEFAULT NULL COMMENT '经度',
  `latitude` double DEFAULT NULL COMMENT '纬度',
  `altitude` double DEFAULT NULL COMMENT '海拔高度(米)',
  `speed` double DEFAULT NULL COMMENT '速度(km/h)',
  `direction` double DEFAULT NULL COMMENT '方向角(度，正北为0，顺时针)',
  `report_time` datetime DEFAULT NULL COMMENT '位置上报时间',
  `geo_coord_sys` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'WGS84' COMMENT '坐标系(WGS84/GCJ02/BD09)',
  `created_org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device_identification` (`device_identification`) USING BTREE,
  KEY `idx_report_time` (`report_time`) USING BTREE,
  KEY `idx_device_time` (`device_identification`,`report_time`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='视频设备移动位置表';

-- ----------------------------
-- Records of video_device_mobile_position
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_gateway_mapping
-- ----------------------------
DROP TABLE IF EXISTS `video_gateway_mapping`;
CREATE TABLE `video_gateway_mapping` (
  `id` bigint NOT NULL COMMENT '主键',
  `src_protocol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '源协议(JT1078/ISUP等)',
  `src_device_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '源设备标识',
  `src_channel_identification` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '源通道标识',
  `gb_device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '映射GB28181设备编号',
  `gb_channel_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '映射GB28181通道编号',
  `gb_platform_id` bigint DEFAULT NULL COMMENT '目标上级平台ID',
  `enable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用(0=禁用/1=启用)',
  `auto_push` tinyint(1) NOT NULL DEFAULT '0' COMMENT '自动推流(0=否/1=是)',
  `mapping_config` json DEFAULT NULL COMMENT '映射配置(JSON)',
  `register_status` tinyint(1) DEFAULT '0' COMMENT '注册状态(0=未注册/1=已注册)',
  `last_register_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后注册时间',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `src_channel_identification_key` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci GENERATED ALWAYS AS ((case when (`deleted` <> 0) then concat(_utf8mb4'D:',`id`) when (nullif(trim(`src_channel_identification`),_utf8mb4'') is null) then _utf8mb4'N:' else concat(_utf8mb4'V:',trim(`src_channel_identification`)) end)) STORED COMMENT '映射唯一键归一值',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_src_device_channel_key` (`src_protocol`,`src_device_identification`,`src_channel_identification_key`) USING BTREE,
  KEY `idx_gb_device_id` (`gb_device_id`) USING BTREE,
  KEY `idx_gb_channel_id` (`gb_channel_id`) USING BTREE,
  KEY `idx_gb_platform_id` (`gb_platform_id`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='网关协议映射表';

-- ----------------------------
-- Records of video_gateway_mapping
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
  `host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '服务器地址(IP/域名)',
  `hook_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Hook回调地址(IP/域名)',
  `sdp_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'SDP地址(IP/域名)',
  `stream_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '流播放地址(IP/域名)',
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
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '服务器版本号',
  `capabilities` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '服务器能力集(JSON，标记支持哪些API)',
  `max_streams` int DEFAULT NULL COMMENT '最大承载流数量(用于负载均衡)',
  `current_streams` int DEFAULT '0' COMMENT '当前流数量',
  `cpu_usage` decimal(5,2) DEFAULT NULL COMMENT 'CPU使用率(心跳上报)',
  `memory_usage` decimal(5,2) DEFAULT NULL COMMENT '内存使用率(心跳上报)',
  `network_in_speed` bigint DEFAULT NULL COMMENT '入网速率bytes/s(心跳上报)',
  `network_out_speed` bigint DEFAULT NULL COMMENT '出网速率bytes/s(心跳上报)',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建人组织',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_media_server_unique_ip_http_port` (`host`,`http_port`) USING BTREE COMMENT '唯一IP和HTTP端口组合',
  KEY `idx_type` (`type`) USING BTREE COMMENT '类型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流媒体服务器信息表';

-- ----------------------------
-- Records of video_media_server
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_notify_subscription
-- ----------------------------
DROP TABLE IF EXISTS `video_notify_subscription`;
CREATE TABLE `video_notify_subscription` (
  `id` bigint NOT NULL COMMENT '主键',
  `subscription_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订阅名称',
  `channel_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '渠道类型(字典 NOTIFY_CHANNEL_TYPE)',
  `channel_config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '渠道凭证JSON（EncryptTypeHandler 整体加密落盘）',
  `template_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息模板编码(ExtendMsgTemplate.code)',
  `event_types` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订阅事件类型(逗号分隔)',
  `priority_filter` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '告警级别过滤(逗号分隔,空=全部)',
  `recipient_scope` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SELF' COMMENT '接收范围: SELF/ORG/CUSTOM',
  `recipient_ids` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '接收人用户ID(逗号分隔)',
  `at_all` tinyint NOT NULL DEFAULT '0' COMMENT '@所有人(0=否/1=是)',
  `jump_url_template` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '跳转链接模板',
  `msg_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '消息内容模板(支持${变量})',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用/1=启用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_event_types` (`event_types`(100)) USING BTREE,
  KEY `idx_channel_type` (`channel_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='视频事件通知订阅配置';

-- ----------------------------
-- Records of video_notify_subscription
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_platform
-- ----------------------------
DROP TABLE IF EXISTS `video_platform`;
CREATE TABLE `video_platform` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '平台名称',
  `enable` tinyint(1) DEFAULT '1' COMMENT '启用状态(0=禁用/1=启用)',
  `server_gb_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '平台国标编号',
  `server_gb_domain` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '平台国标域',
  `server_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '上级SIP服务IP/域名',
  `server_port` int DEFAULT NULL COMMENT '平台SIP端口',
  `device_gb_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '本平台在上级的设备编号',
  `device_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '本机设备IP/域名',
  `device_port` int DEFAULT NULL COMMENT '本平台SIP端口',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '认证用户名',
  `password` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '认证密码（EncryptTypeHandler 加密落盘）',
  `expires` int DEFAULT '3600' COMMENT '注册有效期(秒)',
  `keep_timeout` int DEFAULT '60' COMMENT '心跳超时(秒)',
  `transport` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'UDP' COMMENT '传输协议(UDP/TCP)',
  `character_set` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'GB2312' COMMENT '字符集(GB2312/UTF-8)',
  `ptz` tinyint(1) DEFAULT '0' COMMENT '是否支持PTZ',
  `rtcp` tinyint(1) DEFAULT '0' COMMENT '是否支持RTCP',
  `status` tinyint(1) DEFAULT '0' COMMENT '注册状态(0=未注册/1=已注册)',
  `catalog_subscribe` tinyint(1) DEFAULT '0' COMMENT '订阅目录变化',
  `alarm_subscribe` tinyint(1) DEFAULT '0' COMMENT '订阅告警',
  `mobile_position_subscribe` tinyint(1) DEFAULT '0' COMMENT '订阅位置',
  `catalog_group` int DEFAULT '1' COMMENT '目录分组大小',
  `as_message_channel` tinyint(1) DEFAULT '0' COMMENT '作为消息通道',
  `send_stream_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '推流IP/域名',
  `auto_push_channel` tinyint(1) DEFAULT '0' COMMENT '自动推送通道',
  `catalog_with_platform` int DEFAULT '0' COMMENT '目录包含平台',
  `catalog_with_group` int DEFAULT '0' COMMENT '目录包含分组',
  `catalog_with_region` int DEFAULT '0' COMMENT '目录包含区域',
  `civil_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '行政区划编码',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '厂商',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '型号',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地址',
  `register_way` tinyint DEFAULT '1' COMMENT '注册方式(1=符合标准)',
  `secrecy` tinyint DEFAULT '0' COMMENT '保密属性(0=不涉密/1=涉密)',
  `server_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '服务器ID',
  `cascade_type` tinyint DEFAULT '0' COMMENT '级联类型(0=作为下级/1=作为上级)',
  `gb_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'GB28181协议版本',
  `online_status` tinyint(1) DEFAULT '0' COMMENT '在线状态(0=离线/1=在线)',
  `register_expires` int DEFAULT '3600' COMMENT '注册有效期(秒)',
  `keepalive_interval` int DEFAULT '60' COMMENT '心跳间隔(秒)',
  `keepalive_timeout_count` int DEFAULT '3' COMMENT '心跳超时次数',
  `last_register_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最近注册时间',
  `last_keepalive_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最近心跳时间',
  `start_offline_push` tinyint(1) DEFAULT '0' COMMENT '推送离线通道',
  `sip_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'SIP服务IP/域名',
  `sip_port` int DEFAULT NULL COMMENT 'SIP监听端口',
  `hook_url_prefix` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Hook URL前缀',
  `service_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '服务实例ID',
  `cascade_sdp_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '级联SDP IP/域名',
  `created_org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_server_gb_id` (`server_gb_id`) USING BTREE,
  KEY `idx_online_status` (`online_status`) USING BTREE,
  KEY `idx_enable` (`enable`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='视频级联平台表';

-- ----------------------------
-- Records of video_platform
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_platform_catalog
-- ----------------------------
DROP TABLE IF EXISTS `video_platform_catalog`;
CREATE TABLE `video_platform_catalog` (
  `id` bigint NOT NULL COMMENT '主键',
  `platform_id` bigint NOT NULL COMMENT '级联平台ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '目录名称',
  `gb_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '目录国标编号',
  `parent_id` bigint DEFAULT NULL COMMENT '上级目录ID(顶层为空)',
  `catalog_type` tinyint DEFAULT '0' COMMENT '目录类型(0=行政区划/1=业务分组/2=虚拟组织)',
  `civil_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '行政区划编码',
  `sort_order` int DEFAULT '0' COMMENT '排序序号',
  `created_org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_platform_id` (`platform_id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='视频级联目录表';

-- ----------------------------
-- Records of video_platform_catalog
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_platform_channel
-- ----------------------------
DROP TABLE IF EXISTS `video_platform_channel`;
CREATE TABLE `video_platform_channel` (
  `id` bigint NOT NULL COMMENT '主键',
  `platform_id` bigint NOT NULL COMMENT '级联平台ID',
  `device_channel_id` bigint DEFAULT NULL COMMENT '设备通道表ID',
  `catalog_id` bigint DEFAULT NULL COMMENT '所属目录ID',
  `device_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备国标编号',
  `channel_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '通道国标编号',
  `custom_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '自定义名称',
  `custom_gb_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '自定义国标编号',
  `created_org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_platform_id` (`platform_id`) USING BTREE,
  KEY `idx_device_identification` (`device_identification`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='视频级联平台通道关联表';

-- ----------------------------
-- Records of video_platform_channel
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_record_file
-- ----------------------------
DROP TABLE IF EXISTS `video_record_file`;
CREATE TABLE `video_record_file` (
  `id` bigint NOT NULL COMMENT '主键',
  `plan_id` bigint DEFAULT NULL COMMENT '关联录像计划ID(手动录制时为空)',
  `device_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '设备国标编号',
  `channel_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '通道国标编号',
  `stream_identification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '流标识',
  `app` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '应用名',
  `media_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '流媒体服务器标识',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件名',
  `file_id` bigint DEFAULT NULL COMMENT '文件ID(关联base服务File表)',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT '文件大小(字节)',
  `file_format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'mp4' COMMENT '文件格式(mp4/flv/ts)',
  `duration` int NOT NULL DEFAULT '0' COMMENT '时长(秒)',
  `start_time` datetime DEFAULT NULL COMMENT '录像开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '录像结束时间',
  `thumbnail_file_id` bigint DEFAULT NULL COMMENT '缩略图文件ID(关联base服务File表)',
  `file_status` tinyint NOT NULL DEFAULT '0' COMMENT '文件状态(0=录制中/1=已完成/2=已过期/3=已删除)',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数(JSON)',
  `created_org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_plan_id` (`plan_id`) USING BTREE,
  KEY `idx_device_channel` (`device_identification`,`channel_identification`) USING BTREE,
  KEY `idx_start_time` (`start_time`) USING BTREE,
  KEY `idx_end_time` (`end_time`) USING BTREE,
  KEY `idx_file_id` (`file_id`) USING BTREE,
  KEY `idx_file_status` (`file_status`) USING BTREE,
  KEY `idx_media_identification` (`media_identification`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='视频录像文件表';

-- ----------------------------
-- Records of video_record_file
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_record_plan
-- ----------------------------
DROP TABLE IF EXISTS `video_record_plan`;
CREATE TABLE `video_record_plan` (
  `id` bigint NOT NULL COMMENT '主键',
  `plan_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '计划名称',
  `plan_type` tinyint NOT NULL DEFAULT '0' COMMENT '计划类型(0=设备录像/1=云端录像)',
  `media_identification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '指定流媒体服务器标识(空则自动分配)',
  `record_format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'mp4' COMMENT '录像格式(mp4/flv/ts)',
  `segment_duration` int NOT NULL DEFAULT '3600' COMMENT '分段时长(秒，默认1小时)',
  `retention_days` int NOT NULL DEFAULT '30' COMMENT '保留天数(超期自动清理)',
  `storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '存储路径(空则用默认路径)',
  `plan_status` tinyint NOT NULL DEFAULT '0' COMMENT '计划状态(0=停用/1=启用)',
  `schedule_rule` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '调度规则(JSON，支持周期/CRON/一次性)',
  `extend_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '扩展参数(JSON)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `created_org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_plan_status` (`plan_status`) USING BTREE,
  KEY `idx_plan_type` (`plan_type`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='视频录像计划表';

-- ----------------------------
-- Records of video_record_plan
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for video_sip_config
-- ----------------------------
DROP TABLE IF EXISTS `video_sip_config`;
CREATE TABLE `video_sip_config` (
  `id` bigint NOT NULL COMMENT '主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `sip_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '国标编号(SIP ID)',
  `sip_domain` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'SIP域',
  `sip_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'SIP认证密码(AES加密)',
  `sip_server_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'SIP接入地址(域名或IP)',
  `bind_ip` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '绑定IP(多网卡隔离,逗号分隔)',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认(1=是)',
  `register_interval` int DEFAULT NULL COMMENT '注册有效期(秒)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用/1=启用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_org_id` bigint DEFAULT NULL COMMENT '创建组织ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sip_id` (`sip_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_created_org_id` (`created_org_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='租户SIP服务配置';

-- ----------------------------
-- Records of video_sip_config
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
  `pull_retry_count` int DEFAULT '0' COMMENT '拉流重试次数',
  `max_retry_count` int DEFAULT '3' COMMENT '最大重试次数',
  `last_pull_time` datetime DEFAULT NULL COMMENT '最近拉流时间',
  `last_error` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最近错误信息',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
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
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0=正常/1=删除)',
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

-- 基线完整性：默认库基线应先导入；结果必须为 0。
SELECT COUNT(*) AS `base_role_resource_orphan_count_should_be_0`
FROM `base_role_resource_rel` AS `role_resource`
LEFT JOIN `thinglinks_ds_c_defaults`.`def_resource` AS `resource`
  ON `resource`.`id` = `role_resource`.`resource_id`
 AND `resource`.`application_id` = `role_resource`.`application_id`
LEFT JOIN `base_role` AS `role`
  ON `role`.`id` = `role_resource`.`role_id`
WHERE `resource`.`id` IS NULL
   OR `role`.`id` IS NULL;
