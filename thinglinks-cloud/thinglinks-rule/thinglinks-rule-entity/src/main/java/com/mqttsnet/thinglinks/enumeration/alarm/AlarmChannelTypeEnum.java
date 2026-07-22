package com.mqttsnet.thinglinks.enumeration.alarm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 告警渠道类型
 * </p>
 *
 * @author shihuan sun
 * @date 2024-05-31
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "AlarmChannelTypeEnum", description = "告警渠道类型枚举")
public enum AlarmChannelTypeEnum {

    DING_TALK(0, "钉钉", "DINGTALK_ALARM"),

    ENTERPRISE_WECHAT(1, "企业微信", "WECHAT_ALARM"),

    FS(2, "飞书", "FS_ALARM"),

    SITE_MESSAGE(3, "站内信", "SITE_ALARM");

    private Integer value;
    private String desc;

    /**
     * 渠道模版编码
     * 通知需要的模版编码
     */
    private String channelTemplateCode;

    public static Optional<AlarmChannelTypeEnum> fromValue(Integer value) {
        return Optional.ofNullable(value)
                .flatMap(val -> Stream.of(AlarmChannelTypeEnum.values())
                        .filter(e -> e.getValue().equals(val))
                        .findFirst());
    }


    public void setValue(Integer value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
