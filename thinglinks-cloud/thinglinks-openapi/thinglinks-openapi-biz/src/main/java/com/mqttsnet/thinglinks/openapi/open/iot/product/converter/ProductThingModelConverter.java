package com.mqttsnet.thinglinks.openapi.open.iot.product.converter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.thinglinks.openapi.open.iot.product.resp.IotNorthboundProductGetThingModelResponse;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.productcommand.vo.param.ProductCommandParamVO;
import com.mqttsnet.thinglinks.productcommandrequest.vo.param.ProductCommandRequestParamVO;
import com.mqttsnet.thinglinks.productcommandresponse.vo.param.ProductCommandResponseParamVO;
import com.mqttsnet.thinglinks.productproperty.vo.param.ProductPropertyParamVO;
import com.mqttsnet.thinglinks.productservice.vo.param.ProductServiceParamVO;
import lombok.experimental.UtilityClass;

/**
 * Description:
 * 产品物模型转换器
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/02/02
 */
@UtilityClass
public class ProductThingModelConverter {

    /**
     * 将 ProductParamVO 转换为 IotNorthboundProductGetThingModelResponse
     *
     * @param productParamVO 产品物模型参数VO
     * @return 北向API响应
     */
    public static IotNorthboundProductGetThingModelResponse convert(ProductParamVO productParamVO) {
        if (Objects.isNull(productParamVO)) {
            return null;
        }

        return IotNorthboundProductGetThingModelResponse.builder()
                .appId(productParamVO.getAppId())
                .productIdentification(productParamVO.getProductIdentification())
                .templateId(productParamVO.getTemplateId())
                .productName(productParamVO.getProductName())
                .productType(productParamVO.getProductType())
                .manufacturerId(productParamVO.getManufacturerId())
                .manufacturerName(productParamVO.getManufacturerName())
                .model(productParamVO.getModel())
                .dataFormat(productParamVO.getDataFormat())
                .deviceType(productParamVO.getDeviceType())
                .protocolType(productParamVO.getProtocolType())
                .activeVersionNo(productParamVO.getActiveVersionNo())
                .remark(productParamVO.getRemark())
                .services(convertServices(productParamVO.getServices()))
                .build();
    }

