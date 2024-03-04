package com.mqttsnet.thinglinks.link.api.domain.cache.device;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * 设备档案缓存VO
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
@EqualsAndHashCode(callSuper = false)
@Builder
@ApiModel(value = "DeviceCacheVO", description = "设备档案缓存VO")
public class DeviceCacheVO extends Device implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();
    /**
     * 设备产品基础信息
     */
    @ApiModelProperty(value = "设备产品基础信息")
    private ProductCacheVO productCacheVO;

}
