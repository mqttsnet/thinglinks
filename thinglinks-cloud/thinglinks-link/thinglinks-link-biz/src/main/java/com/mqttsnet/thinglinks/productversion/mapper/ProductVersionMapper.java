package com.mqttsnet.thinglinks.productversion.mapper;

import com.mqttsnet.basic.base.mapper.SuperMapper;
import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;
import org.springframework.stereotype.Repository;

/**
 * 产品物模型版本快照 Mapper。
 *
 * @author mqttsnet
 * @see ProductVersion
 */
@Repository
public interface ProductVersionMapper extends SuperMapper<ProductVersion> {
}
