package com.mqttsnet.thinglinks.video.dto.media.zlm.hook;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ZLM Hook 回复对象。
 * <p>
 * 标准字段 {@code code}/{@code msg}。
 * 可选字段 {@code close}（仅 on_stream_none_reader 需要）：
 * true 表示让 ZLM 立即关闭该流，false 保留。默认不序列化 null 值，避免对
 * 其他不支持该字段的 hook 产生干扰。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HookResult {

    private int code;
    private String msg;

    /**
     * on_stream_none_reader 专用：是否让 ZLM 关闭流。
     */
    private Boolean close;

    public HookResult() {
    }

    public HookResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static HookResult SUCCESS() {
        return new HookResult(0, "success");
    }

    public static HookResultForOnPublish Fail() {
        return new HookResultForOnPublish(-1, "fail");
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Boolean getClose() {
        return close;
    }

    public void setClose(Boolean close) {
        this.close = close;
    }
}
