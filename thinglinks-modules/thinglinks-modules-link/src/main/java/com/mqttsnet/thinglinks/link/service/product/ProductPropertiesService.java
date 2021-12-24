package com.mqttsnet.thinglinks.link.service.product;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
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
public interface ProductPropertiesService{


    int deleteByPrimaryKey(Long id);

    int insert(ProductProperties record);

    int insertOrUpdate(ProductProperties record);

    int insertOrUpdateSelective(ProductProperties record);

    int insertSelective(ProductProperties record);

    ProductProperties selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProductProperties record);

    int updateByPrimaryKey(ProductProperties record);

    int updateBatch(List<ProductProperties> list);

    int updateBatchSelective(List<ProductProperties> list);

    int batchInsert(List<ProductProperties> list);

}
