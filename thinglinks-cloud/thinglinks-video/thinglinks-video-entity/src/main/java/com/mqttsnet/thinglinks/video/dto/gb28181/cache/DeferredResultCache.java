package com.mqttsnet.thinglinks.video.dto.gb28181.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 异步请求结果缓存对象。
 * <p>
 * 用于跨节点的 DeferredResult 结果传递。
 * 本地保留 DeferredResult 引用，Redis 存储可序列化的结果信号。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeferredResultCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 命令类型（如 CALLBACK_PLAY）
     */
    private String commandType;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 结果 JSON
     */
    private String resultJson;

    /**
     * 创建时间戳
     */
    private Long createdAt;
}
