package com.mqttsnet.thinglinks.collection.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.AppInfo;

import java.util.List;
import java.util.Map;

/**
 * app进程
 */
public interface AppInfoMapper {

    List<AppInfo> selectByParams(Map<String, Object> params);
}