    /**
     * 转换服务列表
     *
     * @param services 服务参数 VO 列表
     * @return 物模型服务列表;入参空返空列表
     */
    private static List<IotNorthboundProductGetThingModelResponse.ThingModelService> convertServices(List<ProductServiceParamVO> services) {
        if (CollUtil.isEmpty(services)) {
            return Collections.emptyList();
        }

        return services.stream()
                .map(ProductThingModelConverter::convertService)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个服务
     *
     * @param service 服务参数 VO
     * @return 物模型服务;入参 null 返 null
     */
    private static IotNorthboundProductGetThingModelResponse.ThingModelService convertService(ProductServiceParamVO service) {
        if (Objects.isNull(service)) {
            return null;
        }

        return IotNorthboundProductGetThingModelResponse.ThingModelService.builder()
                .productId(service.getProductId())
                .serviceCode(service.getServiceCode())
                .serviceName(service.getServiceName())
                .serviceType(service.getServiceType())
                .serviceStatus(service.getServiceStatus())
                .description(service.getDescription())
                .remark(service.getRemark())
                .commands(convertCommands(service.getCommands()))
                .properties(convertProperties(service.getProperties()))
                .build();
    }

    /**
     * 转换属性列表
     *
     * @param properties 属性参数 VO 列表
     * @return 物模型属性列表;入参空返空列表
     */
    private static List<IotNorthboundProductGetThingModelResponse.ThingModelProperty> convertProperties(List<ProductPropertyParamVO> properties) {
        if (CollUtil.isEmpty(properties)) {
            return Collections.emptyList();
        }

        return properties.stream()
                .map(ProductThingModelConverter::convertProperty)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个属性
     *
     * @param property 属性参数 VO
     * @return 物模型属性;入参 null 返 null
     */
    private static IotNorthboundProductGetThingModelResponse.ThingModelProperty convertProperty(ProductPropertyParamVO property) {
        if (Objects.isNull(property)) {
            return null;
        }

        return IotNorthboundProductGetThingModelResponse.ThingModelProperty.builder()
                .serviceId(property.getServiceId())
                .propertyCode(property.getPropertyCode())
                .propertyName(property.getPropertyName())
                .datatype(property.getDatatype())
                .description(property.getDescription())
                .enumlist(property.getEnumlist())
                .max(property.getMax())
                .maxlength(property.getMaxlength())
                .method(property.getMethod())
                .min(property.getMin())
                .required(property.getRequired())
                .step(property.getStep())
                .unit(property.getUnit())
                .icon(property.getIcon())
                .remark(property.getRemark())
                .build();
    }

    /**
     * 转换命令列表
     *
     * @param commands 命令参数 VO 列表
     * @return 物模型命令列表;入参空返空列表
     */
    private static List<IotNorthboundProductGetThingModelResponse.ThingModelCommand> convertCommands(List<ProductCommandParamVO> commands) {
        if (CollUtil.isEmpty(commands)) {
            return Collections.emptyList();
        }

        return commands.stream()
                .map(ProductThingModelConverter::convertCommand)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个命令
     *
     * @param command 命令参数 VO
     * @return 物模型命令;入参 null 返 null
     */
    private static IotNorthboundProductGetThingModelResponse.ThingModelCommand convertCommand(ProductCommandParamVO command) {
        if (Objects.isNull(command)) {
            return null;
        }

        return IotNorthboundProductGetThingModelResponse.ThingModelCommand.builder()
                .serviceId(command.getServiceId())
                .commandCode(command.getCommandCode())
                .commandName(command.getCommandName())
                .description(command.getDescription())
                .remark(command.getRemark())
                .requests(convertCommandRequests(command.getRequests()))
                .responses(convertCommandResponses(command.getResponses()))
                .build();
    }

    /**
     * 转换命令请求参数列表
     *
     * @param requests 命令请求参数 VO 列表
     * @return 物模型命令请求参数列表;入参空返空列表
     */
    private static List<IotNorthboundProductGetThingModelResponse.ThingModelCommandRequest> convertCommandRequests(List<ProductCommandRequestParamVO> requests) {
        if (CollUtil.isEmpty(requests)) {
            return Collections.emptyList();
        }

        return requests.stream()
                .map(ProductThingModelConverter::convertCommandRequest)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个命令请求参数
     *
     * @param request 命令请求参数 VO
     * @return 物模型命令请求参数;入参 null 返 null
     */
    private static IotNorthboundProductGetThingModelResponse.ThingModelCommandRequest convertCommandRequest(ProductCommandRequestParamVO request) {
        if (Objects.isNull(request)) {
            return null;
        }

        return IotNorthboundProductGetThingModelResponse.ThingModelCommandRequest.builder()
                .serviceId(request.getServiceId())
                .commandId(request.getCommandId())
                .parameterCode(request.getParameterCode())
                .parameterName(request.getParameterName())
                .parameterDescription(request.getParameterDescription())
                .datatype(request.getDatatype())
                .enumlist(request.getEnumlist())
                .max(request.getMax())
                .maxlength(request.getMaxlength())
                .min(request.getMin())
                .required(request.getRequired())
                .step(request.getStep())
                .unit(request.getUnit())
                .remark(request.getRemark())
                .build();
    }

    /**
     * 转换命令响应参数列表
     *
     * @param responses 命令响应参数 VO 列表
     * @return 物模型命令响应参数列表;入参空返空列表
     */
    private static List<IotNorthboundProductGetThingModelResponse.ThingModelCommandResponse> convertCommandResponses(List<ProductCommandResponseParamVO> responses) {
        if (CollUtil.isEmpty(responses)) {
            return Collections.emptyList();
        }

        return responses.stream()
                .map(ProductThingModelConverter::convertCommandResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个命令响应参数
     *
     * @param response 命令响应参数 VO
     * @return 物模型命令响应参数;入参 null 返 null
     */
    private static IotNorthboundProductGetThingModelResponse.ThingModelCommandResponse convertCommandResponse(ProductCommandResponseParamVO response) {
        if (Objects.isNull(response)) {
            return null;
        }

        return IotNorthboundProductGetThingModelResponse.ThingModelCommandResponse.builder()
                .commandId(response.getCommandId())
                .serviceId(response.getServiceId())
                .parameterCode(response.getParameterCode())
                .parameterName(response.getParameterName())
                .parameterDescription(response.getParameterDescription())
                .datatype(response.getDatatype())
                .enumlist(response.getEnumlist())
                .max(response.getMax())
                .maxlength(response.getMaxlength())
                .min(response.getMin())
                .required(response.getRequired())
                .step(response.getStep())
                .unit(response.getUnit())
                .remark(response.getRemark())
                .build();
    }

}
