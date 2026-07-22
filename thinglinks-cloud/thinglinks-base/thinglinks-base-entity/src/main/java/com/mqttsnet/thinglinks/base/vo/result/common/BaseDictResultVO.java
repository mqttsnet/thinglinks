package com.mqttsnet.thinglinks.base.vo.result.common;


import java.io.Serial;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <p>
 * 实体类
 * 字典
 * </p>
 *
 * @author mqttsnet
 * @since 2021-10-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "BaseDictResultVO", description = "字典")
public class BaseDictResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 分类;[10-系统字典 20-业务字典]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.DICT_CLASSIFY)
     */
    @Schema(description = "分类")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.DICT_CLASSIFY)

    private String classify;
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

}
