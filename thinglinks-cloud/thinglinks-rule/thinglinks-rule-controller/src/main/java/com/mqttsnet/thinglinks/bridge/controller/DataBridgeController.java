package com.mqttsnet.thinglinks.bridge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.entity.bridge.DataBridge;
import com.mqttsnet.thinglinks.service.bridge.DataBridgeService;
import com.mqttsnet.thinglinks.vo.query.bridge.DataBridgePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.DataBridgeResultVO;
import com.mqttsnet.thinglinks.vo.save.bridge.DataBridgeSaveVO;
import com.mqttsnet.thinglinks.vo.update.bridge.DataBridgeUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 数据桥接-规则前端控制器。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/dataBridge")
@Tag(name = "数据桥接-规则")
public class DataBridgeController extends SuperController<DataBridgeService, Long, DataBridge,
        DataBridgeSaveVO, DataBridgeUpdateVO, DataBridgePageQuery, DataBridgeResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<DataBridge> handlerWrapper(DataBridge model, PageParams<DataBridgePageQuery> params) {
        QueryWrap<DataBridge> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("rule_data_bridge");
        return queryWrap;
    }

    /**
     * page 钩子 ── PageController 默认 page 走 BeanPlusUtil.toBeanPage 直转,不经
     * service.getDataBridgeResultVOList,dataSourceCode/Name 默认空。先调父类做字典回显,再 attach。
     */
    @Override
    public void handlerResult(IPage<DataBridgeResultVO> page) {
        super.handlerResult(page);
        Optional.ofNullable(page).ifPresent(p -> superService.attachDataSourceInfo(p.getRecords()));
    }

    @Operation(summary = "保存桥接规则", description = "默认 enable=false,必须测试发送成功后手动启用")
    @PostMapping("/saveDataBridge")
    @WebLog(value = "保存桥接规则", request = false)
    public R<DataBridgeSaveVO> saveDataBridge(@RequestBody @Validated DataBridgeSaveVO saveVO) {
        return wrap("保存桥接规则", () -> superService.saveDataBridge(saveVO));
    }

    @Operation(summary = "修改桥接规则", description = "配置变更后 enable 自动重置为 false")
    @PutMapping("/updateDataBridge")
    @WebLog(value = "修改桥接规则", request = false)
    public R<DataBridgeUpdateVO> updateDataBridge(@RequestBody @Validated DataBridgeUpdateVO updateVO) {
        return wrap("修改桥接规则", () -> superService.updateDataBridge(updateVO));
    }

    /**
     * 桥接规则详情(含 actionConfigJson 明文,编辑表单回显用)。
     * 独立 URL 避免与 SuperController 的 GetMapping("/detail") 因 generic 桥接方法冲突误路由。
     */
    @Operation(summary = "桥接规则详情")
    @GetMapping("/getDataBridgeDetail/{id}")
    @Parameters({@Parameter(name = "id", description = "桥接规则 ID", required = true)})
    public R<DataBridgeResultVO> getDataBridgeDetail(@PathVariable("id") Long id) {
        return wrap("查询桥接规则详情 id=" + id, () -> {
            DataBridgeResultVO result = superService.getDataBridgeDetail(id);
            echoService.action(result);
            return result;
        });
    }

    @Operation(summary = "测试发送", description = "调 Sink.send 实际发送一次样例消息到下游")
    @PostMapping("/testSink/{id}")
    @WebLog(value = "测试发送", request = false)
    @Parameters({@Parameter(name = "id", description = "桥接规则 ID", required = true)})
    public R<Map<String, Object>> testSink(@PathVariable("id") Long id,
                                           @RequestBody Map<String, Object> sampleEnvelope) {
        return wrap("测试发送 id=" + id, () -> superService.testSink(id, sampleEnvelope));
    }

    @Operation(summary = "启停桥接规则", description = "启用前要求关联数据源已启用")
    @PutMapping("/changeStatus/{id}")
    @WebLog(value = "启停桥接规则", request = false)
    @Parameters({
            @Parameter(name = "id", description = "桥接规则 ID", required = true),
            @Parameter(name = "enable", description = "true=启用 / false=禁用", required = true)
    })
    public R<Boolean> changeStatus(@PathVariable("id") Long id, @RequestParam("enable") Boolean enable) {
        return wrap("启停桥接规则 id=" + id, () -> superService.changeStatus(id, enable));
    }

    @Operation(summary = "复制规则", description = "同模板新建一条 disabled 规则")
    @PostMapping("/copy/{id}")
    @WebLog(value = "复制桥接规则", request = false)
    @Parameters({@Parameter(name = "id", description = "源规则 ID", required = true)})
    public R<Long> copyRule(@PathVariable("id") Long id) {
        return wrap("复制桥接规则 id=" + id, () -> superService.copyRule(id));
    }

    @Operation(summary = "删除桥接规则")
    @DeleteMapping("/deleteDataBridge/{id}")
    @WebLog(value = "删除桥接规则", request = false)
    @Parameters({@Parameter(name = "id", description = "桥接规则 ID", required = true)})
    public R<Boolean> deleteDataBridge(@PathVariable("id") Long id) {
        return wrap("删除桥接规则 id=" + id, () -> superService.deleteDataBridge(id));
    }

    /**
     * 统一 try/catch 包装:业务异常返 BizException,其它异常 log + R.fail()。
     */
    private <T> R<T> wrap(String opDesc, Supplier<T> action) {
        try {
            return R.success(action.get());
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("{} 失败: {}", opDesc, e.getMessage(), e);
            return R.fail();
        }
    }
}
