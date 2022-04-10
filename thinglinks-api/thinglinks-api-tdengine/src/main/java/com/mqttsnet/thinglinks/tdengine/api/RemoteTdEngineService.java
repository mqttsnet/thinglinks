package com.mqttsnet.thinglinks.tdengine.api;

/**
 * @InterfaceDescription: 时序性数据库TdEngine服务
 * @InterfaceName: RemoteTdEngineService
 * @Author: thinglinks
 * @Date: 2021-12-31 10:57:16
 * @Version 1.0
 */

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;
import com.mqttsnet.thinglinks.tdengine.api.factory.RemoteTdEngineFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "remoteTdEngineService", value = ServiceNameConstants.THINGLINKS_TDENGINE, fallbackFactory = RemoteTdEngineFallbackFactory.class)
public interface RemoteTdEngineService {

    /**
     * @param databaseName 数据库名称
     * @return R
     * @MethodDescription 创建tdEngine数据库
     * @author thinglinks
     * @Date 2021/12/31 11:05
     */
    @PostMapping("/dataOperation/createDb")
    R<?> createDataBase(@RequestParam("databaseName") String databaseName);

    /**
     * @param superTableDto 创建超级表需要的入参的实体类
     * @return R
     * @MethodDescription 创建超级表
     * @author thinglinks
     * @Date 2021/12/27 11:05
     */
    @PostMapping("/dataOperation/createSTb")
    R<?> createSuperTable(@Validated @RequestBody SuperTableDto superTableDto);

    /**
     *@MethodDescription 创建超级表的子表
     *@param tableDto 创建超级表的子表需要的入参的实体类
     *@return R
     *@author thinglinks
     *@Date 2021/12/27 11:06
     */
    @PostMapping("/dataOperation/createTb")
    R<?> createTable(@Validated @RequestBody TableDto tableDto);

    /**
     *@MethodDescription 插入数据
     *@param tableDto 插入数据需要的入参的实体类
     *@return R
     *@author thinglinks
     *@Date 2022/1/10 14:44
     */
    @PostMapping("/dataOperation/insertData")
    R<?> insertData(@Validated @RequestBody TableDto tableDto);

    @PostMapping("/dataOperation/addColumnInStb")
    R<?> addColumnInStb(@RequestBody SuperTableDto superTableDto);

    @PostMapping("/dataOperation/getDataByTimestamp")
    R<?> getDataByTimestamp(@Validated @RequestBody SelectDto selectDto);
}
