package com.mqttsnet.thinglinks.link.service.product;

import java.util.List;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsRequests;

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
public interface ProductCommandsRequestsService {


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

    /**
     * 查询产品模型设备下发服务命令属性
     *
     * @param id 产品模型设备下发服务命令属性主键
     * @return 产品模型设备下发服务命令属性
     */
    ProductCommandsRequests selectProductCommandsRequestsById(Long id);

    /**
     * 查询产品模型设备下发服务命令属性列表
     *
     * @param productCommandsRequests 产品模型设备下发服务命令属性
     * @return 产品模型设备下发服务命令属性集合
     */
    List<ProductCommandsRequests> selectProductCommandsRequestsList(ProductCommandsRequests productCommandsRequests);

    /**
     * 新增产品模型设备下发服务命令属性
     *
     * @param productCommandsRequests 产品模型设备下发服务命令属性
     * @return 结果
     */
    int insertProductCommandsRequests(ProductCommandsRequests productCommandsRequests);

    /**
     * 修改产品模型设备下发服务命令属性
     *
     * @param productCommandsRequests 产品模型设备下发服务命令属性
     * @return 结果
     */
    int updateProductCommandsRequests(ProductCommandsRequests productCommandsRequests);

    /**
     * 批量删除产品模型设备下发服务命令属性
     *
     * @param ids 需要删除的产品模型设备下发服务命令属性主键集合
     * @return 结果
     */
    int deleteProductCommandsRequestsByIds(Long[] ids);

}
