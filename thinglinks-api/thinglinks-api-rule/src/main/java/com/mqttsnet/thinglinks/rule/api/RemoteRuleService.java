package com.mqttsnet.thinglinks.rule.api;

/**
 * @InterfaceDescription: 规则服务
 * @InterfaceName: RemoteRuleService
 * @Author: thinglinks
 * @Date: 2021-12-31 10:57:16
 * @Version 1.0
 */

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.rule.api.factory.RemoteRuleFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(contextId = "remoteRuleService", value = ServiceNameConstants.THINGLINKS_RULE, fallbackFactory = RemoteRuleFallbackFactory.class)
public interface RemoteRuleService {

    @GetMapping("/rule/api/check-rule-conditions/{ruleIdentification}")
    public R<Boolean> checkRuleConditions(@PathVariable("ruleIdentification") String ruleIdentification);

}
