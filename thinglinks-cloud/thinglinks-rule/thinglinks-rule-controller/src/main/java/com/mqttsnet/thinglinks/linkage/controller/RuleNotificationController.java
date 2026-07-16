package com.mqttsnet.thinglinks.linkage.controller;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.service.alarm.RuleNotificationTemplateService;
import com.mqttsnet.thinglinks.vo.param.linkage.RuleNotificationPreviewParamVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleNotificationPreviewResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleNotificationVariableGroupResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 规则通知模板。
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/ruleNotification")
@Tag(name = "规则通知模板")
public class RuleNotificationController {

    private final RuleNotificationTemplateService ruleNotificationTemplateService;

    @Operation(summary = "查询规则通知模板变量")
    @GetMapping("/variables")
    public R<List<RuleNotificationVariableGroupResultVO>> variables() {
        return R.success(ruleNotificationTemplateService.listVariables());
    }

    @Operation(summary = "预览规则通知模板")
    @PostMapping("/preview")
    public R<RuleNotificationPreviewResultVO> preview(@RequestBody RuleNotificationPreviewParamVO param) {
        return R.success(ruleNotificationTemplateService.preview(param));
    }
}
