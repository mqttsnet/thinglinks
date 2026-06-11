package com.mqttsnet.thinglinks.cacert.entity.audit;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;

/**
 * CA 证书审计日志 ── 记录证书全生命周期操作(导入/颁发/吊销/下载/SSL 测试)。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName(value = "ca_cert_audit_log")
public class CaCertAuditLog extends Entity<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 关联 CA 证书 ID */
    @TableField(value = "ca_id", condition = EQUAL)
    private Long caId;

    /** CA 证书序列号 */
    @TableField(value = "ca_serial_number", condition = EQUAL)
    private String caSerialNumber;

    /** 动作类型,见 {@code CaCertAuditTypeEnum} */
    @TableField(value = "type", condition = EQUAL)
    private String type;

    /** 详情(JSON 或自由文本) */
    @TableField(value = "detail")
    private String detail;
}
