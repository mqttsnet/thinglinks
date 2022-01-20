package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.LogInfo;
import com.mqttsnet.thinglinks.monitor.mapper.LogInfoMapper;
import com.mqttsnet.thinglinks.monitor.service.LogInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:LogInfoService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: LogInfoService.java
 */
@Service
public class LogInfoServiceImpl implements LogInfoService {

    private static final Logger logger = LoggerFactory.getLogger(LogInfoServiceImpl.class);

    @Autowired
    private LogInfoMapper logInfoMapper;

    @Override
    public List<LogInfo> selectByParams(Map<String, Object> params) {
        List<LogInfo> list = logInfoMapper.selectByParams(params);
        return list;
    }

    @Override
    public void saveRecord(List<LogInfo> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        for (LogInfo as : recordList) {
            as.setId(UUID.getUUID());
            as.setCreateTime(new Date());
        }
        logInfoMapper.insertList(recordList);
    }

    @Override
    public void save(String hostname, String infoContent, String state) {
        LogInfo logInfo = new LogInfo();
        logInfo.setHostname(hostname);
        logInfo.setInfoContent(infoContent);
        logInfo.setState(state);
        logInfo.setId(UUID.getUUID());
        logInfo.setCreateTime(DateUtils.getNowTime());
        try {
            logInfoMapper.save(logInfo);
        } catch (Exception e) {
            logger.error("保存日志信息异常：", e);
        }
    }

    @Override
    public int countByParams(Map<String, Object> params) {
        return logInfoMapper.countByParams(params);
    }

    @Override
    public int deleteById(String[] ids) {
        return logInfoMapper.deleteById(ids);
    }

    @Override
    public LogInfo selectById(String id) {
        return logInfoMapper.selectById(id);
    }

}
