package com.mqttsnet.thinglinks.link.api.domain.cache.product;

import com.mqttsnet.thinglinks.link.api.domain.product.model.ProductModel;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@ApiModel(value = "ProductModelCacheVO", description = "产品模型缓存VO")
public class ProductModelCacheVO extends ProductModel implements Serializable {

}
