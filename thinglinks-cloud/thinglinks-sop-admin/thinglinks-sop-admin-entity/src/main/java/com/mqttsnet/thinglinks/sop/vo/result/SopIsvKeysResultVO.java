package com.mqttsnet.thinglinks.sop.vo.result;

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
 * ISV秘钥管理
 * </p>
 *
 * @author zuihou
 * @since 2025-05-11 10:34:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "ISV秘钥管理")
public class SopIsvKeysResultVO extends AuditableResultVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    private String appId;

    /**
     * ISV
     * isv_info.id
     */
    @Schema(description = "ISV")
    private Long isvId;
    /**
     * 秘钥格式
     * [1-PKCS8(JAVA适用) 2-PKCS1(非JAVA适用)]
     */
    @Schema(description = "秘钥格式")
    private Integer keyFormat;
    /**
     * 开发者生成的公钥
     */
    @Schema(description = "开发者生成的公钥")
    private String publicKeyIsv;
    /**
     * 开发者生成的私钥
     * （提供给开发者）
     */
    @Schema(description = "开发者生成的私钥")
    private String privateKeyIsv;
    /**
     * 平台生成的公钥
     * （提供给开发者）
     */
    @Schema(description = "平台生成的公钥")
    private String publicKeyPlatform;
    /**
     * 平台生成的私钥
     */
    @Schema(description = "平台生成的私钥")
    private String privateKeyPlatform;


}
