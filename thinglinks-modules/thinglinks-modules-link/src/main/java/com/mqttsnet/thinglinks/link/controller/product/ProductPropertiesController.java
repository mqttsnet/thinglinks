package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.common.core.utils.SecurityUtils;
import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
import com.mqttsnet.thinglinks.link.service.product.ProductPropertiesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
* (product_properties)表控制层
*
* @author thinglinks
*/
@RestController
@RequestMapping("product_properties")
public class ProductPropertiesController extends BaseController {
/**
* 服务对象
*/
@Resource
private ProductPropertiesService productPropertiesService;

/**
* 通过主键查询单条数据
*
* @param id 主键
* @return 单条数据
*/
@GetMapping("selectOne")
public ProductProperties selectOne(Long id) {
return productPropertiesService.selectByPrimaryKey(id);
}

    /**
     * 查询产品模型服务属性列表
     */
    @PreAuthorize(hasPermi = "link:properties:list")
    @GetMapping("/list")
    public TableDataInfo list(ProductProperties productProperties) {
        startPage();
        List<ProductProperties> list = productPropertiesService.selectProductPropertiesList(productProperties);
        return getDataTable(list);
    }

    /**
     * 导出产品模型服务属性列表
     */
    @PreAuthorize(hasPermi = "link:properties:export")
    @Log(title = "产品模型服务属性", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductProperties productProperties) throws IOException {
        List<ProductProperties> list = productPropertiesService.selectProductPropertiesList(productProperties);
        ExcelUtil<ProductProperties> util = new ExcelUtil<ProductProperties>(ProductProperties.class);
        util.exportExcel(response, list, "产品模型服务属性数据");
    }

    /**
     * 获取产品模型服务属性详细信息
     */
    @PreAuthorize(hasPermi = "link:properties:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(productPropertiesService.selectProductPropertiesById(id));
    }

    /**
     * 新增产品模型服务属性
     */
    @PreAuthorize(hasPermi = "link:properties:add")
    @Log(title = "产品模型服务属性", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductProperties productProperties) {
        productProperties.setCreateBy(SecurityUtils.getUsername());
        return toAjax(productPropertiesService.insertProductProperties(productProperties));
    }

    /**
     * 修改产品模型服务属性
     */
    @PreAuthorize(hasPermi = "link:properties:edit")
    @Log(title = "产品模型服务属性", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProductProperties productProperties) {
        productProperties.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(productPropertiesService.updateProductProperties(productProperties));
    }

    /**
     * 删除产品模型服务属性
     */
    @PreAuthorize(hasPermi = "link:properties:remove")
    @Log(title = "产品模型服务属性", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productPropertiesService.deleteProductPropertiesByIds(ids));
    }
}
