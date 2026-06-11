package com.mqttsnet.thinglinks.productproperty.controller;

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
import com.mqttsnet.thinglinks.productproperty.entity.ProductProperty;
import com.mqttsnet.thinglinks.productproperty.service.ProductPropertyService;
import com.mqttsnet.thinglinks.productproperty.vo.query.ProductPropertyPageQuery;
import com.mqttsnet.thinglinks.productproperty.vo.result.ProductPropertyResultVO;
import com.mqttsnet.thinglinks.productproperty.vo.save.ProductPropertySaveVO;
import com.mqttsnet.thinglinks.productproperty.vo.update.ProductPropertyUpdateVO;
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
 * 产品模型服务属性表
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
@RequestMapping("/productProperty")
@Tag(name = "产品模型服务属性")
public class ProductPropertyController extends SuperController<ProductPropertyService, Long, ProductProperty, ProductPropertySaveVO,
        ProductPropertyUpdateVO, ProductPropertyPageQuery, ProductPropertyResultVO> {
    private final EchoService echoService;
    private final DistributedLock distributedLock;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<ProductProperty> handlerWrapper(ProductProperty model, PageParams<ProductPropertyPageQuery> params) {
        QueryWrap<ProductProperty> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("product_property");
        return queryWrap;
    }


    /**
     * 新增 产品模型服务属性信息表
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存产品模型服务属性")
    @PostMapping("/saveProductProperty")
    @WebLog(value = "保存产品模型服务属性", request = false)
    public R<ProductProperty> saveProductProperty(@Valid @RequestBody ProductPropertySaveVO saveVO) {
        try {
            CacheKey lockCacheKey = LinkLockKeyBuilder.forSaveProductPropertyByUserId(ContextUtil.getUserId());
            LockRunResult<ProductProperty> lockRunResult = distributedLock.tryLockAndRun(
                    lockCacheKey.getKey(),
                    lockCacheKey.getExpire().getSeconds(),
                    TimeUnit.SECONDS,
                    () -> superService.saveProductProperty(saveVO));
            if (!lockRunResult.isLocked()) {
                return R.fail(R.LOCK_ACQUIRE_ERROR_MESSAGE);
            }
            return R.success(lockRunResult.getResult());
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("产品模型服务属性保存失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }


    /**
     * 修改 产品模型服务属性信息表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改产品模型服务属性")
    @PutMapping("/updateProductProperty")
    @WebLog(value = "修改产品模型服务属性", request = false)
    public R<ProductProperty> updateProductProperty(@Valid @RequestBody ProductPropertyUpdateVO updateVO) {
        try {
            CacheKey lockCacheKey = LinkLockKeyBuilder.forUpdateProductPropertyByUserId(ContextUtil.getUserId());
            LockRunResult<ProductProperty> lockRunResult = distributedLock.tryLockAndRun(
                    lockCacheKey.getKey(),
                    lockCacheKey.getExpire().getSeconds(),
                    TimeUnit.SECONDS,
                    () -> superService.updateProductProperty(updateVO));
            if (!lockRunResult.isLocked()) {
                return R.fail(R.LOCK_ACQUIRE_ERROR_MESSAGE);
            }
            return R.success(lockRunResult.getResult());
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("修改产品模型服务属性失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 删除产品模型服务属性
     *
     * @param id 产品模型服务属性ID
     * @return 删除结果
     */
    @Operation(summary = "删除产品模型服务属性", description = "根据产品模型服务属性ID删除产品模型服务属性")
    @Parameters({
            @Parameter(name = "id", description = "产品模型服务属性ID", required = true, example = "1"),
    })
    @DeleteMapping("/deleteProductProperty/{id}")
    @WebLog(value = "删除产品模型服务属性", request = false)
    public R<Boolean> deleteProductProperty(@PathVariable("id") Long id) {
        log.info("deleteProductProperty id:{}", id);
        return R.success(superService.deleteProductProperty(id));
    }

}