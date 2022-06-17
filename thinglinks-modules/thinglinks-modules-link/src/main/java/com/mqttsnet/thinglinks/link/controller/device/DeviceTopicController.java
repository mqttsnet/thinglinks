package com.mqttsnet.thinglinks.link.controller.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceTopic;
import com.mqttsnet.thinglinks.link.service.device.DeviceTopicService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 设备Topic管理
 *
 * @author sunshihuan
 */
@RestController
@RequestMapping("/deviceTopic")
public class DeviceTopicController {
    /**
     * 服务对象
     */
    @Resource
    private DeviceTopicService deviceTopicService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public DeviceTopic selectOne(Integer id) {
        return deviceTopicService.selectByPrimaryKey(Long.valueOf(id));
    }

}
