package com.mqttsnet.thinglinks.card.vo.result.sim;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>
 * 表单查询方法返回值VO
 * 物联卡设备关系表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-06-27 00:10:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "物联卡设备关系表")
public class CardSimDeviceResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 物联卡ID
     */
    @Schema(description = "物联卡ID")
    private Long cardId;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 设备标识
     */
    @Schema(description = "设备标识")
    private String deviceIdentification;


}
