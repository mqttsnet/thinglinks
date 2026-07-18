package com.mqttsnet.thinglinks.base.executor.service.jobhandler;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.base.executor.service.AbstractTenantJob;
import com.mqttsnet.thinglinks.msg.biz.MsgBiz;
import com.mqttsnet.thinglinks.msg.service.ExtendMsgService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @program: thinglinks-base-executor
 * @description: Base Job
 * @packagename: com.xxl.job.executor.service.jobhandler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-07-13 11:46
 **/
@Component
@Slf4j
public class BaseJob extends AbstractTenantJob {
    @Autowired
    private MsgBiz msgBiz;
    @Autowired
    private ExtendMsgService extendMsgService;

    @XxlJob("smsSendJobHandler")
    public void smsSendJobHandler() {
        String param = XxlJobHelper.getJobParam();
        ArgumentAssert.notEmpty(param, "参数不能为空");
        Map<String, String> map = JsonUtil.parse(param, Map.class);
        String tenantIdStr = map.get(ContextConstants.TENANT_ID_HEADER);
        String msgIdStr = map.get("msgId");
        if (StrUtil.isEmpty(tenantIdStr) || StrUtil.isEmpty(msgIdStr)) {
            return;
        }
        Long tenantId = Long.valueOf(tenantIdStr);
        Long msgId = Long.valueOf(msgIdStr);
        XxlJobHelper.log("tenantId={}, msgId={}", tenantId, msgId);

        ContextUtil.setTenantId(tenantId);

        msgBiz.execSend(msgId);
    }

    @XxlJob("publishMsgJobHandler")
    public void publishMsgJobHandler() {
        String param = XxlJobHelper.getJobParam();
        ArgumentAssert.notEmpty(param, "参数不能为空");
        Map<String, String> map = JsonUtil.parse(param, Map.class);
        String tenantIdStr = map.get(ContextConstants.TENANT_ID_HEADER);
        String msgIdStr = map.get("msgId");
        if (StrUtil.isEmpty(tenantIdStr) || StrUtil.isEmpty(msgIdStr)) {
            return;
        }
        Long tenantId = Long.valueOf(tenantIdStr);
        Long msgId = Long.valueOf(msgIdStr);
        XxlJobHelper.log("tenantId={}, msgId={}", tenantId, msgId);

        ContextUtil.setTenantId(tenantId);

        extendMsgService.publishNotice(msgId);
    }


}
