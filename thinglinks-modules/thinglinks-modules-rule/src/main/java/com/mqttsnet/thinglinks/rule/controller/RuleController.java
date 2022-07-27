package com.mqttsnet.thinglinks.rule.controller;

import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.RemoteProductService;
import com.mqttsnet.thinglinks.rule.api.domain.Rule;
import com.mqttsnet.thinglinks.rule.api.domain.RuleConditions;
import com.mqttsnet.thinglinks.rule.service.RuleConditionsService;
import com.mqttsnet.thinglinks.rule.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 规则处理类
 *
 * @author shisen
 */
@RestController
@RequestMapping("/rule")
public class RuleController extends BaseController {

    @Autowired
    private RuleService ruleService;

    @Autowired
    private RuleConditionsService ruleConditionsService;

    @Autowired
    private RemoteProductService remoteProductService;

    /**
     * 规则触发条件验证
     */
    @GetMapping(value = "/checkRuleConditions")
    public AjaxResult checkRuleConditions(@RequestParam(value = "ruleIdentification", required = true) String ruleIdentification) {
        // 查询规则
        Rule rule = ruleService.selectByRuleIdentification(ruleIdentification);
        if (Objects.isNull(rule)) {
            return AjaxResult.error("规则不存在！");
        }
        // 查询触发条件
        List<RuleConditions> ruleConditions = ruleConditionsService.selectByRuleId(rule.getId());
        for (RuleConditions re : ruleConditions) {
            remoteProductService.selectByIdProperties(re.getPropertiesId());
        }
        // 验证条件
        return AjaxResult.success(true);
    }
}
