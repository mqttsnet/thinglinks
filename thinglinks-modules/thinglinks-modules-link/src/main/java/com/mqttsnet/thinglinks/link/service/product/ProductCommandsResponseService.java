package com.mqttsnet.thinglinks.link.service.product;

import java.util.List;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsResponse;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/25$ 23:52$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/25$ 23:52$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public interface ProductCommandsResponseService {


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

    /**
     * 查询产品模型设备响应服务命令属性
     *
     * @param id 产品模型设备响应服务命令属性主键
     * @return 产品模型设备响应服务命令属性
     */
    ProductCommandsResponse selectProductCommandsResponseById(Long id);

    /**
     * 查询产品模型设备响应服务命令属性列表
     *
     * @param productCommandsResponse 产品模型设备响应服务命令属性
     * @return 产品模型设备响应服务命令属性集合
     */
    List<ProductCommandsResponse> selectProductCommandsResponseList(ProductCommandsResponse productCommandsResponse);

    /**
     * 新增产品模型设备响应服务命令属性
     *
     * @param productCommandsResponse 产品模型设备响应服务命令属性
     * @return 结果
     */
    int insertProductCommandsResponse(ProductCommandsResponse productCommandsResponse);

    /**
     * 修改产品模型设备响应服务命令属性
     *
     * @param productCommandsResponse 产品模型设备响应服务命令属性
     * @return 结果
     */
    int updateProductCommandsResponse(ProductCommandsResponse productCommandsResponse);

    /**
     * 批量删除产品模型设备响应服务命令属性
     *
     * @param ids 需要删除的产品模型设备响应服务命令属性主键集合
     * @return 结果
     */
    int deleteProductCommandsResponseByIds(Long[] ids);

    List<ProductCommandsResponse> selectProductCommandsResponseByCommandIdList(@Param("commandIdList") List<Long> commandIdList);

}
