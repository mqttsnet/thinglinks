package com.mqttsnet.thinglinks.video.dto.gb28181.sip;

import com.mqttsnet.thinglinks.video.dto.gb28181.SipTransactionInfo;
import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionTypeEnum;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.Data;

/**
 * 事务信息
 * @author mqttsnet
 */
@Data
public class SsrcTransaction {

    /**
     * 设备编号
     */
    private String deviceIdentification;

    /**
     * 上级平台的编号
     */
    private String platformId;

    /**
     * 通道的数据库ID
     */
    private String channelIdentification;

    /**
     * 会话的CALL ID
     */
    private String callId;

    /**
     * 关联的流应用名
     */
    private String app;

    /**
     * 关联的流ID
     */
    private String stream;

    /**
     * 使用的流媒体
     */
    private String mediaIdentification;

    /**
     * 使用的SSRC
     */
    private String ssrc;

    /**
     * 分配给本次 INVITE 会话的 RTP 接收端口（null 为老数据，孤儿清理时跳过）。
     * <p>与 {@link #ssrc} 同时分配、同时释放 —— 会话关闭时由 {@code InviteSessionService.closeSession}
     * 根据本字段调 {@code RtpPortService.releasePort}，避免端口池耗尽。
     */
    private Integer rtpPort;

    /**
     * INVITE 请求的 From-tag（我方作为 UAC 生成，INVITE 发出时设置）。
     * <p>后续 in-dialog 请求（BYE/INFO/re-INVITE）的 From-tag 必须与此一致，
     * 否则 GB28181 设备会以 481 Call/Transaction Does Not Exist 拒绝。
     * <p>集群场景：存 Redis 让任意实例都能发 BYE。
     */
    private String fromTag;

    /**
     * INVITE 2xx 响应中的 To-tag（对端设备生成，响应到达时抽取保存）。
     * <p>后续 in-dialog 请求（BYE/INFO/re-INVITE）的 To-tag 必须填入此值，
     * 否则设备会判为 out-of-dialog 而拒绝。
     */
    private String toTag;

    /**
     * 当前会话已使用的 CSeq 序号（初始 = INVITE 的 CSeq = 1）。
     * <p>in-dialog 的每个后续请求必须递增 CSeq：BYE/INFO/re-INVITE 都要 {@code cseq + 1}。
     * 违反 RFC 3261 §14.1 会被 SBC/代理或设备拒绝。
     * <p>使用 Long 与 JAIN-SIP {@code CSeqHeader.getSeqNumber()} 对齐。
     */
    private Long cseq;

    /**
     * 事务信息
     */
    private SipTransactionInfo sipTransactionInfo;

    /**
     * 类型
     */
    private InviteSessionTypeEnum type;

    /**
     * 创建时间（epoch millis）。用于孤儿会话对账时的宽限期判定，
     * 避免刚创建还未完成 INVITE 协商的会话被误判回收。
     */
    private Long createdAt;

    public SsrcTransaction() {
        this.createdAt = System.currentTimeMillis();
    }

    public static SsrcTransaction buildForDevice(String deviceIdentification, String channelIdentification, String callId, String app, String stream,
                                                 String ssrc, String mediaIdentification, SIPResponse response, InviteSessionTypeEnum type) {
        SsrcTransaction ssrcTransaction = new SsrcTransaction();
        ssrcTransaction.setDeviceIdentification(deviceIdentification);
        ssrcTransaction.setChannelIdentification(channelIdentification);
        ssrcTransaction.setCallId(callId);
        ssrcTransaction.setApp(app);
        ssrcTransaction.setStream(stream);
        ssrcTransaction.setMediaIdentification(mediaIdentification);
        ssrcTransaction.setSsrc(ssrc);
        ssrcTransaction.setSipTransactionInfo(new SipTransactionInfo(response));
        ssrcTransaction.setType(type);
        return ssrcTransaction;
    }

    public static SsrcTransaction buildForPlatform(String platformId, String channelIdentification, String callId, String app, String stream,
                                                   String ssrc, String mediaIdentification, SIPResponse response, InviteSessionTypeEnum type) {
        SsrcTransaction ssrcTransaction = new SsrcTransaction();
        ssrcTransaction.setPlatformId(platformId);
        ssrcTransaction.setChannelIdentification(channelIdentification);
        ssrcTransaction.setCallId(callId);
        ssrcTransaction.setStream(stream);
        ssrcTransaction.setApp(app);
        ssrcTransaction.setMediaIdentification(mediaIdentification);
        ssrcTransaction.setSsrc(ssrc);
        ssrcTransaction.setSipTransactionInfo(new SipTransactionInfo(response));
        ssrcTransaction.setType(type);
        return ssrcTransaction;
    }

    /**
     * 会话存续毫秒数，依据 {@link #createdAt}。createdAt 为空时返回 -1（旧数据兜底）。
     */
    public long ageMillis() {
        return createdAt == null ? -1 : (System.currentTimeMillis() - createdAt);
    }
}
