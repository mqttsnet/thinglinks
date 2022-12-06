package com.mqttsnet.thinglinks.rule.api.domain.model;


import lombok.Data;

@Data
public class ActionCommandsModel {


    private Integer id;


    private Integer businessType;

    private String ruleIdentification;



    private String productIdentification;
    private String productName;

    private String deviceIdentificaiton;
    private String deviceName;

    private Integer serviceId;
    private String serviceName;


    private Integer commandId;
    private String commandName;

    private String commandBody;
}
