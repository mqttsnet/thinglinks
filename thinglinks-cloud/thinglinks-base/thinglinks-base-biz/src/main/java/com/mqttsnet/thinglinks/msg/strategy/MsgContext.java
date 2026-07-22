package com.mqttsnet.thinglinks.msg.strategy;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.SpringUtils;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.msg.entity.DefInterface;
import com.mqttsnet.thinglinks.msg.entity.ExtendInterfaceLog;
import com.mqttsnet.thinglinks.msg.entity.ExtendInterfaceLogging;
import com.mqttsnet.thinglinks.msg.entity.ExtendMsg;
import com.mqttsnet.thinglinks.msg.entity.ExtendMsgRecipient;
import com.mqttsnet.thinglinks.msg.entity.ExtendMsgTemplate;
import com.mqttsnet.thinglinks.msg.enumeration.InterfaceExecModeEnum;
import com.mqttsnet.thinglinks.msg.enumeration.MsgInterfaceLoggingStatusEnum;
import com.mqttsnet.thinglinks.msg.enumeration.TaskStatus;
import com.mqttsnet.thinglinks.msg.glue.GlueFactory;
import com.mqttsnet.thinglinks.msg.manager.ExtendInterfaceLogManager;
import com.mqttsnet.thinglinks.msg.manager.ExtendInterfaceLoggingManager;
import com.mqttsnet.thinglinks.msg.manager.ExtendMsgManager;
import com.mqttsnet.thinglinks.msg.strategy.domain.MsgParam;
import com.mqttsnet.thinglinks.msg.strategy.domain.MsgResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author mqttsnet
 * @date 2022/7/10 0010 14:13
 */
@Component
@Slf4j

@RequiredArgsConstructor
public class MsgContext {
    private final ExtendInterfaceLogManager extendInterfaceLogManager;
    private final ExtendMsgManager extendMsgManager;
    private final ExtendInterfaceLoggingManager extendInterfaceLoggingManager;

    @Transactional(rollbackFor = Exception.class)
    @DS(DsConstant.BASE_TENANT)
    public boolean execSend(ExtendMsg extendMsg,
                            ExtendMsgTemplate extendMsgTemplate,
                            List<ExtendMsgRecipient> recipientList,
                            DefInterface defInterface,
                            Map<String, Object> propertyParams) {
        ExtendInterfaceLog extendInterfaceLog = extendInterfaceLogManager.getByInterfaceId(defInterface.getId());
        if (extendInterfaceLog == null) {
            extendInterfaceLog = new ExtendInterfaceLog();
            extendInterfaceLog.setInterfaceId(defInterface.getId());
            extendInterfaceLog.setName(defInterface.getName());
            extendInterfaceLog.setFailCount(0);
            extendInterfaceLog.setSuccessCount(0);
            extendInterfaceLogManager.save(extendInterfaceLog);
        }

        ExtendInterfaceLogging logging = ExtendInterfaceLogging.builder()
                .status(MsgInterfaceLoggingStatusEnum.INIT.getValue())
                .logId(extendInterfaceLog.getId())
                .bizId(extendMsg.getBizId())
                .execTime(LocalDateTime.now())
                .params(extendMsg.getParam())
                .build();


        MsgParam msgParam = MsgParam.builder().extendMsg(extendMsg).extendMsgTemplate(extendMsgTemplate)
                .propertyParams(propertyParams)
                .recipientList(recipientList).build();

        try {
            MsgResult result;
            MsgStrategy msgStrategy;
            if (InterfaceExecModeEnum.IMPL_CLASS.eq(defInterface.getExecMode())) {
                // 实现类
                String implClass = defInterface.getImplClass();
                msgStrategy = SpringUtils.getBean(implClass, MsgStrategy.class);
                ArgumentAssert.notNull(msgStrategy, "实现类[{}]不存在", implClass);
                result = msgStrategy.exec(msgParam);
            } else {
                /*
                 * 注意： 脚本中，不支持lombok注解
                 */
                msgStrategy = GlueFactory.getInstance().loadNewInstance(defInterface.getScript());
                ArgumentAssert.notNull(msgStrategy, "实现类不存在");
                result = msgStrategy.exec(msgParam);
            }

            boolean success = msgStrategy.isSuccess(result);
            if (success) {
                logging.setStatus(MsgInterfaceLoggingStatusEnum.SUCCESS.getValue());
                extendMsg.setStatus(TaskStatus.SUCCESS);
                extendInterfaceLogManager.incrSuccessCount(extendInterfaceLog.getId());
            } else {
                extendMsg.setStatus(TaskStatus.FAIL);
                logging.setStatus(MsgInterfaceLoggingStatusEnum.FAIL.getValue());
                extendInterfaceLogManager.incrFailCount(extendInterfaceLog.getId());
            }

            logging.setResult(JSON.toJSONString(result));

            extendMsg.setTitle(result.getTitle());
            extendMsg.setContent(result.getContent());
            extendMsgManager.updateById(extendMsg);
        } catch (Exception e) {

            extendMsg.setStatus(TaskStatus.FAIL);
            extendMsgManager.updateById(extendMsg);

            log.error("执行发送消息失败", e);
            logging.setStatus(MsgInterfaceLoggingStatusEnum.FAIL.getValue());
            logging.setErrorMsg(ExceptionUtil.getRootCauseMessage(e));
            extendInterfaceLogManager.incrFailCount(extendInterfaceLog.getId());

        } finally {
            extendInterfaceLoggingManager.save(logging);
        }
        return true;
    }


}
