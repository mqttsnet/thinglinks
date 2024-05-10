
package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceOpenAnyService;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.*;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.TopoQueryDeviceResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 设备管理开放服务降级处理
 *
 * @author xiaonannet
 */
@Component
public class RemoteDeviceOpenAnyFallbackFactory implements FallbackFactory<RemoteDeviceOpenAnyService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteDeviceOpenAnyFallbackFactory.class);

    @Override
    public RemoteDeviceOpenAnyService create(Throwable throwable) {
        log.error("设备管理开放服务调用失败:{}", throwable.getMessage());
        return new RemoteDeviceOpenAnyService() {

            @Override
            public R<Boolean> clientAuthentication(Map<String, Object> params) {
                return R.fail("客户端身份认证失败:" + throwable.getMessage());
            }

            @Override
            public R<TopoAddDeviceResultVO> saveSubDeviceByMqtt(TopoAddSubDeviceParam topoAddSubDeviceParam) {
                return R.fail("（MQTT）协议新增子设备档案失败:" + throwable.getMessage());
            }

            @Override
            public R<TopoAddDeviceResultVO> saveSubDeviceByHttp(TopoAddSubDeviceParam topoAddSubDeviceParam) {
                return R.fail("（HTTP）协议新增子设备档案失败:" + throwable.getMessage());
            }

            @Override
            public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByMqtt(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
                return R.fail("MQTT协议修改子设备连接状态失败:" + throwable.getMessage());
            }

            @Override
            public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByHttp(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
                return R.fail("HTTP协议修改子设备连接状态失败:" + throwable.getMessage());
            }

            @Override
            public R<TopoDeviceOperationResultVO> deleteSubDeviceByMqtt(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
                return R.fail("MQTT协议删除子设备失败:" + throwable.getMessage());
            }

            @Override
            public R<TopoDeviceOperationResultVO> deleteSubDeviceByHttp(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
                return R.fail("HTTP协议删除子设备失败:" + throwable.getMessage());
            }

            @Override
            public R<TopoDeviceOperationResultVO> deviceDataReportByMqtt(TopoDeviceDataReportParam topoDeviceDataReportParam) {
                return R.fail("MQTT协议设备数据上报失败:" + throwable.getMessage());
            }

            @Override
            public R<TopoDeviceOperationResultVO> deviceDataReportByHttp(TopoDeviceDataReportParam topoDeviceDataReportParam) {
                return R.fail("HTTP协议设备数据上报失败:" + throwable.getMessage());
            }

            @Override
            public R<TopoQueryDeviceResultVO> queryDeviceByMqtt(TopoQueryDeviceParam topoQueryDeviceParam) {
                return R.fail("MQTT协议查询设备失败:" + throwable.getMessage());
            }

            @Override
            public R<TopoQueryDeviceResultVO> queryDeviceByHttp(TopoQueryDeviceParam topoQueryDeviceParam) {
                return R.fail("HTTP协议查询设备失败:" + throwable.getMessage());
            }

            @Override
            public R<OtaCommandResponseParam> saveUpgradeRecordByMqtt(OtaCommandResponseParam otaCommandResponseParam) {
                return R.fail("MQTT协议保存升级记录失败:" + throwable.getMessage());
            }

            @Override
            public R<OtaCommandResponseParam> saveUpgradeRecordByHttp(OtaCommandResponseParam otaCommandResponseParam) {
                return R.fail("HTTP协议保存升级记录失败:" + throwable.getMessage());
            }
        };
    }
}
