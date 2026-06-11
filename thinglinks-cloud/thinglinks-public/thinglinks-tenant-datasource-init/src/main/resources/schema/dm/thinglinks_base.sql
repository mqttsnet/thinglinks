CREATE TABLE "base_dict"
(
    "id" BIGINT NOT NULL,
    "parent_id" BIGINT,
    "parent_key" VARCHAR(255),
    "key_" VARCHAR(255),
    "classify" VARCHAR(2 CHAR) DEFAULT '20',
"name" VARCHAR(255),
"state" BIT DEFAULT 1,
"remark" VARCHAR(255),
"sort_value" INT DEFAULT 1,
"icon" VARCHAR(255),
"css_style" VARCHAR(255),
"css_class" VARCHAR(255),
"created_by" BIGINT,
"created_time" TIMESTAMP(0),
"updated_by" BIGINT,
"updated_time" TIMESTAMP(0),
"created_org_id" BIGINT,
CONSTRAINT "base_dict_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_dict" IS '字典';
COMMENT ON COLUMN "base_dict"."classify" IS '分类;[10-系统字典 20-业务字典]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Tenant.DICT_CLASSIFY)';
COMMENT ON COLUMN "base_dict"."created_by" IS '创建人';
COMMENT ON COLUMN "base_dict"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_dict"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_dict"."css_class" IS 'css类元素';
COMMENT ON COLUMN "base_dict"."css_style" IS 'css样式';
COMMENT ON COLUMN "base_dict"."icon" IS '图标';
COMMENT ON COLUMN "base_dict"."id" IS 'ID';
COMMENT ON COLUMN "base_dict"."key_" IS '标识';
COMMENT ON COLUMN "base_dict"."name" IS '名称';
COMMENT ON COLUMN "base_dict"."parent_id" IS '所属字典';
COMMENT ON COLUMN "base_dict"."parent_key" IS '所属字典标识';
COMMENT ON COLUMN "base_dict"."remark" IS '备注';
COMMENT ON COLUMN "base_dict"."sort_value" IS '排序';
COMMENT ON COLUMN "base_dict"."state" IS '状态';
COMMENT ON COLUMN "base_dict"."updated_by" IS '更新人';
COMMENT ON COLUMN "base_dict"."updated_time" IS '更新时间';


CREATE TABLE "base_employee"
(
    "id" BIGINT NOT NULL,
    "is_default" BIT DEFAULT 0,
    "position_id" BIGINT,
    "user_id" BIGINT,
    "last_company_id" BIGINT,
    "last_dept_id" BIGINT,
    "real_name" VARCHAR(255),
    "active_status" VARCHAR(2 CHAR) DEFAULT '20',
"position_status" VARCHAR(2 CHAR) DEFAULT '10',
"state" BIT DEFAULT 1,
"created_by" BIGINT,
"created_time" TIMESTAMP(0),
"updated_by" BIGINT,
"updated_time" TIMESTAMP(0),
"created_org_id" BIGINT,
CONSTRAINT "base_employee_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
CONSTRAINT "base_employee_uk_emp_user_id" UNIQUE("user_id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_employee" IS '员工';
COMMENT ON COLUMN "base_employee"."active_status" IS '激活状态;[10-未激活 20-已激活]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Global.ACTIVE_STATUS)';
COMMENT ON COLUMN "base_employee"."created_by" IS '创建人';
COMMENT ON COLUMN "base_employee"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_employee"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_employee"."id" IS 'ID';
COMMENT ON COLUMN "base_employee"."is_default" IS '是否默认员工;[0-否 1-是]';
COMMENT ON COLUMN "base_employee"."last_company_id" IS '上次登录公司ID';
COMMENT ON COLUMN "base_employee"."last_dept_id" IS '上次登录部门ID';
COMMENT ON COLUMN "base_employee"."position_id" IS '所属岗位';
COMMENT ON COLUMN "base_employee"."position_status" IS '职位状态;[10-在职 20-离职]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.POSITION_STATUS)';
COMMENT ON COLUMN "base_employee"."real_name" IS '真实姓名';
COMMENT ON COLUMN "base_employee"."state" IS '状态;[0-禁用 1-启用]';
COMMENT ON COLUMN "base_employee"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "base_employee"."updated_time" IS '最后更新时间';
COMMENT ON COLUMN "base_employee"."user_id" IS '用户';


CREATE TABLE "base_employee_org_rel"
(
    "id" BIGINT NOT NULL,
    "org_id" BIGINT,
    "employee_id" BIGINT,
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "created_org_id" BIGINT,
    CONSTRAINT "base_employee_org_rel_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "base_employee_org_rel_uk_employee_org" UNIQUE("org_id", "employee_id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_employee_org_rel" IS '员工所在部门';
COMMENT ON COLUMN "base_employee_org_rel"."created_by" IS '创建人';
COMMENT ON COLUMN "base_employee_org_rel"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_employee_org_rel"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_employee_org_rel"."employee_id" IS '关联员工';
COMMENT ON COLUMN "base_employee_org_rel"."id" IS 'ID';
COMMENT ON COLUMN "base_employee_org_rel"."org_id" IS '关联机构';
COMMENT ON COLUMN "base_employee_org_rel"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "base_employee_org_rel"."updated_time" IS '最后更新时间';


CREATE TABLE "base_employee_role_rel"
(
    "id" BIGINT NOT NULL,
    "role_id" BIGINT,
    "employee_id" BIGINT,
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "created_org_id" BIGINT,
    CONSTRAINT "base_employee_role_rel_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "base_employee_role_rel_uk_err_role_employee" UNIQUE("role_id", "employee_id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_employee_role_rel" IS '员工的角色';
COMMENT ON COLUMN "base_employee_role_rel"."created_by" IS '创建人';
COMMENT ON COLUMN "base_employee_role_rel"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_employee_role_rel"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_employee_role_rel"."employee_id" IS '所属员工;#base_employee';
COMMENT ON COLUMN "base_employee_role_rel"."id" IS 'ID';
COMMENT ON COLUMN "base_employee_role_rel"."role_id" IS '拥有角色;#base_role';
COMMENT ON COLUMN "base_employee_role_rel"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "base_employee_role_rel"."updated_time" IS '最后更新时间';


CREATE TABLE "base_operation_log"
(
    "id" BIGINT NOT NULL,
    "request_ip" VARCHAR(50),
    "type" VARCHAR(5) DEFAULT 'OPT',
    "user_name" VARCHAR(50),
    "description" VARCHAR(255),
    "class_path" VARCHAR(255),
    "action_method" VARCHAR(50),
    "request_uri" VARCHAR(500),
    "http_method" VARCHAR(10) DEFAULT 'GET',
    "start_time" TIMESTAMP(0),
    "finish_time" TIMESTAMP(0),
    "consuming_time" BIGINT DEFAULT 0,
    "ua" VARCHAR(500),
    "created_time" TIMESTAMP(0),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "base_operation_log_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_operation_log" IS '操作日志';
COMMENT ON COLUMN "base_operation_log"."action_method" IS '请求方法';
COMMENT ON COLUMN "base_operation_log"."class_path" IS '类路径';
COMMENT ON COLUMN "base_operation_log"."consuming_time" IS '消耗时间';
COMMENT ON COLUMN "base_operation_log"."created_by" IS '创建人';
COMMENT ON COLUMN "base_operation_log"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_operation_log"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_operation_log"."description" IS '操作描述';
COMMENT ON COLUMN "base_operation_log"."finish_time" IS '完成时间';
COMMENT ON COLUMN "base_operation_log"."http_method" IS '请求类型;#HttpMethod{GET:GET请求;POST:POST请求;PUT:PUT请求;DELETE:DELETE请求;PATCH:PATCH请求;TRACE:TRACE请求;HEAD:HEAD请求;OPTIONS:OPTIONS请求;}';
COMMENT ON COLUMN "base_operation_log"."id" IS '主键';
COMMENT ON COLUMN "base_operation_log"."request_ip" IS '操作IP';
COMMENT ON COLUMN "base_operation_log"."request_uri" IS '请求地址';
COMMENT ON COLUMN "base_operation_log"."start_time" IS '开始时间';
COMMENT ON COLUMN "base_operation_log"."type" IS '日志类型;#LogType{OPT:操作类型;EX:异常类型}';
COMMENT ON COLUMN "base_operation_log"."ua" IS '浏览器';
COMMENT ON COLUMN "base_operation_log"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "base_operation_log"."updated_time" IS '最后更新时间';
COMMENT ON COLUMN "base_operation_log"."user_name" IS '操作人';


CREATE TABLE "base_operation_log_ext"
(
    "id" BIGINT NOT NULL,
    "params" CLOB,
    "result" CLOB,
    "ex_detail" CLOB,
    "created_time" TIMESTAMP(0),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "base_operation_log_ext_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_operation_log_ext" IS '操作日志扩展';
COMMENT ON COLUMN "base_operation_log_ext"."created_by" IS '创建人';
COMMENT ON COLUMN "base_operation_log_ext"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_operation_log_ext"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_operation_log_ext"."ex_detail" IS '异常描述';
COMMENT ON COLUMN "base_operation_log_ext"."id" IS '主键';
COMMENT ON COLUMN "base_operation_log_ext"."params" IS '请求参数';
COMMENT ON COLUMN "base_operation_log_ext"."result" IS '返回值';
COMMENT ON COLUMN "base_operation_log_ext"."updated_by" IS '最后更新人ID';
COMMENT ON COLUMN "base_operation_log_ext"."updated_time" IS '最后更新时间';


CREATE TABLE "base_org"
(
    "id" BIGINT NOT NULL,
    "name" VARCHAR(255),
    "type_" VARCHAR(2 CHAR) DEFAULT '10',
"short_name" VARCHAR(255),
"parent_id" BIGINT,
"tree_grade" INT DEFAULT 0,
"tree_path" VARCHAR(255),
"sort_value" INT DEFAULT 1,
"state" BIT DEFAULT 1,
"remarks" VARCHAR(255),
"created_time" TIMESTAMP(0),
"created_by" BIGINT,
"updated_time" TIMESTAMP(0),
"updated_by" BIGINT,
"created_org_id" BIGINT,
CONSTRAINT "base_org_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
CONSTRAINT "base_org_uk_org_name" UNIQUE("name")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_org" IS '组织';
COMMENT ON COLUMN "base_org"."created_by" IS '创建人';
COMMENT ON COLUMN "base_org"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_org"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_org"."id" IS 'ID';
COMMENT ON COLUMN "base_org"."name" IS '名称';
COMMENT ON COLUMN "base_org"."parent_id" IS '父组织';
COMMENT ON COLUMN "base_org"."remarks" IS '备注';
COMMENT ON COLUMN "base_org"."short_name" IS '简称';
COMMENT ON COLUMN "base_org"."sort_value" IS '排序';
COMMENT ON COLUMN "base_org"."state" IS '状态;[0-禁用 1-启用]';
COMMENT ON COLUMN "base_org"."tree_grade" IS '树层级';
COMMENT ON COLUMN "base_org"."tree_path" IS '树路径;用id拼接树结构';
COMMENT ON COLUMN "base_org"."type_" IS '类型;[10-单位 20-部门]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.ORG_TYPE)';
COMMENT ON COLUMN "base_org"."updated_by" IS '修改人';
COMMENT ON COLUMN "base_org"."updated_time" IS '修改时间';


CREATE TABLE "base_org_role_rel"
(
    "id" BIGINT NOT NULL,
    "org_id" BIGINT,
    "role_id" BIGINT,
    "created_time" TIMESTAMP(0),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "base_org_role_rel_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "base_org_role_rel_uk_org_role" UNIQUE("org_id", "role_id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_org_role_rel" IS '组织的角色';
COMMENT ON COLUMN "base_org_role_rel"."created_by" IS '创建人';
COMMENT ON COLUMN "base_org_role_rel"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_org_role_rel"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_org_role_rel"."id" IS 'ID';
COMMENT ON COLUMN "base_org_role_rel"."org_id" IS '所属部门;#base_org';
COMMENT ON COLUMN "base_org_role_rel"."role_id" IS '拥有角色;#base_role';
COMMENT ON COLUMN "base_org_role_rel"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "base_org_role_rel"."updated_time" IS '最后更新时间';


CREATE TABLE "base_parameter"
(
    "id" BIGINT NOT NULL,
    "key_" VARCHAR(255),
    "value" VARCHAR(255),
    "name" VARCHAR(255),
    "remarks" VARCHAR(255),
    "state" BIT DEFAULT 1,
    "param_type" VARCHAR(2 CHAR) DEFAULT '20',
"created_by" BIGINT,
"created_time" TIMESTAMP(0),
"updated_by" BIGINT,
"updated_time" TIMESTAMP(0),
"created_org_id" BIGINT,
CONSTRAINT "base_parameter_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_parameter" IS '个性参数';
COMMENT ON COLUMN "base_parameter"."created_by" IS '创建人id';
COMMENT ON COLUMN "base_parameter"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_parameter"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_parameter"."id" IS 'ID';
COMMENT ON COLUMN "base_parameter"."key_" IS '参数键';
COMMENT ON COLUMN "base_parameter"."name" IS '参数名称';
COMMENT ON COLUMN "base_parameter"."param_type" IS '类型;[10-系统参数 20-业务参数]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Tenant.PARAMETER_TYPE)';
COMMENT ON COLUMN "base_parameter"."remarks" IS '备注';
COMMENT ON COLUMN "base_parameter"."state" IS '状态';
COMMENT ON COLUMN "base_parameter"."updated_by" IS '更新人id';
COMMENT ON COLUMN "base_parameter"."updated_time" IS '更新时间';
COMMENT ON COLUMN "base_parameter"."value" IS '参数值';


CREATE TABLE "base_position"
(
    "id" BIGINT NOT NULL,
    "name" VARCHAR(255),
    "org_id" BIGINT,
    "state" BIT DEFAULT 1,
    "remarks" VARCHAR(255),
    "created_time" TIMESTAMP(0),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "base_position_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "base_position_uk_name" UNIQUE("name")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_position" IS '岗位';
COMMENT ON COLUMN "base_position"."created_by" IS '创建人';
COMMENT ON COLUMN "base_position"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_position"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_position"."id" IS 'ID';
COMMENT ON COLUMN "base_position"."name" IS '名称';
COMMENT ON COLUMN "base_position"."org_id" IS '所属组织;#base_org@Echo(api = EchoApi.ORG_ID_CLASS)';
COMMENT ON COLUMN "base_position"."remarks" IS '备注';
COMMENT ON COLUMN "base_position"."state" IS '状态;0-禁用 1-启用';
COMMENT ON COLUMN "base_position"."updated_by" IS '修改人';
COMMENT ON COLUMN "base_position"."updated_time" IS '修改时间';


CREATE TABLE "base_role"
(
    "id" BIGINT NOT NULL,
    "category" VARCHAR(2 CHAR) DEFAULT '10',
"type_" VARCHAR(2 CHAR) DEFAULT '20',
"name" VARCHAR(50),
"code" VARCHAR(20),
"remarks" VARCHAR(255),
"state" BIT DEFAULT 1,
"readonly_" BIT DEFAULT 0,
"created_by" BIGINT,
"created_time" TIMESTAMP(0),
"updated_by" BIGINT,
"updated_time" TIMESTAMP(0),
"created_org_id" BIGINT,
CONSTRAINT "base_role_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
CONSTRAINT "base_role_uk_code" UNIQUE("code")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_role" IS '角色';
COMMENT ON COLUMN "base_role"."category" IS '角色类别;[10-功能角色 20-桌面角色 30-数据角色]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.ROLE_CATEGORY)';
COMMENT ON COLUMN "base_role"."code" IS '编码';
COMMENT ON COLUMN "base_role"."created_by" IS '创建人';
COMMENT ON COLUMN "base_role"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_role"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_role"."id" IS 'ID';
COMMENT ON COLUMN "base_role"."name" IS '名称';
COMMENT ON COLUMN "base_role"."readonly_" IS '内置角色';
COMMENT ON COLUMN "base_role"."remarks" IS '备注';
COMMENT ON COLUMN "base_role"."state" IS '状态';
COMMENT ON COLUMN "base_role"."type_" IS '角色类型;[10-系统角色 20-自定义角色]; 
@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Global.DATA_TYPE)';
COMMENT ON COLUMN "base_role"."updated_by" IS '更新人';
COMMENT ON COLUMN "base_role"."updated_time" IS '更新时间';


CREATE TABLE "base_role_resource_rel"
(
    "id" BIGINT NOT NULL,
    "resource_id" BIGINT,
    "application_id" BIGINT,
    "role_id" BIGINT,
    "created_time" TIMESTAMP(0),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "base_role_resource_rel_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "base_role_resource_rel_uk_role_resource" UNIQUE("resource_id", "role_id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "base_role_resource_rel" IS '角色的资源';
COMMENT ON COLUMN "base_role_resource_rel"."application_id" IS '所属应用;#def_application';
COMMENT ON COLUMN "base_role_resource_rel"."created_by" IS '创建人';
COMMENT ON COLUMN "base_role_resource_rel"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "base_role_resource_rel"."created_time" IS '创建时间';
COMMENT ON COLUMN "base_role_resource_rel"."id" IS '主键';
COMMENT ON COLUMN "base_role_resource_rel"."resource_id" IS '拥有资源;#def_resource';
COMMENT ON COLUMN "base_role_resource_rel"."role_id" IS '所属角色;#base_role';
COMMENT ON COLUMN "base_role_resource_rel"."updated_by" IS '最后更新人';
COMMENT ON COLUMN "base_role_resource_rel"."updated_time" IS '最后更新时间';


CREATE TABLE "card_channel_info"
(
    "id" BIGINT NOT NULL,
    "channel_name" VARCHAR(50),
    "key_parameter" VARCHAR(1000),
    "official_flag" TINYINT DEFAULT 0,
    "refresh_flag" TINYINT DEFAULT 0,
    "operator_type" TINYINT DEFAULT 1,
    "province_name" VARCHAR(100),
    "province_code" VARCHAR(100),
    "appKey" VARCHAR(64),
    "secret" VARCHAR(64),
    "code" VARCHAR(100),
    "app_id" VARCHAR(64),
    "password" VARCHAR(64),
    "status" TINYINT DEFAULT 0,
    "channel_type" TINYINT,
    "extend_params" CLOB,
    "remark" VARCHAR(250),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "created_org_id" BIGINT,
    CONSTRAINT "card_channel_info_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "card_channel_info" IS '物联卡渠道表';
COMMENT ON COLUMN "card_channel_info"."app_id" IS '客户appid';
COMMENT ON COLUMN "card_channel_info"."appKey" IS '公共应用键';
COMMENT ON COLUMN "card_channel_info"."channel_name" IS '渠道商名称（如:中国移动）';
COMMENT ON COLUMN "card_channel_info"."channel_type" IS '渠道类别(如:山东移动)';
COMMENT ON COLUMN "card_channel_info"."code" IS '公共code';
COMMENT ON COLUMN "card_channel_info"."created_by" IS '创建人';
COMMENT ON COLUMN "card_channel_info"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "card_channel_info"."created_time" IS '创建时间';
COMMENT ON COLUMN "card_channel_info"."extend_params" IS '扩展参数';
COMMENT ON COLUMN "card_channel_info"."id" IS '渠道id';
COMMENT ON COLUMN "card_channel_info"."key_parameter" IS '密钥集合';
COMMENT ON COLUMN "card_channel_info"."official_flag" IS '是否直属官方平台(如直接对接是移动onelink  0不是  1是)';
COMMENT ON COLUMN "card_channel_info"."operator_type" IS '所属运营商(1移动、2电信 、3联通）';
COMMENT ON COLUMN "card_channel_info"."password" IS '密匙';
COMMENT ON COLUMN "card_channel_info"."province_code" IS '省份编码';
COMMENT ON COLUMN "card_channel_info"."province_name" IS '省份名称';
COMMENT ON COLUMN "card_channel_info"."refresh_flag" IS 'token是否需要重复刷新 true是 false否 默认是: false';
COMMENT ON COLUMN "card_channel_info"."remark" IS '备注';
COMMENT ON COLUMN "card_channel_info"."secret" IS '公共密钥';
COMMENT ON COLUMN "card_channel_info"."status" IS '渠道状态(0启用、1停用)';
COMMENT ON COLUMN "card_channel_info"."updated_by" IS '更新人';
COMMENT ON COLUMN "card_channel_info"."updated_time" IS '更新时间';


CREATE TABLE "card_channel_info_config"
(
    "id" BIGINT NOT NULL,
    "channel_id" BIGINT,
    "request_type_code" VARCHAR(50),
    "url" VARCHAR(255),
    "extend_params" CLOB,
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "card_channel_info_config_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "card_channel_info_config" IS '物联卡渠道信息配置表';
COMMENT ON COLUMN "card_channel_info_config"."channel_id" IS '渠道id';
COMMENT ON COLUMN "card_channel_info_config"."created_by" IS '创建人';
COMMENT ON COLUMN "card_channel_info_config"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "card_channel_info_config"."created_time" IS '创建时间';
COMMENT ON COLUMN "card_channel_info_config"."extend_params" IS '扩展参数';
COMMENT ON COLUMN "card_channel_info_config"."remark" IS '备注';
COMMENT ON COLUMN "card_channel_info_config"."request_type_code" IS '请求类型编码';
COMMENT ON COLUMN "card_channel_info_config"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "card_channel_info_config"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "card_channel_info_config"."url" IS '供应商接口地址';


CREATE TABLE "card_sim_device"
(
    "id" BIGINT NOT NULL,
    "card_id" BIGINT,
    "device_identification" VARCHAR(255),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "card_sim_device_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "card_sim_device" IS '物联网卡与设备关系表';
COMMENT ON COLUMN "card_sim_device"."card_id" IS '物联卡ID';
COMMENT ON COLUMN "card_sim_device"."created_by" IS '创建人';
COMMENT ON COLUMN "card_sim_device"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "card_sim_device"."created_time" IS '创建时间';
COMMENT ON COLUMN "card_sim_device"."device_identification" IS '设备标识';
COMMENT ON COLUMN "card_sim_device"."id" IS '主键';
COMMENT ON COLUMN "card_sim_device"."remark" IS '备注';
COMMENT ON COLUMN "card_sim_device"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "card_sim_device"."updated_time" IS '最后修改时间';


CREATE OR REPLACE  INDEX "card_sim_device_idx_card_id" ON "card_sim_device"("card_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "card_sim_device_idx_device_id" ON "card_sim_device"("device_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "card_sim_info"
(
    "id" BIGINT NOT NULL,
    "imsi" VARCHAR(50),
    "iccid" VARCHAR(50),
    "card_number" VARCHAR(50),
    "card_type" TINYINT,
    "channel_id" BIGINT,
    "flows_used" DECIMAL(17,2) DEFAULT 0.,
    "flows_total" DECIMAL(17,2) DEFAULT 0.,
    "flows_rest" DECIMAL(17,2) DEFAULT 0.,
    "virtual_flows_used" DECIMAL(17,2) DEFAULT 0.,
    "virtual_flows_rest" DECIMAL(17,2) DEFAULT 0.,
    "virtual_flows_total" DECIMAL(17,2) DEFAULT 0.,
    "sms_flag" TINYINT DEFAULT 0,
    "gprs_flag" TINYINT DEFAULT 0,
    "open_time" TIMESTAMP(0),
    "last_open_time" TIMESTAMP(0),
    "start_time" TIMESTAMP(0),
    "end_time" TIMESTAMP(0),
    "flows_end_time" TIMESTAMP(0),
    "carrier_type" TINYINT DEFAULT 1,
    "sms_count" TINYINT DEFAULT 0,
    "status" TINYINT DEFAULT 1,
    "use_type" TINYINT DEFAULT 1,
    "apn_name" VARCHAR(100) DEFAULT 'CMIOT',
    "ip_address" VARCHAR(255),
    "gain_time" TIMESTAMP(0),
    "online_flag" TINYINT,
    "stop_card_type" TINYINT DEFAULT 0,
    "monthly_warning" VARCHAR(32),
    "imei" VARCHAR(32),
    "limit_flow" NUMBER(22,0) DEFAULT 0.,
    "limit_flag" TINYINT DEFAULT 0,
    "limit_status" TINYINT DEFAULT 0,
    "offer_id" BIGINT,
    "searchable_status" TINYINT DEFAULT 1,
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "card_sim_info_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "card_sim_info" IS '物联网卡信息表';
COMMENT ON COLUMN "card_sim_info"."apn_name" IS 'APN名称';
COMMENT ON COLUMN "card_sim_info"."card_number" IS '卡号';
COMMENT ON COLUMN "card_sim_info"."card_type" IS '卡类型 0 插拔卡 1 贴片IC卡';
COMMENT ON COLUMN "card_sim_info"."carrier_type" IS '运营商类型 1 移动 2 电信 3 联通';
COMMENT ON COLUMN "card_sim_info"."channel_id" IS '渠道ID';
COMMENT ON COLUMN "card_sim_info"."created_by" IS '创建人';
COMMENT ON COLUMN "card_sim_info"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "card_sim_info"."created_time" IS '创建时间';
COMMENT ON COLUMN "card_sim_info"."end_time" IS '失效时间';
COMMENT ON COLUMN "card_sim_info"."flows_end_time" IS '流量到期时间';
COMMENT ON COLUMN "card_sim_info"."flows_rest" IS '本月剩余流量';
COMMENT ON COLUMN "card_sim_info"."flows_total" IS '总流量';
COMMENT ON COLUMN "card_sim_info"."flows_used" IS '已用流量';
COMMENT ON COLUMN "card_sim_info"."gain_time" IS '获取时间';
COMMENT ON COLUMN "card_sim_info"."gprs_flag" IS 'GPRS开关 0 关 1 开';
COMMENT ON COLUMN "card_sim_info"."iccid" IS 'SIM卡唯一识别码';
COMMENT ON COLUMN "card_sim_info"."id" IS '主键';
COMMENT ON COLUMN "card_sim_info"."imei" IS '关联设备IMEI';
COMMENT ON COLUMN "card_sim_info"."imsi" IS '国际移动用户识别码';
COMMENT ON COLUMN "card_sim_info"."ip_address" IS 'IP地址';
COMMENT ON COLUMN "card_sim_info"."last_open_time" IS '最晚激活时间';
COMMENT ON COLUMN "card_sim_info"."limit_flag" IS '流量阀值状态 0 未开启 1 开启';
COMMENT ON COLUMN "card_sim_info"."limit_flow" IS '流量限制阀值';
COMMENT ON COLUMN "card_sim_info"."limit_status" IS '流量限制状态 0 未限制 1 已限制';
COMMENT ON COLUMN "card_sim_info"."monthly_warning" IS '当月流量预警记录';
COMMENT ON COLUMN "card_sim_info"."offer_id" IS '事务ID';
COMMENT ON COLUMN "card_sim_info"."online_flag" IS '在线状态 0 不在线 1 在线';
COMMENT ON COLUMN "card_sim_info"."open_time" IS '开卡时间';
COMMENT ON COLUMN "card_sim_info"."remark" IS '备注';
COMMENT ON COLUMN "card_sim_info"."searchable_status" IS 'API是否可查 0 不可查 1 可查';
COMMENT ON COLUMN "card_sim_info"."sms_count" IS '已发送短信数量';
COMMENT ON COLUMN "card_sim_info"."sms_flag" IS '是否有短信 0 无 1 有';
COMMENT ON COLUMN "card_sim_info"."start_time" IS '激活时间';
COMMENT ON COLUMN "card_sim_info"."status" IS 'SIM卡状态 1 待激活 2 已激活 3 停机';
COMMENT ON COLUMN "card_sim_info"."stop_card_type" IS '停卡类型 1 系统停卡 2 人工停卡 0 正常';
COMMENT ON COLUMN "card_sim_info"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "card_sim_info"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "card_sim_info"."use_type" IS '使用类型 1 普卡 2 共享池 3 流量池';
COMMENT ON COLUMN "card_sim_info"."virtual_flows_rest" IS '虚拟剩余流量';
COMMENT ON COLUMN "card_sim_info"."virtual_flows_total" IS '虚拟总流量';
COMMENT ON COLUMN "card_sim_info"."virtual_flows_used" IS '虚拟已用流量';


CREATE OR REPLACE  INDEX "card_sim_info_idx_channel_id" ON "card_sim_info"("channel_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "card_sim_info_idx_card_type" ON "card_sim_info"("card_type" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "card_sim_info_idx_status" ON "card_sim_info"("status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "card_sim_info_idx_card_number" ON "card_sim_info"("card_number" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "card_trigger_log"
(
    "id" BIGINT NOT NULL,
    "msisdn" VARCHAR(50),
    "trigger_type" SMALLINT,
    "trigger_cause" VARCHAR(255),
    "trigger_mode" SMALLINT,
    "status" SMALLINT DEFAULT 0,
    "trigger_time" TIMESTAMP(0),
    "value" VARCHAR(50),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "card_trigger_log_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "card_trigger_log" IS '物联卡异常触发表';
COMMENT ON COLUMN "card_trigger_log"."created_by" IS '创建人';
COMMENT ON COLUMN "card_trigger_log"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "card_trigger_log"."created_time" IS '创建时间';
COMMENT ON COLUMN "card_trigger_log"."msisdn" IS '卡号';
COMMENT ON COLUMN "card_trigger_log"."remark" IS '备注';
COMMENT ON COLUMN "card_trigger_log"."status" IS '执行状态: 0处理中 1 失败  2 成功';
COMMENT ON COLUMN "card_trigger_log"."trigger_cause" IS '触发原因';
COMMENT ON COLUMN "card_trigger_log"."trigger_mode" IS '触发类型 1: 系统触发  2: 人工触发';
COMMENT ON COLUMN "card_trigger_log"."trigger_time" IS '触发时间';
COMMENT ON COLUMN "card_trigger_log"."trigger_type" IS '触发类型 1: 卡号关停 2 数据服务关停, 3预警记录';
COMMENT ON COLUMN "card_trigger_log"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "card_trigger_log"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "card_trigger_log"."value" IS '变量值';


CREATE TABLE "card_warning"
(
    "id" BIGINT NOT NULL,
    "warning_name" VARCHAR(50),
    "warning_threshold" SMALLINT,
    "alarm_id" BIGINT,
    "warning_type" SMALLINT,
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "card_warning_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "card_warning" IS '物联卡告警表';
COMMENT ON COLUMN "card_warning"."alarm_id" IS '告警渠道';
COMMENT ON COLUMN "card_warning"."created_by" IS '创建人';
COMMENT ON COLUMN "card_warning"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "card_warning"."created_time" IS '创建时间';
COMMENT ON COLUMN "card_warning"."id" IS 'id';
COMMENT ON COLUMN "card_warning"."remark" IS '备注';
COMMENT ON COLUMN "card_warning"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "card_warning"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "card_warning"."warning_name" IS '预警名称';
COMMENT ON COLUMN "card_warning"."warning_threshold" IS '预警阈值';
COMMENT ON COLUMN "card_warning"."warning_type" IS '1 卡级 2流量池';


CREATE TABLE "device"
(
    "id" DECIMAL NOT NULL,
    "client_id" VARCHAR(255),
    "user_name" VARCHAR(255),
    "password" VARCHAR(255),
    "app_id" VARCHAR(64),
    "auth_mode" TINYINT DEFAULT 0,
    "encrypt_key" VARCHAR(255),
    "encrypt_vector" VARCHAR(255),
    "sign_key" VARCHAR(255),
    "encrypt_method" TINYINT DEFAULT 0,
    "device_identification" VARCHAR(255),
    "device_name" VARCHAR(255),
    "connector" VARCHAR(255),
    "description" VARCHAR(255),
    "device_status" TINYINT DEFAULT 0,
    "connect_status" TINYINT DEFAULT 0,
    "last_status_event_hlc" BIGINT NOT NULL DEFAULT 0,
    "device_tags" VARCHAR(255),
    "product_identification" VARCHAR(100),
    "bound_product_version_no" VARCHAR(64) DEFAULT '',
    "sw_version" VARCHAR(255),
    "fw_version" VARCHAR(255),
    "device_sdk_version" VARCHAR(255) DEFAULT 'v1',
    "gateway_id" VARCHAR(255),
    "node_type" TINYINT DEFAULT 0,
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "device_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "device_idx_client_id" UNIQUE("client_id"),
    CONSTRAINT "device_idx_device_identification" UNIQUE("device_identification")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "device" IS '设备档案信息表';
COMMENT ON COLUMN "device"."app_id" IS '应用ID';
COMMENT ON COLUMN "device"."bound_product_version_no" IS '设备绑定的产品版本序号(快照标识,数据上报路径的物模型解析依据,灰度发布时可与产品当前版本不同)';
COMMENT ON COLUMN "device"."auth_mode" IS '认证方式';
COMMENT ON COLUMN "device"."client_id" IS '客户端标识';
COMMENT ON COLUMN "device"."connect_status" IS '连接状态';
COMMENT ON COLUMN "device"."connector" IS '连接实例';
COMMENT ON COLUMN "device"."created_by" IS '创建人';
COMMENT ON COLUMN "device"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "device"."created_time" IS '创建时间';
COMMENT ON COLUMN "device"."description" IS '设备描述';
COMMENT ON COLUMN "device"."device_identification" IS '设备标识';
COMMENT ON COLUMN "device"."device_name" IS '设备名称';
COMMENT ON COLUMN "device"."device_sdk_version" IS 'sdk版本';
COMMENT ON COLUMN "device"."device_status" IS '设备状态';
COMMENT ON COLUMN "device"."device_tags" IS '设备标签';
COMMENT ON COLUMN "device"."encrypt_key" IS '加密秘钥';
COMMENT ON COLUMN "device"."encrypt_method" IS '协议加密方式';
COMMENT ON COLUMN "device"."encrypt_vector" IS '加密向量';
COMMENT ON COLUMN "device"."fw_version" IS '固件版本';
COMMENT ON COLUMN "device"."gateway_id" IS '网关设备id';
COMMENT ON COLUMN "device"."id" IS 'id';
COMMENT ON COLUMN "device"."last_status_event_hlc" IS '最新状态事件因果时钟(HLC,64-bit)';
COMMENT ON COLUMN "device"."node_type" IS '设备类型';
COMMENT ON COLUMN "device"."password" IS '密码';
COMMENT ON COLUMN "device"."product_identification" IS '产品标识';
COMMENT ON COLUMN "device"."remark" IS '备注';
COMMENT ON COLUMN "device"."sign_key" IS '签名密钥';
COMMENT ON COLUMN "device"."sw_version" IS '软件版本';
COMMENT ON COLUMN "device"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "device"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "device"."user_name" IS '用户名';


CREATE TABLE "device_action"
(
    "id" BIGINT NOT NULL,
    "device_identification" VARCHAR(255),
    "action_type" VARCHAR(255),
    "message" CLOB,
    "status" TINYINT DEFAULT 0,
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "device_action_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "device_action" IS '设备动作数据';
COMMENT ON COLUMN "device_action"."action_type" IS '动作类型';
COMMENT ON COLUMN "device_action"."created_by" IS '创建人';
COMMENT ON COLUMN "device_action"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "device_action"."created_time" IS '创建时间';
COMMENT ON COLUMN "device_action"."device_identification" IS '设备标识';
COMMENT ON COLUMN "device_action"."id" IS 'id';
COMMENT ON COLUMN "device_action"."message" IS '内容信息';
COMMENT ON COLUMN "device_action"."remark" IS '备注';
COMMENT ON COLUMN "device_action"."status" IS '状态(0:成功、1:失败)';
COMMENT ON COLUMN "device_action"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "device_action"."updated_time" IS '最后修改时间';


CREATE TABLE "device_command"
(
    "id" DECIMAL NOT NULL,
    "device_identification" VARCHAR(255),
    "command_identification" VARCHAR(255),
    "command_type" TINYINT DEFAULT 0,
    "status" TINYINT DEFAULT 0,
    "content" CLOB,
    "remark" CLOB,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "device_command_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "device_command" IS '设备命令下发及响应表';
COMMENT ON COLUMN "device_command"."command_identification" IS '命令标识';
COMMENT ON COLUMN "device_command"."command_type" IS '命令类型(0:命名下发、1:命令响应)';
COMMENT ON COLUMN "device_command"."content" IS '内容';
COMMENT ON COLUMN "device_command"."created_by" IS '创建人';
COMMENT ON COLUMN "device_command"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "device_command"."created_time" IS '创建时间';
COMMENT ON COLUMN "device_command"."device_identification" IS '设备标识';
COMMENT ON COLUMN "device_command"."id" IS 'id';
COMMENT ON COLUMN "device_command"."remark" IS '备注';
COMMENT ON COLUMN "device_command"."status" IS '状态';
COMMENT ON COLUMN "device_command"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "device_command"."updated_time" IS '最后修改时间';


CREATE TABLE "device_location"
(
    "id" BIGINT NOT NULL,
    "device_identification" VARCHAR(255),
    "latitude" DECIMAL(10,7),
    "longitude" DECIMAL(10,7),
    "full_name" VARCHAR(500),
    "province_code" VARCHAR(50),
    "city_code" VARCHAR(50),
    "region_code" VARCHAR(50),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "device_location_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "device_location" IS '设备位置表';
COMMENT ON COLUMN "device_location"."city_code" IS '市编码';
COMMENT ON COLUMN "device_location"."created_by" IS '创建人';
COMMENT ON COLUMN "device_location"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "device_location"."created_time" IS '创建时间';
COMMENT ON COLUMN "device_location"."device_identification" IS '设备标识';
COMMENT ON COLUMN "device_location"."full_name" IS '位置名称';
COMMENT ON COLUMN "device_location"."id" IS '主键';
COMMENT ON COLUMN "device_location"."latitude" IS '纬度';
COMMENT ON COLUMN "device_location"."longitude" IS '经度';
COMMENT ON COLUMN "device_location"."province_code" IS '省,直辖市编码';
COMMENT ON COLUMN "device_location"."region_code" IS '区县';
COMMENT ON COLUMN "device_location"."remark" IS '备注';
COMMENT ON COLUMN "device_location"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "device_location"."updated_time" IS '最后修改时间';


CREATE TABLE "extend_interface_log"
(
    "id" BIGINT NOT NULL,
    "interface_id" BIGINT,
    "name" VARCHAR(255),
    "success_count" INT DEFAULT 0,
    "fail_count" INT DEFAULT 0,
    "last_exec_time" TIMESTAMP(0),
    "created_time" TIMESTAMP(0),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    CONSTRAINT "extend_interface_log_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "extend_interface_log_UK_EIL_INTERFACE_ID" UNIQUE("interface_id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "extend_interface_log" IS '接口执行日志';
COMMENT ON COLUMN "extend_interface_log"."created_by" IS '创建人';
COMMENT ON COLUMN "extend_interface_log"."created_time" IS '创建时间';
COMMENT ON COLUMN "extend_interface_log"."fail_count" IS '失败次数';
COMMENT ON COLUMN "extend_interface_log"."interface_id" IS '接口ID;
#extend_interface';
COMMENT ON COLUMN "extend_interface_log"."last_exec_time" IS '最后执行时间';
COMMENT ON COLUMN "extend_interface_log"."name" IS '接口名称';
COMMENT ON COLUMN "extend_interface_log"."success_count" IS '成功次数';
COMMENT ON COLUMN "extend_interface_log"."updated_by" IS '修改人';
COMMENT ON COLUMN "extend_interface_log"."updated_time" IS '修改时间';


CREATE TABLE "extend_interface_logging"
(
    "id" BIGINT NOT NULL,
    "log_id" BIGINT,
    "exec_time" TIMESTAMP(0),
    "status" VARCHAR(2 CHAR) DEFAULT '01',
"params" CLOB,
"result" CLOB,
"error_msg" CLOB,
"biz_id" BIGINT,
"created_time" TIMESTAMP(0),
"created_by" BIGINT,
"updated_time" TIMESTAMP(0),
"updated_by" BIGINT,
CONSTRAINT "extend_interface_logging_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "extend_interface_logging" IS '接口执行日志记录';
COMMENT ON COLUMN "extend_interface_logging"."biz_id" IS '业务ID';
COMMENT ON COLUMN "extend_interface_logging"."created_by" IS '创建人';
COMMENT ON COLUMN "extend_interface_logging"."created_time" IS '创建时间';
COMMENT ON COLUMN "extend_interface_logging"."error_msg" IS '异常信息';
COMMENT ON COLUMN "extend_interface_logging"."exec_time" IS '执行时间';
COMMENT ON COLUMN "extend_interface_logging"."log_id" IS '接口日志ID;
#extend_interface_log';
COMMENT ON COLUMN "extend_interface_logging"."params" IS '请求参数';
COMMENT ON COLUMN "extend_interface_logging"."result" IS '接口返回';
COMMENT ON COLUMN "extend_interface_logging"."status" IS '执行状态;[01-初始化 02-成功 03-失败]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.MSG_INTERFACE_LOGGING_STATUS)';
COMMENT ON COLUMN "extend_interface_logging"."updated_by" IS '修改人';
COMMENT ON COLUMN "extend_interface_logging"."updated_time" IS '修改时间';


CREATE TABLE "extend_msg"
(
    "id" BIGINT NOT NULL,
    "template_code" VARCHAR(255),
    "type" VARCHAR(2 CHAR),
"status" VARCHAR(10),
"channel" VARCHAR(255),
"param" TEXT,
"config_list" TEXT,
"title" VARCHAR(255),
"content" CLOB,
"send_time" TIMESTAMP(0),
"biz_id" BIGINT,
"biz_type" VARCHAR(255),
"remind_mode" VARCHAR(2 CHAR),
"author" VARCHAR(255),
"created_by" BIGINT,
"created_time" TIMESTAMP(0),
"updated_by" BIGINT,
"updated_time" TIMESTAMP(0),
"created_org_id" BIGINT,
CONSTRAINT "extend_msg_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "extend_msg" IS '消息';
COMMENT ON COLUMN "extend_msg"."author" IS '发布人姓名';
COMMENT ON COLUMN "extend_msg"."biz_id" IS '业务ID';
COMMENT ON COLUMN "extend_msg"."biz_type" IS '业务类型';
COMMENT ON COLUMN "extend_msg"."channel" IS '发送渠道;
#SourceType{APP:应用;SERVICE:服务}';
COMMENT ON COLUMN "extend_msg"."config_list" IS '消息配置参数';
COMMENT ON COLUMN "extend_msg"."content" IS '发送内容';
COMMENT ON COLUMN "extend_msg"."created_by" IS '创建人ID';
COMMENT ON COLUMN "extend_msg"."created_org_id" IS '创建人所属机构';
COMMENT ON COLUMN "extend_msg"."created_time" IS '创建时间';
COMMENT ON COLUMN "extend_msg"."id" IS '短信记录ID';
COMMENT ON COLUMN "extend_msg"."param" IS '参数;需要封装为[{‘key’:‘‘,;’value’:‘‘}, {’key2’:‘‘, ’value2’:‘‘}]格式';
COMMENT ON COLUMN "extend_msg"."remind_mode" IS '提醒方式;@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_REMIND_MODE)[01-待办 02-预警 03-提醒]';
COMMENT ON COLUMN "extend_msg"."send_time" IS '发送时间';
COMMENT ON COLUMN "extend_msg"."status" IS '执行状态;
#TaskStatus{DRAFT:草稿;WAITING:等待执行;SUCCESS:执行成功;FAIL:执行失败}';
COMMENT ON COLUMN "extend_msg"."template_code" IS '消息模板;
#extend_msg_template';
COMMENT ON COLUMN "extend_msg"."title" IS '标题';
COMMENT ON COLUMN "extend_msg"."type" IS '消息类型;[01-短信 02-邮件 03-站内信];@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.MSG_TEMPLATE_TYPE)';
COMMENT ON COLUMN "extend_msg"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "extend_msg"."updated_time" IS '最后修改时间';


CREATE OR REPLACE  INDEX "extend_msg_tempate_id_topic_content" ON "extend_msg"("template_code" ASC,"title" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "extend_msg_recipient"
(
    "id" BIGINT NOT NULL,
    "msg_id" BIGINT,
    "recipient" VARCHAR(255),
    "ext" VARCHAR(255),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    CONSTRAINT "extend_msg_recipient_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "extend_msg_recipient" IS '消息接收人';
COMMENT ON COLUMN "extend_msg_recipient"."created_by" IS '创建人';
COMMENT ON COLUMN "extend_msg_recipient"."created_time" IS '创建时间';
COMMENT ON COLUMN "extend_msg_recipient"."ext" IS '扩展信息';
COMMENT ON COLUMN "extend_msg_recipient"."id" IS 'ID';
COMMENT ON COLUMN "extend_msg_recipient"."msg_id" IS '任务ID;
#extend_msg';
COMMENT ON COLUMN "extend_msg_recipient"."recipient" IS '接收人;
可能是手机号、邮箱、用户ID等';
COMMENT ON COLUMN "extend_msg_recipient"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "extend_msg_recipient"."updated_time" IS '最后修改时间';


CREATE OR REPLACE  INDEX "extend_msg_recipient_task_id_tel_num" ON "extend_msg_recipient"("msg_id" ASC,"recipient" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "extend_msg_template"
(
    "id" BIGINT NOT NULL,
    "interface_id" BIGINT,
    "type" VARCHAR(2 CHAR),
"code" VARCHAR(255),
"name" VARCHAR(255),
"state" BIT,
"template_code" VARCHAR(255),
"sign" VARCHAR(255),
"title" VARCHAR(255),
"content" CLOB,
"script" VARCHAR(255),
"param" VARCHAR(255),
"remarks" VARCHAR(255),
"target_" VARCHAR(2 CHAR),
"auto_read" BIT DEFAULT 1,
"remind_mode" VARCHAR(2 CHAR),
"url" VARCHAR(255),
"created_by" BIGINT,
"created_time" TIMESTAMP(0),
"updated_by" BIGINT,
"updated_time" TIMESTAMP(0),
CONSTRAINT "extend_msg_template_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
CONSTRAINT "extend_msg_template_UK_EX_MSG_TEMPLATE_CODE" UNIQUE("code")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "extend_msg_template" IS '消息模板';
COMMENT ON COLUMN "extend_msg_template"."auto_read" IS '自动已读';
COMMENT ON COLUMN "extend_msg_template"."code" IS '模板标识';
COMMENT ON COLUMN "extend_msg_template"."content" IS '模板内容';
COMMENT ON COLUMN "extend_msg_template"."created_by" IS '创建人ID';
COMMENT ON COLUMN "extend_msg_template"."created_time" IS '创建时间';
COMMENT ON COLUMN "extend_msg_template"."id" IS '模板ID';
COMMENT ON COLUMN "extend_msg_template"."interface_id" IS '接口ID';
COMMENT ON COLUMN "extend_msg_template"."name" IS '模板名称';
COMMENT ON COLUMN "extend_msg_template"."param" IS '模板参数';
COMMENT ON COLUMN "extend_msg_template"."remarks" IS '备注';
COMMENT ON COLUMN "extend_msg_template"."remind_mode" IS '提醒方式;@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_REMIND_MODE)[01-待办 02-预警 03-提醒]';
COMMENT ON COLUMN "extend_msg_template"."script" IS '脚本';
COMMENT ON COLUMN "extend_msg_template"."sign" IS '签名';
COMMENT ON COLUMN "extend_msg_template"."state" IS '状态';
COMMENT ON COLUMN "extend_msg_template"."target_" IS '打开方式;@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_TARGET)[01-页面 02-弹窗 03-新开窗口]';
COMMENT ON COLUMN "extend_msg_template"."template_code" IS '模板编码';
COMMENT ON COLUMN "extend_msg_template"."title" IS '标题';
COMMENT ON COLUMN "extend_msg_template"."type" IS '消息类型;[01-短信 02-邮件 03-站内信];@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.MSG_TEMPLATE_TYPE)';
COMMENT ON COLUMN "extend_msg_template"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "extend_msg_template"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "extend_msg_template"."url" IS '跳转地址';


CREATE TABLE "extend_notice"
(
    "id" BIGINT NOT NULL,
    "msg_id" BIGINT,
    "biz_id" VARCHAR(64),
    "biz_type" VARCHAR(64),
    "recipient_id" BIGINT,
    "remind_mode" VARCHAR(2 CHAR),
"title" VARCHAR(255),
"content" CLOB,
"author" VARCHAR(255),
"url" VARCHAR(255),
"target_" VARCHAR(2 CHAR),
"auto_read" BIT,
"handle_time" TIMESTAMP(0),
"read_time" TIMESTAMP(0),
"is_read" BIT DEFAULT 0,
"is_handle" BIT DEFAULT 0,
"created_time" TIMESTAMP(0),
"created_by" BIGINT,
"updated_time" TIMESTAMP(0),
"updated_by" BIGINT,
"created_org_id" BIGINT,
CONSTRAINT "extend_notice_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "extend_notice" IS '通知表';
COMMENT ON COLUMN "extend_notice"."author" IS '发布人';
COMMENT ON COLUMN "extend_notice"."auto_read" IS '自动已读';
COMMENT ON COLUMN "extend_notice"."biz_id" IS '业务ID';
COMMENT ON COLUMN "extend_notice"."biz_type" IS '业务类型';
COMMENT ON COLUMN "extend_notice"."content" IS '内容';
COMMENT ON COLUMN "extend_notice"."created_by" IS '创建人id';
COMMENT ON COLUMN "extend_notice"."created_org_id" IS '所属组织';
COMMENT ON COLUMN "extend_notice"."created_time" IS '创建时间';
COMMENT ON COLUMN "extend_notice"."handle_time" IS '处理时间';
COMMENT ON COLUMN "extend_notice"."id" IS 'ID';
COMMENT ON COLUMN "extend_notice"."is_handle" IS '是否处理';
COMMENT ON COLUMN "extend_notice"."is_read" IS '是否已读';
COMMENT ON COLUMN "extend_notice"."msg_id" IS '消息ID';
COMMENT ON COLUMN "extend_notice"."read_time" IS '读取时间';
COMMENT ON COLUMN "extend_notice"."recipient_id" IS '接收人';
COMMENT ON COLUMN "extend_notice"."remind_mode" IS '提醒方式;@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_REMIND_MODE)[01-待办 02-预警 03-提醒]';
COMMENT ON COLUMN "extend_notice"."target_" IS '打开方式;@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_TARGET)[01-页面 02-弹窗 03-新开窗口]';
COMMENT ON COLUMN "extend_notice"."title" IS '标题';
COMMENT ON COLUMN "extend_notice"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "extend_notice"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "extend_notice"."url" IS '处理地址';


CREATE TABLE "ota_upgrade_records"
(
    "id" BIGINT NOT NULL,
    "task_id" BIGINT,
    "device_identification" VARCHAR(100),
    "upgrade_status" SMALLINT DEFAULT 0,
    "progress" SMALLINT DEFAULT 0,
    "error_code" VARCHAR(100),
    "error_message" VARCHAR(255),
    "start_time" TIMESTAMP(0),
    "end_time" TIMESTAMP(0),
    "success_details" CLOB,
    "failure_details" CLOB,
    "log_details" CLOB,
    "remark" VARCHAR(255),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT,
    CONSTRAINT "ota_upgrade_records_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "ota_upgrade_records_idx_task_id_device_identification" UNIQUE("task_id", "device_identification")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "ota_upgrade_records" IS 'OTA升级记录表';
COMMENT ON COLUMN "ota_upgrade_records"."created_by" IS '创建人';
COMMENT ON COLUMN "ota_upgrade_records"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "ota_upgrade_records"."created_time" IS '记录创建时间';
COMMENT ON COLUMN "ota_upgrade_records"."device_identification" IS '设备标识';
COMMENT ON COLUMN "ota_upgrade_records"."end_time" IS '升级结束时间';
COMMENT ON COLUMN "ota_upgrade_records"."error_code" IS '错误代码';
COMMENT ON COLUMN "ota_upgrade_records"."error_message" IS '错误信息';
COMMENT ON COLUMN "ota_upgrade_records"."failure_details" IS '升级失败详细信息';
COMMENT ON COLUMN "ota_upgrade_records"."id" IS '主键';
COMMENT ON COLUMN "ota_upgrade_records"."log_details" IS '升级过程日志';
COMMENT ON COLUMN "ota_upgrade_records"."progress" IS '升级进度（百分比）';
COMMENT ON COLUMN "ota_upgrade_records"."remark" IS '描述';
COMMENT ON COLUMN "ota_upgrade_records"."start_time" IS '升级开始时间';
COMMENT ON COLUMN "ota_upgrade_records"."success_details" IS '升级成功详细信息';
COMMENT ON COLUMN "ota_upgrade_records"."task_id" IS '任务ID，关联ota_upgrade_tasks表';
COMMENT ON COLUMN "ota_upgrade_records"."updated_by" IS '更新人';
COMMENT ON COLUMN "ota_upgrade_records"."updated_time" IS '更新时间';
COMMENT ON COLUMN "ota_upgrade_records"."upgrade_status" IS '升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)';


CREATE OR REPLACE  INDEX "ota_upgrade_records_idx_task_id" ON "ota_upgrade_records"("task_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "ota_upgrade_tasks"
(
    "id" BIGINT NOT NULL,
    "upgrade_id" BIGINT,
    "task_name" VARCHAR(100),
    "task_status" SMALLINT DEFAULT 0,
    "scheduled_time" TIMESTAMP(0),
    "description" VARCHAR(255),
    "remark" VARCHAR(255),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT,
    CONSTRAINT "ota_upgrade_tasks_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "ota_upgrade_tasks" IS 'OTA升级任务表';
COMMENT ON COLUMN "ota_upgrade_tasks"."created_by" IS '创建人';
COMMENT ON COLUMN "ota_upgrade_tasks"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "ota_upgrade_tasks"."created_time" IS '创建时间';
COMMENT ON COLUMN "ota_upgrade_tasks"."description" IS '任务描述';
COMMENT ON COLUMN "ota_upgrade_tasks"."id" IS '主键';
COMMENT ON COLUMN "ota_upgrade_tasks"."remark" IS '描述';
COMMENT ON COLUMN "ota_upgrade_tasks"."scheduled_time" IS '计划执行时间';
COMMENT ON COLUMN "ota_upgrade_tasks"."task_name" IS '任务名称';
COMMENT ON COLUMN "ota_upgrade_tasks"."task_status" IS '任务状态(0:待发布、1:进行中、2:已完成、3:已取消)';
COMMENT ON COLUMN "ota_upgrade_tasks"."updated_by" IS '更新人';
COMMENT ON COLUMN "ota_upgrade_tasks"."updated_time" IS '更新时间';
COMMENT ON COLUMN "ota_upgrade_tasks"."upgrade_id" IS '升级包ID，关联ota_upgrades表';


CREATE OR REPLACE  INDEX "ota_upgrade_tasks_idx_upgrade_id" ON "ota_upgrade_tasks"("upgrade_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "ota_upgrades"
(
    "id" BIGINT NOT NULL,
    "app_id" VARCHAR(64),
    "package_name" VARCHAR(100),
    "package_type" SMALLINT DEFAULT 0,
    "product_identification" VARCHAR(100),
    "version" VARCHAR(255),
    "file_location" VARCHAR(255),
    "sign_method" SMALLINT NOT NULL DEFAULT 0,
    "status" SMALLINT DEFAULT 0,
    "description" VARCHAR(255),
    "custom_info" CLOB,
    "remark" VARCHAR(255),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT,
    CONSTRAINT "ota_upgrades_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "ota_upgrades" IS 'OTA升级包';
COMMENT ON COLUMN "ota_upgrades"."app_id" IS '应用ID';
COMMENT ON COLUMN "ota_upgrades"."created_by" IS '创建人';
COMMENT ON COLUMN "ota_upgrades"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "ota_upgrades"."created_time" IS '创建时间';
COMMENT ON COLUMN "ota_upgrades"."custom_info" IS '自定义信息';
COMMENT ON COLUMN "ota_upgrades"."description" IS '升级包功能描述';
COMMENT ON COLUMN "ota_upgrades"."file_location" IS '升级包的位置';
COMMENT ON COLUMN "ota_upgrades"."sign_method" IS '签名方法(0-MD5、1-SHA256)';
COMMENT ON COLUMN "ota_upgrades"."id" IS '主键';
COMMENT ON COLUMN "ota_upgrades"."package_name" IS '包名称';
COMMENT ON COLUMN "ota_upgrades"."package_type" IS '升级包类型(0:软件包、1:固件包)';
COMMENT ON COLUMN "ota_upgrades"."product_identification" IS '产品标识';
COMMENT ON COLUMN "ota_upgrades"."remark" IS '描述';
COMMENT ON COLUMN "ota_upgrades"."status" IS '状态';
COMMENT ON COLUMN "ota_upgrades"."updated_by" IS '更新人';
COMMENT ON COLUMN "ota_upgrades"."updated_time" IS '更新时间';
COMMENT ON COLUMN "ota_upgrades"."version" IS '升级包版本号';


CREATE OR REPLACE  INDEX "ota_upgrades_idx_version" ON "ota_upgrades"("version" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "ota_upgrades_idx_app_id" ON "ota_upgrades"("app_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "product"
(
    "id" BIGINT NOT NULL,
    "app_id" VARCHAR(64),
    "template_id" BIGINT,
    "product_name" VARCHAR(255),
    "product_identification" VARCHAR(100),
    "product_type" TINYINT DEFAULT 0,
    "manufacturer_id" VARCHAR(255),
    "manufacturer_name" VARCHAR(255),
    "model" VARCHAR(255),
    "data_format" VARCHAR(255),
    "device_type" VARCHAR(255),
    "protocol_type" VARCHAR(255),
    "product_status" TINYINT DEFAULT 0,
    "active_version_no" VARCHAR(64) DEFAULT '',
    "previous_full_version_no" VARCHAR(64) DEFAULT '',
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "product_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "product_idx_product_identification" UNIQUE("product_identification")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "product" IS '产品模型';
COMMENT ON COLUMN "product"."app_id" IS '应用ID';
COMMENT ON COLUMN "product"."created_by" IS '创建人';
COMMENT ON COLUMN "product"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "product"."created_time" IS '创建时间';
COMMENT ON COLUMN "product"."data_format" IS '数据格式';
COMMENT ON COLUMN "product"."device_type" IS '设备类型';
COMMENT ON COLUMN "product"."id" IS 'id';
COMMENT ON COLUMN "product"."manufacturer_id" IS '厂商ID';
COMMENT ON COLUMN "product"."manufacturer_name" IS '厂商名称';
COMMENT ON COLUMN "product"."model" IS '产品型号';
COMMENT ON COLUMN "product"."product_identification" IS '产品标识';
COMMENT ON COLUMN "product"."product_name" IS '产品名称';
COMMENT ON COLUMN "product"."product_status" IS '状态';
COMMENT ON COLUMN "product"."product_type" IS '产品类型';
COMMENT ON COLUMN "product"."active_version_no" IS '产品当前激活的版本序号(系统发布时生成的快照标识,16位短雪花字符串,非用户语义化版本号)';
COMMENT ON COLUMN "product"."previous_full_version_no" IS '灰度切换前的全量版本序号(仅灰度态有值,灰度晋升/回滚后清空,供回滚定位与灰度路由用)';
COMMENT ON COLUMN "product"."protocol_type" IS '接入协议';
COMMENT ON COLUMN "product"."remark" IS '产品描述';
COMMENT ON COLUMN "product"."template_id" IS '产品模板ID';
COMMENT ON COLUMN "product"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "product"."updated_time" IS '最后修改时间';


CREATE OR REPLACE  INDEX "product_idx_manufacturer_id" ON "product"("manufacturer_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "product_command"
(
    "id" BIGINT NOT NULL,
    "service_id" BIGINT,
    "command_code" VARCHAR(255),
    "command_name" VARCHAR(255),
    "description" VARCHAR(255),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "product_command_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "product_command" IS '产品模型设备服务命令表';
COMMENT ON COLUMN "product_command"."command_code" IS '命令编码';
COMMENT ON COLUMN "product_command"."command_name" IS '命令名称';
COMMENT ON COLUMN "product_command"."created_by" IS '创建人';
COMMENT ON COLUMN "product_command"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "product_command"."created_time" IS '创建时间';
COMMENT ON COLUMN "product_command"."description" IS '命令描述。';
COMMENT ON COLUMN "product_command"."id" IS '命令id';
COMMENT ON COLUMN "product_command"."remark" IS '备注';
COMMENT ON COLUMN "product_command"."service_id" IS '服务ID';
COMMENT ON COLUMN "product_command"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "product_command"."updated_time" IS '最后修改时间';


CREATE OR REPLACE  INDEX "product_command_service_id" ON "product_command"("service_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "product_command_request"
(
    "id" BIGINT NOT NULL,
    "service_id" BIGINT DEFAULT 0,
    "command_id" BIGINT DEFAULT 0,
    "parameter_code" VARCHAR(255),
    "parameter_name" VARCHAR(255),
    "parameter_description" VARCHAR(255),
    "datatype" VARCHAR(255),
    "enumlist" VARCHAR(255),
    "max" VARCHAR(255),
    "maxlength" VARCHAR(255),
    "min" VARCHAR(255),
    "required" VARCHAR(255) DEFAULT '0',
    "step" VARCHAR(255),
    "unit" VARCHAR(255),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "product_command_request_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "product_command_request" IS '产品模型设备下发服务命令属性表';
COMMENT ON COLUMN "product_command_request"."command_id" IS '命令ID';
COMMENT ON COLUMN "product_command_request"."created_by" IS '创建人';
COMMENT ON COLUMN "product_command_request"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "product_command_request"."created_time" IS '创建时间';
COMMENT ON COLUMN "product_command_request"."datatype" IS '数据类型';
COMMENT ON COLUMN "product_command_request"."enumlist" IS '枚举值';
COMMENT ON COLUMN "product_command_request"."id" IS 'id';
COMMENT ON COLUMN "product_command_request"."max" IS '最大值';
COMMENT ON COLUMN "product_command_request"."maxlength" IS '字符串长度';
COMMENT ON COLUMN "product_command_request"."min" IS '最小值';
COMMENT ON COLUMN "product_command_request"."parameter_code" IS '参数编码';
COMMENT ON COLUMN "product_command_request"."parameter_description" IS '参数描述';
COMMENT ON COLUMN "product_command_request"."parameter_name" IS '参数名称';
COMMENT ON COLUMN "product_command_request"."remark" IS '备注';
COMMENT ON COLUMN "product_command_request"."required" IS '是否必填';
COMMENT ON COLUMN "product_command_request"."service_id" IS '服务ID';
COMMENT ON COLUMN "product_command_request"."step" IS '步长';
COMMENT ON COLUMN "product_command_request"."unit" IS '单位';
COMMENT ON COLUMN "product_command_request"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "product_command_request"."updated_time" IS '最后修改时间';


CREATE OR REPLACE  INDEX "product_command_request_command_id" ON "product_command_request"("command_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "product_command_request_service_id" ON "product_command_request"("service_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "product_command_response"
(
    "id" BIGINT NOT NULL,
    "command_id" BIGINT DEFAULT 0,
    "service_id" BIGINT DEFAULT 0,
    "datatype" VARCHAR(255),
    "enumlist" VARCHAR(255),
    "max" VARCHAR(255),
    "maxlength" VARCHAR(255),
    "min" VARCHAR(255),
    "parameter_description" VARCHAR(255),
    "parameter_name" VARCHAR(255),
    "parameter_code" VARCHAR(255),
    "required" VARCHAR(255) DEFAULT '0',
    "step" VARCHAR(255),
    "unit" VARCHAR(255),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "product_command_response_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "product_command_response" IS '产品模型设备响应服务命令属性表';
COMMENT ON COLUMN "product_command_response"."command_id" IS '命令ID';
COMMENT ON COLUMN "product_command_response"."created_by" IS '创建人';
COMMENT ON COLUMN "product_command_response"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "product_command_response"."created_time" IS '创建时间';
COMMENT ON COLUMN "product_command_response"."datatype" IS '数据类型';
COMMENT ON COLUMN "product_command_response"."enumlist" IS '枚举值';
COMMENT ON COLUMN "product_command_response"."id" IS 'id';
COMMENT ON COLUMN "product_command_response"."max" IS '最大值';
COMMENT ON COLUMN "product_command_response"."maxlength" IS '字符串长度';
COMMENT ON COLUMN "product_command_response"."min" IS '最小值';
COMMENT ON COLUMN "product_command_response"."parameter_code" IS '参数字段值';
COMMENT ON COLUMN "product_command_response"."parameter_description" IS '参数描述';
COMMENT ON COLUMN "product_command_response"."parameter_name" IS '参数名字。';
COMMENT ON COLUMN "product_command_response"."remark" IS '备注';
COMMENT ON COLUMN "product_command_response"."required" IS '是否必填';
COMMENT ON COLUMN "product_command_response"."service_id" IS '服务ID';
COMMENT ON COLUMN "product_command_response"."step" IS '步长';
COMMENT ON COLUMN "product_command_response"."unit" IS '单位';
COMMENT ON COLUMN "product_command_response"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "product_command_response"."updated_time" IS '最后修改时间';


CREATE OR REPLACE  INDEX "product_command_response_command_id" ON "product_command_response"("command_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "product_command_response_service_id" ON "product_command_response"("service_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "product_property"
(
    "id" BIGINT NOT NULL,
    "service_id" BIGINT,
    "property_code" VARCHAR(255),
    "property_name" VARCHAR(255),
    "datatype" VARCHAR(255),
    "description" VARCHAR(255),
    "enumlist" VARCHAR(255),
    "max" VARCHAR(255),
    "maxlength" VARCHAR(255),
    "method" VARCHAR(255),
    "min" VARCHAR(255),
    "required" VARCHAR(255) DEFAULT '0',
    "step" VARCHAR(255),
    "unit" VARCHAR(255),
    "icon" VARCHAR(255),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "product_property_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "product_property" IS '产品模型服务属性表';
COMMENT ON COLUMN "product_property"."created_by" IS '创建人';
COMMENT ON COLUMN "product_property"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "product_property"."created_time" IS '创建时间';
COMMENT ON COLUMN "product_property"."datatype" IS '数据类型';
COMMENT ON COLUMN "product_property"."description" IS '属性描述';
COMMENT ON COLUMN "product_property"."enumlist" IS '枚举值';
COMMENT ON COLUMN "product_property"."icon" IS '图标';
COMMENT ON COLUMN "product_property"."id" IS '属性id';
COMMENT ON COLUMN "product_property"."max" IS '最大值';
COMMENT ON COLUMN "product_property"."maxlength" IS '字符串长度';
COMMENT ON COLUMN "product_property"."method" IS '访问模式';
COMMENT ON COLUMN "product_property"."min" IS '最小值';
COMMENT ON COLUMN "product_property"."property_code" IS '属性编码';
COMMENT ON COLUMN "product_property"."property_name" IS '属性名称';
COMMENT ON COLUMN "product_property"."remark" IS '备注';
COMMENT ON COLUMN "product_property"."required" IS '是否必填';
COMMENT ON COLUMN "product_property"."service_id" IS '服务ID';
COMMENT ON COLUMN "product_property"."step" IS '步长';
COMMENT ON COLUMN "product_property"."unit" IS '单位';
COMMENT ON COLUMN "product_property"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "product_property"."updated_time" IS '最后修改时间';


CREATE OR REPLACE  INDEX "product_property_service_id" ON "product_property"("service_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "product_service"
(
    "id" BIGINT NOT NULL,
    "product_id" BIGINT,
    "service_code" VARCHAR(255),
    "service_name" VARCHAR(255),
    "service_type" VARCHAR(255),
    "service_status" TINYINT DEFAULT 0,
    "description" VARCHAR(255),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "product_service_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "product_service" IS '产品模型服务表';
COMMENT ON COLUMN "product_service"."created_by" IS '创建人';
COMMENT ON COLUMN "product_service"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "product_service"."created_time" IS '创建时间';
COMMENT ON COLUMN "product_service"."description" IS '服务描述';
COMMENT ON COLUMN "product_service"."id" IS '服务id';
COMMENT ON COLUMN "product_service"."product_id" IS '产品ID';
COMMENT ON COLUMN "product_service"."remark" IS '备注';
COMMENT ON COLUMN "product_service"."service_code" IS '服务编码';
COMMENT ON COLUMN "product_service"."service_name" IS '服务名称';
COMMENT ON COLUMN "product_service"."service_status" IS '状态';
COMMENT ON COLUMN "product_service"."service_type" IS '服务类型';
COMMENT ON COLUMN "product_service"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "product_service"."updated_time" IS '最后修改时间';


CREATE TABLE "product_topic"
(
    "id" DECIMAL NOT NULL,
    "product_id" BIGINT,
    "topic_type" TINYINT,
    "topic" VARCHAR(100),
    "publisher" VARCHAR(255),
    "subscriber" VARCHAR(255),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "product_topic_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "product_topic" IS '产品Topic信息表';
COMMENT ON COLUMN "product_topic"."created_by" IS '创建人';
COMMENT ON COLUMN "product_topic"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "product_topic"."created_time" IS '创建时间';
COMMENT ON COLUMN "product_topic"."id" IS 'id';
COMMENT ON COLUMN "product_topic"."product_id" IS '设备id';
COMMENT ON COLUMN "product_topic"."publisher" IS '发布者';
COMMENT ON COLUMN "product_topic"."remark" IS '备注';
COMMENT ON COLUMN "product_topic"."subscriber" IS '订阅者';
COMMENT ON COLUMN "product_topic"."topic" IS 'topic';
COMMENT ON COLUMN "product_topic"."topic_type" IS '类型(0:基础Topic,1:自定义Topic)';
COMMENT ON COLUMN "product_topic"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "product_topic"."updated_time" IS '最后修改时间';


CREATE TABLE "product_version"
(
    "id" BIGINT NOT NULL,
    "product_identification" VARCHAR(100) DEFAULT '',
    "version_no" VARCHAR(64) DEFAULT '',
    "version_status" TINYINT DEFAULT 0,
    "product_snapshot_json" CLOB,
    "publish_strategy" TINYINT,
    "canary_config_json" CLOB,
    "publish_time" TIMESTAMP(0),
    "remark" VARCHAR(500) DEFAULT '',
    "created_org_id" BIGINT,
    "deleted" TINYINT DEFAULT 0,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    CONSTRAINT "product_version_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "product_version" IS '产品物模型版本快照';
COMMENT ON COLUMN "product_version"."id" IS 'id';
COMMENT ON COLUMN "product_version"."product_identification" IS '产品标识';
COMMENT ON COLUMN "product_version"."version_no" IS '版本序号(系统发布时生成的不可变快照标识,16位短雪花字符串)';
COMMENT ON COLUMN "product_version"."version_status" IS '版本状态[0-草稿 1-已发布 2-灰度中 3-影子 4-已回滚 5-已归档]';
COMMENT ON COLUMN "product_version"."product_snapshot_json" IS '产品快照JSON(冻结整棵产品树)';
COMMENT ON COLUMN "product_version"."publish_strategy" IS '发布策略[0-全量 1-灰度 2-影子]';
COMMENT ON COLUMN "product_version"."canary_config_json" IS '灰度配置JSON';
COMMENT ON COLUMN "product_version"."publish_time" IS '发布时间';
COMMENT ON COLUMN "product_version"."remark" IS '备注';
COMMENT ON COLUMN "product_version"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "product_version"."deleted" IS '逻辑删除标识(0-未删除、1-已删除)';
COMMENT ON COLUMN "product_version"."created_time" IS '创建时间';
COMMENT ON COLUMN "product_version"."created_by" IS '创建人';
COMMENT ON COLUMN "product_version"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "product_version"."updated_by" IS '最后修改人';

CREATE OR REPLACE  INDEX "product_version_idx_product_identification" ON "product_version"("product_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "product_version_idx_version_no" ON "product_version"("version_no" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "product_version_idx_version_status" ON "product_version"("version_status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "product_publish_record"
(
    "id" BIGINT NOT NULL,
    "product_identification" VARCHAR(100) DEFAULT '',
    "source_version" VARCHAR(64) DEFAULT '',
    "target_version" VARCHAR(64) DEFAULT '',
    "intent" TINYINT DEFAULT 0,
    "status" TINYINT DEFAULT 0,
    "ddl_summary" CLOB,
    "failed_reason" VARCHAR(2000) DEFAULT '',
    "started_time" TIMESTAMP(0),
    "finished_time" TIMESTAMP(0),
    "remark" VARCHAR(500) DEFAULT '',
    "created_org_id" BIGINT,
    "deleted" TINYINT DEFAULT 0,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    CONSTRAINT "product_publish_record_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "product_publish_record" IS '产品发布记录';
COMMENT ON COLUMN "product_publish_record"."id" IS 'id';
COMMENT ON COLUMN "product_publish_record"."product_identification" IS '产品标识';
COMMENT ON COLUMN "product_publish_record"."source_version" IS '源版本号';
COMMENT ON COLUMN "product_publish_record"."target_version" IS '目标版本号';
COMMENT ON COLUMN "product_publish_record"."intent" IS '操作意图[0-发布 1-回滚 2-历史清理]';
COMMENT ON COLUMN "product_publish_record"."status" IS '执行状态[0-执行中 1-成功 2-失败]';
COMMENT ON COLUMN "product_publish_record"."ddl_summary" IS 'DDL列表JSON数组(已执行的DDL明细 + 重试元数据)';
COMMENT ON COLUMN "product_publish_record"."failed_reason" IS '失败原因(成功时为空)';
COMMENT ON COLUMN "product_publish_record"."started_time" IS '开始时间';
COMMENT ON COLUMN "product_publish_record"."finished_time" IS '结束时间';
COMMENT ON COLUMN "product_publish_record"."remark" IS '备注';
COMMENT ON COLUMN "product_publish_record"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "product_publish_record"."deleted" IS '逻辑删除标识(0-未删除、1-已删除)';
COMMENT ON COLUMN "product_publish_record"."created_time" IS '创建时间';
COMMENT ON COLUMN "product_publish_record"."created_by" IS '创建人';
COMMENT ON COLUMN "product_publish_record"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "product_publish_record"."updated_by" IS '最后修改人';

CREATE OR REPLACE  INDEX "product_publish_record_idx_product_identification" ON "product_publish_record"("product_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "product_publish_record_idx_target_version" ON "product_publish_record"("target_version" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "product_version_change_log"
(
    "id" BIGINT NOT NULL,
    "product_identification" VARCHAR(100) DEFAULT '',
    "version_no" VARCHAR(64) DEFAULT '',
    "change_type" TINYINT DEFAULT 1,
    "target_type" TINYINT,
    "change_summary" VARCHAR(500) DEFAULT '',
    "change_detail_json" CLOB,
    "created_org_id" BIGINT,
    "deleted" TINYINT DEFAULT 0,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    CONSTRAINT "product_version_change_log_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "product_version_change_log" IS '产品物模型版本变更日志';
COMMENT ON COLUMN "product_version_change_log"."id" IS 'id';
COMMENT ON COLUMN "product_version_change_log"."product_identification" IS '产品标识';
COMMENT ON COLUMN "product_version_change_log"."version_no" IS '版本序号:本批变更归属版本(草稿期累积,发布后固化,对应 product_version.version_no)';
COMMENT ON COLUMN "product_version_change_log"."change_type" IS '变更类型[0-新增 1-编辑 2-删除]';
COMMENT ON COLUMN "product_version_change_log"."target_type" IS '变更维度[0-产品信息 1-服务 2-属性 3-命令]';
COMMENT ON COLUMN "product_version_change_log"."change_summary" IS '变更摘要';
COMMENT ON COLUMN "product_version_change_log"."change_detail_json" IS '字段级变更明细JSON(覆盖产品所有字段)';
COMMENT ON COLUMN "product_version_change_log"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "product_version_change_log"."deleted" IS '逻辑删除标识(0-未删除、1-已删除)';
COMMENT ON COLUMN "product_version_change_log"."created_time" IS '创建时间';
COMMENT ON COLUMN "product_version_change_log"."created_by" IS '创建人';
COMMENT ON COLUMN "product_version_change_log"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "product_version_change_log"."updated_by" IS '最后修改人';

CREATE OR REPLACE  INDEX "product_version_change_log_idx_product_identification" ON "product_version_change_log"("product_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE  INDEX "product_version_change_log_idx_change_type" ON "product_version_change_log"("change_type" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;


CREATE TABLE "rule"
(
    "id" BIGINT NOT NULL,
    "rule_name" VARCHAR(100),
    "rule_identification" VARCHAR(100),
    "app_id" VARCHAR(64),
    "effective_type" SMALLINT,
    "appoint_content" VARCHAR(500),
    "status" SMALLINT DEFAULT 0,
    "remark" VARCHAR(255),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT,
    CONSTRAINT "rule_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "rule_idx_rule_identification" UNIQUE("rule_identification")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule" IS '规则信息表';
COMMENT ON COLUMN "rule"."app_id" IS '应用ID';
COMMENT ON COLUMN "rule"."appoint_content" IS '指定内容';
COMMENT ON COLUMN "rule"."created_by" IS '创建人';
COMMENT ON COLUMN "rule"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule"."effective_type" IS '生效类型';
COMMENT ON COLUMN "rule"."id" IS '主键';
COMMENT ON COLUMN "rule"."remark" IS '描述';
COMMENT ON COLUMN "rule"."rule_identification" IS '规则标识';
COMMENT ON COLUMN "rule"."rule_name" IS '规则名称';
COMMENT ON COLUMN "rule"."status" IS '启用状态';
COMMENT ON COLUMN "rule"."updated_by" IS '更新人';
COMMENT ON COLUMN "rule"."updated_time" IS '更新时间';


CREATE TABLE "rule_alarm"
(
    "id" BIGINT NOT NULL,
    "app_id" VARCHAR(64),
    "alarm_name" VARCHAR(200),
    "alarm_identification" VARCHAR(100),
    "alarm_scene" VARCHAR(255),
    "alarm_channel_ids" VARCHAR(255),
    "level" SMALLINT DEFAULT 0,
    "status" SMALLINT DEFAULT 0,
    "remark" VARCHAR(255),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT,
    CONSTRAINT "rule_alarm_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_alarm" IS '告警规则表';
COMMENT ON COLUMN "rule_alarm"."alarm_channel_ids" IS '告警渠道ID集合';
COMMENT ON COLUMN "rule_alarm"."alarm_identification" IS '告警编码';
COMMENT ON COLUMN "rule_alarm"."alarm_name" IS '告警名称';
COMMENT ON COLUMN "rule_alarm"."alarm_scene" IS '告警场景';
COMMENT ON COLUMN "rule_alarm"."app_id" IS '应用ID';
COMMENT ON COLUMN "rule_alarm"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_alarm"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_alarm"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_alarm"."id" IS '主键';
COMMENT ON COLUMN "rule_alarm"."level" IS '告警级别';
COMMENT ON COLUMN "rule_alarm"."remark" IS '描述';
COMMENT ON COLUMN "rule_alarm"."status" IS '启用状态';
COMMENT ON COLUMN "rule_alarm"."updated_by" IS '更新人';
COMMENT ON COLUMN "rule_alarm"."updated_time" IS '更新时间';


CREATE TABLE "rule_alarm_channel"
(
    "id" BIGINT NOT NULL,
    "channel_name" VARCHAR(200),
    "channel_type" SMALLINT,
    "channel_config" CLOB,
    "status" SMALLINT DEFAULT 0,
    "remark" VARCHAR(255),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT,
    CONSTRAINT "rule_alarm_channel_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_alarm_channel" IS '告警规则渠道表';
COMMENT ON COLUMN "rule_alarm_channel"."channel_config" IS '告警配置';
COMMENT ON COLUMN "rule_alarm_channel"."channel_name" IS '渠道名称';
COMMENT ON COLUMN "rule_alarm_channel"."channel_type" IS '渠道类型';
COMMENT ON COLUMN "rule_alarm_channel"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_alarm_channel"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_alarm_channel"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_alarm_channel"."id" IS '主键';
COMMENT ON COLUMN "rule_alarm_channel"."remark" IS '描述';
COMMENT ON COLUMN "rule_alarm_channel"."status" IS '启用状态';
COMMENT ON COLUMN "rule_alarm_channel"."updated_by" IS '更新人';
COMMENT ON COLUMN "rule_alarm_channel"."updated_time" IS '更新时间';


CREATE TABLE "rule_alarm_record"
(
    "id" BIGINT NOT NULL,
    "app_id" VARCHAR(64),
    "alarm_identification" VARCHAR(100),
    "occurred_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "handled_time" TIMESTAMP(0),
    "handling_notes" CLOB,
    "resolved_time" TIMESTAMP(0),
    "resolution_notes" CLOB,
    "content_data" CLOB,
    "handled_status" SMALLINT DEFAULT 0,
    "remark" VARCHAR(255),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT,
    CONSTRAINT "rule_alarm_record_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_alarm_record" IS '告警规则记录表';
COMMENT ON COLUMN "rule_alarm_record"."alarm_identification" IS '告警编码';
COMMENT ON COLUMN "rule_alarm_record"."app_id" IS '应用ID';
COMMENT ON COLUMN "rule_alarm_record"."content_data" IS '告警具体内容信息';
COMMENT ON COLUMN "rule_alarm_record"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_alarm_record"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_alarm_record"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_alarm_record"."handled_status" IS '处理状态';
COMMENT ON COLUMN "rule_alarm_record"."handled_time" IS '处理时间';
COMMENT ON COLUMN "rule_alarm_record"."handling_notes" IS '处理记录';
COMMENT ON COLUMN "rule_alarm_record"."id" IS '主键';
COMMENT ON COLUMN "rule_alarm_record"."occurred_time" IS '发生时间';
COMMENT ON COLUMN "rule_alarm_record"."remark" IS '描述';
COMMENT ON COLUMN "rule_alarm_record"."resolution_notes" IS '解决记录';
COMMENT ON COLUMN "rule_alarm_record"."resolved_time" IS '解决时间';
COMMENT ON COLUMN "rule_alarm_record"."updated_by" IS '更新人';
COMMENT ON COLUMN "rule_alarm_record"."updated_time" IS '更新时间';


CREATE TABLE "rule_condition"
(
    "id" BIGINT NOT NULL,
    "rule_id" BIGINT,
    "condition_identification" VARCHAR(100),
    "condition_type" SMALLINT DEFAULT 0,
    "condition_scheme" CLOB,
    "status" SMALLINT DEFAULT 0,
    "anti_shake" SMALLINT DEFAULT 0,
    "anti_shake_scheme" VARCHAR(255),
    "remark" VARCHAR(255),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT,
    CONSTRAINT "rule_condition_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_condition" IS '规则条件表';
COMMENT ON COLUMN "rule_condition"."anti_shake" IS '防抖状态';
COMMENT ON COLUMN "rule_condition"."anti_shake_scheme" IS '防抖策略';
COMMENT ON COLUMN "rule_condition"."condition_identification" IS '条件编码';
COMMENT ON COLUMN "rule_condition"."condition_scheme" IS '条件内容';
COMMENT ON COLUMN "rule_condition"."condition_type" IS '条件类型';
COMMENT ON COLUMN "rule_condition"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_condition"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_condition"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_condition"."id" IS '主键';
COMMENT ON COLUMN "rule_condition"."remark" IS '描述';
COMMENT ON COLUMN "rule_condition"."rule_id" IS '规则';
COMMENT ON COLUMN "rule_condition"."status" IS '启用状态';
COMMENT ON COLUMN "rule_condition"."updated_by" IS '更新人';
COMMENT ON COLUMN "rule_condition"."updated_time" IS '更新时间';


CREATE OR REPLACE  INDEX "rule_condition_idx_rule_id" ON "rule_condition"("rule_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "rule_condition_action"
(
    "id" BIGINT NOT NULL,
    "rule_condition_id" BIGINT,
    "action_type" SMALLINT DEFAULT 0,
    "action_content" CLOB,
    "remark" VARCHAR(255),
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT,
    CONSTRAINT "rule_condition_action_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_condition_action" IS '规则条件动作表';
COMMENT ON COLUMN "rule_condition_action"."action_content" IS '动作内容';
COMMENT ON COLUMN "rule_condition_action"."action_type" IS '执行动作';
COMMENT ON COLUMN "rule_condition_action"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_condition_action"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_condition_action"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_condition_action"."id" IS '主键';
COMMENT ON COLUMN "rule_condition_action"."remark" IS '描述';
COMMENT ON COLUMN "rule_condition_action"."rule_condition_id" IS '规则条件ID';
COMMENT ON COLUMN "rule_condition_action"."updated_by" IS '更新人';
COMMENT ON COLUMN "rule_condition_action"."updated_time" IS '更新时间';


CREATE OR REPLACE  INDEX "rule_condition_action_idx_rule_condition_id" ON "rule_condition_action"("rule_condition_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

CREATE TABLE "rule_instance"
(
    "id" DECIMAL NOT NULL,
    "app_id" VARCHAR(64),
    "rule_name" VARCHAR(255),
    "flow_id" VARCHAR(200),
    "flow_data" CLOB,
    "type" TINYINT DEFAULT 0,
    "instance_address" VARCHAR(100),
    "status" TINYINT DEFAULT 0,
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "rule_instance_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "rule_instance_idx_flow_id" UNIQUE("flow_id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_instance" IS '规则实例表';
COMMENT ON COLUMN "rule_instance"."app_id" IS '应用ID';
COMMENT ON COLUMN "rule_instance"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_instance"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_instance"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_instance"."flow_data" IS '流程数据';
COMMENT ON COLUMN "rule_instance"."flow_id" IS '流程ID， 规则实例类型为“规则编排”时，该项为对应的NedRed流程';
COMMENT ON COLUMN "rule_instance"."id" IS 'id';
COMMENT ON COLUMN "rule_instance"."instance_address" IS '实例地址';
COMMENT ON COLUMN "rule_instance"."remark" IS '备注';
COMMENT ON COLUMN "rule_instance"."rule_name" IS '规则实例名称';
COMMENT ON COLUMN "rule_instance"."status" IS '状态';
COMMENT ON COLUMN "rule_instance"."type" IS '规则实例类型(0规则编排、1设备告警、2数据转发）';
COMMENT ON COLUMN "rule_instance"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "rule_instance"."updated_time" IS '最后修改时间';


CREATE TABLE "undo_log"
(
    "id" BIGINT IDENTITY(1, 1) NOT NULL,
    "branch_id" BIGINT,
    "xid" VARCHAR(100),
    "context" VARCHAR(128),
    "rollback_info" BLOB,
    "log_status" INT,
    "log_created" TIMESTAMP(0),
    "log_modified" TIMESTAMP(0),
    CONSTRAINT "undo_log_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "undo_log_ux_undo_log" UNIQUE("xid", "branch_id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "undo_log" IS 'AT transaction mode undo table';
COMMENT ON COLUMN "undo_log"."branch_id" IS 'branch transaction id';
COMMENT ON COLUMN "undo_log"."context" IS 'undo_log context,such as serialization';
COMMENT ON COLUMN "undo_log"."id" IS 'increment id';
COMMENT ON COLUMN "undo_log"."log_created" IS 'create datetime';
COMMENT ON COLUMN "undo_log"."log_modified" IS 'modify datetime';
COMMENT ON COLUMN "undo_log"."log_status" IS '0:normal status,1:defense status';
COMMENT ON COLUMN "undo_log"."rollback_info" IS 'rollback info';
COMMENT ON COLUMN "undo_log"."xid" IS 'global transaction id';


CREATE TABLE "video_media_server"
(
    "id" DECIMAL NOT NULL,
    "app_id" VARCHAR(64),
    "name" VARCHAR(255),
    "media_identification" VARCHAR(255),
    "ip" VARCHAR(50),
    "hook_ip" VARCHAR(50),
    "sdp_ip" VARCHAR(50),
    "stream_ip" VARCHAR(50),
    "http_port" INT,
    "http_ssl_port" INT,
    "rtmp_port" INT,
    "rtmp_ssl_port" INT,
    "rtp_proxy_port" INT,
    "rtsp_port" INT,
    "rtsp_ssl_port" INT,
    "flv_port" INT,
    "flv_ssl_port" INT,
    "ws_flv_port" INT,
    "ws_flv_ssl_port" INT,
    "auto_config" TINYINT DEFAULT 0,
    "secret" VARCHAR(50),
    "type" VARCHAR(50) DEFAULT 'zlm',
    "rtp_enable" TINYINT DEFAULT 0,
    "rtp_port_range" VARCHAR(50),
    "send_rtp_port_range" VARCHAR(50),
    "record_assist_port" INT,
    "default_server" TINYINT DEFAULT 0,
    "hook_alive_interval" INT,
    "record_path" VARCHAR(255),
    "record_day" INT DEFAULT 7,
    "transcode_suffix" VARCHAR(255),
    "online_status" TINYINT DEFAULT 0,
    "extend_params" CLOB,
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "video_media_server_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "video_media_server_uk_media_server_unique_ip_http_port" UNIQUE("ip", "http_port")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_media_server" IS '流媒体服务器信息表';
COMMENT ON COLUMN "video_media_server"."app_id" IS '应用ID';
COMMENT ON COLUMN "video_media_server"."auto_config" IS '是否开启自动配置ZLM';
COMMENT ON COLUMN "video_media_server"."created_by" IS '创建人';
COMMENT ON COLUMN "video_media_server"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "video_media_server"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_media_server"."default_server" IS '是否是默认ZLM服务器';
COMMENT ON COLUMN "video_media_server"."extend_params" IS '扩展参数';
COMMENT ON COLUMN "video_media_server"."flv_port" IS 'FLV端口';
COMMENT ON COLUMN "video_media_server"."flv_ssl_port" IS 'FLV SSL端口';
COMMENT ON COLUMN "video_media_server"."hook_alive_interval" IS 'keepalive hook触发间隔，单位秒';
COMMENT ON COLUMN "video_media_server"."hook_ip" IS 'hook使用的IP（zlm访问客户端使用的IP）';
COMMENT ON COLUMN "video_media_server"."http_port" IS 'HTTP端口';
COMMENT ON COLUMN "video_media_server"."http_ssl_port" IS 'HTTPS端口';
COMMENT ON COLUMN "video_media_server"."id" IS '唯一标识符';
COMMENT ON COLUMN "video_media_server"."ip" IS '服务器IP地址';
COMMENT ON COLUMN "video_media_server"."media_identification" IS '媒体唯一标识';
COMMENT ON COLUMN "video_media_server"."name" IS '名称';
COMMENT ON COLUMN "video_media_server"."online_status" IS '在线状态(0:离线、1:在线)';
COMMENT ON COLUMN "video_media_server"."record_assist_port" IS '录制辅助服务端口';
COMMENT ON COLUMN "video_media_server"."record_day" IS '录像存储时长（天）';
COMMENT ON COLUMN "video_media_server"."record_path" IS '录像存储路径';
COMMENT ON COLUMN "video_media_server"."remark" IS '备注';
COMMENT ON COLUMN "video_media_server"."rtmp_port" IS 'RTMP端口';
COMMENT ON COLUMN "video_media_server"."rtmp_ssl_port" IS 'RTMP SSL端口';
COMMENT ON COLUMN "video_media_server"."rtp_enable" IS '是否启用多端口模式';
COMMENT ON COLUMN "video_media_server"."rtp_port_range" IS '多端口RTP收流端口范围';
COMMENT ON COLUMN "video_media_server"."rtp_proxy_port" IS 'RTP代理端口（单端口模式）';
COMMENT ON COLUMN "video_media_server"."rtsp_port" IS 'RTSP端口';
COMMENT ON COLUMN "video_media_server"."rtsp_ssl_port" IS 'RTSP SSL端口';
COMMENT ON COLUMN "video_media_server"."sdp_ip" IS 'SDP IP地址';
COMMENT ON COLUMN "video_media_server"."secret" IS 'ZLM鉴权参数';
COMMENT ON COLUMN "video_media_server"."send_rtp_port_range" IS 'RTP发流端口范围';
COMMENT ON COLUMN "video_media_server"."stream_ip" IS '流IP地址';
COMMENT ON COLUMN "video_media_server"."transcode_suffix" IS '转码的前缀';
COMMENT ON COLUMN "video_media_server"."type" IS '类型（zlm/abl）';
COMMENT ON COLUMN "video_media_server"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "video_media_server"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "video_media_server"."ws_flv_port" IS 'WebSocket FLV端口';
COMMENT ON COLUMN "video_media_server"."ws_flv_ssl_port" IS 'WebSocket FLV SSL端口';


CREATE TABLE "video_stream_proxy"
(
    "id" DECIMAL NOT NULL,
    "app_id" VARCHAR(64),
    "proxy_type" VARCHAR(50),
    "proxy_name" VARCHAR(255),
    "stream_identification" VARCHAR(255),
    "url" VARCHAR(255),
    "src_url" VARCHAR(255),
    "dst_url" VARCHAR(255),
    "timeout_ms" INT,
    "ffmpeg_cmd_key" VARCHAR(255),
    "rtp_type" VARCHAR(50),
    "gb_identification" VARCHAR(255),
    "media_identification" VARCHAR(255),
    "enable_audio" TINYINT DEFAULT 0,
    "enable_mp4" TINYINT DEFAULT 0,
    "status" TINYINT DEFAULT 0,
    "enable_remove_none_reader" TINYINT DEFAULT 0,
    "stream_key" VARCHAR(255),
    "enable_disable_none_reader" TINYINT DEFAULT 0,
    "extend_params" CLOB,
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "video_stream_proxy_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "video_stream_proxy_uk_stream_proxy_app_id_stream_identification" UNIQUE("app_id", "stream_identification")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_stream_proxy" IS '视频拉流代理信息表';
COMMENT ON COLUMN "video_stream_proxy"."app_id" IS '应用ID';
COMMENT ON COLUMN "video_stream_proxy"."created_by" IS '创建人';
COMMENT ON COLUMN "video_stream_proxy"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "video_stream_proxy"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_stream_proxy"."dst_url" IS '目标地址';
COMMENT ON COLUMN "video_stream_proxy"."enable_audio" IS '是否启用音频';
COMMENT ON COLUMN "video_stream_proxy"."enable_disable_none_reader" IS '无人观看时是否自动停用';
COMMENT ON COLUMN "video_stream_proxy"."enable_mp4" IS '是否启用MP4';
COMMENT ON COLUMN "video_stream_proxy"."enable_remove_none_reader" IS '无人观看时是否删除';
COMMENT ON COLUMN "video_stream_proxy"."extend_params" IS '扩展参数';
COMMENT ON COLUMN "video_stream_proxy"."ffmpeg_cmd_key" IS 'FFmpeg模板KEY';
COMMENT ON COLUMN "video_stream_proxy"."gb_identification" IS '国标唯一标识';
COMMENT ON COLUMN "video_stream_proxy"."id" IS '唯一标识符';
COMMENT ON COLUMN "video_stream_proxy"."media_identification" IS '媒体唯一标识';
COMMENT ON COLUMN "video_stream_proxy"."proxy_name" IS '代理名称';
COMMENT ON COLUMN "video_stream_proxy"."proxy_type" IS '代理类型';
COMMENT ON COLUMN "video_stream_proxy"."remark" IS '备注';
COMMENT ON COLUMN "video_stream_proxy"."rtp_type" IS 'RTSP拉流时的拉流方式（0：TCP，1：UDP，2：组播）';
COMMENT ON COLUMN "video_stream_proxy"."src_url" IS '源地址';
COMMENT ON COLUMN "video_stream_proxy"."status" IS '状态';
COMMENT ON COLUMN "video_stream_proxy"."stream_identification" IS '流唯一标识';
COMMENT ON COLUMN "video_stream_proxy"."stream_key" IS '拉流代理时ZLM返回的KEY，用于停止拉流代理';
COMMENT ON COLUMN "video_stream_proxy"."timeout_ms" IS '超时时间（毫秒）';
COMMENT ON COLUMN "video_stream_proxy"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "video_stream_proxy"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "video_stream_proxy"."url" IS '拉流地址';


CREATE TABLE "video_stream_push"
(
    "id" DECIMAL NOT NULL,
    "app_id" VARCHAR(64),
    "stream_identification" VARCHAR(255),
    "total_reader_count" INT,
    "origin_type" TINYINT,
    "origin_url" VARCHAR(255),
    "vhost" VARCHAR(255),
    "bytes_speed" NUMBER(22,0),
    "alive_second" BIGINT,
    "media_identification" VARCHAR(255),
    "server_id" VARCHAR(50),
    "push_time" TIMESTAMP(0),
    "status" TINYINT DEFAULT 0,
    "push_ing" TINYINT DEFAULT 0,
    "self" TINYINT DEFAULT 0,
    "extend_params" CLOB,
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "video_stream_push_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "video_stream_push_uk_stream_proxy_app_id_stream_identification" UNIQUE("app_id", "stream_identification")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_stream_push" IS '视频推流信息表';
COMMENT ON COLUMN "video_stream_push"."alive_second" IS '存活时间，单位秒';
COMMENT ON COLUMN "video_stream_push"."app_id" IS '应用ID';
COMMENT ON COLUMN "video_stream_push"."bytes_speed" IS '数据产生速度，单位byte/s';
COMMENT ON COLUMN "video_stream_push"."created_by" IS '创建人';
COMMENT ON COLUMN "video_stream_push"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "video_stream_push"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_stream_push"."extend_params" IS '扩展参数';
COMMENT ON COLUMN "video_stream_push"."id" IS 'id';
COMMENT ON COLUMN "video_stream_push"."media_identification" IS '媒体唯一标识';
COMMENT ON COLUMN "video_stream_push"."origin_type" IS '产生源类型';
COMMENT ON COLUMN "video_stream_push"."origin_url" IS '产生源的url';
COMMENT ON COLUMN "video_stream_push"."push_ing" IS '是否正在推流';
COMMENT ON COLUMN "video_stream_push"."push_time" IS '推流时间';
COMMENT ON COLUMN "video_stream_push"."remark" IS '备注';
COMMENT ON COLUMN "video_stream_push"."self" IS '是否自己平台的推流';
COMMENT ON COLUMN "video_stream_push"."server_id" IS '使用的服务ID';
COMMENT ON COLUMN "video_stream_push"."status" IS '状态';
COMMENT ON COLUMN "video_stream_push"."stream_identification" IS '流唯一标识';
COMMENT ON COLUMN "video_stream_push"."total_reader_count" IS '观看总人数';
COMMENT ON COLUMN "video_stream_push"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "video_stream_push"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "video_stream_push"."vhost" IS '音视频轨道';


CREATE TABLE "view_project"
(
    "id" DECIMAL NOT NULL,
    "project_identification" VARCHAR(255),
    "project_name" VARCHAR(255),
    "status" TINYINT DEFAULT (-1),
    "content" CLOB,
    "index_image_id" VARCHAR(255),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "view_project_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "view_project" IS '可视化项目表';
COMMENT ON COLUMN "view_project"."content" IS '存储数据';
COMMENT ON COLUMN "view_project"."created_by" IS '创建人';
COMMENT ON COLUMN "view_project"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "view_project"."created_time" IS '创建时间';
COMMENT ON COLUMN "view_project"."id" IS 'id';
COMMENT ON COLUMN "view_project"."index_image_id" IS '首页图片';
COMMENT ON COLUMN "view_project"."project_identification" IS '项目标识';
COMMENT ON COLUMN "view_project"."project_name" IS '项目名称';
COMMENT ON COLUMN "view_project"."remark" IS '备注';
COMMENT ON COLUMN "view_project"."status" IS '项目状态[1-发布,-1-未发布]';
COMMENT ON COLUMN "view_project"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "view_project"."updated_time" IS '最后修改时间';


CREATE TABLE "view_project_template"
(
    "id" DECIMAL NOT NULL,
    "template_identification" VARCHAR(255),
    "template_name" VARCHAR(255),
    "status" TINYINT DEFAULT (-1),
    "content" CLOB,
    "index_image_id" VARCHAR(255),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "view_project_template_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "view_project_template" IS '可视化项目模板表';
COMMENT ON COLUMN "view_project_template"."content" IS '存储数据';
COMMENT ON COLUMN "view_project_template"."created_by" IS '创建人';
COMMENT ON COLUMN "view_project_template"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "view_project_template"."created_time" IS '创建时间';
COMMENT ON COLUMN "view_project_template"."id" IS 'id';
COMMENT ON COLUMN "view_project_template"."index_image_id" IS '首页图片';
COMMENT ON COLUMN "view_project_template"."remark" IS '备注';
COMMENT ON COLUMN "view_project_template"."status" IS '项目状态[1-发布,-1-未发布]';
COMMENT ON COLUMN "view_project_template"."template_identification" IS '模版标识';
COMMENT ON COLUMN "view_project_template"."template_name" IS '模版名称';
COMMENT ON COLUMN "view_project_template"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "view_project_template"."updated_time" IS '最后修改时间';




-- ============================================================
-- 以下表为 MySQL 同步:补齐 37 张表 (CA 证书 / device 扩展 / mobile_space / OTA / plugin / rule / video)
-- ============================================================
-- ----------------------------
-- Table structure for ca_cert_license
-- ----------------------------
DROP TABLE IF EXISTS "ca_cert_license";
CREATE TABLE "ca_cert_license"
(
    "id" BIGINT NOT NULL,
    "cert_name" VARCHAR(50) DEFAULT '',
    "issuer_common_name" VARCHAR(255) DEFAULT '',
    "serial_number" VARCHAR(100) DEFAULT '',
    "common_name" VARCHAR(50) DEFAULT '',
    "organization" VARCHAR(50) DEFAULT '',
    "organizational_unit" VARCHAR(50) DEFAULT '',
    "country_name" VARCHAR(50) DEFAULT '',
    "province_name" VARCHAR(50) DEFAULT '',
    "locality_name" VARCHAR(50) DEFAULT '',
    "email" VARCHAR(50) DEFAULT '',
    "license_base64" CLOB,
    "business_license_fileid" VARCHAR(100) DEFAULT '',
    "authorization_cert_fileid" VARCHAR(100) DEFAULT '',
    "ca_cert_pem" CLOB,
    "cert_fileid" VARCHAR(100) DEFAULT '',
    "algorithm" TINYINT NOT NULL DEFAULT '0',
    "sign_algorithm" TINYINT NOT NULL DEFAULT '0',
    "param1" VARCHAR(2048) DEFAULT '',
    "param2" VARCHAR(2048) DEFAULT '',
    "extend_params" CLOB,
    "not_before" TIMESTAMP(0) DEFAULT NULL,
    "not_after" TIMESTAMP(0) DEFAULT NULL,
    "revoke_time" TIMESTAMP(0) DEFAULT NULL,
    "revoke_reason" VARCHAR(255) DEFAULT '',
    "state" TINYINT NOT NULL DEFAULT '0',
    "thumbprint" VARCHAR(255) DEFAULT '',
    "remark" VARCHAR(500) DEFAULT '',
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "ca_cert_license_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "ca_cert_license" IS 'CA许可证证书表';
COMMENT ON COLUMN "ca_cert_license"."algorithm" IS '算法(0-RSA、1-EC)';
COMMENT ON COLUMN "ca_cert_license"."authorization_cert_fileid" IS '授权证书文件ID';
COMMENT ON COLUMN "ca_cert_license"."business_license_fileid" IS '营业执照文件ID';
COMMENT ON COLUMN "ca_cert_license"."ca_cert_pem" IS 'CA证书(PEM格式)';
COMMENT ON COLUMN "ca_cert_license"."cert_fileid" IS '证书文件ID';
COMMENT ON COLUMN "ca_cert_license"."cert_name" IS '证书名称';
COMMENT ON COLUMN "ca_cert_license"."common_name" IS '通用名称';
COMMENT ON COLUMN "ca_cert_license"."country_name" IS '国家';
COMMENT ON COLUMN "ca_cert_license"."created_by" IS '创建人';
COMMENT ON COLUMN "ca_cert_license"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "ca_cert_license"."created_time" IS '创建时间';
COMMENT ON COLUMN "ca_cert_license"."deleted" IS '逻辑删除标识(0-未删除、1-已删除)';
COMMENT ON COLUMN "ca_cert_license"."email" IS '邮箱';
COMMENT ON COLUMN "ca_cert_license"."extend_params" IS '扩展信息';
COMMENT ON COLUMN "ca_cert_license"."id" IS 'id';
COMMENT ON COLUMN "ca_cert_license"."issuer_common_name" IS '颁发者通用名称';
COMMENT ON COLUMN "ca_cert_license"."license_base64" IS 'License文件内容(Base64编码)';
COMMENT ON COLUMN "ca_cert_license"."locality_name" IS '城市';
COMMENT ON COLUMN "ca_cert_license"."not_after" IS '证书过期时间';
COMMENT ON COLUMN "ca_cert_license"."not_before" IS '证书颁发时间';
COMMENT ON COLUMN "ca_cert_license"."organization" IS '组织名称';
COMMENT ON COLUMN "ca_cert_license"."organizational_unit" IS '组织单位名称';
COMMENT ON COLUMN "ca_cert_license"."param1" IS 'RSA公钥n或ECC Point x';
COMMENT ON COLUMN "ca_cert_license"."param2" IS 'RSA公钥e或ECC Point y';
COMMENT ON COLUMN "ca_cert_license"."province_name" IS '省份/州';
COMMENT ON COLUMN "ca_cert_license"."remark" IS '备注';
COMMENT ON COLUMN "ca_cert_license"."revoke_reason" IS '撤销原因';
COMMENT ON COLUMN "ca_cert_license"."revoke_time" IS '证书撤销时间';
COMMENT ON COLUMN "ca_cert_license"."serial_number" IS '证书序列号';
COMMENT ON COLUMN "ca_cert_license"."sign_algorithm" IS '签名算法(0-SHA256withRSA)';
COMMENT ON COLUMN "ca_cert_license"."state" IS '证书状态(0-待完善、1-已颁发、2-已撤销)';
COMMENT ON COLUMN "ca_cert_license"."thumbprint" IS '证书指纹(SHA-256)';
COMMENT ON COLUMN "ca_cert_license"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "ca_cert_license"."updated_time" IS '最后修改时间';

CREATE OR REPLACE INDEX "ca_cert_license_idx_serial_number" ON "ca_cert_license"("serial_number" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for ca_cert_audit_log
-- ----------------------------
DROP TABLE IF EXISTS "ca_cert_audit_log";
CREATE TABLE "ca_cert_audit_log"
(
    "id" BIGINT NOT NULL,
    "ca_id" BIGINT DEFAULT NULL,
    "ca_serial_number" VARCHAR(100) DEFAULT NULL,
    "type" VARCHAR(50) NOT NULL,
    "detail" CLOB,
    "created_org_id" BIGINT DEFAULT NULL,
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) DEFAULT NULL,
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) DEFAULT NULL,
    "deleted" INT DEFAULT '0',
    CONSTRAINT "ca_cert_audit_log_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "ca_cert_audit_log" IS 'CA 证书审计日志';
COMMENT ON COLUMN "ca_cert_audit_log"."ca_id" IS '关联 CA 证书 ID';
COMMENT ON COLUMN "ca_cert_audit_log"."ca_serial_number" IS 'CA 证书序列号';
COMMENT ON COLUMN "ca_cert_audit_log"."created_by" IS '创建人';
COMMENT ON COLUMN "ca_cert_audit_log"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "ca_cert_audit_log"."created_time" IS '创建时间';
COMMENT ON COLUMN "ca_cert_audit_log"."deleted" IS '是否删除(0-未删除/1-已删除)';
COMMENT ON COLUMN "ca_cert_audit_log"."detail" IS '详情(JSON 或自由文本)';
COMMENT ON COLUMN "ca_cert_audit_log"."id" IS 'id';
COMMENT ON COLUMN "ca_cert_audit_log"."type" IS '动作类型: IMPORT/ISSUE/REVOKE/DOWNLOAD_PACK/SSL_TEST';
COMMENT ON COLUMN "ca_cert_audit_log"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "ca_cert_audit_log"."updated_time" IS '最后修改时间';

CREATE OR REPLACE INDEX "ca_cert_audit_log_idx_ca_id" ON "ca_cert_audit_log"("ca_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "ca_cert_audit_log_idx_type_created" ON "ca_cert_audit_log"("type" ASC,"created_time" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for device_acl_rule
-- ----------------------------
DROP TABLE IF EXISTS "device_acl_rule";
CREATE TABLE "device_acl_rule"
(
    "id" BIGINT NOT NULL,
    "rule_name" VARCHAR(100) DEFAULT '',
    "product_identification" VARCHAR(100) NOT NULL DEFAULT '',
    "device_identification" VARCHAR(255) DEFAULT '',
    "rule_level" TINYINT NOT NULL DEFAULT '0',
    "action_type" TINYINT NOT NULL DEFAULT '0',
    "priority" INT NOT NULL DEFAULT '500',
    "topic_pattern" VARCHAR(255) DEFAULT '',
    "ip_whitelist" VARCHAR(255) DEFAULT '',
    "decision" TINYINT NOT NULL DEFAULT '1',
    "enabled" TINYINT NOT NULL DEFAULT '0',
    "remark" VARCHAR(500) DEFAULT '',
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "device_acl_rule_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "device_acl_rule" IS '设备访问控制(ACL)规则表';
COMMENT ON COLUMN "device_acl_rule"."action_type" IS '动作类型(0:全部、1:发布、2:订阅、3:取消订阅)';
COMMENT ON COLUMN "device_acl_rule"."created_by" IS '创建人';
COMMENT ON COLUMN "device_acl_rule"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "device_acl_rule"."created_time" IS '创建时间';
COMMENT ON COLUMN "device_acl_rule"."decision" IS '决策(0:拒绝、1:允许)';
COMMENT ON COLUMN "device_acl_rule"."deleted" IS '逻辑删除标识(0-未删除、1-已删除)';
COMMENT ON COLUMN "device_acl_rule"."device_identification" IS '设备标识';
COMMENT ON COLUMN "device_acl_rule"."enabled" IS '是否启用';
COMMENT ON COLUMN "device_acl_rule"."id" IS 'id';
COMMENT ON COLUMN "device_acl_rule"."ip_whitelist" IS 'IP白名单地址(多个用逗号分隔)';
COMMENT ON COLUMN "device_acl_rule"."priority" IS '规则优先级(0-1000,值越小优先级越高)';
COMMENT ON COLUMN "device_acl_rule"."product_identification" IS '产品标识';
COMMENT ON COLUMN "device_acl_rule"."remark" IS '备注';
COMMENT ON COLUMN "device_acl_rule"."rule_level" IS '规则级别(0:产品级、1:设备级)';
COMMENT ON COLUMN "device_acl_rule"."rule_name" IS '规则名称';
COMMENT ON COLUMN "device_acl_rule"."topic_pattern" IS 'MQTT主题模式(支持通配符)';
COMMENT ON COLUMN "device_acl_rule"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "device_acl_rule"."updated_time" IS '最后修改时间';

CREATE OR REPLACE INDEX "device_acl_rule_idx_device_identification" ON "device_acl_rule"("device_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "device_acl_rule_idx_product_identification" ON "device_acl_rule"("product_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for device_group
-- ----------------------------
DROP TABLE IF EXISTS "device_group";
CREATE TABLE "device_group"
(
    "id" BIGINT NOT NULL,
    "parent_id" BIGINT NOT NULL,
    "sort_value" INT DEFAULT '1',
    "app_id" VARCHAR(64) NOT NULL DEFAULT '',
    "group_name" VARCHAR(255) DEFAULT '',
    "type" TINYINT NOT NULL DEFAULT '0',
    "state" TINYINT NOT NULL DEFAULT 1,
    "description" VARCHAR(500) DEFAULT '',
    "remark" VARCHAR(500) DEFAULT '',
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "device_group_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "device_group" IS '设备分组表';
COMMENT ON COLUMN "device_group"."app_id" IS '应用ID';
COMMENT ON COLUMN "device_group"."created_by" IS '创建人';
COMMENT ON COLUMN "device_group"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "device_group"."created_time" IS '创建时间';
COMMENT ON COLUMN "device_group"."deleted" IS '逻辑删除标识(0-未删除、1-已删除)';
COMMENT ON COLUMN "device_group"."description" IS '分组描述';
COMMENT ON COLUMN "device_group"."group_name" IS '分组名称';
COMMENT ON COLUMN "device_group"."id" IS 'id';
COMMENT ON COLUMN "device_group"."parent_id" IS '父级ID';
COMMENT ON COLUMN "device_group"."remark" IS '备注';
COMMENT ON COLUMN "device_group"."sort_value" IS '排序;默认升序';
COMMENT ON COLUMN "device_group"."state" IS '状态;[0-禁用 1-启用]';
COMMENT ON COLUMN "device_group"."type" IS '分组类型';
COMMENT ON COLUMN "device_group"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "device_group"."updated_time" IS '最后修改时间';

CREATE OR REPLACE INDEX "device_group_idx_parent_id" ON "device_group"("parent_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for device_group_rel
-- ----------------------------
DROP TABLE IF EXISTS "device_group_rel";
CREATE TABLE "device_group_rel"
(
    "id" BIGINT NOT NULL,
    "group_id" BIGINT NOT NULL,
    "device_identification" VARCHAR(255) NOT NULL DEFAULT '',
    "remark" VARCHAR(500) DEFAULT '',
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "device_group_rel_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "device_group_rel_uk_group_device" UNIQUE("group_id","device_identification")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "device_group_rel" IS '设备分组资源关系表';
COMMENT ON COLUMN "device_group_rel"."created_by" IS '创建人';
COMMENT ON COLUMN "device_group_rel"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "device_group_rel"."created_time" IS '创建时间';
COMMENT ON COLUMN "device_group_rel"."deleted" IS '逻辑删除标识(0-未删除、1-已删除)';
COMMENT ON COLUMN "device_group_rel"."device_identification" IS '设备标识';
COMMENT ON COLUMN "device_group_rel"."group_id" IS '分组ID;#device_group';
COMMENT ON COLUMN "device_group_rel"."id" IS 'id';
COMMENT ON COLUMN "device_group_rel"."remark" IS '备注';
COMMENT ON COLUMN "device_group_rel"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "device_group_rel"."updated_time" IS '最后修改时间';

CREATE OR REPLACE INDEX "device_group_rel_idx_group_id" ON "device_group_rel"("group_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "device_group_rel_idx_device_identification" ON "device_group_rel"("device_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for mobile_space
-- ----------------------------
DROP TABLE IF EXISTS "mobile_space";
CREATE TABLE "mobile_space"
(
    "id" BIGINT NOT NULL,
    "space_name" VARCHAR(255) DEFAULT '',
    "full_name" VARCHAR(500) NOT NULL DEFAULT '',
    "latitude" DECIMAL(10,7) NOT NULL,
    "longitude" DECIMAL(10,7) NOT NULL,
    "province_code" VARCHAR(50) NOT NULL DEFAULT '',
    "city_code" VARCHAR(50) NOT NULL DEFAULT '',
    "region_code" VARCHAR(50) NOT NULL DEFAULT '',
    "remark" VARCHAR(500) DEFAULT '',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "mobile_space_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "mobile_space" IS '移动端-空间表';
COMMENT ON COLUMN "mobile_space"."city_code" IS '市编码';
COMMENT ON COLUMN "mobile_space"."created_by" IS '创建人';
COMMENT ON COLUMN "mobile_space"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "mobile_space"."created_time" IS '创建时间';
COMMENT ON COLUMN "mobile_space"."full_name" IS '位置名称';
COMMENT ON COLUMN "mobile_space"."id" IS 'id';
COMMENT ON COLUMN "mobile_space"."latitude" IS '纬度';
COMMENT ON COLUMN "mobile_space"."longitude" IS '经度';
COMMENT ON COLUMN "mobile_space"."province_code" IS '省,直辖市编码';
COMMENT ON COLUMN "mobile_space"."region_code" IS '区县';
COMMENT ON COLUMN "mobile_space"."remark" IS '备注';
COMMENT ON COLUMN "mobile_space"."space_name" IS '空间名称';
COMMENT ON COLUMN "mobile_space"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "mobile_space"."updated_time" IS '最后修改时间';

-- ----------------------------
-- Table structure for mobile_space_device
-- ----------------------------
DROP TABLE IF EXISTS "mobile_space_device";
CREATE TABLE "mobile_space_device"
(
    "id" BIGINT NOT NULL,
    "space_id" BIGINT NOT NULL,
    "product_identification" VARCHAR(100) NOT NULL DEFAULT '',
    "device_identification" VARCHAR(255) NOT NULL DEFAULT '',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "mobile_space_device_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "mobile_space_device_uniq_space_product_device" UNIQUE("space_id","product_identification","device_identification")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "mobile_space_device" IS '空间设备绑定表';
COMMENT ON COLUMN "mobile_space_device"."created_by" IS '创建人';
COMMENT ON COLUMN "mobile_space_device"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "mobile_space_device"."created_time" IS '创建时间';
COMMENT ON COLUMN "mobile_space_device"."device_identification" IS '设备标识';
COMMENT ON COLUMN "mobile_space_device"."id" IS 'id';
COMMENT ON COLUMN "mobile_space_device"."product_identification" IS '产品标识';
COMMENT ON COLUMN "mobile_space_device"."space_id" IS '空间ID';
COMMENT ON COLUMN "mobile_space_device"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "mobile_space_device"."updated_time" IS '最后修改时间';

-- ----------------------------
-- Table structure for mobile_space_member
-- ----------------------------
DROP TABLE IF EXISTS "mobile_space_member";
CREATE TABLE "mobile_space_member"
(
    "id" BIGINT NOT NULL,
    "space_id" BIGINT NOT NULL,
    "member_id" BIGINT NOT NULL,
    "member_type" TINYINT NOT NULL DEFAULT '0',
    "remark" VARCHAR(500) DEFAULT '',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "mobile_space_member_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "mobile_space_member_idx_uniq_space_member" UNIQUE("space_id","member_id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON COLUMN "mobile_space_member"."created_by" IS '创建人';
COMMENT ON COLUMN "mobile_space_member"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "mobile_space_member"."created_time" IS '创建时间';
COMMENT ON COLUMN "mobile_space_member"."id" IS '主键';
COMMENT ON COLUMN "mobile_space_member"."member_id" IS '人员ID';
COMMENT ON COLUMN "mobile_space_member"."member_type" IS '人员类型:( 0:成员、1:管理员、 2:所有者)';
COMMENT ON COLUMN "mobile_space_member"."remark" IS '备注';
COMMENT ON COLUMN "mobile_space_member"."space_id" IS '空间ID';
COMMENT ON COLUMN "mobile_space_member"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "mobile_space_member"."updated_time" IS '最后修改时间';

-- ----------------------------
-- Table structure for ota_upgrade_targets
-- ----------------------------
DROP TABLE IF EXISTS "ota_upgrade_targets";
CREATE TABLE "ota_upgrade_targets"
(
    "id" BIGINT NOT NULL,
    "task_id" BIGINT NOT NULL,
    "target_value" VARCHAR(100) NOT NULL DEFAULT '',
    "target_status" SMALLINT NOT NULL DEFAULT '0',
    "remark" VARCHAR(255) DEFAULT '',
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "ota_upgrade_targets_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "ota_upgrade_targets" IS 'OTA升级目标表';
COMMENT ON COLUMN "ota_upgrade_targets"."created_by" IS '创建人';
COMMENT ON COLUMN "ota_upgrade_targets"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "ota_upgrade_targets"."created_time" IS '创建时间';
COMMENT ON COLUMN "ota_upgrade_targets"."deleted" IS '逻辑删除标识(0-未删除、1-已删除)';
COMMENT ON COLUMN "ota_upgrade_targets"."id" IS '主键';
COMMENT ON COLUMN "ota_upgrade_targets"."remark" IS '描述';
COMMENT ON COLUMN "ota_upgrade_targets"."target_status" IS '目标状态(0:待推送,1:推送中,2:推送成功,3:推送失败)';
COMMENT ON COLUMN "ota_upgrade_targets"."target_value" IS '目标值(产品标识/设备标识/分组ID/省市区域编码)';
COMMENT ON COLUMN "ota_upgrade_targets"."task_id" IS '任务ID';
COMMENT ON COLUMN "ota_upgrade_targets"."updated_by" IS '更新人';
COMMENT ON COLUMN "ota_upgrade_targets"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "ota_upgrade_targets_idx_task_id" ON "ota_upgrade_targets"("task_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "ota_upgrade_targets_idx_target_value" ON "ota_upgrade_targets"("target_value" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "ota_upgrade_targets_idx_target_status" ON "ota_upgrade_targets"("target_status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for plugin_info
-- ----------------------------
DROP TABLE IF EXISTS "plugin_info";
CREATE TABLE "plugin_info"
(
    "id" BIGINT NOT NULL,
    "app_id" VARCHAR(64) NOT NULL DEFAULT '',
    "plugin_identification" VARCHAR(255) NOT NULL DEFAULT '',
    "plugin_code" VARCHAR(255) NOT NULL DEFAULT '',
    "plugin_name" VARCHAR(255) NOT NULL DEFAULT '',
    "version" VARCHAR(50) NOT NULL DEFAULT '',
    "description" CLOB,
    "file_id" VARCHAR(255) NOT NULL DEFAULT '',
    "file_size" DECIMAL(10,2) NOT NULL DEFAULT '0.00',
    "status" TINYINT NOT NULL DEFAULT '0',
    "level" TINYINT NOT NULL DEFAULT '0',
    "type" TINYINT NOT NULL DEFAULT '0',
    "run_mode" TINYINT NOT NULL DEFAULT '0',
    "license_type" VARCHAR(50) DEFAULT '',
    "license_key" VARCHAR(255) DEFAULT '',
    "valid_until" date DEFAULT NULL,
    "file_hash" VARCHAR(255) DEFAULT '',
    "scan_status" VARCHAR(50) DEFAULT 'PENDING',
    "scan_report_file_id" VARCHAR(255) DEFAULT '',
    "scan_date" TIMESTAMP(0) DEFAULT NULL,
    "scan_summary" CLOB,
    "extend_params" CLOB,
    "remark" VARCHAR(500) DEFAULT '',
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "plugin_info_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "plugin_info" IS '插件信息表';
COMMENT ON COLUMN "plugin_info"."app_id" IS '应用ID，所属应用场景';
COMMENT ON COLUMN "plugin_info"."created_by" IS '创建人';
COMMENT ON COLUMN "plugin_info"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "plugin_info"."created_time" IS '创建时间';
COMMENT ON COLUMN "plugin_info"."description" IS '插件描述，取自 pluginMeta.properties';
COMMENT ON COLUMN "plugin_info"."extend_params" IS '扩展参数（预留）';
COMMENT ON COLUMN "plugin_info"."file_hash" IS '文件的哈希值，用于验证文件的完整性（如 SHA-256）';
COMMENT ON COLUMN "plugin_info"."file_id" IS '文件在服务器上的唯一标识，用于查询文件临时路径';
COMMENT ON COLUMN "plugin_info"."file_size" IS '文件大小（MB）';
COMMENT ON COLUMN "plugin_info"."id" IS '主键';
COMMENT ON COLUMN "plugin_info"."level" IS '插件级别：0-系统级，1-用户级';
COMMENT ON COLUMN "plugin_info"."license_key" IS '许可证密钥或证书';
COMMENT ON COLUMN "plugin_info"."license_type" IS '许可证类型（如GPL, MIT, 商业等）';
COMMENT ON COLUMN "plugin_info"."plugin_code" IS '插件代码标识，取自 pluginMeta.properties';
COMMENT ON COLUMN "plugin_info"."plugin_identification" IS '插件唯一标识，自动生成：plugin_code + version';
COMMENT ON COLUMN "plugin_info"."plugin_name" IS '插件名称，文件名';
COMMENT ON COLUMN "plugin_info"."remark" IS '备注';
COMMENT ON COLUMN "plugin_info"."run_mode" IS '运行模式：0-单节点，1-集群';
COMMENT ON COLUMN "plugin_info"."scan_date" IS '最后一次扫描的日期';
COMMENT ON COLUMN "plugin_info"."scan_report_file_id" IS '扫描报告的文件ID';
COMMENT ON COLUMN "plugin_info"."scan_status" IS '扫描状态：PENDING, SUCCESS, FAILED';
COMMENT ON COLUMN "plugin_info"."scan_summary" IS '扫描摘要（如发现的漏洞数目等）';
COMMENT ON COLUMN "plugin_info"."status" IS '状态';
COMMENT ON COLUMN "plugin_info"."type" IS '插件类型：0-设备协议插件，1-业务插件';
COMMENT ON COLUMN "plugin_info"."updated_by" IS '更新人';
COMMENT ON COLUMN "plugin_info"."updated_time" IS '更新时间';
COMMENT ON COLUMN "plugin_info"."valid_until" IS '许可证有效期';
COMMENT ON COLUMN "plugin_info"."version" IS '插件版本，取自 pluginMeta.properties';

-- ----------------------------
-- Table structure for plugin_instance
-- ----------------------------
DROP TABLE IF EXISTS "plugin_instance";
CREATE TABLE "plugin_instance"
(
    "id" BIGINT NOT NULL,
    "instance_identification" VARCHAR(255) NOT NULL DEFAULT '',
    "instance_name" VARCHAR(255) NOT NULL DEFAULT '',
    "application_name" VARCHAR(255) NOT NULL DEFAULT '',
    "weight" INT NOT NULL DEFAULT '0',
    "healthy" TINYINT NOT NULL DEFAULT '0',
    "enabled" TINYINT NOT NULL DEFAULT '0',
    "ephemeral" TINYINT NOT NULL DEFAULT '0',
    "cluster_name" VARCHAR(50) NOT NULL DEFAULT '',
    "heart_beat_interval" BIGINT DEFAULT NULL,
    "heart_beat_time_out" BIGINT DEFAULT NULL,
    "ip_delete_time_out" BIGINT DEFAULT NULL,
    "machine_ip" VARCHAR(50) NOT NULL DEFAULT '',
    "machine_port" VARCHAR(20) NOT NULL DEFAULT '',
    "port_range_start" INT NOT NULL,
    "port_range_end" INT NOT NULL,
    "extend_params" CLOB,
    "remark" VARCHAR(500) DEFAULT '',
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "plugin_instance_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "plugin_instance_idx_instance_identification" UNIQUE("instance_identification")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "plugin_instance" IS '插件实例信息表';
COMMENT ON COLUMN "plugin_instance"."application_name" IS '应用名称，SpringBoot应用名称';
COMMENT ON COLUMN "plugin_instance"."cluster_name" IS '实例所在集群名称';
COMMENT ON COLUMN "plugin_instance"."created_by" IS '创建人';
COMMENT ON COLUMN "plugin_instance"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "plugin_instance"."created_time" IS '创建时间';
COMMENT ON COLUMN "plugin_instance"."enabled" IS '实例是否启用';
COMMENT ON COLUMN "plugin_instance"."ephemeral" IS '实例是否为临时实例';
COMMENT ON COLUMN "plugin_instance"."extend_params" IS '扩展参数（预留）';
COMMENT ON COLUMN "plugin_instance"."healthy" IS '实例的健康状态';
COMMENT ON COLUMN "plugin_instance"."heart_beat_interval" IS '实例心跳间隔时间(毫秒)';
COMMENT ON COLUMN "plugin_instance"."heart_beat_time_out" IS '实例心跳超时时间(毫秒)';
COMMENT ON COLUMN "plugin_instance"."id" IS '主键';
COMMENT ON COLUMN "plugin_instance"."instance_identification" IS '实例唯一标识';
COMMENT ON COLUMN "plugin_instance"."instance_name" IS '实例名称';
COMMENT ON COLUMN "plugin_instance"."ip_delete_time_out" IS '实例IP删除超时时间(毫秒)';
COMMENT ON COLUMN "plugin_instance"."machine_ip" IS '实例机器IP地址';
COMMENT ON COLUMN "plugin_instance"."machine_port" IS '实例机器端口';
COMMENT ON COLUMN "plugin_instance"."port_range_end" IS '实例可用端口范围结束值';
COMMENT ON COLUMN "plugin_instance"."port_range_start" IS '实例可用端口范围起始值';
COMMENT ON COLUMN "plugin_instance"."remark" IS '备注';
COMMENT ON COLUMN "plugin_instance"."updated_by" IS '更新人';
COMMENT ON COLUMN "plugin_instance"."updated_time" IS '更新时间';
COMMENT ON COLUMN "plugin_instance"."weight" IS '实例的权重';

-- ----------------------------
-- Table structure for plugin_instance_heartbeat
-- ----------------------------
DROP TABLE IF EXISTS "plugin_instance_heartbeat";
CREATE TABLE "plugin_instance_heartbeat"
(
    "id" BIGINT NOT NULL,
    "instance_identification" VARCHAR(255) NOT NULL DEFAULT '',
    "machine_ip" VARCHAR(45) NOT NULL DEFAULT '',
    "last_heartbeat_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "heartbeat_interval" INT NOT NULL DEFAULT '60',
    "status" TINYINT NOT NULL DEFAULT '0',
    "heartbeat_message" VARCHAR(500) DEFAULT '',
    "extend_params" CLOB,
    "remark" VARCHAR(500) DEFAULT '',
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "plugin_instance_heartbeat_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "plugin_instance_heartbeat" IS '插件实例心跳表';
COMMENT ON COLUMN "plugin_instance_heartbeat"."created_by" IS '创建人';
COMMENT ON COLUMN "plugin_instance_heartbeat"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "plugin_instance_heartbeat"."created_time" IS '创建时间';
COMMENT ON COLUMN "plugin_instance_heartbeat"."extend_params" IS '扩展参数（预留）';
COMMENT ON COLUMN "plugin_instance_heartbeat"."heartbeat_interval" IS '心跳间隔时间（秒）';
COMMENT ON COLUMN "plugin_instance_heartbeat"."heartbeat_message" IS '心跳详细信息';
COMMENT ON COLUMN "plugin_instance_heartbeat"."id" IS '主键';
COMMENT ON COLUMN "plugin_instance_heartbeat"."instance_identification" IS '实例唯一标识，关联 plugin_instance 表的 instance_identification';
COMMENT ON COLUMN "plugin_instance_heartbeat"."last_heartbeat_time" IS '上次心跳时间';
COMMENT ON COLUMN "plugin_instance_heartbeat"."machine_ip" IS '插件运行所在的机器 IP 地址';
COMMENT ON COLUMN "plugin_instance_heartbeat"."remark" IS '备注';
COMMENT ON COLUMN "plugin_instance_heartbeat"."status" IS '心跳状态：0-正常，1-异常';
COMMENT ON COLUMN "plugin_instance_heartbeat"."updated_by" IS '更新人';
COMMENT ON COLUMN "plugin_instance_heartbeat"."updated_time" IS '更新时间';

-- ----------------------------
-- Table structure for plugin_instance_mapping
-- ----------------------------
DROP TABLE IF EXISTS "plugin_instance_mapping";
CREATE TABLE "plugin_instance_mapping"
(
    "id" BIGINT NOT NULL,
    "plugin_identification" VARCHAR(255) NOT NULL DEFAULT '',
    "instance_identification" VARCHAR(255) NOT NULL DEFAULT '',
    "port" INT NOT NULL,
    "port_type" VARCHAR(50) DEFAULT '',
    "status" TINYINT NOT NULL DEFAULT '0',
    "remark" VARCHAR(500) DEFAULT '',
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    CONSTRAINT "plugin_instance_mapping_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "plugin_instance_mapping_idx_plugin_instance_port" UNIQUE("plugin_identification","instance_identification","port")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "plugin_instance_mapping" IS '插件与实例及端口管理表';
COMMENT ON COLUMN "plugin_instance_mapping"."created_by" IS '创建人';
COMMENT ON COLUMN "plugin_instance_mapping"."created_time" IS '创建时间';
COMMENT ON COLUMN "plugin_instance_mapping"."id" IS '主键';
COMMENT ON COLUMN "plugin_instance_mapping"."instance_identification" IS '实例唯一标识，关联 plugin_instance 表的 instance_identification';
COMMENT ON COLUMN "plugin_instance_mapping"."plugin_identification" IS '插件唯一标识，关联 plugin_info 表的 plugin_identification';
COMMENT ON COLUMN "plugin_instance_mapping"."port" IS '插件在该实例上使用的端口号';
COMMENT ON COLUMN "plugin_instance_mapping"."port_type" IS '端口类型或用途（如 HTTP, HTTPS, 管理端口等）';
COMMENT ON COLUMN "plugin_instance_mapping"."remark" IS '备注';
COMMENT ON COLUMN "plugin_instance_mapping"."status" IS '端口：0-正常，1-异常';
COMMENT ON COLUMN "plugin_instance_mapping"."updated_by" IS '更新人';
COMMENT ON COLUMN "plugin_instance_mapping"."updated_time" IS '更新时间';

-- ----------------------------
-- Table structure for plugin_log
-- ----------------------------
DROP TABLE IF EXISTS "plugin_log";
CREATE TABLE "plugin_log"
(
    "id" BIGINT NOT NULL,
    "plugin_identification" VARCHAR(255) NOT NULL DEFAULT '',
    "instance_identification" VARCHAR(255) NOT NULL DEFAULT '',
    "log_level" TINYINT NOT NULL DEFAULT '0',
    "message" CLOB NOT NULL,
    "log_timestamp" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "node_id" VARCHAR(255) DEFAULT '',
    "thread_name" VARCHAR(255) DEFAULT '',
    "exception_stacktrace" CLOB,
    "context_info" CLOB,
    "error_code" VARCHAR(100) DEFAULT '',
    "execution_time" DECIMAL(10,2) DEFAULT NULL,
    "remark" VARCHAR(500) DEFAULT '',
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "plugin_log_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "plugin_log" IS '插件运行日志表';
COMMENT ON COLUMN "plugin_log"."context_info" IS '上下文信息，可能包括请求ID、用户ID等';
COMMENT ON COLUMN "plugin_log"."created_by" IS '创建人';
COMMENT ON COLUMN "plugin_log"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "plugin_log"."created_time" IS '创建时间';
COMMENT ON COLUMN "plugin_log"."error_code" IS '错误代码（如果适用）';
COMMENT ON COLUMN "plugin_log"."exception_stacktrace" IS '异常堆栈信息（如有异常）';
COMMENT ON COLUMN "plugin_log"."execution_time" IS '执行时间（毫秒）';
COMMENT ON COLUMN "plugin_log"."id" IS '主键';
COMMENT ON COLUMN "plugin_log"."instance_identification" IS '实例唯一标识，关联 plugin_instance 表的 instance_identification';
COMMENT ON COLUMN "plugin_log"."log_level" IS '日志级别：0-DEBUG，1-INFO，2-WARN，3-ERROR';
COMMENT ON COLUMN "plugin_log"."log_timestamp" IS '日志记录时间';
COMMENT ON COLUMN "plugin_log"."message" IS '日志消息内容';
COMMENT ON COLUMN "plugin_log"."node_id" IS '运行节点标识（集群模式下用以区分具体节点）';
COMMENT ON COLUMN "plugin_log"."plugin_identification" IS '插件唯一标识，关联 plugin_info 表的 plugin_identification';
COMMENT ON COLUMN "plugin_log"."remark" IS '备注';
COMMENT ON COLUMN "plugin_log"."thread_name" IS '运行时线程名称';
COMMENT ON COLUMN "plugin_log"."updated_by" IS '更新人';
COMMENT ON COLUMN "plugin_log"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "plugin_log_idx_plugin_identification" ON "plugin_log"("plugin_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "plugin_log_idx_instance_identification" ON "plugin_log"("instance_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "plugin_log_idx_node_id" ON "plugin_log"("node_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for rule_action_execution_log
-- ----------------------------
DROP TABLE IF EXISTS "rule_action_execution_log";
CREATE TABLE "rule_action_execution_log"
(
    "id" BIGINT NOT NULL,
    "rule_execution_id" BIGINT NOT NULL,
    "action_type" SMALLINT NOT NULL,
    "action_content" CLOB NOT NULL,
    "result" TINYINT NOT NULL,
    "start_time" TIMESTAMP(0) NOT NULL,
    "end_time" TIMESTAMP(0) DEFAULT NULL,
    "remark" VARCHAR(255) DEFAULT '',
    "extend_params" CLOB,
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "rule_action_execution_log_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_action_execution_log" IS '规则动作执行日志表';
COMMENT ON COLUMN "rule_action_execution_log"."action_content" IS '动作内容';
COMMENT ON COLUMN "rule_action_execution_log"."action_type" IS '动作类型：0-命令下发，1-触发告警，2-数据转发';
COMMENT ON COLUMN "rule_action_execution_log"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_action_execution_log"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_action_execution_log"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_action_execution_log"."end_time" IS '动作结束执行时间';
COMMENT ON COLUMN "rule_action_execution_log"."extend_params" IS '扩展参数（文本格式）';
COMMENT ON COLUMN "rule_action_execution_log"."id" IS '主键';
COMMENT ON COLUMN "rule_action_execution_log"."remark" IS '描述';
COMMENT ON COLUMN "rule_action_execution_log"."result" IS '动作是否执行成功';
COMMENT ON COLUMN "rule_action_execution_log"."rule_execution_id" IS '规则执行日志ID（外键）';
COMMENT ON COLUMN "rule_action_execution_log"."start_time" IS '动作开始执行时间';
COMMENT ON COLUMN "rule_action_execution_log"."updated_by" IS '更新人';
COMMENT ON COLUMN "rule_action_execution_log"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "rule_action_execution_log_idx_rule_execution_id" ON "rule_action_execution_log"("rule_execution_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for rule_bridge_execution_step
-- ----------------------------
DROP TABLE IF EXISTS "rule_bridge_execution_step";
CREATE TABLE "rule_bridge_execution_step"
(
    "id" BIGINT NOT NULL,
    "trace_id" VARCHAR(64) NOT NULL DEFAULT '',
    "bridge_rule_id" BIGINT DEFAULT NULL,
    "step_no" INT NOT NULL DEFAULT '0',
    "step_type" VARCHAR(30) NOT NULL DEFAULT '',
    "step_name" VARCHAR(100) NOT NULL DEFAULT '',
    "status" CHAR(2) NOT NULL DEFAULT '00',
    "latency_ms" INT DEFAULT NULL,
    "input_summary" CLOB,
    "output_summary" CLOB,
    "error_msg" VARCHAR(4000) DEFAULT NULL,
    "started_at" TIMESTAMP(0) NOT NULL,
    "extend_params" VARCHAR(2048) DEFAULT NULL,
    "remark" VARCHAR(512) DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "rule_bridge_execution_step_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_bridge_execution_step" IS '桥接执行步骤明细（链路回放展示用）';
COMMENT ON COLUMN "rule_bridge_execution_step"."bridge_rule_id" IS '关联桥接规则 ID（同 traceId 命中多条规则时区分 step 归属）';
COMMENT ON COLUMN "rule_bridge_execution_step"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_bridge_execution_step"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_bridge_execution_step"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_bridge_execution_step"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "rule_bridge_execution_step"."error_msg" IS '失败错误（status=01 时填；透传下游 raw 错误，对齐 ERROR_MSG_MAX_LENGTH=4000）';
COMMENT ON COLUMN "rule_bridge_execution_step"."extend_params" IS '扩展参数（步骤特异协议数据 JSON：SINK_SEND 含 sinkType/partition/messageId；RULE_MATCH 含命中条件细节；RATE_LIMIT 含阈值/当前 QPS；TRANSFORM 含 scriptId/scriptVersion 等）';
COMMENT ON COLUMN "rule_bridge_execution_step"."id" IS '主键';
COMMENT ON COLUMN "rule_bridge_execution_step"."input_summary" IS '输入摘要 JSON（envelope payload 前 1KB / 命中条件 / 模板变量等）';
COMMENT ON COLUMN "rule_bridge_execution_step"."latency_ms" IS '本步骤耗时（毫秒）';
COMMENT ON COLUMN "rule_bridge_execution_step"."output_summary" IS '输出摘要 JSON（转换后 payload / sink 返回值 / 发送 messageId 等）';
COMMENT ON COLUMN "rule_bridge_execution_step"."remark" IS '备注';
COMMENT ON COLUMN "rule_bridge_execution_step"."started_at" IS '步骤开始时间（毫秒精度）';
COMMENT ON COLUMN "rule_bridge_execution_step"."status" IS '00-成功 / 01-失败 / 02-跳过';
COMMENT ON COLUMN "rule_bridge_execution_step"."step_name" IS '步骤可读名称（中文，前端卡片标题用）';
COMMENT ON COLUMN "rule_bridge_execution_step"."step_no" IS '步骤顺序号（从1起，前端按此排序）';
COMMENT ON COLUMN "rule_bridge_execution_step"."step_type" IS '类型枚举：INGEST-数据接入 / RULE_MATCH-规则匹配 / RATE_LIMIT-限流 / TRANSFORM-脚本转换 / SINK_SEND-投递 / DEAD_LETTER-死信 / INBOUND_FORWARD-入站还原';
COMMENT ON COLUMN "rule_bridge_execution_step"."trace_id" IS '关联 trace（FK→rule_bridge_execution_trace.trace_id）';
COMMENT ON COLUMN "rule_bridge_execution_step"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "rule_bridge_execution_step"."updated_time" IS '最后修改时间';

CREATE OR REPLACE INDEX "rule_bridge_execution_step_idx_trace_rule_step" ON "rule_bridge_execution_step"("trace_id" ASC,"bridge_rule_id" ASC,"step_no" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "rule_bridge_execution_step_idx_status_time" ON "rule_bridge_execution_step"("status" ASC,"started_at" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for rule_bridge_execution_trace
-- ----------------------------
DROP TABLE IF EXISTS "rule_bridge_execution_trace";
CREATE TABLE "rule_bridge_execution_trace"
(
    "id" BIGINT NOT NULL,
    "trace_id" VARCHAR(64) NOT NULL DEFAULT '',
    "bridge_rule_id" BIGINT DEFAULT NULL,
    "direction" CHAR(2) NOT NULL DEFAULT '10',
    "trigger_source" VARCHAR(20) NOT NULL DEFAULT '',
    "tenant_id" VARCHAR(128) DEFAULT NULL,
    "product_identification" VARCHAR(128) DEFAULT NULL,
    "device_identification" VARCHAR(128) DEFAULT NULL,
    "action_type" VARCHAR(50) DEFAULT NULL,
    "topic" VARCHAR(255) DEFAULT NULL,
    "data_source_id" BIGINT DEFAULT NULL,
    "subscription_source_id" BIGINT DEFAULT NULL,
    "status" CHAR(2) NOT NULL DEFAULT '00',
    "step_count" INT NOT NULL DEFAULT '0',
    "total_latency_ms" INT DEFAULT NULL,
    "start_time" TIMESTAMP(0) NOT NULL,
    "end_time" TIMESTAMP(0) DEFAULT NULL,
    "source_payload_summary" TEXT DEFAULT NULL,
    "result_summary" VARCHAR(2000) DEFAULT NULL,
    "error_msg" VARCHAR(4000) DEFAULT NULL,
    "extend_params" VARCHAR(2048) DEFAULT NULL,
    "remark" VARCHAR(512) DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "rule_bridge_execution_trace_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "rule_bridge_execution_trace_uk_trace_rule" UNIQUE("trace_id","bridge_rule_id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_bridge_execution_trace" IS '桥接执行trace主表（链路回放用）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."action_type" IS '事件类型（PUBLISH/CONNECT/CLOSE/...，复用 LINK_DEVICE_ACTION_TYPE 字典）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."bridge_rule_id" IS '关联桥接规则 ID（出站必填；入站为订阅源拉取时为空）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_bridge_execution_trace"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_bridge_execution_trace"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_bridge_execution_trace"."data_source_id" IS '关联数据源 ID（出站=目标 sink；入站=来源 source）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "rule_bridge_execution_trace"."device_identification" IS '设备标识（出站时来自设备事件）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."direction" IS '桥接方向：10-出站 / 20-入站';
COMMENT ON COLUMN "rule_bridge_execution_trace"."end_time" IS '执行结束时间（毫秒精度）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."error_msg" IS '失败时的错误信息（透传 RocketMQ/Kafka/HTTP 等下游 raw 错误，含堆栈描述；对齐 ERROR_MSG_MAX_LENGTH=4000）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."extend_params" IS '扩展参数';
COMMENT ON COLUMN "rule_bridge_execution_trace"."id" IS '主键';
COMMENT ON COLUMN "rule_bridge_execution_trace"."product_identification" IS '产品标识（出站时来自设备事件）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."remark" IS '备注';
COMMENT ON COLUMN "rule_bridge_execution_trace"."result_summary" IS '结果摘要（成功的 sink / 失败原因等一句话；对齐 RESULT_SUMMARY_MAX_LENGTH=2000）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."source_payload_summary" IS '源消息摘要（完整 envelope 报文；便于排查 + 死信回放）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."start_time" IS '执行开始时间（毫秒精度）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."status" IS '整体状态：00-成功 / 01-失败 / 02-部分成功 / 03-死信';
COMMENT ON COLUMN "rule_bridge_execution_trace"."step_count" IS '执行的步骤总数（关联 rule_bridge_execution_step 计数）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."subscription_source_id" IS '关联订阅源 ID（仅入站）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "rule_bridge_execution_trace"."topic" IS '设备事件 topic';
COMMENT ON COLUMN "rule_bridge_execution_trace"."total_latency_ms" IS '总耗时毫秒（开始到结束）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."trace_id" IS '全链路追踪ID（贯穿 mqs → RocketMQ → rule，可与设备 publish 日志串联）';
COMMENT ON COLUMN "rule_bridge_execution_trace"."trigger_source" IS '触发来源：DEVICE_DATA-设备数据 / SUBSCRIPTION-订阅源 / TEST_SINK-测试发送 / REPLAY-死信回放';
COMMENT ON COLUMN "rule_bridge_execution_trace"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "rule_bridge_execution_trace"."updated_time" IS '最后修改时间';

CREATE OR REPLACE INDEX "rule_bridge_execution_trace_idx_rule_status_time" ON "rule_bridge_execution_trace"("bridge_rule_id" ASC,"status" ASC,"start_time" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "rule_bridge_execution_trace_idx_device_time" ON "rule_bridge_execution_trace"("device_identification" ASC,"start_time" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "rule_bridge_execution_trace_idx_tenant_time" ON "rule_bridge_execution_trace"("tenant_id" ASC,"start_time" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for rule_condition_execution_log
-- ----------------------------
DROP TABLE IF EXISTS "rule_condition_execution_log";
CREATE TABLE "rule_condition_execution_log"
(
    "id" BIGINT NOT NULL,
    "rule_execution_id" BIGINT NOT NULL,
    "condition_uuid" VARCHAR(100) NOT NULL DEFAULT '',
    "condition_type" SMALLINT NOT NULL,
    "evaluation_result" TINYINT NOT NULL,
    "start_time" TIMESTAMP(0) NOT NULL,
    "end_time" TIMESTAMP(0) DEFAULT NULL,
    "remark" VARCHAR(255) DEFAULT '',
    "extend_params" CLOB,
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "rule_condition_execution_log_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_condition_execution_log" IS '规则条件执行日志表';
COMMENT ON COLUMN "rule_condition_execution_log"."condition_type" IS '条件类型：0-设备属性触发，1-定时触发，2-设备动作触发等';
COMMENT ON COLUMN "rule_condition_execution_log"."condition_uuid" IS '条件唯一标识';
COMMENT ON COLUMN "rule_condition_execution_log"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_condition_execution_log"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_condition_execution_log"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_condition_execution_log"."end_time" IS '条件评估结束时间';
COMMENT ON COLUMN "rule_condition_execution_log"."evaluation_result" IS '条件是否成立';
COMMENT ON COLUMN "rule_condition_execution_log"."extend_params" IS '扩展参数（文本格式）';
COMMENT ON COLUMN "rule_condition_execution_log"."id" IS '主键';
COMMENT ON COLUMN "rule_condition_execution_log"."remark" IS '描述';
COMMENT ON COLUMN "rule_condition_execution_log"."rule_execution_id" IS '规则执行日志ID';
COMMENT ON COLUMN "rule_condition_execution_log"."start_time" IS '条件评估开始时间';
COMMENT ON COLUMN "rule_condition_execution_log"."updated_by" IS '更新人';
COMMENT ON COLUMN "rule_condition_execution_log"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "rule_condition_execution_log_idx_condition_uuid" ON "rule_condition_execution_log"("condition_uuid" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "rule_condition_execution_log_idx_rule_execution_id" ON "rule_condition_execution_log"("rule_execution_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for rule_data_bridge
-- ----------------------------
DROP TABLE IF EXISTS "rule_data_bridge";
CREATE TABLE "rule_data_bridge"
(
    "id" BIGINT NOT NULL,
    "app_id" VARCHAR(128) NOT NULL DEFAULT '',
    "rule_name" VARCHAR(255) NOT NULL DEFAULT '',
    "rule_code" VARCHAR(128) NOT NULL DEFAULT '',
    "direction" CHAR(2) NOT NULL DEFAULT '10',
    "data_source_id" BIGINT NOT NULL,
    "match_config_json" CLOB NOT NULL,
    "action_config_json" CLOB NOT NULL,
    "qos" INT DEFAULT NULL,
    "rate_limit_qps" INT DEFAULT NULL,
    "retry_max_times" INT DEFAULT NULL,
    "retry_backoff_ms" INT DEFAULT NULL,
    "timeout_ms" INT DEFAULT NULL,
    "dead_letter_data_source_id" BIGINT DEFAULT NULL,
    "enable" TINYINT NOT NULL DEFAULT '0',
    "priority" INT NOT NULL DEFAULT '100',
    "extend_params" VARCHAR(2048) DEFAULT NULL,
    "remark" VARCHAR(512) DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "rule_data_bridge_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "rule_data_bridge_uk_rule_code" UNIQUE("rule_code")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_data_bridge" IS '数据桥接-规则';
COMMENT ON COLUMN "rule_data_bridge"."action_config_json" IS '动作配置JSON（含 sink 特异参数；EncryptTypeHandler 整体加密落盘，防内联 Bearer token 等泄漏）。出站含payloadTemplate/transformScript/sourceType特异参数；入站含targetHandler/fieldMapping';
COMMENT ON COLUMN "rule_data_bridge"."app_id" IS '应用ID';
COMMENT ON COLUMN "rule_data_bridge"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_data_bridge"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_data_bridge"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_data_bridge"."data_source_id" IS '关联数据源 FK→rule_data_source.id';
COMMENT ON COLUMN "rule_data_bridge"."dead_letter_data_source_id" IS '规则级死信数据源覆盖';
COMMENT ON COLUMN "rule_data_bridge"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "rule_data_bridge"."direction" IS '桥接方向：10-出站(平台→第三方) / 20-入站(第三方→平台)';
COMMENT ON COLUMN "rule_data_bridge"."enable" IS '是否启用：0-禁用 / 1-启用（必须测试发送成功后手动启用）';
COMMENT ON COLUMN "rule_data_bridge"."extend_params" IS '扩展参数（兜底，未来加加密/流量分级/A-B 灰度等 0 改表）';
COMMENT ON COLUMN "rule_data_bridge"."id" IS '主键';
COMMENT ON COLUMN "rule_data_bridge"."match_config_json" IS '匹配条件JSON。出站含productIdentifications/actionTypes/topicFilter/deviceFilter/payloadFilter/timeWindow；入站含subscriptionSourceIds/messageFilter';
COMMENT ON COLUMN "rule_data_bridge"."priority" IS '优先级（数字越小越先匹配；同事件命中多条时按此排序）';
COMMENT ON COLUMN "rule_data_bridge"."qos" IS '规则级可靠性级别覆盖（NULL=用数据源默认）';
COMMENT ON COLUMN "rule_data_bridge"."rate_limit_qps" IS '规则级 QPS 限流覆盖';
COMMENT ON COLUMN "rule_data_bridge"."remark" IS '备注';
COMMENT ON COLUMN "rule_data_bridge"."retry_backoff_ms" IS '规则级初始退避时长覆盖（毫秒）';
COMMENT ON COLUMN "rule_data_bridge"."retry_max_times" IS '规则级最大重试次数覆盖';
COMMENT ON COLUMN "rule_data_bridge"."rule_code" IS '规则业务唯一编码（snowflake）';
COMMENT ON COLUMN "rule_data_bridge"."rule_name" IS '规则名称（列表页展示）';
COMMENT ON COLUMN "rule_data_bridge"."timeout_ms" IS '规则级单次发送超时覆盖（毫秒）';
COMMENT ON COLUMN "rule_data_bridge"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "rule_data_bridge"."updated_time" IS '最后修改时间';

CREATE OR REPLACE INDEX "rule_data_bridge_idx_app_direction_enable" ON "rule_data_bridge"("app_id" ASC,"direction" ASC,"enable" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "rule_data_bridge_idx_data_source_id" ON "rule_data_bridge"("data_source_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for rule_data_source
-- ----------------------------
DROP TABLE IF EXISTS "rule_data_source";
CREATE TABLE "rule_data_source"
(
    "id" BIGINT NOT NULL,
    "app_id" VARCHAR(128) NOT NULL DEFAULT '',
    "data_source_name" VARCHAR(255) NOT NULL DEFAULT '',
    "data_source_code" VARCHAR(128) NOT NULL DEFAULT '',
    "direction" CHAR(2) NOT NULL DEFAULT '10',
    "source_type" VARCHAR(32) NOT NULL DEFAULT '',
    "connection_json" CLOB NOT NULL,
    "credential_json" CLOB,
    "serialization" VARCHAR(20) NOT NULL DEFAULT 'CLOB',
    "default_qos" INT NOT NULL DEFAULT '1',
    "default_rate_limit_qps" INT NOT NULL DEFAULT '0',
    "default_retry_max_times" INT NOT NULL DEFAULT '3',
    "default_retry_backoff_ms" INT NOT NULL DEFAULT '1000',
    "default_timeout_ms" INT NOT NULL DEFAULT '5000',
    "default_dead_letter_data_source_id" BIGINT DEFAULT NULL,
    "enable" TINYINT NOT NULL DEFAULT '0',
    "health_status" VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    "last_health_check_time" TIMESTAMP(0) DEFAULT NULL,
    "extend_params" VARCHAR(2048) DEFAULT NULL,
    "remark" VARCHAR(512) DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "rule_data_source_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "rule_data_source_uk_data_source_code" UNIQUE("data_source_code")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_data_source" IS '数据桥接-数据源（出/入站共用）';
COMMENT ON COLUMN "rule_data_source"."app_id" IS '应用ID';
COMMENT ON COLUMN "rule_data_source"."connection_json" IS '连接参数JSON（host/port/topic/database/mode 等；EncryptTypeHandler 整体加密落盘）';
COMMENT ON COLUMN "rule_data_source"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_data_source"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_data_source"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_data_source"."credential_json" IS '凭证JSON（密码/密钥/token；EncryptTypeHandler 整体加密落盘）';
COMMENT ON COLUMN "rule_data_source"."data_source_code" IS '业务唯一编码（snowflake，外部系统引用）';
COMMENT ON COLUMN "rule_data_source"."data_source_name" IS '数据源名称（用户起的友好标识，列表页显示）';
COMMENT ON COLUMN "rule_data_source"."default_dead_letter_data_source_id" IS '默认死信投递的数据源 FK（一般指向告警 Kafka）';
COMMENT ON COLUMN "rule_data_source"."default_qos" IS '默认可靠性级别：0-fire-forget / 1-at-least-once / 2-exactly-once';
COMMENT ON COLUMN "rule_data_source"."default_rate_limit_qps" IS '默认 QPS 限流（0=不限）';
COMMENT ON COLUMN "rule_data_source"."default_retry_backoff_ms" IS '默认初始退避时长 ms（指数倍增 1s/2s/4s/...）';
COMMENT ON COLUMN "rule_data_source"."default_retry_max_times" IS '默认最大重试次数（不含首次发送）';
COMMENT ON COLUMN "rule_data_source"."default_timeout_ms" IS '默认单次发送超时 ms';
COMMENT ON COLUMN "rule_data_source"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "rule_data_source"."direction" IS '方向：10-出站sink / 20-入站source / 30-双向';
COMMENT ON COLUMN "rule_data_source"."enable" IS '是否启用：0-禁用 / 1-启用（必须测试连接成功后手动启用）';
COMMENT ON COLUMN "rule_data_source"."extend_params" IS '扩展参数（协议特异调参 JSON：acks/compression/timeout/poolSize 等）';
COMMENT ON COLUMN "rule_data_source"."health_status" IS '健康状态：HEALTHY/DEGRADED/DOWN/UNKNOWN（HealthCheckScheduler 5min 探活更新）';
COMMENT ON COLUMN "rule_data_source"."id" IS '主键';
COMMENT ON COLUMN "rule_data_source"."last_health_check_time" IS '上次健康检查时间';
COMMENT ON COLUMN "rule_data_source"."remark" IS '备注';
COMMENT ON COLUMN "rule_data_source"."serialization" IS '序列化策略：JSON/AVRO/STRING/BINARY（与 Serializer.name() 匹配）';
COMMENT ON COLUMN "rule_data_source"."source_type" IS '协议类型：KAFKA/REDIS/ROCKETMQ/MYSQL/HTTP/WEBHOOK/MQTT；与 ConnectorType 1:1 对齐';
COMMENT ON COLUMN "rule_data_source"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "rule_data_source"."updated_time" IS '最后修改时间';

CREATE OR REPLACE INDEX "rule_data_source_idx_app_id_direction" ON "rule_data_source"("app_id" ASC,"direction" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "rule_data_source_idx_enable" ON "rule_data_source"("enable" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for rule_execution_log
-- ----------------------------
DROP TABLE IF EXISTS "rule_execution_log";
CREATE TABLE "rule_execution_log"
(
    "id" BIGINT NOT NULL,
    "rule_identification" VARCHAR(100) NOT NULL DEFAULT '',
    "rule_name" VARCHAR(255) NOT NULL DEFAULT '',
    "status" SMALLINT NOT NULL DEFAULT '0',
    "start_time" TIMESTAMP(0) NOT NULL,
    "end_time" TIMESTAMP(0) DEFAULT NULL,
    "remark" VARCHAR(255) DEFAULT '',
    "extend_params" CLOB,
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "rule_execution_log_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_execution_log" IS '规则执行日志表';
COMMENT ON COLUMN "rule_execution_log"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_execution_log"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_execution_log"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_execution_log"."end_time" IS '规则执行结束时间';
COMMENT ON COLUMN "rule_execution_log"."extend_params" IS '扩展参数（文本格式）';
COMMENT ON COLUMN "rule_execution_log"."id" IS '主键';
COMMENT ON COLUMN "rule_execution_log"."remark" IS '描述';
COMMENT ON COLUMN "rule_execution_log"."rule_identification" IS '规则标识';
COMMENT ON COLUMN "rule_execution_log"."rule_name" IS '规则名称';
COMMENT ON COLUMN "rule_execution_log"."start_time" IS '规则执行开始时间';
COMMENT ON COLUMN "rule_execution_log"."status" IS '规则执行状态：0-未执行，1-执行中，2-已完成';
COMMENT ON COLUMN "rule_execution_log"."updated_by" IS '更新人';
COMMENT ON COLUMN "rule_execution_log"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "rule_execution_log_idx_rule_identification" ON "rule_execution_log"("rule_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for rule_groovy_script
-- ----------------------------
DROP TABLE IF EXISTS "rule_groovy_script";
CREATE TABLE "rule_groovy_script"
(
    "id" BIGINT NOT NULL,
    "name" VARCHAR(255) DEFAULT '',
    "app_id" VARCHAR(128) NOT NULL DEFAULT '',
    "script_type" VARCHAR(128) NOT NULL DEFAULT '',
    "channel_code" VARCHAR(128) NOT NULL DEFAULT '',
    "product_identification" VARCHAR(128) NOT NULL DEFAULT '',
    "topic_pattern" VARCHAR(100) NOT NULL DEFAULT '',
    "enable" TINYINT NOT NULL DEFAULT '0',
    "script_content" CLOB NOT NULL,
    "extend_params" CLOB,
    "object_version" VARCHAR(100) NOT NULL DEFAULT 'v1.0.0',
    "remark" VARCHAR(500) DEFAULT '',
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "rule_groovy_script_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "rule_groovy_script_idx_only_key" UNIQUE("script_type","channel_code","product_identification","topic_pattern","object_version")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_groovy_script" IS '规则脚本表';
COMMENT ON COLUMN "rule_groovy_script"."app_id" IS '应用ID';
COMMENT ON COLUMN "rule_groovy_script"."product_identification" IS '产品标识';
COMMENT ON COLUMN "rule_groovy_script"."topic_pattern" IS '主题模式';
COMMENT ON COLUMN "rule_groovy_script"."channel_code" IS '渠道编码';
COMMENT ON COLUMN "rule_groovy_script"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_groovy_script"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_groovy_script"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_groovy_script"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "rule_groovy_script"."enable" IS '是否启用';
COMMENT ON COLUMN "rule_groovy_script"."extend_params" IS '扩展信息';
COMMENT ON COLUMN "rule_groovy_script"."id" IS '主键';
COMMENT ON COLUMN "rule_groovy_script"."name" IS '名称';
COMMENT ON COLUMN "rule_groovy_script"."object_version" IS '版本号';
COMMENT ON COLUMN "rule_groovy_script"."script_type" IS '脚本类型';
COMMENT ON COLUMN "rule_groovy_script"."remark" IS '备注';
COMMENT ON COLUMN "rule_groovy_script"."script_content" IS '脚本内容';
COMMENT ON COLUMN "rule_groovy_script"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "rule_groovy_script"."updated_time" IS '最后修改时间';

-- ----------------------------
-- Table structure for rule_subscription_source
-- ----------------------------
DROP TABLE IF EXISTS "rule_subscription_source";
CREATE TABLE "rule_subscription_source"
(
    "id" BIGINT NOT NULL,
    "app_id" VARCHAR(128) NOT NULL DEFAULT '',
    "source_name" VARCHAR(255) NOT NULL DEFAULT '',
    "source_code" VARCHAR(128) NOT NULL DEFAULT '',
    "data_source_id" BIGINT NOT NULL,
    "target_handler" VARCHAR(50) NOT NULL DEFAULT 'MQTT_FORWARD',
    "mapping_json" CLOB NOT NULL,
    "target_product_identification" VARCHAR(128) DEFAULT NULL,
    "target_topic_template" VARCHAR(500) DEFAULT NULL,
    "enable" TINYINT NOT NULL DEFAULT '0',
    "last_consume_offset" VARCHAR(200) DEFAULT NULL,
    "extend_params" VARCHAR(2048) DEFAULT NULL,
    "remark" VARCHAR(512) DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    CONSTRAINT "rule_subscription_source_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "rule_subscription_source_uk_source_code" UNIQUE("source_code")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "rule_subscription_source" IS '数据桥接-订阅源';
COMMENT ON COLUMN "rule_subscription_source"."app_id" IS '应用ID';
COMMENT ON COLUMN "rule_subscription_source"."created_by" IS '创建人';
COMMENT ON COLUMN "rule_subscription_source"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "rule_subscription_source"."created_time" IS '创建时间';
COMMENT ON COLUMN "rule_subscription_source"."data_source_id" IS '复用数据源 FK→rule_data_source.id（direction 须为 20-入站 或 30-双向）';
COMMENT ON COLUMN "rule_subscription_source"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "rule_subscription_source"."enable" IS '是否启用：0-禁用 / 1-启用（必须测试连接成功后手动启用）';
COMMENT ON COLUMN "rule_subscription_source"."extend_params" IS '扩展参数';
COMMENT ON COLUMN "rule_subscription_source"."id" IS '主键';
COMMENT ON COLUMN "rule_subscription_source"."last_consume_offset" IS '上次消费位点（Kafka offset / MQTT messageId / HTTP 时间戳；重启后接续消费）';
COMMENT ON COLUMN "rule_subscription_source"."mapping_json" IS '字段映射 JSON（如 [{"sourceField":"device_id","targetField":"deviceIdentification"}]）';
COMMENT ON COLUMN "rule_subscription_source"."remark" IS '备注';
COMMENT ON COLUMN "rule_subscription_source"."source_code" IS '业务唯一编码（snowflake；HTTP 入站 endpoint URL 用此值）';
COMMENT ON COLUMN "rule_subscription_source"."source_name" IS '订阅源名称（用户可读）';
COMMENT ON COLUMN "rule_subscription_source"."target_handler" IS '入站后处理方式：MQTT_FORWARD-伪装设备 publish / RAW_INSERT-直接写 DeviceAction / RULE_TRIGGER-触发场景联动';
COMMENT ON COLUMN "rule_subscription_source"."target_product_identification" IS 'target_handler=MQTT_FORWARD 时的目标产品标识';
COMMENT ON COLUMN "rule_subscription_source"."target_topic_template" IS '目标 topic 模板（含 ${} 占位符，如 $thing/up/property/${productId}/${deviceId}）';
COMMENT ON COLUMN "rule_subscription_source"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "rule_subscription_source"."updated_time" IS '最后修改时间';

CREATE OR REPLACE INDEX "rule_subscription_source_idx_app_id_enable" ON "rule_subscription_source"("app_id" ASC,"enable" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_channel
-- ----------------------------
DROP TABLE IF EXISTS "video_channel";
CREATE TABLE "video_channel"
(
    "id" BIGINT NOT NULL,
    "device_identification" VARCHAR(64) NOT NULL,
    "channel_identification" VARCHAR(64) NOT NULL,
    "channel_no" INT DEFAULT NULL,
    "channel_type" SMALLINT DEFAULT NULL,
    "channel_name" VARCHAR(128) DEFAULT NULL,
    "stream_identification" VARCHAR(64) DEFAULT NULL,
    "stream_type" VARCHAR(32) DEFAULT NULL,
    "manufacturer" VARCHAR(128) DEFAULT NULL,
    "model" VARCHAR(128) DEFAULT NULL,
    "online_status" TINYINT DEFAULT '0',
    "host" VARCHAR(256) DEFAULT NULL,
    "port" INT DEFAULT NULL,
    "password" VARCHAR(128) DEFAULT NULL,
    "longitude" DECIMAL(12,8) DEFAULT NULL,
    "latitude" DECIMAL(12,8) DEFAULT NULL,
    "full_address" VARCHAR(512) DEFAULT NULL,
    "province_code" VARCHAR(16) DEFAULT NULL,
    "city_code" VARCHAR(16) DEFAULT NULL,
    "region_code" VARCHAR(16) DEFAULT NULL,
    "has_audio" TINYINT DEFAULT '0',
    "ptz_type" TINYINT DEFAULT NULL,
    "ptz_capability" TINYINT DEFAULT '0',
    "talk_capability" TINYINT DEFAULT '0',
    "secrecy" TINYINT DEFAULT '0',
    "channel_config" CLOB DEFAULT NULL,
    "extend_params" VARCHAR(1024) DEFAULT NULL,
    "remark" VARCHAR(512) DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    CONSTRAINT "video_channel_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "video_channel_uk_channel_identification" UNIQUE("channel_identification")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_channel" IS '统一通道表';
COMMENT ON COLUMN "video_channel"."channel_config" IS '通道专属配置(JSON)';
COMMENT ON COLUMN "video_channel"."channel_identification" IS '通道标识';
COMMENT ON COLUMN "video_channel"."channel_name" IS '通道名称';
COMMENT ON COLUMN "video_channel"."channel_no" IS '逻辑通道号';
COMMENT ON COLUMN "video_channel"."channel_type" IS '通道类型(GB28181行业编码131~143)';
COMMENT ON COLUMN "video_channel"."city_code" IS '市级编码';
COMMENT ON COLUMN "video_channel"."created_by" IS '创建人';
COMMENT ON COLUMN "video_channel"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "video_channel"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_channel"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_channel"."device_identification" IS '所属设备标识';
COMMENT ON COLUMN "video_channel"."extend_params" IS '扩展参数';
COMMENT ON COLUMN "video_channel"."full_address" IS '安装地址';
COMMENT ON COLUMN "video_channel"."has_audio" IS '支持音频(0=否/1=是)';
COMMENT ON COLUMN "video_channel"."host" IS '通道地址(IP/域名)';
COMMENT ON COLUMN "video_channel"."id" IS '主键';
COMMENT ON COLUMN "video_channel"."latitude" IS '纬度';
COMMENT ON COLUMN "video_channel"."longitude" IS '经度';
COMMENT ON COLUMN "video_channel"."manufacturer" IS '厂商';
COMMENT ON COLUMN "video_channel"."model" IS '型号';
COMMENT ON COLUMN "video_channel"."online_status" IS '在线状态(0=离线/1=在线)';
COMMENT ON COLUMN "video_channel"."password" IS '设备口令';
COMMENT ON COLUMN "video_channel"."port" IS '端口';
COMMENT ON COLUMN "video_channel"."province_code" IS '省级编码';
COMMENT ON COLUMN "video_channel"."ptz_capability" IS '支持云台控制(0=否/1=是)';
COMMENT ON COLUMN "video_channel"."ptz_type" IS '云台类型(0=未知/1=球机/2=半球/3=固定枪机/4=遥控枪机)';
COMMENT ON COLUMN "video_channel"."region_code" IS '行政区划编码';
COMMENT ON COLUMN "video_channel"."remark" IS '备注';
COMMENT ON COLUMN "video_channel"."secrecy" IS '保密属性(0=不涉密/1=涉密)';
COMMENT ON COLUMN "video_channel"."stream_identification" IS '流标识';
COMMENT ON COLUMN "video_channel"."stream_type" IS '流类型';
COMMENT ON COLUMN "video_channel"."talk_capability" IS '支持对讲(0=否/1=是)';
COMMENT ON COLUMN "video_channel"."updated_by" IS '修改人';
COMMENT ON COLUMN "video_channel"."updated_time" IS '修改时间';

CREATE OR REPLACE INDEX "video_channel_idx_device_identification" ON "video_channel"("device_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_channel_idx_online_status" ON "video_channel"("online_status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_channel_idx_stream_identification" ON "video_channel"("stream_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_channel_idx_created_org_id" ON "video_channel"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_device
-- ----------------------------
DROP TABLE IF EXISTS "video_device";
CREATE TABLE "video_device"
(
    "id" BIGINT NOT NULL,
    "device_identification" VARCHAR(64) NOT NULL,
    "access_protocol" VARCHAR(32) NOT NULL,
    "device_name" VARCHAR(128) DEFAULT NULL,
    "custom_name" VARCHAR(128) DEFAULT NULL,
    "manufacturer" VARCHAR(128) DEFAULT NULL,
    "model" VARCHAR(128) DEFAULT NULL,
    "firmware" VARCHAR(128) DEFAULT NULL,
    "host" VARCHAR(256) DEFAULT NULL,
    "port" INT DEFAULT NULL,
    "wan_host" VARCHAR(256) DEFAULT NULL,
    "lan_host" VARCHAR(256) DEFAULT NULL,
    "access_endpoint" VARCHAR(512) DEFAULT NULL,
    "sdp_host" VARCHAR(256) DEFAULT NULL,
    "local_host" VARCHAR(256) DEFAULT NULL,
    "transport" VARCHAR(16) DEFAULT NULL,
    "stream_mode" VARCHAR(16) DEFAULT NULL,
    "online_status" TINYINT DEFAULT '0',
    "register_time" VARCHAR(32) DEFAULT NULL,
    "last_keepalive_time" VARCHAR(32) DEFAULT NULL,
    "expires" INT DEFAULT NULL,
    "keepalive_interval" INT DEFAULT NULL,
    "keepalive_timeout_count" INT DEFAULT NULL,
    "auth_type" VARCHAR(32) DEFAULT NULL,
    "auth_secret" VARCHAR(512) DEFAULT NULL,
    "media_identification" VARCHAR(64) DEFAULT NULL,
    "channel_count" INT DEFAULT '0',
    "ability" VARCHAR(512) DEFAULT NULL,
    "protocol_config" CLOB DEFAULT NULL,
    "extend_params" VARCHAR(1024) DEFAULT NULL,
    "remark" VARCHAR(512) DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    CONSTRAINT "video_device_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "video_device_uk_device_identification" UNIQUE("device_identification")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_device" IS '统一设备表';
COMMENT ON COLUMN "video_device"."ability" IS '设备能力集描述';
COMMENT ON COLUMN "video_device"."access_endpoint" IS '完整访问端点(host:port)';
COMMENT ON COLUMN "video_device"."access_protocol" IS '设备接入协议(GB28181/ONVIF/ISUP/JT1078/SIP/PELCO_D/PELCO_P)';
COMMENT ON COLUMN "video_device"."auth_secret" IS '认证密钥(加密存储)';
COMMENT ON COLUMN "video_device"."auth_type" IS '认证方式(PASSWORD/VALIDATE_CODE/AUTH_TOKEN/CERTIFICATE/DIGEST/NONE)';
COMMENT ON COLUMN "video_device"."channel_count" IS '通道数量';
COMMENT ON COLUMN "video_device"."created_by" IS '创建人';
COMMENT ON COLUMN "video_device"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "video_device"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_device"."custom_name" IS '自定义名称';
COMMENT ON COLUMN "video_device"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_device"."device_identification" IS '设备标识';
COMMENT ON COLUMN "video_device"."device_name" IS '设备名称';
COMMENT ON COLUMN "video_device"."expires" IS '注册有效期(秒)';
COMMENT ON COLUMN "video_device"."extend_params" IS '扩展参数';
COMMENT ON COLUMN "video_device"."firmware" IS '固件版本';
COMMENT ON COLUMN "video_device"."host" IS '设备地址(IP/域名)';
COMMENT ON COLUMN "video_device"."id" IS '主键';
COMMENT ON COLUMN "video_device"."keepalive_interval" IS '心跳间隔(秒)';
COMMENT ON COLUMN "video_device"."keepalive_timeout_count" IS '心跳超时次数';
COMMENT ON COLUMN "video_device"."lan_host" IS '局域网地址(IP/域名)';
COMMENT ON COLUMN "video_device"."last_keepalive_time" IS '最后心跳时间';
COMMENT ON COLUMN "video_device"."local_host" IS '本地SIP交互地址(IP/域名)';
COMMENT ON COLUMN "video_device"."manufacturer" IS '厂商';
COMMENT ON COLUMN "video_device"."media_identification" IS '媒体服务唯一标识';
COMMENT ON COLUMN "video_device"."model" IS '型号';
COMMENT ON COLUMN "video_device"."online_status" IS '是否在线(0=离线/1=在线)';
COMMENT ON COLUMN "video_device"."port" IS '端口';
COMMENT ON COLUMN "video_device"."protocol_config" IS '协议专属配置(JSON)';
COMMENT ON COLUMN "video_device"."register_time" IS '注册时间';
COMMENT ON COLUMN "video_device"."remark" IS '备注';
COMMENT ON COLUMN "video_device"."sdp_host" IS '收流地址(IP/域名)';
COMMENT ON COLUMN "video_device"."stream_mode" IS '数据流传输模式';
COMMENT ON COLUMN "video_device"."transport" IS '传输协议(UDP/TCP)';
COMMENT ON COLUMN "video_device"."updated_by" IS '修改人';
COMMENT ON COLUMN "video_device"."updated_time" IS '修改时间';
COMMENT ON COLUMN "video_device"."wan_host" IS '公网地址(IP/域名)';

CREATE OR REPLACE INDEX "video_device_idx_access_protocol" ON "video_device"("access_protocol" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_idx_online_status" ON "video_device"("online_status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_idx_media_identification" ON "video_device"("media_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_idx_created_org_id" ON "video_device"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_device_alarm
-- ----------------------------
DROP TABLE IF EXISTS "video_device_alarm";
CREATE TABLE "video_device_alarm"
(
    "id" BIGINT NOT NULL,
    "device_identification" VARCHAR(50) NOT NULL,
    "channel_identification" VARCHAR(50) DEFAULT NULL,
    "alarm_priority" TINYINT DEFAULT NULL,
    "alarm_method" TINYINT DEFAULT NULL,
    "alarm_time" TIMESTAMP(0) DEFAULT NULL,
    "alarm_description" VARCHAR(500) DEFAULT NULL,
    "alarm_type" TINYINT DEFAULT NULL,
    "alarm_type_param" VARCHAR(500) DEFAULT NULL,
    "longitude" DOUBLE DEFAULT NULL,
    "latitude" DOUBLE DEFAULT NULL,
    "handle_status" TINYINT NOT NULL DEFAULT '0',
    "handle_user_id" BIGINT DEFAULT NULL,
    "handle_time" TIMESTAMP(0) DEFAULT NULL,
    "handle_result" CLOB,
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    CONSTRAINT "video_device_alarm_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_device_alarm" IS '视频设备告警表';
COMMENT ON COLUMN "video_device_alarm"."alarm_description" IS '告警描述';
COMMENT ON COLUMN "video_device_alarm"."alarm_method" IS '告警方式';
COMMENT ON COLUMN "video_device_alarm"."alarm_priority" IS '告警级别(1=一级警情/2=二级警情/3=三级警情/4=四级警情)';
COMMENT ON COLUMN "video_device_alarm"."alarm_time" IS '告警时间（设备上报时间）';
COMMENT ON COLUMN "video_device_alarm"."alarm_type" IS '告警类型';
COMMENT ON COLUMN "video_device_alarm"."alarm_type_param" IS '告警类型参数(JSON)';
COMMENT ON COLUMN "video_device_alarm"."channel_identification" IS '通道国标编号';
COMMENT ON COLUMN "video_device_alarm"."created_by" IS '创建人';
COMMENT ON COLUMN "video_device_alarm"."created_org_id" IS '所属组织ID';
COMMENT ON COLUMN "video_device_alarm"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_device_alarm"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_device_alarm"."device_identification" IS '设备国标编号';
COMMENT ON COLUMN "video_device_alarm"."handle_result" IS '处理结果描述';
COMMENT ON COLUMN "video_device_alarm"."handle_status" IS '处理状态(0=待处理/1=处理中/2=已处理/3=已忽略)';
COMMENT ON COLUMN "video_device_alarm"."handle_time" IS '处理时间';
COMMENT ON COLUMN "video_device_alarm"."handle_user_id" IS '处理人ID';
COMMENT ON COLUMN "video_device_alarm"."id" IS '主键';
COMMENT ON COLUMN "video_device_alarm"."latitude" IS '纬度';
COMMENT ON COLUMN "video_device_alarm"."longitude" IS '经度';
COMMENT ON COLUMN "video_device_alarm"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_device_alarm"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "video_device_alarm_idx_device_identification" ON "video_device_alarm"("device_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_alarm_idx_alarm_time" ON "video_device_alarm"("alarm_time" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_alarm_idx_handle_status" ON "video_device_alarm"("handle_status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_alarm_idx_alarm_priority" ON "video_device_alarm"("alarm_priority" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_alarm_idx_created_org_id" ON "video_device_alarm"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_device_group
-- ----------------------------
DROP TABLE IF EXISTS "video_device_group";
CREATE TABLE "video_device_group"
(
    "id" BIGINT NOT NULL,
    "group_name" VARCHAR(100) NOT NULL,
    "parent_id" BIGINT DEFAULT NULL,
    "group_type" TINYINT NOT NULL DEFAULT '0',
    "sort_order" INT NOT NULL DEFAULT '0',
    "group_path" VARCHAR(500) DEFAULT NULL,
    "group_level" INT NOT NULL DEFAULT '1',
    "icon" VARCHAR(100) DEFAULT NULL,
    "enable" TINYINT NOT NULL DEFAULT '1',
    "extend_params" CLOB,
    "remark" VARCHAR(500) DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    CONSTRAINT "video_device_group_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_device_group" IS '视频设备分组表';
COMMENT ON COLUMN "video_device_group"."created_by" IS '创建人';
COMMENT ON COLUMN "video_device_group"."created_org_id" IS '所属组织ID';
COMMENT ON COLUMN "video_device_group"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_device_group"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_device_group"."enable" IS '启用状态(0=禁用/1=启用)';
COMMENT ON COLUMN "video_device_group"."extend_params" IS '扩展参数(JSON)';
COMMENT ON COLUMN "video_device_group"."group_level" IS '层级深度(从1开始)';
COMMENT ON COLUMN "video_device_group"."group_name" IS '分组名称';
COMMENT ON COLUMN "video_device_group"."group_path" IS '层级路径(如: /1/2/3，便于快速查子孙)';
COMMENT ON COLUMN "video_device_group"."group_type" IS '分组类型(0=自定义分组/1=行政区划/2=业务分组)';
COMMENT ON COLUMN "video_device_group"."icon" IS '图标标识';
COMMENT ON COLUMN "video_device_group"."id" IS '主键';
COMMENT ON COLUMN "video_device_group"."parent_id" IS '上级分组ID(顶层为空)';
COMMENT ON COLUMN "video_device_group"."remark" IS '备注';
COMMENT ON COLUMN "video_device_group"."sort_order" IS '排序序号';
COMMENT ON COLUMN "video_device_group"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_device_group"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "video_device_group_idx_parent_id" ON "video_device_group"("parent_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_group_idx_group_path" ON "video_device_group"("group_path" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_group_idx_sort_order" ON "video_device_group"("sort_order" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_group_idx_enable" ON "video_device_group"("enable" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_group_idx_created_org_id" ON "video_device_group"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_device_group_relation
-- ----------------------------
DROP TABLE IF EXISTS "video_device_group_relation";
CREATE TABLE "video_device_group_relation"
(
    "id" BIGINT NOT NULL,
    "group_id" BIGINT NOT NULL,
    "device_identification" VARCHAR(50) NOT NULL,
    "channel_identification" VARCHAR(50) DEFAULT NULL,
    "sort_order" INT NOT NULL DEFAULT '0',
    "extend_params" CLOB,
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    CONSTRAINT "video_device_group_relation_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "video_device_group_relation_uk_group_device_channel" UNIQUE("group_id","device_identification","channel_identification")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_device_group_relation" IS '视频设备分组关联表';
COMMENT ON COLUMN "video_device_group_relation"."channel_identification" IS '通道国标编号(空表示设备级别关联)';
COMMENT ON COLUMN "video_device_group_relation"."created_by" IS '创建人';
COMMENT ON COLUMN "video_device_group_relation"."created_org_id" IS '所属组织ID';
COMMENT ON COLUMN "video_device_group_relation"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_device_group_relation"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_device_group_relation"."device_identification" IS '设备国标编号';
COMMENT ON COLUMN "video_device_group_relation"."extend_params" IS '扩展参数(JSON)';
COMMENT ON COLUMN "video_device_group_relation"."group_id" IS '分组ID';
COMMENT ON COLUMN "video_device_group_relation"."id" IS '主键';
COMMENT ON COLUMN "video_device_group_relation"."sort_order" IS '分组内排序序号';
COMMENT ON COLUMN "video_device_group_relation"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_device_group_relation"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "video_device_group_relation_idx_group_id" ON "video_device_group_relation"("group_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_group_relation_idx_device_identification" ON "video_device_group_relation"("device_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_group_relation_idx_created_org_id" ON "video_device_group_relation"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_device_mobile_position
-- ----------------------------
DROP TABLE IF EXISTS "video_device_mobile_position";
CREATE TABLE "video_device_mobile_position"
(
    "id" BIGINT NOT NULL,
    "device_identification" VARCHAR(50) NOT NULL,
    "channel_identification" VARCHAR(50) DEFAULT NULL,
    "longitude" DOUBLE DEFAULT NULL,
    "latitude" DOUBLE DEFAULT NULL,
    "altitude" DOUBLE DEFAULT NULL,
    "speed" DOUBLE DEFAULT NULL,
    "direction" DOUBLE DEFAULT NULL,
    "report_time" TIMESTAMP(0) DEFAULT NULL,
    "geo_coord_sys" VARCHAR(20) DEFAULT 'WGS84',
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    CONSTRAINT "video_device_mobile_position_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_device_mobile_position" IS '视频设备移动位置表';
COMMENT ON COLUMN "video_device_mobile_position"."altitude" IS '海拔高度(米)';
COMMENT ON COLUMN "video_device_mobile_position"."channel_identification" IS '通道国标编号';
COMMENT ON COLUMN "video_device_mobile_position"."created_by" IS '创建人';
COMMENT ON COLUMN "video_device_mobile_position"."created_org_id" IS '所属组织ID';
COMMENT ON COLUMN "video_device_mobile_position"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_device_mobile_position"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_device_mobile_position"."device_identification" IS '设备国标编号';
COMMENT ON COLUMN "video_device_mobile_position"."direction" IS '方向角(度，正北为0，顺时针)';
COMMENT ON COLUMN "video_device_mobile_position"."geo_coord_sys" IS '坐标系(WGS84/GCJ02/BD09)';
COMMENT ON COLUMN "video_device_mobile_position"."id" IS '主键';
COMMENT ON COLUMN "video_device_mobile_position"."latitude" IS '纬度';
COMMENT ON COLUMN "video_device_mobile_position"."longitude" IS '经度';
COMMENT ON COLUMN "video_device_mobile_position"."report_time" IS '位置上报时间';
COMMENT ON COLUMN "video_device_mobile_position"."speed" IS '速度(km/h)';
COMMENT ON COLUMN "video_device_mobile_position"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_device_mobile_position"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "video_device_mobile_position_idx_device_identification" ON "video_device_mobile_position"("device_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_mobile_position_idx_report_time" ON "video_device_mobile_position"("report_time" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_mobile_position_idx_device_time" ON "video_device_mobile_position"("device_identification" ASC,"report_time" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_device_mobile_position_idx_created_org_id" ON "video_device_mobile_position"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_gateway_mapping
-- ----------------------------
DROP TABLE IF EXISTS "video_gateway_mapping";
CREATE TABLE "video_gateway_mapping"
(
    "id" BIGINT NOT NULL,
    "src_protocol" VARCHAR(32) NOT NULL,
    "src_device_identification" VARCHAR(64) NOT NULL,
    "src_channel_identification" VARCHAR(64) DEFAULT NULL,
    "gb_device_id" VARCHAR(64) NOT NULL,
    "gb_channel_id" VARCHAR(64) DEFAULT NULL,
    "gb_platform_id" BIGINT DEFAULT NULL,
    "enable" TINYINT NOT NULL DEFAULT '1',
    "auto_push" TINYINT NOT NULL DEFAULT '0',
    "mapping_config" CLOB DEFAULT NULL,
    "register_status" TINYINT DEFAULT '0',
    "last_register_time" VARCHAR(32) DEFAULT NULL,
    "remark" VARCHAR(512) DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    CONSTRAINT "video_gateway_mapping_PRIMARY" NOT CLUSTER PRIMARY KEY("id"),
    CONSTRAINT "video_gateway_mapping_uk_src_device_channel" UNIQUE("src_protocol","src_device_identification","src_channel_identification")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_gateway_mapping" IS '网关协议映射表';
COMMENT ON COLUMN "video_gateway_mapping"."auto_push" IS '自动推流(0=否/1=是)';
COMMENT ON COLUMN "video_gateway_mapping"."created_by" IS '创建人';
COMMENT ON COLUMN "video_gateway_mapping"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "video_gateway_mapping"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_gateway_mapping"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_gateway_mapping"."enable" IS '是否启用(0=禁用/1=启用)';
COMMENT ON COLUMN "video_gateway_mapping"."gb_channel_id" IS '映射GB28181通道编号';
COMMENT ON COLUMN "video_gateway_mapping"."gb_device_id" IS '映射GB28181设备编号';
COMMENT ON COLUMN "video_gateway_mapping"."gb_platform_id" IS '目标上级平台ID';
COMMENT ON COLUMN "video_gateway_mapping"."id" IS '主键';
COMMENT ON COLUMN "video_gateway_mapping"."last_register_time" IS '最后注册时间';
COMMENT ON COLUMN "video_gateway_mapping"."mapping_config" IS '映射配置(JSON)';
COMMENT ON COLUMN "video_gateway_mapping"."register_status" IS '注册状态(0=未注册/1=已注册)';
COMMENT ON COLUMN "video_gateway_mapping"."remark" IS '备注';
COMMENT ON COLUMN "video_gateway_mapping"."src_channel_identification" IS '源通道标识';
COMMENT ON COLUMN "video_gateway_mapping"."src_device_identification" IS '源设备标识';
COMMENT ON COLUMN "video_gateway_mapping"."src_protocol" IS '源协议(JT1078/ISUP等)';
COMMENT ON COLUMN "video_gateway_mapping"."updated_by" IS '修改人';
COMMENT ON COLUMN "video_gateway_mapping"."updated_time" IS '修改时间';

CREATE OR REPLACE INDEX "video_gateway_mapping_idx_gb_device_id" ON "video_gateway_mapping"("gb_device_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_gateway_mapping_idx_gb_channel_id" ON "video_gateway_mapping"("gb_channel_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_gateway_mapping_idx_gb_platform_id" ON "video_gateway_mapping"("gb_platform_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_gateway_mapping_idx_created_org_id" ON "video_gateway_mapping"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_notify_subscription
-- ----------------------------
DROP TABLE IF EXISTS "video_notify_subscription";
CREATE TABLE "video_notify_subscription"
(
    "id" BIGINT NOT NULL,
    "subscription_name" VARCHAR(200) NOT NULL,
    "channel_type" VARCHAR(30) NOT NULL,
    "channel_config" CLOB,
    "template_code" VARCHAR(100) NOT NULL,
    "event_types" VARCHAR(500) NOT NULL,
    "priority_filter" VARCHAR(100) DEFAULT NULL,
    "recipient_scope" VARCHAR(20) NOT NULL DEFAULT 'SELF',
    "recipient_ids" VARCHAR(2000) DEFAULT NULL,
    "at_all" TINYINT NOT NULL DEFAULT '0',
    "jump_url_template" VARCHAR(500) DEFAULT NULL,
    "msg_template" CLOB,
    "status" TINYINT NOT NULL DEFAULT '1',
    "remark" VARCHAR(500) DEFAULT NULL,
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    CONSTRAINT "video_notify_subscription_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_notify_subscription" IS '视频事件通知订阅配置';
COMMENT ON COLUMN "video_notify_subscription"."at_all" IS '@所有人(0=否/1=是)';
COMMENT ON COLUMN "video_notify_subscription"."channel_config" IS '渠道凭证(JSON)';
COMMENT ON COLUMN "video_notify_subscription"."channel_type" IS '渠道类型(字典 NOTIFY_CHANNEL_TYPE)';
COMMENT ON COLUMN "video_notify_subscription"."created_by" IS '创建人';
COMMENT ON COLUMN "video_notify_subscription"."created_org_id" IS '创建组织ID';
COMMENT ON COLUMN "video_notify_subscription"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_notify_subscription"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_notify_subscription"."event_types" IS '订阅事件类型(逗号分隔)';
COMMENT ON COLUMN "video_notify_subscription"."id" IS '主键';
COMMENT ON COLUMN "video_notify_subscription"."jump_url_template" IS '跳转链接模板';
COMMENT ON COLUMN "video_notify_subscription"."msg_template" IS '消息内容模板(支持${变量})';
COMMENT ON COLUMN "video_notify_subscription"."priority_filter" IS '告警级别过滤(逗号分隔,空=全部)';
COMMENT ON COLUMN "video_notify_subscription"."recipient_ids" IS '接收人用户ID(逗号分隔)';
COMMENT ON COLUMN "video_notify_subscription"."recipient_scope" IS '接收范围: SELF/ORG/CUSTOM';
COMMENT ON COLUMN "video_notify_subscription"."remark" IS '备注';
COMMENT ON COLUMN "video_notify_subscription"."status" IS '状态(0=禁用/1=启用)';
COMMENT ON COLUMN "video_notify_subscription"."subscription_name" IS '订阅名称';
COMMENT ON COLUMN "video_notify_subscription"."template_code" IS '消息模板编码(ExtendMsgTemplate.code)';
COMMENT ON COLUMN "video_notify_subscription"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_notify_subscription"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "video_notify_subscription_idx_event_types" ON "video_notify_subscription"("event_types" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_notify_subscription_idx_channel_type" ON "video_notify_subscription"("channel_type" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_notify_subscription_idx_status" ON "video_notify_subscription"("status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_notify_subscription_idx_created_org_id" ON "video_notify_subscription"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_platform
-- ----------------------------
DROP TABLE IF EXISTS "video_platform";
CREATE TABLE "video_platform"
(
    "id" BIGINT NOT NULL,
    "name" VARCHAR(100) DEFAULT NULL,
    "enable" TINYINT DEFAULT '1',
    "server_gb_id" VARCHAR(50) DEFAULT NULL,
    "server_gb_domain" VARCHAR(50) DEFAULT NULL,
    "server_ip" VARCHAR(128) DEFAULT '',
    "server_port" INT DEFAULT NULL,
    "device_gb_id" VARCHAR(50) DEFAULT NULL,
    "device_ip" VARCHAR(128) DEFAULT '',
    "device_port" INT DEFAULT NULL,
    "username" VARCHAR(100) DEFAULT NULL,
    "password" VARCHAR(200) DEFAULT NULL,
    "expires" INT DEFAULT '3600',
    "keep_timeout" INT DEFAULT '60',
    "transport" VARCHAR(10) DEFAULT 'UDP',
    "character_set" VARCHAR(20) DEFAULT 'GB2312',
    "ptz" TINYINT DEFAULT '0',
    "rtcp" TINYINT DEFAULT '0',
    "status" TINYINT DEFAULT '0',
    "catalog_subscribe" TINYINT DEFAULT '0',
    "alarm_subscribe" TINYINT DEFAULT '0',
    "mobile_position_subscribe" TINYINT DEFAULT '0',
    "catalog_group" INT DEFAULT '1',
    "as_message_channel" TINYINT DEFAULT '0',
    "send_stream_ip" VARCHAR(128) DEFAULT '',
    "auto_push_channel" TINYINT DEFAULT '0',
    "catalog_with_platform" INT DEFAULT '0',
    "catalog_with_group" INT DEFAULT '0',
    "catalog_with_region" INT DEFAULT '0',
    "civil_code" VARCHAR(50) DEFAULT NULL,
    "manufacturer" VARCHAR(100) DEFAULT NULL,
    "model" VARCHAR(100) DEFAULT NULL,
    "address" VARCHAR(200) DEFAULT NULL,
    "register_way" TINYINT DEFAULT '1',
    "secrecy" TINYINT DEFAULT '0',
    "server_id" VARCHAR(50) DEFAULT NULL,
    "cascade_type" TINYINT DEFAULT '0',
    "gb_version" VARCHAR(20) DEFAULT NULL,
    "online_status" TINYINT DEFAULT '0',
    "register_expires" INT DEFAULT '3600',
    "keepalive_interval" INT DEFAULT '60',
    "keepalive_timeout_count" INT DEFAULT '3',
    "last_register_time" VARCHAR(50) DEFAULT NULL,
    "last_keepalive_time" VARCHAR(50) DEFAULT NULL,
    "start_offline_push" TINYINT DEFAULT '0',
    "sip_ip" VARCHAR(128) DEFAULT '',
    "sip_port" INT DEFAULT NULL,
    "hook_url_prefix" VARCHAR(200) DEFAULT NULL,
    "service_instance_id" VARCHAR(100) DEFAULT NULL,
    "cascade_sdp_ip" VARCHAR(128) DEFAULT '',
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    CONSTRAINT "video_platform_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_platform" IS '视频级联平台表';
COMMENT ON COLUMN "video_platform"."address" IS '地址';
COMMENT ON COLUMN "video_platform"."alarm_subscribe" IS '订阅告警';
COMMENT ON COLUMN "video_platform"."as_message_channel" IS '作为消息通道';
COMMENT ON COLUMN "video_platform"."auto_push_channel" IS '自动推送通道';
COMMENT ON COLUMN "video_platform"."cascade_sdp_ip" IS '级联SDP IP/域名';
COMMENT ON COLUMN "video_platform"."cascade_type" IS '级联类型(0=作为下级/1=作为上级)';
COMMENT ON COLUMN "video_platform"."catalog_group" IS '目录分组大小';
COMMENT ON COLUMN "video_platform"."catalog_subscribe" IS '订阅目录变化';
COMMENT ON COLUMN "video_platform"."catalog_with_group" IS '目录包含分组';
COMMENT ON COLUMN "video_platform"."catalog_with_platform" IS '目录包含平台';
COMMENT ON COLUMN "video_platform"."catalog_with_region" IS '目录包含区域';
COMMENT ON COLUMN "video_platform"."character_set" IS '字符集(GB2312/UTF-8)';
COMMENT ON COLUMN "video_platform"."civil_code" IS '行政区划编码';
COMMENT ON COLUMN "video_platform"."created_by" IS '创建人';
COMMENT ON COLUMN "video_platform"."created_org_id" IS '所属组织ID';
COMMENT ON COLUMN "video_platform"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_platform"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_platform"."device_gb_id" IS '本平台在上级的设备编号';
COMMENT ON COLUMN "video_platform"."device_ip" IS '本机设备IP/域名';
COMMENT ON COLUMN "video_platform"."device_port" IS '本平台SIP端口';
COMMENT ON COLUMN "video_platform"."enable" IS '启用状态(0=禁用/1=启用)';
COMMENT ON COLUMN "video_platform"."expires" IS '注册有效期(秒)';
COMMENT ON COLUMN "video_platform"."gb_version" IS 'GB28181协议版本';
COMMENT ON COLUMN "video_platform"."hook_url_prefix" IS 'Hook URL前缀';
COMMENT ON COLUMN "video_platform"."id" IS '主键';
COMMENT ON COLUMN "video_platform"."keep_timeout" IS '心跳超时(秒)';
COMMENT ON COLUMN "video_platform"."keepalive_interval" IS '心跳间隔(秒)';
COMMENT ON COLUMN "video_platform"."keepalive_timeout_count" IS '心跳超时次数';
COMMENT ON COLUMN "video_platform"."last_keepalive_time" IS '最近心跳时间';
COMMENT ON COLUMN "video_platform"."last_register_time" IS '最近注册时间';
COMMENT ON COLUMN "video_platform"."manufacturer" IS '厂商';
COMMENT ON COLUMN "video_platform"."mobile_position_subscribe" IS '订阅位置';
COMMENT ON COLUMN "video_platform"."model" IS '型号';
COMMENT ON COLUMN "video_platform"."name" IS '平台名称';
COMMENT ON COLUMN "video_platform"."online_status" IS '在线状态(0=离线/1=在线)';
COMMENT ON COLUMN "video_platform"."password" IS '认证密码';
COMMENT ON COLUMN "video_platform"."ptz" IS '是否支持PTZ';
COMMENT ON COLUMN "video_platform"."register_expires" IS '注册有效期(秒)';
COMMENT ON COLUMN "video_platform"."register_way" IS '注册方式(1=符合标准)';
COMMENT ON COLUMN "video_platform"."rtcp" IS '是否支持RTCP';
COMMENT ON COLUMN "video_platform"."secrecy" IS '保密属性(0=不涉密/1=涉密)';
COMMENT ON COLUMN "video_platform"."send_stream_ip" IS '推流IP/域名';
COMMENT ON COLUMN "video_platform"."server_gb_domain" IS '平台国标域';
COMMENT ON COLUMN "video_platform"."server_gb_id" IS '平台国标编号';
COMMENT ON COLUMN "video_platform"."server_id" IS '服务器ID';
COMMENT ON COLUMN "video_platform"."server_ip" IS '上级SIP服务IP/域名';
COMMENT ON COLUMN "video_platform"."server_port" IS '平台SIP端口';
COMMENT ON COLUMN "video_platform"."service_instance_id" IS '服务实例ID';
COMMENT ON COLUMN "video_platform"."sip_ip" IS 'SIP服务IP/域名';
COMMENT ON COLUMN "video_platform"."sip_port" IS 'SIP监听端口';
COMMENT ON COLUMN "video_platform"."start_offline_push" IS '推送离线通道';
COMMENT ON COLUMN "video_platform"."status" IS '注册状态(0=未注册/1=已注册)';
COMMENT ON COLUMN "video_platform"."transport" IS '传输协议(UDP/TCP)';
COMMENT ON COLUMN "video_platform"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_platform"."updated_time" IS '更新时间';
COMMENT ON COLUMN "video_platform"."username" IS '认证用户名';

CREATE OR REPLACE INDEX "video_platform_idx_server_gb_id" ON "video_platform"("server_gb_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_platform_idx_online_status" ON "video_platform"("online_status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_platform_idx_enable" ON "video_platform"("enable" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_platform_idx_created_org_id" ON "video_platform"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_platform_catalog
-- ----------------------------
DROP TABLE IF EXISTS "video_platform_catalog";
CREATE TABLE "video_platform_catalog"
(
    "id" BIGINT NOT NULL,
    "platform_id" BIGINT NOT NULL,
    "name" VARCHAR(100) DEFAULT NULL,
    "gb_id" VARCHAR(50) DEFAULT NULL,
    "parent_id" BIGINT DEFAULT NULL,
    "catalog_type" TINYINT DEFAULT '0',
    "civil_code" VARCHAR(50) DEFAULT NULL,
    "sort_order" INT DEFAULT '0',
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    CONSTRAINT "video_platform_catalog_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_platform_catalog" IS '视频级联目录表';
COMMENT ON COLUMN "video_platform_catalog"."catalog_type" IS '目录类型(0=行政区划/1=业务分组/2=虚拟组织)';
COMMENT ON COLUMN "video_platform_catalog"."civil_code" IS '行政区划编码';
COMMENT ON COLUMN "video_platform_catalog"."created_by" IS '创建人';
COMMENT ON COLUMN "video_platform_catalog"."created_org_id" IS '所属组织ID';
COMMENT ON COLUMN "video_platform_catalog"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_platform_catalog"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_platform_catalog"."gb_id" IS '目录国标编号';
COMMENT ON COLUMN "video_platform_catalog"."id" IS '主键';
COMMENT ON COLUMN "video_platform_catalog"."name" IS '目录名称';
COMMENT ON COLUMN "video_platform_catalog"."parent_id" IS '上级目录ID(顶层为空)';
COMMENT ON COLUMN "video_platform_catalog"."platform_id" IS '级联平台ID';
COMMENT ON COLUMN "video_platform_catalog"."sort_order" IS '排序序号';
COMMENT ON COLUMN "video_platform_catalog"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_platform_catalog"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "video_platform_catalog_idx_platform_id" ON "video_platform_catalog"("platform_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_platform_catalog_idx_parent_id" ON "video_platform_catalog"("parent_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_platform_catalog_idx_created_org_id" ON "video_platform_catalog"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_platform_channel
-- ----------------------------
DROP TABLE IF EXISTS "video_platform_channel";
CREATE TABLE "video_platform_channel"
(
    "id" BIGINT NOT NULL,
    "platform_id" BIGINT NOT NULL,
    "device_channel_id" BIGINT DEFAULT NULL,
    "catalog_id" BIGINT DEFAULT NULL,
    "device_identification" VARCHAR(50) DEFAULT NULL,
    "channel_identification" VARCHAR(50) DEFAULT NULL,
    "custom_name" VARCHAR(100) DEFAULT NULL,
    "custom_gb_id" VARCHAR(50) DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    CONSTRAINT "video_platform_channel_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_platform_channel" IS '视频级联平台通道关联表';
COMMENT ON COLUMN "video_platform_channel"."catalog_id" IS '所属目录ID';
COMMENT ON COLUMN "video_platform_channel"."channel_identification" IS '通道国标编号';
COMMENT ON COLUMN "video_platform_channel"."created_by" IS '创建人';
COMMENT ON COLUMN "video_platform_channel"."created_org_id" IS '所属组织ID';
COMMENT ON COLUMN "video_platform_channel"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_platform_channel"."custom_gb_id" IS '自定义国标编号';
COMMENT ON COLUMN "video_platform_channel"."custom_name" IS '自定义名称';
COMMENT ON COLUMN "video_platform_channel"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_platform_channel"."device_channel_id" IS '设备通道表ID';
COMMENT ON COLUMN "video_platform_channel"."device_identification" IS '设备国标编号';
COMMENT ON COLUMN "video_platform_channel"."id" IS '主键';
COMMENT ON COLUMN "video_platform_channel"."platform_id" IS '级联平台ID';
COMMENT ON COLUMN "video_platform_channel"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_platform_channel"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "video_platform_channel_idx_platform_id" ON "video_platform_channel"("platform_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_platform_channel_idx_device_identification" ON "video_platform_channel"("device_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_platform_channel_idx_created_org_id" ON "video_platform_channel"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_record_file
-- ----------------------------
DROP TABLE IF EXISTS "video_record_file";
CREATE TABLE "video_record_file"
(
    "id" BIGINT NOT NULL,
    "plan_id" BIGINT DEFAULT NULL,
    "device_identification" VARCHAR(50) DEFAULT NULL,
    "channel_identification" VARCHAR(50) DEFAULT NULL,
    "stream_identification" VARCHAR(100) DEFAULT NULL,
    "app" VARCHAR(50) DEFAULT NULL,
    "media_identification" VARCHAR(50) DEFAULT NULL,
    "file_name" VARCHAR(200) DEFAULT NULL,
    "file_id" BIGINT DEFAULT NULL,
    "file_size" BIGINT NOT NULL DEFAULT '0',
    "file_format" VARCHAR(20) NOT NULL DEFAULT 'mp4',
    "duration" INT NOT NULL DEFAULT '0',
    "start_time" TIMESTAMP(0) DEFAULT NULL,
    "end_time" TIMESTAMP(0) DEFAULT NULL,
    "thumbnail_file_id" BIGINT DEFAULT NULL,
    "file_status" TINYINT NOT NULL DEFAULT '0',
    "extend_params" CLOB,
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    CONSTRAINT "video_record_file_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_record_file" IS '视频录像文件表';
COMMENT ON COLUMN "video_record_file"."app" IS '应用名';
COMMENT ON COLUMN "video_record_file"."channel_identification" IS '通道国标编号';
COMMENT ON COLUMN "video_record_file"."created_by" IS '创建人';
COMMENT ON COLUMN "video_record_file"."created_org_id" IS '所属组织ID';
COMMENT ON COLUMN "video_record_file"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_record_file"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_record_file"."device_identification" IS '设备国标编号';
COMMENT ON COLUMN "video_record_file"."duration" IS '时长(秒)';
COMMENT ON COLUMN "video_record_file"."end_time" IS '录像结束时间';
COMMENT ON COLUMN "video_record_file"."extend_params" IS '扩展参数(JSON)';
COMMENT ON COLUMN "video_record_file"."file_format" IS '文件格式(mp4/flv/ts)';
COMMENT ON COLUMN "video_record_file"."file_id" IS '文件ID(关联base服务File表)';
COMMENT ON COLUMN "video_record_file"."file_name" IS '文件名';
COMMENT ON COLUMN "video_record_file"."file_size" IS '文件大小(字节)';
COMMENT ON COLUMN "video_record_file"."file_status" IS '文件状态(0=录制中/1=已完成/2=已过期/3=已删除)';
COMMENT ON COLUMN "video_record_file"."id" IS '主键';
COMMENT ON COLUMN "video_record_file"."media_identification" IS '流媒体服务器标识';
COMMENT ON COLUMN "video_record_file"."plan_id" IS '关联录像计划ID(手动录制时为空)';
COMMENT ON COLUMN "video_record_file"."start_time" IS '录像开始时间';
COMMENT ON COLUMN "video_record_file"."stream_identification" IS '流标识';
COMMENT ON COLUMN "video_record_file"."thumbnail_file_id" IS '缩略图文件ID(关联base服务File表)';
COMMENT ON COLUMN "video_record_file"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_record_file"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "video_record_file_idx_plan_id" ON "video_record_file"("plan_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_record_file_idx_device_channel" ON "video_record_file"("device_identification" ASC,"channel_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_record_file_idx_start_time" ON "video_record_file"("start_time" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_record_file_idx_end_time" ON "video_record_file"("end_time" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_record_file_idx_file_id" ON "video_record_file"("file_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_record_file_idx_file_status" ON "video_record_file"("file_status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_record_file_idx_media_identification" ON "video_record_file"("media_identification" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_record_file_idx_created_org_id" ON "video_record_file"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_record_plan
-- ----------------------------
DROP TABLE IF EXISTS "video_record_plan";
CREATE TABLE "video_record_plan"
(
    "id" BIGINT NOT NULL,
    "plan_name" VARCHAR(100) NOT NULL,
    "plan_type" TINYINT NOT NULL DEFAULT '0',
    "media_identification" VARCHAR(50) DEFAULT NULL,
    "record_format" VARCHAR(20) NOT NULL DEFAULT 'mp4',
    "segment_duration" INT NOT NULL DEFAULT '3600',
    "retention_days" INT NOT NULL DEFAULT '30',
    "storage_path" VARCHAR(500) DEFAULT NULL,
    "plan_status" TINYINT NOT NULL DEFAULT '0',
    "schedule_rule" CLOB,
    "extend_params" CLOB,
    "remark" VARCHAR(500) DEFAULT NULL,
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    CONSTRAINT "video_record_plan_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_record_plan" IS '视频录像计划表';
COMMENT ON COLUMN "video_record_plan"."created_by" IS '创建人';
COMMENT ON COLUMN "video_record_plan"."created_org_id" IS '所属组织ID';
COMMENT ON COLUMN "video_record_plan"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_record_plan"."deleted" IS '逻辑删除(0=正常/1=删除)';
COMMENT ON COLUMN "video_record_plan"."extend_params" IS '扩展参数(JSON)';
COMMENT ON COLUMN "video_record_plan"."id" IS '主键';
COMMENT ON COLUMN "video_record_plan"."media_identification" IS '指定流媒体服务器标识(空则自动分配)';
COMMENT ON COLUMN "video_record_plan"."plan_name" IS '计划名称';
COMMENT ON COLUMN "video_record_plan"."plan_status" IS '计划状态(0=停用/1=启用)';
COMMENT ON COLUMN "video_record_plan"."plan_type" IS '计划类型(0=设备录像/1=云端录像)';
COMMENT ON COLUMN "video_record_plan"."record_format" IS '录像格式(mp4/flv/ts)';
COMMENT ON COLUMN "video_record_plan"."remark" IS '备注';
COMMENT ON COLUMN "video_record_plan"."retention_days" IS '保留天数(超期自动清理)';
COMMENT ON COLUMN "video_record_plan"."schedule_rule" IS '调度规则(JSON，支持周期/CRON/一次性)';
COMMENT ON COLUMN "video_record_plan"."segment_duration" IS '分段时长(秒，默认1小时)';
COMMENT ON COLUMN "video_record_plan"."storage_path" IS '存储路径(空则用默认路径)';
COMMENT ON COLUMN "video_record_plan"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_record_plan"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "video_record_plan_idx_plan_status" ON "video_record_plan"("plan_status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_record_plan_idx_plan_type" ON "video_record_plan"("plan_type" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_record_plan_idx_created_org_id" ON "video_record_plan"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

-- ----------------------------
-- Table structure for video_sip_config
-- ----------------------------
DROP TABLE IF EXISTS "video_sip_config";
CREATE TABLE "video_sip_config"
(
    "id" BIGINT NOT NULL,
    "config_name" VARCHAR(100) NOT NULL,
    "sip_id" VARCHAR(50) NOT NULL,
    "sip_domain" VARCHAR(50) NOT NULL,
    "sip_password" VARCHAR(100) DEFAULT NULL,
    "sip_server_address" VARCHAR(200) DEFAULT NULL,
    "bind_ip" VARCHAR(500) DEFAULT NULL,
    "is_default" TINYINT NOT NULL DEFAULT '0',
    "register_interval" INT DEFAULT NULL,
    "status" TINYINT NOT NULL DEFAULT '1',
    "remark" VARCHAR(500) DEFAULT NULL,
    "created_by" BIGINT DEFAULT NULL,
    "created_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT DEFAULT NULL,
    "updated_time" TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    "created_org_id" BIGINT DEFAULT NULL,
    "deleted" TINYINT NOT NULL DEFAULT '0',
    CONSTRAINT "video_sip_config_PRIMARY" NOT CLUSTER PRIMARY KEY("id")
) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "video_sip_config" IS '租户SIP服务配置';
COMMENT ON COLUMN "video_sip_config"."bind_ip" IS '绑定IP(多网卡隔离场景下监听的网卡IP，逗号分隔，留空=不限制)';
COMMENT ON COLUMN "video_sip_config"."config_name" IS '配置名称';
COMMENT ON COLUMN "video_sip_config"."created_by" IS '创建人';
COMMENT ON COLUMN "video_sip_config"."created_org_id" IS '创建组织ID';
COMMENT ON COLUMN "video_sip_config"."created_time" IS '创建时间';
COMMENT ON COLUMN "video_sip_config"."deleted" IS '逻辑删除';
COMMENT ON COLUMN "video_sip_config"."id" IS '主键';
COMMENT ON COLUMN "video_sip_config"."is_default" IS '是否默认(1=是)';
COMMENT ON COLUMN "video_sip_config"."register_interval" IS '注册有效期(秒)';
COMMENT ON COLUMN "video_sip_config"."remark" IS '备注';
COMMENT ON COLUMN "video_sip_config"."sip_domain" IS 'SIP域(SIP服务器编号前10位，行政区划码)';
COMMENT ON COLUMN "video_sip_config"."sip_id" IS 'SIP服务器编号(设备端GB28181配置中的"SIP服务器编号"，20位数字，不能填设备自己的国标编号)';
COMMENT ON COLUMN "video_sip_config"."sip_password" IS 'SIP认证密码(AES加密，与设备端"SIP认证密码"一致)';
COMMENT ON COLUMN "video_sip_config"."sip_server_address" IS 'SIP服务器地址(设备端"SIP服务器IP/地址"，域名或IP，集群可填Nginx VIP)';
COMMENT ON COLUMN "video_sip_config"."status" IS '状态(0=禁用/1=启用)';
COMMENT ON COLUMN "video_sip_config"."updated_by" IS '更新人';
COMMENT ON COLUMN "video_sip_config"."updated_time" IS '更新时间';

CREATE OR REPLACE INDEX "video_sip_config_idx_sip_id" ON "video_sip_config"("sip_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_sip_config_idx_status" ON "video_sip_config"("status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;
CREATE OR REPLACE INDEX "video_sip_config_idx_created_org_id" ON "video_sip_config"("created_org_id" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

