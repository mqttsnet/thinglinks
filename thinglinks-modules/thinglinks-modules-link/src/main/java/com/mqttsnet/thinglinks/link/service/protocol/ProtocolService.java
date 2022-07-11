package com.mqttsnet.thinglinks.link.service.protocol;

import com.mqttsnet.thinglinks.link.api.domain.protocol.Protocol;

import java.util.Collection;
import java.util.List;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.link.service.protocol
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-01 17:56
 **/
public interface ProtocolService {

    int deleteByPrimaryKey(Long id);

    int insert(Protocol record);

    int insertOrUpdate(Protocol record);

    int insertOrUpdateSelective(Protocol record);

    int insertSelective(Protocol record);

    Protocol selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Protocol record);

    int updateByPrimaryKey(Protocol record);

    int updateBatch(List<Protocol> list);

    int updateBatchSelective(List<Protocol> list);

    int batchInsert(List<Protocol> list);

    /**
     * 查询协议管理
     *
     * @param id 协议管理主键
     * @return 协议管理
     */
    public Protocol selectProtocolById(Long id);

    /**
     * 查询协议管理列表
     *
     * @param protocol 协议管理
     * @return 协议管理集合
     */
    public List<Protocol> selectProtocolList(Protocol protocol);

    /**
     * 新增协议管理
     *
     * @param protocol 协议管理
     * @return 结果
     */
    public int insertProtocol(Protocol protocol);

    /**
     * 修改协议管理
     *
     * @param protocol 协议管理
     * @return 结果
     */
    public int updateProtocol(Protocol protocol);

    /**
     * 批量删除协议管理
     *
     * @param ids 需要删除的协议管理主键集合
     * @return 结果
     */
    public int deleteProtocolByIds(Long[] ids);



	Protocol findOneByProductIdentificationAndProtocolTypeAndStatus(String productIdentification,String protocolType,String status);

    /**
     * 批量启用协议管理
     * @param ids
     * @return
     */
    public int enable(Long[] ids);

    /**
     * 批量禁用协议管理
     * @param ids
     * @return
     */
    public int disable(Long[] ids);



	List<Protocol> findAllByIdIn(Collection<Long> idCollection);



	int updateStatusById(String updatedStatus,Long id);


    /**
     * 协议脚本缓存刷新
     * @return
     */
    int protocolScriptCacheRefresh();



	List<Protocol> findAllByStatus(String status);

}
