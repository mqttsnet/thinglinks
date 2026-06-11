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

/**
 * <p>
 * 表单查询方法返回值VO
 * 空间人员绑定表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-08-30 15:40:08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "空间人员绑定表")
public class MobileSpaceMemberResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 空间ID
     */
    @Schema(description = "空间ID")
    private Long spaceId;
    /**
     * 人员ID
     */
    @Schema(description = "人员ID")
    private Long memberId;
    /**
     * 人员类型( 0:成员 1:管理员  2:所有者)
     */
    @Schema(description = "人员类型( 0:成员 1:管理员  2:所有者)")
    private Integer memberType;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


    /**
     * 空间用户信息
     */
    @Schema(description = "空间用户信息")
    private MobileSpaceDefUserResultVO defUserResultVO;


}
