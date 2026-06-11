package com.mqttsnet.thinglinks.link.api.anyuser.hystrix;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.link.api.anyuser.ProductTopicOpenAnyUserApi;

import java.util.List;

/**
 * {@link ProductTopicOpenAnyUserApi} 降级处理 ── 超时返回 timeout R。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
public class ProductTopicOpenAnyUserApiFallback implements ProductTopicOpenAnyUserApi {

    @Override
    public R<List<String>> findTopicsByIds(List<Long> ids) {
        return R.timeout();
    }
}
