package com.mqttsnet.thinglinks.productversion.vo.diff;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品版本差异 ── 单个字段级变更,由 ReflectionDiffBuilder 反射比对产出。
 * label 取自字段的 @Schema(description),变更类型按「空 ↔ 有值」推断。
 *
 * @author mqttsnet
 * @see ProductVersionDiffNode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionFieldDiffVO", description = "版本差异 - 字段级变更")
public class ProductVersionFieldDiffVO implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 回显结果集合 ── EchoService 把 @Echo 字段的查回值写入此处。注意 before/after 是 Object 类型,
     * 无法在编译期标 @Echo,EchoService 不会自动填充,echoMap 在本 VO 仅作形态占位;实际字典翻译由前端按 dictType 走 useDict.getDictLabel。
     */
    @Builder.Default
    @Schema(description = "回显结果集合(字典 / 用户 / 组织等翻译值)")
    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "字段名")
    private String field;

    @Schema(description = "字段中文名(取自字段 @Schema 描述,缺失时回退字段名)")
    private String label;

    @Schema(description = "变更类型(ADDED/REMOVED/MODIFIED)")
    private String changeType;

    @Schema(description = "原值")
    private Object before;

    @Schema(description = "新值")
    private Object after;

    /**
     * 字典类型标识 —— 来自源字段 @Echo 注解的 dictType,前端按此 key 走 useDict.getDictLabel 翻译值为中文展示。
     * 无字典属性的字段为 null。
     */
    @Schema(description = "字典类型(供前端按值翻译为中文,无字典属性为 null)")
    private String dictType;
}
