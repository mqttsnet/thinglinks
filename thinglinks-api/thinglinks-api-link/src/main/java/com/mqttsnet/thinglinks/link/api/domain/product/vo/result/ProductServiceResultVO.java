package com.mqttsnet.thinglinks.link.api.domain.product.vo.result;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
@EqualsAndHashCode(callSuper = false)
@Builder
@ApiModel(value = "ProductServiceResultVO", description = "产品模型服务表")
public class ProductServiceResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    private List<Map<String, Object>> echoList = ListUtil.toList();


    @ApiModelProperty(value = "服务id")
    private Long id;

    /**
     * 产品ID
     */
    @ApiModelProperty(value = "产品ID")
    private Long productId;
    /**
     * 服务编码:支持英文大小写、数字、下划线和中划线
     */
    @ApiModelProperty(value = "服务编码:支持英文大小写、数字、下划线和中划线")
    private String serviceCode;
    /**
     * 服务名称
     */
    @ApiModelProperty(value = "服务名称")
    private String serviceName;
    /**
     * 服务类型
     */
    @ApiModelProperty(value = "服务类型")
    private String serviceType;
    /**
     * 状态(字典值：0启用  1停用)
     */
    @ApiModelProperty(value = "状态(字典值：0启用  1停用)")
    private Integer serviceStatus;
    /**
     * 服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。
     */
    @ApiModelProperty(value = "服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。")
    private String description;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
    /**
     * 创建人组织
     */
    @ApiModelProperty(value = "创建人组织")
    private Long createdOrgId;

    @ApiModelProperty(value = "产品服务命令")
    private List<ProductCommandResultVO> commands;

    @ApiModelProperty(value = "产品服务属性")
    private List<ProductPropertyResultVO> properties;

}
