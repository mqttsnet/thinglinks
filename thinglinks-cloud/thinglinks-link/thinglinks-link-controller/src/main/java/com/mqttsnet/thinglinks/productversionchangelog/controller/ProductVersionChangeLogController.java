package com.mqttsnet.thinglinks.productversionchangelog.controller;

import java.util.List;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.productversionchangelog.entity.ProductVersionChangeLog;
import com.mqttsnet.thinglinks.productversionchangelog.service.ProductVersionChangeLogService;
import com.mqttsnet.thinglinks.productversionchangelog.vo.query.ProductVersionChangeLogPageQuery;
import com.mqttsnet.thinglinks.productversionchangelog.vo.result.ProductVersionChangeLogResultVO;
import com.mqttsnet.thinglinks.productversionchangelog.vo.save.ProductVersionChangeLogSaveVO;
import com.mqttsnet.thinglinks.productversionchangelog.vo.update.ProductVersionChangeLogUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品物模型版本变更日志前端控制器,对外主要用 /page 分页查询(前端变更历史时间线)。
 * 变更日志为 append-only 审计流水,写入由 {@code ProductVersionService.upsertDraft} 内部触发,基类 save / update / delete 不在业务上使用。
 *
 * @author mqttsnet
 * @see ProductVersionChangeLogService
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/productVersionChangeLog")
@Tag(name = "产品物模型版本变更日志")
public class ProductVersionChangeLogController extends SuperController<ProductVersionChangeLogService, Long, ProductVersionChangeLog,
    ProductVersionChangeLogSaveVO, ProductVersionChangeLogUpdateVO, ProductVersionChangeLogPageQuery, ProductVersionChangeLogResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    /**
     * 按产品标识查询全部变更日志(created_time 倒序)。
     */
    @Operation(summary = "按产品查询变更日志")
    @GetMapping("/listByProduct")
    public R<List<ProductVersionChangeLogResultVO>> listByProduct(@RequestParam String productIdentification) {
        List<ProductVersionChangeLog> list = superService.listByProductIdentification(productIdentification);
        List<ProductVersionChangeLogResultVO> result =
            BeanPlusUtil.toBeanList(list, ProductVersionChangeLogResultVO.class);
        echoService.action(result);
        return R.success(result);
    }
}
