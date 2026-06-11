package com.mqttsnet.thinglinks.productpublishrecord.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.thinglinks.productpublishrecord.vo.ddl.PublishDdlItemVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

/**
 * 产品发布记录实体,记录发布 / 回滚 / 历史清理操作的执行轨迹。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.productpublishrecord.enumeration.ProductPublishRecordIntentEnum
 * @see com.mqttsnet.thinglinks.productpublishrecord.enumeration.ProductPublishRecordStatusEnum
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName(value = "product_publish_record", autoResultMap = true)
public class ProductPublishRecord extends Entity<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 产品标识。 */
    @TableField(value = "product_identification", condition = EQUAL)
    private String productIdentification;

    /** 源版本号(发布时为上一版,历史清理时同 target,首次发布为 null)。 */
    @TableField(value = "source_version", condition = EQUAL)
    private String sourceVersion;

    /** 目标版本号(发布:新版本;回滚:回滚目标;历史清理:被清理版本)。 */
    @TableField(value = "target_version", condition = EQUAL)
    private String targetVersion;

    /** 操作意图(0-发布,1-回滚,2-历史清理)。 */
    @TableField(value = "intent", condition = EQUAL)
    private Integer intent;

    /** 执行状态(0-执行中,1-成功,2-失败)。 */
    @TableField(value = "status", condition = EQUAL)
    private Integer status;

    /**
     * DDL 执行明细列表 ── 每条对应一个 service 的 CREATE_STABLE / DROP_STABLE 执行结果。
     * DB 列仍是 ddl_summary TEXT(JSON 字符串),由 {@link JacksonTypeHandler} 自动序列化为 List。
     * 表必须 {@code autoResultMap = true}(已开启)typeHandler 才生效。
     */
    @TableField(value = "ddl_summary", typeHandler = JacksonTypeHandler.class)
    private List<PublishDdlItemVO> ddlItems;

    /** 失败原因(成功时为 null)。 */
    @TableField(value = "failed_reason", condition = LIKE)
    private String failedReason;

    /** 开始时间。 */
    @TableField(value = "started_time", condition = EQUAL)
    private LocalDateTime startedTime;

    /** 结束时间。 */
    @TableField(value = "finished_time", condition = EQUAL)
    private LocalDateTime finishedTime;

    /** 备注。 */
    @TableField(value = "remark", condition = LIKE)
    private String remark;

    /** 创建人组织。 */
    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /** 逻辑删除标识(0-未删除、1-已删除)。 */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
