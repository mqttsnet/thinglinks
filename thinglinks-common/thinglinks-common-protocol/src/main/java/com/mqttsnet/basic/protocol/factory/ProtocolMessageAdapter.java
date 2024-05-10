package com.mqttsnet.basic.protocol.factory;

import java.util.Map;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.protocol.model.ProtocolDataMessageDTO;
import com.mqttsnet.basic.protocol.utils.ProtocolMessageSignatureVerifierUtils;
import com.mqttsnet.basic.protocol.utils.ProtocolRegexTopicVariableExtractorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @program: thinglinks
 * @description: 协议信息适配器
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-30 15:08
 **/
@Slf4j
@Component
public class ProtocolMessageAdapter {

    public boolean validateProtocolData(String body) {
        return ProtocolMessageSignatureVerifierUtils.validateProtocolData(body);
    }

    public Map<String, String> extractVariables(String topic) {
        return ProtocolRegexTopicVariableExtractorUtils.extractVariables(topic);
    }

    public ProtocolDataMessageDTO parseProtocolDataMessage(String body) {
        return BeanUtil.toBean(JSON.parseObject(body), ProtocolDataMessageDTO.class);
    }

    public String decryptMessage(String body, EncryptionDetailsDTO encryptionDetailsDTO) throws Exception {
        String signKey = encryptionDetailsDTO.getSignKey();
        String encryptKey = encryptionDetailsDTO.getEncryptKey();
        String encryptVector = encryptionDetailsDTO.getEncryptVector();
        return ProtocolMessageSignatureVerifierUtils.decryptMessage(body, signKey, encryptKey, encryptVector);
    }

    public <T> ProtocolDataMessageDTO<T> buildResponse(ProtocolDataMessageDTO<T> protocolDataMessageDTO,
                                                       String resultDataBody, EncryptionDetailsDTO encryptionDetailsDTO) throws Exception {
        String signKey = encryptionDetailsDTO.getSignKey();
        String encryptKey = encryptionDetailsDTO.getEncryptKey();
        String encryptVector = encryptionDetailsDTO.getEncryptVector();
        //数据加密签名处理
        String dataBody = ProtocolMessageSignatureVerifierUtils.encryptMessage(resultDataBody, protocolDataMessageDTO.getHead().getCipherFlag(), signKey, encryptKey,
                encryptVector);
        //JSON字符串转换为对象
        protocolDataMessageDTO = JSON.parseObject(dataBody, new TypeReference<ProtocolDataMessageDTO>() {
        });


        return protocolDataMessageDTO;
    }

    public <T> ProtocolDataMessageDTO buildResponse(String resultDataBody, EncryptionDetailsDTO encryptionDetailsDTO) throws Exception {
        String signKey = encryptionDetailsDTO.getSignKey();
        String encryptKey = encryptionDetailsDTO.getEncryptKey();
        String encryptVector = encryptionDetailsDTO.getEncryptVector();
        //数据加密签名处理
        String dataBody = ProtocolMessageSignatureVerifierUtils.encryptMessage(resultDataBody, encryptionDetailsDTO.getCipherFlag(), signKey, encryptKey,
                encryptVector);
        //JSON字符串转换为对象
        ProtocolDataMessageDTO protocolDataMessageDTO = JSON.parseObject(dataBody, new TypeReference<ProtocolDataMessageDTO>() {
        });


        return protocolDataMessageDTO;
    }
}
