package com.mqttsnet.thinglinks.product.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.cache.lock.DistributedLock;
import com.mqttsnet.basic.cache.lock.LockRunResult;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.common.lock.link.LinkLockKeyBuilder;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.device.vo.result.ProductOverviewResultVO;
import com.mqttsnet.thinglinks.product.converter.ProductModelConverter;
import com.mqttsnet.thinglinks.product.entity.Product;
import com.mqttsnet.thinglinks.product.service.ProductService;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.query.ProductPageQuery;
import com.mqttsnet.thinglinks.product.vo.result.ProductModelJsonResultVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.product.vo.save.ProductSaveVO;
import com.mqttsnet.thinglinks.product.vo.update.ProductUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 前端控制器
 * 产品模型
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/product")
@Tag(name = "产品模型")
public class ProductController extends SuperController<ProductService, Long, Product, ProductSaveVO,
        ProductUpdateVO, ProductPageQuery, ProductResultVO> {
    /**
     * Jackson 单例 ── 导出 JSON 用,ObjectMapper 初始化代价大必须 static final.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final EchoService echoService;
    private final DistributedLock distributedLock;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    @Override
    public QueryWrap<Product> handlerWrapper(Product model, PageParams<ProductPageQuery> params) {
        QueryWrap<Product> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("product");
        return queryWrap;
    }

    /**
     * 新增 产品模型信息表
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存产品模型")
    @PostMapping("/saveProduct")
    @WebLog(value = "保存产品模型", request = false)
    public R<ProductSaveVO> saveProduct(@RequestBody ProductSaveVO saveVO) {
        try {
            CacheKey lockCacheKey = LinkLockKeyBuilder.forSaveProductByUserId(ContextUtil.getUserId());
            LockRunResult<ProductSaveVO> lockRunResult = distributedLock.tryLockAndRun(
                    lockCacheKey.getKey(),
                    lockCacheKey.getExpire().getSeconds(),
                    TimeUnit.SECONDS,
                    () -> superService.saveProduct(saveVO));
            if (!lockRunResult.isLocked()) {
                return R.fail(R.LOCK_ACQUIRE_ERROR_MESSAGE);
            }
            return R.success(lockRunResult.getResult());
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("产品模型保存失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 快捷生成产品模型
     *
     * @param paramVO 保存参数
     * @return 实体
     */
    @Operation(summary = "快捷生成产品模型")
    @PostMapping("/generateProductJson")
    @WebLog(value = "快捷生成产品模型", request = false)
    public R generateProductJson(@RequestBody ProductParamVO paramVO) {
        superService.generateProductJson(paramVO);
        return R.success();
    }

    /**
     * 修改 产品模型信息表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改产品模型")
    @PutMapping("/updateProduct")
    @WebLog(value = "修改产品模型", request = false)
    public R<ProductUpdateVO> updateProduct(@RequestBody ProductUpdateVO updateVO) {
        try {
            CacheKey lockCacheKey = LinkLockKeyBuilder.forUpdateProductByUserId(ContextUtil.getUserId());
            LockRunResult<ProductUpdateVO> lockRunResult = distributedLock.tryLockAndRun(
                    lockCacheKey.getKey(),
                    lockCacheKey.getExpire().getSeconds(),
                    TimeUnit.SECONDS,
                    () -> superService.updateProduct(updateVO));
            if (!lockRunResult.isLocked()) {
                return R.fail(R.LOCK_ACQUIRE_ERROR_MESSAGE);
            }
            return R.success(lockRunResult.getResult());
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("修改产品模型失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 删除产品模型
     *
     * @param id 产品模型ID
     * @return 删除结果
     */
    @Operation(summary = "删除产品模型", description = "根据产品模型ID删除产品模型")
    @Parameters({
            @Parameter(name = "id", description = "产品模型ID", required = true)
    })
    @DeleteMapping("/deleteProduct/{id}")
    @WebLog(value = "删除产品模型", request = false)
    public R<Boolean> deleteProduct(@PathVariable("id") Long id) {
        log.info("deleteProduct id:{}", id);
        return R.success(superService.deleteProduct(id));
    }

    /**
     * 批量删除产品模型
     *
     * @param ids 产品模型ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除产品模型", description = "根据产品模型ID列表批量删除产品模型")
    @DeleteMapping("/deleteProducts")
    @WebLog(value = "批量删除产品模型", request = false)
    public R<Boolean> deleteProducts(@RequestBody List<Long> ids) {
        log.info("deleteProducts ids:{}", ids);
        boolean allDeleted = ids.stream().distinct().allMatch(id -> superService.deleteProduct(id));
        return R.success(allDeleted);
    }


    /**
     * 查询产品物模型信息
     *
     * @param productIdentification 产品标识
     * @return {@link ProductParamVO} 产品物模型信息
     */
    @Operation(summary = "查询产品物模型信息", description = "查询产品物模型信息(包含可用状态的服务属性、服务命令)")
    @GetMapping(value = "/getFullInfo/{productIdentification}")
    public R<ProductParamVO> getFullInfo(@PathVariable("productIdentification") String productIdentification) {
        log.info("getFullInfo productIdentification:{}", productIdentification);
        ProductParamVO productDetails = superService.selectFullProductByProductIdentification(productIdentification);
        return R.success(productDetails);
    }

    /**
     * 查询多个产品物模型信息
     *
     * @param productIdentifications 产品标识符列表
     * @return 产品详细信息列表（不存在的产品会被跳过）
     */
    @Operation(summary = "查询多个产品物模型信息", description = "查询多个查询产品物模型信息，不存在的产品会被自动跳过")
    @GetMapping(value = "/getFullInfos")
    public R<List<ProductParamVO>> getFullInfos(@RequestParam List<String> productIdentifications) {
        List<ProductParamVO> productDetails = productIdentifications.stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .map(productIdentification -> {
                    try {
                        return superService.selectFullProductByProductIdentification(productIdentification);
                    } catch (Exception e) {
                        log.warn("查询产品失败，跳过该产品 - 产品标识: {}, 错误: {}", productIdentification, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return R.success(productDetails);
    }


    /**
     * 导入产品模型json数据
     *
     * @param file
     * @return
     */
    @Operation(summary = "导入产品模型JSON数据")
    @PostMapping(value = "/importProductJsonFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @WebLog(value = "导入产品模型", request = false)
    public R<?> importProductJsonFile(@RequestPart("file") @Parameter(description = "产品模型JSON文件", required = true, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) MultipartFile file,
                                   @RequestParam("appId") @Parameter(description = "应用ID", required = true) String appId) {
        superService.importProductJson(file, appId);
        return R.success();
    }

    @Operation(
            summary = "导出产品模型JSON",
            description = "根据产品标识导出产品模型的完整配置信息为JSON文件，文件名格式：productModel_产品标识_用户ID_时间戳.json"
    )
    @Parameters({
            @Parameter(name = "productIdentification", description = "产品标识", required = true)
    })
    @GetMapping(value = "/exportJson/{productIdentification}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @WebLog(value = "导出产品模型JSON", request = false)
    public void exportJson(@PathVariable("productIdentification") String productIdentification, HttpServletResponse response) throws Exception {
        // 查询产品详情
        ProductParamVO productDetails = superService.selectFullProductByProductIdentification(productIdentification);
        ProductModelJsonResultVO productModelJsonResultVO = ProductModelConverter.toProductModelJsonResultVO(productDetails);
        // 生成文件名（格式：productModel_产品标识_用户ID_时间戳.json）
        String fileName = String.format("productModel_%s_%s_%s.json", productIdentification, ContextUtil.getUserId(), SnowflakeIdUtil.nextId());
        // 设置响应头
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + fileName);
        // 直接序列化并写入响应流(复用类级单例 OBJECT_MAPPER)
        OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(response.getOutputStream(), productModelJsonResultVO);
        log.info("导出产品模型成功 - 产品标识: {}, 文件名: {}", productIdentification, fileName);
    }


    /**
     * 获取产品概况统计信息
     *
     * @return {@link R<ProductOverviewResultVO>} 产品概况统计信息
     */
    @Operation(summary = "获取产品概况统计信息", description = "统计产品的概况信息")
    @GetMapping("/productOverview")
    public R<ProductOverviewResultVO> getProductOverview() {
        DataScopeHelper.startDataScope("product");
        ProductOverviewResultVO productOverview = superService.getProductOverview();
        return R.success(productOverview);
    }

    /**
     * 初始化产品基础Topic
     *
     * @param productIdentification 产品标识
     * @param reInit                是否重新初始化
     * @return 初始化结果
     */
    @Operation(summary = "初始化产品基础Topic", description = "初始化产品基础Topic，可选择是否重新初始化")
    @Parameters({
            @Parameter(name = "productIdentification", description = "产品标识", required = true),
            @Parameter(name = "reInit", description = "是否重新初始化(true:删除后重新初始化, false:仅初始化未初始化的)")
    })
    @PostMapping("/initProductBaseTopics/{productIdentification}")
    @WebLog(value = "初始化产品基础Topic", request = false)
    public R<Boolean> initProductBaseTopics(
            @PathVariable("productIdentification") String productIdentification,
            @RequestParam(value = "reInit", defaultValue = "false") Boolean reInit) {
        log.info("初始化产品Topic - 产品标识: {}, 重新初始化: {}", productIdentification, reInit);
        Boolean result = superService.initProductBaseTopics(productIdentification, reInit);
        String message = Boolean.TRUE.equals(reInit) ? "重新初始化" : "初始化";
        return R.success(result, message + "产品Topic成功");
    }


}