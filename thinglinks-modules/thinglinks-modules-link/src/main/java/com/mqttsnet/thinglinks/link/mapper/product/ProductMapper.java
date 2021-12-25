package com.mqttsnet.thinglinks.link.mapper.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**

* @Description:    java类作用描述
* @Author:         ShiHuan Sun
* @E-mail:         13733918655@163.com
* @Website:        http://thinglinks.mqttsnet.com
* @CreateDate:     2021/12/25$ 23:52$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/25$ 23:52$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
@Mapper
public interface ProductMapper {
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
    int insert(Product record);

    int insertOrUpdate(Product record);

    int insertOrUpdateSelective(Product record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(Product record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    Product selectByPrimaryKey(Long id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(Product record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(Product record);

    int updateBatch(List<Product> list);

    int updateBatchSelective(List<Product> list);

    int batchInsert(@Param("list") List<Product> list);
}