package com.mqttsnet.thinglinks.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.sdk.param.device.IotNorthboundDeviceDataReportParam;
import com.mqttsnet.thinglinks.sdk.param.device.IotNorthboundDeviceDeleteSubDeviceParam;
import com.mqttsnet.thinglinks.sdk.param.device.IotNorthboundDeviceManagerCreateParam;
import com.mqttsnet.thinglinks.sdk.param.device.IotNorthboundDeviceManagerGetDetailParam;
import com.mqttsnet.thinglinks.sdk.param.device.IotNorthboundDeviceManagerIssueCommandParam;
import com.mqttsnet.thinglinks.sdk.param.device.IotNorthboundDeviceManagerQueryShadowParam;
import com.mqttsnet.thinglinks.sdk.param.device.IotNorthboundDeviceManagerUpdateStatusParam;
import com.mqttsnet.thinglinks.sdk.param.device.IotNorthboundDeviceQueryParam;
import com.mqttsnet.thinglinks.sdk.param.device.IotNorthboundDeviceUpdateSubDeviceConnectStatusParam;
import com.mqttsnet.thinglinks.sdk.request.device.IotNorthboundDeviceDataReportRequest;
import com.mqttsnet.thinglinks.sdk.request.device.IotNorthboundDeviceDeleteSubDeviceRequest;
import com.mqttsnet.thinglinks.sdk.request.device.IotNorthboundDeviceGetDetailRequest;
import com.mqttsnet.thinglinks.sdk.request.device.IotNorthboundDeviceIssueCommandRequest;
import com.mqttsnet.thinglinks.sdk.request.device.IotNorthboundDeviceManagerCreateRequest;
import com.mqttsnet.thinglinks.sdk.request.device.IotNorthboundDeviceQueryRequest;
import com.mqttsnet.thinglinks.sdk.request.device.IotNorthboundDeviceQueryShadowRequest;
import com.mqttsnet.thinglinks.sdk.request.device.IotNorthboundDeviceUpdateStatusRequest;
import com.mqttsnet.thinglinks.sdk.request.device.IotNorthboundDeviceUpdateSubDeviceConnectStatusRequest;
import com.mqttsnet.thinglinks.sdk.response.device.IotNorthboundDeviceDataReportResponse;
import com.mqttsnet.thinglinks.sdk.response.device.IotNorthboundDeviceDeleteSubDeviceResponse;
import com.mqttsnet.thinglinks.sdk.response.device.IotNorthboundDeviceGetDetailResponse;
import com.mqttsnet.thinglinks.sdk.response.device.IotNorthboundDeviceIssueCommandResponse;
import com.mqttsnet.thinglinks.sdk.response.device.IotNorthboundDeviceManagerCreateResponse;
import com.mqttsnet.thinglinks.sdk.response.device.IotNorthboundDeviceQueryResponse;
import com.mqttsnet.thinglinks.sdk.response.device.IotNorthboundDeviceQueryShadowResponse;
import com.mqttsnet.thinglinks.sdk.response.device.IotNorthboundDeviceUpdateStatusResponse;
import com.mqttsnet.thinglinks.sdk.response.device.IotNorthboundDeviceUpdateSubDeviceConnectStatusResponse;
import com.mqttsnet.thinglinks.sdkcore.client.OpenClient;
import com.mqttsnet.thinglinks.sdkcore.common.Result;
import junit.framework.TestCase;

/**
 * Description:
 * 北向API-设备管理测试类
 * 测试SDK
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/6/23
 */
public class IotNorthboundDeviceManagerTest extends TestCase {

    //租户ID,以下appId 归属此租户
    String tenantId = "1";

