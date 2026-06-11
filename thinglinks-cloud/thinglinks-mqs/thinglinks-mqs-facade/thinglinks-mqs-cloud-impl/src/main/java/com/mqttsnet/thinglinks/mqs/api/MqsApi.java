package com.mqttsnet.thinglinks.mqs.api;

import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.mqs.api.hystrix.MqsApiFallback;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * mqs 远程接口
 *
 * @author xiaonannet
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:thinglinks-mqs-server}", fallback = MqsApiFallback.class, path = "/mqs")
public interface MqsApi {


}
