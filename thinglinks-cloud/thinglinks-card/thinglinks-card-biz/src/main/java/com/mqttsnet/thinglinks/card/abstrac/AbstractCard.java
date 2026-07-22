package com.mqttsnet.thinglinks.card.abstrac;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.StringUtils;
import com.mqttsnet.thinglinks.card.constants.IotCardConstants;
import com.mqttsnet.thinglinks.card.constants.OperatorRequestConstants;
import com.mqttsnet.thinglinks.card.entity.auto.IotCardAuthToken;
import com.mqttsnet.thinglinks.card.supports.OfficialProvider;
import com.mqttsnet.thinglinks.card.utils.analysisUtil.OnelinkDataParsingUtil;
import com.mqttsnet.thinglinks.card.utils.chinamobile.RequestType;
import com.mqttsnet.thinglinks.card.utils.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * 运营商卡数据抽象模板,默认使用移动v5数据格式,
 * 对接第三方接口请继承AbstractCard抽象类,实现对应方法和适配数据即可
 *
 * @Author: shisen
 * @Date: 2024/06/27 14:53
 */
@Slf4j
public abstract class AbstractCard implements OfficialProvider {

    private final RestTemplate restTemplate;

    protected AbstractCard(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected RestTemplate getRestTemplate() {
        return restTemplate;
    }

    /**
     * 准备卡片操作流程。
     *
     * @param iotCardToken IOT卡令牌。
     * @return 处理后的IOT卡令牌。
     */
    public IotCardAuthToken prepareCollar(IotCardAuthToken iotCardToken) {
        setRequestUrlAndMethod(iotCardToken);
        constructionPublicParmet(iotCardToken);
        String resultData = submitRequest(iotCardToken);
        processingData(resultData, iotCardToken);
        handleRequestTypes(iotCardToken);
        return iotCardToken;
    }

    /**
     * 设置请求URL和请求方式
     *
     * @param iotCardToken IOT卡令牌。
     */
    private void setRequestUrlAndMethod(IotCardAuthToken iotCardToken) {
        Map<String, List<String>> urlMethodsMap = OperatorRequestConstants.getProviderUrlMethods(iotCardToken.getSupportId(), iotCardToken.getRequestType());
        if (urlMethodsMap == null || urlMethodsMap.isEmpty()) {
            throw new BizException("该用户暂未开通此接口权限,请联系管理员");
        }
        String url = urlMethodsMap.keySet().iterator().next();
        List<String> methods = urlMethodsMap.get(url);
        String requestMethod = methods.contains(OperatorRequestConstants.POST) ? OperatorRequestConstants.POST : methods.get(0);

        iotCardToken.setUrl(url);
        iotCardToken.setRequestMethod(requestMethod);
    }

    /**
     * 处理不同请求类型。
     *
     * @param iotCardToken IOT卡令牌。
     */
    private void handleRequestTypes(IotCardAuthToken iotCardToken) {
        switch (iotCardToken.getRequestType()) {
            case RequestType.API25Z00:
                this.conversionRealNameQueryIot(iotCardToken);
                break;
            case RequestType.API25S04:
                if (StringUtils.isNotNull(iotCardToken.getResultObjct())) {
                    this.conversionStatus(iotCardToken);
                }
                break;
            case RequestType.API25M01:
                if (StringUtils.isNotNull(iotCardToken.getResultObjct())) {
                    this.conversionSimSession(iotCardToken);
                }
                break;
            case RequestType.API25M00:
                if (StringUtils.isNotNull(iotCardToken.getResultObjct())) {
                    this.conversionOnOffStatus(iotCardToken);
                }
                break;
            default:
                break;
        }
    }

    protected void conversionRealNameQueryIot(IotCardAuthToken iotCardToken) {
    }

    protected void conversionStatus(IotCardAuthToken iotCardToken) {
    }

    protected void conversionSimSession(IotCardAuthToken iotCardToken) {
    }

    protected void conversionOnOffStatus(IotCardAuthToken iotCardToken) {
    }

    /**
     * 构造公共请求参数。
     *
     * @param iotCardToken IOT卡令牌。
     */
    private void constructionPublicParmet(IotCardAuthToken iotCardToken) {
        Map<String, Object> parameters = Optional.ofNullable(iotCardToken.getParameters()).orElse(new HashMap<>());

        parameters.put(IotCardConstants.TOKEN, iotCardToken.getToken());
        parameters.put("transid", iotCardToken.getTransid());

        Optional.ofNullable(iotCardToken.getCardNum())
                .filter(StringUtils::isNotEmpty)
                .ifPresentOrElse(
                        cardNum -> parameters.put(IotCardConstants.MSISDN, cardNum),
                        () -> {
                            Optional.ofNullable(iotCardToken.getImsi())
                                    .filter(StringUtils::isNotEmpty)
                                    .ifPresentOrElse(
                                            imsi -> parameters.put(IotCardConstants.IMSIS, imsi),
                                            () -> {
                                                Optional.ofNullable(iotCardToken.getIccid())
                                                        .filter(StringUtils::isNotEmpty)
                                                        .ifPresent(iccid -> parameters.put(IotCardConstants.ICCID, iccid));
                                            }
                                    );
                        }
                );

        switch (iotCardToken.getRequestType()) {
            case RequestType.API23S03:
                parameters.put("operType", iotCardToken.getPrivateParameters().get("operType"));
                break;
            case RequestType.API23S06:
                int operType = Integer.parseInt(iotCardToken.getPrivateParameters().get("operType").toString());
                if (operType == 9 || operType == 11) {
                    parameters.put("reason", "01");
                }
                parameters.put("operType", operType);
                break;
            case RequestType.API25U03:
                parameters.put("queryDate", iotCardToken.getPrivateParameters().get("queryDate"));
                break;
            default:
                log.warn("Unknown request type: {}", iotCardToken.getRequestType());
                break;
        }

        iotCardToken.setParameters(parameters);
    }

    /**
     * 提交请求并获取响应数据。
     *
     * @param iotCardToken IOT卡令牌。
     * @return 响应数据。
     */
    protected String submitRequest(IotCardAuthToken iotCardToken) {
        long startTime = System.currentTimeMillis();
        String res = "";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 使用 Optional 处理 headerParameters
            Optional.ofNullable(iotCardToken.getHeaderParameters()).ifPresent(headers::setAll);

            String url = Optional.ofNullable(iotCardToken.getUrl())
                    .orElseThrow(() -> new BizException("URL不能为空"));
            String requestMethod = Optional.ofNullable(iotCardToken.getRequestMethod())
                    .orElseThrow(() -> new BizException("请求方法不能为空"));

            if (OperatorRequestConstants.POST.equals(requestMethod)) {
                // POST 请求
                Map<String, Object> parameters = Optional.ofNullable(iotCardToken.getParameters()).orElseGet(HashMap::new);
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
                res = response.getBody();
            } else {
                // GET 请求
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
                Optional.ofNullable(iotCardToken.getParameters()).orElseGet(HashMap::new).forEach((key, value) -> {
                    if (value != null) {
                        uriBuilder.queryParam(key, value);
                    }
                });

                HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, requestEntity, String.class);
                res = response.getBody();
            }

        } catch (Exception e) {
            log.error("卡源请求失败，卡源={}，请求类型={}，exceptionType={}",
                    iotCardToken.getChannelName(), iotCardToken.getRequestType(), e.getClass().getSimpleName());
        }

