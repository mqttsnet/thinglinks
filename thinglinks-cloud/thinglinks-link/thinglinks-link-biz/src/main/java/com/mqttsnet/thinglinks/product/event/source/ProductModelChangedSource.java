package com.mqttsnet.thinglinks.product.event.source;

import java.io.Serial;
import java.io.Serializable;

import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductChangeTargetTypeEnum;
import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductVersionChangeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品物模型变更事件源 —— 携带一次 CRUD 持久化动作的完整变更语义。
 *
 * <p>产品基础信息 / 服务 / 属性 / 命令(及命令请求 / 响应参数)的每次 save / update / delete
 * 各发一条。before / after 由发事件处直接传入(读事件本身,不依赖快照反推)。</p>
 *
 * @author mqttsnet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductModelChangedSource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品标识(单个)。
     */
    private String productIdentification;

    /**
     * 变更类型:新增 / 编辑 / 删除。
     */
    private ProductVersionChangeTypeEnum changeType;

    /**
     * 变更维度:产品信息 / 服务 / 属性 / 命令。
     */
    private ProductChangeTargetTypeEnum targetType;

    /**
     * 原对象:操作前查询的对象(ResultVO);新增时为 null。
     */
    private transient Object before;

    /**
     * 目标对象:操作后对象(ResultVO);删除时为 null。
     */
    private transient Object after;

    /**
     * 变更摘要:发事件处拼装透传,如「编辑 服务『默认属性控制』」。
     */
    private String changeSummary;
}
