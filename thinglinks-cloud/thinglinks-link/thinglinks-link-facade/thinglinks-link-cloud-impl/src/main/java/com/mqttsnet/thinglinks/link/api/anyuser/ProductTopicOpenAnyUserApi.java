package com.mqttsnet.thinglinks.link.api.anyuser;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.link.api.anyuser.hystrix.ProductTopicOpenAnyUserApiFallback;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 产品 Topic 开放接口 Feign。北向 TOPIC_IDS 桥接模式调本接口反查模板。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:thinglinks-link-server}",
        fallback = ProductTopicOpenAnyUserApiFallback.class, path = "/anyUser/productTopicOpen")
public interface ProductTopicOpenAnyUserApi {

    /**
     * 根据 ProductTopic ID 列表批量查 topic 模板字符串。
     */
    @Operation(summary = "北向API批量查询产品Topic", description = "根据ProductTopic ID列表反查topic模板列表")
    @PostMapping(path = "/findTopicsByIds")
    R<List<String>> findTopicsByIds(@RequestBody List<Long> ids);
}
