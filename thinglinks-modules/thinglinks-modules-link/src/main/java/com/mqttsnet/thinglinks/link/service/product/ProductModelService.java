package com.mqttsnet.thinglinks.link.service.product;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductModel;
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
public interface ProductModelService{


    int deleteByPrimaryKey(Long id);

    int insert(ProductModel record);

    int insertOrUpdate(ProductModel record);

    int insertOrUpdateSelective(ProductModel record);

    int insertSelective(ProductModel record);

    ProductModel selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProductModel record);

    int updateByPrimaryKey(ProductModel record);

    int updateBatch(List<ProductModel> list);

    int updateBatchSelective(List<ProductModel> list);

    int batchInsert(List<ProductModel> list);

}
