package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.*;

import java.util.List;

/**
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: DiskIoStateService.java
 */
public interface MailSetService {

    /**
     * 新增
     *
     * @param mailSet
     */
    public void save(MailSet mailSet);

    /**
     * 删除
     *
     * @param id
     * @return
     */
    public int deleteById(String[] id);

    /**
     * 根据条件查询
     *
     * @param mailSet
     * @return
     */
    public List<MailSet> selectMailSetList(MailSet mailSet);

    /**
     * 更新
     *
     * @param MailSet
     * @return
     */
    int updateById(MailSet MailSet);

}
