package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.controller.interceptor.PermissionInterceptor;
import com.xxl.job.admin.controller.vo.XxlJobInfoVO;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.thread.JobScheduleHelper;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.DateUtil;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/api/jobinfo")
public class JobInfoApiController {
    private static Logger logger = LoggerFactory.getLogger(JobInfoApiController.class);

    @Resource
    private XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private XxlJobService xxlJobService;

    @RequestMapping("/add")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> add(HttpServletRequest request, XxlJobInfo jobInfo) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
            && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
            && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN_KEY))) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "请在请求头中携带正确的token");
        }

        // opt
        XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
        return xxlJobService.add(jobInfo, loginUser);
    }

    @RequestMapping("/update")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> update(HttpServletRequest request, XxlJobInfo jobInfo) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
            && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
            && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN_KEY))) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "请在请求头中携带正确的token");
        }


        // opt
        XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
        return xxlJobService.update(jobInfo, loginUser);
    }

    @RequestMapping("/remove")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> remove(HttpServletRequest request, int id) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
            && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
            && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN_KEY))) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "请在请求头中携带正确的token");
        }

        return xxlJobService.remove(id);
    }

    @RequestMapping("/stop")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> pause(HttpServletRequest request, int id) {

        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
            && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
            && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN_KEY))) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "请在请求头中携带正确的token");
        }

        return xxlJobService.stop(id);
    }

    @RequestMapping("/start")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> start(HttpServletRequest request, int id) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
            && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
            && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN_KEY))) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "请在请求头中携带正确的token");
        }

        return xxlJobService.start(id);
    }

    @RequestMapping("/save")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> save(HttpServletRequest request, @RequestBody XxlJobInfoVO xxlJobInfo) {

        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
            && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
            && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN_KEY))) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "请在请求头中携带正确的token");
        }

        return xxlJobService.save(xxlJobInfo);
    }

    @RequestMapping("/trigger")
    @ResponseBody
    public ReturnT<String> triggerJob(HttpServletRequest request, int id, String executorParam, String addressList) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
            && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
            && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN_KEY))) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "请在请求头中携带正确的token");
        }

        // login user
        XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
        // trigger
        return xxlJobService.trigger(loginUser, id, executorParam, addressList);
    }

    @RequestMapping("/nextTriggerTime")
    @ResponseBody
    public ReturnT<List<String>> nextTriggerTime(HttpServletRequest request, String scheduleType, String scheduleConf) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
            && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
            && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN_KEY))) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "请在请求头中携带正确的token");
        }

        XxlJobInfo paramXxlJobInfo = new XxlJobInfo();
        paramXxlJobInfo.setScheduleType(scheduleType);
        paramXxlJobInfo.setScheduleConf(scheduleConf);

        List<String> result = new ArrayList<>();
        try {
            Date lastTime = new Date();
            for (int i = 0; i < 5; i++) {
                lastTime = JobScheduleHelper.generateNextValidTime(paramXxlJobInfo, lastTime);
                if (lastTime != null) {
                    result.add(DateUtil.formatDateTime(lastTime));
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ReturnT<>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type") + I18nUtil.getString("system_unvalid")) + e.getMessage());
        }
        return new ReturnT<>(result);

    }


    @RequestMapping("/loadById")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<XxlJobInfo> loadById(HttpServletRequest request, int id) {

        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
            && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
            && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN_KEY))) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "请在请求头中携带正确的token");
        }

        return xxlJobService.loadById(id);
    }

}
