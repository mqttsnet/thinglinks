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
 * 组权限表
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
@Schema(description = "组权限表")
public class SopPermGroupPermissionResultVO extends AuditableResultVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 分组id
     * perm_group.id
     */
    @Schema(description = "分组id")
    private Long groupId;
    /**
     * 文档id
     * api_info.id
     */
    @Schema(description = "文档id")
    private Long apiId;


}
