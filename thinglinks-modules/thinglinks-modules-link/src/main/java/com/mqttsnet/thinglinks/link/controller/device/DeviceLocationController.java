package com.mqttsnet.thinglinks.link.controller.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceLocation;
import com.mqttsnet.thinglinks.link.service.device.DeviceLocationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (device_location)表控制层
 *
 * @author xxxxx
 */
@RestController
@RequestMapping("device_location")
public class DeviceLocationController {
    /**
     * 服务对象
     */
    @Resource
    private DeviceLocationService deviceLocationService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public DeviceLocation selectOne(Integer id) {
        return deviceLocationService.selectByPrimaryKey(Long.valueOf(id));
    }


}
