package com.mqttsnet.thinglinks.sdk;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.sdk.param.product.IotNorthboundProductManagerGetDetailParam;
import com.mqttsnet.thinglinks.sdk.param.product.IotNorthboundProductManagerGetThingModelParam;
import com.mqttsnet.thinglinks.sdk.request.product.IotNorthboundProductGetDetailRequest;
import com.mqttsnet.thinglinks.sdk.request.product.IotNorthboundProductGetThingModelRequest;
import com.mqttsnet.thinglinks.sdk.response.product.IotNorthboundProductGetDetailResponse;
import com.mqttsnet.thinglinks.sdk.response.product.IotNorthboundProductGetThingModelResponse;
import com.mqttsnet.thinglinks.sdkcore.client.OpenClient;
import com.mqttsnet.thinglinks.sdkcore.common.Result;
import junit.framework.TestCase;

/**
 * Description:
 * 北向API-产品管理测试类
 * 测试SDK
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/01/22
 */
public class IotNorthboundProductManagerTest extends TestCase {

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
     * 测试查询产品详情
     */
    public void testGetProductDetail() {
        // 假设这是已存在的产品标识
        String productIdentification = "1656177939456000"; // 请替换为实际的产品标识

        // 1. 构建请求参数
        IotNorthboundProductGetDetailRequest request = new IotNorthboundProductGetDetailRequest();
        request.setProductIdentification(productIdentification);

        // 2. 封装API参数
        IotNorthboundProductManagerGetDetailParam param = new IotNorthboundProductManagerGetDetailParam();
        param.setBizModel(request);

        try {
            // 3. 打印请求参数
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            // 4. 调用接口
            System.out.println("正在调用查询产品详情接口...");
            System.out.println("产品标识: " + productIdentification);
            Result<IotNorthboundProductGetDetailResponse> result = client.execute(param);

            // 5. 处理结果
            if (result.isSuccess()) {
                IotNorthboundProductGetDetailResponse response = result.getData();
                System.out.println("=== 查询成功 ===");
                System.out.println("产品ID: " + response.getId());
                System.out.println("产品标识: " + response.getProductIdentification());
                System.out.println("产品名称: " + response.getProductName());
                System.out.println("产品类型: " + response.getProductType());
                System.out.println("厂商ID: " + response.getManufacturerId());
                System.out.println("厂商名称: " + response.getManufacturerName());
                System.out.println("产品型号: " + response.getModel());
                System.out.println("协议类型: " + response.getProtocolType());
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
     * 测试查询产品物模型
     */
    public void testGetProductThingModel() {
        // 假设这是已存在的产品标识
        String productIdentification = "1656177939456000"; // 请替换为实际的产品标识

        // 1. 构建请求参数
        IotNorthboundProductGetThingModelRequest request = new IotNorthboundProductGetThingModelRequest();
        request.setProductIdentification(productIdentification);

        // 2. 封装API参数
        IotNorthboundProductManagerGetThingModelParam param = new IotNorthboundProductManagerGetThingModelParam();
        param.setBizModel(request);

        try {
            // 3. 打印请求参数
            System.out.println("=== 请求参数 ===");
            System.out.println(JSON.toJSONString(param));

            // 4. 调用接口
            System.out.println("正在调用查询产品物模型接口...");
            System.out.println("产品标识: " + productIdentification);
            Result<IotNorthboundProductGetThingModelResponse> result = client.execute(param);

            // 5. 处理结果
            if (result.isSuccess()) {
                IotNorthboundProductGetThingModelResponse response = result.getData();
                System.out.println("=== 查询成功 ===");
                System.out.println("应用ID: " + response.getAppId());
                System.out.println("产品标识: " + response.getProductIdentification());
                System.out.println("产品名称: " + response.getProductName());
                System.out.println("产品类型: " + response.getProductType());
                System.out.println("厂商ID: " + response.getManufacturerId());
                System.out.println("厂商名称: " + response.getManufacturerName());
                System.out.println("产品型号: " + response.getModel());
                System.out.println("协议类型: " + response.getProtocolType());
                System.out.println("产品版本: " + response.getActiveVersionNo());

                // 打印服务信息
                if (response.getServices() != null && !response.getServices().isEmpty()) {
                    System.out.println("=== 物模型服务列表 ===");
                    for (IotNorthboundProductGetThingModelResponse.ThingModelService service : response.getServices()) {
                        System.out.println("  服务编码: " + service.getServiceCode());
                        System.out.println("  服务名称: " + service.getServiceName());
                        System.out.println("  服务状态: " + service.getServiceStatus());

                        // 打印属性信息
                        if (service.getProperties() != null && !service.getProperties().isEmpty()) {
                            System.out.println("  === 服务属性列表 ===");
                            for (IotNorthboundProductGetThingModelResponse.ThingModelProperty property : service.getProperties()) {
                                System.out.println("    属性编码: " + property.getPropertyCode());
                                System.out.println("    属性名称: " + property.getPropertyName());
                                System.out.println("    数据类型: " + property.getDatatype());
                                System.out.println("    单位: " + property.getUnit());
                                System.out.println("    ---");
                            }
                        }

                        // 打印命令信息
                        if (service.getCommands() != null && !service.getCommands().isEmpty()) {
                            System.out.println("  === 服务命令列表 ===");
                            for (IotNorthboundProductGetThingModelResponse.ThingModelCommand command : service.getCommands()) {
                                System.out.println("    命令编码: " + command.getCommandCode());
                                System.out.println("    命令名称: " + command.getCommandName());
                                System.out.println("    命令描述: " + command.getDescription());

                                // 打印命令请求参数
                                if (command.getRequests() != null && !command.getRequests().isEmpty()) {
                                    System.out.println("    === 命令请求参数 ===");
                                    for (IotNorthboundProductGetThingModelResponse.ThingModelCommandRequest req : command.getRequests()) {
                                        System.out.println("      参数编码: " + req.getParameterCode());
                                        System.out.println("      参数名称: " + req.getParameterName());
                                        System.out.println("      数据类型: " + req.getDatatype());
                                    }
                                }

                                // 打印命令响应参数
                                if (command.getResponses() != null && !command.getResponses().isEmpty()) {
                                    System.out.println("    === 命令响应参数 ===");
                                    for (IotNorthboundProductGetThingModelResponse.ThingModelCommandResponse resp : command.getResponses()) {
                                        System.out.println("      参数编码: " + resp.getParameterCode());
                                        System.out.println("      参数名称: " + resp.getParameterName());
                                        System.out.println("      数据类型: " + resp.getDatatype());
                                    }
                                }
                                System.out.println("    ---");
                            }
                        }
                        System.out.println("  ===========================");
                    }
                }

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

}
