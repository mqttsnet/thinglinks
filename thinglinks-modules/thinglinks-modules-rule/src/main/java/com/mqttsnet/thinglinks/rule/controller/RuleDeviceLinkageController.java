package com.mqttsnet.thinglinks.rule.controller;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.rule.service.RuleDeviceLinkageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: thinglinks
 * @description: 设备联动Controller
 * @packagename: com.mqttsnet.thinglinks.rule.controller
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-08-07 20:31
 **/
@RestController
@RequestMapping("/ruleDeviceLinkage")
@Slf4j
public class RuleDeviceLinkageController extends BaseController {


    @Autowired
    private RuleDeviceLinkageService ruleDeviceLinkageService;

    /**
     * 触发设备联动规则条件
     * @param ruleIdentification 规则标识
     * @return
     */
    @GetMapping(value = "/triggerDeviceLinkage/{ruleIdentification}")
    public R<?> triggerDeviceLinkage(@PathVariable("ruleIdentification") String ruleIdentification) {
        ruleDeviceLinkageService.triggerDeviceLinkageByRuleIdentification(ruleIdentification);
        return R.ok();
    }
}
