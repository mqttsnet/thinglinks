package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.RemoteProductCommandsRequestsService;
import com.mqttsnet.thinglinks.link.api.RemoteProductCommandsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @program: thinglinks
 * @description: 产品服务管理服务降级处理
 * @packagename: com.mqttsnet.thinglinks.link.api.factory
 * @author: kk
 * @date: 2022-11-19
 **/
@Component
public class RemoteProductCommandsRequestsFallbackFactory implements FallbackFactory<RemoteProductCommandsRequestsService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteProductCommandsRequestsFallbackFactory.class);

    @Override
    public RemoteProductCommandsRequestsService create(Throwable throwable) {
        log.error("产品命令服务管理命令下发调用失败:{}", throwable.getMessage());
        return new RemoteProductCommandsRequestsService() {

            @Override
            public R<?> selectAllRequestsByCommandId(Long commandId) {
                return R.fail("根据产品命令下发属性列表查询命令失败", throwable.getMessage());
            }
        };
    }

}
