package com.mqttsnet.thinglinks.rule.api.domain.model;


import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RuleConditionsModel {

    private Long id;
    private Long ruleId;
    private Integer conditionType;

    private String deviceIdentification;
    private String deviceName;
    private String productIdentification;
    private String productName;
    private Long serviceId;
    private String serviceName;


    private Long propertiesId;

    private String propertiesName;

    private String comparisonMode;
    private String comparisonValue;
    private LocalDateTime createTime;

    private String createBy;

    private String updateBy;
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
