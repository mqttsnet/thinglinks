package com.mqttsnet.thinglinks.video.dto.media.zlm.hook;

import com.mqttsnet.thinglinks.video.enumeration.hook.HookTypeEnum;
import lombok.Data;

/**
 * zlm hook事件的参数
 * @author mqttsnet
 */
@Data
public class Hook {

    private HookTypeEnum hookType;

    private String app;

    private String stream;

    private Long expireTime;


    public static Hook getInstance(HookTypeEnum hookType, String app, String stream) {
        Hook hookSubscribe = new Hook();
        hookSubscribe.setApp(app);
        hookSubscribe.setStream(stream);
        hookSubscribe.setHookType(hookType);
        hookSubscribe.setExpireTime(System.currentTimeMillis() + 5 * 60 * 1000);
        return hookSubscribe;
    }

    public static Hook getInstance(HookTypeEnum hookType, String app, String stream, String mediaServer) {
        // TODO 后续修改所有方法
        return Hook.getInstance(hookType, app, stream);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Hook) {
            Hook param = (Hook) obj;
            return param.getHookType().equals(this.hookType)
                   && param.getApp().equals(this.app)
                   && param.getStream().equals(this.stream);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getHookType() + this.getApp() + this.getStream();
    }
}
