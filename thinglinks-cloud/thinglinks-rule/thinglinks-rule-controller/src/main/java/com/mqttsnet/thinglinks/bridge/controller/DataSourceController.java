package com.mqttsnet.thinglinks.bridge.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.entity.bridge.DataSource;
import com.mqttsnet.thinglinks.service.bridge.DataSourceService;
import com.mqttsnet.thinglinks.vo.query.bridge.DataSourcePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.DataSourceResultVO;
import com.mqttsnet.thinglinks.vo.save.bridge.DataSourceSaveVO;
import com.mqttsnet.thinglinks.vo.update.bridge.DataSourceUpdateVO;
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

import java.util.function.Supplier;

/**
 * 数据桥接-数据源前端控制器。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/dataSource")
@Tag(name = "数据桥接-数据源")
public class DataSourceController extends SuperController<DataSourceService, Long, DataSource,
        DataSourceSaveVO, DataSourceUpdateVO, DataSourcePageQuery, DataSourceResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<DataSource> handlerWrapper(DataSource model, PageParams<DataSourcePageQuery> params) {
        QueryWrap<DataSource> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("rule_data_source");
        return queryWrap;
    }

    @Operation(summary = "保存数据源", description = "默认 enable=false,必须测试连接成功后手动启用")
    @PostMapping("/saveDataSource")
    @WebLog(value = "保存数据源", request = false)
    public R<DataSourceSaveVO> saveDataSource(@RequestBody @Validated DataSourceSaveVO saveVO) {
        return wrap("保存数据源", () -> superService.saveDataSource(saveVO));
    }

    @Operation(summary = "修改数据源", description = "配置变更后 enable 自动重置为 false,需重新测试启用")
    @PutMapping("/updateDataSource")
    @WebLog(value = "修改数据源", request = false)
    public R<DataSourceUpdateVO> updateDataSource(@RequestBody @Validated DataSourceUpdateVO updateVO) {
        return wrap("修改数据源", () -> superService.updateDataSource(updateVO));
    }

    @Operation(summary = "数据源详情", description = "返回完整字段含敏感字段明文(编辑表单回填用)")
    @GetMapping("/getDataSourceDetail/{id}")
    @Parameters({@Parameter(name = "id", description = "数据源 ID", required = true)})
    public R<DataSourceResultVO> getDataSourceDetail(@PathVariable("id") Long id) {
        return wrap("查询数据源详情 id=" + id, () -> {
            DataSourceResultVO result = superService.getDataSourceDetail(id);
            echoService.action(result);
            return result;
        });
    }

    @Operation(summary = "测试连接", description = "基于 DB 已保存的数据源调 Sink/Source.testConnection")
    @PostMapping("/testConnection/{id}")
    @WebLog(value = "测试连接", request = false)
    public R<Boolean> testConnection(@PathVariable("id") Long id) {
        return wrapBool("测试连接 id=" + id, () -> superService.testConnection(id));
    }

    @Operation(summary = "测试连接(表单)", description = "基于表单未保存的值测试,不写 DB")
    @PostMapping("/testConnectionByForm")
    @WebLog(value = "测试连接(表单)", request = false)
    public R<Boolean> testConnectionByForm(@RequestBody @Validated DataSourceSaveVO formVO) {
        return wrapBool("测试连接(表单)", () -> superService.testConnectionByForm(formVO));
    }

    @Operation(summary = "启停数据源", description = "启用前后端兜底校验测试连接")
    @PutMapping("/changeStatus/{id}")
    @WebLog(value = "启停数据源", request = false)
    @Parameters({
            @Parameter(name = "id", description = "数据源 ID", required = true),
            @Parameter(name = "enable", description = "true=启用 / false=禁用", required = true)
    })
    public R<Boolean> changeStatus(@PathVariable("id") Long id, @RequestParam("enable") Boolean enable) {
        return wrap("启停数据源 id=" + id + " enable=" + enable,
                () -> superService.changeStatus(id, enable));
    }

    @Operation(summary = "删除数据源")
    @DeleteMapping("/deleteDataSource/{id}")
    @WebLog(value = "删除数据源", request = false)
    @Parameters({@Parameter(name = "id", description = "数据源 ID", required = true)})
    public R<Boolean> deleteDataSource(@PathVariable("id") Long id) {
        return wrap("删除数据源 id=" + id, () -> superService.deleteDataSource(id));
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

    /**
     * 测试连接专用:异常返 R.success(false) 不返 fail(前端 chip 永远拿到 ok=false 而非空)。
     *
     * @param opDesc 操作描述,用于失败日志
     * @param action 业务动作
     * @return 统一响应;异常返 R.success(false)
     */
    private R<Boolean> wrapBool(String opDesc, Supplier<Boolean> action) {
        try {
            return R.success(action.get());
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("{} 失败: {}", opDesc, e.getMessage(), e);
            return R.success(false);
        }
    }
}
