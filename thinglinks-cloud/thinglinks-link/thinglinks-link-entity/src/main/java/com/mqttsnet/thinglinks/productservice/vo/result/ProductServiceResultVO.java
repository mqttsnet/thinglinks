package com.mqttsnet.thinglinks.productservice.vo.result;

import cn.hutool.core.collection.ListUtil;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.productversionchangelog.vo.DiffIgnore;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import com.mqttsnet.thinglinks.productcommand.vo.result.ProductCommandResultVO;
import com.mqttsnet.thinglinks.productproperty.vo.result.ProductPropertyResultVO;
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
import java.util.Map;

/**
 * <p>
 * 表单查询方法返回值VO
 * 产品模型服务表
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
@Schema(title = "ProductServiceResultVO", description = "产品模型服务表")
public class ProductServiceResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @DiffIgnore
    private List<Map<String, Object>> echoList = ListUtil.toList();


    @Schema(description = "服务id")
    private Long id;

    /**
     * 产品ID(结构外键,不参与变更记录 diff)
     */
    @Schema(description = "产品ID")
    @DiffIgnore
    private Long productId;
    /**
     * 服务编码:支持英文大小写、数字、下划线和中划线
     */
    @Schema(description = "服务编码:支持英文大小写、数字、下划线和中划线")
    private String serviceCode;
    /**
     * 服务名称
     */
    @Schema(description = "服务名称")
    private String serviceName;
    /**
     * 服务类型
     */
    @Schema(description = "服务类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_SERVICE_TYPE)
    private String serviceType;
    /**
     * 状态(字典值：0启用  1停用)
     */
    @Schema(description = "状态(字典值：0启用  1停用)")
    private Integer serviceStatus;
    /**
     * 服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。
     */
    @Schema(description = "服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。")
    private String description;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "产品服务命令")
    private List<ProductCommandResultVO> commands;

    @Schema(description = "产品服务属性")
    private List<ProductPropertyResultVO> properties;

}
