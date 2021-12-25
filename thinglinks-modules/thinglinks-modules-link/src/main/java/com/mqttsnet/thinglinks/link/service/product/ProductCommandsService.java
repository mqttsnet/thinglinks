package com.mqttsnet.thinglinks.link.service.product;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommands;
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
public interface ProductCommandsService{


    int deleteByPrimaryKey(Long id);

    int insert(ProductCommands record);

    int insertOrUpdate(ProductCommands record);

    int insertOrUpdateSelective(ProductCommands record);

    int insertSelective(ProductCommands record);

    ProductCommands selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProductCommands record);

    int updateByPrimaryKey(ProductCommands record);

    int updateBatch(List<ProductCommands> list);

    int updateBatchSelective(List<ProductCommands> list);

    int batchInsert(List<ProductCommands> list);

}
