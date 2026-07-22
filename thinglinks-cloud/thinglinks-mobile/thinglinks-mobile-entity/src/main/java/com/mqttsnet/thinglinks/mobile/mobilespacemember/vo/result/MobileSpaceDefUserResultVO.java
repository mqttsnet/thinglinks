package com.mqttsnet.thinglinks.mobile.mobilespacemember.vo.result;


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
import java.time.LocalDateTime;


/**
 * <p>
 * 实体类
 * 空间用户信息
 * </p>
 *
 * @author mqttsnet
 * @since 2024-09-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "MobileSpaceDefUserResultVO", description = "空间用户信息")
public class MobileSpaceDefUserResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 用户名;大小写数字下划线
     */
    @Schema(description = "用户名")

    private String username;

    @Schema(description = "头像id")
    private Long avatarId;

    /**
     * 昵称
     */
    @Schema(description = "昵称")

    private String nickName;
    /**
     * 邮箱
     */
    @Schema(description = "邮箱")

    private String email;
    /**
     * 手机;1开头11位纯数字
     */
    @Schema(description = "手机")

    private String mobile;
    /**
     * 身份证;15或18位
     */
    @Schema(description = "身份证")

    private String idCard;
    /**
     * 微信OpenId
     */
    @Schema(description = "微信OpenId")

    private String wxOpenId;
    /**
     * 钉钉OpenId
     */
    @Schema(description = "钉钉OpenId")

    private String ddOpenId;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
