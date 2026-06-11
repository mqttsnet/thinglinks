package com.mqttsnet.thinglinks.video.gb28181.cmd;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.SipCommandEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SipCommandSentEventSource;
import com.mqttsnet.thinglinks.video.gb28181.protocol.GbProtocolAdapterFactory;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 * PTZ 云台控制命令发送器。
 * 负责构建和发送 PTZ 相关的 SIP MESSAGE 命令，包括：
 * - 方向控制（上/下/左/右/左上/右上/左下/右下）
 * - 变倍控制（放大/缩小）
 * - 预置位操作（设置/调用/删除）
 * - 巡航控制（开始/停止/添加巡航点/删除巡航点）
 * - 自动扫描（开始/停止/设置左右边界）
 * - 看守位控制（设置/调用/删除）（GB/T 28181-2022）
 * - 拉框放大/缩小（GB/T 28181-2022）
 * - 辅助开关控制
 * <p>
 * PTZ 指令编码遵循 A50F 协议格式（GB/T 28181 附录 A）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PTZCommander {

    private final SipMessageBuilder sipMessageBuilder;
    private final SIPSender sipSender;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final SipCommandEventPublisher sipCommandEventPublisher;

    /**
     * SN 序列号生成器
     */
    private final AtomicInteger snGenerator = new AtomicInteger(1);

    /**
     * 发送方向控制命令
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param leftRight  左右方向（0=停止，1=左移，2=右移）
     * @param upDown     上下方向（0=停止，1=上移，2=下移）
     * @param moveSpeed  移动速度（0-255）
     */
    public void directionControl(String deviceIdentification, String channelIdentification,
                                 String deviceIp, int devicePort,
                                 String transport,
                                 int leftRight, int upDown, int moveSpeed) {
        String cmdStr = SipUtils.cmdString(leftRight, upDown, 0, moveSpeed, 0);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "DIRECTION");
    }

    /**
     * 发送变倍控制命令
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param inOut      变倍方向（0=停止，1=缩小，2=放大）
     * @param zoomSpeed  变倍速度（0-255）
     */
    public void zoomControl(String deviceIdentification, String channelIdentification,
                            String deviceIp, int devicePort,
                            String transport,
                            int inOut, int zoomSpeed) {
        String cmdStr = SipUtils.cmdString(0, 0, inOut, 0, zoomSpeed);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "ZOOM");
    }

    /**
     * 发送组合控制命令（方向 + 变倍同时控制）
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param leftRight  左右方向
     * @param upDown     上下方向
     * @param inOut      变倍方向
     * @param moveSpeed  移动速度
     * @param zoomSpeed  变倍速度
     */
    public void combinedControl(String deviceIdentification, String channelIdentification,
                                String deviceIp, int devicePort,
                                String transport,
                                int leftRight, int upDown, int inOut,
                                int moveSpeed, int zoomSpeed) {
        String cmdStr = SipUtils.cmdString(leftRight, upDown, inOut, moveSpeed, zoomSpeed);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "COMBINED");
    }

    /**
     * 停止 PTZ 控制
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     */
    public void stopPtz(String deviceIdentification, String channelIdentification,
                        String deviceIp, int devicePort,
                        String transport) {
        String cmdStr = SipUtils.cmdString(0, 0, 0, 0, 0);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "STOP");
    }

    /**
     * 预置位 - 设置
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param presetId   预置位编号（1-255）
     */
    public void presetSet(String deviceIdentification, String channelIdentification,
                          String deviceIp, int devicePort,
                          String transport, int presetId) {
        String cmdStr = buildPresetCommand(0x81, presetId);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "PRESET_SET:" + presetId);
    }

    /**
     * 预置位 - 调用
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param presetId   预置位编号
     */
    public void presetCall(String deviceIdentification, String channelIdentification,
                           String deviceIp, int devicePort,
                           String transport, int presetId) {
        String cmdStr = buildPresetCommand(0x82, presetId);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "PRESET_CALL:" + presetId);
    }

    /**
     * 预置位 - 删除
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param presetId   预置位编号
     */
    public void presetDelete(String deviceIdentification, String channelIdentification,
                             String deviceIp, int devicePort,
                             String transport, int presetId) {
        String cmdStr = buildPresetCommand(0x83, presetId);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "PRESET_DELETE:" + presetId);
    }

    /**
     * 巡航 - 开始巡航
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param cruiseId   巡航组编号
     */
    public void cruiseStart(String deviceIdentification, String channelIdentification,
                            String deviceIp, int devicePort,
                            String transport, int cruiseId) {
        String cmdStr = buildCruiseCommand(0x80, cruiseId, 0, 0);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "CRUISE_START:" + cruiseId);
    }

    /**
     * 巡航 - 停止巡航
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param cruiseId   巡航组编号
     */
    public void cruiseStop(String deviceIdentification, String channelIdentification,
                           String deviceIp, int devicePort,
                           String transport, int cruiseId) {
        String cmdStr = buildCruiseCommand(0x00, cruiseId, 0, 0);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "CRUISE_STOP:" + cruiseId);
    }

    /**
     * 巡航 - 添加巡航点
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param cruiseId   巡航组编号
     * @param presetId   预置位编号
     * @param speed      巡航速度
     * @param stayTime   停留时间（秒）
     */
    public void cruiseAddPoint(String deviceIdentification, String channelIdentification,
                               String deviceIp, int devicePort,
                               String transport,
                               int cruiseId, int presetId, int speed, int stayTime) {
        // 添加预置位到巡航组
        String cmdStr = buildCruiseCommand(0x81, cruiseId, presetId, 0);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "CRUISE_ADD_POINT");

        // 设置巡航速度
        String speedCmd = buildCruiseCommand(0x82, cruiseId, presetId, speed);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, speedCmd, "CRUISE_SET_SPEED");

        // 设置停留时间
        String stayCmd = buildCruiseCommand(0x83, cruiseId, presetId, stayTime);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, stayCmd, "CRUISE_SET_STAY");
    }

    /**
     * 巡航 - 删除巡航点
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param cruiseId   巡航组编号
     * @param presetId   预置位编号
     */
    public void cruiseDeletePoint(String deviceIdentification, String channelIdentification,
                                  String deviceIp, int devicePort,
                                  String transport,
                                  int cruiseId, int presetId) {
        String cmdStr = buildCruiseCommand(0x84, cruiseId, presetId, 0);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "CRUISE_DELETE_POINT");
    }

    /**
     * 自动扫描 - 开始
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param scanId     扫描组编号
     * @param speed      扫描速度
     */
    public void scanStart(String deviceIdentification, String channelIdentification,
                          String deviceIp, int devicePort,
                          String transport, int scanId, int speed) {
        String cmdStr = buildScanCommand(0x80, scanId, speed);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "SCAN_START:" + scanId);
    }

    /**
     * 自动扫描 - 停止
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param scanId     扫描组编号
     */
    public void scanStop(String deviceIdentification, String channelIdentification,
                         String deviceIp, int devicePort,
                         String transport, int scanId) {
        String cmdStr = buildScanCommand(0x00, scanId, 0);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "SCAN_STOP:" + scanId);
    }

    /**
     * 自动扫描 - 设置左边界
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param scanId     扫描组编号
     */
    public void scanSetLeftBorder(String deviceIdentification, String channelIdentification,
                                  String deviceIp, int devicePort,
                                  String transport, int scanId) {
        String cmdStr = buildScanCommand(0x81, scanId, 0);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "SCAN_LEFT_BORDER:" + scanId);
    }

    /**
     * 自动扫描 - 设置右边界
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param scanId     扫描组编号
     */
    public void scanSetRightBorder(String deviceIdentification, String channelIdentification,
                                   String deviceIp, int devicePort,
                                   String transport, int scanId) {
        String cmdStr = buildScanCommand(0x82, scanId, 0);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr, "SCAN_RIGHT_BORDER:" + scanId);
    }

    /**
     * 辅助开关控制
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param switchId   开关编号（1-255）
     * @param on         true=开启，false=关闭
     */
    public void auxiliarySwitch(String deviceIdentification, String channelIdentification,
                                String deviceIp, int devicePort,
                                String transport, int switchId, boolean on) {
        String cmdStr = buildAuxSwitchCommand(switchId, on);
        sendPtzCommand(deviceIdentification, channelIdentification, deviceIp, devicePort, transport, cmdStr,
                "AUX_SWITCH:" + switchId + "=" + (on ? "ON" : "OFF"));
    }

    /**
     * 发送 PTZ 控制命令（通过 SIP MESSAGE + DeviceControl XML）
     */
    private void sendPtzCommand(String deviceIdentification, String channelIdentification,
                                String deviceIp, int devicePort,
                                String transport, String ptzCmd, String operation) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            String sn = String.valueOf(snGenerator.getAndIncrement());
            String xml = buildDeviceControlXml(sn, channelIdentification, ptzCmd);

            CallIdHeader callId = sipSender.getNewCallIdHeader(tenantConfig.getEffectiveHost(), transport);
            Request request = sipMessageBuilder.buildMessageRequest(
                    deviceIdentification, deviceIp, devicePort, transport, xml, callId, tenantConfig);

            sipSender.transmitRequest(tenantConfig.getEffectiveHost(), request);

            log.debug("发送PTZ控制({}): deviceIdentification={}, channelIdentification={}, cmd={}",
                    operation, deviceIdentification, channelIdentification, ptzCmd);

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType("PTZ_" + operation)
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(channelIdentification)
                            .callId(callId.getCallId())
                            .sipMethod("MESSAGE")
                            .build());
        } catch (Exception e) {
            log.error("发送PTZ控制({})失败: deviceIdentification={}, channelIdentification={}",
                    operation, deviceIdentification, channelIdentification, e);
            throw BizException.wrap("发送PTZ控制失败: " + e.getMessage());
        }
    }

    /**
     * 构建设备控制 XML（PTZ 命令）
     */
    private String buildDeviceControlXml(String sn, String channelIdentification, String ptzCmd) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Control>
                <CmdType>DeviceControl</CmdType>
                <SN>%s</SN>
                <DeviceID>%s</DeviceID>
                <PTZCmd>%s</PTZCmd>
                </Control>""".formatted(sn, channelIdentification, ptzCmd);
    }

    /**
     * 构建预置位指令码（A50F 格式）
     *
     * @param cmdCode  指令码（0x81=设置，0x82=调用，0x83=删除）
     * @param presetId 预置位编号
     * @return A50F 指令字符串
     */
    private String buildPresetCommand(int cmdCode, int presetId) {
        var sb = new StringBuilder("A50F01");
        sb.append(String.format("%02X", cmdCode));
        sb.append("00");
        sb.append(String.format("%02X", presetId));
        sb.append("00");
        int checkCode = (0xA5 + 0x0F + 0x01 + cmdCode + presetId) % 0x100;
        sb.append(String.format("%02X", checkCode));
        return sb.toString();
    }

    /**
     * 构建巡航指令码（A50F 格式）
     *
     * @param cmdCode  巡航子命令
     * @param cruiseId 巡航组编号
     * @param presetId 预置位编号
     * @param param    参数（速度/停留时间）
     * @return A50F 指令字符串
     */
    private String buildCruiseCommand(int cmdCode, int cruiseId, int presetId, int param) {
        var sb = new StringBuilder("A50F01");
        sb.append(String.format("%02X", cmdCode));
        sb.append(String.format("%02X", cruiseId));
        sb.append(String.format("%02X", presetId));
        sb.append(String.format("%02X", param));
        int checkCode = (0xA5 + 0x0F + 0x01 + cmdCode + cruiseId + presetId + param) % 0x100;
        sb.append(String.format("%02X", checkCode));
        return sb.toString();
    }

    /**
     * 构建扫描指令码（A50F 格式）
     *
     * @param cmdCode 扫描子命令
     * @param scanId  扫描组编号
     * @param speed   扫描速度
     * @return A50F 指令字符串
     */
    private String buildScanCommand(int cmdCode, int scanId, int speed) {
        var sb = new StringBuilder("A50F01");
        sb.append(String.format("%02X", cmdCode));
        sb.append(String.format("%02X", scanId));
        sb.append("00");
        sb.append(String.format("%02X", speed));
        int checkCode = (0xA5 + 0x0F + 0x01 + cmdCode + scanId + speed) % 0x100;
        sb.append(String.format("%02X", checkCode));
        return sb.toString();
    }

    /**
     * 构建辅助开关指令码（A50F 格式）
     *
     * @param switchId 开关编号
     * @param on       是否开启
     * @return A50F 指令字符串
     */
    private String buildAuxSwitchCommand(int switchId, boolean on) {
        int cmdCode = on ? 0x88 : 0x89;
        var sb = new StringBuilder("A50F01");
        sb.append(String.format("%02X", cmdCode));
        sb.append("00");
        sb.append(String.format("%02X", switchId));
        sb.append("00");
        int checkCode = (0xA5 + 0x0F + 0x01 + cmdCode + switchId) % 0x100;
        sb.append(String.format("%02X", checkCode));
        return sb.toString();
    }
}
