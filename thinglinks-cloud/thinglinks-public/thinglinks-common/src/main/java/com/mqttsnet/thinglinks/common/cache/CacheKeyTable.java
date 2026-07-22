package com.mqttsnet.thinglinks.common.cache;

/**
 * 用于同一管理和生成缓存的key， 避免多个项目使用的key重复
 * <p>
 * 使用@Cacheable时， 其value值一定要在此处指定
 *
 * @author mqttsnet
 * @date 2020/10/21
 */
public interface CacheKeyTable {

    /**
     * 验证码 前缀
     * 完整key: captcha:{key} -> str
     */
    String CAPTCHA = "captcha";
    /**
     * token 前缀
     * 完整key： token:{token} -> userid
     */
    String TOKEN = "token";


    //------------------

    // 权限系统缓存 start
    /**
     * 总 登录次数
     * total_login_pv:{TENANT} -> Long
     */
    String TOTAL_LOGIN_PV = "total_login_pv";
    /**
     * 今日 登录次数
     * today_login_pv:{TENANT}:{today} -> Long
     */
    String TODAY_LOGIN_PV = "today_login_pv";
    /**
     * 今日登录总ip
     * today_login_iv:{TENANT}:{today} -> int
     */
    String TODAY_LOGIN_IV = "today_login_iv";
    /**
     * 今日登录总ip
     * TOTAL_LOGIN_IV:{TENANT} -> int
     */
    String TOTAL_LOGIN_IV = "total_login_iv";
    /**
     * 今日 PV
     * today_pv:{TENANT} -> int
     */
    String TODAY_PV = "today_pv";
    /**
     * 总 PV
     * total_pv:{TENANT} -> int
     */
    String TOTAL_PV = "total_pv";
    /**
     * 最近10访问记录
     * login_log_tenday:{TENANT}:{today}:{account} -> Map
     */
    String LOGIN_LOG_TEN_DAY = "login_log_tenday";
    /**
     * 登录总次数
     * login_log_browser:{TENANT} -> Map
     */
    String LOGIN_LOG_BROWSER = "login_log_browser";
    /**
     * 登录总次数
     * login_log_system{TENANT} -> Map
     */
    String LOGIN_LOG_SYSTEM = "login_log_system";
    /**
     * 参数 前缀
     * 完整key: parameter_key:{key} -> obj
     */
    String PARAMETER_KEY = "parameter_key";
    /**
     * 在用用户 前缀
     * 完整key: online:{userid} -> token (String)
     */
    String ONLINE = "online";
    /**
     * 用户token 前缀
     * 完整key: token_user_id:{token} -> userid (Long)
     */
    String TOKEN_USER_ID = "token_user_id";
    /**
     * 用户注册 前缀
     * 完整key: register:{注册类型}:{手机号}
     */
    String REGISTER_USER = "register";

    interface System {

        /**
         * 租户
         */
        String TENANT = "def_tenant";
        /**
         * 应用
         */
        String APPLICATION = "def_application";
        /**
         * 默认字典
         */
        String DICT = "def_dict";
        /**
         * 默认参数
         */
        String DEF_PARAMETER = "def_parameter";

        /**
         * 用户 前缀
         */
        String DEF_USER = "def_user";
        /**
         * 客户端
         */
        String DEF_CLIENT = "def_client";

        /**
         * 租户拥有的资源
         */
        String TENANT_APPLICATION_RESOURCE = "t_a_r";
        /**
         * 租户拥有的应用
         */
        String TENANT_APPLICATION = "t_a";

        /**
         * 资源
         */
        String RESOURCE = "dr";
        /**
         * 资源接口
         */
        String RESOURCE_API = "dra";
        String ALL_RESOURCE_API = "all_dra";
    }


    /**
     * 消息服务缓存 start
     */
    interface Base {

        /**
         * 租户自定义字典
         */
        String BASE_DICT = "base_dict";
        /**
         * 组织 前缀
         */
        String BASE_ORG = "base_org";
        /**
         * 岗位 前缀
         */
        String BASE_POSITION = "base_position";
        /**
         * 员工 前缀
         */
        String BASE_EMPLOYEE = "base_employee";
        /**
         * 全局员工 前缀
         */
        String DEF_USER_TENANT = "def_user_tenant";

        /**
         * 角色 前缀
         * 完整key: role:{roleId}
         */
        String ROLE = "role";
        /**
         * 角色拥有那些资源 前缀
         * 完整key: role_resource:{ROLE_ID} -> [RESOURCE_ID, ...]
         */
        String ROLE_RESOURCE = "role_resource";
        /**
         * 员工拥有那些角色 前缀
         * 完整key: employee_role:{EMPLOYEE_ID} -> [ROLE_ID, ...]
         */
        String EMPLOYEE_ROLE = "employee_role";

        /**
         * 角色拥有那些组织 前缀
         * 完整key: employee_org:{EMPLOYEE_ID} -> [ORG_ID, ...]
         */
        String EMPLOYEE_ORG = "employee_org";

