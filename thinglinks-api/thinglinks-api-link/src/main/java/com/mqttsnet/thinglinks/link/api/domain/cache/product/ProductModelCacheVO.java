package com.mqttsnet.thinglinks.link.api.domain.cache.product;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.thinglinks.link.api.domain.product.model.ProductModel;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * 产品模型缓存VO
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 */

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@ApiModel(value = "ProductModelCacheVO", description = "产品模型缓存VO")
public class ProductModelCacheVO extends ProductModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();
}
