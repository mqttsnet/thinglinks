package com.mqttsnet.thinglinks.plugin.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.entity.plugin.PluginInfo;
import com.mqttsnet.thinglinks.enumeration.plugin.PluginActionStatusEnum;
import com.mqttsnet.thinglinks.service.plugin.PluginInfoService;
import com.mqttsnet.thinglinks.vo.param.plugin.PluginActionRequestParamVO;
import com.mqttsnet.thinglinks.vo.query.plugin.PluginInfoPageQuery;
import com.mqttsnet.thinglinks.vo.result.plugin.PluginInfoDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.plugin.PluginInfoResultVO;
import com.mqttsnet.thinglinks.vo.result.plugin.PluginResultVO;
import com.mqttsnet.thinglinks.vo.save.plugin.PluginInfoSaveVO;
import com.mqttsnet.thinglinks.vo.update.plugin.PluginInfoUpdateVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * 插件信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-08-25 19:05:11
 * @create [2024-08-25 19:05:11] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/pluginInfo")
@Tag(name = "插件信息")
public class PluginInfoController extends SuperController<PluginInfoService, Long, PluginInfo, PluginInfoSaveVO, PluginInfoUpdateVO, PluginInfoPageQuery, PluginInfoResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    @Override
    public QueryWrap<PluginInfo> handlerWrapper(PluginInfo model, PageParams<PluginInfoPageQuery> params) {
        QueryWrap<PluginInfo> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("plugin_info");
        return queryWrap;
    }

    /**
     * 新增 插件信息
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存插件信息", description = "保存插件信息")
    @PostMapping("/savePlugin")
    @WebLog(value = "保存插件信息", request = false)
    public R<PluginInfoSaveVO> savePlugin(@RequestBody PluginInfoSaveVO saveVO) {
        log.info("savePlugin saveVO:{}:", JsonUtil.toJson(saveVO));
        try {
            return R.success(superService.savePlugin(saveVO));
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("保存插件信息失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 修改 插件信息
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改插件信息", description = "修改插件信息")
    @PutMapping("/updatePlugin")
    @WebLog(value = "修改插件信息", request = false)
    public R<PluginInfoUpdateVO> updatePlugin(@RequestBody PluginInfoUpdateVO updateVO) {
        log.info("updatePlugin updateVO:{}:", JsonUtil.toJson(updateVO));
        try {
            return R.success(superService.updatePlugin(updateVO));
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("修改插件信息失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }

    }


    /**
     * 扫描插件并保存扫描结果（插件安全扫描）
     *
     * @param pluginIdentification 插件标识符
     * @return {@link R<PluginInfoResultVO> } 包含扫描结果的对象
     */
    @Operation(summary = "扫描插件", description = "Scans a plugin and saves the result to the database")
    @Parameters({@Parameter(name = "pluginIdentification", description = "Plugin Identification", required = true),})
    @GetMapping("/scanPlugin/{pluginIdentification}")
    public R<PluginInfoResultVO> scanPlugin(@PathVariable("pluginIdentification") String pluginIdentification) {
        log.info("Initiating scan for pluginIdentification: {}", pluginIdentification);
        try {
            PluginInfoResultVO pluginInfoResultVO = superService.scanAndSavePluginResult(pluginIdentification);
            echoService.action(pluginInfoResultVO);
            return R.success(pluginInfoResultVO);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("Failed to scan pluginIdentification: {}. Error: {}", pluginIdentification, e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 预加载插件
     *
     * @param id 插件ID
     * @return {@link R<PluginInfoResultVO>} 包含预加载结果的对象
     */
    @Operation(summary = "预加载插件", description = "Preloads a plugin based on plugin id")
    @Parameter(name = "id", description = "Plugin ID", required = true)
    @GetMapping("/preloadPlugin/{id}")
    public R<PluginResultVO> preloadPlugin(@PathVariable("id") Long id) {
        log.info("Initiating preload for plugin id: {}", id);
        try {
            return R.success(superService.preloadPlugin(id));
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("Failed to preload plugin for id: {}. Error: {}", id, e.getMessage(), e);
            return R.fail();
        }
    }


    /**
     * 根据插件ID获取插件详情
     *
     * @param id 插件ID
     * @return 插件详情
     */
    @Operation(summary = "根据插件ID获取插件详情", description = "根据插件ID获取插件详情")
    @GetMapping("/getPluginInfoDetails/{id}")
    @Parameter(name = "id", description = "插件ID", required = true)
    public R<PluginInfoDetailsResultVO> getPluginInfoDetails(@PathVariable("id") Long id) {
        log.info("getPluginDetails id:{}", id);
        try {
            PluginInfoDetailsResultVO result = superService.getPluginInfoDetails(id);
            echoService.action(result);
            return R.success(result);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("根据插件ID获取插件详情失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }


    /**
     * 安装或卸载插件
     *
     * @param request 插件安装或卸载请求对象
     * @return {@link R<?>} 包含安装卸载结果的对象
     */
    @Operation(summary = "安装或卸载插件", description = "根据插件ID和实例标识集合执行插件的安装或卸载操作")
    @PostMapping("/installOrUninstall")
    public R<?> installOrUninstallPlugin(@Validated @RequestBody PluginActionRequestParamVO request) {
        log.info("Received request to {} plugin {} on instances {}", PluginActionStatusEnum.fromValue(request.getStatus()).get(), request.getPluginId(), request.getInstanceId());
        try {
            superService.installOrUninstallPlugin(request.getPluginId(), request.getInstanceId(), PluginActionStatusEnum.fromValue(request.getStatus()).get());
            return R.success();
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("安装或卸载插件失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }

    }

    /**
     * 删除插件信息
     *
     * @param id 插件信息ID
     * @return 删除结果
     */
    @Operation(summary = "删除插件信息", description = "根据插件信息ID删除插件信息")
    @Parameters({
            @Parameter(name = "id", description = "id", required = true)
    })
    @DeleteMapping("/deletePluginInfo/{id}")
    @WebLog(value = "删除插件信息", request = false)
    public R<Boolean> deletePluginInfo(@PathVariable("id") Long id) {
        log.info("deletePluginInfo id:{}", id);
        try {
            return R.success(superService.deletePluginInfo(id));
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("删除插件信息失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 批量删除插件信息
     *
     * @param ids 插件信息ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除插件信息", description = "根据插件信息ID列表批量删除插件信息")
    @Parameters({@Parameter(name = "ids", description = "插件信息ID列表", required = true)})
    @DeleteMapping("/deletePluginInfos")
    @WebLog(value = "批量删除插件信息", request = false)
    public R<Boolean> deletePluginInfos(@RequestBody List<Long> ids) {
        log.info("deletePluginInfos ids:{}", ids);
        try {
            boolean allDeleted = ids.stream().distinct().allMatch(id -> superService.deletePluginInfo(id));
            return R.success(allDeleted);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("批量删除插件信息失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * 卸载所有插件实例
     *
     * @param id 插件信息ID
     * @return 卸载结果
     */
    @Operation(summary = "卸载所有插件实例", description = "根据插件ID卸载所有相关的插件实例")
    @Parameters({
            @Parameter(name = "id", description = "id", required = true)
    })
    @DeleteMapping("/uninstallAllPluginsForInstances/{id}")
    @WebLog(value = "卸载所有插件实例")
    public R<Boolean> uninstallAllPluginsForInstances(@Parameter(name = "id", description = "id", required = true) @PathVariable("id") Long id) {
        log.info("uninstallAllPluginsForInstances id:{}", id);
        try {
            // 卸载所有实例中的插件
            superService.uninstallAllPluginsForInstances(id);
            return R.success(true);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("卸载所有插件实例失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }


}


