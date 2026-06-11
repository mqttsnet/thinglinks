package com.mqttsnet.thinglinks.msg.strategy.impl.sms;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.dinger.content.WeChatInfo;
import com.mqttsnet.basic.dinger.content.WeChatInfoReq;
import com.mqttsnet.basic.dinger.process.INoticeProcessor;
import com.mqttsnet.basic.dinger.process.WeChatNoticeProcessor;
import com.mqttsnet.basic.dinger.properties.WeChatProperties;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.model.Kv;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.SpringUtils;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.msg.entity.ExtendMsg;
import com.mqttsnet.thinglinks.msg.entity.ExtendMsgRecipient;
import com.mqttsnet.thinglinks.msg.strategy.MsgStrategy;
import com.mqttsnet.thinglinks.msg.strategy.domain.MsgParam;
import com.mqttsnet.thinglinks.msg.strategy.domain.MsgResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 企业微信消息策略
 */
@Slf4j
@Service("wxSmsMsgStrategyImpl")
public class WxSmsMsgStrategyImpl implements MsgStrategy {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 执行发送
     *
     * @param msgParam msgParam
     * @return com.mqttsnet.thinglinks.msg.strategy.domain.MsgResult
     * @throws Exception 异常
     * @author mqttsnet
     * @date 2022/10/28 4:58 PM
     * @create [2022/10/28 4:58 PM ] [mqttsnet] [初始创建]
     */
    @Override
    public MsgResult exec(MsgParam msgParam) throws Exception {
        ExtendMsg extendMsg = msgParam.getExtendMsg();
        List<ExtendMsgRecipient> recipientList = msgParam.getRecipientList();
        String content = extendMsg.getContent();
        ArgumentAssert.notEmpty(content, "消息内容为空,请配置");
        // 接收人手机号
        List<String> phoneNumbers = recipientList.stream().map(ExtendMsgRecipient::getRecipient).collect(Collectors.toList());
        // 处理模版配置的参数
        Map<String, Object> propertyMap = new HashMap<>();
        if (CollUtil.isNotEmpty(msgParam.getPropertyParams())) {
            propertyMap = msgParam.getPropertyParams();
        }
        if (StrUtil.isNotEmpty(extendMsg.getConfigList())) {
            List<Kv> list = JsonUtil.parseArray(extendMsg.getConfigList(), Kv.class);
            for (Kv kv : list) {
                propertyMap.put(kv.getKey(), kv.getValue());
            }
        }
        // 配置信息为空
        ArgumentAssert.notEmpty(propertyMap, "配置信息为空,请配置");
        // 转换成对象
        WeChatProperties weChatProperties = BeanUtil.copyProperties(propertyMap, WeChatProperties.class);
        // 接收人手机号
        if (CollectionUtil.isNotEmpty(phoneNumbers)) {
            weChatProperties.setAtMobiles(phoneNumbers);
        }
        if (StrPool.YES.equals(weChatProperties.getIsAtAll())) {
            weChatProperties.getAtMobiles().removeAll(weChatProperties.getAtMobiles());
            weChatProperties.getAtMobiles().add(StrPool.All);
        }
        // 注:历史代码先 SpringUtils.getBean("weChatNoticeProcessor") 但未使用 ── 实际用本地 new 的 processor.
        // 已删除死代码 getBean,如未来需要走 Spring 管理的 NoticeProcessor,直接 @Resource(name=...) 注入即可.
        WeChatNoticeProcessor processor = new WeChatNoticeProcessor(restTemplate);
        WeChatInfo weChatInfo = new WeChatInfo(weChatProperties.getMsgType(), content, weChatProperties.getAtMobiles());
        Object o = processor.sendNotice(WeChatInfoReq.builder().weChatProperties(weChatProperties).weChatInfo(weChatInfo).build());
        return MsgResult.builder().result(o).build();
    }


    /**
     * 是否执行成功
     *
     * @param result 执行函数的返回值
     * @return
     */
    @Override
    public boolean isSuccess(MsgResult result) {
        return MsgStrategy.super.isSuccess(result);
    }

}
