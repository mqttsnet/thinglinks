package com.mqttsnet.thinglinks.rule.api.domain.model;


import lombok.Data;

@Data
public class ActionCommandsModel {


    private Long id;


    private Integer businessType;

    private String ruleIdentification;



    private String productIdentification;
    private String productName;

    private String deviceIdentificaiton;
    private String deviceName;

    private Long serviceId;
    private String serviceName;


    private Long commandId;
    private String commandName;

    private Object commandBody;
}
