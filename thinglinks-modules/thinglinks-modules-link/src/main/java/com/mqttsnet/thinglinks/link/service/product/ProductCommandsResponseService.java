package com.mqttsnet.thinglinks.link.service.product;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsResponse;
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
public interface ProductCommandsResponseService{


    int deleteByPrimaryKey(Long id);

    int insert(ProductCommandsResponse record);

    int insertOrUpdate(ProductCommandsResponse record);

    int insertOrUpdateSelective(ProductCommandsResponse record);

    int insertSelective(ProductCommandsResponse record);

    ProductCommandsResponse selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProductCommandsResponse record);

    int updateByPrimaryKey(ProductCommandsResponse record);

    int updateBatch(List<ProductCommandsResponse> list);

    int updateBatchSelective(List<ProductCommandsResponse> list);

    int batchInsert(List<ProductCommandsResponse> list);

}
