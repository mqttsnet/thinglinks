package com.mqttsnet.thinglinks.link.service.ota.impl;

import com.mqttsnet.thinglinks.common.core.exception.ServiceException;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.ota.entity.OtaUpgradeRecords;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.save.OtaUpgradeRecordsSaveVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.update.OtaUpgradeRecordsUpdateVO;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.OtaCommandResponseParam;
import com.mqttsnet.thinglinks.link.mapper.ota.OtaUpgradeRecordsMapper;
import com.mqttsnet.thinglinks.link.service.ota.OtaUpgradeRecordsService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class OtaUpgradeRecordsServiceImpl implements OtaUpgradeRecordsService {

    @Resource
    private OtaUpgradeRecordsMapper otaUpgradeRecordsMapper;

    @Resource
    private TokenService tokenService;

    /**
     * 保存新的OTA升级记录。
     *
     * @param saveVO 要保存的记录
     * @return 已保存的记录
     */
    @Override
    public OtaUpgradeRecordsSaveVO saveOtaUpgradeRecord(OtaUpgradeRecordsSaveVO saveVO) {

        validateOtaUpgradeRecordsSaveVO(saveVO);

        // Validate and map the saveVO
        OtaUpgradeRecords record = buildOtaUpgradeRecordSaveVO(saveVO);
        otaUpgradeRecordsMapper.insertOtaUpgradeRecords(record);
        return BeanPlusUtil.toBeanIgnoreError(record, OtaUpgradeRecordsSaveVO.class);
    }


    /**
     * 更新现有的OTA升级记录。
     *
     * @param updateVO 要更新的记录
     * @return 更新后的记录
     */
    @Override
    public OtaUpgradeRecordsUpdateVO updateOtaUpgradeRecord(OtaUpgradeRecordsUpdateVO updateVO) {
        validateOtaUpgradeRecordsUpdateVO(updateVO);

        // Validate and fetch existing record
        OtaUpgradeRecords existingRecord = otaUpgradeRecordsMapper.selectOtaUpgradeRecordsById(updateVO.getId());
        if (existingRecord == null) {
            throw new ServiceException("OTA upgrade record not found");
        }

        // Update the record
        OtaUpgradeRecords records = buildOtaUpgradeRecordSaveVO(updateVO);

        otaUpgradeRecordsMapper.updateOtaUpgradeRecordsById(records);

        return BeanPlusUtil.toBeanIgnoreError(records, OtaUpgradeRecordsUpdateVO.class);
    }



    /**
     * 从MQTT事件中保存OTA升级记录。
     *
     * @param otaCommandResponseParam 包含OTA命令响应的消息主体。
     * @return {@link OtaCommandResponseParam} 已保存的OTA升级记录。
     */
    @Override
    public OtaCommandResponseParam saveOtaUpgradeRecordByMqtt(OtaCommandResponseParam otaCommandResponseParam) {
        return null;
    }

    /**
     * 从HTTP事件中保存OTA升级记录。
     *
     * @param otaCommandResponseParam 包含OTA命令响应的消息主体。
     * @return {@link OtaCommandResponseParam} 已保存的OTA升级记录。
     */
    @Override
    public OtaCommandResponseParam saveUpgradeRecordByHttp(OtaCommandResponseParam otaCommandResponseParam) {
        return null;
    }


    private <T> OtaUpgradeRecords buildOtaUpgradeRecordSaveVO(T vo) {
        SysUser sysUser = tokenService.getLoginUser().getSysUser();
        OtaUpgradeRecords otaUpgradeRecords = BeanPlusUtil.toBeanIgnoreError(vo, OtaUpgradeRecords.class);
        otaUpgradeRecords.setCreatedBy(sysUser.getUserName());
        otaUpgradeRecords.setUpdatedBy(sysUser.getUserName());
        return otaUpgradeRecords;
    }

    private void validateOtaUpgradeRecordsSaveVO(OtaUpgradeRecordsSaveVO saveVO) {
    }

    private void validateOtaUpgradeRecordsUpdateVO(OtaUpgradeRecordsUpdateVO updateVO) {
    }
}
