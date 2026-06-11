package com.mqttsnet.thinglinks.vo.query.script;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.mqttsnet.basic.utils.StrPool;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.IntStream;

/**
 * GroovyScript 查询实体
 *
 * @author mqttsnet 2025/03/18 12:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "GroovyScriptQuery", description = "规则脚本参数")
public class RuleGroovyScriptQuery {
    private static final Splitter KEY_SPLITTER = Splitter.on(StrPool.COLON).trimResults();
    /**
     * 脚本类型 + 渠道编码 + 产品标识 + 主题模式
     */
    private static final int REQUIRED_PARTS = 4;

    /**
     * 脚本唯一标识
     * 唯一键定义(脚本类型:渠道编码:产品标识:主题模式)
     * 中间通过:分割 StrPool.COLON
     */
    @Schema(description = "脚本唯一标识")
    private String uniqueKey;


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


    public RuleGroovyScriptQuery(String uniqueKey) {
        validateUniqueKey(uniqueKey);
        List<String> parts = KEY_SPLITTER.splitToList(uniqueKey);
        this.scriptType = parts.get(0);
        this.channelCode = parts.get(1);
        this.productIdentification = parts.get(2);
        this.topicPattern = parts.get(3);
        this.uniqueKey = uniqueKey;
    }


    private void validateUniqueKey(String uniqueKey) {
        // 基础非空检查
        Preconditions.checkArgument(StringUtils.isNotBlank(uniqueKey), "脚本唯一键不能为空");

        List<String> parts = KEY_SPLITTER.splitToList(uniqueKey);

        // 字段数量验证
        Preconditions.checkArgument(parts.size() == REQUIRED_PARTS, "脚本唯一键格式错误，应有4个冒号分隔字段，实际收到%d个字段。输入值: %s", parts.size(), uniqueKey);

        // 逐个字段非空验证
        IntStream.range(0, REQUIRED_PARTS).forEach(i -> {
            String part = parts.get(i);
            Preconditions.checkArgument(StringUtils.isNotBlank(part), "第%d个字段不能为空或空白。字段位置说明：0=脚本类型 1=渠道编码 2=产品标识 3=主题模式。问题字段索引: %d", i + 1, i);
        });
    }


}
