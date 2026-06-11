package com.mqttsnet.thinglinks.productversionchangelog.entity;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 产品物模型版本变更日志实体 ── append-only 审计流水,产品树在未发布前每次有效变更就追加一行(草稿编辑流水)。
 * 区别于版本对比({@code ProductVersionService.diff} 的两版本全量差异);发布动作不写本表(走 product_publish_record)。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductVersionChangeTypeEnum
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName(value = "product_version_change_log", autoResultMap = true)
public class ProductVersionChangeLog extends Entity<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品标识。
     */
    @TableField(value = "product_identification", condition = LIKE)
    private String productIdentification;

    /**
     * 版本序号:本批变更归属版本(对应 {@code product_version.version_no})。
     */
    @TableField(value = "version_no", condition = EQUAL)
    private String versionNo;

    /**
     * 变更类型(0-新增,1-编辑,2-删除)。
     */
    @TableField(value = "change_type", condition = EQUAL)
    private Integer changeType;

    /**
     * 变更维度(0-产品信息 1-服务 2-属性 3-命令)。
     */
    @TableField(value = "target_type", condition = EQUAL)
    private Integer targetType;

    /**
     * 变更摘要(人类可读,如"新增 1 个服务、修改 2 个属性")。
     */
    @TableField(value = "change_summary", condition = LIKE)
    private String changeSummary;

    /**
     * 字段级变更明细 JSON(对应 {@code ProductVersionDiffVO} 序列化结果,覆盖产品所有字段)。
     */
    @TableField(value = "change_detail_json")
    private String changeDetailJson;

    /**
     * 创建人组织。
     */
    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识(0-未删除、1-已删除)。
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
