package com.mqttsnet.thinglinks.link.mapper.ota;

import com.mqttsnet.thinglinks.link.api.domain.ota.entity.OtaUpgradeRecords;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OtaUpgradeRecordsMapper {
    // 插入一条OTA升级记录
    int insertOtaUpgradeRecords(OtaUpgradeRecords otaUpgradeRecords);

    // 根据ID删除一条OTA升级记录
    int deleteOtaUpgradeRecordsById(Long id);

    // 更新一条OTA升级记录
    int updateOtaUpgradeRecordsById(OtaUpgradeRecords otaUpgradeRecords);

    // 根据ID查询一条OTA升级记录
    OtaUpgradeRecords selectOtaUpgradeRecordsById(Long id);

    // 查询所有OTA升级记录
    List<OtaUpgradeRecords> selectAllOtaUpgradeRecords();
}
