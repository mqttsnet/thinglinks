package com.mqttsnet.thinglinks.video.media.common;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * 流媒体服务器 REST API 统一响应包装类。
 * 封装 ZLM/ABL 等不同流媒体服务器的 HTTP API 响应，
 * 提供统一的成功/失败判断和数据提取方法。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaApiResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码（0=成功，非0=失败）
     */
    private int code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据（JSON 格式）
     */
    private JSONObject data;

    /**
     * 原始响应体
     */
    private String rawBody;

    /**
     * 判断请求是否成功
     *
     * @return true=成功
     */
    public boolean isSuccess() {
        return code == 0;
    }

    /**
     * 构建成功响应
     *
     * @param data 响应数据
     * @return 成功的 MediaApiResult
     */
    public static MediaApiResult success(JSONObject data) {
        return MediaApiResult.builder()
                .code(0)
                .msg("success")
                .data(data)
                .build();
    }

    /**
     * 构建失败响应
     *
     * @param code 错误码
     * @param msg  错误消息
     * @return 失败的 MediaApiResult
     */
    public static MediaApiResult fail(int code, String msg) {
        return MediaApiResult.builder()
                .code(code)
                .msg(msg)
                .build();
    }

    /**
     * 构建超时响应
     *
     * @param msg 超时消息
     * @return 超时的 MediaApiResult
     */
    public static MediaApiResult timeout(String msg) {
        return MediaApiResult.builder()
                .code(-1)
                .msg(msg)
                .build();
    }
}
