package com.mqttsnet.thinglinks.productversionchangelog.vo.save;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品物模型版本变更日志保存 VO(SuperController 范型占位)。
 *
 * <p>变更日志为 append-only 资产变更审计流水,写入由 ProductVersionService.upsertDraft
 * 内部触发。本 VO 仅满足 {@link com.mqttsnet.basic.base.controller.SuperController} 范型签名,
 * 默认 save 接口不在业务上使用。</p>
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionChangeLogSaveVO", description = "变更日志保存(占位)")
public class ProductVersionChangeLogSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "产品标识")
    private String productIdentification;
}
