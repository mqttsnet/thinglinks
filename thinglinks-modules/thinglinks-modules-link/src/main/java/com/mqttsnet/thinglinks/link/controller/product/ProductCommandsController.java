package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.utils.SecurityUtils;
import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommands;
import com.mqttsnet.thinglinks.link.service.product.ProductCommandsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * (product_commands)表控制层
 *
 * @author thinglinks
 */
@RestController
@RequestMapping("/productCommands")
public class ProductCommandsController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProductCommandsService productCommandsService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public ProductCommands selectOne(Long id) {
        return productCommandsService.selectByPrimaryKey(id);
    }

    /**
     * 查询产品模型服务命令列表
     */
    @PreAuthorize(hasPermi = "link:commands:list")
    @GetMapping("/list")
    public TableDataInfo list(ProductCommands productCommands) {
        startPage();
        List<ProductCommands> list = productCommandsService.selectProductCommandsList(productCommands);
        return getDataTable(list);
    }

    /**
     * 导出产品模型服务命令列表
     */
    @PreAuthorize(hasPermi = "link:commands:export")
    @Log(title = "产品模型服务命令", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductCommands productCommands) throws IOException {
        List<ProductCommands> list = productCommandsService.selectProductCommandsList(productCommands);
        ExcelUtil<ProductCommands> util = new ExcelUtil<ProductCommands>(ProductCommands.class);
        util.exportExcel(response, list, "产品模型服务命令数据");
    }

    /**
     * 获取产品模型服务命令详细信息
     */
    @PreAuthorize(hasPermi = "link:commands:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(productCommandsService.selectProductCommandsById(id));
    }

    /**
     * 新增产品模型服务命令
     */
    @PreAuthorize(hasPermi = "link:commands:add")
    @Log(title = "产品模型服务命令", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductCommands productCommands) {
        productCommands.setCreateBy(SecurityUtils.getUsername());
        return toAjax(productCommandsService.insertProductCommands(productCommands));
    }

    /**
     * 修改产品模型服务命令
     */
    @PreAuthorize(hasPermi = "link:commands:edit")
    @Log(title = "产品模型服务命令", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProductCommands productCommands) {
        productCommands.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(productCommandsService.updateProductCommands(productCommands));
    }

    /**
     * 删除产品模型服务命令
     */
    @PreAuthorize(hasPermi = "link:commands:remove")
    @Log(title = "产品模型服务命令", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productCommandsService.deleteProductCommandsByIds(ids));
    }
    @PostMapping("/selectProductCommandsByIdList")
    public R<?> selectProductCommandsByIdList(@RequestBody List<Long> commandIdList) {
        return R.ok(productCommandsService.selectProductCommandsByIdList(commandIdList));
    }
}
