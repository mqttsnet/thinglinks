package com.mqttsnet.thinglinks.link.mapper.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.CasbinRule;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}