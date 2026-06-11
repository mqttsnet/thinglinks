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
 * 文档信息
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
@Schema(description = "文档信息")
public class SopDocInfoResultVO extends AuditableResultVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 应用id
     * doc_app.id
     */
    @Schema(description = "应用id")
    private Long docAppId;
    /**
     * 文档标题
     */
    @Schema(description = "文档标题")
    private String docTitle;
    /**
     * 文档id
     * torna.doc_info.id
     */
    @Schema(description = "文档id")
    private Long docId;
    /**
     * 文档编码
     */
    @Schema(description = "文档编码")
    private String docCode;
    /**
     * 文档类型
     * [1-dubbo 2-富文本 3-Markdown]
     */
    @Schema(description = "文档类型")
    private Integer docType;
    /**
     * 来源类型
     * [1-torna 2-自建]
     */
    @Schema(description = "来源类型")
    private Integer sourceType;
    /**
     * 文档版本号
     */
    @Schema(description = "文档版本号")
    private String docVersion;
    /**
     * 文档名称
     */
    @Schema(description = "文档名称")
    private String docName;
    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;
    /**
     * 是否分类
     */
    @Schema(description = "是否分类")
    private Integer isFolder;
    /**
     * 状态
     * [0-未发布 1-已发布]
     */
    @Schema(description = "状态")
    private Integer isPublish;
    /**
     * 父文档节点id
     */
    @Schema(description = "父文档节点id")
    private Long parentId;


}
