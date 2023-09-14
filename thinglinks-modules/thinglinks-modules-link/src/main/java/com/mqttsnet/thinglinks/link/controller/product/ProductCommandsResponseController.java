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
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsRequests;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsResponse;
import com.mqttsnet.thinglinks.link.service.product.ProductCommandsResponseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * (product_commands_response)表控制层
 *
 * @author thinglinks
 */
@RestController
@RequestMapping("/product/commands/response")
public class ProductCommandsResponseController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProductCommandsResponseService productCommandsResponseService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public ProductCommandsResponse selectOne(Long id) {
        return productCommandsResponseService.selectByPrimaryKey(id);
    }

    /**
     * 查询命令响应参数列表
     */
    @PreAuthorize(hasPermi = "link:response:list")
    @GetMapping("/list")
    public TableDataInfo list(ProductCommandsResponse productCommandsResponse) {
        startPage();
        List<ProductCommandsResponse> list = productCommandsResponseService.selectProductCommandsResponseList(productCommandsResponse);
        return getDataTable(list);
    }

    /**
     * 导出命令响应参数列表
     */
    @PreAuthorize(hasPermi = "link:response:export")
    @Log(title = "命令响应参数", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductCommandsResponse productCommandsResponse) throws IOException {
        List<ProductCommandsResponse> list = productCommandsResponseService.selectProductCommandsResponseList(productCommandsResponse);
        ExcelUtil<ProductCommandsResponse> util = new ExcelUtil<ProductCommandsResponse>(ProductCommandsResponse.class);
        util.exportExcel(response, list, "命令响应参数数据");
    }

    /**
     * 获取命令响应参数详细信息
     */
    @PreAuthorize(hasPermi = "link:response:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(productCommandsResponseService.selectProductCommandsResponseById(id));
    }

    /**
     * 新增命令响应参数
     */
    @PreAuthorize(hasPermi = "link:response:add")
    @Log(title = "命令响应参数", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductCommandsResponse productCommandsResponse)  throws IOException {
        productCommandsResponse.setCreateBy(SecurityUtils.getUsername());
        return toAjax(productCommandsResponseService.insertProductCommandsResponse(productCommandsResponse));
    }

    /**
     * 修改命令响应参数
     */
    @PreAuthorize(hasPermi = "link:response:edit")
    @Log(title = "命令响应参数", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProductCommandsResponse productCommandsResponse) {
        productCommandsResponse.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(productCommandsResponseService.updateProductCommandsResponse(productCommandsResponse));
    }

    /**
     * 删除命令响应参数
     */
    @PreAuthorize(hasPermi = "link:response:remove")
    @Log(title = "命令响应参数", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productCommandsResponseService.deleteProductCommandsResponseByIds(ids));
    }



}
