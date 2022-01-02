package com.mqttsnet.thinglinks.monitor.controller;

import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.monitor.api.domain.MailSet;
import com.mqttsnet.thinglinks.monitor.service.*;
import com.mqttsnet.thinglinks.monitor.util.msg.WarnMailUtil;
import com.mqttsnet.thinglinks.monitor.util.staticvar.StaticKeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName:MailSetController.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: MailSetController.java
 */
@RestController
@RequestMapping("/mailset")
public class MailSetController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MailSetController.class);

    @Resource
    private MailSetService mailSetService;

    @Resource
    private LogInfoService logInfoService;

    /**
     * 根据条件查询列表
     *
     * @param mailSet
     * @return
     */
    @GetMapping(value = "/list")
    public TableDataInfo MailSetList(MailSet mailSet) {
        startPage();
        List<MailSet> list = mailSetService.selectMailSetList(mailSet);
        return getDataTable(list);
    }


    /**
     * 保存邮件设置信息
     *
     * @param mailSet
     * @return
     */
    @PostMapping(value = "/save")
    public AjaxResult saveMailSet(@RequestBody MailSet mailSet) {
        String result = "success";
        try {
            mailSetService.save(mailSet);
            StaticKeys.mailSet = mailSet;
        } catch (Exception e) {
            result = "erroe";
            logger.error("保存邮件设置信息错误：", e);
            logInfoService.save("邮件设置信息错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return result.equals("success") ? AjaxResult.success() : AjaxResult.error("保存邮件设置信息错误");
    }

    /**
     * 更新邮件设置信息
     *
     * @param mailSet
     * @return
     */
    @PostMapping(value = "/update")
    public AjaxResult updateMailSet(@RequestBody MailSet mailSet) {
        String result = "success";
        try {
            mailSetService.updateById(mailSet);
            StaticKeys.mailSet = mailSet;
        } catch (Exception e) {
            logger.error("更新邮件设置信息错误：", e);
            logInfoService.save("邮件设置信息错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return result.equals("success") ? AjaxResult.success() : AjaxResult.error("更新邮件设置信息错误");
    }

    /**
     * 测试发送
     *
     * @param mailSet
     * @return
     */
    @PostMapping(value = "/test")
    public AjaxResult test(@RequestBody MailSet mailSet) {
        String result = "success";
        try {
            if (StringUtils.isEmpty(mailSet.getId())) {
                mailSetService.save(mailSet);
            } else {
                mailSetService.updateById(mailSet);
            }
            StaticKeys.mailSet = mailSet;
            result = WarnMailUtil.sendMail(mailSet.getToMail(), "Thinglinks测试邮件发送", "Thinglinks测试邮件发送");
        } catch (Exception e) {
            logger.error("测试邮件设置信息错误：", e);
            logInfoService.save("测试邮件设置信息错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return result.equals("success") ? AjaxResult.success() : AjaxResult.error("测试邮件设置信息错误");
    }

    /**
     * 删除告警邮件信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public AjaxResult delete(@PathVariable String[] ids) {
        String result = "success";
        try {
            mailSetService.deleteById(ids);
        } catch (Exception e) {
            result = "error";
            logger.error("删除告警邮件设置错误", e);
            logInfoService.save("删除告警邮件设置错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return result.equals("success") ? AjaxResult.success() : AjaxResult.error("删除告警邮件设置错误");
    }

}
