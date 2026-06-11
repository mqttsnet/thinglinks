package com.mqttsnet.thinglinks.video.dto.gb28181.audio;


import com.mqttsnet.thinglinks.video.dto.gb28181.SipTransactionInfo;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.AudioBroadcastCatchStatusEnum;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.Data;

/**
 * 缓存语音广播的状态
 * @author mqttsnet
 */
@Data
public class AudioBroadcastCatch {


    /**
     * 设备编号
     */
    private String deviceIdentification;
    /**
     * 通道编号
     */
    private String channelIdentification;
    /**
     * 流媒体信息
     */
    private VideoMediaServerResultDTO mediaServerItem;
    /**
     * 关联的流APP
     */
    private String app;
    /**
     * 关联的流STREAM
     */
    private String stream;
    /**
     *  是否是级联语音喊话
     */
    private boolean isFromPlatform;
    /**
     * 语音广播状态
     */
    private AudioBroadcastCatchStatusEnum status;
    /**
     * 请求信息
     */
    private SipTransactionInfo sipTransactionInfo;
    /**
     * 请求结果回调
     */
    private AudioBroadcastEvent event;

    public AudioBroadcastCatch(
            String deviceIdentification,
            String channelIdentification,
            VideoMediaServerResultDTO mediaServerItem,
            String app,
            String stream,
            AudioBroadcastEvent event,
            AudioBroadcastCatchStatusEnum status,
            boolean isFromPlatform
    ) {
        this.deviceIdentification = deviceIdentification;
        this.channelIdentification = channelIdentification;
        this.status = status;
        this.event = event;
        this.isFromPlatform = isFromPlatform;
        this.app = app;
        this.stream = stream;
        this.mediaServerItem = mediaServerItem;
    }

    public AudioBroadcastCatch() {
    }

    public void setSipTransactionInfoByRequest(SIPResponse sipResponse) {
        this.sipTransactionInfo = new SipTransactionInfo(sipResponse);
    }
}
