package com.mqttsnet.thinglinks.monitor.task;

import cn.hutool.core.collection.CollectionUtil;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.*;
import com.mqttsnet.thinglinks.monitor.mapper.*;
import com.mqttsnet.thinglinks.monitor.service.*;
import com.mqttsnet.thinglinks.monitor.util.msg.WarnMailUtil;
import com.mqttsnet.thinglinks.monitor.util.msg.WarnPools;
import com.mqttsnet.thinglinks.monitor.util.staticvar.BatchData;
import com.mqttsnet.thinglinks.monitor.util.staticvar.StaticKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName:ScheduledTask.java
 * @author: shisen
 * @date: 2021月12月29日
 * @Description: ScheduledTask.java
 */
@Component
public class ScheduledTask {

    private Logger logger = LoggerFactory.getLogger(ScheduledTask.class);

    /**
     * 线程池
     */
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 40, 2, TimeUnit.MINUTES, new LinkedBlockingDeque<>());

    @Autowired
    SystemInfoService systemInfoService;
    @Autowired
    DeskStateService deskStateService;
    @Autowired
    LogInfoService logInfoService;
    @Autowired
    AppInfoService appInfoService;
    @Autowired
    CpuStateService cpuStateService;
    @Autowired
    MemStateService memStateService;
    @Autowired
    NetIoStateService netIoStateService;
    @Autowired
    SysLoadStateService sysLoadStateService;
    @Autowired
    TcpStateService tcpStateService;
    @Autowired
    AppStateService appStateService;
    @Autowired
    MailSetService mailSetService;
    @Autowired
    IntrusionInfoService intrusionInfoService;
    @Autowired
    HostInfoService hostInfoService;
    @Autowired
    HeathMonitorService heathMonitorService;
    @Autowired
    SystemInfoMapper systemInfoMapper;
    @Autowired
    CpuStateMapper cpuStateMapper;
    @Autowired
    DeskStateMapper deskStateMapper;
    @Autowired
    MemStateMapper memStateMapper;
    @Autowired
    NetIoStateMapper netIoStateMapper;
    @Autowired
    SysLoadStateMapper sysLoadStateMapper;
    @Autowired
    TcpStateMapper tcpStateMapper;
    @Autowired
    AppInfoMapper appInfoMapper;
    @Autowired
    AppStateMapper appStateMapper;
    @Autowired
    MailSetMapper mailSetMapper;
    @Autowired
    IntrusionInfoMapper intrusionInfoMapper;
    @Autowired
    LogInfoMapper logInfoMapper;

    /**
     * 20秒后执行
     * 初始化操作
     */
    @Scheduled(initialDelay = 20000L, fixedRate = 600 * 60 * 1000)
    public void initTask() {
        try {
            List<MailSet> list = mailSetService.selectMailSetList(new MailSet());
            if (list.size() > 0) {
                StaticKeys.mailSet = list.get(0);
            }
        } catch (Exception e) {
            logger.error("初始化操作错误", e);
        }

    }

    /**
     * 300秒后执行
     * 检测主机是否已经下线，检测进程是否下线
     */
    @Scheduled(initialDelay = 300000L, fixedRate = 20 * 60 * 1000)
    public void hostDownCheckTask() {
        Date date = DateUtils.getNowTime();
        long delayTime = 900 * 1000;
        try {
            List<SystemInfo> list = systemInfoService.selectSystemInfoList(new SystemInfo());
            if (!CollectionUtil.isEmpty(list)) {
                List<SystemInfo> updateList = new ArrayList<SystemInfo>();
                List<LogInfo> logInfoList = new ArrayList<LogInfo>();
                for (SystemInfo systemInfo : list) {
                    Date createTime = systemInfo.getCreateTime();
                    long diff = date.getTime() - createTime.getTime();
                    if (diff > delayTime) {
                        if (!StringUtils.isEmpty(WarnPools.MEM_WARN_MAP.get(systemInfo.getId()))) {
                            continue;
                        }
                        systemInfo.setState(StaticKeys.DOWN_STATE);
                        LogInfo logInfo = new LogInfo();
                        logInfo.setHostname("主机下线：" + systemInfo.getHostname());
                        logInfo.setInfoContent("超过10分钟未上报状态，可能已下线：" + systemInfo.getHostname());
                        logInfo.setState(StaticKeys.LOG_ERROR);
                        logInfoList.add(logInfo);
                        updateList.add(systemInfo);
                        Runnable runnable = () -> {
                            WarnMailUtil.sendHostDown(systemInfo, true);
                        };
                        executor.execute(runnable);
                    } else {
                        if (!StringUtils.isEmpty(WarnPools.MEM_WARN_MAP.get(systemInfo.getId()))) {
                            Runnable runnable = () -> {
                                WarnMailUtil.sendHostDown(systemInfo, false);
                            };
                            executor.execute(runnable);
                        }
                    }
                }
                if (updateList.size() > 0) {
                    systemInfoService.updateRecord(updateList);
                }
                if (logInfoList.size() > 0) {
                    logInfoService.saveRecord(logInfoList);
                }
            }
        } catch (Exception e) {
            logger.error("检测主机是否下线错误", e);
        }

        try {
            Map<String, Object> params = new HashMap<String, Object>();
            List<AppInfo> list = appInfoService.selectByParams(params);
            if (!CollectionUtil.isEmpty(list)) {
                List<AppInfo> updateList = new ArrayList<AppInfo>();
                List<LogInfo> logInfoList = new ArrayList<LogInfo>();
                for (AppInfo appInfo : list) {
                    Date createTime = appInfo.getCreateTime();
                    long diff = date.getTime() - createTime.getTime();
                    if (diff > delayTime) {
                        if (!StringUtils.isEmpty(WarnPools.MEM_WARN_MAP.get(appInfo.getId()))) {
                            continue;
                        }
                        appInfo.setState(StaticKeys.DOWN_STATE);
                        LogInfo logInfo = new LogInfo();
                        logInfo.setHostname("进程下线IP：" + appInfo.getHostname() + "，名称：" + appInfo.getAppName());
                        logInfo.setInfoContent("超过10分钟未上报状态，可能已下线IP：" + appInfo.getHostname() + "，名称：" + appInfo.getAppName() + "，进程ID：" + appInfo.getAppPid());
                        logInfo.setState(StaticKeys.LOG_ERROR);
                        logInfoList.add(logInfo);
                        updateList.add(appInfo);
                        Runnable runnable = () -> {
                            WarnMailUtil.sendAppDown(appInfo, true);
                        };
                        executor.execute(runnable);
                    } else {
                        if (!StringUtils.isEmpty(WarnPools.MEM_WARN_MAP.get(appInfo.getId()))) {
                            Runnable runnable = () -> {
                                WarnMailUtil.sendAppDown(appInfo, false);
                            };
                            executor.execute(runnable);
                        }
                    }
                }
                if (updateList.size() > 0) {
                    appInfoService.updateRecord(updateList);
                }
                if (logInfoList.size() > 0) {
                    logInfoService.saveRecord(logInfoList);
                }
            }
        } catch (Exception e) {
            logger.error("检测进程是否下线错误", e);
        }


    }

    /**
     * 30秒后执行，之后每隔1分钟执行, 单位：ms。
     * 批量提交数据
     */
    @Scheduled(initialDelay = 30000L, fixedRate = 1 * 60 * 1000)
    public synchronized void commitTask() {
        logger.info("批量提交监控数据任务开始----------" + DateUtils.getCurrentDateTime());
        try {
            if (BatchData.APP_STATE_LIST.size() > 0) {
                List<AppState> APP_STATE_LIST = new ArrayList<AppState>();
                APP_STATE_LIST.addAll(BatchData.APP_STATE_LIST);
                BatchData.APP_STATE_LIST.clear();
                appStateService.saveRecord(APP_STATE_LIST);
            }
            if (BatchData.CPU_STATE_LIST.size() > 0) {
                List<CpuState> CPU_STATE_LIST = new ArrayList<CpuState>();
                CPU_STATE_LIST.addAll(BatchData.CPU_STATE_LIST);
                BatchData.CPU_STATE_LIST.clear();
                cpuStateService.saveRecord(CPU_STATE_LIST);
            }
            if (BatchData.MEM_STATE_LIST.size() > 0) {
                List<MemState> MEM_STATE_LIST = new ArrayList<MemState>();
                MEM_STATE_LIST.addAll(BatchData.MEM_STATE_LIST);
                BatchData.MEM_STATE_LIST.clear();
                memStateService.saveRecord(MEM_STATE_LIST);
            }
            if (BatchData.NETIO_STATE_LIST.size() > 0) {
                List<NetIoState> NETIO_STATE_LIST = new ArrayList<NetIoState>();
                NETIO_STATE_LIST.addAll(BatchData.NETIO_STATE_LIST);
                BatchData.NETIO_STATE_LIST.clear();
                netIoStateService.saveRecord(NETIO_STATE_LIST);
            }
            if (BatchData.SYSLOAD_STATE_LIST.size() > 0) {
                List<SysLoadState> SYSLOAD_STATE_LIST = new ArrayList<SysLoadState>();
                SYSLOAD_STATE_LIST.addAll(BatchData.SYSLOAD_STATE_LIST);
                BatchData.SYSLOAD_STATE_LIST.clear();
                sysLoadStateService.saveRecord(SYSLOAD_STATE_LIST);
            }
            if (BatchData.LOG_INFO_LIST.size() > 0) {
                List<LogInfo> LOG_INFO_LIST = new ArrayList<LogInfo>();
                LOG_INFO_LIST.addAll(BatchData.LOG_INFO_LIST);
                BatchData.LOG_INFO_LIST.clear();
                logInfoService.saveRecord(LOG_INFO_LIST);
            }
            if (BatchData.DESK_STATE_LIST.size() > 0) {
                Map<String, Object> paramsDel = new HashMap<String, Object>();

                List<DeskState> DESK_STATE_LIST = new ArrayList<DeskState>();
                DESK_STATE_LIST.addAll(BatchData.DESK_STATE_LIST);
                BatchData.DESK_STATE_LIST.clear();
                List<String> hostnameList = new ArrayList<String>();
                for (DeskState deskState : DESK_STATE_LIST) {
                    if (!hostnameList.contains(deskState.getHostname())) {
                        hostnameList.add(deskState.getHostname());
                    }
                }
                for (String hostname : hostnameList) {
                    paramsDel.put("hostname", hostname);
                    deskStateService.deleteByAccHname(paramsDel);
                }
                deskStateService.saveRecord(DESK_STATE_LIST);
            }
            if (BatchData.SYSTEM_INFO_LIST.size() > 0) {
                List<SystemInfo> SYSTEM_INFO_LIST = new ArrayList<SystemInfo>();
                SYSTEM_INFO_LIST.addAll(BatchData.SYSTEM_INFO_LIST);
                BatchData.SYSTEM_INFO_LIST.clear();
                List<SystemInfo> updateList = new ArrayList<SystemInfo>();
                List<SystemInfo> insertList = new ArrayList<SystemInfo>();
                List<SystemInfo> savedList = systemInfoService.selectSystemInfoList(new SystemInfo());
                for (SystemInfo systemInfo : SYSTEM_INFO_LIST) {
                    boolean issaved = false;
                    for (SystemInfo systemInfoS : savedList) {
                        if (StringUtils.isNotEmpty(systemInfoS.getHostname())) {
                            if (systemInfoS.getHostname().equals(systemInfo.getHostname())) {
                                systemInfo.setId(systemInfoS.getId());
                                updateList.add(systemInfo);
                                issaved = true;
                                break;
                            }
                        }
                    }
                    if (!issaved) {
                        insertList.add(systemInfo);
                    }
                }
                systemInfoService.updateRecord(updateList);
                systemInfoService.saveRecord(insertList);
            }
            if (BatchData.APP_INFO_LIST.size() > 0) {
                Map<String, Object> paramsDel = new HashMap<String, Object>();
                List<AppInfo> APP_INFO_LIST = new ArrayList<AppInfo>();
                APP_INFO_LIST.addAll(BatchData.APP_INFO_LIST);
                BatchData.APP_INFO_LIST.clear();
                List<AppInfo> updateList = new ArrayList<AppInfo>();
                List<AppInfo> insertList = new ArrayList<AppInfo>();
                List<AppInfo> savedList = appInfoService.selectByParams(paramsDel);
                for (AppInfo systemInfo : APP_INFO_LIST) {
                    boolean issaved = false;
                    for (AppInfo systemInfoS : savedList) {
                        if (StringUtils.isNotEmpty(systemInfoS.getHostname()) && StringUtils.isNotEmpty(systemInfoS.getAppPid())) {
                            if (systemInfoS.getHostname().equals(systemInfo.getHostname()) && systemInfoS.getAppPid().equals(systemInfo.getAppPid())) {
                                systemInfo.setId(systemInfoS.getId());
                                updateList.add(systemInfo);
                                issaved = true;
                                break;
                            }
                        }
                    }
                    if (!issaved) {
                        insertList.add(systemInfo);
                    }
                }
                appInfoService.updateRecord(updateList);
                appInfoService.saveRecord(insertList);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("批量提交监控数据错误----------", e);
            logInfoService.save("commitTask", "批量提交监控数据错误：" + e.toString(), StaticKeys.LOG_ERROR);
        }
        logger.info("批量提交监控数据任务结束----------" + DateUtils.getCurrentDateTime());
    }


    /**
     * 每天凌晨1:10执行
     * 删除历史数据，15天
     */
    @Scheduled(cron = "0 10 1 * * ?")
    public void clearHisdataTask() {
        logger.info("定时清空历史数据任务开始----------" + DateUtils.getCurrentDateTime());
        WarnPools.clearOldData();//清空发告警邮件的记录
        String nowTime = DateUtils.getCurrentDateTime();
        //15天前时间
        String thrityDayBefore = DateUtils.getDateBefore(nowTime, 15);
        Map<String, Object> paramsDel = new HashMap<String, Object>();
        try {
            paramsDel.put(StaticKeys.SEARCH_END_TIME, thrityDayBefore);
            //执行删除操作begin
            if (paramsDel.get(StaticKeys.SEARCH_END_TIME) != null && !"".equals(paramsDel.get(StaticKeys.SEARCH_END_TIME))) {
                cpuStateMapper.deleteByAccountAndDate(paramsDel); //删除cpu监控信息
                deskStateMapper.deleteByAccountAndDate(paramsDel); //删除磁盘监控信息
                memStateMapper.deleteByAccountAndDate(paramsDel); //删除内存监控信息
                netIoStateMapper.deleteByAccountAndDate(paramsDel); //删除吞吐率监控信息
                sysLoadStateMapper.deleteByAccountAndDate(paramsDel); //删除负载状态监控信息
                tcpStateMapper.deleteByAccountAndDate(paramsDel); //删除tcp监控信息
                appInfoMapper.deleteByDate(paramsDel);
                appStateMapper.deleteByDate(paramsDel);
                systemInfoMapper.deleteByAccountAndDate(paramsDel);
                intrusionInfoMapper.deleteByAccountAndDate(paramsDel);
                //删除15天前的日志信息
                logInfoMapper.deleteByDate(paramsDel);
                logInfoService.save("定时清空历史数据完成", "定时清空历史数据完成：", StaticKeys.LOG_ERROR);
            }
            //执行删除操作end
        } catch (Exception e) {
            logger.error("定时清空历史数据任务出错：", e);
            logInfoService.save("定时清空历史数据错误", "定时清空历史数据错误：" + e.toString(), StaticKeys.LOG_ERROR);
        }
        logger.info("定时清空历史数据任务结束----------" + DateUtils.getCurrentDateTime());
    }
}
