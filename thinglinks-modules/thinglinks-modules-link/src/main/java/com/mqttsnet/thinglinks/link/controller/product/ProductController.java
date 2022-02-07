package com.mqttsnet.thinglinks.link.controller.product;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.constant.HttpStatus;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * (product)表控制层
 *
 * @author thinglinks
 */
@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    /**
     * 服务对象
     */
    @Resource
    private ProductService productService;

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
     * 新增产品模型
     */
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
    }

    /**
     * @param file
     * @return AjaxResult
     * @description: 导入产品模型json数据
     * @throws:
     * @author: thinglinks
     * @datetime: 2021/3/11 17:08
     */
    @PreAuthorize(hasPermi = "link:product:import")
    @Log(title = "产品模型", businessType = BusinessType.IMPORT)
    @PostMapping("/importProductJson")
    public AjaxResult importProductJson(@RequestParam("file") MultipartFile file) throws Exception {
        AjaxResult ajaxResult = productService.importProductJson(file);
        //存储产品模型原始文件
       /* if (ajaxResult.get(AjaxResult.CODE_TAG).equals(200)){
            minioFileService.upload(file,loginUser.getUsername(), Constants.APPLICATION_OCTET_STREAM);
        }*/
        return ajaxResult;
    }
}
