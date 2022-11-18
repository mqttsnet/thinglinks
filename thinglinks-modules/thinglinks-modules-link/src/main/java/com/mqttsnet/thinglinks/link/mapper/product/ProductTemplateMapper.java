package com.mqttsnet.thinglinks.link.mapper.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.link.mapper.product
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-11-18 20:36
 **/
@Mapper
public interface ProductTemplateMapper {
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
    int insert(ProductTemplate record);

    int insertOrUpdate(ProductTemplate record);

    int insertOrUpdateSelective(ProductTemplate record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(ProductTemplate record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    ProductTemplate selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(ProductTemplate record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(ProductTemplate record);

    int updateBatch(List<ProductTemplate> list);

    int updateBatchSelective(List<ProductTemplate> list);

    int batchInsert(@Param("list") List<ProductTemplate> list);

    /**
     * 查询产品模板
     *
     * @param id 产品模板主键
     * @return 产品模板
     */
    ProductTemplate selectProductTemplateById(Long id);

    /**
     * 查询产品模板列表
     *
     * @param productTemplate 产品模板
     * @return 产品模板集合
     */
    List<ProductTemplate> selectProductTemplateList(ProductTemplate productTemplate);

    /**
     * 新增产品模板
     *
     * @param productTemplate 产品模板
     * @return 结果
     */
    int insertProductTemplate(ProductTemplate productTemplate);

    /**
     * 修改产品模板
     *
     * @param productTemplate 产品模板
     * @return 结果
     */
    int updateProductTemplate(ProductTemplate productTemplate);

    /**
     * 批量删除产品模板
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteProductTemplateByIds(Long[] ids);
}