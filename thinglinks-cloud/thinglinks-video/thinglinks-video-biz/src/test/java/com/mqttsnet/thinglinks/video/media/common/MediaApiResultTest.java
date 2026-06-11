package com.mqttsnet.thinglinks.video.media.common;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Description:
 * MediaApiResult 统一响应包装类单元测试。
 * 覆盖成功/失败/超时构建、状态判断、错误码语义。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DisplayName("MediaApiResult 统一响应包装测试")
class MediaApiResultTest {

    @Test
    @DisplayName("success_构建成功响应_code为0且isSuccess为true")
    void success_buildsSuccessResponse_codeZeroAndIsSuccessTrue() {
        var data = new JSONObject();
        data.put("key", "value");
        var result = MediaApiResult.success(data);

        assertEquals(0, result.getCode());
        assertEquals("success", result.getMsg());
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("value", result.getData().getString("key"));
    }

    @Test
    @DisplayName("success_空数据_仍然成功")
    void success_nullData_stillSuccess() {
        var result = MediaApiResult.success(null);

        assertTrue(result.isSuccess());
        assertEquals(0, result.getCode());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("fail_构建失败响应_code非0且isSuccess为false")
    void fail_buildsFailResponse_codeNonZeroAndIsSuccessFalse() {
        var result = MediaApiResult.fail(-2, "参数错误");

        assertEquals(-2, result.getCode());
        assertEquals("参数错误", result.getMsg());
        assertFalse(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("fail_code为负2_表示客户端错误不可重试")
    void fail_codeNeg2_clientErrorNonRetryable() {
        var result = MediaApiResult.fail(-2, "JSON解析失败");

        assertFalse(result.isSuccess());
        assertEquals(-2, result.getCode());
        // code=-2 是客户端错误，不可重试（与 code=-1 网络超时区分）
        assertNotEquals(-1, result.getCode());
    }

    @Test
    @DisplayName("timeout_构建超时响应_code为负1")
    void timeout_buildsTimeoutResponse_codeNeg1() {
        var result = MediaApiResult.timeout("连接超时: Connection refused");

        assertEquals(-1, result.getCode());
        assertTrue(result.getMsg().contains("连接超时"));
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("timeout_code为负1_表示网络超时可重试")
    void timeout_codeNeg1_networkTimeoutRetryable() {
        var result = MediaApiResult.timeout("超时");

        // code=-1 是网络超时，RetryableMediaRestClient 会重试
        assertEquals(-1, result.getCode());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("isSuccess_各种code值_只有0返回true")
    void isSuccess_variousCodes_onlyZeroReturnsTrue() {
        assertTrue(MediaApiResult.builder().code(0).build().isSuccess());
        assertFalse(MediaApiResult.builder().code(1).build().isSuccess());
        assertFalse(MediaApiResult.builder().code(-1).build().isSuccess());
        assertFalse(MediaApiResult.builder().code(-2).build().isSuccess());
        assertFalse(MediaApiResult.builder().code(-3).build().isSuccess());
        assertFalse(MediaApiResult.builder().code(100).build().isSuccess());
    }

    @Test
    @DisplayName("builder_完整构建_所有字段正确")
    void builder_fullBuild_allFieldsCorrect() {
        var data = new JSONObject();
        data.put("streams", 5);
        var result = MediaApiResult.builder()
                .code(0)
                .msg("success")
                .data(data)
                .rawBody("{\"code\":0,\"streams\":5}")
                .build();

        assertEquals(0, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals(5, result.getData().getIntValue("streams"));
        assertEquals("{\"code\":0,\"streams\":5}", result.getRawBody());
    }

    @Test
    @DisplayName("noArgsConstructor_默认值_code为0但无msg")
    void noArgsConstructor_defaultValues() {
        var result = new MediaApiResult();

        assertEquals(0, result.getCode()); // int 默认值
        assertNull(result.getMsg());
        assertNull(result.getData());
        assertNull(result.getRawBody());
        // 注意：默认 code=0 虽然 isSuccess()=true，但这是无意义的空对象
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("fail_code为负3_表示重试耗尽或中断")
    void fail_codeNeg3_retryExhaustedOrInterrupted() {
        var result = MediaApiResult.fail(-3, "重试耗尽");

        assertEquals(-3, result.getCode());
        assertFalse(result.isSuccess());
        // code=-3 由 RetryableMediaRestClient 在重试耗尽或中断时返回
    }

    @Test
    @DisplayName("serializable_实现Serializable接口")
    void serializable_implementsSerializable() {
        var result = MediaApiResult.success(new JSONObject());
        assertTrue(result instanceof java.io.Serializable);
    }
}
