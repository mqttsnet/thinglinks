package com.mqttsnet.thinglinks.device.vo.result;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <p>
 * 表单查询方法返回值VO
 * 设备动作数据
 * </p>
 *
 * @author mqttsnet
 * @date 2023-06-10 16:38:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "DeviceActionResultVO", description = "设备动作数据")
public class DeviceActionResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 设备标识
     */
    @Schema(description = "设备标识")
    private String deviceIdentification;
    /**
     * 动作类型
     */
    @Schema(description = "动作类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_DEVICE_ACTION_TYPE)
    private String actionType;
    /**
     * 内容信息
     */
    @Schema(description = "内容信息")
    private String message;
    /**
     * 状态:0=成功 / 1=失败
     */
    @Schema(description = "状态:0=成功 / 1=失败", example = "0", allowableValues = "0,1")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_DEVICE_ACTION_STATUS)
    private Integer status;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


}
