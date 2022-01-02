package com.mqttsnet.thinglinks.monitor.config.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.mqttsnet.thinglinks.common.core.utils.SpringUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.*;
import com.mqttsnet.thinglinks.monitor.config.mail.MailConfig;
import com.mqttsnet.thinglinks.monitor.service.LogInfoService;
import com.mqttsnet.thinglinks.monitor.util.msg.WarnMailUtil;
import com.mqttsnet.thinglinks.monitor.util.msg.WarnPools;
import com.mqttsnet.thinglinks.monitor.util.staticvar.BatchData;
import com.mqttsnet.thinglinks.monitor.util.staticvar.StaticKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 消费采集的服务器数据消息
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "thinglinks-collection", topic = "thinglinks_collection_system")
public class ConsumerCollection implements RocketMQListener {

    public static final String content_suffix = "<p><a target='_blank' href='http://www.wgstart.com'>Thinglinks</a>敬上";

    private static LogInfoService logInfoService = (LogInfoService) SpringUtils.getBean(LogInfoService.class);

    private static MailConfig mailConfig = (MailConfig) SpringUtils.getBean(MailConfig.class);

    ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 40, 2, TimeUnit.MINUTES, new LinkedBlockingDeque<>());

    @Override
    public void onMessage(Object message) {
        assert message != null;
        log.info("Monitor消费服务器采集消息" + message);
        cn.hutool.json.JSONObject agentJsonObject = (cn.hutool.json.JSONObject) JSONUtil.parse(String.valueOf(message));
        cn.hutool.json.JSONObject resultJson = new cn.hutool.json.JSONObject();
        cn.hutool.json.JSONObject cpuState = agentJsonObject.getJSONObject("cpuState");
        cn.hutool.json.JSONObject memState = agentJsonObject.getJSONObject("memState");
        cn.hutool.json.JSONObject sysLoadState = agentJsonObject.getJSONObject("sysLoadState");
        JSONArray appInfoList = agentJsonObject.getJSONArray("appInfoList");
        JSONArray appStateList = agentJsonObject.getJSONArray("appStateList");
        cn.hutool.json.JSONObject logInfo = agentJsonObject.getJSONObject("logInfo");
        cn.hutool.json.JSONObject systemInfo = agentJsonObject.getJSONObject("systemInfo");
        cn.hutool.json.JSONObject netIoState = agentJsonObject.getJSONObject("netIoState");
        JSONArray deskStateList = agentJsonObject.getJSONArray("deskStateList");
        try {
            if (logInfo != null) {
                LogInfo bean = new LogInfo();
                BeanUtil.copyProperties(logInfo, bean);
                BatchData.LOG_INFO_LIST.add(bean);
            }
            if (cpuState != null) {
                CpuState bean = new CpuState();
                BeanUtil.copyProperties(cpuState, bean);
                BatchData.CPU_STATE_LIST.add(bean);
                Runnable runnable = () -> {
                    sendCpuWarnInfo(bean);
                };
                executor.execute(runnable);
            }
            if (memState != null) {
                MemState bean = new MemState();
                BeanUtil.copyProperties(memState, bean);
                BatchData.MEM_STATE_LIST.add(bean);
                Runnable runnable = () -> {
                    sendWarnInfo(bean);
                };
                executor.execute(runnable);
            }
            if (sysLoadState != null) {
                SysLoadState bean = new SysLoadState();
                BeanUtil.copyProperties(sysLoadState, bean);
                BatchData.SYSLOAD_STATE_LIST.add(bean);
            }
            if (netIoState != null) {
                NetIoState bean = new NetIoState();
                BeanUtil.copyProperties(netIoState, bean);
                BatchData.NETIO_STATE_LIST.add(bean);
            }
            if (appInfoList != null && appStateList != null) {
                List<AppInfo> appInfoResList = JSONUtil.toList(appInfoList, AppInfo.class);
                for (AppInfo appInfo : appInfoResList) {
                    BatchData.APP_INFO_LIST.add(appInfo);
                }
                List<AppState> appStateResList = JSONUtil.toList(appStateList, AppState.class);
                for (AppState appState : appStateResList) {
                    BatchData.APP_STATE_LIST.add(appState);
                }
            }
            if (systemInfo != null) {
                SystemInfo bean = new SystemInfo();
                BeanUtil.copyProperties(systemInfo, bean);
                BatchData.SYSTEM_INFO_LIST.add(bean);
            }
            if (deskStateList != null) {
                for (Object jsonObjects : deskStateList) {
                    DeskState bean = new DeskState();
                    BeanUtil.copyProperties(jsonObjects, bean);
                    BatchData.DESK_STATE_LIST.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断系统内存使用率是否超过98%，超过则发送告警邮件
     *
     * @param memState
     * @return
     */
    public static boolean sendWarnInfo(MemState memState) {
        if (StaticKeys.mailSet == null) {
            return false;
        }
        MailSet mailSet = StaticKeys.mailSet;
        if (StaticKeys.NO_SEND_WARN.equals(mailConfig.getAllWarnMail()) || StaticKeys.NO_SEND_WARN.equals(mailConfig.getMemWarnMail())) {
            return false;
        }
        String key = memState.getHostname();
        if (!StringUtils.isEmpty(WarnPools.MEM_WARN_MAP.get(key))) {
            return false;
        }
        if (memState.getUsePer() != null && memState.getUsePer() >= mailConfig.getMemWarnVal()) {
            try {
                String title = "内存告警：" + memState.getHostname();
                String commContent = "服务器：" + memState.getHostname() + ",内存使用率为" + Double.valueOf(memState.getUsePer()) + "%，可能存在异常，请查看";
                //发送邮件
                sendMail(mailSet.getToMail(), title, commContent);
                //标记已发送过告警信息
                WarnPools.MEM_WARN_MAP.put(key, "1");
                //记录发送信息
                logInfoService.save(title, commContent, StaticKeys.LOG_ERROR);
            } catch (Exception e) {
                log.error("发送内存告警邮件失败：", e);
                logInfoService.save("发送内存告警邮件错误", e.toString(), StaticKeys.LOG_ERROR);
            }
        }
        return false;
    }

    /**
     * 判断系统cpu使用率是否超过98%，超过则发送告警邮件
     *
     * @param cpuState
     * @return
     */
    public static boolean sendCpuWarnInfo(CpuState cpuState) {
        if (StaticKeys.mailSet == null) {
            return false;
        }
        MailSet mailSet = StaticKeys.mailSet;
        if (StaticKeys.NO_SEND_WARN.equals(mailConfig.getAllWarnMail()) || StaticKeys.NO_SEND_WARN.equals(mailConfig.getCpuWarnMail())) {
            return false;
        }
        String key = cpuState.getHostname();
        if (!StringUtils.isEmpty(WarnPools.MEM_WARN_MAP.get(key))) {
            return false;
        }
        if (cpuState.getSys() != null && cpuState.getSys() >= mailConfig.getCpuWarnVal()) {
            try {
                String title = "CPU告警：" + cpuState.getHostname();
                String commContent = "服务器：" + cpuState.getHostname() + ",CPU使用率为" + Double.valueOf(cpuState.getSys()) + "%，可能存在异常，请查看";
                //发送邮件
                sendMail(mailSet.getToMail(), title, commContent);
                //标记已发送过告警信息
                WarnPools.MEM_WARN_MAP.put(key, "1");
                //记录发送信息
                logInfoService.save(title, commContent, StaticKeys.LOG_ERROR);
            } catch (Exception e) {
                log.error("发送内存告警邮件失败：", e);
                logInfoService.save("发送内存告警邮件错误", e.toString(), StaticKeys.LOG_ERROR);
            }
        }
        return false;
    }



    public static String sendMail(String mails, String mailTitle, String mailContent) {
        try {
            HtmlEmail email = new HtmlEmail();
            email.setHostName(StaticKeys.mailSet.getSmtpHost());
            email.setSmtpPort(Integer.valueOf(StaticKeys.mailSet.getSmtpPort()));
            if ("1".equals(StaticKeys.mailSet.getSmtpSSL())) {
                email.setSSL(true);
            }
            email.setAuthenticator(new DefaultAuthenticator(StaticKeys.mailSet.getFromMailName(), StaticKeys.mailSet.getFromPwd()));
            email.setFrom(StaticKeys.mailSet.getFromMailName());//发信者
            email.setSubject("[WGCLOUD] " + mailTitle);//标题
            email.setCharset("UTF-8");//编码格式
            email.setHtmlMsg(mailContent + content_suffix);//内容
            email.addTo(mails.split(";"));
            email.setSentDate(new Date());
            email.send();//发送
            return "success";
        } catch (Exception e) {
            log.error("发送邮件错误：", e);
            logInfoService.save("发送邮件错误", e.toString(), StaticKeys.LOG_ERROR);
            return "error";
        }
    }

}
