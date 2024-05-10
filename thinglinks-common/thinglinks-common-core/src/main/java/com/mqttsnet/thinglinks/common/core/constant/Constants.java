package com.mqttsnet.thinglinks.common.core.constant;

/**
 * 通用常量信息
 *
 * @author thinglinks
 */
public class Constants {

    /**
     * 项目前缀
     */
    public static final String PROJECT_PREFIX = "thinglinks";
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * RMI 远程方法调用
     */
    public static final String LOOKUP_RMI = "rmi://";

    /**
     * LDAP 远程方法调用
     */
    public static final String LOOKUP_LDAP = "ldap://";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 成功标记
     */
    public static final Integer SUCCESS = 200;

    /**
     * 失败标记
     */
    public static final Integer FAIL = 500;

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 启用
     */
    public static final String ENABLE = "0";

    /**
     * 禁用
     */
    public static final String DISABLE = "1";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 当前记录起始索引
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 排序列
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 排序的方向 "desc" 或者 "asc".
     */
    public static final String IS_ASC = "isAsc";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 验证码有效期（分钟）
     */
    public static final long CAPTCHA_EXPIRATION = 2;

    /**
     * 令牌有效期（分钟）
     */
    public final static long TOKEN_EXPIRE = 720;

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * 重复提交token key
     */
    public static final String IDEMPOTENT_TOKEN_NAME = "idempotentToken";

    /**
     * 文件上传类型
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * 重复提交 cache key
     */
    public static final String RESUBMIT_URL_KEY = "resubmit_url:";

    /**
     * 下划线
     */
    public static final String UNDERLINE = "_";

    /**
     * 分隔符
     */
    public static final String SEPARATOR = ",";


    /**
     * 分号
     */
    public static final String SEMICOLON = ";";

    /**
     * all(全部)
     */
    public static final String ALL = "all";


}
