package com.mqttsnet.thinglinks.collection.common.job;

import cn.hutool.json.JSONObject;
import com.mqttsnet.thinglinks.collection.common.recketmq.CollectionProducer;
import com.mqttsnet.thinglinks.collection.common.recketmq.MQConfig;
import com.mqttsnet.thinglinks.collection.mapper.AppInfoMapper;
import com.mqttsnet.thinglinks.collection.util.OshiUtil;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.FormatUtil;
import com.mqttsnet.thinglinks.monitor.api.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.sql.Timestamp;
import java.util.*;

/**
 * 定时推送服务数据
 *
 * @author shisen
 */
@Component
@Slf4j
public class ScheduledJob {

    public static List<AppInfo> appInfoList = Collections.synchronizedList(new ArrayList<AppInfo>());

    private SystemInfo systemInfo = null;

    @Autowired
    private CollectionProducer collectionProducer;

    @Autowired
    private AppInfoMapper appInfoMapper;

    @Autowired
    private MQConfig mqConfig;

    @Value("${base.bindIp}")
    private String bindIp;

    /**
     * 60秒后执行，每隔120秒执行, 单位：ms。
     */
    //@Scheduled(initialDelay = 59 * 1000L, fixedRate = 120 * 1000)
    @Scheduled(initialDelay = 1000L, fixedRate = 1000)
    public void minTask() {
        List<AppInfo> APP_INFO_LIST_CP = new ArrayList<AppInfo>();
        APP_INFO_LIST_CP.addAll(appInfoList);
        JSONObject jsonObject = new JSONObject();
        LogInfo logInfo = new LogInfo();
        Timestamp t = DateUtils.getNowTime();

        logInfo.setHostname(bindIp + "：Agent错误");
        logInfo.setCreateTime(t);
        try {
            oshi.SystemInfo si = new oshi.SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            OperatingSystem os = si.getOperatingSystem();
            // 操作系统信息
            systemInfo = OshiUtil.os(hal.getProcessor(), os, bindIp);
            systemInfo.setCreateTime(t);
            // 文件系统信息
            List<DeskState> deskStateList = OshiUtil.file(t, os.getFileSystem(), bindIp);
            // cpu信息
            CpuState cpuState = OshiUtil.cpu(hal.getProcessor(), bindIp);
            cpuState.setCreateTime(t);
            // 内存信息
            MemState memState = OshiUtil.memory(hal.getMemory(), bindIp);
            memState.setCreateTime(t);
            // 网络流量信息
            NetIoState netIoState = OshiUtil.net(hal, bindIp);
            netIoState.setCreateTime(t);
            // 系统负载信息
            SysLoadState sysLoadState = OshiUtil.getLoadState(systemInfo, hal.getProcessor(), bindIp);
            if (sysLoadState != null) {
                sysLoadState.setCreateTime(t);
            }
            jsonObject.put("cpuState", cpuState);
            jsonObject.put("memState", memState);
            jsonObject.put("netIoState", netIoState);
            if (sysLoadState != null) {
                jsonObject.put("sysLoadState", sysLoadState);
            }
            if (systemInfo != null) {
                systemInfo.setVersionDetail(systemInfo.getVersion() + "，总内存：" + oshi.util.FormatUtil.formatBytes(hal.getMemory().getTotal()));
                systemInfo.setMemPer(memState.getUsePer());
                systemInfo.setCpuPer(cpuState.getSys());
                jsonObject.put("systemInfo", systemInfo);
            }
            jsonObject.put("deskStateList", deskStateList);
            //进程信息
            if (APP_INFO_LIST_CP.size() > 0) {
                List<AppInfo> appInfoResList = new ArrayList<>();
                List<AppState> appStateResList = new ArrayList<>();
                for (AppInfo appInfo : APP_INFO_LIST_CP) {
                    appInfo.setHostname(bindIp);
                    appInfo.setCreateTime(t);
                    appInfo.setState("1");
                    String pid = FormatUtil.getPidByFile(appInfo.getAppType(), appInfo.getAppPid());
                    if (StringUtils.isEmpty(pid)) {
                        continue;
                    }
                    AppState appState = OshiUtil.getLoadPid(pid, os, hal.getMemory());
                    if (appState != null) {
                        appState.setCreateTime(t);
                        appState.setAppInfoId(appInfo.getId());
                        appInfo.setMemPer(appState.getMemPer());
                        appInfo.setCpuPer(appState.getCpuPer());
                        appInfoResList.add(appInfo);
                        appStateResList.add(appState);
                    }
                }
                jsonObject.put("appInfoList", appInfoResList);
                jsonObject.put("appStateList", appStateResList);
            }
            log.debug("---------------" + jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logInfo.setInfoContent(e.toString());
        } finally {
            if (!StringUtils.isEmpty(logInfo.getInfoContent())) {
                jsonObject.put("logInfo", logInfo);
            }
            collectionProducer.senJsonObject(mqConfig.getSystemTopic(), jsonObject.toString());
        }
    }

    /**
     * 30秒后执行，每隔5分钟执行, 单位：ms。
     * 获取监控进程
     */
    //@Scheduled(initialDelay = 28 * 1000L, fixedRate = 300 * 1000)
   /* @Scheduled(initialDelay = 1000L, fixedRate = 1000)
    public void appInfoListTask() {
        JSONObject jsonObject = new JSONObject();
        LogInfo logInfo = new LogInfo();
        Timestamp t = DateUtils.getNowTime();
        logInfo.setHostname(bindIp + "：Agent获取进程列表错误");
        logInfo.setCreateTime(t);
        try {
            JSONObject paramsJson = new JSONObject();
            paramsJson.put("hostname", bindIp);
            List<AppInfo> list = appInfoMapper.selectByParams((Map<String, Object>) new HashMap<>().put("hostname", bindIp));
            if (list != null && list.size() > 0) {
                appInfoList.clear();
                appInfoList = list;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logInfo.setInfoContent(e.toString());
        } finally {
            if (!StringUtils.isEmpty(logInfo.getInfoContent())) {
                jsonObject.put("logInfo", logInfo);
            }
            collectionProducer.senJsonObject(mqConfig.getSystemTopic(), jsonObject.toString());
        }
    }*/
}
