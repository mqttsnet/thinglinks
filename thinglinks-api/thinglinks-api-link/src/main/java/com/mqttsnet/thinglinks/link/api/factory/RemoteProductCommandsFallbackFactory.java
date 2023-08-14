package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
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
public class RemoteProductCommandsFallbackFactory implements FallbackFactory<RemoteProductCommandsService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteProductCommandsFallbackFactory.class);

    @Override
    public RemoteProductCommandsService create(Throwable throwable) {
        log.error("产品命令服务管理服务调用失败:{}", throwable.getMessage());
        return new RemoteProductCommandsService() {

            @Override
            public R<?> selectProductCommandsByIdList(List<Long> commandIdList){
                return R.fail("根据产品命令id列表查询服务失败", throwable.getMessage());
            }

            @Override
            public R<?> selectAllByServiceId(Long serviceId) {
                return R.fail("根据产品服务id列表查询命令失败", throwable.getMessage());
            }

        };
    }

}
