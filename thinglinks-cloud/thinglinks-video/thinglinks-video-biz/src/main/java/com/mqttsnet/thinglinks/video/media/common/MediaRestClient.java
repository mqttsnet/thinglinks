package com.mqttsnet.thinglinks.video.media.common;

import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;

import java.util.Map;

/**
 * Description:
 * 流媒体服务器 REST 客户端统一接口。
 * 封装对 ZLM/ABL 等不同流媒体服务器的 HTTP API 调用，
 * 每种流媒体服务器提供各自的实现类。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface MediaRestClient {

    /**
     * 发送 GET 请求
     *
     * @param mediaServer 流媒体服务器信息
     * @param api         API 路径
     * @param params      请求参数
     * @return API 响应结果
     */
    MediaApiResult get(VideoMediaServer mediaServer, String api, Map<String, Object> params);

    /**
     * 发送 POST 请求（JSON Body）
     *
     * @param mediaServer 流媒体服务器信息
     * @param api         API 路径
     * @param body        请求体（JSON）
     * @return API 响应结果
     */
    MediaApiResult post(VideoMediaServer mediaServer, String api, JSONObject body);

    /**
     * 发送 POST 请求（表单参数）
     *
     * @param mediaServer 流媒体服务器信息
     * @param api         API 路径
     * @param params      表单参数
     * @return API 响应结果
     */
    MediaApiResult postForm(VideoMediaServer mediaServer, String api, Map<String, Object> params);

    /**
     * 构建完整的 API 请求 URL
     *
     * @param mediaServer 流媒体服务器信息
     * @param api         API 路径
     * @return 完整 URL
     */
    String buildUrl(VideoMediaServer mediaServer, String api);
}
