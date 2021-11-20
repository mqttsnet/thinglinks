package net.mqtts.link.api;

import net.mqtts.common.core.constant.ServiceNameConstants;
import net.mqtts.common.core.domain.R;
import net.mqtts.link.api.domain.MqttsDeviceDatas;
import net.mqtts.link.api.factory.RemoteMqttsDeviceDatasFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 设备消息服务
 *
 * @author shisen
 */
@FeignClient(contextId = "remoteMqttsDeviceDatasService", value = ServiceNameConstants.MQTTS_LINK, fallbackFactory = RemoteMqttsDeviceDatasFallbackFactory.class)
public interface RemoteMqttsDeviceDatasService {

    /**
     * 新增设备消息
     */
    @PostMapping("/device/datas/add")
    public R add(@RequestBody MqttsDeviceDatas mqttsDeviceDatas);

}
