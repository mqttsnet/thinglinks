package com.mqttsnet.thinglinks.video.gb28181.transmit.callback;

import com.mqttsnet.thinglinks.video.dto.gb28181.event.DeferredResultEx;
import com.mqttsnet.thinglinks.video.manager.sip.DeferredResultManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步请求处理（本地 DeferredResult + Redis 信号模式）。
 * <p>
 * DeferredResult 是 Spring Web 对象，不可序列化，必须保留在本地。
 * Redis 用于记录结果信号，支持未来跨节点结果传递。
 *
 * @author mqttsnet
 */
@Slf4j
@SuppressWarnings(value = {"rawtypes", "unchecked"})
@Component
@RequiredArgsConstructor
public class DeferredResultHolder {

    public static final String CALLBACK_CMD_PLAY = "CALLBACK_PLAY";
    public static final String CALLBACK_CMD_PLAYBACK = "CALLBACK_PLAYBACK";
    public static final String CALLBACK_CMD_DOWNLOAD = "CALLBACK_DOWNLOAD";
    public static final String UPLOAD_FILE_CHANNEL = "UPLOAD_FILE_CHANNEL";
    public static final String CALLBACK_CMD_MOBILE_POSITION = "CALLBACK_CMD_MOBILE_POSITION";
    public static final String CALLBACK_CMD_SNAP = "CALLBACK_SNAP";

    private final DeferredResultManager deferredResultManager;

    /**
     * 本地 DeferredResult 注册表（保留，因为 DeferredResult 不可序列化）
     */
    private final Map<String, Map<String, DeferredResultEx>> map = new ConcurrentHashMap<>();

    public void put(String key, String id, DeferredResultEx result) {
        Map<String, DeferredResultEx> deferredResultMap = map.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        deferredResultMap.put(id, result);
        deferredResultManager.writePending(key, id);
    }

    public void put(String key, String id, DeferredResult result) {
        put(key, id, new DeferredResultEx(result));
    }

    public DeferredResultEx get(String key, String id) {
        Map<String, DeferredResultEx> deferredResultMap = map.get(key);
        if (deferredResultMap == null || ObjectUtils.isEmpty(id)) {
            return null;
        }
        return deferredResultMap.get(id);
    }

    public Collection<DeferredResultEx> getAllByKey(String key) {
        Map<String, DeferredResultEx> deferredResultMap = map.get(key);
        if (deferredResultMap == null) {
            return null;
        }
        return deferredResultMap.values();
    }

    public boolean exist(String key, String id) {
        if (key == null) {
            return false;
        }
        Map<String, DeferredResultEx> deferredResultMap = map.get(key);
        if (id == null) {
            return deferredResultMap != null;
        } else {
            return deferredResultMap != null && deferredResultMap.get(id) != null;
        }
    }

    /**
     * 释放单个请求
     */
    public void invokeResult(RequestMessage msg) {
        Map<String, DeferredResultEx> deferredResultMap = map.get(msg.getKey());
        if (deferredResultMap == null) {
            return;
        }
        DeferredResultEx result = deferredResultMap.get(msg.getId());
        if (result == null) {
            return;
        }
        result.getDeferredResult().setResult(msg.getData());
        deferredResultMap.remove(msg.getId());
        if (deferredResultMap.isEmpty()) {
            map.remove(msg.getKey());
        }
        deferredResultManager.writeResult(msg.getKey(), msg.getId());
    }

    /**
     * 释放所有的请求
     */
    public void invokeAllResult(RequestMessage msg) {
        Map<String, DeferredResultEx> deferredResultMap = map.get(msg.getKey());
        if (deferredResultMap == null) {
            return;
        }
        synchronized (this) {
            deferredResultMap = map.get(msg.getKey());
            if (deferredResultMap == null) {
                return;
            }
            Set<String> ids = deferredResultMap.keySet();
            for (String id : ids) {
                DeferredResultEx result = deferredResultMap.get(id);
                if (result == null) {
                    return;
                }
                if (result.getFilter() != null) {
                    Object handler = result.getFilter().handler(msg.getData());
                    result.getDeferredResult().setResult(handler);
                } else {
                    result.getDeferredResult().setResult(msg.getData());
                }
                deferredResultManager.writeResult(msg.getKey(), id);
            }
            map.remove(msg.getKey());
        }
    }

}
