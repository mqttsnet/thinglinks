package com.mqttsnet.thinglinks.monitor.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.FormatUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.monitor.api.domain.*;
import com.mqttsnet.thinglinks.monitor.dto.*;
import com.mqttsnet.thinglinks.monitor.service.*;
import com.mqttsnet.thinglinks.monitor.util.staticvar.StaticKeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:DashboardCotroller.java
 * @author:shisen
 * @date: 2021年12月29日
 * @Description: DashboardCotroller.java
 */
@RestController
@RequestMapping(value = "/dash")
public class DashboardCotroller extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardCotroller.class);
    @Resource
    DashboardService dashboardService;
    @Resource
    CpuStateService cpuStateService;
    @Resource
    DeskStateService deskStateService;
    @Resource
    MemStateService memStateService;
    @Resource
    NetIoStateService netIoStateService;
    @Resource
    SysLoadStateService sysLoadStateService;
    @Resource
    SystemInfoService systemInfoService;
    @Resource
    AppInfoService appInfoService;
    @Resource
    private LogInfoService logInfoService;
    @Autowired
    HeathMonitorService heathMonitorService;
    @Autowired
    HostInfoService hostInfoService;

    /**
     * 主板页面数据
     *
     * @return
     */
    @GetMapping(value = "/main")
    public AjaxResult mainList() {
        Map<String, Object> params = new HashMap<String, Object>();
        Map datas = new HashMap();
        List<ChartInfo> chartInfoList = new ArrayList<ChartInfo>();
        try {
            int totalSystemInfoSize = systemInfoService.countByParams(params);
            datas.put("totalSystemInfoSize", totalSystemInfoSize);
            int totalSizeApp = appInfoService.countByParams(params);
            datas.put("totalSizeApp", totalSizeApp);
            params.put("memPer", 90);
            int memPerSize_90 = systemInfoService.countByParams(params);
            double a = 0;
            if (totalSystemInfoSize != 0) {
                a = (double) memPerSize_90 / totalSystemInfoSize;
            }
            ChartInfo memPerSize_90_chart = new ChartInfo();
            memPerSize_90_chart.setItem("内存>90%");
            memPerSize_90_chart.setCount(memPerSize_90);
            memPerSize_90_chart.setPercent(FormatUtil.formatDouble(a, 2));
            chartInfoList.add(memPerSize_90_chart);
            params.put("memPer", 50);
            params.put("memPerLe", 90);
            int memPerSize_50_90 = systemInfoService.countByParams(params);
            double b = 0;
            if (totalSystemInfoSize != 0) {
                b = (double) memPerSize_50_90 / totalSystemInfoSize;
            }
            ChartInfo memPerSize_50_90_chart = new ChartInfo();
            memPerSize_50_90_chart.setItem("内存>50%且<90%");
            memPerSize_50_90_chart.setCount(memPerSize_50_90);
            memPerSize_50_90_chart.setPercent(FormatUtil.formatDouble(b, 2));
            chartInfoList.add(memPerSize_50_90_chart);
            params.clear();

            params.put("cpuPer", 90);
            int cpuPerSize_90 = systemInfoService.countByParams(params);
            double c = 0;
            if (totalSystemInfoSize != 0) {
                c = (double) cpuPerSize_90 / totalSystemInfoSize;
            }
            ChartInfo cpuPerSize_90_chart = new ChartInfo();
            cpuPerSize_90_chart.setItem("CPU>90%");
            cpuPerSize_90_chart.setCount(cpuPerSize_90);
            cpuPerSize_90_chart.setPercent(FormatUtil.formatDouble(c, 2));
            chartInfoList.add(cpuPerSize_90_chart);
            params.clear();

            params.put("cpuPer", 90);
            params.put("memPer", 90);
            int perSize_90_90 = systemInfoService.countByParams(params);
            double d = 0;
            if (totalSystemInfoSize != 0) {
                d = (double) perSize_90_90 / totalSystemInfoSize;
            }
            ChartInfo perSize_90_90_chart = new ChartInfo();
            perSize_90_90_chart.setItem("CPU和内存>90%");
            perSize_90_90_chart.setCount(perSize_90_90);
            perSize_90_90_chart.setPercent(FormatUtil.formatDouble(d, 2));
            chartInfoList.add(perSize_90_90_chart);
            params.clear();

            params.put("memPerLe", 50);
            params.put("cpuPerLe", 50);
            int perSize_50_50 = systemInfoService.countByParams(params);
            double e = 0;
            if (totalSystemInfoSize != 0) {
                e = (double) perSize_50_50 / totalSystemInfoSize;
            }
            ChartInfo perSize_50_50_chart = new ChartInfo();
            perSize_50_50_chart.setItem("CPU和内存<50%");
            perSize_50_50_chart.setCount(perSize_50_50);
            perSize_50_50_chart.setPercent(FormatUtil.formatDouble(e, 2));
            chartInfoList.add(perSize_50_50_chart);
            datas.put("chartInfoList", JSONUtil.parseArray(chartInfoList));
            params.clear();

            params.put("cpuPer", 90);
            int memPerSizeApp = appInfoService.countByParams(params);
            datas.put("memPerSizeApp", memPerSizeApp);
            params.clear();

            int logSize = logInfoService.countByParams(params);
            datas.put("logSize", logSize);
            params.clear();

            int heathSize = heathMonitorService.countByParams(params);
            datas.put("heathSize", heathSize);

            params.put("heathStatus", "200");
            int heath200Size = heathMonitorService.countByParams(params);
            datas.put("heath200Size", heath200Size);
            datas.put("heatherrSize", (heathSize - heath200Size));
        } catch (Exception e) {
            logger.error("主面板信息异常：", e);
            logInfoService.save("dash/main", "主面板信息错误：" + e.toString(), StaticKeys.LOG_ERROR);
        }
        return AjaxResult.success(datas);
    }

    /**
     * 根据条件查询host列表
     *
     * @param systemInfo
     * @return
     */
    @GetMapping(value = "/systemInfoList")
    public TableDataInfo systemInfoList(SystemInfo systemInfo) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<SystemInfo> systemInfos = null;
        try {
            startPage();
            systemInfos = systemInfoService.selectSystemInfoList(systemInfo);
            // 设置磁盘总使用率 begin
            for (SystemInfo systemInfo1 : systemInfos) {
                params.put("hostname", systemInfo1.getHostname());
                List<DeskState> deskStates = deskStateService.selectByParams(params);
                try {
                    Double sumSize = 0d;
                    Double useSize = 0d;
                    for (DeskState deskState : deskStates) {
                        if (!StringUtils.isEmpty(deskState.getSize()) && !StringUtils.isEmpty(deskState.getUsed())) {
                            sumSize += Double.valueOf(deskState.getSize().replace("G", ""));
                            useSize += Double.valueOf(deskState.getUsed().replace("G", ""));
                        }
                    }
                    systemInfo1.setDiskPer(0D);
                    if (sumSize != 0) {
                        systemInfo1.setDiskPer(FormatUtil.formatDouble((useSize / sumSize) * 100, 2));
                    }
                } catch (Exception e) {
                    logger.error("设置磁盘总使用率错误", e);
                }
            }
        } catch (Exception e) {
            logger.error("查询服务器列表错误：", e);
            logInfoService.save("查询服务器列表错误", e.toString(), StaticKeys.LOG_ERROR);
        }
        return getDataTable(systemInfos);
    }


    /**
     * 根据IP查询服务器详情信息
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/detail/{id}")
    public AjaxResult hostDetail(@PathVariable(value = "id") String id) {
        // 服务器名称
        String hostname = "";
        Map datas = new HashMap();
        try {
            SystemInfo systemInfo = systemInfoService.selectById(id);
            hostname = systemInfo.getHostname();
            datas.put("systemInfo", systemInfo);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("hostname", systemInfo.getHostname());
            List<DeskState> deskStateList = deskStateService.selectByParams(params);
            datas.put("deskStateList", deskStateList);
        } catch (Exception e) {
            logger.error("服务器详细信息错误：", e);
            logInfoService.save(hostname, "查看服务器详细信息错误", e.toString());
        }
        return AjaxResult.success(datas);
    }

    /**
     * 删除主机
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/del/{id}")
    public AjaxResult delete(@PathVariable(value = "id") String id) {
        String errorMsg = "删除主机信息错误：";
        try {
            String[] ids = id.split(",");
            for (String i : ids) {
                SystemInfo sys = systemInfoService.selectById(i);
                if (!StringUtils.isEmpty(sys.getHostname())) {
                    hostInfoService.deleteByIp(sys.getHostname().split(","));
                }
                logInfoService.save("删除主机：" + sys.getHostname(), sys.getHostname(), StaticKeys.LOG_ERROR);
            }
            systemInfoService.deleteById(ids);
        } catch (Exception e) {
            logger.error(errorMsg, e);
            logInfoService.save(errorMsg, e.toString(), StaticKeys.LOG_ERROR);
        }
        return AjaxResult.success();
    }


    /**
     * 根据IP查询服务器图形报表
     *
     * @param id
     * @param date
     * @return
     */
    @GetMapping(value = "/chart")
    public AjaxResult hostChart(@RequestParam(value = "id", required = true) String id, @RequestParam(value = "date", required = true) String date) {
        // 服务器名称
        String hostname = "";
        Map datas = new HashMap();
        try {
            SystemInfo systemInfo = systemInfoService.selectById(id);
            hostname = systemInfo.getHostname();
            datas.put("systemInfo", systemInfo);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("hostname", systemInfo.getHostname());
            if (StringUtils.isEmpty(date)) {
                date = DateUtils.getCurrentDate();
            }
            dashboardService.setDateParam(date, params);
            datas.put("datenow", date);
            datas.put("dateList", dashboardService.getDateList());
            List<CpuState> cpuStateList = cpuStateService.selectByParams(params);
            datas.put("cpuStateList", JSONUtil.parseArray(cpuStateList));
            datas.put("cpuStateMaxVal", findCpuMaxVal(cpuStateList));
            List<MemState> memStateList = memStateService.selectByParams(params);
            datas.put("memStateList", JSONUtil.parseArray(memStateList));
            List<SysLoadState> ysLoadSstateList = sysLoadStateService.selectByParams(params);
            datas.put("ysLoadSstateList", JSONUtil.parseArray(ysLoadSstateList));
            datas.put("ysLoadSstateMaxVal", findLoadMaxVal(ysLoadSstateList));
            List<NetIoState> netIoStateList = netIoStateService.selectByParams(params);
            List<NetIoStateDto> netIoStateDtoList = toNetIoStateDto(netIoStateList);
            datas.put("netIoStateList", JSONUtil.parseArray(netIoStateDtoList));
            datas.put("netIoStateBytMaxVal", findNetIoStateBytMaxVal(netIoStateDtoList));
            datas.put("netIoStatePckMaxVal", findNetIoStatePckMaxVal(netIoStateDtoList));
        } catch (Exception e) {
            logger.error("服务器图形报表错误：", e);
            logInfoService.save(hostname, "图形报表错误", e.toString());
        }
        return AjaxResult.success(datas);
    }


    private double findCpuMaxVal(List<CpuState> cpuStateList) {
        double maxval = 0;
        if (!CollectionUtil.isEmpty(cpuStateList)) {
            for (CpuState cpuState : cpuStateList) {
                if (null != cpuState.getIdle() && cpuState.getIdle() > maxval) {
                    maxval = cpuState.getIdle();
                }
                if (null != cpuState.getSys() && cpuState.getSys() > maxval) {
                    maxval = cpuState.getSys();
                }
                if (null != cpuState.getIowait() && cpuState.getIowait() > maxval) {
                    maxval = cpuState.getIowait();
                }
            }
        }
        if (maxval == 0) {
            maxval = 100;
        }
        return Math.ceil(maxval);
    }

    private double findLoadMaxVal(List<SysLoadState> ysLoadSstateList) {
        double maxval = 0;
        if (!CollectionUtil.isEmpty(ysLoadSstateList)) {
            for (SysLoadState sysLoadState : ysLoadSstateList) {
                if (null != sysLoadState.getOneLoad() && sysLoadState.getOneLoad() > maxval) {
                    maxval = sysLoadState.getOneLoad();
                }
                if (null != sysLoadState.getFiveLoad() && sysLoadState.getFiveLoad() > maxval) {
                    maxval = sysLoadState.getFiveLoad();
                }
                if (null != sysLoadState.getFifteenLoad() && sysLoadState.getFifteenLoad() > maxval) {
                    maxval = sysLoadState.getFifteenLoad();
                }
            }
        }
        if (maxval == 0) {
            maxval = 1;
        }
        return Math.ceil(maxval);
    }

    private List<NetIoStateDto> toNetIoStateDto(List<NetIoState> netIoStateList) {
        List<NetIoStateDto> dtoList = new ArrayList<>();
        for (NetIoState netIoState : netIoStateList) {
            NetIoStateDto dto = new NetIoStateDto();
            dto.setCreateTime(netIoState.getCreateTime());
            dto.setDateStr(netIoState.getDateStr());
            dto.setHostname(netIoState.getHostname());
            dto.setRxbyt(Integer.valueOf(netIoState.getRxbyt()));
            dto.setRxpck(Integer.valueOf(netIoState.getRxpck()));
            dto.setTxbyt(Integer.valueOf(netIoState.getTxbyt()));
            dto.setTxpck(Integer.valueOf(netIoState.getTxpck()));
            dtoList.add(dto);
        }
        return dtoList;
    }

    private double findNetIoStateBytMaxVal(List<NetIoStateDto> netIoStateList) {
        double maxval = 0;
        if (!CollectionUtil.isEmpty(netIoStateList)) {
            for (NetIoStateDto netIoState : netIoStateList) {
                if (null != netIoState.getRxbyt() && netIoState.getRxbyt() > maxval) {
                    maxval = netIoState.getRxbyt();
                }
                if (null != netIoState.getTxbyt() && netIoState.getTxbyt() > maxval) {
                    maxval = netIoState.getTxbyt();
                }
            }
        }
        if (maxval == 0) {
            maxval = 1;
        }
        return Math.ceil(maxval);
    }

    private double findNetIoStatePckMaxVal(List<NetIoStateDto> netIoStateList) {
        double maxval = 0;
        if (!CollectionUtil.isEmpty(netIoStateList)) {
            for (NetIoStateDto netIoState : netIoStateList) {
                if (null != netIoState.getRxpck() && netIoState.getRxpck() > maxval) {
                    maxval = netIoState.getRxpck();
                }
                if (null != netIoState.getTxpck() && netIoState.getTxpck() > maxval) {
                    maxval = netIoState.getTxpck();
                }
            }
        }
        if (maxval == 0) {
            maxval = 1;
        }
        return Math.ceil(maxval);
    }

}