
package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.RemoteCacheOpenAnyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 缓存开放服务降级处理
 *
 * @author xiaonannet
 */
@Component
public class RemoteCacheOpenAnyFallbackFactory implements FallbackFactory<RemoteCacheOpenAnyService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteCacheOpenAnyFallbackFactory.class);

    @Override
    public RemoteCacheOpenAnyService create(Throwable throwable) {
        log.error("缓存开放服务调用失败:{}", throwable.getMessage());
        return new RemoteCacheOpenAnyService() {

            @Override
            public R refreshAllDeviceCaches() {
                return R.fail("刷新设备缓存失败" + throwable.getMessage());
            }

            @Override
            public R refreshAllDeviceInfoCaches() {
                return R.fail("刷新子设备信息缓存失败" + throwable.getMessage());
            }

            @Override
            public R refreshAllProductCaches() {
                return R.fail("刷新产品缓存失败" + throwable.getMessage());
            }

            @Override
            public R refreshAllProductModelCaches() {
                return R.fail("刷新产品模型缓存失败" + throwable.getMessage());
            }
        };
    }
}
