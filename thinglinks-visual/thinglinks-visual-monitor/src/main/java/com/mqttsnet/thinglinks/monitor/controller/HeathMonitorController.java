package com.mqttsnet.thinglinks.monitor.controller;

import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.monitor.api.domain.HeathMonitor;
import com.mqttsnet.thinglinks.monitor.service.*;
import com.mqttsnet.thinglinks.monitor.util.staticvar.StaticKeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:HeathMonitorController.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: HeathMonitorController.java
 */
@RestController
@RequestMapping("/heathMonitor")
public class HeathMonitorController extends BaseController {


    private static final Logger logger = LoggerFactory.getLogger(HeathMonitorController.class);

    @Resource
    private HeathMonitorService heathMonitorService;

    @Resource
    private LogInfoService logInfoService;

    /**
     * 根据条件查询心跳监控列表
     *
     * @param HeathMonitor
     * @return
     */
    @GetMapping(value = "/list")
    public TableDataInfo heathMonitorList(HeathMonitor HeathMonitor) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<HeathMonitor> heathMonitors = null;
        try {
            startPage();
            heathMonitors = heathMonitorService.selectByParams(params);
        } catch (Exception e) {
            logger.error("查询服务心跳监控错误", e);
            logInfoService.save("查询心跳监控错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return getDataTable(heathMonitors);
    }

    /**
     * 保存心跳监控信息
     *
     * @param HeathMonitor
     * @return
     */
    @PostMapping(value = "/save")
    public AjaxResult saveHeathMonitor(@RequestBody HeathMonitor HeathMonitor) {
        String msg = "success";
        try {
            if (StringUtils.isEmpty(HeathMonitor.getId())) {
                heathMonitorService.save(HeathMonitor);
            } else {
                heathMonitorService.updateById(HeathMonitor);
            }
        } catch (Exception e) {
            msg = "error";
            logger.error("保存服务心跳监控错误：", e);
            logInfoService.save(HeathMonitor.getAppName(), "保存心跳监控错误：" + e.toString(), StaticKeys.LOG_ERROR);
        }
        return msg.equals("success") ? AjaxResult.success() : AjaxResult.error("操作异常");
    }


    /**
     * 查看该心跳监控
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/edit/{id}")
    public AjaxResult selectById(@PathVariable(value = "id") String id) {
        String errorMsg = "编辑服务心跳监控：";
        HeathMonitor heathMonitor = new HeathMonitor();
        try {
            heathMonitor = heathMonitorService.selectById(id);
        } catch (Exception e) {
            logger.error(errorMsg, e);
            logInfoService.save(heathMonitor.getAppName(), errorMsg + e.toString(), StaticKeys.LOG_ERROR);
        }
        return AjaxResult.success(heathMonitor);
    }


    /**
     * 删除心跳监控
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/del/{id}")
    public AjaxResult delete(@PathVariable(value = "id") String id) {
        String errorMsg = "删除服务心跳监控错误：";
        HeathMonitor HeathMonitor = new HeathMonitor();
        try {
            HeathMonitor = heathMonitorService.selectById(id);
            logInfoService.save("删除服务心跳监控：" + HeathMonitor.getAppName(), "删除服务心跳监控：" + HeathMonitor.getAppName() + "：" + HeathMonitor.getHeathUrl(), StaticKeys.LOG_ERROR);
            heathMonitorService.deleteById(id.split(","));
        } catch (Exception e) {
            logger.error(errorMsg, e);
            logInfoService.save(HeathMonitor.getAppName(), errorMsg + e.toString(), StaticKeys.LOG_ERROR);
        }
        return AjaxResult.success();
    }

}
