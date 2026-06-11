package com.mqttsnet.thinglinks.vo.result.script;

import com.google.common.base.Joiner;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.common.cache.rule.groovy.GroovyScriptCacheKeyBuilder;
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
 * 规则脚本表
 * </p>
 *
 * @author mqttsnet
 * @date 2025-03-24 09:54:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "RuleGroovyScriptResultVO", description = "规则脚本表")
public class RuleGroovyScriptResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 脚本名称
     */
    @Schema(description = "脚本名称")
    private String name;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String appId;
    /**
     * 脚本类型
     */
    @Schema(description = "脚本类型")
    private String scriptType;
    /**
     * 渠道编码
     */
    @Schema(description = "渠道编码")
    private String channelCode;
    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    private String productIdentification;
    /**
     * 主题模式
     */
    @Schema(description = "主题模式")
    private String topicPattern;
    /**
     * 是否启用 [0-禁用 1-启用]
     */
    @Schema(description = "是否启用")
    private Boolean enable;
    /**
     * 脚本内容
     */
    @Schema(description = "脚本内容")
    private String scriptContent;
    /**
     * 扩展信息
     */
    @Schema(description = "扩展信息")
    private String extendParams;
    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private String objectVersion;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


    /**
     * 获取 CacheHashKey
     *
     * @return {@link CacheKey} 缓存Key
     */
    public CacheKey getCacheKey() {
        return GroovyScriptCacheKeyBuilder.builder(buildOnlyKey());
    }


    /**
     * 构建唯一Key
     * 这4个字段值构成了唯一 key   可以唯一确定一个groovy脚本
     *
     * @return 唯一 Key
     */
    public String buildOnlyKey() {
        return Joiner.on(StrPool.COLON).join(scriptType, channelCode, productIdentification, topicPattern);
    }

}
