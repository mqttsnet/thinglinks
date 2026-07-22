package com.mqttsnet.thinglinks.productpublishrecord.vo.ddl;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TDengine 超级表字段定义快照 ── {@link PublishDdlItemVO} 的子项,用于前端"表结构"展示。
 * 来源是 CREATE STABLE 成功后 {@code DESCRIBE STABLE} 反查(单一真相源,反映 TD 对字段的隐式调整),不是提交时的 DTO。
 *
 * @author mqttsnet
 * @see PublishDdlItemVO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "DdlFieldVO", description = "TDengine 超级表字段定义快照")
public class DdlFieldVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 字段名(如 ts / event_time / temperature)。 */
    @Schema(description = "字段名")
    private String field;

    /** 字段类型(TDengine 实际类型:TIMESTAMP / INT / NCHAR / BINARY / ...)。 */
    @Schema(description = "字段类型")
    private String type;

    /** 字段长度(变长类型 NCHAR/BINARY 的字符数;定长类型为 null)。 */
    @Schema(description = "字段长度(字符数)")
    private Integer length;

    /**
     * 字段实际字节数,前端"占行级上限的比例"用。计算规则:NCHAR=length×4(UTF-32)、
     * BINARY/VARCHAR/VARBINARY=length×1、TIMESTAMP/BIGINT/DOUBLE=8、INT/FLOAT=4、SMALLINT=2、TINYINT/BOOL=1。
     */
    @Schema(description = "实际字节数")
    private Integer bytes;
}
