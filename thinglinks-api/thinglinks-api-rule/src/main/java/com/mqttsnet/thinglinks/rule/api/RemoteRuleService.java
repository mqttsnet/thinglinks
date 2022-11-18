package com.mqttsnet.thinglinks.rule.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.rule.api.factory.RemoteRuleFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @InterfaceDescription: 规则服务
 * @InterfaceName: RemoteRuleService
 * @Author: thinglinks
 * @Date: 2021-12-31 10:57:16
 * @Version 1.0
 */

@FeignClient(contextId = "remoteRuleService", value = ServiceNameConstants.THINGLINKS_RULE, fallbackFactory = RemoteRuleFallbackFactory.class)
public interface RemoteRuleService {

    @GetMapping("/rule/ruleDeviceLinkage/triggerDeviceLinkage/{ruleIdentification}")
    public R<?> triggerDeviceLinkage(@PathVariable("ruleIdentification") String ruleIdentification);

}
