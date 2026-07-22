package com.mqttsnet.thinglinks.video.gb28181;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigCacheVO;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigHolder;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.config.gb28181.SipConfig;
import com.mqttsnet.thinglinks.video.service.device.VideoSipConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description:
 * 租户 SIP 配置提供者（核心）。
 * <p>
 * 只用一个全局 Hash（field=sipId）。
 * <ul>
 *   <li>入站: {@link #resolveBySipId(String)} — sipId → Hash → 得到 tenantId + 配置</li>
 *   <li>入站(兼容): {@link #resolveByFuzzyMatch(String, String)} — sipId 精确匹配失败时，回退到 SIP 域匹配</li>
 *   <li>出站: {@link #resolve()} — ThreadLocal 优先 → 否则从租户库查默认 sipId → Hash</li>
 * </ul>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantSipConfigProvider {

    private final SipConfig sipConfig;
    private final SipLayer sipLayer;
    private final VideoSipConfigService videoSipConfigService;
    /** SIP 配置缓存读写 + DB 溯源 + 模糊匹配统一走 helper，本类不再直接 hGet/hSet/hGetAll */
    private final VideoCacheDataHelper videoCacheDataHelper;

    /**
     * 获取当前租户的 SIP 参数（出站消息用）。
     * <p>
     * 1. 优先 ThreadLocal（入站 SIP 请求 handlerTenantId 时已 set）
     * 2. 否则从租户库查默认 sipId → 再查 Redis 全局 Hash
     */
    public TenantSipConfig resolve() {
        // 1. ThreadLocal 缓存
        TenantSipConfig cached = TenantSipConfigHolder.get();
        if (cached != null) {
            return cached;
        }
        // 2. 从租户库查默认配置的 sipId
        String defaultSipId = videoSipConfigService.getDefaultSipId();
        if (StrUtil.isBlank(defaultSipId)) {
            throw BizException.wrap("租户 SIP 配置未找到，请先在 SIP 配置页面完成配置并设为默认");
        }
        // 3. 用 sipId 查全局 Hash
        TenantSipConfigCacheVO cacheVO = resolveBySipId(defaultSipId)
                .orElseThrow(() -> BizException.wrap("SIP 配置缓存未找到 [" + defaultSipId + "]，请刷新缓存"));
        return TenantSipConfig.of(cacheVO, sipLayer.getMonitorIp(), sipConfig.getPort());
    }

    /**
     * 根据 sipId 精确查全局 Hash（入站路由 + 出站查配置通用）。
     * <p>cache miss 时由 {@code VideoCacheDataHelper} 自动从 DB 溯源回写，避免每次刷缓存都要手动操作。
     *
     * @param sipId SIP 服务器编号
     * @return 命中返 {@code Optional}; 缓存中无返 {@link Optional#empty()}
     */
    public Optional<TenantSipConfigCacheVO> resolveBySipId(String sipId) {
        return videoCacheDataHelper.getSipConfigBySipId(sipId);
    }

    /**
     * 模糊匹配：精确 sipId 查不到时，扫描全部配置进行匹配。
     * <p>
     * 匹配策略（优先级从高到低）：
     * 1. sipId 精确匹配（已在调用方完成）
     * 2. SIP 域匹配：设备编号前 10 位 = sipDomain
     * 3. 单配置直接使用：全局只有一个 SIP 配置时直接命中
     *
     * @param requestSipId         Request-URI 中提取的 SIP 服务器编号
     * @param deviceIdentification 设备国标编号（FROM header）
     * @return 匹配到的租户配置，未匹配返回 {@link Optional#empty()}
     */
    public Optional<TenantSipConfigCacheVO> resolveByFuzzyMatch(String requestSipId, String deviceIdentification) {
        return videoCacheDataHelper.resolveSipConfigByFuzzyMatch(requestSipId, deviceIdentification);
    }

    /**
     * 校验 sipId 全局唯一。
     */
    public boolean isSipIdExists(String sipId) {
        return resolveBySipId(sipId).isPresent();
    }

    /**
     * 校验"设备编号"与"平台 SIP 服务器编号"不能相同。
     * <p>这是 GB28181 接入最常见的踩坑点：摄像头出厂默认会把"SIP 服务器编号"和"设备编号"都填成同一个值
     * （比如 44010200492000000001），用户没改就直接接入。然后我方平台向设备发 INVITE 时
     * From URI 用户名 = 平台 sipId = 设备自己的 deviceId，设备 SIP 栈认为是"自己给自己发消息"，
     * 200 OK 之后立即 BYE 拆 dialog，前端表现就是一直转圈拉不到流。
     * <p>校验由调用方在 play / playback / download 等会下发 INVITE 的入口主动调用一次，
     * 命中冲突直接抛 BizException 给前端清晰的错误信息，避免无谓地走完 SIP 协商再失败。
     *
     * @param deviceIdentification 目标设备国标编号（device_identification）
     * @throws BizException 当设备编号与当前租户默认 sipId 相同时抛出
     */
    public void assertPlatformIdNotConflictWithDevice(String deviceIdentification) {
        if (StrUtil.isBlank(deviceIdentification)) {
            return;
        }
        TenantSipConfig tenantConfig;
        try {
            tenantConfig = resolve();
        } catch (Exception e) {
            // SIP 配置缺失等错误让原本的流程继续抛它自己的 BizException，本校验不掩盖
            return;
        }
        if (tenantConfig != null && deviceIdentification.equals(tenantConfig.getSipId())) {
            throw BizException.wrap("设备编号 [" + deviceIdentification + "] 与平台「SIP 服务器编号」相同，"
                    + "GB28181 不允许这种配置（设备会在 INVITE 200 OK 后立即 BYE 导致拉不到流）。"
                    + "请进入设备 GB28181 配置页面，把「设备编号」改成与「SIP 服务器编号」不同的 20 位国标编号。");
        }
    }
}
