package net.mqtts.link.domain;

import java.math.BigDecimal;

import net.mqtts.common.core.annotation.Excel;
import net.mqtts.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 设备管理对象 mqtts_device
 * 
 * @author mqtts
 * @date 2021-10-22
 */
public class MqttsDevice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @Excel(name = "id")
    private Long id;

    /** 客户端标识 */
    @Excel(name = "客户端标识")
    private String clientId;

    /** 用户名 */
    private String userName;

    /** 密码 */
    private String password;

    /** 认证方式 */
    @Excel(name = "认证方式")
    private String authMode;

    /** 设备标识 */
    @Excel(name = "设备标识")
    private String deviceId;

    /** 设备名称 */
    @Excel(name = "设备名称")
    private String deviceName;

    /** 纬度 */
    private BigDecimal latitude;

    /** 经度 */
    private BigDecimal longitude;

    /** 连接实例 */
    @Excel(name = "连接实例")
    private String connector;

    /** 设备描述 */
    @Excel(name = "设备描述")
    private String deviceDescription;

    /** 设备状态 */
    @Excel(name = "设备状态")
    private String deviceStatus;

    /** 连接状态 */
    @Excel(name = "连接状态")
    private String connectStatus;

    /** 是否遗言 */
    @Excel(name = "是否遗言")
    private String isWill;

    /** 设备标签 */
    @Excel(name = "设备标签")
    private String deviceTags;

    /** 产品型号 */
    @Excel(name = "产品型号")
    private String productId;

    /** 厂商ID */
    @Excel(name = "厂商ID")
    private String manufacturerId;

    /** 产品协议类型 */
    @Excel(name = "产品协议类型")
    private String protocolType;

    /** 设备类型 */
    @Excel(name = "设备类型")
    private String deviceType;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setClientId(String clientId) 
    {
        this.clientId = clientId;
    }

    public String getClientId() 
    {
        return clientId;
    }
    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getUserName() 
    {
        return userName;
    }
    public void setPassword(String password) 
    {
        this.password = password;
    }

    public String getPassword() 
    {
        return password;
    }
    public void setAuthMode(String authMode) 
    {
        this.authMode = authMode;
    }

    public String getAuthMode() 
    {
        return authMode;
    }
    public void setDeviceId(String deviceId) 
    {
        this.deviceId = deviceId;
    }

    public String getDeviceId() 
    {
        return deviceId;
    }
    public void setDeviceName(String deviceName) 
    {
        this.deviceName = deviceName;
    }

    public String getDeviceName() 
    {
        return deviceName;
    }
    public void setLatitude(BigDecimal latitude) 
    {
        this.latitude = latitude;
    }

    public BigDecimal getLatitude() 
    {
        return latitude;
    }
    public void setLongitude(BigDecimal longitude) 
    {
        this.longitude = longitude;
    }

    public BigDecimal getLongitude() 
    {
        return longitude;
    }
    public void setConnector(String connector) 
    {
        this.connector = connector;
    }

    public String getConnector() 
    {
        return connector;
    }
    public void setDeviceDescription(String deviceDescription) 
    {
        this.deviceDescription = deviceDescription;
    }

    public String getDeviceDescription() 
    {
        return deviceDescription;
    }
    public void setDeviceStatus(String deviceStatus) 
    {
        this.deviceStatus = deviceStatus;
    }

    public String getDeviceStatus() 
    {
        return deviceStatus;
    }
    public void setConnectStatus(String connectStatus) 
    {
        this.connectStatus = connectStatus;
    }

    public String getConnectStatus() 
    {
        return connectStatus;
    }
    public void setIsWill(String isWill) 
    {
        this.isWill = isWill;
    }

    public String getIsWill() 
    {
        return isWill;
    }
    public void setDeviceTags(String deviceTags) 
    {
        this.deviceTags = deviceTags;
    }

    public String getDeviceTags() 
    {
        return deviceTags;
    }
    public void setProductId(String productId) 
    {
        this.productId = productId;
    }

    public String getProductId() 
    {
        return productId;
    }
    public void setManufacturerId(String manufacturerId) 
    {
        this.manufacturerId = manufacturerId;
    }

    public String getManufacturerId() 
    {
        return manufacturerId;
    }
    public void setProtocolType(String protocolType) 
    {
        this.protocolType = protocolType;
    }

    public String getProtocolType() 
    {
        return protocolType;
    }
    public void setDeviceType(String deviceType) 
    {
        this.deviceType = deviceType;
    }

    public String getDeviceType() 
    {
        return deviceType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("clientId", getClientId())
            .append("userName", getUserName())
            .append("password", getPassword())
            .append("authMode", getAuthMode())
            .append("deviceId", getDeviceId())
            .append("deviceName", getDeviceName())
            .append("latitude", getLatitude())
            .append("longitude", getLongitude())
            .append("connector", getConnector())
            .append("deviceDescription", getDeviceDescription())
            .append("deviceStatus", getDeviceStatus())
            .append("connectStatus", getConnectStatus())
            .append("isWill", getIsWill())
            .append("deviceTags", getDeviceTags())
            .append("productId", getProductId())
            .append("manufacturerId", getManufacturerId())
            .append("protocolType", getProtocolType())
            .append("deviceType", getDeviceType())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
