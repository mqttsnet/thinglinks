package com.mqttsnet.thinglinks.link.facade;

import com.mqttsnet.basic.base.R;

import java.util.List;

/**
 * 产品 Topic 开放接口 Facade。
 *
 * <p>cloud 部署 → 走 Feign(ProductTopicOpenInnerApi);单体 boot 部署 → 直接调 Service。
 * 业务侧只面向 Facade 接口编程,部署模式切换零改动。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
public interface ProductTopicOpenInnerFacade {

    /**
     * 根据 ProductTopic 主键 ID 列表批量查 topic 模板字符串。
     *
     * @param ids 主键列表(空 / null → 返回空列表)
     * @return topic 模板列表(永不返回 null;失败返回 timeout R)
     */
    R<List<String>> findTopicsByIds(List<Long> ids);
}
