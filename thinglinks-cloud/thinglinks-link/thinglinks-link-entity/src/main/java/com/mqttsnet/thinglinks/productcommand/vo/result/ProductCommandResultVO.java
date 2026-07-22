package com.mqttsnet.thinglinks.productcommand.vo.result;

import com.mqttsnet.thinglinks.productversionchangelog.vo.DiffIgnore;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import com.mqttsnet.thinglinks.productcommandrequest.vo.result.ProductCommandRequestResultVO;
import com.mqttsnet.thinglinks.productcommandresponse.vo.result.ProductCommandResponseResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.List;

/**
 * <p>
 * 表单查询方法返回值VO
 * 产品模型设备服务命令表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "ProductCommandResultVO", description = "产品模型设备服务命令表")
public class ProductCommandResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "命令id")
    private Long id;

    /**
     * 服务ID(结构外键,不参与变更记录 diff)
     */
    @Schema(description = "服务ID")
    @DiffIgnore
    private Long serviceId;
    /**
     * 指示命令的编码，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。支持英文大小写、数字及下划线，长度[2,50]。
     */
    @Schema(description = "指示命令的编码，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。支持英文大小写、数字及下划线，长度[2,50]。")
    private String commandCode;
    /**
     * 指示命令名称
     */
    @Schema(description = "指示命令名称")
    private String commandName;
    /**
     * 命令描述。
     */
    @Schema(description = "命令描述。")
    private String description;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "产品请求服务命令属性")
    private List<ProductCommandRequestResultVO> requests;

    @Schema(description = "产品响应服务命令属性")
    private List<ProductCommandResponseResultVO> responses;

}
