package com.mqttsnet.thinglinks.card.strategy;

import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.thinglinks.card.abstrac.AbstractCard;
import com.mqttsnet.thinglinks.card.constants.IotCardConstants;
import com.mqttsnet.thinglinks.card.constants.OperatorRequestConstants;
import com.mqttsnet.thinglinks.card.entity.auto.ApiResponse;
import com.mqttsnet.thinglinks.card.entity.auto.IotCardAuthToken;
import com.mqttsnet.thinglinks.card.entity.channel.CardChannelInfo;
import com.mqttsnet.thinglinks.card.enumeration.OperatorTypeEnum;
import com.mqttsnet.thinglinks.card.utils.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * 中国移动卡具体实现类。
 *
 * @Author: shisen
 * @Date: 2024/06/27 22:13
 */
@Slf4j
@Component
public class ChinaMobileCardStrategy extends AbstractCard {

    @Autowired
    public ChinaMobileCardStrategy(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public OperatorTypeEnum getSupportId() {
        return OperatorTypeEnum.CHINA_MOBILE;
    }

    /**
     * 新增卡源所需要填写的密钥参数,前端动态变更
     *
     * @return
     */
    @Override
    public Map<String, String> getKeyParameters() {
        Map keyMap = new HashMap();
        keyMap.put("appid", "运营商提供appid");
        keyMap.put("password", "运营商提供password");
        return keyMap;
    }

    @Override
    public String getName() {
        return OperatorTypeEnum.CHINA_MOBILE.getDesc();
    }

    @Override
    public IotCardAuthToken authenticate(CardChannelInfo channelInfo, IotCardAuthToken iotToken) {
        // 实现中国移动卡的认证逻辑
        return getOneLinkToken(channelInfo, iotToken);
    }

    private IotCardAuthToken getOneLinkToken(CardChannelInfo channelInfo, IotCardAuthToken iotToken) {
        // 当前时间戳
        String startTs = DateUtils.dateTimeNow();
        // 生成transid获取token用
        String transid = generateTransid(channelInfo, startTs);
        try {
            String res = fetchTokenResponse(channelInfo, transid);
            ApiResponse response = JSONObject.parseObject(res, ApiResponse.class);
            if (Objects.isNull(response)) {
                log.warn("移动官方 Onelink 直连获取令牌时返回空响应");
                return null;
            }
            log.debug("移动官方 Onelink 直连令牌请求完成，status={}", response.getStatus());
            // 判断是否成功
            if (HttpStatus.ONE_LINK_STATUS.equals(response.getStatus())) {
                // 获取token成功
                return processSuccessfulResponse(response, transid, channelInfo, iotToken);
            }
        } catch (Exception e) {
            log.error("移动官方 Onelink 直连获取令牌失败，exceptionType={}",
                    e.getClass().getSimpleName());
        }
        return null;
    }

    private String generateTransid(CardChannelInfo channelInfo, String startTs) {
        return String.format("%s%s%s%s", channelInfo.getAppId(), startTs, "00000000", channelInfo.getPassword());
    }

    private String fetchTokenResponse(CardChannelInfo channelInfo, String transid) throws Exception {
        String url = OperatorRequestConstants.CHINA_MOBILE_URL_PREFIX + OperatorRequestConstants.TOKEN;

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam(IotCardConstants.APPID, channelInfo.getAppId())
                .queryParam(IotCardConstants.PASSWORD, channelInfo.getPassword())
                .queryParam(IotCardConstants.TRANSID, transid);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = getRestTemplate().exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    private IotCardAuthToken processSuccessfulResponse(ApiResponse response, String transid, CardChannelInfo channelInfo, IotCardAuthToken iotToken) {
        Optional<ApiResponse.Result> optionalResult = response.getResult().stream().findFirst();
        if (!optionalResult.isPresent()) {
            return null;
        }
        ApiResponse.Result result = optionalResult.get();
        iotToken.setToken(result.getToken());
        iotToken.setTransid(transid);
        iotToken.setChannelName(channelInfo.getChannelName());
        return iotToken;
    }
}
