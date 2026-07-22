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
import com.mqttsnet.thinglinks.entity.bridge.SubscriptionSource;
import com.mqttsnet.thinglinks.service.bridge.SubscriptionSourceService;
import com.mqttsnet.thinglinks.vo.query.bridge.SubscriptionSourcePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.SubscriptionSourceResultVO;
import com.mqttsnet.thinglinks.vo.save.bridge.SubscriptionSourceSaveVO;
import com.mqttsnet.thinglinks.vo.update.bridge.SubscriptionSourceUpdateVO;
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

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 数据桥接-订阅源前端控制器。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/subscriptionSource")
@Tag(name = "数据桥接-订阅源")
public class SubscriptionSourceController extends SuperController<SubscriptionSourceService, Long, SubscriptionSource,
        SubscriptionSourceSaveVO, SubscriptionSourceUpdateVO, SubscriptionSourcePageQuery, SubscriptionSourceResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<SubscriptionSource> handlerWrapper(SubscriptionSource model,
                                                       PageParams<SubscriptionSourcePageQuery> params) {
        QueryWrap<SubscriptionSource> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("rule_subscription_source");
        return queryWrap;
    }

    /**
     * page 钩子,反填关联数据源(同 DataBridgeController)。
     *
     * @param page 分页结果
     */
    @Override
    public void handlerResult(IPage<SubscriptionSourceResultVO> page) {
        super.handlerResult(page);
        Optional.ofNullable(page).ifPresent(p -> superService.attachDataSourceInfo(p.getRecords()));
    }

    @Operation(summary = "保存订阅源", description = "默认 enable=false,必须手动启用")
    @PostMapping("/saveSubscriptionSource")
    @WebLog(value = "保存订阅源", request = false)
    public R<SubscriptionSourceSaveVO> saveSubscriptionSource(@RequestBody @Validated SubscriptionSourceSaveVO saveVO) {
        return wrap("保存订阅源", () -> superService.saveSubscriptionSource(saveVO));
    }

    @Operation(summary = "修改订阅源", description = "配置变更后 enable 自动重置为 false")
    @PutMapping("/updateSubscriptionSource")
    @WebLog(value = "修改订阅源", request = false)
    public R<SubscriptionSourceUpdateVO> updateSubscriptionSource(@RequestBody @Validated SubscriptionSourceUpdateVO updateVO) {
        return wrap("修改订阅源", () -> superService.updateSubscriptionSource(updateVO));
    }

    @Operation(summary = "订阅源详情")
    @GetMapping("/getSubscriptionSourceDetail/{id}")
    @Parameters({@Parameter(name = "id", description = "订阅源 ID", required = true)})
    public R<SubscriptionSourceResultVO> getSubscriptionSourceDetail(@PathVariable("id") Long id) {
        return wrap("查询订阅源详情 id=" + id, () -> {
            SubscriptionSourceResultVO result = superService.getSubscriptionSourceDetail(id);
            echoService.action(result);
            return result;
        });
    }

    @Operation(summary = "启停订阅源", description = "启用时启动 KafkaConsumer/MqttClient subscribe;禁用时 stop")
    @PutMapping("/changeStatus/{id}")
    @WebLog(value = "启停订阅源", request = false)
    @Parameters({
            @Parameter(name = "id", description = "订阅源 ID", required = true),
            @Parameter(name = "enable", description = "true=启用 / false=禁用", required = true)
    })
    public R<Boolean> changeStatus(@PathVariable("id") Long id, @RequestParam("enable") Boolean enable) {
        return wrap("启停订阅源 id=" + id, () -> superService.changeStatus(id, enable));
    }

    @Operation(summary = "删除订阅源")
    @DeleteMapping("/deleteSubscriptionSource/{id}")
    @WebLog(value = "删除订阅源", request = false)
    @Parameters({@Parameter(name = "id", description = "订阅源 ID", required = true)})
    public R<Boolean> deleteSubscriptionSource(@PathVariable("id") Long id) {
        return wrap("删除订阅源 id=" + id, () -> superService.deleteSubscriptionSource(id));
    }

    /**
     * 统一 try/catch:业务异常返 BizException,其它异常 log + R.fail()。
     *
     * @param opDesc 操作描述,用于失败日志
     * @param action 业务动作
     * @param <T>    返回数据类型
     * @return 统一响应
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