        /**
         * 角色拥有那些组织 前缀
         * 完整key: org_role:{ORG_ID} -> [ROLE_ID, ...]
         */
        String ORG_ROLE = "org_role";
    }
    // 消息服务缓存 end

    // Mqs物联网业务系统缓存 start
    interface Mqs {


        /**
         * WS 协议 Session 前缀
         */
        String PROTOCOL_WS_SESSION = "def_protocol_ws_session";

        /**
         * 协议总线指标计数器(集群级)。
         * <p>Hash 桶按 (tenantId, date, dimension) 维度,field 按 label 组合,value 累计计数。
         * <p>完整 key: mqs:{tenantId}:bus_stats_counter:bucket:obj:{date}:{dimension}
         */
        String BUS_STATS_COUNTER = "bus_stats_counter";

        /**
         * 协议总线时延 ZSet(P99 计算 / 滚动窗口分位查询)。
         * <p>完整 key: mqs:{tenantId}:bus_stats_latency:zset:obj:{date}:{stage}
         */
        String BUS_STATS_LATENCY = "bus_stats_latency";

        /**
         * 设备心跳补偿节流标记(同设备 60s 内最多一次 status 修正写).
         * <p>{@code DeviceHeartbeatStage} 用:PING 通过 ttl ≤ 0 判断未节流时,
         * SET 节流 key + 调 facade 强制把 status 写为 ONLINE(无 hlc,不污染 lifecycle stream),
         * 自愈 link 偶发写失败或被节流误判导致的 OFFLINE 假象.
         * <p>完整 key: mqs:{tenantId}:heartbeat_reconcile:id:string:{clientId}
         */
        String HEARTBEAT_RECONCILE = "heartbeat_reconcile";

    }

    // Mqs物联网业务系统缓存 end

    // Broker 协议接入层缓存 start
    interface Broker {

        /**
         * WS 设备 session 全量信息:clientId → 设备连接渠道信息(JSON)。
         * <p>存设备整条会话的渠道信息(clientId / 租户 / 设备&产品标识 / 协议 / 接入节点 /
         * channelId / 接入时间 / 最近活跃时间),供<b>多节点共享读取</b>:任一 broker / 业务节点
         * 都能据此判断设备是否在线、连在哪个节点、走什么渠道。
         * <p><b>不用于下行路由</b> —— 下行命令走 RocketMQ 广播(各节点查本地 session 投递),
         * 不再依赖此 key 里的节点地址做点对点转发(节点地址仅作诊断展示)。
         * <p>TTL = 心跳超时(默认 90s);broker @OnOpen 写入,@OnClose 清除,心跳续期。
         */
        String WS_SESSION = "def_ws_session";

    }

    // Broker 协议接入层缓存 end

    // Link物联网业务系统缓存 start
    interface Link {

        /**
         * 全局设备档案 前缀
         */
        String DEVICE = "def_device";

        /**
         * 全局设备ACL规则 前缀
         */
        String DEVICE_ACL_RULE = "def_device_acl_rule";

        /**
         * 全局产品信息 前缀
         */
        String PRODUCT = "def_product";

        /**
         * 全局产品模型 前缀
         */
        String PRODUCT_MODEL = "def_product_model";

        /**
         * 全局产品模型超级表 前缀
         */
        String PRODUCT_MODEL_SUPER_TABLE = "def_product_model_super_table";


        /**
         * 全局设备数据收集池 前缀
         */
        String DEVICE_DATA_COLLECTION_POOL = "def_device_data_collection_pool";

        /**
         * 全局设备动作收集池 前缀
         */
        String DEVICE_ACTION_COLLECTION_POOL = "def_device_action_collection_pool";


        /**
         * 全局设备上行原始数据收集池 前缀
         */
        String ASCENDING_DEVICE_ORIGINAL_DATA_COLLECTION_POOL = "def_ascending_device_original_data_collection_pool";

        /**
         * 全局设备下行原始数据收集池 前缀
         */
        String DESCENDING_DEVICE_ORIGINAL_DATA_COLLECTION_POOL = "def_descending_device_original_data_collection_pool";


        /**
         * 全局上行数据计数器 前缀
         */
        String UP_LINK_DATA_COUNTER = "def_up_link_data_counter";

        /**
         * 全局下行数据计数器 前缀
         */
        String DOWN_LINK_DATA_COUNTER = "def_down_link_data_counter";


        /**
         * OTA升级任务执行器偏移量 前缀
         */
        String OTA_UPGRADE_TASK_EXECUTOR_OFFSET = "def_ota_upgrade_task_executor_offset";

        /**
         * OTA升级记录 前缀
         */
        String OTA_UPGRADE_RECORDS = "def_ota_upgrade_records";
    }
    // Link物联网业务系统缓存 end

    // Rule 规则服务缓存 start
    interface Rule {

        /**
         * 默认插件信息  前缀
         */
        String DEF_PLUGIN_INFO = "def_plugin_info";


        /**
         * 默认规则脚本  前缀
         */
        String DEF_GROOVY_SCRIPT = "def_groovy_script";

