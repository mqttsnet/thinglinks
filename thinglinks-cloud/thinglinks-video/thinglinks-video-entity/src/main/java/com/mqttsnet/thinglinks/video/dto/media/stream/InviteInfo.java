package com.mqttsnet.thinglinks.video.dto.media.stream;

import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionStatusEnum;
import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionTypeEnum;
import lombok.Data;

/**
 * 记录每次发送invite消息的状态
 * @author mqttsnet
 */
@Data
public class InviteInfo {

    private String deviceIdentification;

    private String channelIdentification;

    private String stream;

    private SSRCInfo ssrcInfo;

    private String receiveIp;

    private Integer receivePort;

    private String streamMode;

    private InviteSessionTypeEnum type;

    private InviteSessionStatusEnum status;

    private StreamInfo streamInfo;

    private String mediaIdentification;

    private Long expirationTime;

    private Long createTime;

    private Boolean record;

    private String startTime;

    private String endTime;


    public static InviteInfo getInviteInfo(String deviceIdentification, String channelIdentification, String stream, SSRCInfo ssrcInfo, String mediaIdentification,
                                           String receiveIp, Integer receivePort, String streamMode,
                                           InviteSessionTypeEnum type, InviteSessionStatusEnum status, Boolean record) {
        InviteInfo inviteInfo = new InviteInfo();
        inviteInfo.setDeviceIdentification(deviceIdentification);
        inviteInfo.setChannelIdentification(channelIdentification);
        inviteInfo.setStream(stream);
        inviteInfo.setSsrcInfo(ssrcInfo);
        inviteInfo.setReceiveIp(receiveIp);
        inviteInfo.setReceivePort(receivePort);
        inviteInfo.setStreamMode(streamMode);
        inviteInfo.setType(type);
        inviteInfo.setStatus(status);
        inviteInfo.setMediaIdentification(mediaIdentification);
        inviteInfo.setRecord(record);
        return inviteInfo;
    }

}
