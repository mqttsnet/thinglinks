package com.mqttsnet.thinglinks.card.strategy;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.basic.utils.sm.Sm3Utils;
import com.mqttsnet.thinglinks.card.abstrac.AbstractCard;
import com.mqttsnet.thinglinks.card.entity.auto.ApiResponse;
import com.mqttsnet.thinglinks.card.entity.auto.IotCardAuthToken;
import com.mqttsnet.thinglinks.card.entity.auto.IotKeyParameter;
import com.mqttsnet.thinglinks.card.entity.channel.CardChannelInfo;
import com.mqttsnet.thinglinks.card.enumeration.OperatorTypeEnum;
import com.mqttsnet.thinglinks.card.utils.msisdn.ChannelItems;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * 中国电信卡具体实现类。
 *
 * @Author: shisen
 * @Date: 2024/06/27 11:37
 */
@Slf4j
@Component
public class ChinaTelecomCardStrategy extends AbstractCard {

    @Autowired
    public ChinaTelecomCardStrategy(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public OperatorTypeEnum getSupportId() {
        return OperatorTypeEnum.CHINA_TELECOM;
    }


    @Override
    public String getName() {
        return ChannelItems.DXGF;
    }

    /**
     * 新增卡源所需要填写的密钥参数,前端动态变更
     *
     * @return
     */
    @Override
    public Map<String, String> getKeyParameters() {
        Map keyMap = new HashMap();
        keyMap.put("user_id", "运营商提供32位user_id");
        keyMap.put("password", "运营商提供 密码");
        keyMap.put("secret", "运营商提供 密钥9位key密钥");
        return keyMap;
    }

    @Override
    public IotCardAuthToken authenticate(CardChannelInfo channelInfo, IotCardAuthToken iotToken) throws IOException {
        // 实现中国电信卡的认证逻辑
        IotKeyParameter iotKeyParameter = new IotKeyParameter();
        iotKeyParameter.setAppid(channelInfo.getAppId());
        iotKeyParameter.setPassword(channelInfo.getPassword());

        Map<String, String> result = Optional.of(Sm3Utils.makeToken(iotKeyParameter.getAppid(), iotKeyParameter.getPassword()))
                .orElseThrow(() -> new IOException("Failed to generate token"));

        // 直接走 fastjson2 Map→Bean 一步到位,无需"对象→JSON 字符串→反序列化"的二次转换
        ApiResponse.Unicom unicom = Optional.ofNullable(JSON.parseObject(JSON.toJSONString(result), ApiResponse.Unicom.class))
                .orElseThrow(() -> new IOException("Invalid response from token generation"));

        Optional.ofNullable(unicom.getToken()).ifPresent(iotToken::setToken);
        Optional.ofNullable(unicom.getTransId()).ifPresent(iotToken::setTransid);
        Optional.ofNullable(unicom.getTimestamp()).ifPresent(iotToken::setTime);
        Optional.ofNullable(iotKeyParameter.getCode()).ifPresent(iotToken::setDev_id);
        Optional.ofNullable(iotKeyParameter.getAppid()).ifPresent(iotToken::setApikey);
        Optional.ofNullable(channelInfo.getChannelName()).ifPresent(iotToken::setChannelName);

        return iotToken;
    }


}
