package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.common.core.annotation.NoRepeatSubmit;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.system.api.RemoteFileService;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * (product)产品表控制层
 *
 * @author thinglinks
 */
@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProductService productService;
    @Autowired
    private RemoteFileService remoteFileService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/selectOne")
    public Product selectOne(Long id) {
        return productService.selectByPrimaryKey(id);
    }

   /* *//**
     * 新增产品模型
     *//*
    @PreAuthorize(hasPermi = "link:product:add")
    @Log(title = "产品模型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JSONObject content) {
        JSONObject tokenObj = new JSONObject();
        try {
//            AjaxResult ajaxResult = productService.insert(content);
            return null;
        } catch (JSONException e) {
            return new AjaxResult(HttpStatus.ERROR, "文件数据的json格式错误", tokenObj);
        } catch (Exception e) {
            log.error("新增产品模型异常：", e);
            return new AjaxResult(HttpStatus.ERROR, "快捷生成失败", tokenObj);
        }
    }*/

    /**
     * 导入产品模型json数据
     * @param file json文件
     * @param updateSupport 是否更新已经存在的产品模型数据
     * @param appId 应用ID
     * @param templateId  产品模型模板ID
     * @param status 状态(字典值：启用  停用)
     * @return AjaxResult
     * @throws Exception
     */
//    @PreAuthorize(hasPermi = "link:product:import")
    @Log(title = "产品管理", businessType = BusinessType.IMPORT)
    @PostMapping("/importProductJsonFile")
    public AjaxResult importProductJson(MultipartFile file,
                                        Boolean updateSupport,
                                        String appId,
                                        String templateId,
                                        String status
    ) throws Exception {
        AjaxResult ajaxResult = productService.importProductJson(file,updateSupport,appId,templateId,status);
        //存储产品模型原始文件
//        final R<SysFile> uploadMessage = remoteFileService.upload(file);
        return ajaxResult;
    }

    /**
     * 查询产品管理列表
     */
    @PreAuthorize(hasPermi = "link:product:list")
    @GetMapping("/list")
    public TableDataInfo list(Product product)
    {
        startPage();
        List<Product> list = productService.selectProductList(product);
        return getDataTable(list);
    }

    /**
     * 导出产品管理列表
     */
    @PreAuthorize(hasPermi = "link:product:export")
    @Log(title = "产品管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Product product) throws IOException
    {
        List<Product> list = productService.selectProductList(product);
        ExcelUtil<Product> util = new ExcelUtil<Product>(Product.class);
        util.exportExcel(response, list, "产品管理数据");
    }

    /**
     * 获取产品管理详细信息
     */
    @PreAuthorize(hasPermi = "link:product:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(productService.selectProductById(id));
    }

    /**
     * 新增产品管理
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "link:product:add")
    @Log(title = "产品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Product product)
    {
        return toAjax(productService.insertProduct(product));
    }

    /**
     * 修改产品管理
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "link:product:edit")
    @Log(title = "产品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Product product)
    {
        return toAjax(productService.updateProduct(product));
    }

    /**
     * 删除产品管理
     */
    @PreAuthorize(hasPermi = "link:product:remove")
    @Log(title = "产品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(productService.deleteProductByIds(ids));
    }


    /**
     *校验产品名称是否存在
     * @param productName
     * @return
     */
    @GetMapping(value = "/validationFindOneByProductName/{productName}")
    public AjaxResult validationFindOneByProductName(@PathVariable("productName") String productName)
    {
        Product oneByProductName = productService.findOneByProductName(productName);
        if (StringUtils.isNull(oneByProductName)){
            AjaxResult.success("产品名称可用");
        }
        return AjaxResult.error("产品名称已存在");
    }

    /**
     * 获取超级表模型
     * @return
     */
    @GetMapping(value = "/findCreateSuperTableDataModel")
    public AjaxResult findCreateSuperTableDataModel()
    {
        try {
            final List<SuperTableDto> superTableDataModel = productService.createSuperTableDataModel();
            return AjaxResult.success(superTableDataModel);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return AjaxResult.error("产品数据异常,请联系管理员");
    }
}
