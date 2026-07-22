package com.mqttsnet.thinglinks.video.media.server.event.media;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * 流媒体事件基类。
 * <p>
 * 构造时自动快照当前线程的租户上下文（ContextUtil.getLocalMap），
 * 供 @Async 监听方通过 {@link #getContextLocalMap()} 恢复上下文。
 */
public class MediaEvent extends ApplicationEvent {

    private String app;
    private String stream;
    private VideoMediaServerResultDTO mediaServer;
    private String schema;

    /**
     * 发布时自动快照租户上下文，@Async 监听方用 ContextUtil.setLocalMap 恢复
     */
    private final Map<String, String> contextLocalMap;

    public MediaEvent(Object source) {
        super(source);
        this.contextLocalMap = ContextUtil.getLocalMap();
    }

    public Map<String, String> getContextLocalMap() {
        return contextLocalMap;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public VideoMediaServerResultDTO getMediaServer() {
        return mediaServer;
    }

    public void setMediaServer(VideoMediaServerResultDTO mediaServer) {
        this.mediaServer = mediaServer;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

}
