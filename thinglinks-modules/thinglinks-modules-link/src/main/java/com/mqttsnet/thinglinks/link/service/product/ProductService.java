package com.mqttsnet.thinglinks.link.service.product;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
public interface ProductService{


    int deleteByPrimaryKey(Long id);

    int insert(Product record);

    int insertOrUpdate(Product record);

    int insertOrUpdateSelective(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    int updateBatch(List<Product> list);

    int updateBatchSelective(List<Product> list);

    int batchInsert(List<Product> list);

    /**
     * 产品模型导入
     * @param file
     * @return AjaxResult
     * @throws Exception
     */
    AjaxResult importProductJson(MultipartFile file) throws Exception;

    /**
     * 新增产品模型
     *
     * @param content 产品模型
     * @return 结果
     */
    AjaxResult insertProduct(JSONObject content)throws Exception;

}
