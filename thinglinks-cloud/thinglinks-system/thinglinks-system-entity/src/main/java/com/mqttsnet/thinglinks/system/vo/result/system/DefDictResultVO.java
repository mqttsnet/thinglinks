package com.mqttsnet.thinglinks.system.vo.result.system;


import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
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
 * 实体类
 * 字典
 * </p>
 *
 * @author mqttsnet
 * @since 2021-10-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "DefDictResultVO", description = "字典")
public class DefDictResultVO extends AuditableResultVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 分类;[10-系统字典 20-业务字典]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.DICT_CLASSIFY)
     */
    @Schema(description = "分类")

    private String classify;

    /**
     * 字典分组
     */
    @Schema(description = "字典分组")
    private String dictGroup;

    /**
     * 数据类型
     * [1-字符串 2-整型 3-布尔]
     */
    @Schema(description = "数据类型")
    private String dataType;
    /**
     * 标识
     */
    @Schema(description = "标识")

    private String key;
    /**
     * 名称
     */
    @Schema(description = "名称")

    private String name;
    /**
     * 状态
     */
    @Schema(description = "状态")

    private Boolean state;
    /**
     * 备注
     */
    @Schema(description = "备注")

    private String remark;


    /**
     * 查询枚举字典时使用
     */
    @Schema(description = "枚举是否存在")
    private Boolean exist;

    private List<DefDictItemResultVO> itemList;
}
