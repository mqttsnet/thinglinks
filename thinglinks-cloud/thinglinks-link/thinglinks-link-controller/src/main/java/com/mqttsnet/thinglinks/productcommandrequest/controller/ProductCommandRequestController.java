package com.mqttsnet.thinglinks.productcommandrequest.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.productcommandrequest.entity.ProductCommandRequest;
import com.mqttsnet.thinglinks.productcommandrequest.service.ProductCommandRequestService;
import com.mqttsnet.thinglinks.productcommandrequest.vo.query.ProductCommandRequestPageQuery;
import com.mqttsnet.thinglinks.productcommandrequest.vo.result.ProductCommandRequestResultVO;
import com.mqttsnet.thinglinks.productcommandrequest.vo.save.ProductCommandRequestSaveVO;
import com.mqttsnet.thinglinks.productcommandrequest.vo.update.ProductCommandRequestUpdateVO;
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
 * 产品模型服务命令属性请求参数
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
@RequestMapping("/productCommandRequest")
@Tag(name = "产品模型设备下发服务命令属性")
public class ProductCommandRequestController extends SuperController<ProductCommandRequestService, Long, ProductCommandRequest, ProductCommandRequestSaveVO,
        ProductCommandRequestUpdateVO, ProductCommandRequestPageQuery, ProductCommandRequestResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    /**
     * 新增 产品模型设备下发服务命令属性信息表
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存产品模型设备下发服务命令属性")
    @PostMapping("/saveProductCommandRequest")
    @WebLog(value = "保存产品模型设备下发服务命令属性", request = false)
    public R saveProductCommandRequest(@Valid @RequestBody ProductCommandRequestSaveVO saveVO) {
        return R.success(superService.saveProductCommandRequest(saveVO));
    }


    /**
     * 修改 产品模型设备下发服务命令属性信息表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改产品模型设备下发服务命令属性")
    @PutMapping("/updateProductCommandRequest")
    @WebLog(value = "修改产品模型设备下发服务命令属性", request = false)
    public R updateProductCommandRequest(@Valid @RequestBody ProductCommandRequestUpdateVO updateVO) {
        return R.success(superService.updateProductCommandRequest(updateVO));
    }

    /**
     * 删除产品模型设备下发服务命令属性
     *
     * @param id 产品模型设备下发服务命令属性ID
     * @return 删除结果
     */
    @Operation(summary = "删除产品模型设备下发服务命令属性", description = "根据产品模型设备下发服务命令属性ID删除产品模型设备下发服务命令属性")
    @Parameters({
            @Parameter(description = "产品模型设备下发服务命令属性ID", required = true)
    })
    @DeleteMapping("/deleteProductCommandRequest/{id}")
    @WebLog(value = "删除产品模型设备下发服务命令属性", request = false)
    public R<Boolean> deleteProductCommandRequest(@PathVariable("id") Long id) {
        log.info("deleteProductCommandRequest id:{}", id);
        return R.success(superService.deleteProductCommandRequest(id));
    }

}


