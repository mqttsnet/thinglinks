package com.mqttsnet.thinglinks.inner.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.producttopic.service.ProductTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 产品 Topic 内部接口(inner):Feign 服务间 RPC(Nacos 直连、不过网关),透传 TenantId、无需 Token；网关拒绝外部访问。
 * 主要给北向 TOPIC_IDS 桥接模式用。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/inner/productTopicOpen")
@Tag(name = "inner-产品Topic相关API")
public class ProductTopicOpenInnerController {

    @Resource
    private ProductTopicService productTopicService;

    /**
     * 根据 ID 列表批量查 topic 模板。fail-soft + 上限 500。
     */
    @Operation(summary = "北向API批量查询产品Topic")
    @PostMapping("/findTopicsByIds")
    @WebLog("北向API批量查询产品Topic")
    public R<List<String>> findTopicsByIds(@RequestBody(required = false) List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return R.success(Collections.emptyList());
            }
            return R.success(productTopicService.findTopicsByIds(ids));
        } catch (Exception e) {
            log.error("查询产品Topic失败,ids size={}", ids.size(), e);
            return R.fail("查询产品Topic失败: " + e.getMessage());
        }
    }
}
