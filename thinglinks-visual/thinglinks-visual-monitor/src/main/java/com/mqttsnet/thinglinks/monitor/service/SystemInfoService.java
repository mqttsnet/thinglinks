package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.SystemInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:SystemInfoService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: SystemInfoService.java
 */
public interface SystemInfoService {

    /**
     * 查询系统信息列表
     *
     * @param systemInfo
     * @return
     */
    List<SystemInfo> selectSystemInfoList(SystemInfo systemInfo);

    /**
     * 新增系统信息
     *
     * @param SystemInfo
     */
    void save(SystemInfo SystemInfo);

    /**
     * 批量新增系统信息
     *
     * @param recordList
     */
    void saveRecord(List<SystemInfo> recordList);

    /**
     * 批量更新系统信息
     *
     * @param recordList
     */
    void updateRecord(List<SystemInfo> recordList);

    /**
     * 更新系统信息
     *
     * @param systemInfo
     */
    void updateById(SystemInfo systemInfo);

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    int deleteById(String[] ids);

    /**
     * 根据ID查询
     *
     * @param id
     */
    SystemInfo selectById(String id);

    /**
     * @param params
     */
    int countByParams(Map<String, Object> params);

    /**
     * @param accountId
     */
    List<SystemInfo> selectByAccountId(String accountId);

    /**
     * 根据主机地址删除 hostname
     * @param params
     */
    int deleteByAccHname(Map<String, Object> params);

}