        log.info("卡源请求完成，卡源={}，请求类型={}，请求方法={}，耗时={}ms，是否收到响应={}",
                iotCardToken.getChannelName(),
                iotCardToken.getRequestType(),
                iotCardToken.getRequestMethod(),
                (System.currentTimeMillis() - startTime),
                StringUtils.isNotEmpty(res));

        return res;
    }

    /**
     * 处理响应数据。
     *
     * @param resultData   响应数据。
     * @param iotCardToken IOT卡令牌。
     */
    protected void processingData(String resultData, IotCardAuthToken iotCardToken) {
        IotCardAuthToken.ResultMap resultMap = JSONObject.parseObject(resultData, IotCardAuthToken.ResultMap.class);
        if (HttpStatus.ONE_LINK_STATUS.equalsIgnoreCase(resultMap.getStatus())) {
            String toJSONString = JSON.toJSONString(OnelinkDataParsingUtil.splitResult(JSONObject.parseObject(resultData), iotCardToken.getRequestType()));
            if (RequestType.API23U07.equalsIgnoreCase(iotCardToken.getRequestType())) {
                resultMap.setResult(JSON.toJSONString(OnelinkDataParsingUtil.simDataMargin(JSONObject.parseObject(toJSONString), "accmMarginList")));
            } else {
                resultMap.setResult(toJSONString);
            }
        }
        iotCardToken.setResultMap(resultMap);
    }


}
