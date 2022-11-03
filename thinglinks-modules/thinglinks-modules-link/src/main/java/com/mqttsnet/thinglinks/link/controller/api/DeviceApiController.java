package com.mqttsnet.thinglinks.link.controller.api;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 设备Api接口
 *
 * @author thinglinks
 */
@RestController
@RequestMapping("/device/api")
public class DeviceApiController {

    /**
     * 服务对象
     */
    @Resource
    private DeviceService deviceService;


    /**
     * 根据产品标识查询所属的设备标识
     */
    @GetMapping("/select-by-product-identification/{productIdentification}")
    public R<?> selectByProductIdentification(@PathVariable("productIdentification") String productIdentification) {
        return R.ok(deviceService.selectByProductIdentification(productIdentification));
    }

}
