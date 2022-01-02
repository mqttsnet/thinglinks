package com.mqttsnet.thinglinks.monitor.dto;

import com.mqttsnet.thinglinks.monitor.api.domain.BaseEntity;

/**
 * @ClassName:ChartInfo.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: app端口信息
 */
public class ChartInfo extends BaseEntity {

    private static final long serialVersionUID = -2913111613773445949L;

    /**
     * host名称
     */
    private String item;

    /**
     * 应用进程ID
     */
    private Integer count;

    /**
     * 应用进程名称
     */
    private Double percent;

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }
}