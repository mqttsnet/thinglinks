package com.mqttsnet.thinglinks.tdengine.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @ClassDescription:
 * @ClassName: RemoteTdEngineFallbackFactory
 * @Author: thinglinks
 * @Date: 2021-12-31 11:00:59
 * @Version 1.0
 */
@Component
public class RemoteTdEngineFallbackFactory implements FallbackFactory<RemoteTdEngineService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteTdEngineFallbackFactory.class);

    @Override
    public RemoteTdEngineService create(Throwable throwable) {
        log.error("时序性数据库服务调用失败:{}", throwable.getMessage());
        return new RemoteTdEngineService()
        {

            @Override
            public R<?> createDataBase(String databaseName) {
                return R.fail("创建数据库失败:{}", throwable.getMessage());
            }

            @Override
            public R<?> createSuperTable(SuperTableDto superTableDto) {
                return R.fail("创建超级表失败:{}", throwable.getMessage());
            }

            @Override
            public R<?> createTable(TableDto tableDto) {
                return R.fail("创建表失败:{}", throwable.getMessage());
            }

            @Override
            public R<?> insertData(TableDto tableDto) {
                return R.fail("插入数据失败:{}", throwable.getMessage());
            }

            @Override
            public R<?> addColumnInStb(SuperTableDto superTableDto) {
                return R.fail("超级表增加列失败:{}", throwable.getMessage());
            }

            @Override
            public R<?> getDataByTimestamp(SelectDto selectDto) {
                return R.fail("查询数据失败:{}", throwable.getMessage());
            }
        };
    }
}
