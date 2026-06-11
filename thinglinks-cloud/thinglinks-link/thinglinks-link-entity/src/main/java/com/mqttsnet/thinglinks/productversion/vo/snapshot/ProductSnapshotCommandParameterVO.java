package com.mqttsnet.thinglinks.productversion.vo.snapshot;

import java.io.Serial;
import java.io.Serializable;
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
 * 产品快照 ── 命令参数节点(request / response 共用)。
 *
 * @author mqttsnet
 * @see ProductSnapshotCommandVO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductSnapshotCommandParameterVO", description = "快照命令参数节点")
public class ProductSnapshotCommandParameterVO implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典 / 用户 / 组织等回显结果集合 ── EchoService 把 @Echo 字段的查回值写入此处。
     */
    @Builder.Default
    @Schema(description = "回显结果集合(字典 / 用户 / 组织等翻译值)")
    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "参数编码")
    private String parameterCode;

    @Schema(description = "参数名称")
    private String parameterName;

    @Schema(description = "数据类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_SERVICE_COMMAND_DATA_TYPE)
    private String datatype;

    @Schema(description = "枚举可选值")
    private String enumlist;

    @Schema(description = "最小值")
    private String min;

    @Schema(description = "最大值")
    private String max;

    @Schema(description = "步长")
    private String step;

    @Schema(description = "最大字符长度")
    private String maxlength;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "是否必填")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_SERVICE_REQUIRED)
    private Integer required;

    @Schema(description = "参数描述")
    private String description;
}
