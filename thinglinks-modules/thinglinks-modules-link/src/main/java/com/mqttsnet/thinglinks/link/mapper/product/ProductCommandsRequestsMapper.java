package com.mqttsnet.thinglinks.link.mapper.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsRequests;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**

* @Description:    java类作用描述
* @Author:         ShiHuan Sun
* @E-mail:         13733918655@163.com
* @Website:        http://thinglinks.mqttsnet.com
* @CreateDate:     2021/12/23$ 18:40$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/23$ 18:40$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
@Mapper
public interface ProductCommandsRequestsMapper {
    /**
     * delete by primary key
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert record to table
     * @param record the record
     * @return insert count
     */
    int insert(ProductCommandsRequests record);

    int insertOrUpdate(ProductCommandsRequests record);

    int insertOrUpdateSelective(ProductCommandsRequests record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(ProductCommandsRequests record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    ProductCommandsRequests selectByPrimaryKey(Long id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(ProductCommandsRequests record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(ProductCommandsRequests record);

    int updateBatch(List<ProductCommandsRequests> list);

    int updateBatchSelective(List<ProductCommandsRequests> list);

    int batchInsert(@Param("list") List<ProductCommandsRequests> list);
}