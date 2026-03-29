package com.mqttsnet.thinglinks.product.event.source;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Description:
 * 产品物模型更新事件源
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/3/27
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductModelUpdatedEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品标识集合
     */
    private List<String> productIdentificationList;

}
