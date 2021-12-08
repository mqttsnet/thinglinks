package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.factory.RemoteMqttsDeviceDatasFallbackFactory;
import com.mqttsnet.thinglinks.link.api.domain.MqttsDeviceDatas;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 设备消息服务
 *
 * @author shisen
 */
@FeignClient(contextId = "remoteMqttsDeviceDatasService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteMqttsDeviceDatasFallbackFactory.class)
public interface RemoteMqttsDeviceDatasService {

    /**
     * 新增设备消息
     */
    @PostMapping("/device/datas/add")
    public R add(@RequestBody MqttsDeviceDatas mqttsDeviceDatas);

}
