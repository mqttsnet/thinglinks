package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.common.core.utils.SecurityUtils;
import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductTemplate;
import com.mqttsnet.thinglinks.link.service.product.ProductTemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * (productTemplate)表控制层
 *
 * @author thinglinks
 */
@RestController
@RequestMapping("/productTemplate")
public class ProductTemplateController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProductTemplateService productTemplateService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public ProductTemplate selectOne(Long id) {
        return productTemplateService.selectByPrimaryKey(id);
    }

    /**
     * 查询产品模板列表
     */
    @PreAuthorize(hasPermi = "link:productTemplate:list")
    @GetMapping("/list")
    public TableDataInfo list(ProductTemplate productTemplate) {
        startPage();
        List<ProductTemplate> list = productTemplateService.selectProductTemplateList(productTemplate);
        return getDataTable(list);
    }

    /**
     * 查询产品模板列表
     */
    @PreAuthorize(hasPermi = "link:productTemplate:list")
    @GetMapping("/query")
    public AjaxResult query(ProductTemplate productTemplate) {
        List<ProductTemplate> list = productTemplateService.selectProductTemplateList(productTemplate);
        return AjaxResult.success(list);
    }

    /**
     * 导出产品模板列表
     */
    @PreAuthorize(hasPermi = "link:productTemplate:export")
    @Log(title = "产品模板", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductTemplate productTemplate) throws IOException {
        List<ProductTemplate> list = productTemplateService.selectProductTemplateList(productTemplate);
        ExcelUtil<ProductTemplate> util = new ExcelUtil<ProductTemplate>(ProductTemplate.class);
        util.exportExcel(response, list, "产品模板数据");
    }

    /**
     * 获取产品模板详细信息
     */
    @PreAuthorize(hasPermi = "link:productTemplate:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(productTemplateService.selectProductTemplateById(id));
    }

    /**
     * 获取产品模板详细信息 带服务、属性
     */
    @PreAuthorize(hasPermi = "link:productTemplate:query")
    @GetMapping(value = "/getFull/{id}")
    public AjaxResult getFullInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(productTemplateService.selectFullProductTemplateById(id));
    }

    /**
     * 新增产品模板
     */
    @PreAuthorize(hasPermi = "link:productTemplate:add")
    @Log(title = "产品模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductTemplate productTemplate) {
        productTemplate.setCreateBy(SecurityUtils.getUsername());
        return toAjax(productTemplateService.insertProductTemplate(productTemplate));
    }

    /**
     * 修改产品模板
     */
    @PreAuthorize(hasPermi = "link:productTemplate:edit")
    @Log(title = "产品模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProductTemplate productTemplate) {
        productTemplate.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(productTemplateService.updateProductTemplate(productTemplate));
    }

    /**
     * 删除产品模板
     */
    @PreAuthorize(hasPermi = "link:productTemplate:remove")
    @Log(title = "产品模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productTemplateService.deleteProductTemplateByIds(ids));
    }
}
