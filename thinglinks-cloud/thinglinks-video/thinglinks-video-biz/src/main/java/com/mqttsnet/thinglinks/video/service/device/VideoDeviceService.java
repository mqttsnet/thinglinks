package com.mqttsnet.thinglinks.video.service.device;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceExtendParams;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
import com.mqttsnet.thinglinks.video.entity.device.VideoDevice;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoDeviceSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoDeviceUpdateVO;

import java.util.List;

/**
 * 业务接口 - 统一设备表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
public interface VideoDeviceService extends SuperService<Long, VideoDevice> {

    /**
     * 根据设备标识查询设备
     *
     * @param deviceIdentification 设备标识
     * @return 设备结果VO
     */
    VideoDeviceResultVO getByDeviceIdentification(String deviceIdentification);

    /**
     * 保存设备信息
     *
     * @param saveVO 设备保存VO
     */
    void saveDeviceInfo(VideoDeviceSaveVO saveVO);

    /**
     * 更新设备信息
     *
     * @param updateVO 设备更新VO
     */
    void updateDeviceInfo(VideoDeviceUpdateVO updateVO);

    /**
     * 按需合并设备 {@code protocol_config} 字段（patch 语义）。
     * <p>
     * patch 中非 null 的字段会覆盖 DB 现有值，为 null 的字段保持原样不动。适合多个
     * 不同 handler（REGISTER / Catalog / DeviceInfo）各自写自己关心的字段、互不干扰。
     *
     * @param deviceIdentification 设备国标编号
     * @param patch                只包含要更新字段的 VideoDeviceProtocolConfig 实例
     */
    void patchProtocolConfig(String deviceIdentification, VideoDeviceProtocolConfig patch);

    /**
     * 按需合并设备 {@code extend_params} 字段（patch 语义）。
     * <p>
     * 内部会先 {@link VideoDeviceExtendParams#fromJson} 反序列化现值，调用
     * {@link VideoDeviceExtendParams#merge} 合并 patch，再 toJsonString 回写。
     *
     * @param deviceIdentification 设备国标编号
     * @param patch                只包含要更新字段的 VideoDeviceExtendParams 实例
     */
    void patchExtendParams(String deviceIdentification, VideoDeviceExtendParams patch);

    /**
     * 强制设备下线（更新状态 + 清理缓存）
     *
     * @param deviceIdentification 设备国标编号
     */
    void forceOffline(String deviceIdentification);

    /**
     * 统计在线设备数
     */
    long countOnline();

    /**
     * 统计离线设备数
     */
    long countOffline();


    /**
     * 统计设备总数
     */
    long countTotal();

    /**
     * 查询所有在线设备
     *
     * @return 在线设备列表
     */
    List<VideoDeviceResultVO> listOnlineDevices();

    /**
     * 删除设备及其所有通道（级联删除，纯业务层）。
     * 活跃通道检查由 Controller 层完成（需要协议层的 SsrcTransactionManager）。
     *
     * @param deviceIdentification 设备标识
     */
    void deleteDeviceWithChannels(String deviceIdentification);
}
