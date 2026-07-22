package com.mqttsnet.thinglinks.video.isup.cmd;

import com.mqttsnet.thinglinks.video.isup.sdk.IsupSdkAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Description:
 * ISUP PTZ 云台命令执行器。
 * 封装方向控制、变倍控制、预置位操作等 SDK 调用逻辑。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IsupPtzCommander {

    private final IsupSdkAdapter isupSdkAdapter;

    /**
     * PTZ 方向控制常量
     */
    private static final int PTZ_UP = 21;
    private static final int PTZ_DOWN = 22;
    private static final int PTZ_LEFT = 23;
    private static final int PTZ_RIGHT = 24;
    private static final int PTZ_UP_LEFT = 25;
    private static final int PTZ_UP_RIGHT = 26;
    private static final int PTZ_DOWN_LEFT = 27;
    private static final int PTZ_DOWN_RIGHT = 28;
    private static final int PTZ_ZOOM_IN = 11;
    private static final int PTZ_ZOOM_OUT = 12;

    /**
     * 预置位操作常量
     */
    private static final int PRESET_SET = 8;
    private static final int PRESET_CALL = 39;
    private static final int PRESET_DELETE = 10;

    /**
     * 方向控制。
     *
     * @param userId    登录句柄
     * @param channelNo 通道号
     * @param direction 方向（UP/DOWN/LEFT/RIGHT/UP_LEFT/UP_RIGHT/DOWN_LEFT/DOWN_RIGHT）
     * @param speed     速度（1-7）
     * @return 是否执行成功
     */
    public boolean directionControl(Integer userId, Integer channelNo, String direction, Integer speed) {
        int command = switch (direction.toUpperCase()) {
            case "UP" -> PTZ_UP;
            case "DOWN" -> PTZ_DOWN;
            case "LEFT" -> PTZ_LEFT;
            case "RIGHT" -> PTZ_RIGHT;
            case "UP_LEFT", "LEFT_UP" -> PTZ_UP_LEFT;
            case "UP_RIGHT", "RIGHT_UP" -> PTZ_UP_RIGHT;
            case "DOWN_LEFT", "LEFT_DOWN" -> PTZ_DOWN_LEFT;
            case "DOWN_RIGHT", "RIGHT_DOWN" -> PTZ_DOWN_RIGHT;
            default -> throw new IllegalArgumentException("不支持的PTZ方向: " + direction);
        };
        log.info("ISUP PTZ方向控制: userId={}, channelNo={}, direction={}, speed={}", userId, channelNo, direction, speed);
        return isupSdkAdapter.ptzControl(userId, channelNo, command, 0, speed);
    }

    /**
     * 变倍控制。
     *
     * @param userId    登录句柄
     * @param channelNo 通道号
     * @param zoomType  变倍类型（ZOOM_IN / ZOOM_OUT）
     * @param speed     速度（1-7）
     * @return 是否执行成功
     */
    public boolean zoomControl(Integer userId, Integer channelNo, String zoomType, Integer speed) {
        int command = switch (zoomType.toUpperCase()) {
            case "ZOOM_IN" -> PTZ_ZOOM_IN;
            case "ZOOM_OUT" -> PTZ_ZOOM_OUT;
            default -> throw new IllegalArgumentException("不支持的变倍类型: " + zoomType);
        };
        log.info("ISUP PTZ变倍控制: userId={}, channelNo={}, zoomType={}, speed={}", userId, channelNo, zoomType, speed);
        return isupSdkAdapter.ptzControl(userId, channelNo, command, 0, speed);
    }

    /**
     * 设置预置位。
     *
     * @param userId      登录句柄
     * @param channelNo   通道号
     * @param presetIndex 预置位编号
     * @return 是否执行成功
     */
    public boolean presetSet(Integer userId, Integer channelNo, Integer presetIndex) {
        log.info("ISUP 设置预置位: userId={}, channelNo={}, presetIndex={}", userId, channelNo, presetIndex);
        return isupSdkAdapter.ptzPreset(userId, channelNo, PRESET_SET, presetIndex);
    }

    /**
     * 调用预置位。
     *
     * @param userId      登录句柄
     * @param channelNo   通道号
     * @param presetIndex 预置位编号
     * @return 是否执行成功
     */
    public boolean presetCall(Integer userId, Integer channelNo, Integer presetIndex) {
        log.info("ISUP 调用预置位: userId={}, channelNo={}, presetIndex={}", userId, channelNo, presetIndex);
        return isupSdkAdapter.ptzPreset(userId, channelNo, PRESET_CALL, presetIndex);
    }

    /**
     * 删除预置位。
     *
     * @param userId      登录句柄
     * @param channelNo   通道号
     * @param presetIndex 预置位编号
     * @return 是否执行成功
     */
    public boolean presetDelete(Integer userId, Integer channelNo, Integer presetIndex) {
        log.info("ISUP 删除预置位: userId={}, channelNo={}, presetIndex={}", userId, channelNo, presetIndex);
        return isupSdkAdapter.ptzPreset(userId, channelNo, PRESET_DELETE, presetIndex);
    }

    /**
     * 停止 PTZ 控制。
     *
     * @param userId    登录句柄
     * @param channelNo 通道号
     * @return 是否执行成功
     */
    public boolean ptzStop(Integer userId, Integer channelNo) {
        log.info("ISUP PTZ停止: userId={}, channelNo={}", userId, channelNo);
        return isupSdkAdapter.ptzControl(userId, channelNo, PTZ_UP, 1, 0);
    }
}
