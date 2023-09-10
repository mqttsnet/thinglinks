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
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsRequests;
import com.mqttsnet.thinglinks.link.service.product.ProductCommandsRequestsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * (product_commands_requests)表控制层
 *
 * @author thinglinks
 */
@RestController
@RequestMapping("/product/commands/requests")
public class ProductCommandsRequestsController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProductCommandsRequestsService productCommandsRequestsService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public ProductCommandsRequests selectOne(Long id) {
        return productCommandsRequestsService.selectByPrimaryKey(id);
    }

    /**
     * 查询命令下发参数列表
     */
    @PreAuthorize(hasPermi = "link:requests:list")
    @GetMapping("/list")
    public TableDataInfo list(ProductCommandsRequests productCommandsRequests) {
        startPage();
        List<ProductCommandsRequests> list = productCommandsRequestsService.selectProductCommandsRequestsList(productCommandsRequests);
        return getDataTable(list);
    }

    /**
     * 导出命令下发参数列表
     */
    @PreAuthorize(hasPermi = "link:requests:export")
    @Log(title = "命令下发参数", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductCommandsRequests productCommandsRequests) throws IOException {
        List<ProductCommandsRequests> list = productCommandsRequestsService.selectProductCommandsRequestsList(productCommandsRequests);
        ExcelUtil<ProductCommandsRequests> util = new ExcelUtil<ProductCommandsRequests>(ProductCommandsRequests.class);
        util.exportExcel(response, list, "命令下发参数数据");
    }

    /**
     * 获取命令下发参数详细信息
     */
    @PreAuthorize(hasPermi = "link:requests:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(productCommandsRequestsService.selectProductCommandsRequestsById(id));
    }

    /**
     * 新增命令下发参数
     */
    @PreAuthorize(hasPermi = "link:requests:add")
    @Log(title = "命令下发参数", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductCommandsRequests productCommandsRequests) {
        productCommandsRequests.setCreateBy(SecurityUtils.getUsername());
        return toAjax(productCommandsRequestsService.insertProductCommandsRequests(productCommandsRequests));
    }

    /**
     * 修改命令下发参数
     */
    @PreAuthorize(hasPermi = "link:requests:edit")
    @Log(title = "命令下发参数", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProductCommandsRequests productCommandsRequests) {
        productCommandsRequests.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(productCommandsRequestsService.updateProductCommandsRequests(productCommandsRequests));
    }

    /**
     * 删除命令下发参数
     */
    @PreAuthorize(hasPermi = "link:requests:remove")
    @Log(title = "命令下发参数", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productCommandsRequestsService.deleteProductCommandsRequestsByIds(ids));
    }

    /**
     * 查询产品模型服务命令下发属性列表
     */
    @GetMapping("/selectAllCommandsRequestsByCommandId/{commandId}")
    public R selectAllCommandsRequestsByCommandId(@PathVariable("commandId") Long commandId) {
        ProductCommandsRequests productCommandsRequests = new ProductCommandsRequests();
        productCommandsRequests.setCommandsId(commandId);
        List<ProductCommandsRequests> list = productCommandsRequestsService.selectProductCommandsRequestsList(productCommandsRequests);
        return R.ok(list);
    }


}
