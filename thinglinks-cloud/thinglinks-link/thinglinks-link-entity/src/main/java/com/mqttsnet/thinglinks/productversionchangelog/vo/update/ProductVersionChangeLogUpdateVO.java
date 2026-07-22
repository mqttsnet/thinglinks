package com.mqttsnet.thinglinks.productversionchangelog.vo.update;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品物模型版本变更日志更新 VO(SuperController 范型占位)。
 *
 * <p>变更日志为 append-only 审计流水,记录不可变更。本 VO 仅满足
 * {@link com.mqttsnet.basic.base.controller.SuperController} 范型签名,默认 update 接口不在业务上使用。</p>
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionChangeLogUpdateVO", description = "变更日志更新(占位)")
public class ProductVersionChangeLogUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;
}
