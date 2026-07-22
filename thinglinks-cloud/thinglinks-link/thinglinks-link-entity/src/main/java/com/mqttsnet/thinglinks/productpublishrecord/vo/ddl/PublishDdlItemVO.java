package com.mqttsnet.thinglinks.productpublishrecord.vo.ddl;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.mqttsnet.thinglinks.productpublishrecord.enumeration.PublishDdlOperationEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条 DDL 执行明细 ── 一次发布有 N 个 service 对应 N 条记录,每条独立 success / errorMsg / durationMs。
 * JSON 数组持久化在 product_publish_record.ddl_summary TEXT 字段。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "PublishDdlItemVO", description = "单条 TD时序数据库 DDL 执行明细")
public class PublishDdlItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 操作类型 ── {@link PublishDdlOperationEnum} 强类型,Jackson 序列化为 enum.name() 字符串。 */
    @Schema(description = "操作类型")
    private PublishDdlOperationEnum operation;

    /** super table 名。 */
    @Schema(description = "超级表名")
    private String stableName;

    /** 服务编码(便于用户从 stable 名反查到物模型 service)。 */
    @Schema(description = "服务编码")
    private String serviceCode;

    /** 服务名(可选)。 */
    @Schema(description = "服务名称")
    private String serviceName;

    /** 字段数(本次 DDL 涉及的 schema columns 数量,含 ts/event_time 系统字段)。 */
    @Schema(description = "字段数")
    private Integer columnCount;

    /** 是否执行成功。 */
    @Schema(description = "是否成功")
    private Boolean success;

    /** 失败原因 ── success=false 时来自 TD 驱动的 errMsg。 */
    @Schema(description = "失败原因")
    private String errorMsg;

    /** 执行耗时(毫秒)。 */
    @Schema(description = "执行耗时(毫秒)")
    private Long durationMs;

    /**
     * 本条 DDL 实际执行的时间戳(ISO 字符串 yyyy-MM-dd HH:mm:ss)。用 String 而非 LocalDateTime
     * 是为避开 JacksonTypeHandler 默认 ObjectMapper 不带 jsr310 的麻烦。Job 每次重试更新为最新时间戳。
     */
    @Schema(description = "本次执行时间(yyyy-MM-dd HH:mm:ss)")
    private String executedAt;

    /** 执行次数 ── 首次执行 = 1,Job 每次重试 += 1。 */
    @Schema(description = "执行次数(成功 = 1,失败重试累加)")
    private Integer attemptCount;

    /**
     * 表的普通字段列表(CREATE_STABLE 成功后由 {@code DESCRIBE STABLE} 反查得到)。
     * 存的是 TD 实际 schema(非提交的 DTO),单一真相源;DROP_STABLE / describe 失败时为 null。
     */
    @Schema(description = "表的普通字段列表(create 成功后 describe 反查)")
    private List<DdlFieldVO> schemaFields;

    /** 表的 tag 字段列表(同 schemaFields,由 describe 反查)。 */
    @Schema(description = "表的 tag 字段列表")
    private List<DdlFieldVO> tagsFields;

    /** 单行字段字节数合计(不含 tag,TD 行级上限 65531),前端算占比 chip 用。 */
    @Schema(description = "单行字段字节数合计")
    private Integer rowBytes;
}
