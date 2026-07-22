package com.mqttsnet.thinglinks.productversion.vo.result;

import java.io.Serial;
import java.time.LocalDateTime;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
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
 * 产品物模型版本返回 VO。
 *
 * <p>@Echo 字段在 ResponseAdvice 中被自动回填字典 label,前端无需再查。</p>
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "ProductVersionResultVO", description = "产品物模型版本")
public class ProductVersionResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "产品标识")
    private String productIdentification;

    @Schema(description = "版本序号(系统发布时生成的不可变快照标识)")
    private String versionNo;

    @Schema(description = "版本状态(字典 PRODUCT_VERSION_STATUS)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.PRODUCT_VERSION_STATUS)
    private Integer versionStatus;

    @Schema(description = "产品快照 JSON")
    private String productSnapshotJson;

    @Schema(description = "发布策略(字典 PRODUCT_PUBLISH_STRATEGY)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.PRODUCT_PUBLISH_STRATEGY)
    private Integer publishStrategy;

    @Schema(description = "灰度配置 JSON")
    private String canaryConfigJson;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "备注")
    private String remark;
}
