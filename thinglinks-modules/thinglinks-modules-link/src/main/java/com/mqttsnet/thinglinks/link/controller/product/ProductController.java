package com.mqttsnet.thinglinks.link.controller.product;

import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.annotation.NoRepeatSubmit;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.model.ProductModel;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.system.api.RemoteFileService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    @Resource
    private RemoteFileService remoteFileService;
    @Autowired
    private RedisService redisService;

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

    /**
     * 通过主产品标识查询产品
     *
     * @param productIdentification 产品标识
     * @return 单条数据
     */
    @GetMapping("/selectByProductIdentification/{productIdentification}")
    public R<?> selectByProductIdentification(@PathVariable(value = "productIdentification") String productIdentification) {
        return R.ok(productService.selectByProductIdentification(productIdentification));
    }

    /**
     * 导入产品模型json数据
     *
     * @param file                   json文件
     * @param updateSupport          是否更新已经存在的产品模型数据
     * @param appId                  应用ID
     * @param templateIdentification 产品模型模板标识
     * @param status                 状态(字典值：启用  停用)
     * @return AjaxResult
     * @throws Exception
     */
    @PreAuthorize(hasPermi = "link:product:import")
    @Log(title = "产品管理", businessType = BusinessType.IMPORT)
    @PostMapping("/importProductJsonFile")
    public AjaxResult importProductJson(MultipartFile file,
                                        Boolean updateSupport,
                                        String appId,
                                        String templateIdentification,
                                        String status
    ) throws Exception {
        AjaxResult ajaxResult = productService.importProductJson(file, updateSupport, appId, templateIdentification, status);
        //存储产品模型原始文件
//        final R<SysFile> uploadMessage = remoteFileService.upload(file);
        return ajaxResult;
    }

    /**
     * 查询产品管理列表
     */
    @PreAuthorize(hasPermi = "link:product:list")
    @GetMapping("/list")
    public TableDataInfo list(Product product) {
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
    public void export(HttpServletResponse response, Product product) throws IOException {
        List<Product> list = productService.selectProductList(product);
        ExcelUtil<Product> util = new ExcelUtil<Product>(Product.class);
        util.exportExcel(response, list, "产品管理数据");
    }

    /**
     * 获取产品管理详细信息
     */
    @PreAuthorize(hasPermi = "link:product:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(productService.selectProductById(id));
    }

    /**
     * 获取产品管理详细信息
     */
    @PreAuthorize(hasPermi = "link:product:query")
    @GetMapping(value = "/getFullInfo/{id}")
    public AjaxResult getFullInfo(@PathVariable("id") Long id) {
        ProductModel productModel = productService.selectFullProductById(id);
        return AjaxResult.success(productModel);
    }

    /**
     * 新增产品管理
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "link:product:add")
    @Log(title = "产品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Product product) {

        int row = productService.insertProduct(product);

        if (row == -1) {
            return AjaxResult.error("产品名称已经存在！");
        }

        return toAjax(row);
    }

    /**
     * 修改产品管理
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "link:product:edit")
    @Log(title = "产品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Product product) {
        return toAjax(productService.updateProduct(product));
    }

    /**
     * 删除产品管理
     */
    @PreAuthorize(hasPermi = "link:product:remove")
    @Log(title = "产品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productService.deleteProductByIds(ids));
    }


    /**
     * 校验产品名称是否存在
     *
     * @param productName
     * @return
     */
    @GetMapping(value = "/validationFindOneByProductName/{productName}")
    public AjaxResult validationFindOneByProductName(@PathVariable("productName") String productName) {
        Product oneByProductName = productService.findOneByProductName(productName);
        if (StringUtils.isNull(oneByProductName)) {
            AjaxResult.success("产品名称可用");
        }
        return AjaxResult.error("产品名称已存在");
    }

    /**
     * 快捷生成产品模型json数据
     *
     * @param params (content 模型json数据、appId 应用ID、templateIdentification  产品模型模板标识、status 状态(字典值：启用  停用))
     * @return AjaxResult
     * @throws Exception
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "link:product:generate")
    @Log(title = "产品管理", businessType = BusinessType.INSERT)
    @PostMapping("/generateProductJson")
    public AjaxResult generateProductJson(@RequestBody Map<String, Object> params) throws Exception {
        final Object content = params.get("content");
        final Object appId = params.get("appId");
        final Object templateIdentification = params.get("templateIdentification");
        final Object status = params.get("status");
        AjaxResult ajaxResult = productService.productJsonDataAnalysis(JSONObject.parseObject(content.toString()), appId.toString(), templateIdentification.toString(), status.toString());
        return ajaxResult;
    }


    /**
     * 获取所有产品
     *
     * @param status 状态
     * @return 列表数据
     */
    @GetMapping("/selectAllProduct/{status}")
    public R<?> selectAllProductByStatus(@PathVariable(value = "status") String status) {
        return R.ok(productService.selectAllProductByStatus(status));
    }

    @PostMapping("/selectProductByProductIdentificationList")
    public R<?> selectProductByProductIdentificationList(@RequestBody List<String> productIdentificationList) {
        return R.ok(productService.selectProductByProductIdentificationList(productIdentificationList));
    }


//    @PreAuthorize(hasPermi = "link:product:empowerment")
    @ApiOperation(value = "产品赋能", httpMethod = "GET", notes = "产品赋能")
    @Log(title = "产品管理", businessType = BusinessType.OTHER)
    @GetMapping(value = "/productEmpowerment/{productIds}")
    public AjaxResult productEmpowerment(@PathVariable("productIds") Long[] productIds) {
        try {
            return AjaxResult.success(productService.productEmpowerment(productIds));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return AjaxResult.error("产品赋能异常,请联系管理员");
    }

}
