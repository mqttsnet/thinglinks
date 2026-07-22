package com.mqttsnet.thinglinks.productservice.controller;

import java.util.concurrent.TimeUnit;

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
import com.mqttsnet.thinglinks.common.lock.link.LinkLockKeyBuilder;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.productservice.entity.ProductServices;
import com.mqttsnet.thinglinks.productservice.service.ProductServiceService;
import com.mqttsnet.thinglinks.productservice.vo.query.ProductServicePageQuery;
import com.mqttsnet.thinglinks.productservice.vo.result.ProductServiceResultVO;
import com.mqttsnet.thinglinks.productservice.vo.save.ProductServiceSaveVO;
import com.mqttsnet.thinglinks.productservice.vo.update.ProductServiceUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * 产品模型服务服务表
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
@RequestMapping("/productService")
@Tag(name = "产品模型服务")
public class ProductServiceController extends SuperController<ProductServiceService, Long, ProductServices, ProductServiceSaveVO,
        ProductServiceUpdateVO, ProductServicePageQuery, ProductServiceResultVO> {
    private final EchoService echoService;
    private final DistributedLock distributedLock;

    @Override

    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<ProductServices> handlerWrapper(ProductServices model, PageParams<ProductServicePageQuery> params) {
        QueryWrap<ProductServices> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("product_service");
        return queryWrap;
    }

    /**
     * 新增 产品模型服务信息表
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存产品模型服务")
    @PostMapping("/saveProductService")
    @WebLog(value = "保存产品模型服务", request = false)
    public R<ProductServices> saveProductService(@Valid @RequestBody ProductServiceSaveVO saveVO) {
        try {
            CacheKey lockCacheKey = LinkLockKeyBuilder.forSaveProductServiceByUserId(ContextUtil.getUserId());
            LockRunResult<ProductServices> lockRunResult = distributedLock.tryLockAndRun(
                    lockCacheKey.getKey(),
                    lockCacheKey.getExpire().getSeconds(),
                    TimeUnit.SECONDS,
                    () -> superService.saveProductService(saveVO));
            if (!lockRunResult.isLocked()) {
                return R.fail(R.LOCK_ACQUIRE_ERROR_MESSAGE);
            }
            return R.success(lockRunResult.getResult());
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("产品模型服务保存失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }


    /**
     * 修改 产品模型服务信息表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改产品模型服务")
    @PutMapping("/updateProductService")
    @WebLog(value = "修改产品模型服务", request = false)
    public R<ProductServices> updateProductService(@Valid @RequestBody ProductServiceUpdateVO updateVO) {
        try {
            CacheKey lockCacheKey = LinkLockKeyBuilder.forUpdateProductServiceByUserId(ContextUtil.getUserId());
            LockRunResult<ProductServices> lockRunResult = distributedLock.tryLockAndRun(
                    lockCacheKey.getKey(),
                    lockCacheKey.getExpire().getSeconds(),
                    TimeUnit.SECONDS,
                    () -> superService.updateProductService(updateVO));
            if (!lockRunResult.isLocked()) {
                return R.fail(R.LOCK_ACQUIRE_ERROR_MESSAGE);
            }
            return R.success(lockRunResult.getResult());
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("修改产品模型服务失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 删除产品模型服务
     *
     * @param id 产品模型服务ID
     * @return 删除结果
     */
    @Operation(summary = "删除产品模型服务", description = "根据产品模型服务ID删除产品模型服务")
    @Parameters({
            @Parameter(description = "产品模型服务ID", required = true)
    })
    @DeleteMapping("/deleteProductService/{id}")
    @WebLog(value = "删除产品模型服务", request = false)
    public R<Boolean> deleteProductService(@PathVariable("id") Long id) {
        log.info("deleteProductService id:{}", id);
        return R.success(superService.deleteProductService(id));
    }

}