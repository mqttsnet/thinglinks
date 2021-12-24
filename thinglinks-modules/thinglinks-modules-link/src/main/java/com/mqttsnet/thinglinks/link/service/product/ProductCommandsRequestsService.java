package com.mqttsnet.thinglinks.link.service.product;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsRequests;
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
public interface ProductCommandsRequestsService{


    int deleteByPrimaryKey(Long id);

    int insert(ProductCommandsRequests record);

    int insertOrUpdate(ProductCommandsRequests record);

    int insertOrUpdateSelective(ProductCommandsRequests record);

    int insertSelective(ProductCommandsRequests record);

    ProductCommandsRequests selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProductCommandsRequests record);

    int updateByPrimaryKey(ProductCommandsRequests record);

    int updateBatch(List<ProductCommandsRequests> list);

    int updateBatchSelective(List<ProductCommandsRequests> list);

    int batchInsert(List<ProductCommandsRequests> list);

}
