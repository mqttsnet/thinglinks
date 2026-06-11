package com.mqttsnet.thinglinks.msg.strategy.impl.sms;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.dinger.content.FeiShuInfo;
import com.mqttsnet.basic.dinger.content.FeiShuInfoReq;
import com.mqttsnet.basic.dinger.content.FeiShuRobotResult;
import com.mqttsnet.basic.dinger.enums.MsgTypeEnum;
import com.mqttsnet.basic.dinger.process.INoticeProcessor;
import com.mqttsnet.basic.dinger.properties.FeiShuProperties;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.model.Kv;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.msg.entity.ExtendMsg;
import com.mqttsnet.thinglinks.msg.entity.ExtendMsgRecipient;
import com.mqttsnet.thinglinks.msg.strategy.MsgStrategy;
import com.mqttsnet.thinglinks.msg.strategy.domain.MsgParam;
import com.mqttsnet.thinglinks.msg.strategy.domain.MsgResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 飞书消息策略
 */
@Slf4j
@Service("feiShuSmsMsgStrategyImpl")
public class FeiShuSmsMsgStrategyImpl implements MsgStrategy {

    /**
     * 飞书通知处理器 ── 启动期按 bean name 注入,替代历史的 {@code SpringUtils.getBean(name, type)} 运行时查询.
     */
    @Resource(name = "feiShuNoticeProcessor")
    private INoticeProcessor noticeProcessor;

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
        FeiShuProperties feiShuProperties = BeanUtil.copyProperties(propertyMap, FeiShuProperties.class);
        // 接收人手机号
        if (CollectionUtil.isNotEmpty(phoneNumbers)) {
            feiShuProperties.setAtMobiles(phoneNumbers);
        }
        FeiShuInfo param = FeiShuInfo.builder().msgType(feiShuProperties.getMsgType().getMsgType()).build();
        if (MsgTypeEnum.TEXT.equals(feiShuProperties.getMsgType())) {
            param.setContent(FeiShuInfo.ContentDTO.builder().text(content).build());
        }
        FeiShuInfoReq feiShuInfoReq = FeiShuInfoReq.builder().feiShuInfo(param).feiShuProperties(feiShuProperties).build();
        Object o = noticeProcessor.sendNotice(feiShuInfoReq);
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
        FeiShuRobotResult feiShuRobotResult = (FeiShuRobotResult) result.getResult();
        return feiShuRobotResult.getCode() == 0;
    }
}
