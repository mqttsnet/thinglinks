package com.mqttsnet.thinglinks.link.mapper.casbinRule;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.CasbinRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @Description: java类作用描述
* @Author: ShiHuan SUN
* @E-mail: 13733918655@163.com
* @Website: http://thinglinks.mqttsnet.com
* @CreateDate: 2022/6/15$ 15:23$
* @UpdateUser: ShiHuan SUN
* @UpdateDate: 2022/6/15$ 15:23$
* @UpdateRemark: 修改内容
* @Version: V1.0
*/
@Mapper
public interface CasbinRuleMapper {
    /**
     * delete by primary key
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * insert record to table
     * @param record the record
     * @return insert count
     */
    int insert(CasbinRule record);

    int insertOrUpdate(CasbinRule record);

    int insertOrUpdateSelective(CasbinRule record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(CasbinRule record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    CasbinRule selectByPrimaryKey(Integer id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(CasbinRule record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(CasbinRule record);

    int updateBatch(List<CasbinRule> list);

    int updateBatchSelective(List<CasbinRule> list);

    int batchInsert(@Param("list") List<CasbinRule> list);

    /**
     * 查询CAS规则管理
     *
     * @param id CAS规则管理主键
     * @return CAS规则管理
     */
    public CasbinRule selectCasbinRuleById(Long id);

    /**
     * 查询CAS规则管理列表
     *
     * @param casbinRule CAS规则管理
     * @return CAS规则管理集合
     */
    public List<CasbinRule> selectCasbinRuleList(CasbinRule casbinRule);

    /**
     * 新增CAS规则管理
     *
     * @param casbinRule CAS规则管理
     * @return 结果
     */
    public int insertCasbinRule(CasbinRule casbinRule);

    /**
     * 修改CAS规则管理
     *
     * @param casbinRule CAS规则管理
     * @return 结果
     */
    public int updateCasbinRule(CasbinRule casbinRule);

    /**
     * 删除CAS规则管理
     *
     * @param id CAS规则管理主键
     * @return 结果
     */
    public int deleteCasbinRuleById(Long id);

    /**
     * 批量删除CAS规则管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCasbinRuleByIds(Long[] ids);
}