package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.MailSet;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:MailSetMapper.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 查看磁盘IO使用情况
 */
@Repository
public interface MailSetMapper {

    /**
     * 根据条件查询
     *
     * @param mailSet
     * @return
     * @throws Exception
     */
    public List<MailSet> selectMailSetList(MailSet mailSet);

    /**
     * 新增
     *
     * @param MailSet
     * @throws Exception
     */
    public void save(MailSet MailSet);

    /**
     * 根据ID删除
     *
     * @param id
     * @return
     * @throws Exception
     */
    public int deleteById(String[] id);

    /**
     * 更新
     *
     * @param MailSet
     * @return
     * @throws Exception
     */
    public int updateById(MailSet MailSet);

}
