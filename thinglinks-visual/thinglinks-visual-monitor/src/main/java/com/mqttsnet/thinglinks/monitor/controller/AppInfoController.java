package com.mqttsnet.thinglinks.monitor.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mqttsnet.thinglinks.common.core.utils.CodeUtil;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.monitor.api.domain.AppInfo;
import com.mqttsnet.thinglinks.monitor.api.domain.AppState;
import com.mqttsnet.thinglinks.monitor.api.domain.SystemInfo;
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
 * @ClassName:AppInfoController.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: AppInfoController.java
 */
@RestController
@RequestMapping("/appInfo")
public class AppInfoController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AppInfoController.class);

    @Resource
    private AppInfoService appInfoService;

    @Resource
    private AppStateService appStateService;

    @Resource
    private LogInfoService logInfoService;

    @Resource
    private SystemInfoService systemInfoService;

    @Resource
    private DashboardService dashboardService;

    /**
     * agent查询进程列表
     *
     * @param hostname
     * @return
     */
    @GetMapping(value = "/agentList")
    public AjaxResult agentList(Map params) {
        try {
            List<AppInfo> list = appInfoService.selectByParams(params);
            AjaxResult.success(list);
        } catch (Exception e) {
            logger.error("agent获取进程信息错误", e);
            logInfoService.save("agent获取进程信息错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return AjaxResult.error("agent获取进程信息错误");
    }

    /**
     * 根据条件查询进程列表
     *
     * @param appInfo
     * @return
     */
    @GetMapping(value = "/list")
    public TableDataInfo AppInfoList(AppInfo appInfo) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<AppInfo> list = null;
        try {
            StringBuffer url = new StringBuffer();
            String hostname = null;
            if (!StringUtils.isEmpty(appInfo.getHostname())) {
                hostname = CodeUtil.unescape(appInfo.getHostname());
                params.put("hostname", hostname.trim());
                url.append("&hostname=").append(CodeUtil.escape(hostname));
            }
            startPage();
            list = appInfoService.selectByParams(params);
        } catch (Exception e) {
            logger.error("查询进程信息错误", e);
            logInfoService.save("查询进程信息错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return getDataTable(list);
    }

    /**
     * 保存应用监控信息
     *
     * @param AppInfo
     * @return
     */
    @PostMapping(value = "/save")
    public AjaxResult saveAppInfo(@RequestBody AppInfo AppInfo) {
        String msg = "success";
        try {
            if (StringUtils.isEmpty(AppInfo.getId())) {
                appInfoService.save(AppInfo);
            } else {
                appInfoService.updateById(AppInfo);
            }
        } catch (Exception e) {
            msg = "error";
            logger.error("保存进程错误：", e);
            logInfoService.save(AppInfo.getHostname(), "保存进程错误：" + e.toString(), StaticKeys.LOG_ERROR);
        }
        return msg.equals("success") ? AjaxResult.success("成功") : AjaxResult.error("失败");
    }

    /**
     * 查询详情
     *
     * @return
     */
    @GetMapping(value = "/selectById/{id}")
    public AjaxResult selectById(@PathVariable(value = "id") String id) {
        Map map = new HashMap();
        AppInfo appInfo = new AppInfo();
        try {
            List<SystemInfo> systemInfoList = systemInfoService.selectSystemInfoList(new SystemInfo());
            map.put("systemInfoList", systemInfoList);
            appInfo = appInfoService.selectById(id);
            map.put("appInfo", appInfo);
        } catch (Exception e) {
            logInfoService.save(appInfo.getAppPid(), e.toString(), StaticKeys.LOG_ERROR);
        }
        return AjaxResult.success(map);
    }


    /**
     * 查看该应用统计图
     *
     * @param id
     * @param date
     * @return
     */
    @GetMapping(value = "/view")
    public AjaxResult viewChart(@RequestParam(value = "id", required = true) String id, @RequestParam(value = "date", required = true) String date) {
        String errorMsg = "查看进程统计图错误：";
        AppInfo appInfo = new AppInfo();
        Map datas = new HashMap();
        try {
            appInfo = appInfoService.selectById(id);
            Map<String, Object> params = new HashMap<String, Object>();
            if (StringUtils.isEmpty(date)) {
                date = DateUtils.getCurrentDate();
            }
            dashboardService.setDateParam(date, params);
            datas.put("datenow", date);
            datas.put("dateList", dashboardService.getDateList());
            datas.put("appInfo", appInfo);
            datas.put("appInfo", appInfo);
            /**
             * 传参查询
             */
            params.put("appInfoId", appInfo.getId());
            List<AppState> appStateList = appStateService.selectByParams(params);
            datas.put("appStateList", JSONUtil.parseArray(appStateList));
        } catch (Exception e) {
            logger.error(errorMsg, e);
            logInfoService.save(appInfo.getHostname() + ":" + appInfo.getAppPid(), errorMsg + e.toString(), StaticKeys.LOG_ERROR);
        }
        return AjaxResult.success(datas);
    }


    /**
     * 删除进程
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/del/{id}")
    public AjaxResult delete(@PathVariable(value = "id") String id) {
        String errorMsg = "删除进程信息错误：";
        AppInfo appInfo = new AppInfo();
        try {
            appInfo = appInfoService.selectById(id);
            logInfoService.save("删除进程：" + appInfo.getHostname(), "删除进程：" + appInfo.getHostname() + "：" + appInfo.getAppPid(), StaticKeys.LOG_ERROR);
            appInfoService.deleteById(id.split(","));
        } catch (Exception e) {
            logger.error(errorMsg, e);
            logInfoService.save(appInfo.getHostname() + ":" + appInfo.getAppPid(), errorMsg + e.toString(), StaticKeys.LOG_ERROR);
        }
        return AjaxResult.success();
    }

}