    String url = "http://localhost:18750/api";
    String appId = "20250622646351801644697600";
    /**
     * 开发者私钥
     */
    String privateKeyIsv = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDJR5pDEncZPAWEIYgTLLC2jJNBO0eX401vrlzPzKVdbFDjr+m+tYkB6ysfLZZxmMZsjZS10ZbEwko2Z7F+VgRkYhqHFa5yiimgoHCJcUSgBnsoCyBDtYUdzzkU6IFD/VRx02ssxaOWebGi3aJ3qfYj9ThDiSvrZ5JhFIJNA4Qw98894UIG4P8Ypqf95/ztLh13LhRzlbPmzsDlru1tCixObidq1/r5f2NpJNF0H94JerGMZC3550+s9hlcf0NTpdaeMnVsyBzXkbiZFPwn4iGUIezroeswhIgdxuv+nKDlEXGE71+0fOM7asgGdiBFs+9Q1bWfZtgFcT15XaiD91GrAgMBAAECggEAUJAr0w/Xgs6u6ImhVQdsvcx7fj8Tc1yEGKECPhxLzh52LAQzu2Ue7xkpW+Pb1SEQvs9WiAXZYmzf1nHfrdERFfrcYlhPyEG70rKVCLZBAQpHDiqR4fgMTXetgcxkPQnvXFYCjluXFDomWyl6B4qMXi0fNYz4etMsWFYkp12ycgRwQEltkhmnXREkexEKn64zQ4w3CqpYa6SDsbPbIuWU8TRKwMRu1O34GKjYQzT0t7fMfeVLIkgwRt2NvTAHdOA6WJpRJZbu0BGRJ/MoIgumfY0dZGkMKgQ8Ss2/fgAC4ezb2uQq7VEIkr+X5MBoTfd7ure1lJaXaRBO5yi4zx1AuQKBgQDXD7OxE3FCH/svxazOUmIZBQ8eKSZ5WtrxN25inBF7FUAt7JzkH658Nl9bI8mjRywMqIO5+TGy1EbHmRT3NXFoGE7PKFpYJQq3uAWTFUpmm6jq3Hz0K9loZpNHE2X7vQmuNckmH7n52DVoHdrS2SNwMMnVDsbnoVH+9wOTnX7IiQKBgQDvmExlqJhsxILqH3AETk47FyA12945i+Y7my/Zwag0URKmKb97fpph/PHZGLY46u4RvjaHjgF2CIgA0VRNri5teHz6ycxMJ+c0ALsW8k0qFSn0Judl3P9/2oykETxJgf5i7J6fLhWoGGgzEPuOtboPMbxTmquU80ELsqszbwgTkwKBgQDVjvMaWTsztbxioQJL08U0ITD0+1oIUi9uL2Q/Kcm2FtEniXn2kntbP0cLowsdR5S3cTN1nr46Xc93b01BeuGFw/WmguTL+OSesW9fslmycMqZhp/SfCSlJo8DmoDALeUKCMEZSZINRyUpodPlYthtFRWnq8faxfg9np7/m/sFOQKBgQDPobIDHxoImDvDXL8cXCfbeW0Gf6WTRTOeUBwln3d+7ws0SFWSVeLALA4CTzILHTx2z9o9msiVW0tROhbOwrWlVJcqVprfHG12WXPIrO5GG9Uhql05n00jpWff/re6FBc570h/Nda6t3wFVSBLSh0Vccp8wTsUd8HUu1810XC0VQKBgDrOLBKAgdR+ckJuw/y0Fa3SJWCBRrbPAZfUtOQ4n3+DQ825X28/ycn5h5ArWeEXd6zftOrERJtGLujl+Nwc/mv8C/EeOQig9HfqASFKlL4XnMTuMYRglK5ItU9+B1FuwGnzzmsFzrch8ymdMgAr4auwySfvH7zeBEM4CM5eHNmc";
    /**
     * 开放平台提供的公钥
     * 前往【开发运营系统】--开放平台，ISV管理--秘钥管理，生成平台提供的公私钥，然后把【平台公钥】放到这里
     */
    String publicKeyPlatform = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzSNidMVQg73jPrGpn4m5xqYWI7+zxkYeZJ2W3M5/mamqh98xkcOI3k4BqiF75PJLqJzDPwAmC/15yao3jcUU5/jOpe/2PVZQic7xwxiv44Jg2uOUdpv8Y/JwiRRJY80UABlK8wzr60h8+Mxx0h4FiO44XdIOmPiCg/cElnqKR0kOMMINVBoex4BVjGKxoLKuFRzYFxtyRkd/dEee9+TJBKl/W6B3swWYRoKjg55RoogfFjEp2I4CSm2GE23qHLNB+uZsB0rB1FCHbkDXeC2tTa5zbX9NjjGS4LECH0qDqd5qWvldWLkEXikKP0bWuqVukRV0lN113UdpIIDZeM/xpwIDAQAB";

    // 声明一个就行
    OpenClient client = new OpenClient(url, appId, privateKeyIsv, publicKeyPlatform);


