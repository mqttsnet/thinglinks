package com.mqttsnet.thinglinks.video.cache;

import java.util.Optional;

/**
 * @author mqttsnet
 */
public interface VideoCacheDataHelper {


    /**
     * 将device信息写入redis
     *
     * @param deviceCacheVO 设备信息
     */
    void setDeviceInfo(VideoDeviceCacheVO deviceCacheVO);

    /**
     * 获取缓存中的设备信息
     *
     * @param deviceIdentification 设备标识
     * @return {@link VideoDeviceCacheVO}  设备缓存VO
     */
    VideoDeviceCacheVO getDeviceInfo(String deviceIdentification);

    /**
     * 删除设备缓存信息
     *
     * @param deviceIdentification 设备国标编号
     */
    void removeDeviceInfo(String deviceIdentification);

    /**
     * 将通道信息写入 Redis
     *
     * @param channelCacheVO 通道信息
     */
    void setChannelInfo(VideoChannelCacheVO channelCacheVO);

    /**
     * 获取缓存中的通道信息
     *
     * @param channelIdentification 通道标识
     * @return {@link VideoChannelCacheVO} 通道缓存 VO
     */
    VideoChannelCacheVO getChannelInfo(String channelIdentification);

    /**
     * 删除通道缓存信息
     *
     * @param channelIdentification 通道国标编号
     */
    void removeChannelInfo(String channelIdentification);

    /**
     * 按 sipId 获取租户 SIP 配置（cache-aside）。
     * <p>cache miss 时自动调 manager 溯源 DB 并回写全局 Hash 缓存——避免上层每次刷缓存都要手动操作。
     *
     * @param sipId SIP 服务器编号
     * @return SIP 配置 cache VO；DB 也无该 sipId 时返回 {@link Optional#empty()}
     */
    Optional<TenantSipConfigCacheVO> getSipConfigBySipId(String sipId);

    /**
     * sipId 精确匹配失败时的回退路由：
     * <ol>
     *   <li>SIP 域匹配：设备编号前 10 位 = sipDomain</li>
     *   <li>单配置直接命中：全局只有一个 SIP 配置时返回它</li>
     * </ol>
     * 这种"扫全部 + 业务规则筛选"属于"复杂存储结构 + 数据组装"场景，封装到 helper 内供协议层调用。
     *
     * @param requestSipId         入站请求中提取的 sipId（仅用于日志归因）
     * @param deviceIdentification FROM header 设备国标编号
     * @return 匹配到的租户配置；未匹配返回 {@link Optional#empty()}
     */
    Optional<TenantSipConfigCacheVO> resolveSipConfigByFuzzyMatch(String requestSipId, String deviceIdentification);
}
