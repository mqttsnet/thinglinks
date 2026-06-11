package com.mqttsnet.thinglinks.productpublishrecord.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发布记录 DDL 明细的操作类型枚举,对应 product_publish_record.ddl_summary JSON 数组里每条 item 的 operation 字段。
 * 序列化契约:Jackson 按 {@link Enum#name()} 序列化为字符串(如 "CREATE_STABLE"),跟老 JSON 数据 / 前端 i18n key 兼容,故不带 value。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.productpublishrecord.vo.ddl.PublishDdlItemVO
 */
@Getter
@AllArgsConstructor
@Schema(title = "PublishDdlOperationEnum", description = "发布记录 DDL 明细操作类型")
public enum PublishDdlOperationEnum {

    /** 创建超级表 ── 产品发布时,每个服务对应一个 stable。 */
    CREATE_STABLE("建表"),

    /** 删除超级表 ── 历史清理(PURGE_HISTORY)时使用。 */
    DROP_STABLE("删表"),

    /** 修改超级表 ── 预留,当前发布流程不会触发。 */
    ALTER_STABLE("改表"),
    ;

    /** 中文描述,日志 / 异常文案 / 兜底展示用。 */
    private final String desc;
}
