package com.mqttsnet.thinglinks.link.api.domain.product.model;

import java.util.List;

/**
 * 产品模板详情
 *
 * @author lvwshuai
 * @date 2022年07月15日 14:13
 */
public class ProductTemplateModel {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String appId;
    private String templateName;
    private String status;
    private String remark;
    private List<Services> services;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<Services> getServices() {
        return services;
    }

    public void setServices(List<Services> services) {
        this.services = services;
    }
}
