package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.TopoAddSubDeviceParam;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.TopoDeleteSubDeviceParam;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.TopoUpdateSubDeviceStatusParam;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.link.api.factory.RemoteDeviceInfoFallbackFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 子设备管理服务
 *
 * @author ShiHuan SUN
 */
@FeignClient(contextId = "remoteDeviceInfoService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteDeviceInfoFallbackFactory.class)
public interface RemoteDeviceInfoService {

    /**
     * 刷新子设备数据模型
     *
     * @param ids
     * @return
     */
    @GetMapping("/deviceInfo/refreshDeviceInfoDataModel")
    public AjaxResult refreshDeviceInfoDataModel(@RequestParam(name = "ids", required = false) Long[] ids);




}
