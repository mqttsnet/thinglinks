package com.mqttsnet.thinglinks.productcommand.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.productcommand.entity.ProductCommand;
import com.mqttsnet.thinglinks.productcommand.service.ProductCommandService;
import com.mqttsnet.thinglinks.productcommand.vo.query.ProductCommandPageQuery;
import com.mqttsnet.thinglinks.productcommand.vo.result.ProductCommandResultVO;
import com.mqttsnet.thinglinks.productcommand.vo.save.ProductCommandSaveVO;
import com.mqttsnet.thinglinks.productcommand.vo.update.ProductCommandUpdateVO;
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
 * 产品模型设备服务命令表
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
@RequestMapping("/productCommand")
@Tag(name = "产品模型设备服务命令")
public class ProductCommandController extends SuperController<ProductCommandService, Long, ProductCommand, ProductCommandSaveVO,
        ProductCommandUpdateVO, ProductCommandPageQuery, ProductCommandResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<ProductCommand> handlerWrapper(ProductCommand model, PageParams<ProductCommandPageQuery> params) {
        QueryWrap<ProductCommand> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("product_command");
        return queryWrap;
    }

    /**
     * 新增 产品模型设备服务命令信息表
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存产品模型设备服务命令")
    @PostMapping("/saveProductCommand")
    @WebLog(value = "保存产品模型设备服务命令", request = false)
    public R saveProductCommand(@Valid @RequestBody ProductCommandSaveVO saveVO) {
        return R.success(superService.saveProductCommand(saveVO));
    }


    /**
     * 修改 产品模型设备服务命令信息表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改产品模型设备服务命令")
    @PutMapping("/updateProductCommand")
    @WebLog(value = "修改产品模型设备服务命令", request = false)
    public R updateProductCommand(@Valid @RequestBody ProductCommandUpdateVO updateVO) {
        return R.success(superService.updateProductCommand(updateVO));
    }

    /**
     * 删除产品模型设备服务命令
     *
     * @param id 产品模型设备服务命令ID
     * @return 删除结果
     */
    @Operation(summary = "删除产品模型设备服务命令", description = "根据产品模型设备服务命令ID删除产品模型设备服务命令")
    @Parameters({
            @Parameter(name = "id", description = "产品模型设备服务命令ID", required = true)
    })
    @DeleteMapping("/deleteProductCommand/{id}")
    @WebLog(value = "删除产品模型设备服务命令", request = false)
    public R<Boolean> deleteProductCommand(@PathVariable("id") Long id) {
        log.info("deleteProductCommand id:{}", id);
        return R.success(superService.deleteProductCommand(id));
    }

}


