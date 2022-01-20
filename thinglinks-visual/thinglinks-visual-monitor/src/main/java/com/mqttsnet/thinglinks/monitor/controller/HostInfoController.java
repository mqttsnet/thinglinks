package com.mqttsnet.thinglinks.monitor.controller;

import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.monitor.api.domain.SystemInfo;
import com.mqttsnet.thinglinks.monitor.service.*;
import com.mqttsnet.thinglinks.monitor.util.staticvar.StaticKeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @ClassName:HostInfoController.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 主机备注信息
 */
@RestController
@RequestMapping("/host")
public class HostInfoController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(HostInfoController.class);

    @Resource
    private SystemInfoService systemInfoService;

    @Resource
    private LogInfoService logInfoService;

    /**
     * 保存主机备注信息
     *
     * @param SystemInfo
     * @return
     */
    @PostMapping(value = "/save")
    public AjaxResult saveHostInfo(@RequestBody SystemInfo SystemInfo) {
        String msg = "success";
        try {
            if (StringUtils.isEmpty(SystemInfo.getId())) {
                systemInfoService.save(SystemInfo);
            } else {
                SystemInfo ho = systemInfoService.selectById(SystemInfo.getId());
                ho.setRemark(SystemInfo.getRemark());
                systemInfoService.updateById(ho);
            }
        } catch (Exception e) {
            msg = "error";
            logger.error("保存主机备注信息错误：", e);
            logInfoService.save(SystemInfo.getHostname(), "保存主机备注信息错误：" + e.toString(), StaticKeys.LOG_ERROR);
        }
        return msg.equals("success") ? AjaxResult.success() : AjaxResult.error("操作异常");
    }

}
