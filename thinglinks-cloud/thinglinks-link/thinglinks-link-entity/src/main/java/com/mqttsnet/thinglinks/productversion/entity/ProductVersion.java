package com.mqttsnet.thinglinks.productversion.entity;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;
import java.time.LocalDateTime;

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
 * 产品物模型版本快照实体,每次发布产生一行,product_snapshot_json 冻结当时整棵产品树,不可变。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.productversion.enumeration.ProductVersionStatusEnum
 * @see com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName(value = "product_version", autoResultMap = true)
public class ProductVersion extends Entity<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品标识。
     */
    @TableField(value = "product_identification", condition = LIKE)
    private String productIdentification;

    /** 版本序号(系统发布时生成的不可变快照标识,16 位短雪花字符串)。 */
    @TableField(value = "version_no", condition = EQUAL)
    private String versionNo;

    /**
     * 版本状态(0-草稿,1-已发布,2-灰度中,3-影子,4-已回滚,5-已归档)。
     */
    @TableField(value = "version_status", condition = EQUAL)
    private Integer versionStatus;

    /**
     * 产品快照 JSON,对应 {@link com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotVO}。
     */
    @TableField(value = "product_snapshot_json")
    private String productSnapshotJson;

    /**
     * 发布策略(0-全量,1-灰度,2-影子)。
     */
    @TableField(value = "publish_strategy", condition = EQUAL)
    private Integer publishStrategy;

    /**
     * 灰度配置 JSON。
     */
    @TableField(value = "canary_config_json")
    private String canaryConfigJson;

    /**
     * 发布时间。
     */
    @TableField(value = "publish_time", condition = EQUAL)
    private LocalDateTime publishTime;

    /**
     * 备注。
     */
    @TableField(value = "remark", condition = LIKE)
    private String remark;

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
