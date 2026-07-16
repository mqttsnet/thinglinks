package com.mqttsnet.thinglinks.link.api.inner.hystrix;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.link.api.inner.ProductTopicOpenInnerApi;

import java.util.List;

/**
 * {@link ProductTopicOpenInnerApi} 降级处理 ── 超时返回 timeout R。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
public class ProductTopicOpenInnerApiFallback implements ProductTopicOpenInnerApi {

    @Override
    public R<List<String>> findTopicsByIds(List<Long> ids) {
        return R.timeout();
    }
}