    /**
     * 测试创建普通设备
     */
    public void testCreateOrdinaryDevice() {
        // 1. 构建请求参数
        IotNorthboundDeviceManagerCreateRequest request = new IotNorthboundDeviceManagerCreateRequest();

        // 必填字段
        request.setDeviceIdentification("NORMAL-" + System.currentTimeMillis());
        request.setDeviceName("普通设备_" + System.currentTimeMillis());
        request.setProductIdentification("4001308136820736");
        request.setNodeType(0); // 0-普通设备
        request.setAppId("thinglinks"); // 应用ID（必填）

        // 认证信息
        request.setAuthMode(0); // 0-用户名密码认证
        request.setClientId("NORMAL_CLIENT_" + System.currentTimeMillis() + "@" + tenantId);
        request.setUserName("normal_user");
        request.setPassword("normal_pass123");

        // 加密配置（明文传输）
        request.setEncryptMethod(0); // 0-明文传输
        request.setEncryptKey("");
        request.setEncryptVector("");
        request.setSignKey("1234"); // 签名密钥（明文传输时可为空）

        // 设备状态：1-已启用，连接状态：0-未连接
        request.setDeviceStatus(1);
        request.setConnectStatus(0);

        // 版本信息
        request.setSwVersion("v1.0.0");
        request.setFwVersion("v1.0.0");
        request.setDeviceSdkVersion("v1.0");

        // 选填字段
        request.setDescription("自动化测试创建的普通设备 - 明文传输");

        // 2. 封装API参数
        IotNorthboundDeviceManagerCreateParam param = new IotNorthboundDeviceManagerCreateParam();
        param.setBizModel(request);

        try {
            // 3. 打印请求参数
            System.out.println("=== 普通设备创建请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            // 4. 调用接口
            System.out.println("正在调用普通设备创建接口...");
            Result<IotNorthboundDeviceManagerCreateResponse> result = client.execute(param);

            // 5. 处理结果
            if (result.isSuccess()) {
                IotNorthboundDeviceManagerCreateResponse response = result.getData();
                System.out.println("=== 普通设备创建成功 ===");
                System.out.println("设备标识: " + response.getDeviceIdentification());
                System.out.println("设备名称: " + response.getDeviceName());
                System.out.println("客户端ID: " + response.getClientId());
                System.out.println("认证方式: " + response.getAuthMode() + " (0=用户名密码)");
                System.out.println("设备类型: " + response.getNodeType() + " (0=普通设备)");
                System.out.println("产品标识: " + response.getProductIdentification());
                System.out.println("完整响应: " + JSON.toJSONString(response));
            } else {
                System.out.println("=== 普通设备创建失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }


    /**
     * 测试创建网关设备
     */
    public void testCreateGatewayDevice() {
        // 1. 构建请求参数
        IotNorthboundDeviceManagerCreateRequest request = new IotNorthboundDeviceManagerCreateRequest();

        // 设置网关设备
        request.setDeviceIdentification("GATEWAY-" + System.currentTimeMillis());
        request.setDeviceName("测试网关设备_" + System.currentTimeMillis());
        request.setProductIdentification("4001308136820736");
        request.setNodeType(1); // 1-网关设备
        request.setAppId("thinglinks"); // 应用ID（必填）

        // 认证信息
        request.setAuthMode(0); // 0-用户名密码认证
        request.setClientId("GATEWAY_CLIENT_" + System.currentTimeMillis() + "@" + tenantId);
        request.setUserName("gateway_admin");
        request.setPassword("gateway_pass123");

        // 加密配置
        request.setEncryptMethod(0); // 0-明文传输
        request.setEncryptKey("");
        request.setEncryptVector("");
        request.setSignKey("1234");

        // 设备状态：1-已启用，连接状态：0-未连接
        request.setDeviceStatus(1);
        request.setConnectStatus(0);

        // 版本信息
        request.setSwVersion("v1.0.0");
        request.setFwVersion("v1.0.0");
        request.setDeviceSdkVersion("v1.0");

        request.setDescription("自动化测试创建的网关设备");

        // 2. 封装API参数
        IotNorthboundDeviceManagerCreateParam param = new IotNorthboundDeviceManagerCreateParam();
        param.setBizModel(request);

        try {
            // 3. 打印请求参数
            System.out.println("=== 网关设备创建请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            // 4. 调用接口
            System.out.println("正在调用网关设备创建接口...");
            Result<IotNorthboundDeviceManagerCreateResponse> result = client.execute(param);

            // 5. 处理结果
            if (result.isSuccess()) {
                IotNorthboundDeviceManagerCreateResponse response = result.getData();
                System.out.println("=== 网关设备创建成功 ===");
                System.out.println("设备标识: " + response.getDeviceIdentification());
                System.out.println("设备名称: " + response.getDeviceName());
                System.out.println("客户端ID: " + response.getClientId());
                System.out.println("认证方式: " + response.getAuthMode() + " (0=用户名密码)");
                System.out.println("设备类型: " + response.getNodeType() + " (1=网关设备)");
                System.out.println("产品标识: " + response.getProductIdentification());
                System.out.println("完整响应: " + JSON.toJSONString(response));
            } else {
                System.out.println("=== 网关设备创建失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }


    /**
     * 测试创建子设备（依赖于已存在的网关设备）
     * 注意：运行此方法前需要先运行 testCreateGatewayDevice 创建网关设备
     */
    public void testCreateSubDevice() {
        // 1. 构建请求参数
        IotNorthboundDeviceManagerCreateRequest request = new IotNorthboundDeviceManagerCreateRequest();

        // 设置子设备
        request.setDeviceIdentification("SUB-" + System.currentTimeMillis());
        request.setDeviceName("测试子设备_" + System.currentTimeMillis());
        request.setProductIdentification("4001308136820736");
        request.setNodeType(2); // 2-子设备
        request.setAppId("thinglinks"); // 应用ID（必填）
        // ⚠️ 重要：gatewayId 需要填写已存在的网关设备的设备标识
        // 先运行testCreateGatewayDevice获取实际网关设备标识
        request.setGatewayId("GATEWAY-" + System.currentTimeMillis());

        // 认证信息
        request.setAuthMode(0);
        request.setClientId("SUB_CLIENT_" + System.currentTimeMillis() + "@" + tenantId);
        request.setUserName("sub_user");
        request.setPassword("sub_pass123");

        // 加密配置
        request.setEncryptMethod(0);
        request.setEncryptKey("");
        request.setEncryptVector("");
        request.setSignKey("1234");

        // 设备状态
        request.setDeviceStatus(1);
        request.setConnectStatus(0);

        // 版本信息
        request.setSwVersion("v1.0.0");
        request.setFwVersion("v1.0.0");
        request.setDeviceSdkVersion("v1.0");

        request.setDescription("自动化测试创建的子设备");

        // 2. 封装API参数
        IotNorthboundDeviceManagerCreateParam param = new IotNorthboundDeviceManagerCreateParam();
        param.setBizModel(request);

        try {
            // 3. 打印请求参数
            System.out.println("=== 子设备创建请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            // 4. 调用接口
            System.out.println("正在调用子设备创建接口...");
            Result<IotNorthboundDeviceManagerCreateResponse> result = client.execute(param);

            // 5. 处理结果
            if (result.isSuccess()) {
                IotNorthboundDeviceManagerCreateResponse response = result.getData();
                System.out.println("=== 子设备创建成功 ===");
                System.out.println("设备标识: " + response.getDeviceIdentification());
                System.out.println("设备名称: " + response.getDeviceName());
                System.out.println("客户端ID: " + response.getClientId());
                System.out.println("认证方式: " + response.getAuthMode() + " (0=用户名密码)");
                System.out.println("设备类型: " + response.getNodeType() + " (2=子设备)");
                System.out.println("网关设备ID: " + response.getGatewayId());
                System.out.println("产品标识: " + response.getProductIdentification());
                System.out.println("完整响应: " + JSON.toJSONString(response));
            } else {
                System.out.println("=== 子设备创建失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }


    /**
     * 测试查询设备详情
     */
    public void testGetDeviceDetail() {
        // 假设这是已创建的设备标识
        String deviceIdentification = "NORMAL-1770278192236"; // 请替换为实际的设备标识

        // 1. 构建请求参数
        IotNorthboundDeviceGetDetailRequest request = new IotNorthboundDeviceGetDetailRequest();
        request.setDeviceIdentification(deviceIdentification);

        // 2. 封装API参数
        IotNorthboundDeviceManagerGetDetailParam param = new IotNorthboundDeviceManagerGetDetailParam();
        param.setBizModel(request);

        try {
            // 3. 打印请求参数
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            // 4. 调用接口
            System.out.println("正在调用查询设备详情接口...");
            System.out.println("设备标识: " + deviceIdentification);
            Result<IotNorthboundDeviceGetDetailResponse> result = client.execute(param);

            // 5. 处理结果
            if (result.isSuccess()) {
                IotNorthboundDeviceGetDetailResponse response = result.getData();
                System.out.println("=== 查询成功 ===");
                System.out.println("设备标识: " + response.getDeviceIdentification());
                System.out.println("设备名称: " + response.getDeviceName());
                System.out.println("客户端标识: " + response.getClientId());
                System.out.println("用户名: " + response.getUserName());
                System.out.println("认证方式: " + response.getAuthMode());
                System.out.println("设备状态: " + response.getDeviceStatus());
                System.out.println("连接状态: " + response.getConnectStatus());
                System.out.println("设备类型: " + response.getNodeType());
                System.out.println("产品标识: " + response.getProductIdentification());
                System.out.println("完整响应: " + JSON.toJSONString(response));
            } else {
                System.out.println("=== 查询失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }


    /**
     * 测试下发设备命令（串行）
     */
    public void testIssueCommandSerial() {
        // 1. 构建请求参数
        IotNorthboundDeviceIssueCommandRequest request = new IotNorthboundDeviceIssueCommandRequest();

        // 添加串行命令
        List<IotNorthboundDeviceIssueCommandRequest.CommandItem> serialCommands = new ArrayList<>();
        IotNorthboundDeviceIssueCommandRequest.CommandItem command1 = new IotNorthboundDeviceIssueCommandRequest.CommandItem();
        command1.setDeviceIdentification("NORMAL-1770278192236");
        command1.setProductIdentification("4001308136820736");
        command1.setMsgType("cloudReq");
        command1.setServiceCode("service001");
        command1.setCmd("command001");
        command1.setParams(Map.of("power", "on"));
        serialCommands.add(command1);

        request.setSerial(serialCommands);

        // 2. 封装API参数
        IotNorthboundDeviceManagerIssueCommandParam param = new IotNorthboundDeviceManagerIssueCommandParam();
        param.setBizModel(request);

        try {
            // 3. 打印请求参数
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            // 4. 调用接口
            System.out.println("正在调用下发设备命令接口（串行）...");
            Result<IotNorthboundDeviceIssueCommandResponse> result = client.execute(param);

            // 5. 处理结果
            if (result.isSuccess()) {
                IotNorthboundDeviceIssueCommandResponse response = result.getData();
                System.out.println("=== 命令下发成功 ===");
                List<IotNorthboundDeviceIssueCommandResponse.CommandResultItem> commandResults = response.getCommandResults();
                System.out.println("结果数量: " + (commandResults != null ? commandResults.size() : 0));
                if (commandResults != null && !commandResults.isEmpty()) {
                    for (IotNorthboundDeviceIssueCommandResponse.CommandResultItem item : commandResults) {
                        System.out.println("----------------------------------------");
                        System.out.println("设备标识: " + item.getDeviceIdentification());
                        System.out.println("命令标识: " + item.getCommandIdentification());
                        System.out.println("命令类型: " + item.getCommandType());
                        System.out.println("状态: " + item.getStatus());
                        System.out.println("内容: " + item.getContent());
                        System.out.println("备注: " + item.getRemark());
                    }
                    System.out.println("========================================");
                    System.out.println("完整响应: " + JSON.toJSONString(response));
                }
            } else {
                System.out.println("=== 命令下发失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }

    /**
     * 测试下发设备命令（并行）
     */
    public void testIssueCommandParallel() {
        // 1. 构建请求参数
        IotNorthboundDeviceIssueCommandRequest request = new IotNorthboundDeviceIssueCommandRequest();

        // 添加并行命令
        List<IotNorthboundDeviceIssueCommandRequest.CommandItem> parallelCommands = new ArrayList<>();

        // 命令1
        IotNorthboundDeviceIssueCommandRequest.CommandItem command1 = new IotNorthboundDeviceIssueCommandRequest.CommandItem();
        command1.setDeviceIdentification("NORMAL-" + System.currentTimeMillis());
        command1.setProductIdentification("PROD_001");
        command1.setMsgType("cloudReq");
        command1.setServiceCode("service001");
        command1.setCmd("queryStatus");
        command1.setParams(Map.of("action", "query"));
        parallelCommands.add(command1);

        // 命令2
        IotNorthboundDeviceIssueCommandRequest.CommandItem command2 = new IotNorthboundDeviceIssueCommandRequest.CommandItem();
        command2.setDeviceIdentification("NORMAL-" + System.currentTimeMillis());
        command2.setProductIdentification("PROD_001");
        command2.setMsgType("cloudReq");
        command2.setServiceCode("service002");
        command2.setCmd("getConfig");
        command2.setParams(Map.of("action", "get"));
        parallelCommands.add(command2);

        request.setParallel(parallelCommands);

        // 2. 封装API参数
        IotNorthboundDeviceManagerIssueCommandParam param = new IotNorthboundDeviceManagerIssueCommandParam();
        param.setBizModel(request);

        try {
            // 3. 打印请求参数
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            // 4. 调用接口
            System.out.println("正在调用下发设备命令接口（并行）...");
            System.out.println("命令数量: " + parallelCommands.size());
            Result<IotNorthboundDeviceIssueCommandResponse> result = client.execute(param);

            // 5. 处理结果
            if (result.isSuccess()) {
                IotNorthboundDeviceIssueCommandResponse response = result.getData();
                System.out.println("=== 命令下发成功 ===");
                List<IotNorthboundDeviceIssueCommandResponse.CommandResultItem> commandResults = response.getCommandResults();
                System.out.println("结果数量: " + (commandResults != null ? commandResults.size() : 0));
                if (commandResults != null && !commandResults.isEmpty()) {
                    for (IotNorthboundDeviceIssueCommandResponse.CommandResultItem item : commandResults) {
                        System.out.println("----------------------------------------");
                        System.out.println("设备标识: " + item.getDeviceIdentification());
                        System.out.println("命令标识: " + item.getCommandIdentification());
                        System.out.println("命令类型: " + item.getCommandType());
                        System.out.println("状态: " + item.getStatus());
                        System.out.println("内容: " + item.getContent());
                        System.out.println("备注: " + item.getRemark());
                    }
                    System.out.println("========================================");
                    System.out.println("完整响应: " + JSON.toJSONString(response));
                }
            } else {
                System.out.println("=== 命令下发失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }

    /**
     * 测试查询设备影子
     */
    public void testQueryDeviceShadow() {
        // 1. 构建请求参数
        IotNorthboundDeviceQueryShadowRequest request = new IotNorthboundDeviceQueryShadowRequest();
        request.setDeviceIdentification("4001308136820736_2000000"); // 请替换为实际的设备标识

        // 可选参数
//        request.setStartTime(1622552643000000000L);
//        request.setEndTime(1622552643000000000L);
        request.setServiceCode("default_attributes_controls");

        // 2. 封装API参数
        IotNorthboundDeviceManagerQueryShadowParam param = new IotNorthboundDeviceManagerQueryShadowParam();
        param.setBizModel(request);

        try {
            // 3. 打印请求参数
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            // 4. 调用接口
            System.out.println("正在调用查询设备影子接口...");
            System.out.println("设备标识: " + request.getDeviceIdentification());
            Result<IotNorthboundDeviceQueryShadowResponse> result = client.execute(param);

            // 5. 处理结果
            if (result.isSuccess()) {
                IotNorthboundDeviceQueryShadowResponse response = result.getData();
                System.out.println("=== 查询成功 ===");
                System.out.println("应用ID: " + response.getAppId());
                System.out.println("模板ID: " + response.getTemplateId());
                System.out.println("产品标识: " + response.getProductIdentification());
                System.out.println("产品名称: " + response.getProductName());
                System.out.println("产品类型: " + response.getProductType());
                System.out.println("厂商ID: " + response.getManufacturerId());
                System.out.println("厂商名称: " + response.getManufacturerName());
                System.out.println("产品型号: " + response.getModel());
                System.out.println("数据格式: " + response.getDataFormat());
                System.out.println("设备类型: " + response.getDeviceType());
                System.out.println("协议类型: " + response.getProtocolType());
                System.out.println("产品版本: " + response.getActiveVersionNo());
                System.out.println("图标: " + response.getIcon());
                System.out.println("产品描述: " + response.getRemark());
                System.out.println("创建人组织: " + response.getCreatedOrgId());

                // 打印服务列表
                if (response.getServices() != null && !response.getServices().isEmpty()) {
                    System.out.println("\n=== 产品服务列表 ===");
                    System.out.println("服务数量: " + response.getServices().size());
                    int index = 1;
                    for (IotNorthboundDeviceQueryShadowResponse.ServiceItem service : response.getServices()) {
                        System.out.println("\n服务 " + index + ":");
                        System.out.println("  服务ID: " + service.getId());
                        System.out.println("  产品ID: " + service.getProductId());
                        System.out.println("  服务编码: " + service.getServiceCode());
                        System.out.println("  服务名称: " + service.getServiceName());
                        System.out.println("  服务类型: " + service.getServiceType());
                        System.out.println("  服务状态: " + service.getServiceStatus());
                        System.out.println("  服务描述: " + service.getDescription());
                        System.out.println("  备注: " + service.getRemark());
                        System.out.println("  创建人组织: " + service.getCreatedOrgId());
                        System.out.println("  命令数量: " + (service.getCommands() != null ? service.getCommands().size() : 0));
                        System.out.println("  属性数量: " + (service.getProperties() != null ? service.getProperties().size() : 0));
                        index++;
                    }
                }
                System.out.println("\n========================================");
                System.out.println("完整响应: " + JSON.toJSONString(response));
            } else {
                System.out.println("=== 查询失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }

    /**
     * 测试修改设备状态
     */
    public void testUpdateDeviceStatus() {
        // 假设这是已创建的设备标识
        String deviceIdentification = "NORMAL-1737212000000"; // 请替换为实际的设备标识
        Integer newStatus = 1; // 0-未激活、1-已激活、2-已禁用

        // 1. 构建请求参数
        IotNorthboundDeviceUpdateStatusRequest request = new IotNorthboundDeviceUpdateStatusRequest();
        request.setDeviceIdentification(deviceIdentification);
        request.setStatus(newStatus);

        // 2. 封装API参数
        IotNorthboundDeviceManagerUpdateStatusParam param = new IotNorthboundDeviceManagerUpdateStatusParam();
        param.setBizModel(request);

        try {
            // 3. 打印请求参数
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            // 4. 调用接口
            System.out.println("正在调用修改设备状态接口...");
            System.out.println("设备标识: " + deviceIdentification);
            System.out.println("新状态: " + newStatus + " (" + getStatusDesc(newStatus) + ")");
            Result<IotNorthboundDeviceUpdateStatusResponse> result = client.execute(param);

            // 5. 处理结果
            if (result.isSuccess()) {
                IotNorthboundDeviceUpdateStatusResponse response = result.getData();
                System.out.println("=== 修改成功 ===");
                System.out.println("操作结果: " + (Boolean.TRUE.equals(response.getSuccess()) ? "成功" : "失败"));
                System.out.println("设备标识: " + response.getDeviceIdentification());
                System.out.println("设备名称: " + response.getDeviceName());
                System.out.println("客户端标识: " + response.getClientId());
                System.out.println("用户名: " + response.getUserName());
                System.out.println("应用ID: " + response.getAppId());
                System.out.println("更新前状态: " + response.getPreviousStatus() + " (" + getStatusDesc(response.getPreviousStatus()) + ")");
                System.out.println("更新后状态: " + response.getCurrentStatus() + " (" + getStatusDesc(response.getCurrentStatus()) + ")");
                System.out.println("连接状态: " + response.getConnectStatus() + " (" + getConnectStatusDesc(response.getConnectStatus()) + ")");
                System.out.println("设备类型: " + response.getNodeType() + " (" + getNodeTypeDesc(response.getNodeType()) + ")");
                System.out.println("产品标识: " + response.getProductIdentification());
                System.out.println("产品名称: " + response.getProductName());
                System.out.println("软件版本: " + response.getSwVersion());
                System.out.println("固件版本: " + response.getFwVersion());
                System.out.println("SDK版本: " + response.getDeviceSdkVersion());
                System.out.println("设备描述: " + response.getDescription());
                System.out.println("网关设备ID: " + response.getGatewayId());
                System.out.println("备注: " + response.getRemark());
                System.out.println("更新时间: " + response.getUpdateTime());
                System.out.println("----------------------------------------");
                System.out.println("完整响应: " + JSON.toJSONString(response));
            } else {
                System.out.println("=== 修改失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }

    /**
     * 测试激活设备
     */
    public void testActivateDevice() {
        String deviceIdentification = "NORMAL-1737212000000"; // 请替换为实际的设备标识

        // 1. 构建请求参数
        IotNorthboundDeviceUpdateStatusRequest request = new IotNorthboundDeviceUpdateStatusRequest();
        request.setDeviceIdentification(deviceIdentification);
        request.setStatus(1); // 已激活

        // 2. 封装API参数
        IotNorthboundDeviceManagerUpdateStatusParam param = new IotNorthboundDeviceManagerUpdateStatusParam();
        param.setBizModel(request);

        try {
            System.out.println("=== 激活设备 ===");
            System.out.println("设备标识: " + deviceIdentification);
            Result<IotNorthboundDeviceUpdateStatusResponse> result = client.execute(param);

            if (result.isSuccess()) {
                IotNorthboundDeviceUpdateStatusResponse response = result.getData();
                System.out.println("激活结果: " + (Boolean.TRUE.equals(response.getSuccess()) ? "成功" : "失败"));
            } else {
                System.out.println("激活失败: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }

    /**
     * 测试禁用设备
     */
    public void testDisableDevice() {
        String deviceIdentification = "NORMAL-1737212000000"; // 请替换为实际的设备标识

        // 1. 构建请求参数
        IotNorthboundDeviceUpdateStatusRequest request = new IotNorthboundDeviceUpdateStatusRequest();
        request.setDeviceIdentification(deviceIdentification);
        request.setStatus(2); // 已禁用

        // 2. 封装API参数
        IotNorthboundDeviceManagerUpdateStatusParam param = new IotNorthboundDeviceManagerUpdateStatusParam();
        param.setBizModel(request);

        try {
            System.out.println("=== 禁用设备 ===");
            System.out.println("设备标识: " + deviceIdentification);
            Result<IotNorthboundDeviceUpdateStatusResponse> result = client.execute(param);

            if (result.isSuccess()) {
                IotNorthboundDeviceUpdateStatusResponse response = result.getData();
                System.out.println("禁用结果: " + (Boolean.TRUE.equals(response.getSuccess()) ? "成功" : "失败"));
            } else {
                System.out.println("禁用失败: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "未激活";
            case 1:
                return "已激活";
            case 2:
                return "已禁用";
            default:
                return "未知";
        }
    }

    /**
     * 获取连接状态描述
     */
    private String getConnectStatusDesc(Integer connectStatus) {
        if (connectStatus == null) {
            return "未知";
        }
        switch (connectStatus) {
            case 0:
                return "未连接";
            case 1:
                return "在线";
            case 2:
                return "离线";
            default:
                return "未知";
        }
    }

    /**
     * 获取设备类型描述
     */
    private String getNodeTypeDesc(Integer nodeType) {
        if (nodeType == null) {
            return "未知";
        }
        return switch (nodeType) {
            case 0 -> "普通设备";
            case 1 -> "网关设备";
            case 2 -> "子设备";
            default -> "未知";
        };
    }

    /**
     * 测试修改子设备连接状态
     */
    public void testUpdateSubDeviceConnectStatus() {
        // 1. 构建请求参数
        IotNorthboundDeviceUpdateSubDeviceConnectStatusRequest request = new IotNorthboundDeviceUpdateSubDeviceConnectStatusRequest();
        request.setGatewayIdentification("GATEWAY_001");

        List<IotNorthboundDeviceUpdateSubDeviceConnectStatusRequest.DeviceStatus> deviceStatuses = new ArrayList<>();
        deviceStatuses.add(IotNorthboundDeviceUpdateSubDeviceConnectStatusRequest.DeviceStatus.builder()
                .deviceId("SUB_DEVICE_001")
                .status("ONLINE")
                .build());
        deviceStatuses.add(IotNorthboundDeviceUpdateSubDeviceConnectStatusRequest.DeviceStatus.builder()
                .deviceId("SUB_DEVICE_002")
                .status("OFFLINE")
                .build());
        request.setDeviceStatuses(deviceStatuses);

        // 2. 封装API参数
        IotNorthboundDeviceUpdateSubDeviceConnectStatusParam param = new IotNorthboundDeviceUpdateSubDeviceConnectStatusParam();
        param.setBizModel(request);

        try {
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            System.out.println("正在调用修改子设备连接状态接口...");
            Result<IotNorthboundDeviceUpdateSubDeviceConnectStatusResponse> result = client.execute(param);

            if (result.isSuccess()) {
                IotNorthboundDeviceUpdateSubDeviceConnectStatusResponse response = result.getData();
                System.out.println("=== 修改成功 ===");
                System.out.println("状态码: " + response.getStatusCode());
                System.out.println("状态描述: " + response.getStatusDesc());
                if (response.getData() != null) {
                    for (IotNorthboundDeviceUpdateSubDeviceConnectStatusResponse.OperationResult op : response.getData()) {
                        System.out.println("设备ID: " + op.getDeviceId() + ", 状态码: " + op.getStatusCode() + ", 描述: " + op.getStatusDesc());
                    }
                }
            } else {
                System.out.println("=== 修改失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }

    /**
     * 测试删除子设备
     */
    public void testDeleteSubDevice() {
        // 1. 构建请求参数
        IotNorthboundDeviceDeleteSubDeviceRequest request = new IotNorthboundDeviceDeleteSubDeviceRequest();
        request.setGatewayIdentification("GATEWAY_001");

        List<String> deviceIds = new ArrayList<>();
        deviceIds.add("SUB_DEVICE_001");
        deviceIds.add("SUB_DEVICE_002");
        request.setDeviceIds(deviceIds);

        // 2. 封装API参数
        IotNorthboundDeviceDeleteSubDeviceParam param = new IotNorthboundDeviceDeleteSubDeviceParam();
        param.setBizModel(request);

        try {
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            System.out.println("正在调用删除子设备接口...");
            Result<IotNorthboundDeviceDeleteSubDeviceResponse> result = client.execute(param);

            if (result.isSuccess()) {
                IotNorthboundDeviceDeleteSubDeviceResponse response = result.getData();
                System.out.println("=== 删除成功 ===");
                System.out.println("状态码: " + response.getStatusCode());
                System.out.println("状态描述: " + response.getStatusDesc());
                if (response.getData() != null) {
                    for (IotNorthboundDeviceDeleteSubDeviceResponse.OperationResult op : response.getData()) {
                        System.out.println("设备ID: " + op.getDeviceId() + ", 状态码: " + op.getStatusCode() + ", 描述: " + op.getStatusDesc());
                    }
                }
            } else {
                System.out.println("=== 删除失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }

    /**
     * 测试设备数据上报
     */
    public void testDeviceDataReport() {
        // 1. 构建请求参数
        IotNorthboundDeviceDataReportRequest request = new IotNorthboundDeviceDataReportRequest();

        List<IotNorthboundDeviceDataReportRequest.DeviceData> devices = new ArrayList<>();
        IotNorthboundDeviceDataReportRequest.DeviceData deviceData = new IotNorthboundDeviceDataReportRequest.DeviceData();
        deviceData.setDeviceId("DEVICE_001");

        List<IotNorthboundDeviceDataReportRequest.ServiceData> services = new ArrayList<>();
        services.add(IotNorthboundDeviceDataReportRequest.ServiceData.builder()
                .serviceCode("temperature")
                .data(Map.of("value", 25.5, "unit", "℃"))
                .eventTime(System.currentTimeMillis())
                .build());
        services.add(IotNorthboundDeviceDataReportRequest.ServiceData.builder()
                .serviceCode("humidity")
                .data(Map.of("value", 60, "unit", "%"))
                .eventTime(System.currentTimeMillis())
                .build());
        deviceData.setServices(services);
        devices.add(deviceData);
        request.setDevices(devices);

        // 2. 封装API参数
        IotNorthboundDeviceDataReportParam param = new IotNorthboundDeviceDataReportParam();
        param.setBizModel(request);

        try {
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            System.out.println("正在调用设备数据上报接口...");
            Result<IotNorthboundDeviceDataReportResponse> result = client.execute(param);

            if (result.isSuccess()) {
                IotNorthboundDeviceDataReportResponse response = result.getData();
                System.out.println("=== 上报成功 ===");
                System.out.println("状态码: " + response.getStatusCode());
                System.out.println("状态描述: " + response.getStatusDesc());
                if (response.getData() != null) {
                    for (IotNorthboundDeviceDataReportResponse.OperationResult op : response.getData()) {
                        System.out.println("设备ID: " + op.getDeviceId() + ", 状态码: " + op.getStatusCode() + ", 描述: " + op.getStatusDesc());
                    }
                }
            } else {
                System.out.println("=== 上报失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }

    /**
     * 测试批量查询设备信息
     */
    public void testQueryDevices() {
        // 1. 构建请求参数
        IotNorthboundDeviceQueryRequest request = new IotNorthboundDeviceQueryRequest();

        List<String> deviceIds = new ArrayList<>();
        deviceIds.add("DEVICE_001");
        deviceIds.add("DEVICE_002");
        request.setDeviceIds(deviceIds);

        // 2. 封装API参数
        IotNorthboundDeviceQueryParam param = new IotNorthboundDeviceQueryParam();
        param.setBizModel(request);

        try {
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            System.out.println("正在调用批量查询设备信息接口...");
            Result<IotNorthboundDeviceQueryResponse> result = client.execute(param);

            if (result.isSuccess()) {
                IotNorthboundDeviceQueryResponse response = result.getData();
                System.out.println("=== 查询成功 ===");
                System.out.println("状态码: " + response.getStatusCode());
                System.out.println("状态描述: " + response.getStatusDesc());
                if (response.getData() != null) {
                    for (IotNorthboundDeviceQueryResponse.DeviceItem item : response.getData()) {
                        System.out.println("----------------------------------------");
                        System.out.println("设备ID: " + item.getDeviceId());
                        System.out.println("状态码: " + item.getStatusCode());
                        System.out.println("状态描述: " + item.getStatusDesc());
                        if (item.getDeviceInfo() != null) {
                            IotNorthboundDeviceQueryResponse.DeviceInfo info = item.getDeviceInfo();
                            System.out.println("设备名称: " + info.getDeviceName());
                            System.out.println("客户端ID: " + info.getClientId());
                            System.out.println("设备状态: " + info.getDeviceStatus());
                            System.out.println("连接状态: " + info.getConnectStatus());
                            System.out.println("产品标识: " + info.getProductIdentification());
                            System.out.println("设备类型: " + info.getNodeType());
                        }
                    }
                }
            } else {
                System.out.println("=== 查询失败 ===");
                System.out.println("错误码: " + result.getSubCode());
                System.out.println("错误信息: " + result.getSubMsg());
            }
        } catch (Exception e) {
            System.out.println("!!! 发生异常 !!!");
            e.printStackTrace();
        }
    }

}
