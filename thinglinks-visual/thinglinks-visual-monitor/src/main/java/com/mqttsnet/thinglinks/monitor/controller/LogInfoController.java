package com.mqttsnet.thinglinks.monitor.controller;

import com.mqttsnet.thinglinks.common.core.utils.CodeUtil;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.monitor.api.domain.LogInfo;
import com.mqttsnet.thinglinks.monitor.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:LogInfoController.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: LogInfoController.java
 */
@RestController
@RequestMapping("/log")
public class LogInfoController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LogInfoController.class);

    @Resource
    private LogInfoService logInfoService;

    /**
     * 根据条件查询日志信息列表
     *
     * @param logInfo
     * @return
     */
    @GetMapping(value = "/list")
    public TableDataInfo LogInfoList(LogInfo logInfo) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<LogInfo> logInfos = null;
        try {
            StringBuffer url = new StringBuffer();
            String hostname = null;
            if (!StringUtils.isEmpty(logInfo.getHostname())) {
                hostname = CodeUtil.unescape(logInfo.getHostname());
                params.put("hostname", hostname.trim());
                url.append("&hostname=").append(CodeUtil.escape(hostname));
            }
            startPage();
            logInfos = logInfoService.selectByParams(params);
        } catch (Exception e) {
            logger.error("查询日志错误", e);
        }
        return getDataTable(logInfos);
    }

    /**
     * 查看日志信息
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/view/{id}")
    public AjaxResult viewLogInfo(@PathVariable(value = "id") String id) {
        LogInfo logInfo = null;
        try {
            logInfo = logInfoService.selectById(id);
        } catch (Exception e) {
            logger.error("查看日志信息：", e);
        }
       return AjaxResult.success(logInfo);
    }

}
