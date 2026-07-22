package com.mqttsnet.thinglinks.job.facade.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.job.dto.JobReturnT;
import com.mqttsnet.thinglinks.job.dto.XxlJobInfoVO;
import com.mqttsnet.thinglinks.job.facade.JobFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author tangyh
 * @since 2024/9/21 00:09
 */
@Service
@RequiredArgsConstructor
public class JobFacadeImpl implements JobFacade {
    @Value("${" + Constants.PROJECT_PREFIX + ".feign.job.job-server:http://127.0.0.1:18767}")
    private String jobServerUrl;
    @Value("${" + Constants.PROJECT_PREFIX + ".feign.job.accessToken}")
    private String accessToken;

    @Override
    public R<String> addTimingTask(XxlJobInfoVO xxlJobInfo) {
        String URL = "/thinglinks-job-admin/api/jobinfo/save";
        String result = HttpRequest.post(jobServerUrl + URL)
                .header(JobFacade.HEADER_NAME, accessToken)
                .body(JSON.toJSONString(xxlJobInfo))
                .timeout(20000)//超时，毫秒
                .execute().body();
        return R.success(result);
    }

    @Override
    public JobReturnT<String> updateJob(XxlJobInfoVO xxlJobInfo) {
        String URL = "/thinglinks-job-admin/api/jobinfo/update";
        String result = HttpRequest.post(jobServerUrl + URL)
                .header(JobFacade.HEADER_NAME, accessToken)
                .body(JSON.toJSONString(xxlJobInfo))
                .timeout(20000)//超时，毫秒
                .execute().body();
        JobReturnT<String> success = JobReturnT.SUCCESS;
        success.setContent(result);
        return success;
    }

    @Override
    public JobReturnT<String> removeJob(Integer id) {
        String URL = "/thinglinks-job-admin/api/jobinfo/remove";
        String result = HttpRequest.post(jobServerUrl + URL)
                .header(JobFacade.HEADER_NAME, accessToken)
                .form("id", String.valueOf(id))
                .timeout(20000)//超时，毫秒
                .execute().body();
        JobReturnT<String> success = JobReturnT.SUCCESS;
        success.setContent(result);
        return success;
    }

    @Override
    public JobReturnT<String> pauseJob(Integer id) {
        String URL = "/thinglinks-job-admin/api/jobinfo/stop";
        String result = HttpRequest.post(jobServerUrl + URL)
                .header(JobFacade.HEADER_NAME, accessToken)
                .form("id", String.valueOf(id))
                .timeout(20000)//超时，毫秒
                .execute().body();
        JobReturnT<String> success = JobReturnT.SUCCESS;
        success.setContent(result);
        return success;
    }

    @Override
    public JobReturnT<String> startJob(Integer id) {
        String URL = "/thinglinks-job-admin/api/jobinfo/start";
        String result = HttpRequest.post(jobServerUrl + URL)
                .header(JobFacade.HEADER_NAME, accessToken)
                .form("id", String.valueOf(id))
                .timeout(20000)//超时，毫秒
                .execute().body();
        JobReturnT<String> success = JobReturnT.SUCCESS;
        success.setContent(result);
        return success;
    }

    @Override
    public JobReturnT<String> saveTimingTask(XxlJobInfoVO xxlJobInfo) {
        String URL = "/thinglinks-job-admin/api/jobinfo/save";
        String result = HttpRequest.post(jobServerUrl + URL)
                .header(JobFacade.HEADER_NAME, accessToken)
                .body(JSON.toJSONString(xxlJobInfo))
                .timeout(20000)//超时，毫秒
                .execute().body();
        JobReturnT<String> success = JobReturnT.SUCCESS;
        success.setContent(result);
        return success;
    }

    @Override
    public JobReturnT<XxlJobInfoVO> loadById(Integer id) {
        String URL = "/thinglinks-job-admin/api/jobinfo/loadById";
        String result = HttpRequest.post(jobServerUrl + URL)
                .header(JobFacade.HEADER_NAME, accessToken)
                .form("id", String.valueOf(id))
                .timeout(20000)//超时，毫秒
                .execute().body();
        JobReturnT<XxlJobInfoVO> success = new JobReturnT();
        success.setCode(200);
        return success;
    }


}
