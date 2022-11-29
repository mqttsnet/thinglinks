package com.mqttsnet.thinglinks.rule.api.domain.model;

import com.mqttsnet.thinglinks.rule.api.domain.RuleConditions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RuleModel {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String appId;
    private String ruleIdentification;
    private String ruleName;
    private String jobIdentification;
    private String status;
    private Short triggering;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private List<RuleConditionsModel> ruleConditionsModelList;

}
