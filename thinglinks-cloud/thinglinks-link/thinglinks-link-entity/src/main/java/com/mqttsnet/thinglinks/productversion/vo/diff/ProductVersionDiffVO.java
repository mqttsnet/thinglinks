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
 * 产品版本完整差异。
 *
 * <p>nodes 为层级变更树(产品 / 服务 / 属性 / 命令 / 命令参数),结构与物模型一致;
 * summary 为遍历该树得到的轻量计数。</p>
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionDiffVO", description = "版本完整差异")
public class ProductVersionDiffVO implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典 / 用户 / 组织等回显结果集合 ── EchoService 把 @Echo 字段的查回值写入此处。
     */
    @Builder.Default
    @Schema(description = "回显结果集合(字典 / 用户 / 组织等翻译值)")
    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "源版本号(为 null 时表示与空快照对比,即首次发布场景)")
    private String sourceVersion;

    @Schema(description = "目标版本号")
    private String targetVersion;

    @Schema(description = "差异摘要")
    private ProductVersionDiffSummaryVO summary;

    @Schema(description = "层级变更树(顶层 = 产品节点 + 各服务节点)")
    private List<ProductVersionDiffNode> nodes;

    @Schema(description = "人类可读变更摘要(无实质变更时为 null)")
    private String changeSummaryText;

    @Schema(description = "主变更类型(0-新增、1-编辑、2-删除),由字段级 diff 反推")
    private Integer changeType;
}
