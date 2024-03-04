package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.*;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoQueryDeviceResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 设备管理服务降级处理
 *
 * @author shisen
 */
@Component
public class RemoteDeviceFallbackFactory implements FallbackFactory<RemoteDeviceService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteDeviceFallbackFactory.class);

    @Override
    public RemoteDeviceService create(Throwable throwable) {
        log.error("设备管理服务调用失败:{}", throwable.getMessage());
        return new RemoteDeviceService() {
            @Override
            public R updateConnectStatusByClientId(Device device) {
                return R.fail("更新设备在线状态失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> clientAuthentication(@RequestBody Map<String, Object> params) {
                return R.fail("客户端身份认证失败:" + throwable.getMessage());
            }

            @Override
            public R<?> selectByProductIdentification(String productIdentification) {
                return R.fail("查询产品下的设备标识失败:" + throwable.getMessage());
            }

            @Override
            public R<Device> selectByProductIdentificationAndDeviceIdentification(String productIdentification, String deviceIdentification) {
                return R.fail("查询产品的设备标识失败:" + throwable.getMessage());
            }

            /**
             * 根据客户端标识获取设备信息
             *
             * @param clientId
             * @return
             */
            @Override
            public R<Device> findOneByClientId(String clientId) {
                return R.fail("查询产品下的设备标识失败:" + throwable.getMessage());
            }

            /**
             *根据产品标识获取产品所有关联设备
             * @param productIdentification
             * @return
             */
            @Override
            public R<?> selectAllByProductIdentification(String productIdentification) {
                return R.fail("查询产品下的设备:" + throwable.getMessage());
            }

            @Override
            public R<?> selectDeviceByDeviceIdentificationList(List<String> deviceIdentificationList) {

                return R.fail("根据设备标识列表查询设备失败:" + throwable.getMessage());
            }

            /**
             * （MQTT）协议新增子设备档案
             *
             * @param topoAddSubDeviceParam 子设备参数
             * @return {@link TopoAddDeviceResultVO} 新增结果
             */
            @Override
            public R<TopoAddDeviceResultVO> saveSubDeviceByMqtt(TopoAddSubDeviceParam topoAddSubDeviceParam) {
                return R.fail("新增子设备失败:" + throwable.getMessage());
            }

            /**
             * （HTTP）协议新增子设备档案
             *
             * @param topoAddSubDeviceParam 子设备参数
             * @return {@link TopoAddDeviceResultVO} 新增结果
             */
            @Override
            public R<TopoAddDeviceResultVO> saveSubDeviceByHttp(TopoAddSubDeviceParam topoAddSubDeviceParam) {
                return R.fail("新增子设备失败:" + throwable.getMessage());
            }

            /**
             * MQTT协议修改子设备连接状态
             *
             * @param topoUpdateSubDeviceStatusParam 连接状态参数
             * @return {@link TopoDeviceOperationResultVO} 修改结果
             */
            @Override
            public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByMqtt(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
                return R.fail("修改子设备连接状态失败:" + throwable.getMessage());
            }

            /**
             * HTTP协议修改子设备连接状态
             *
             * @param topoUpdateSubDeviceStatusParam 连接状态参数
             * @return {@link TopoDeviceOperationResultVO} 修改结果
             */
            @Override
            public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByHttp(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
                return R.fail("修改子设备连接状态失败:" + throwable.getMessage());
            }

            /**
             * MQTT协议删除子设备
             *
             * @param topoDeleteSubDeviceParam 删除参数
             * @return {@link TopoDeviceOperationResultVO} 修改结果
             */
            @Override
            public R<TopoDeviceOperationResultVO> deleteSubDeviceByMqtt(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
                return R.fail("删除子设备失败:" + throwable.getMessage());
            }

            /**
             * HTTP协议删除子设备
             *
             * @param topoDeleteSubDeviceParam 删除参数
             * @return {@link TopoDeviceOperationResultVO} 修改结果
             */
            @Override
            public R<TopoDeviceOperationResultVO> deleteSubDeviceByHttp(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
                return R.fail("删除子设备失败:" + throwable.getMessage());
            }

            /**
             * MQTT协议数据上报
             *
             * @param topoDeviceDataReportParam 数据上报参数
             * @return {@link TopoDeviceOperationResultVO} 上报结果
             */
            @Override
            public R<TopoDeviceOperationResultVO> deviceDataReportByMqtt(TopoDeviceDataReportParam topoDeviceDataReportParam) {
                return R.fail("数据上报失败:" + throwable.getMessage());
            }

            /**
             * HTTP协议数据上报
             *
             * @param topoDeviceDataReportParam 数据上报参数
             * @return {@link TopoDeviceOperationResultVO} 上报结果
             */
            @Override
            public R<TopoDeviceOperationResultVO> deviceDataReportByHttp(TopoDeviceDataReportParam topoDeviceDataReportParam) {
                return R.fail("数据上报失败:" + throwable.getMessage());
            }

            /**
             * Queries device information using the MQTT protocol.
             *
             * @param topoQueryDeviceParam The device query parameters.
             * @return {@link TopoQueryDeviceResultVO} The result of the device query.
             */
            @Override
            public R<TopoQueryDeviceResultVO> queryDeviceByMqtt(TopoQueryDeviceParam topoQueryDeviceParam) {
                return R.fail("查询设备失败:" + throwable.getMessage());
            }

            /**
             * Queries device information using the HTTP protocol.
             *
             * @param topoQueryDeviceParam The device query parameters.
             * @return {@link TopoQueryDeviceResultVO} The result of the device query.
             */
            @Override
            public R<TopoQueryDeviceResultVO> queryDeviceByHttp(TopoQueryDeviceParam topoQueryDeviceParam) {
                return R.fail("查询设备失败:" + throwable.getMessage());
            }

            /**
             * Receives and saves a new OTA upgrade record from an MQTT message. This endpoint
             * captures the command response parameters from the MQTT message body and persists them.
             *
             * @param otaCommandResponseParam The response parameters from an OTA command sent via MQTT.
             * @return {@link R<OtaCommandResponseParam>} A response entity containing the saved OTA upgrade record.
             */
            @Override
            public R<OtaCommandResponseParam> saveUpgradeRecordByMqtt(OtaCommandResponseParam otaCommandResponseParam) {
                return R.fail("保存升级记录失败:" + throwable.getMessage());
            }

            /**
             * Receives and saves a new OTA upgrade record from an HTTP request. This endpoint
             * captures the command response parameters from the request body and persists them.
             *
             * @param otaCommandResponseParam The response parameters from an OTA command sent via HTTP.
             * @return {@link R<OtaCommandResponseParam>} A response wrapper containing the saved OTA upgrade record.
             */
            @Override
            public R<OtaCommandResponseParam> saveUpgradeRecordByHttp(OtaCommandResponseParam otaCommandResponseParam) {
                return R.fail("保存升级记录失败:" + throwable.getMessage());
            }
        };
    }
}
