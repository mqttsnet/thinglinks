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


CREATE TABLE "card_pay"
(
    "id" DECIMAL NOT NULL,
    "channel_id" BIGINT,
    "card_id" BIGINT,
    "msisdn" VARCHAR(50),
    "iccid" VARCHAR(50),
    "openid" VARCHAR(50),
    "platform" SMALLINT,
    "status" SMALLINT,
    "order_no" VARCHAR(50),
    "transaction_id" VARCHAR(100),
    "sign" VARCHAR(200),
    "remark" VARCHAR(200),
    "mode_payment" SMALLINT DEFAULT 0,
    "created_by" BIGINT,
    "created_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "created_org_id" BIGINT,
    CONSTRAINT "card_pay_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "card_pay" IS '物联网卡充值记录表';
COMMENT ON COLUMN "card_pay"."card_id" IS '卡id';
COMMENT ON COLUMN "card_pay"."channel_id" IS '渠道';
COMMENT ON COLUMN "card_pay"."created_by" IS '创建人';
COMMENT ON COLUMN "card_pay"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "card_pay"."created_time" IS '创建时间';
COMMENT ON COLUMN "card_pay"."iccid" IS 'iccid';
COMMENT ON COLUMN "card_pay"."id" IS 'id主键';
COMMENT ON COLUMN "card_pay"."mode_payment" IS '支付类型: 0微信 1支付宝';
COMMENT ON COLUMN "card_pay"."msisdn" IS '卡号';
COMMENT ON COLUMN "card_pay"."openid" IS 'openid';
COMMENT ON COLUMN "card_pay"."order_no" IS '平台生成订单号';
COMMENT ON COLUMN "card_pay"."platform" IS '平台:  0小程序(个人用户) 1 H5(平台用户)  2 平台充值';
COMMENT ON COLUMN "card_pay"."remark" IS '备注';
COMMENT ON COLUMN "card_pay"."sign" IS '加密';
COMMENT ON COLUMN "card_pay"."status" IS '0 未支付 1已支付 2充值失败 3退款';
COMMENT ON COLUMN "card_pay"."transaction_id" IS '微信交易订单号';
COMMENT ON COLUMN "card_pay"."updated_by" IS '更新人';
COMMENT ON COLUMN "card_pay"."updated_time" IS '更新时间';


CREATE OR REPLACE  INDEX "card_pay_msisdn" ON "card_pay"("msisdn" ASC,"status" ASC) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

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


CREATE TABLE "com_appendix"
(
    "id" BIGINT NOT NULL,
    "biz_id" BIGINT,
    "biz_type" VARCHAR(255),
    "file_type" VARCHAR(10),
    "bucket" VARCHAR(255),
    "path" VARCHAR(255),
    "original_file_name" VARCHAR(255),
    "content_type" VARCHAR(255),
    "size_" BIGINT DEFAULT 0,
    "created_time" TIMESTAMP(0),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "com_appendix_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "com_appendix" IS '业务附件';
COMMENT ON COLUMN "com_appendix"."biz_id" IS '业务id';
COMMENT ON COLUMN "com_appendix"."biz_type" IS '业务类型;同一个业务，不同的字段，需要分别设置不同的业务类型';
COMMENT ON COLUMN "com_appendix"."bucket" IS '桶';
COMMENT ON COLUMN "com_appendix"."content_type" IS '文件类型';
COMMENT ON COLUMN "com_appendix"."created_by" IS '创建人';
COMMENT ON COLUMN "com_appendix"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "com_appendix"."created_time" IS '创建时间';
COMMENT ON COLUMN "com_appendix"."file_type" IS '文件类型;#FileType{IMAGE:图片;VIDEO:视频;AUDIO:音频;DOC:文档;OTHER:其他;}';
COMMENT ON COLUMN "com_appendix"."id" IS 'ID';
COMMENT ON COLUMN "com_appendix"."original_file_name" IS '原始文件名';
COMMENT ON COLUMN "com_appendix"."path" IS '文件相对地址';
COMMENT ON COLUMN "com_appendix"."size_" IS '大小';
COMMENT ON COLUMN "com_appendix"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "com_appendix"."updated_time" IS '最后修改时间';


CREATE TABLE "com_file"
(
    "id" BIGINT NOT NULL,
    "biz_type" VARCHAR(255),
    "file_type" VARCHAR(10) DEFAULT 'OTHER',
    "storage_type" VARCHAR(30) DEFAULT 'LOCAL',
    "bucket" VARCHAR(255),
    "path" VARCHAR(255),
    "url" VARCHAR(255),
    "unique_file_name" VARCHAR(255),
    "file_md5" VARCHAR(255),
    "original_file_name" VARCHAR(255),
    "content_type" VARCHAR(255),
    "suffix" VARCHAR(255),
    "size_" BIGINT DEFAULT 0,
    "created_time" TIMESTAMP(0),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "com_file_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "com_file" IS '增量文件上传日志';
COMMENT ON COLUMN "com_file"."biz_type" IS '业务类型;同一个业务，不同的字段，需要分别设置不同的业务类型';
COMMENT ON COLUMN "com_file"."bucket" IS '桶';
COMMENT ON COLUMN "com_file"."content_type" IS '文件类型';
COMMENT ON COLUMN "com_file"."created_by" IS '创建人';
COMMENT ON COLUMN "com_file"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "com_file"."created_time" IS '创建时间';
COMMENT ON COLUMN "com_file"."file_md5" IS '文件md5';
COMMENT ON COLUMN "com_file"."file_type" IS '文件类型;#FileType{IMAGE:图片;VIDEO:视频;AUDIO:音频;DOC:文档;OTHER:其他;}';
COMMENT ON COLUMN "com_file"."id" IS 'ID';
COMMENT ON COLUMN "com_file"."original_file_name" IS '原始文件名';
COMMENT ON COLUMN "com_file"."path" IS '文件相对地址';
COMMENT ON COLUMN "com_file"."size_" IS '大小';
COMMENT ON COLUMN "com_file"."storage_type" IS '存储类型;#FileStorageType{LOCAL:本地;FAST_DFS:FastDFS;MIN_IO:MinIO;ALI_OSS:阿里云OSS;QINIU_OSS:七牛云OSS;HUAWEI_OSS:华为云OSS;}';
COMMENT ON COLUMN "com_file"."suffix" IS '后缀';
COMMENT ON COLUMN "com_file"."unique_file_name" IS '唯一文件名';
COMMENT ON COLUMN "com_file"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "com_file"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "com_file"."url" IS '文件访问地址';


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
    "device_tags" VARCHAR(255),
    "product_identification" VARCHAR(100),
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


CREATE TABLE "empowerment_record"
(
    "id" BIGINT NOT NULL,
    "app_id" VARCHAR(64),
    "empowerment_identification" VARCHAR(100),
    "empowerment_type" TINYINT DEFAULT 0,
    "startTime" TIMESTAMP(0),
    "endTime" TIMESTAMP(0),
    "outcome" CLOB,
    "feedback" CLOB,
    "status" TINYINT DEFAULT 0,
    "version" VARCHAR(255),
    "dependencies" VARCHAR(255),
    "remark" VARCHAR(500),
    "created_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "created_by" BIGINT,
    "updated_time" TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP(),
    "updated_by" BIGINT,
    "created_org_id" BIGINT,
    CONSTRAINT "empowerment_record_PRIMARY" NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "thinglinks_base_1", CLUSTERBTR) ;

COMMENT ON TABLE "empowerment_record" IS '赋能记录表';
COMMENT ON COLUMN "empowerment_record"."app_id" IS '应用ID';
COMMENT ON COLUMN "empowerment_record"."created_by" IS '创建人';
COMMENT ON COLUMN "empowerment_record"."created_org_id" IS '创建人组织';
COMMENT ON COLUMN "empowerment_record"."created_time" IS '创建时间';
COMMENT ON COLUMN "empowerment_record"."dependencies" IS '依赖关系';
COMMENT ON COLUMN "empowerment_record"."empowerment_identification" IS '赋能标识';
COMMENT ON COLUMN "empowerment_record"."empowerment_type" IS '赋能类型';
COMMENT ON COLUMN "empowerment_record"."endTime" IS '结束时间';
COMMENT ON COLUMN "empowerment_record"."feedback" IS '赋能反馈';
COMMENT ON COLUMN "empowerment_record"."id" IS 'id';
COMMENT ON COLUMN "empowerment_record"."outcome" IS '赋能结果';
COMMENT ON COLUMN "empowerment_record"."remark" IS '描述';
COMMENT ON COLUMN "empowerment_record"."startTime" IS '开始时间';
COMMENT ON COLUMN "empowerment_record"."status" IS '状态';
COMMENT ON COLUMN "empowerment_record"."updated_by" IS '最后修改人';
COMMENT ON COLUMN "empowerment_record"."updated_time" IS '最后修改时间';
COMMENT ON COLUMN "empowerment_record"."version" IS '版本';


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
    "product_version" VARCHAR(255),
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
COMMENT ON COLUMN "product"."product_version" IS '产品版本';
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


