package com.mqttsnet.thinglinks.link.service.product;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductTemplate;
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
public interface ProductTemplateService{


    int deleteByPrimaryKey(Long id);

    int insert(ProductTemplate record);

    int insertOrUpdate(ProductTemplate record);

    int insertOrUpdateSelective(ProductTemplate record);

    int insertSelective(ProductTemplate record);

    ProductTemplate selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProductTemplate record);

    int updateByPrimaryKey(ProductTemplate record);

    int updateBatch(List<ProductTemplate> list);

    int updateBatchSelective(List<ProductTemplate> list);

    int batchInsert(List<ProductTemplate> list);

}
