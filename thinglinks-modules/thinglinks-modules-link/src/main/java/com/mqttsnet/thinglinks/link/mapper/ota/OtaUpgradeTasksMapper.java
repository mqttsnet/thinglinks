package com.mqttsnet.thinglinks.link.mapper.ota;

import com.mqttsnet.thinglinks.link.api.domain.ota.entity.OtaUpgradeTasks;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OtaUpgradeTasksMapper {
    // 插入一条记录
    int insertOtaUpgradeTask(OtaUpgradeTasks otaUpgradeTasks);

    // 根据ID删除一条记录
    int deleteOtaUpgradeTaskById(Long id);

    // 更新一条记录
    int updateOtaUpgradeTaskById(OtaUpgradeTasks otaUpgradeTasks);

    // 根据ID查询一条记录
    OtaUpgradeTasks selectOtaUpgradeTaskById(Long id);

    // 查询所有记录
    List<OtaUpgradeTasks> selectAllOtaUpgradeTasks();

    // 根据OtaUpgradeId查询
    int getOtaUpgradeTasksByOtaUpgradeId(Long id);

    //更新状态
    int updateOtaUpgradeTasksByStatus(@Param("id") Long id, @Param("status") Integer status);
}
