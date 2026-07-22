package com.mqttsnet.thinglinks.productversion.vo.snapshot;

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
 * 产品快照 ── 物模型命令节点。
 *
 * @author mqttsnet
 * @see ProductSnapshotServiceVO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductSnapshotCommandVO", description = "快照命令节点")
public class ProductSnapshotCommandVO implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典 / 用户 / 组织等回显结果集合 ── EchoService 把 @Echo 字段的查回值写入此处。
     */
    @Builder.Default
    @Schema(description = "回显结果集合(字典 / 用户 / 组织等翻译值)")
    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "命令编码")
    private String commandCode;

    @Schema(description = "命令名称")
    private String commandName;

    @Schema(description = "命令描述")
    private String description;

    @Schema(description = "请求参数列表")
    private List<ProductSnapshotCommandParameterVO> requests;

    @Schema(description = "响应参数列表")
    private List<ProductSnapshotCommandParameterVO> responses;
}
