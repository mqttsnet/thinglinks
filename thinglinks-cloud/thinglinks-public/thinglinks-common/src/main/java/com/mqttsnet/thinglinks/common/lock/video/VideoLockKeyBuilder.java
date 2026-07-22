package com.mqttsnet.thinglinks.common.lock.video;

import java.time.Duration;

import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.lock.LockKeyTable;

/**
 * Video 模块分布式锁 Key 构建器
 * <p>
 * 参照 {@link com.mqttsnet.thinglinks.common.lock.link.LinkLockKeyBuilder} 实现。
 * 使用 {@code DistributedLock.tryLockAndRun()} 进行分布式锁操作。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-05
 */
public class VideoLockKeyBuilder implements CacheKeyBuilder {

    private String table;
    private String field = "id";
    private Duration expire = Duration.ofSeconds(30);

    private VideoLockKeyBuilder(String table) {
        this.table = table;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 录像计划执行锁
     * <p>防止同一计划被多个实例/线程同时执行</p>
     *
     * @param planId 计划ID
     * @return CacheKey
     */
    public static CacheKey forRecordPlanExecute(Long planId) {
        return new VideoLockKeyBuilder("recordPlan")
                .field("execute")
                .expire(Duration.ofSeconds(60))
                .key(String.valueOf(planId));
    }

    /**
     * 录像文件保存去重锁
     * <p>防止同一录像文件被重复保存（on_record_mp4 回调重试场景）</p>
     *
     * @param fileName 文件名
     * @return CacheKey
     */
    public static CacheKey forRecordFileSave(String fileName) {
        return new VideoLockKeyBuilder("recordFile")
                .field("save")
                .expire(Duration.ofSeconds(30))
                .key(fileName);
    }

    /**
     * SSRC 池孤儿对账锁
     * <p>防止集群多节点同时执行对账导致重复释放</p>
     *
     * @param mediaIdentification 流媒体服务器标识
     * @return CacheKey
     */
    public static CacheKey forSsrcReconcile(String mediaIdentification) {
        return new VideoLockKeyBuilder("ssrcPool")
                .field("reconcile")
                .expire(Duration.ofSeconds(90))
                .key(mediaIdentification);
    }

    /**
     * SSRC 池管理接口锁
     * <p>防止 reset / reconcile 管理操作与定时任务并发冲突</p>
     *
     * @param mediaIdentification 流媒体服务器标识
     * @return CacheKey
     */
    public static CacheKey forSsrcAdmin(String mediaIdentification) {
        return new VideoLockKeyBuilder("ssrcPool")
                .field("admin")
                .expire(Duration.ofSeconds(30))
                .key(mediaIdentification);
    }

    /**
     * 通道级播放互斥锁。
     * <p>防止同一 (设备, 通道) 上同时跑多个 play 流程：用户主动 Play 与
     * {@code StreamAutoRecoveryListener} 自动重试若并行进入 PlayService.play，
     * 第二个会重新分配 SSRC/streamId，撞上 ZLM "stream already exists"，
     * 失败回滚时还会把第一个已开的 RTP 接收服务器一并关掉，把用户的画面也一起干掉。
     * <p>持锁期间第二个 play 入口排队，进来后能命中第一个刚建好的 SsrcTransaction 直接复用。
     * 过期 25s = INVITE 等待 10s + 资源分配 + ZLM Hook，留足缓冲。
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     */
    public static CacheKey forChannelPlay(String deviceIdentification, String channelIdentification) {
        return new VideoLockKeyBuilder("playChannel")
                .field("invite")
                .expire(Duration.ofSeconds(25))
                .key(deviceIdentification + ":" + channelIdentification);
    }

    // ==================== 链式方法 ====================

    public VideoLockKeyBuilder field(String field) {
        this.field = field;
        return this;
    }

    public VideoLockKeyBuilder expire(Duration expire) {
        this.expire = expire;
        return this;
    }

    // ==================== CacheKeyBuilder 接口实现 ====================

    @Override
    public VideoLockKeyBuilder setTenantId(Long tenantId) {
        // 分布式锁不需要租户隔离
        return this;
    }

    @Override
    public String getTenant() {
        return null;
    }

    @Override
    public String getTable() {
        return LockKeyTable.LOCK_PREFIX + ":" + table;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.VIDEO;
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public ValueType getValueType() {
        return null;
    }

    @Override
    public Duration getExpire() {
        return expire;
    }
}
