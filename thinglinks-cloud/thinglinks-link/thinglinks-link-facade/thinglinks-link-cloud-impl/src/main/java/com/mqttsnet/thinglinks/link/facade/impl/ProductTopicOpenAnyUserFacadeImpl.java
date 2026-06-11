package com.mqttsnet.thinglinks.link.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.link.api.anyuser.ProductTopicOpenAnyUserApi;
import com.mqttsnet.thinglinks.link.facade.ProductTopicOpenAnyUserFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品 Topic 开放接口 Facade ── cloud 实现(走 Feign)。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
@Service
public class ProductTopicOpenAnyUserFacadeImpl implements ProductTopicOpenAnyUserFacade {

    @Lazy
    @Autowired
    private ProductTopicOpenAnyUserApi productTopicOpenAnyUserApi;

    @Override
    public R<List<String>> findTopicsByIds(List<Long> ids) {
        return productTopicOpenAnyUserApi.findTopicsByIds(ids);
    }
}
