package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.common.core.utils.SecurityUtils;
import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * (product_services)表控制层
 *
 * @author thinglinks
 */
@RestController
@RequestMapping("product_services")
public class ProductServicesController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProductServicesService productServicesService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public ProductServices selectOne(Long id) {
        return productServicesService.selectByPrimaryKey(id);
    }

    /**
     * 查询产品模型服务列表
     */
    @PreAuthorize(hasPermi = "link:services:list")
    @GetMapping("/list")
    public TableDataInfo list(ProductServices productServices) {
        startPage();
        List<ProductServices> list = productServicesService.selectProductServicesList(productServices);
        return getDataTable(list);
    }

    /**
     * 导出产品模型服务列表
     */
    @PreAuthorize(hasPermi = "link:services:export")
    @Log(title = "产品模型服务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductServices productServices) throws IOException {
        List<ProductServices> list = productServicesService.selectProductServicesList(productServices);
        ExcelUtil<ProductServices> util = new ExcelUtil<ProductServices>(ProductServices.class);
        util.exportExcel(response, list, "产品模型服务数据");
    }

    /**
     * 获取产品模型服务详细信息
     */
    @PreAuthorize(hasPermi = "link:services:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(productServicesService.selectProductServicesById(id));
    }

    /**
     * 新增产品模型服务
     */
    @PreAuthorize(hasPermi = "link:services:add")
    @Log(title = "产品模型服务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductServices productServices) {
        productServices.setCreateBy(SecurityUtils.getUsername());
        return toAjax(productServicesService.insertProductServices(productServices));
    }

    /**
     * 修改产品模型服务
     */
    @PreAuthorize(hasPermi = "link:services:edit")
    @Log(title = "产品模型服务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProductServices productServices) {
        productServices.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(productServicesService.updateProductServices(productServices));
    }

    /**
     * 删除产品模型服务
     */
    @PreAuthorize(hasPermi = "link:services:remove")
    @Log(title = "产品模型服务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productServicesService.deleteProductServicesByIds(ids));
    }
}
