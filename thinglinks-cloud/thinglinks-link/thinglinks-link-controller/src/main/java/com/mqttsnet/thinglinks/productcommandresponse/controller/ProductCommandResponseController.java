package com.mqttsnet.thinglinks.productcommandresponse.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.productcommandresponse.entity.ProductCommandResponse;
import com.mqttsnet.thinglinks.productcommandresponse.service.ProductCommandResponseService;
import com.mqttsnet.thinglinks.productcommandresponse.vo.query.ProductCommandResponsePageQuery;
import com.mqttsnet.thinglinks.productcommandresponse.vo.result.ProductCommandResponseResultVO;
import com.mqttsnet.thinglinks.productcommandresponse.vo.save.ProductCommandResponseSaveVO;
import com.mqttsnet.thinglinks.productcommandresponse.vo.update.ProductCommandResponseUpdateVO;
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
 * 产品模型服务命令属性响应参数
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
@RequestMapping("/productCommandResponse")
@Tag(name = "产品模型设备响应服务命令属性")
public class ProductCommandResponseController extends SuperController<ProductCommandResponseService, Long, ProductCommandResponse, ProductCommandResponseSaveVO,
        ProductCommandResponseUpdateVO, ProductCommandResponsePageQuery, ProductCommandResponseResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<ProductCommandResponse> handlerWrapper(ProductCommandResponse model, PageParams<ProductCommandResponsePageQuery> params) {
        QueryWrap<ProductCommandResponse> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("product_command_response");
        return queryWrap;
    }

    /**
     * 新增 产品模型设备响应服务命令属性信息表
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存产品模型设备响应服务命令属性")
    @PostMapping("/saveProductCommandResponse")
    @WebLog(value = "保存产品模型设备响应服务命令属性", request = false)
    public R saveProductCommandResponse(@Valid @RequestBody ProductCommandResponseSaveVO saveVO) {
        return R.success(superService.saveProductCommandResponse(saveVO));
    }


    /**
     * 修改 产品模型设备响应服务命令属性信息表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改产品模型设备响应服务命令属性")
    @PutMapping("/updateProductCommandResponse")
    @WebLog(value = "修改产品模型设备响应服务命令属性", request = false)
    public R updateProductCommandResponse(@Valid @RequestBody ProductCommandResponseUpdateVO updateVO) {
        return R.success(superService.updateProductCommandResponse(updateVO));
    }

    /**
     * 删除产品模型设备响应服务命令属性
     *
     * @param id 产品模型设备响应服务命令属性ID
     * @return 删除结果
     */
    @Operation(summary = "删除产品模型设备响应服务命令属性", description = "根据产品模型设备响应服务命令属性ID删除产品模型设备响应服务命令属性")
    @Parameters({
            @Parameter(description = "产品模型设备响应服务命令属性ID", required = true)
    })
    @DeleteMapping("/deleteProductCommandResponse/{id}")
    @WebLog(value = "删除产品模型设备响应服务命令属性", request = false)
    public R<Boolean> deleteProductCommandResponse(@PathVariable("id") Long id) {
        log.info("deleteProductCommandResponse id:{}", id);
        return R.success(superService.deleteProductCommandResponse(id));
    }

}


