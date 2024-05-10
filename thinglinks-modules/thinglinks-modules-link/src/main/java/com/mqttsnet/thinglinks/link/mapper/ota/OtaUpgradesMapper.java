package com.mqttsnet.thinglinks.link.mapper.ota;


import com.mqttsnet.thinglinks.link.api.domain.ota.entity.OtaUpgrades;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OtaUpgradesMapper {

    // 插入一条记录
    int insertOtaUpgrades(OtaUpgrades otaUpgrades);
    // 根据ID删除一条记录
    int deleteOtaUpgradeById(Long id);

    // 更新一条记录
    int updateOtaUpgradeById(OtaUpgrades otaUpgrades);

    // 根据ID查询一条记录
    OtaUpgrades selectOtaUpgradeById(Long id);

    // 查询所有记录
    List<OtaUpgrades> selectAllOtaUpgrades();

    // 根据ID修改状态
    int updateOtaUpgradeByStatus(@Param("id") Long id, @Param("status") Integer status);
}
