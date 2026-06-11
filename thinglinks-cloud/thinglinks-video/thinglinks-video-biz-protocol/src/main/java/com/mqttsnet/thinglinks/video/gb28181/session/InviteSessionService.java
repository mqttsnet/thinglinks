package com.mqttsnet.thinglinks.video.gb28181.session;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.SsrcTransaction;
import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionStatusEnum;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.InviteSessionEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.InviteSessionChangedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.InviteSessionClosedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.InviteSessionCreatedEventSource;
import com.mqttsnet.thinglinks.video.manager.ssrc.SsrcTransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Description:
 * Invite 会话业务服务。
 * 管理 SIP INVITE 会话的生命周期，包括创建、状态变更和关闭。
 * 所有状态变更均通过事件驱动通知下游组件。
 * <p>
 * 状态流转：INIT → INVITED → CONNECTED → CLOSING → CLOSED
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class InviteSessionService {

    private final SsrcTransactionManager ssrcTransactionManager;
    private final SsrcPoolService ssrcPoolService;
    private final RtpPortService rtpPortService;
    private final InviteSessionEventPublisher inviteSessionEventPublisher;

    /**
     * 创建会话并保存事务信息
     *
     * @param transaction SSRC 事务
     */
    public void createSession(SsrcTransaction transaction) {
        ssrcTransactionManager.put(transaction.getDeviceIdentification(), transaction);

        // 发布会话创建事件
        inviteSessionEventPublisher.publishInviteSessionCreatedEvent(InviteSessionCreatedEventSource.builder()
                .deviceIdentification(transaction.getDeviceIdentification())
                .channelIdentification(transaction.getChannelIdentification())
                .callId(transaction.getCallId())
                .ssrc(transaction.getSsrc())
                .sessionType(transaction.getType())
                .build());

        log.info("创建Invite会话: deviceIdentification={}, callId={}, type={}, ssrc={}",
                transaction.getDeviceIdentification(), transaction.getCallId(), transaction.getType(), transaction.getSsrc());
    }

    /**
     * 更新会话状态
     *
     * @param deviceIdentification  设备编号
     * @param callId    Call-ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @throws BizException 非法状态转换时抛出
     */
    public void updateSessionStatus(String deviceIdentification, String callId,
                                     InviteSessionStatusEnum oldStatus, InviteSessionStatusEnum newStatus) {
        if (!oldStatus.canTransitTo(newStatus)) {
            throw BizException.wrap(
                    String.format("非法的会话状态转换: %s → %s, deviceIdentification=%s, callId=%s",
                            oldStatus.getDesc(), newStatus.getDesc(), deviceIdentification, callId));
        }

        // 发布状态变更事件
        inviteSessionEventPublisher.publishInviteSessionChangedEvent(InviteSessionChangedEventSource.builder()
                .deviceIdentification(deviceIdentification)
                .callId(callId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build());

        log.info("会话状态变更: deviceIdentification={}, callId={}, {} → {}", deviceIdentification, callId, oldStatus.getDesc(), newStatus.getDesc());
    }

    /**
     * 关闭会话，释放关联资源（SSRC）
     *
     * @param deviceIdentification      设备编号
     * @param callId        Call-ID
     * @param closeReason   关闭原因
     */
    public void closeSession(String deviceIdentification, String callId, String closeReason) {
        SsrcTransaction transaction = ssrcTransactionManager.get(deviceIdentification, callId);
        if (transaction == null) {
            log.warn("关闭会话时未找到事务: deviceIdentification={}, callId={}", deviceIdentification, callId);
            return;
        }

        // 释放 SSRC
        if (StrUtil.isNotBlank(transaction.getSsrc()) && StrUtil.isNotBlank(transaction.getMediaIdentification())) {
            ssrcPoolService.releaseSsrc(transaction.getMediaIdentification(), transaction.getSsrc());
        }

        // 释放 RTP 端口（Phase 2-2）：之前这里只释放 SSRC，RTP 端口每次播放都泄漏，
        // 长期运行导致端口池耗尽再也播不出画面。rtpPort 字段可能为 null（老事务 / 非 Play 类型），
        // 为 null 时跳过即可；release 失败只记 warn，不阻断 SSRC 释放和事务删除。
        Integer rtpPort = transaction.getRtpPort();
        if (rtpPort != null && rtpPort > 0 && StrUtil.isNotBlank(transaction.getMediaIdentification())) {
            try {
                rtpPortService.releasePort(transaction.getMediaIdentification(), rtpPort);
            } catch (Exception e) {
                log.warn("关闭会话释放 RTP 端口失败: deviceIdentification={}, callId={}, rtpPort={}, error={}",
                        deviceIdentification, callId, rtpPort, e.getMessage());
            }
        }

        // 删除事务
        ssrcTransactionManager.remove(deviceIdentification, callId);

        // 发布会话关闭事件
        inviteSessionEventPublisher.publishInviteSessionClosedEvent(InviteSessionClosedEventSource.builder()
                .deviceIdentification(deviceIdentification)
                .callId(callId)
                .ssrc(transaction.getSsrc())
                .mediaIdentification(transaction.getMediaIdentification())
                .closeReason(closeReason)
                .sessionType(transaction.getType())
                .build());

        log.info("关闭Invite会话: deviceIdentification={}, callId={}, reason={}", deviceIdentification, callId, closeReason);
    }

    /**
     * 根据设备编号和 Call-ID 获取事务
     *
     * @param deviceIdentification 设备编号
     * @param callId   Call-ID
     * @return 事务信息
     */
    public Optional<SsrcTransaction> getTransaction(String deviceIdentification, String callId) {
        return Optional.ofNullable(ssrcTransactionManager.get(deviceIdentification, callId));
    }

    /**
     * 获取设备的所有事务
     *
     * @param deviceIdentification 设备编号
     * @return 事务列表
     */
    public List<SsrcTransaction> getAllTransactions(String deviceIdentification) {
        return ssrcTransactionManager.getAll(deviceIdentification);
    }

    /**
     * 根据流 ID 查找事务
     *
     * @param deviceIdentification 设备编号
     * @param stream   流 ID
     * @return 事务信息
     */
    public Optional<SsrcTransaction> getTransactionByStream(String deviceIdentification, String stream) {
        return Optional.ofNullable(ssrcTransactionManager.getByStream(deviceIdentification, stream));
    }

    /**
     * 关闭设备的所有会话
     *
     * @param deviceIdentification    设备编号
     * @param closeReason 关闭原因
     */
    public void closeAllSessions(String deviceIdentification, String closeReason) {
        List<SsrcTransaction> transactions = ssrcTransactionManager.getAll(deviceIdentification);
        for (SsrcTransaction transaction : transactions) {
            closeSession(deviceIdentification, transaction.getCallId(), closeReason);
        }
        log.info("关闭设备所有会话: deviceIdentification={}, count={}, reason={}", deviceIdentification, transactions.size(), closeReason);
    }
}
