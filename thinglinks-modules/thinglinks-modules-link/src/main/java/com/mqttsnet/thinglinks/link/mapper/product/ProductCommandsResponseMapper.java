package com.mqttsnet.thinglinks.link.mapper.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsResponse;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
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
@Mapper
public interface ProductCommandsResponseMapper {
    /**
     * delete by primary key
     *
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert record to table
     *
     * @param record the record
     * @return insert count
     */
    int insert(ProductCommandsResponse record);

    int insertOrUpdate(ProductCommandsResponse record);

    int insertOrUpdateSelective(ProductCommandsResponse record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(ProductCommandsResponse record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    ProductCommandsResponse selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(ProductCommandsResponse record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(ProductCommandsResponse record);

    int updateBatch(List<ProductCommandsResponse> list);

    int updateBatchSelective(List<ProductCommandsResponse> list);

    int batchInsert(@Param("list") List<ProductCommandsResponse> list);

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
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteProductCommandsResponseByIds(Long[] ids);
}
