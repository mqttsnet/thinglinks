package com.mqttsnet.thinglinks.productversion.vo.snapshot;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品快照 ── 服务节点。
 *
 * @author mqttsnet
 * @see ProductSnapshotVO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductSnapshotServiceVO", description = "快照服务节点")
public class ProductSnapshotServiceVO implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典 / 用户 / 组织等回显结果集合 ── EchoService 把 @Echo 字段的查回值写入此处。
     */
    @Builder.Default
    @Schema(description = "回显结果集合(字典 / 用户 / 组织等翻译值)")
    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "服务编码")
    private String serviceCode;

    @Schema(description = "服务名称")
    private String serviceName;

    @Schema(description = "服务类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_SERVICE_TYPE)
    private String serviceType;

    @Schema(description = "服务状态")
    private Integer serviceStatus;

    @Schema(description = "服务描述")
    private String description;

    @Schema(description = "属性列表")
    private List<ProductSnapshotPropertyVO> properties;

    @Schema(description = "命令列表")
    private List<ProductSnapshotCommandVO> commands;
}
