package com.mqttsnet.thinglinks.video.notify;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.Kv;
import com.mqttsnet.thinglinks.context.ContextAwareExecutor;
import com.mqttsnet.thinglinks.msg.facade.MsgFacade;
import com.mqttsnet.thinglinks.msg.vo.save.ExtendMsgRecipientSaveVO;
import com.mqttsnet.thinglinks.msg.vo.update.ExtendMsgSendVO;
import com.mqttsnet.thinglinks.video.entity.device.VideoNotifySubscription;
import com.mqttsnet.thinglinks.video.service.device.VideoNotifySubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Description:
 * 视频通知分发器。
 * <p>
 * 根据事件类型匹配订阅配置 → 渲染模板 → 解析凭证到 configList → MsgFacade.sendByTemplate()。
 * <p>
 * 参考: RuleAlarmRecordServiceImpl.sendAlarmNotification() 完全相同的调用模式。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoNotifyDispatcher {

    private final VideoNotifySubscriptionService subscriptionService;
    private final MsgFacade msgFacade;
    private final ContextAwareExecutor contextAwareExecutor;
    @Qualifier("videoDefaultExecutor") private final ThreadPoolExecutor videoDefaultExecutor;

    @Value("${notify.domain:}")
    private String notifyDomain;

    /**
     * 异步分发通知。
     * <p>
     * 使用 ContextAwareExecutor 自动传递租户上下文，调用方无需手动 getLocalMap。
     * 注意：已在 executeWithContext 异步上下文中的调用方应使用 {@link #dispatchSync} 避免嵌套异步。
     */
    public void dispatch(NotifyRequest req) {
        contextAwareExecutor.executeWithContext(() -> {
            doDispatch(req);
            return null;
        }, videoDefaultExecutor);
    }

    /**
     * 同步分发通知（在已有异步上下文中调用，避免嵌套异步）。
     */
    public void dispatchSync(NotifyRequest req) {
        doDispatch(req);
    }

    private void doDispatch(NotifyRequest req) {
        // 查匹配订阅: status=1 + event_types 包含当前事件
        List<VideoNotifySubscription> subs = subscriptionService.findMatchingByEventType(req.getEventType());
        if (CollUtil.isEmpty(subs)) {
            log.debug("[视频通知] 无匹配订阅, eventType={}", req.getEventType());
            return;
        }

        for (VideoNotifySubscription sub : subs) {
            // 3. 优先级过滤 (仅告警事件)
            if (StrUtil.isNotBlank(sub.getPriorityFilter()) && StrUtil.isNotBlank(req.getPriority())) {
                if (!sub.getPriorityFilter().contains(req.getPriority())) {
                    continue;
                }
            }
            try {
                sendBySubscription(sub, req);
            } catch (Exception e) {
                log.warn("[视频通知] 订阅={} 事件={} 发送失败", sub.getSubscriptionName(), req.getEventType(), e);
            }
        }
    }

    private void sendBySubscription(VideoNotifySubscription sub, NotifyRequest req) {
        // 4. 合并变量 + 注入系统变量
        Map<String, String> vars = new HashMap<>(Optional.ofNullable(req.getVariables()).orElseGet(HashMap::new));
        vars.putIfAbsent("sys.domain", StrUtil.nullToDefault(notifyDomain, ""));
        vars.putIfAbsent("sys.appPath", "/video");

        // 5. 渲染跳转链接模板
        String jumpUrl = renderTemplate(sub.getJumpUrlTemplate(), vars);
        vars.put("jumpUrl", StrUtil.nullToDefault(jumpUrl, ""));

        // 6. 渲染消息内容模板 (有自定义模板则使用，否则留空让 ExtendMsgTemplate 默认)
        String content = renderTemplate(sub.getMsgTemplate(), vars);

        // 7. 解析渠道凭证 → configList (运行时覆盖 DefInterfaceProperty)
        List<Kv> configList = parseChannelConfig(sub.getChannelConfig(), sub.getChannelType());
        if (ObjectUtil.equal(sub.getAtAll(), 1)) {
            configList.add(Kv.builder().key("isAtAll").value("YES").build());
        }

        // 8. 解析接收人
        List<String> recipients = resolveRecipients(sub, req);

        // 9. 构建 ExtendMsgSendVO → MsgFacade.sendByTemplate()
        ExtendMsgSendVO vo = new ExtendMsgSendVO();
        vo.setCode(sub.getTemplateCode());
        vo.setTitle(req.getTitle());
        if (StrUtil.isNotBlank(content)) {
            vo.setContent(content);
        }
        vo.setConfigList(configList);
        vo.setRecipientList(recipients.stream()
                .map(r -> ExtendMsgRecipientSaveVO.builder().recipient(r).build())
                .toList());

        Boolean result = msgFacade.sendByTemplate(vo);
        log.info("[视频通知] 发送{}: 订阅={}, 渠道={}, 模板={}, 接收人数={}",
                Boolean.TRUE.equals(result) ? "成功" : "失败",
                sub.getSubscriptionName(), sub.getChannelType(), sub.getTemplateCode(), recipients.size());
    }

    /**
     * 模板变量替换: ${key} → value
     */
    private String renderTemplate(String template, Map<String, String> variables) {
        if (StrUtil.isBlank(template)) {
            return "";
        }
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", StrUtil.nullToDefault(entry.getValue(), ""));
        }
        return result;
    }

    /**
     * 解析渠道凭证 JSON → Kv 列表 (参考 RuleAlarmRecordServiceImpl.parseChannelConfig)
     */
    private List<Kv> parseChannelConfig(String configJson, String channelType) {
        List<Kv> configList = new ArrayList<>();
        if (StrUtil.isBlank(configJson)) {
            return configList;
        }
        try {
            Map<String, String> configMap = JSON.parseObject(configJson, Map.class);
            Optional.ofNullable(configMap).ifPresent(map ->
                    map.forEach((key, value) -> configList.add(Kv.builder().key(key).value(value).build()))
            );
            if ("DINGTALK".equals(channelType) || "ENTERPRISE_WECHAT".equals(channelType)) {
                configList.add(Kv.builder().key("msgType").value("MARKDOWN").build());
            }
        } catch (Exception e) {
            log.error("[视频通知] 解析渠道凭证失败: channelType={}, error={}", channelType, e.getMessage());
        }
        return configList;
    }

    /**
     * 解析接收人: 优先订阅配置 → 兜底源数据创建人
     */
    private List<String> resolveRecipients(VideoNotifySubscription sub, NotifyRequest req) {
        if (StrUtil.isNotBlank(sub.getRecipientIds())) {
            return StrUtil.split(sub.getRecipientIds(), ',');
        }
        return ObjectUtil.isNotNull(req.getSourceCreatedBy())
                ? List.of(String.valueOf(req.getSourceCreatedBy()))
                : List.of();
    }
}
