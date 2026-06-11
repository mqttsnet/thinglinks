package com.mqttsnet.thinglinks.video.service.stream;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.cache.lock.DistributedLock;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.common.lock.video.VideoLockKeyBuilder;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.SsrcTransaction;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionTypeEnum;
import com.mqttsnet.thinglinks.video.enumeration.stream.SsrcPrefixEnum;
import com.mqttsnet.thinglinks.video.enumeration.stream.StreamModeEnum;
import com.mqttsnet.thinglinks.video.gb28181.cmd.PlayCommander;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.StreamEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.PlayFailedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.PlayRequestedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.StreamClosedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.subscribe.SipSubscribe;
import com.mqttsnet.thinglinks.video.gb28181.session.InviteSessionService;
import com.mqttsnet.thinglinks.video.gb28181.session.RtpPortService;
import com.mqttsnet.thinglinks.video.gb28181.session.SsrcPoolService;
import com.mqttsnet.thinglinks.video.gb28181.session.StreamInfoService;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.media.common.MediaNodeService;
import com.mqttsnet.thinglinks.video.media.common.MediaNodeServiceFactory;
import com.mqttsnet.thinglinks.video.service.device.VideoChannelService;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import javax.sip.ResponseEvent;
import javax.sip.SipFactory;
import javax.sip.header.CallIdHeader;
import javax.sip.header.HeaderFactory;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Description:
 * 播放核心业务服务。
 * 协调 SSRC 分配、RTP 端口分配、SDP 构建、INVITE 发送、流信息缓存等，
 * 实现完整的实时视频播放流程。
 * <p>
 * 播放状态机：IDLE → INVITING → STREAMING → CLOSING → CLOSED
 * 状态变更通过 Event 驱动通知下游组件。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class PlayService {

    private final VideoDeviceService videoDeviceService;
    private final VideoChannelService videoChannelService;
    private final VideoMediaServerService videoMediaServerService;
    private final MediaNodeServiceFactory mediaNodeServiceFactory;
    private final SsrcPoolService ssrcPoolService;
    private final RtpPortService rtpPortService;
    private final InviteSessionService inviteSessionService;
    private final StreamInfoService streamInfoService;
    private final PlayCommander playCommander;
    private final SIPSender sipSender;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final StreamEventPublisher streamEventPublisher;
    private final DistributedLock distributedLock;

    /**
     * 默认应用名
     */
    private static final String DEFAULT_APP = "rtp";

    /**
     * INVITE 响应等待超时（毫秒）。GB28181 设备响应通常 1~3s，慢设备可能到 8s，预留到 10s。
     * 这个值比 {@code sipConfig.timeout}（默认 5s）更宽松，因为 INVITE 涉及设备侧开流。
     */
    private static final long INVITE_RESPONSE_TIMEOUT_MS = 10_000L;

    /**
     * 发起实时点播。
     * <p>外层加 (设备, 通道) 维度的分布式互斥锁，把"用户主动 Play"与
     * {@code StreamAutoRecoveryListener} 自动重试串成单线，
     * 防止两个并行 play 重复分配 SSRC/streamId 撞到 ZLM "stream already exists"，
     * 失败回滚还会把已经成功的会话 RTP 接收服务器一起拆掉、把用户画面也断了。
     * 锁内的第二个调用会直接命中第一个刚建好的 SsrcTransaction 走复用分支。
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @return 流信息（包含多协议 URL），流尚未就绪时仅包含基本信息
     */
    public StreamInfo play(String deviceIdentification, String channelIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        // 用底层 lock / releaseLock 而不是 tryLockAndRun(Supplier)：
        // 后者会把 action 内部抛出的 BizException 一律吞成 "LOCK_FAILED"（参见 RedisDistributedLock 实现），
        // 前端再也拿不到"设备离线 / 通道不存在 / INVITE 超时"等真实原因。
        CacheKey lockKey = VideoLockKeyBuilder.forChannelPlay(deviceIdentification, channelIdentification);
        long expireMs = lockKey.getExpire().toMillis();
        // waitTime≈3s：用户两次点击通常 < 1s，恢复链路退避 5s，排队 3s 不会让前端长转。
        // 内部按 50ms 轮询，3000/50 = 60 次重试。
        int retryTimes = 60;
        long sleepMillis = 50L;
        boolean locked = distributedLock.lock(lockKey.getKey(), expireMs, retryTimes, sleepMillis);
        if (!locked) {
            throw BizException.wrap("通道正在建立中，请稍后重试: deviceIdentification=" + deviceIdentification
                    + ", channelIdentification=" + channelIdentification);
        }
        try {
            return doPlay(deviceIdentification, channelIdentification);
        } finally {
            try {
                distributedLock.releaseLock(lockKey.getKey());
            } catch (Exception e) {
                log.warn("释放通道播放锁失败（已过期或未持有）: key={}, error={}", lockKey.getKey(), e.getMessage());
            }
        }
    }

    /**
     * 实际的点播流程，由 {@link #play(String, String)} 在分布式锁保护下调用。
     */
    private StreamInfo doPlay(String deviceIdentification, String channelIdentification) {
        // 1. 每次都从最新 DB 拿设备
        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) {
            throw BizException.wrap("设备不存在: " + deviceIdentification);
        }
        if (!Boolean.TRUE.equals(device.getOnlineStatus())) {
            throw BizException.wrap("设备离线，无法播放: " + deviceIdentification);
        }

        // 1.5 拦下"设备编号 == 平台 SIP 服务器编号"的自冲突配置：直接发 INVITE 设备会 200 OK 后立刻 BYE，
        //     表现是前端一直转圈。这里提前抛清晰错误，引导用户去设备端把「设备编号」改成与「SIP 服务器编号」不同的值。
        tenantSipConfigProvider.assertPlatformIdNotConflictWithDevice(deviceIdentification);

        // 2. 复用判定只看真实 SIP 会话（SsrcTransaction）：
        //    - 仍有进行中的会话，且会话归属的 mediaIdentification 等于设备当前绑定值 → 复用，按最新 mediaServer 重算 URL 返回
        //    - 仍有进行中的会话，但归属的 mediaIdentification 与设备当前不一致（UI 改了 ZLM）→ stop 释放旧资源后走完整重建
        //    - 没有会话 → 直接走完整重建
        SsrcTransaction activeTx = inviteSessionService.getAllTransactions(deviceIdentification).stream()
                .filter(t -> InviteSessionTypeEnum.PLAY.equals(t.getType()))
                .filter(t -> channelIdentification.equals(t.getChannelIdentification()))
                .findFirst()
                .orElse(null);
        if (activeTx != null) {
            String currentMediaId = device.getMediaIdentification();
            if (activeTx.getMediaIdentification() != null && activeTx.getMediaIdentification().equals(currentMediaId)) {
                // 多分屏 / 多观看者直接复用同一组 URL：ZLM 自己用 totalReaderCount 跟踪订阅者，
                // 多个 player 拉同一个 stream URL 就是多分屏，后端不用额外做引用计数。
                StreamInfo refreshed = streamInfoService.buildAndCacheStreamInfoByMediaId(
                        currentMediaId,
                        deviceIdentification,
                        channelIdentification,
                        InviteSessionTypeEnum.PLAY.getValue(),
                        "rtp",
                        activeTx.getStream(),
                        activeTx.getCallId());
                log.info("通道已在播放中（按最新 ZLM 配置刷新流地址）: deviceIdentification={}, channelIdentification={}, mediaIdentification={}",
                        deviceIdentification, channelIdentification, currentMediaId);
                return refreshed;
            }
            log.warn("[流复用失效] 旧会话归属 mediaIdentification={}，设备当前已切换为 {}，stop 释放后重建: deviceIdentification={}, channelIdentification={}",
                    activeTx.getMediaIdentification(), currentMediaId, deviceIdentification, channelIdentification);
            try {
                // 直接调内部 doStop，跳过 stopInternal 重新拿锁 —— 外层 play 已经持有同一把 (设备, 通道) 锁，
                // 再 lock 同一 key 会自我阻塞 3s 然后失败。
                // sendBye=true：旧会话还在设备侧（dialog 仍 alive），需要正经发 BYE 让设备停 RTP。
                doStop(deviceIdentification, channelIdentification, "切换 ZLM，释放旧会话", true);
            } catch (Exception e) {
                log.warn("[流复用失效] 旧会话清理失败（忽略，继续重建）: {}", e.getMessage());
            }
        }

        // 2.5 校验通道存在（防止给设备发 INVITE 一个不属于它的 channelId，设备会 4xx/直接忽略）
        VideoChannelResultVO channel = videoChannelService.getByChannelIdentification(channelIdentification);
        if (channel == null) {
            throw BizException.wrap("通道不存在: " + channelIdentification
                    + "（请先完成设备 Catalog 同步，或确认通道编号正确）");
        }
        if (channel.getDeviceIdentification() != null
                && !channel.getDeviceIdentification().equals(deviceIdentification)) {
            throw BizException.wrap("通道 " + channelIdentification + " 不属于设备 " + deviceIdentification);
        }

        // 3. 获取流媒体服务器
        VideoMediaServerResultDTO mediaServer = getMediaServer(device.getMediaIdentification());

        String mediaIdentification = mediaServer.getMediaIdentification();
        String ssrc = null;
        String streamId = null;
        int rtpPort = -1;
        // 标记 RTP 接收服务器是否在本次 play 中真正被本会话创建成功。
        // 避免失败回滚时把别人（同 streamId 但属于另一并发 play）刚建好的 RTP 服务器误关掉。
        boolean rtpServerCreated = false;

        try {
            // 3.5 懒初始化池（Phase 2 兜底）：Redis 被清 / 首次启动 / 新流媒体服务器接入时
            //     原本池子不存在，之前会直接抛"SSRC池已耗尽"，用户困惑。这里按需补建。
            //     RTP 端口池支持默认范围兜底（mediaServer.rtpPortRange 为空时用 30000,30500）。
            ssrcPoolService.ensurePoolInitialized(mediaIdentification);
            rtpPortService.ensurePoolInitialized(mediaIdentification, mediaServer.getRtpPortRange());

            // 4. 分配 SSRC
            ssrc = ssrcPoolService.allocateSsrc(mediaIdentification, SsrcPrefixEnum.PLAY, deviceIdentification, channelIdentification);

            // 5. 分配 RTP 端口
            rtpPort = rtpPortService.allocatePort(mediaIdentification, deviceIdentification, channelIdentification);

            // 6. 生成流 ID
            streamId = generateStreamId(ssrc);

            // 7. 获取传输协议和流模式
            String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";
            StreamModeEnum streamMode = StreamModeEnum.fromValue(
                    StrUtil.isNotBlank(device.getStreamMode()) ? device.getStreamMode() : "UDP"
            ).orElse(StreamModeEnum.UDP);

            // 8. 创建 RTP 接收服务器
            MediaNodeService mediaNodeService = mediaNodeServiceFactory.getServiceByType(mediaServer.getType());
            var mediaServerEntity = convertToEntity(mediaServer);
            int tcpMode = switch (streamMode) {
                case TCP_PASSIVE -> 1;
                case TCP_ACTIVE -> 2;
                default -> 0;
            };
            int actualPort = mediaNodeService.createRtpServer(mediaServerEntity, streamId, ssrc, rtpPort, false, false, tcpMode);
            if (actualPort == -1) {
                throw BizException.wrap("创建RTP接收服务器失败: mediaIdentification=" + mediaIdentification);
            }
            rtpServerCreated = true;

            // 9. 生成 CallId
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();
            CallIdHeader callId = sipSender.getNewCallIdHeader(tenantConfig.getEffectiveHost(), transport);

            // 10. 发布播放请求事件
            streamEventPublisher.publishPlayRequestedEvent(PlayRequestedEventSource.builder()
                    .deviceIdentification(deviceIdentification)
                    .channelIdentification(channelIdentification)
                    .mediaIdentification(mediaIdentification)
                    .ssrc(ssrc)
                    .rtpPort(actualPort)
                    .build());

            // 11. 发送 INVITE 请求（异步，内部先订阅再发送，避免响应竞态）
            //     SDP 的 c= IP：sdp_host 优先，否则用 host。运维配什么就用什么。
            String sdpIp = StrUtil.isNotBlank(mediaServer.getSdpHost()) ? mediaServer.getSdpHost() : mediaServer.getHost();
            CompletableFuture<SipSubscribe.EventResult<?>> inviteFuture = playCommander.playInvite(
                    deviceIdentification, channelIdentification,
                    device.getHost(), device.getPort(),
                    transport, streamMode,
                    sdpIp, actualPort, ssrc, callId, INVITE_RESPONSE_TIMEOUT_MS);

            // 12. 等待设备 200 OK；非 2xx / 超时 / 异常一律视为失败，走外层 catch 清理资源
            SipSubscribe.EventResult<?> inviteResult = awaitInviteResponse(inviteFuture, deviceIdentification, channelIdentification);

            // 13. 从 INVITE 2xx 响应提取 Dialog 状态（fromTag / toTag / cseq），后续 BYE/INFO/re-INVITE
            //     必须在同一 Dialog 内用这些 tag + 递增 CSeq 发送，否则设备以 481 拒绝。
            //     SIP 响应头按 RFC 强制必选，直接取值，缺失即抛 NPE 暴露问题。
            ResponseEvent responseEvent = (ResponseEvent) inviteResult.event;
            SIPResponse response = (SIPResponse) responseEvent.getResponse();
            String fromTag = response.getFromHeader().getTag();
            String toTag = response.getToHeader().getTag();
            long inviteCseq = response.getCSeqHeader().getSeqNumber();

            // 14. 创建会话事务（只有设备确认接受才落地，否则无 transaction 残留）
            SsrcTransaction transaction = new SsrcTransaction();
            transaction.setDeviceIdentification(deviceIdentification);
            transaction.setChannelIdentification(channelIdentification);
            transaction.setCallId(callId.getCallId());
            transaction.setApp(DEFAULT_APP);
            transaction.setStream(streamId);
            transaction.setMediaIdentification(mediaIdentification);
            transaction.setSsrc(ssrc);
            transaction.setRtpPort(actualPort); // 闭合端口释放路径：closeSession 将读取此字段回收端口
            transaction.setType(InviteSessionTypeEnum.PLAY);
            transaction.setFromTag(fromTag);
            transaction.setToTag(toTag);
            transaction.setCseq(inviteCseq);
            inviteSessionService.createSession(transaction);

            // 14. 构建并缓存流信息
            StreamInfo streamInfo = streamInfoService.buildAndCacheStreamInfo(
                    mediaServer, deviceIdentification, channelIdentification,
                    InviteSessionTypeEnum.PLAY.getValue(),
                    DEFAULT_APP, streamId, callId.getCallId());

            log.info("实时点播建立成功: deviceIdentification={}, channelIdentification={}, ssrc={}, streamId={}, port={}",
                    deviceIdentification, channelIdentification, ssrc, streamId, actualPort);

            return streamInfo;

        } catch (Exception e) {
            // 异常时仅释放本次 play 真正成功分配的资源：
            // 早期版本只要 streamId 非空就盲调 closeRtpServer，会把另一路并发 play 刚成功开起来的同 streamId
            // RTP 接收服务器一并关掉（streamId = ssrc，跨租户/并发情况下可能撞同名），把别人的画面也搞断。
            // 现在只在 rtpServerCreated=true（本会话亲自 createRtpServer 成功过）时才回收。
            if (rtpServerCreated && StrUtil.isNotBlank(streamId)) {
                try {
                    var mediaNodeService = mediaNodeServiceFactory.getServiceByType(mediaServer.getType());
                    mediaNodeService.closeRtpServer(convertToEntity(mediaServer), streamId);
                } catch (Exception ex) {
                    log.error("关闭RTP服务器失败: streamId={}, error={}", streamId, ex.getMessage());
                }
            }
            if (StrUtil.isNotBlank(ssrc)) {
                try {
                    ssrcPoolService.releaseSsrc(mediaIdentification, ssrc);
                } catch (Exception ex) {
                    log.error("释放SSRC失败: {}", ex.getMessage());
                }
            }
            if (rtpPort > 0) {
                try {
                    rtpPortService.releasePort(mediaIdentification, rtpPort);
                } catch (Exception ex) {
                    log.error("释放RTP端口失败: {}", ex.getMessage());
                }
            }

            // 发布播放失败事件
            streamEventPublisher.publishPlayFailedEvent(PlayFailedEventSource.builder()
                    .deviceIdentification(deviceIdentification)
                    .channelIdentification(channelIdentification)
                    .mediaIdentification(mediaIdentification)
                    .failureReason("INVITE发送失败")
                    .errorMessage(e.getMessage())
                    .build());

            if (e instanceof BizException) {
                throw e;
            }
            throw BizException.wrap("发起实时点播失败: " + e.getMessage());
        }
    }

    /**
     * 同步等待 INVITE 响应，2xx 正常返回 {@link SipSubscribe.EventResult}（供上层提取 Dialog tags），
     * 其他分支统一抛 {@link BizException}。
     * <ul>
     *   <li>2xx → 返回 EventResult</li>
     *   <li>4xx/5xx → 抛 BizException（带设备返回码和原因，便于前端展示）</li>
     *   <li>超时 → 抛 BizException（提示"设备未应答"）</li>
     *   <li>执行异常 → 抛 BizException</li>
     * </ul>
     * 任何非 2xx 分支抛出后，外层 try-catch 会统一回滚 SSRC / RTP 端口 / RTP 接收服务器。
     */
    private SipSubscribe.EventResult<?> awaitInviteResponse(CompletableFuture<SipSubscribe.EventResult<?>> inviteFuture,
                                                            String deviceIdentification, String channelIdentification) {
        SipSubscribe.EventResult<?> result;
        try {
            // future 内部已带 orTimeout；这里再留 1s buffer 防死等
            result = inviteFuture.get(INVITE_RESPONSE_TIMEOUT_MS + 1_000L, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw BizException.wrap("设备未应答 INVITE（超时 " + INVITE_RESPONSE_TIMEOUT_MS + "ms），请检查设备是否在线或网络是否通畅");
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw BizException.wrap("等待 INVITE 响应异常: " + e.getMessage());
        }

        if (result == null) {
            throw BizException.wrap("设备未应答 INVITE");
        }
        if (result.type == SipSubscribe.EventResultType.timeout) {
            throw BizException.wrap("设备未应答 INVITE（SIP 层超时）");
        }
        int statusCode = result.statusCode;
        if (statusCode >= 200 && statusCode < 300) {
            log.info("[INVITE响应] 设备: {}, 通道: {}, 状态码: {} (成功)",
                    deviceIdentification, channelIdentification, statusCode);
            return result;
        }
        // 4xx/5xx：带具体状态码和消息，便于前端区分"设备忙"/"鉴权失败"/"通道不存在"等
        String msg = StrUtil.isNotBlank(result.msg) ? result.msg : "无响应描述";
        throw BizException.wrap("设备拒绝 INVITE: statusCode=" + statusCode + ", reason=" + msg);
    }

    /**
     * 停止播放。
     * <p>加 (设备, 通道) 维度的分布式锁，和 {@link #play(String, String)} 共用同一把 key，
     * 防止"用户播 / 自动恢复 / 用户停"三方并发导致脏状态（INVITE 还没回响应就被 BYE 拆掉、
     * SsrcTransaction 半建半删等）。
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     */
    public void stop(String deviceIdentification, String channelIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        CacheKey lockKey = VideoLockKeyBuilder.forChannelPlay(deviceIdentification, channelIdentification);
        boolean locked = distributedLock.lock(lockKey.getKey(), lockKey.getExpire().toMillis(), 60, 50L);
        if (!locked) {
            throw BizException.wrap("通道正忙，停止操作请稍后重试: deviceIdentification=" + deviceIdentification
                    + ", channelIdentification=" + channelIdentification);
        }
        try {
            doStop(deviceIdentification, channelIdentification, "用户主动停止", true);
        } finally {
            try {
                distributedLock.releaseLock(lockKey.getKey());
            } catch (Exception e) {
                log.warn("释放通道播放锁失败（已过期或未持有）: key={}, error={}", lockKey.getKey(), e.getMessage());
            }
        }
    }

    /**
     * 处理设备主动 BYE：设备已经先把 dialog 拆了，平台侧只补做"关 ZLM RTP server / 释放 SSRC + 端口
     * / 删 SsrcTransaction / 清 StreamInfo 缓存"等清理；**不再回发 BYE 出去**（互相 BYE 没意义）。
     * <p>不做这一步的话，下次再 play 走完整路径会重新分配同一个 SSRC（刚释放，前缀匹配第一个空位），
     * 但 ZLM 那边旧的 RTP 接收服务器还在，{@code openRtpServer} 撞 "stream already exists"，
     * 前端就一直转圈拉不到流。
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @param callId                设备 BYE 携带的 Call-ID
     */
    public void handleRemoteBye(String deviceIdentification, String channelIdentification, String callId) {
        if (StrUtil.isBlank(deviceIdentification) || StrUtil.isBlank(channelIdentification)) {
            log.warn("[设备BYE] 缺少 deviceIdentification 或 channelIdentification，跳过清理: deviceIdentification={}, channelIdentification={}, callId={}",
                    deviceIdentification, channelIdentification, callId);
            return;
        }

        CacheKey lockKey = VideoLockKeyBuilder.forChannelPlay(deviceIdentification, channelIdentification);
        boolean locked = distributedLock.lock(lockKey.getKey(), lockKey.getExpire().toMillis(), 60, 50L);
        if (!locked) {
            // 设备 BYE 不能阻塞太久 —— 拿不到锁也得让出来给本端清理，先记日志兜底交给 ZLM hook。
            log.warn("[设备BYE] 抢锁失败，依赖 ZLM on_stream_none_reader 兜底清理: deviceIdentification={}, channelIdentification={}, callId={}",
                    deviceIdentification, channelIdentification, callId);
            return;
        }
        try {
            doStop(deviceIdentification, channelIdentification, "BYE from device", false);
        } finally {
            try {
                distributedLock.releaseLock(lockKey.getKey());
            } catch (Exception e) {
                log.warn("释放通道播放锁失败（已过期或未持有）: key={}, error={}", lockKey.getKey(), e.getMessage());
            }
        }
    }

    /**
     * 实际的拆流逻辑，由 {@link #stop} / {@link #handleRemoteBye} 持锁后调用，也由 {@link #doPlay}
     * 在持锁的"切 ZLM"分支直接调用（避免 doPlay 已经持锁时再走 {@code stop} 自我阻塞同一把 key）。
     *
     * @param sendBye true=本端主动停止，要发 BYE 通知设备；false=设备已经发过 BYE 来了，本端只做清理
     */
    private void doStop(String deviceIdentification, String channelIdentification, String reason, boolean sendBye) {
        // 查找播放会话：必须按 channelId 过滤，否则多通道同设备并发时会误停其他通道
        List<SsrcTransaction> transactions = inviteSessionService.getAllTransactions(deviceIdentification);
        SsrcTransaction playTransaction = transactions.stream()
                .filter(t -> InviteSessionTypeEnum.PLAY.equals(t.getType()))
                .filter(t -> channelIdentification.equals(t.getChannelIdentification()))
                .findFirst()
                .orElse(null);

        if (playTransaction == null) {
            log.warn("未找到播放会话: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
            // 清理可能残留的流信息缓存
            streamInfoService.removeStreamInfo(deviceIdentification, channelIdentification, InviteSessionTypeEnum.PLAY.getValue());
            return;
        }

        if (sendBye) {
            try {
                // 1. 发送 BYE：必须用**原 INVITE 的 CallId + From-tag + To-tag** 且 CSeq 递增，
                //    否则设备以 481 Call/Transaction Does Not Exist 拒绝 BYE，RTP 流在设备端不关闭。
                VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
                if (device != null) {
                    String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";
                    CallIdHeader callId = buildCallIdHeader(playTransaction.getCallId());
                    long nextCseq = playTransaction.getCseq() + 1L;
                    playCommander.bye(deviceIdentification, channelIdentification,
                            device.getHost(), device.getPort(), transport, callId,
                            playTransaction.getFromTag(), playTransaction.getToTag(), nextCseq);
                }
            } catch (Exception e) {
                // BYE 失败不阻塞本地资源清理 —— 设备侧可能会自行因无 RTP 活动超时关闭
                log.error("发送BYE失败（本地仍继续清理资源）: deviceIdentification={}, channelIdentification={}, error={}",
                        deviceIdentification, channelIdentification, e.getMessage());
            }
        }

        // 2. 关闭 RTP 服务器
        if (StrUtil.isNotBlank(playTransaction.getMediaIdentification()) && StrUtil.isNotBlank(playTransaction.getStream())) {
            try {
                VideoMediaServerResultDTO mediaServer = videoMediaServerService.getVideoMediaServerResultDTO(playTransaction.getMediaIdentification());
                if (mediaServer != null) {
                    MediaNodeService mediaNodeService = mediaNodeServiceFactory.getServiceByType(mediaServer.getType());
                    mediaNodeService.closeRtpServer(convertToEntity(mediaServer), playTransaction.getStream());
                }
            } catch (Exception e) {
                log.error("关闭RTP服务器失败: {}", e.getMessage());
            }
        }

        // 3. 关闭会话（内部统一释放 SSRC + RTP 端口 + 删除 SsrcTransaction）
        inviteSessionService.closeSession(deviceIdentification, playTransaction.getCallId(), reason);

        // 4. 清理流信息缓存
        streamInfoService.removeStreamInfo(deviceIdentification, channelIdentification, InviteSessionTypeEnum.PLAY.getValue());

        // 5. 发布流关闭事件（userInitiated=true：用户主动 / ZLM 确认无观看者，都不需要自动恢复重播）
        streamEventPublisher.publishStreamClosedEvent(StreamClosedEventSource.builder()
                .deviceIdentification(deviceIdentification)
                .channelIdentification(channelIdentification)
                .mediaIdentification(playTransaction.getMediaIdentification())
                .app(playTransaction.getApp())
                .stream(playTransaction.getStream())
                .callId(playTransaction.getCallId())
                .closeReason(reason)
                .userInitiated(true)
                .build());

        log.info("停止播放: deviceIdentification={}, channelIdentification={}, reason={}",
                deviceIdentification, channelIdentification, reason);
    }

    /**
     * 从字符串 callId 构造 JAIN-SIP {@link CallIdHeader}。
     * BYE/INFO 等 in-dialog 请求需要复用原 INVITE 的 callId，但我们只在 Redis 里存了 String，
     * 必须通过 HeaderFactory 手工构造 Header 对象再传给 SipMessageBuilder。
     */
    private CallIdHeader buildCallIdHeader(String callIdStr) throws Exception {
        HeaderFactory headerFactory = SipFactory.getInstance().createHeaderFactory();
        return headerFactory.createCallIdHeader(callIdStr);
    }


    /**
     * 获取流信息
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @return 流信息
     */
    public Optional<StreamInfo> getStreamInfo(String deviceIdentification, String channelIdentification) {
        return streamInfoService.getStreamInfo(deviceIdentification, channelIdentification, InviteSessionTypeEnum.PLAY.getValue());
    }

    /**
     * 获取流媒体服务器
     */
    private VideoMediaServerResultDTO getMediaServer(String mediaIdentification) {
        VideoMediaServerResultDTO mediaServer;
        if (StrUtil.isNotBlank(mediaIdentification)) {
            mediaServer = videoMediaServerService.getVideoMediaServerResultDTO(mediaIdentification);
        } else {
            // 使用默认流媒体服务器（获取列表中第一个在线的）
            var servers = videoMediaServerService.getVideoMediaServerResultDTOList(null);
            mediaServer = servers.stream()
                    .filter(s -> Boolean.TRUE.equals(s.getOnlineStatus()))
                    .findFirst()
                    .orElse(null);
        }

        if (mediaServer == null) {
            throw BizException.wrap("无可用的流媒体服务器");
        }
        if (!Boolean.TRUE.equals(mediaServer.getOnlineStatus())) {
            throw BizException.wrap("流媒体服务器离线: " + mediaServer.getMediaIdentification());
        }
        return mediaServer;
    }

    /**
     * 根据 SSRC 生成流 ID
     * 格式：ssrc 值作为流 ID，去除前导零
     */
    private String generateStreamId(String ssrc) {
        return ssrc;
    }

    /** DTO → Entity 转换，抽到 {@link VideoMediaServerConverter} 去重。 */
    private com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer convertToEntity(VideoMediaServerResultDTO dto) {
        return VideoMediaServerConverter.toEntity(dto);
    }
}
