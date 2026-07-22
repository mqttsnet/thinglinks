package com.mqttsnet.thinglinks.template.vo.result;

import java.io.Serial;

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
 * 表单查询方法返回值VO
 * 可视化项目模板表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-05-17 13:45:51
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "ProjectTemplateDetailsResultVO", description = "可视化项目模板表")
public class ProjectTemplateDetailsResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 模版标识
     */
    @Schema(description = "模版标识")
    private String templateIdentification;
    /**
     * 模版标识
     */
    @Schema(description = "模版标识")
    private String templateName;
    /**
     * 项目状态[1-发布,-1-未发布]
     */
    @Schema(description = "项目状态[1-发布,-1-未发布]")
    private Integer status;
    /**
     * 首页图片
     */
    @Schema(description = "首页图片")
    private String indexImageId;
    /**
     * 存储数据
     */
    @Schema(description = "存储数据")
    private String content;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


}
