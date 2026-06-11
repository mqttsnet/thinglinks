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
 * 产品物模型版本完整快照,产品基础信息 + 整棵服务树。
 *
 * <p>快照按 product_version.version_no 维度切分(每个发布产生一份不可变快照),
 * 反序列化时通过 versionNo 区分,无需独立的 schemaVersion 字段。</p>
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.productversion.entity.ProductVersion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductSnapshotVO", description = "产品物模型版本完整快照")
public class ProductSnapshotVO implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典 / 用户 / 组织等回显结果集合 ── EchoService 把 @Echo 字段的查回值写入此处。
     */
    @Builder.Default
    @Schema(description = "回显结果集合(字典 / 用户 / 组织等翻译值)")
    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "版本序号(本快照的雪花标识,与 product_version.version_no 一致)")
    private String versionNo;

    @Schema(description = "发布时间(epoch ms)")
    private Long publishTime;

    @Schema(description = "发布策略(0-全量,1-灰度,2-影子)")
    private Integer publishStrategy;

    @Schema(description = "应用 ID")
    private String appId;

    @Schema(description = "产品标识")
    private String productIdentification;

    @Schema(description = "模板 ID")
    private Long templateId;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "产品类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_TYPE)
    private Integer productType;

    @Schema(description = "厂商 ID")
    private String manufacturerId;

    @Schema(description = "厂商名称")
    private String manufacturerName;

    @Schema(description = "产品型号")
    private String model;

    @Schema(description = "数据格式")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_DATA_FORMAT)
    private String dataFormat;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "协议类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_PROTOCOL_TYPE)
    private String protocolType;

    @Schema(description = "版本序号(快照内冗余,与本快照的 version 字段相同,便于 JSON 自包含)")
    private String activeVersionNo;

    @Schema(description = "产品描述")
    private String remark;

    @Schema(description = "服务列表")
    private List<ProductSnapshotServiceVO> services;
}
