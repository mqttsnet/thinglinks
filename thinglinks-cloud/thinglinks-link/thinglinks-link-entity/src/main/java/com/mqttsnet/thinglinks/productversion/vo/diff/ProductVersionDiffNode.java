package com.mqttsnet.thinglinks.productversion.vo.diff;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品版本差异 ── 层级节点(递归),与物模型层级 1:1:产品 → 服务 → 属性 / 命令 → 命令参数。
 * 每个节点携带本级字段级变更 fields 与子节点 children,前端按树平铺缩进渲染。
 *
 * @author mqttsnet
 * @see ProductVersionDiffVO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionDiffNode", description = "版本差异 - 层级节点")
public class ProductVersionDiffNode implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 回显结果集合 ── EchoService 把 @Echo 字段的查回值写入此处。 */
    @Builder.Default
    @Schema(description = "回显结果集合(字典 / 用户 / 组织等翻译值)")
    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "层级(PRODUCT/SERVICE/PROPERTY/COMMAND/COMMAND_PARAM)")
    private String level;

    @Schema(description = "命令参数方向(REQUEST/RESPONSE),仅 COMMAND_PARAM 有值")
    private String paramKind;

    @Schema(description = "节点编码(serviceCode/propertyCode/commandCode/parameterCode;产品节点为空)")
    private String code;

    @Schema(description = "节点显示名")
    private String name;

    @Schema(description = "变更类型(ADDED/REMOVED/MODIFIED)")
    private String changeType;

    @Schema(description = "本级字段级变更列表")
    private List<ProductVersionFieldDiffVO> fields;

    @Schema(description = "子节点(服务→属性/命令,命令→请求/响应参数)")
    private List<ProductVersionDiffNode> children;
}
