package com.mqttsnet.thinglinks.card.vo.result.channel;

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

/**
 * <p>
 * 表单查询方法返回值VO
 * 物联卡渠道表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-06-26 21:55:13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "物联卡渠道表")
public class CardChannelInfoResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道id")
    private Long id;

    /**
     * 渠道商名称（如:中国移动）
     */
    @Schema(description = "渠道商名称（如:中国移动）")
    private String channelName;
    /**
     * 密钥集合
     */
    @Schema(description = "密钥集合")
    private String keyParameter;
    /**
     * 是否直属官方平台(如直接对接是移动onelink  0不是  1是)
     */
    @Schema(description = "是否直属官方平台(如直接对接是移动onelink  0不是  1是)")
    private Integer officialFlag;
    /**
     * token是否需要重复刷新 true是 false否 默认是: false
     */
    @Schema(description = "token是否需要重复刷新 true是 false否 默认是: false")
    private Integer refreshFlag;
    /**
     * 所属运营商(1移动、2电信 、3联通）
     */
    @Schema(description = "所属运营商(1移动、2电信 、3联通）")
    private Integer operatorType;
    /**
     * 省份名称
     */
    @Schema(description = "省份名称")
    private String provinceName;
    /**
     * 省份编码
     */
    @Schema(description = "省份编码")
    private String provinceCode;
    /**
     * 公共应用键
     */
    @Schema(description = "公共应用键")
    private String appKey;
    /**
     * 公共密钥
     */
    @Schema(description = "公共密钥")
    private String secret;
    /**
     * 公共code
     */
    @Schema(description = "公共code")
    private String code;
    /**
     * 客户appid
     */
    @Schema(description = "客户appid")
    private String appId;
    /**
     * 密匙
     */
    @Schema(description = "密匙")
    private String password;
    /**
     * 渠道状态(0启用、1停用)
     */
    @Schema(description = "渠道状态(0启用、1停用)")
    private Integer status;
    /**
     * 渠道类别(如:山东移动)
     */
    @Schema(description = "渠道类别(如:山东移动)")
    private Integer channelType;
    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private String extendParams;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


}
