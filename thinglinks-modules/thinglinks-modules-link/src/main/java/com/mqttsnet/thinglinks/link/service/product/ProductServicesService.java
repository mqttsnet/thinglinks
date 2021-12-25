package com.mqttsnet.thinglinks.link.service.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import java.util.List;
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
public interface ProductServicesService{


    int deleteByPrimaryKey(Long id);

    int insert(ProductServices record);

    int insertOrUpdate(ProductServices record);

    int insertOrUpdateSelective(ProductServices record);

    int insertSelective(ProductServices record);

    ProductServices selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProductServices record);

    int updateByPrimaryKey(ProductServices record);

    int updateBatch(List<ProductServices> list);

    int updateBatchSelective(List<ProductServices> list);

    int batchInsert(List<ProductServices> list);

}