        /**
         * 规则脚本执行统计  前缀(按脚本唯一键聚 HASH:total/success/fail)
         */
        String GROOVY_SCRIPT_EXEC_STAT = "groovy_script_exec_stat";

        /**
         * 桥接规则(启用列表) 前缀
         * <p>完整 key 形如: rule:data_bridge_rule:app.dir:obj:{tenantId}:{appId}:{direction} -> List&lt;DataBridge&gt;
         * <p>BridgeRuleCache 走 CachePlusOps(Redis),事件驱动 del 失效。
         */
        String DATA_BRIDGE_RULE = "data_bridge_rule";

        /**
         * 数据桥接-数据源 前缀
         * <p>完整 key 形如: rule:data_source:id:obj:{tenantId}:{dataSourceId} -> DataSource
         * <p>SinkDispatcher 热路径取数据源时走此 key;DataSourceService update/delete 时主动 del。
         */
        String DATA_SOURCE = "data_source";

        /** 规则事件触发索引桶 */
        String RULE_TRIGGER_INDEX = "rule_trigger_index";

        /** 事件触发规则详情缓存 */
        String RULE_TRIGGER_DETAILS = "rule_trigger_details";

        /** 设备最新物模型快照 */
        String RULE_LATEST_SNAPSHOT = "rule_latest_snapshot";

        /** 规则事件防抖计数与首值快照 */
        String RULE_ANTI_SHAKE = "rule_anti_shake";

        /** 规则动作执行冷却闸 */
        String RULE_ACTION_COOLDOWN = "rule_action_cooldown";

    }
    // Rule规则服务缓存 end

    // Card物联卡业务系统缓存 start
    interface Card {

        /**
         * 移动 One Link平台 tokenKey  前缀
         */
        String DEF_CHANNEL_ONE_LINK_TOKEN_KEY = "def_channel_one_link_token_key";

    }
    // Card物联卡业务系统缓存 end


    // Video视频流系统缓存 start
    interface Video {


        /**
         * 全局流媒体服务器 前缀
         */
        String MEDIA_SERVER = "def_media_server";

        /**
         * 全局流媒体服务器 HOOK 前缀
         */
        String MEDIA_SERVER_HOOK = "def_media_server_hook";


        /**
         * 全局设备信息 前缀
         */
        String DEVICE_INFO = "def_device_info";

        /**
         * 全局设备通道信息 前缀
         */
        String DEVICE_CHANNEL = "def_device_channel";


        /**
         * 全局 SIP Session Call信息 前缀
         */
        String SIP_SESSION_CALL = "def_sip_session_call";

        /**
         * SSRC 池 前缀（Hash：已用/可用集合）
         */
        String SSRC_POOL = "def_ssrc_pool";

        /**
         * SSRC 事务 前缀（关联设备/通道/流）
         */
        String SSRC_TRANSACTION = "def_ssrc_transaction";

        /**
         * 流信息 前缀（多协议 URL 缓存）
         */
        String STREAM_INFO = "def_stream_info";

        /**
         * RTP 端口池 前缀
         */
        String RTP_PORT_POOL = "def_rtp_port_pool";

        /**
         * SIP 服务信息 前缀（集群实例注册）
         */
        String SIP_SERVER_INFO = "def_sip_server_info";

        /**
         * 租户 SIP 配置 前缀（全局 Hash，field=sipId）
         */
        String SIP_TENANT_CONFIG = "def_sip_tenant_config";

        /**
         * SIP 事务订阅 前缀（租户维度，等待设备 SIP 响应）
         */
        String SIP_SUBSCRIBE = "def_sip_subscribe";

        /**
         * MESSAGE 消息订阅 前缀（租户维度，等待设备消息应答）
         */
        String MSG_SUBSCRIBE = "def_msg_subscribe";

        /**
         * Hook 订阅 前缀（租户维度，ZLM Hook 回调匹配）
         */
        String HOOK_SUBSCRIBE = "def_hook_subscribe";

        /**
         * 异步请求结果 前缀（租户维度，前端等待 SIP 异步结果）
         */
        String DEFERRED_RESULT = "def_deferred_result";

        /**
         * 流恢复重试 前缀（租户维度，断流自动恢复计数）
         */
        String STREAM_RECOVERY = "def_stream_recovery";

        /**
         * 平台级联注册 前缀（租户维度，上联平台注册状态）
         */
        String PLATFORM_REGISTER = "def_platform_register";

        /**
         * 平台订阅 前缀（租户维度，上级平台 Catalog/Position 订阅）
         */
        String SUBSCRIBE_HOLDER = "def_subscribe_holder";

        /**
         * JT1078 连接 前缀（租户维度，车载终端连接池）
         */
        String JT1078_CONN = "def_jt1078_conn";

        /**
         * ISUP 连接 前缀（租户维度，海康设备连接池）
         */
        String ISUP_CONN = "def_isup_conn";

    }
    // Video视频流系统缓存 end
}
