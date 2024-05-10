package com.mqttsnet.thinglinks.link.service.ota;

import com.mqttsnet.thinglinks.link.api.domain.ota.vo.save.OtaUpgradeRecordsSaveVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.update.OtaUpgradeRecordsUpdateVO;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.OtaCommandResponseParam;

public interface OtaUpgradeRecordsService {

    /**
     * 保存新的OTA升级记录。
     *
     * @param saveVO 要保存的记录
     * @return 已保存的记录
     */
    OtaUpgradeRecordsSaveVO saveOtaUpgradeRecord(OtaUpgradeRecordsSaveVO saveVO);

    /**
     * 更新现有的OTA升级记录。
     *
     * @param updateVO 要更新的记录
     * @return 更新后的记录
     */
    OtaUpgradeRecordsUpdateVO updateOtaUpgradeRecord(OtaUpgradeRecordsUpdateVO updateVO);

    /**
     * 从MQTT事件中保存OTA升级记录。
     *
     * @param otaCommandResponseParam 包含OTA命令响应的消息主体。
     * @return {@link OtaCommandResponseParam} 已保存的OTA升级记录。
     */
    OtaCommandResponseParam saveOtaUpgradeRecordByMqtt(OtaCommandResponseParam otaCommandResponseParam);

    /**
     * 从HTTP事件中保存OTA升级记录。
     *
     * @param otaCommandResponseParam 包含OTA命令响应的消息主体。
     * @return {@link OtaCommandResponseParam} 已保存的OTA升级记录。
     */
    OtaCommandResponseParam saveUpgradeRecordByHttp(OtaCommandResponseParam otaCommandResponseParam);
}
