package com.mqttsnet.thinglinks.service.alarm.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.model.Kv;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.basic.utils.StringUtils;
import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmActionConfigDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmChannelTemplateDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmRecipientDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmRenderedNotificationDTO;
import com.mqttsnet.thinglinks.dto.alarm.channel.dingtalk.DingTalkMessageParamDTO;
import com.mqttsnet.thinglinks.dto.alarm.channel.fs.FeishuMessageParamDTO;
import com.mqttsnet.thinglinks.dto.alarm.channel.site.SiteMessageParamDTO;
import com.mqttsnet.thinglinks.dto.alarm.channel.wechat.WeChatWorkMessageParamDTO;
import com.mqttsnet.thinglinks.dto.linkage.RulePolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.entity.alarm.RuleAlarmRecord;
import com.mqttsnet.thinglinks.enumeration.alarm.AlarmChannelStatusEnum;
import com.mqttsnet.thinglinks.enumeration.alarm.AlarmChannelTypeEnum;
import com.mqttsnet.thinglinks.enumeration.alarm.AlarmRecordHandledStatusEnum;
import com.mqttsnet.thinglinks.manager.alarm.RuleAlarmRecordManager;
import com.mqttsnet.thinglinks.msg.enumeration.NoticeRemindModeEnum;
import com.mqttsnet.thinglinks.msg.facade.MsgFacade;
import com.mqttsnet.thinglinks.msg.vo.save.ExtendMsgRecipientSaveVO;
import com.mqttsnet.thinglinks.msg.vo.update.ExtendMsgPublishVO;
import com.mqttsnet.thinglinks.msg.vo.update.ExtendMsgSendVO;
import com.mqttsnet.thinglinks.protocol.vo.param.DeviceAlarmNotificationRequestParam;
import com.mqttsnet.thinglinks.service.alarm.RuleAlarmChannelService;
import com.mqttsnet.thinglinks.service.alarm.RuleAlarmRecordService;
import com.mqttsnet.thinglinks.service.alarm.RuleAlarmService;
import com.mqttsnet.thinglinks.service.alarm.RuleNotificationTemplateService;
import com.mqttsnet.thinglinks.vo.param.linkage.RuleAlarmRecordHandleParamVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmChannelDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmRecordDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmRecordResultVO;
import com.mqttsnet.thinglinks.vo.save.alarm.RuleAlarmRecordSaveVO;
import com.mqttsnet.thinglinks.vo.update.alarm.RuleAlarmRecordUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 业务实现类
 * 告警记录表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-09-09 21:15:22
 * @create [2023-09-09 21:15:22] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class RuleAlarmRecordServiceImpl extends SuperServiceImpl<RuleAlarmRecordManager, Long, RuleAlarmRecord> implements RuleAlarmRecordService {

    @Autowired
    private RuleAlarmService ruleAlarmService;

    @Autowired
    private RuleAlarmChannelService ruleAlarmChannelService;

    @Autowired
    private MsgFacade msgApi;

    @Autowired
    private RuleNotificationTemplateService ruleNotificationTemplateService;

    /**
     * Save the alarm record.
     *
     * @param saveVO Save parameters
     * @return Saved entity
     */
    @Override
    public RuleAlarmRecordSaveVO saveAlarmRecord(RuleAlarmRecordSaveVO saveVO) {
        log.info("saveRuleAlarmRecord saveVO:{}", saveVO);

        // Validate parameters
        checkRuleAlarmRecordSaveVO(saveVO);

        // Construct parameters
        RuleAlarmRecord ruleAlarmRecord = buildRuleAlarmRecordSaveVO(saveVO);

        // Save alarm record
        superManager.save(ruleAlarmRecord);

        return BeanPlusUtil.toBeanIgnoreError(ruleAlarmRecord, RuleAlarmRecordSaveVO.class);
    }

    /**
     * Validate save parameters.
     *
     * @param saveVO Save parameters
     */
    private void checkRuleAlarmRecordSaveVO(RuleAlarmRecordSaveVO saveVO) {
        ArgumentAssert.notBlank(saveVO.getAppId(), "Application ID cannot be blank");
        // ... Continue with other checks based on your requirements
    }

    /**
     * Construct save parameters.
     *
     * @param saveVO Save parameters
     * @return Alarm record entity
     */
    private RuleAlarmRecord buildRuleAlarmRecordSaveVO(RuleAlarmRecordSaveVO saveVO) {
        RuleAlarmRecord ruleAlarmRecord = BeanPlusUtil.copyProperties(saveVO, RuleAlarmRecord.class);
        if (ruleAlarmRecord.getCreatedOrgId() == null) {
            Long currentDeptId = ContextUtil.getCurrentDeptId();
            if (currentDeptId != null) {
                ruleAlarmRecord.setCreatedOrgId(currentDeptId);
            }
        }
        return ruleAlarmRecord;
    }

    /**
     * Update the alarm record.
     *
     * @param updateVO Update parameters
     * @return Updated result
     */
    @Override
    public RuleAlarmRecordUpdateVO updateAlarmRecord(RuleAlarmRecordUpdateVO updateVO) {
        log.info("updateRuleAlarmRecord updateVO:{}", updateVO);

        // Validate parameters
        checkRuleAlarmRecordUpdateVO(updateVO);

        // Construct parameters
        RuleAlarmRecord ruleAlarmRecord = buildRuleAlarmRecordUpdateVO(updateVO);

        // Update alarm record
        superManager.updateById(ruleAlarmRecord);

        return updateVO;
    }

    /**
     * Validate update parameters.
     *
     * @param updateVO Update parameters
     */
    private void checkRuleAlarmRecordUpdateVO(RuleAlarmRecordUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "ID cannot be null");
        // ... Continue with other checks based on your requirements
    }

    /**
     * Construct update parameters.
     *
     * @param updateVO Update parameters
     * @return Alarm record entity
     */
    private RuleAlarmRecord buildRuleAlarmRecordUpdateVO(RuleAlarmRecordUpdateVO updateVO) {
        return BeanPlusUtil.copyProperties(updateVO, RuleAlarmRecord.class);
    }

    /**
     * Delete the alarm record.
     *
     * @param id ID
     * @return Boolean indicating success or failure
     */
    @Override
    public Boolean deleteAlarmRecord(Long id) {
        ArgumentAssert.notNull(id, "ID cannot be null");

        RuleAlarmRecord ruleAlarmRecord = superManager.getById(id);
        if (ruleAlarmRecord == null) {
            throw BizException.wrap("The rule alarm record does not exist");
        }

        // TODO Validate if the alarm record is being used elsewhere or has dependencies
        // if (isRuleAlarmRecordInUse(id)) {
        //     throw BizException.wrap("The rule alarm record is currently in use and cannot be deleted");
        // }

        return superManager.removeById(id);
    }

    /**
     * Get details of the alarm record.
     *
     * @param id ID
     * @return Alarm record details
     */
    @Override
    public RuleAlarmRecordDetailsResultVO getAlarmRecordDetails(Long id) {
        ArgumentAssert.notNull(id, "Rule alarm record ID cannot be null");

        RuleAlarmRecord ruleAlarmRecord = superManager.getById(id);
        if (ruleAlarmRecord == null) {
            throw BizException.wrap("Rule alarm record does not exist");
        }
        RuleAlarmRecordDetailsResultVO alarmRecordDetailsResultVO = BeanPlusUtil.toBeanIgnoreError(ruleAlarmRecord, RuleAlarmRecordDetailsResultVO.class);

        RuleAlarmDetailsResultVO ruleAlarmDetailsResultVO = ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification(ruleAlarmRecord.getAlarmIdentification());
        if (Objects.nonNull(ruleAlarmDetailsResultVO)) {
            alarmRecordDetailsResultVO.setRuleAlarmDetailsResultVO(ruleAlarmDetailsResultVO);
        }

        return alarmRecordDetailsResultVO;
    }

    @Override
    public void fillAlarmRuleDetails(List<RuleAlarmRecordResultVO> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        Map<String, RuleAlarmDetailsResultVO> detailCache = new HashMap<>();
        records.stream()
                .filter(Objects::nonNull)
                .forEach(record -> {
                    String alarmIdentification = record.getAlarmIdentification();
                    if (StrUtil.isBlank(alarmIdentification)) {
                        return;
                    }
                    RuleAlarmDetailsResultVO detail = detailCache.computeIfAbsent(alarmIdentification,
                            ruleAlarmService::getRuleAlarmDetailsByAlarmIdentification);
                    if (detail != null) {
                        record.setRuleAlarmDetailsResultVO(detail);
                    }
                });
    }

    /**
     * handle or solve alarm record
     *
     * @param recordHandleParamVO parameters
     * @return {@link RuleAlarmRecordUpdateVO} updated alarm record entity
     */
    @Override
    public RuleAlarmRecordUpdateVO handleOrSolveAlarmRecord(RuleAlarmRecordHandleParamVO recordHandleParamVO) {
        log.info("handleAlarmRecord handleDTO: {}", recordHandleParamVO);

        // Validate parameters
        ArgumentAssert.notNull(recordHandleParamVO.getId(), "ID cannot be null");
        ArgumentAssert.notNull(recordHandleParamVO.getHandledStatus(), "Handle status cannot be null");
        ArgumentAssert.notEmpty(recordHandleParamVO.getHandleNotes(), "Handle notes cannot be empty");

        // Retrieve the existing alarm record
        RuleAlarmRecord existingRecord = superManager.getById(recordHandleParamVO.getId());
        if (existingRecord == null) {
            throw new RuntimeException("Alarm record with ID " + recordHandleParamVO.getId() + " not found.");
        }


        // Update the alarm record based on handleType
        switch (AlarmRecordHandledStatusEnum.valueOf(recordHandleParamVO.getHandledStatus())) {
            case IN_PROGRESS:
                existingRecord.setHandledTime(LocalDateTime.now());
                existingRecord.setHandlingNotes(recordHandleParamVO.getHandleNotes());
                existingRecord.setHandledStatus(AlarmRecordHandledStatusEnum.IN_PROGRESS.getValue());
                break;
            case RESOLVED:
                existingRecord.setResolvedTime(LocalDateTime.now());
                existingRecord.setResolutionNotes(recordHandleParamVO.getHandleNotes());
                existingRecord.setHandledStatus(AlarmRecordHandledStatusEnum.RESOLVED.getValue());
                break;
        }

        // Save the updated record
        superManager.updateById(existingRecord);

        // Convert and return updated entity
        return BeanPlusUtil.toBeanIgnoreError(existingRecord, RuleAlarmRecordUpdateVO.class);
    }


    @Override
    public boolean triggerDeviceAlarm(DeviceAlarmNotificationRequestParam requestParam) {
        RuleAlarmActionConfigDTO legacyConfig = RuleAlarmActionConfigDTO.builder()
                .version(2)
                .alarmIdentification(requestParam.getAlarmIdentification())
                .atPhone(requestParam.getAtPhone())
                .contentData(requestParam.getContentData())
                .build();
        return triggerDeviceAlarm(legacyConfig, null);
    }

    @Override
    public boolean triggerDeviceAlarm(RuleAlarmActionConfigDTO actionConfig, PolicyContext policyContext) {
        ArgumentAssert.notNull(actionConfig, "Alarm action config cannot be null");
        ArgumentAssert.notBlank(actionConfig.getAlarmIdentification(), "Alarm identification cannot be blank");

        RuleAlarmDetailsResultVO ruleAlarmDetailsResultVO = ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification(actionConfig.getAlarmIdentification());
        ArgumentAssert.notNull(ruleAlarmDetailsResultVO, "Alarm rule does not exist: {}", actionConfig.getAlarmIdentification());

        List<RuleAlarmChannelDetailsResultVO> channels = Optional.ofNullable(ruleAlarmDetailsResultVO.getRuleAlarmChannelDetailsResultVOList())
                .orElse(new ArrayList<>());
        List<RuleAlarmChannelTemplateDTO> configuredTemplates = Optional.ofNullable(actionConfig.getChannelTemplates())
                .orElse(new ArrayList<>())
                .stream()
                .filter(Objects::nonNull)
                .toList();
        List<Integer> configuredChannelTypes = configuredTemplates.stream()
                .filter(item -> !Boolean.FALSE.equals(item.getEnabled()))
                .map(RuleAlarmChannelTemplateDTO::getChannelType)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        boolean useConfiguredChannels = CollUtil.isNotEmpty(configuredTemplates);
        List<AlarmNotificationDispatch> notificationDispatches = new ArrayList<>();
        channels.stream()
                .filter(item -> AlarmChannelStatusEnum.ACTIVATED.getValue().equals(item.getStatus()))
                .filter(item -> !useConfiguredChannels || configuredChannelTypes.contains(item.getChannelType()))
                .forEach(ruleAlarmChannelResultVO -> {
                    try {
                        RuleAlarmChannelTemplateDTO template = ruleNotificationTemplateService.resolveChannelTemplate(
                                actionConfig,
                                ruleAlarmChannelResultVO.getChannelType(),
                                ruleAlarmDetailsResultVO);
                        RuleAlarmRenderedNotificationDTO rendered = ruleNotificationTemplateService.render(
                                actionConfig,
                                template,
                                ruleAlarmDetailsResultVO,
                                ruleNotificationTemplateService.buildRuntimeVariables(policyContext, ruleAlarmDetailsResultVO));
                        notificationDispatches.add(new AlarmNotificationDispatch(rendered, ruleAlarmChannelResultVO));
                    } catch (Exception e) {
                        log.error("Failed to render alarm notification for channel: {}, error: {}", ruleAlarmChannelResultVO.getChannelType(), e.getMessage(), e);
                    }
                });
        List<RuleAlarmRenderedNotificationDTO> renderedNotifications = notificationDispatches.stream()
                .map(AlarmNotificationDispatch::getRendered)
                .toList();
        saveAlarmRecord(createAlarmRecordSaveVO(actionConfig, ruleAlarmDetailsResultVO, renderedNotifications, policyContext));
        notificationDispatches.forEach(dispatch -> {
            try {
                sendAlarmNotification(dispatch.getRendered(), ruleAlarmDetailsResultVO, dispatch.getChannel());
            } catch (Exception e) {
                log.error("Failed to send alarm notification for channel: {}, error: {}",
                        dispatch.getChannel().getChannelType(), e.getMessage(), e);
            }
        });
        return true;
    }

    private void sendAlarmNotification(RuleAlarmRenderedNotificationDTO rendered,
                                       RuleAlarmDetailsResultVO ruleAlarmDetailsResultVO,
                                       RuleAlarmChannelDetailsResultVO ruleAlarmChannelDetailsResultVO) {
        AlarmChannelTypeEnum alarmChannelTypeEnum = AlarmChannelTypeEnum.fromValue(ruleAlarmChannelDetailsResultVO.getChannelType()).orElse(null);
        if (alarmChannelTypeEnum == null) {
            log.error("Alarm channel type not found: {}", ruleAlarmChannelDetailsResultVO.getChannelType());
            return;
        }
        if (AlarmChannelTypeEnum.SITE_MESSAGE.equals(alarmChannelTypeEnum)) {
            sendSiteNotification(rendered, ruleAlarmChannelDetailsResultVO);
            return;
        }
        sendRobotNotification(rendered, alarmChannelTypeEnum, ruleAlarmChannelDetailsResultVO);
    }

    private void sendRobotNotification(RuleAlarmRenderedNotificationDTO rendered,
                                       AlarmChannelTypeEnum alarmChannelTypeEnum,
                                       RuleAlarmChannelDetailsResultVO ruleAlarmChannelDetailsResultVO) {
        ExtendMsgSendVO extendMsgSendVO = new ExtendMsgSendVO();

        // Set template code
        extendMsgSendVO.setCode(alarmChannelTypeEnum.getChannelTemplateCode());

        // Parse channel configuration parameters
        List<Kv> configList = parseChannelConfig(ruleAlarmChannelDetailsResultVO.getChannelConfig(), alarmChannelTypeEnum);
        List<ExtendMsgRecipientSaveVO> recipientList = createRobotRecipientList(rendered);

        boolean isAtAll = Boolean.TRUE.equals(rendered.getAtAll()) || recipientList.stream()
                .anyMatch(recipient -> BizConstant.ALL.equals(recipient.getRecipient()));

        if (isAtAll) {
            configList.add(Kv.builder().key("isAtAll").value(StrPool.YES).build());
        }

        // Set recipients and configuration
        if (CollUtil.isNotEmpty(recipientList)) {
            extendMsgSendVO.setRecipientList(recipientList);
        }
        if (CollUtil.isNotEmpty(configList)) {
            extendMsgSendVO.setConfigList(configList);
        }

        // Set alarm content
        extendMsgSendVO.setContent(rendered.getContent());
        extendMsgSendVO.setTitle(rendered.getTitle());

        // Send the alarm message
        if (CollUtil.isEmpty(recipientList)) {
            log.warn("Skip alarm robot notification because recipient list is empty. channelType={}", alarmChannelTypeEnum);
            return;
        }
        Boolean booleanR = msgApi.sendByTemplate(extendMsgSendVO);
        log.info("Send alarm message result: {}", booleanR);
        if (!booleanR) {
            log.error("Failed to send alarm message: {}", booleanR);
        }
    }

    private void sendSiteNotification(RuleAlarmRenderedNotificationDTO rendered,
                                      RuleAlarmChannelDetailsResultVO ruleAlarmChannelDetailsResultVO) {
        SiteMessageParamDTO siteConfig = parseSiteMessageConfig(ruleAlarmChannelDetailsResultVO.getChannelConfig());
        List<String> employeeIds = resolveSiteMessageRecipients(rendered, siteConfig);
        if (CollUtil.isEmpty(employeeIds)) {
            log.warn("Skip site alarm notification because employee recipient list is empty.");
            return;
        }
        ExtendMsgPublishVO publishVO = ExtendMsgPublishVO.builder()
                .title(rendered.getTitle())
                .content(rendered.getContent())
                .recipientList(employeeIds)
                .author("规则引擎")
                .remindMode(normalizeRemindMode(siteConfig.getRemindMode()))
                .draft(false)
                .url(StrUtil.blankToDefault(rendered.getUrl(), siteConfig.getUrl()))
                .target(StrUtil.blankToDefault(siteConfig.getTarget(), "01"))
                .autoRead(siteConfig.getAutoRead() == null ? false : siteConfig.getAutoRead())
                .build();
        Boolean result = msgApi.publish(publishVO);
        log.info("Publish site alarm notice result: {}", result);
    }

    private SiteMessageParamDTO parseSiteMessageConfig(String channelConfig) {
        if (StrUtil.isBlank(channelConfig)) {
            return defaultSiteMessageConfig();
        }
        return parseConfig(channelConfig, SiteMessageParamDTO.class)
                .map(config -> SiteMessageParamDTO.builder()
                        .remindMode(StrUtil.blankToDefault(config.getRemindMode(),
                                NoticeRemindModeEnum.EARLY_WARNING.getCode()))
                        .target(StrUtil.blankToDefault(config.getTarget(), "01"))
                        .autoRead(config.getAutoRead() == null ? false : config.getAutoRead())
                        .url(config.getUrl())
                        .recipientList(normalizeSiteRecipientList(config.getRecipientList()))
                        .build())
                .orElseGet(this::defaultSiteMessageConfig);
    }

    private SiteMessageParamDTO defaultSiteMessageConfig() {
        return SiteMessageParamDTO.builder()
                .remindMode(NoticeRemindModeEnum.EARLY_WARNING.getCode())
                .target("01")
                .autoRead(false)
                .recipientList(new ArrayList<>())
                .build();
    }

    private List<String> resolveSiteMessageRecipients(RuleAlarmRenderedNotificationDTO rendered,
                                                       SiteMessageParamDTO siteConfig) {
        List<String> actionRecipients = Optional.ofNullable(rendered.getRecipients())
                .orElse(new ArrayList<>())
                .stream()
                .filter(item -> item != null && StrUtil.equalsIgnoreCase("EMPLOYEE", item.getType()))
                .map(RuleAlarmRecipientDTO::getValue)
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .toList();
        if (CollUtil.isNotEmpty(actionRecipients)) {
            return actionRecipients;
        }
        return normalizeSiteRecipientList(siteConfig.getRecipientList());
    }

    private List<String> normalizeSiteRecipientList(List<String> recipientList) {
        if (recipientList == null) {
            return new ArrayList<>();
        }
        return recipientList.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String normalizeRemindMode(String remindMode) {
        if (Arrays.stream(NoticeRemindModeEnum.values()).anyMatch(item -> item.getCode().equals(remindMode))) {
            return remindMode;
        }
        return NoticeRemindModeEnum.EARLY_WARNING.getCode();
    }

    private List<ExtendMsgRecipientSaveVO> createRobotRecipientList(RuleAlarmRenderedNotificationDTO rendered) {
        List<ExtendMsgRecipientSaveVO> recipientList = new ArrayList<>();
        Optional.ofNullable(rendered.getRecipients())
                .orElse(new ArrayList<>())
                .stream()
                .filter(item -> item != null && (StrUtil.equalsIgnoreCase("PHONE", item.getType()) || StrUtil.equalsIgnoreCase("ALL", item.getType())))
                .map(RuleAlarmRecipientDTO::getValue)
                .filter(StrUtil::isNotBlank)
                .flatMap(value -> Arrays.stream(value.split(StrUtil.COMMA)))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .forEach(value -> {
                    ExtendMsgRecipientSaveVO recipient = new ExtendMsgRecipientSaveVO();
                    recipient.setRecipient(value);
                    recipientList.add(recipient);
                });
        if (Boolean.TRUE.equals(rendered.getAtAll()) && recipientList.stream().noneMatch(item -> BizConstant.ALL.equals(item.getRecipient()))) {
            ExtendMsgRecipientSaveVO recipient = new ExtendMsgRecipientSaveVO();
            recipient.setRecipient(BizConstant.ALL);
            recipientList.add(recipient);
        }
        return recipientList;
    }

    private List<ExtendMsgRecipientSaveVO> createRecipientList(DeviceAlarmNotificationRequestParam requestParam) {
        List<ExtendMsgRecipientSaveVO> recipientList = new ArrayList<>();
        String atPhone = requestParam.getAtPhone();

        if (StringUtils.isNotBlank(atPhone)) {
            Arrays.stream(atPhone.split(StrUtil.COMMA))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .forEach(phone -> {
                        ExtendMsgRecipientSaveVO recipient = new ExtendMsgRecipientSaveVO();
                        recipient.setRecipient(phone);
                        recipientList.add(recipient);
                    });
        }

        return recipientList;
    }

    private RuleAlarmRecordSaveVO createAlarmRecordSaveVO(RuleAlarmActionConfigDTO actionConfig,
                                                          RuleAlarmDetailsResultVO ruleAlarmDetailsResultVO,
                                                          List<RuleAlarmRenderedNotificationDTO> renderedNotifications,
                                                          PolicyContext policyContext) {
        RuleAlarmRecordSaveVO saveVO = new RuleAlarmRecordSaveVO();
        saveVO.setAppId(resolveAlarmRecordAppId(ruleAlarmDetailsResultVO, policyContext));
        saveVO.setAlarmIdentification(actionConfig.getAlarmIdentification());
        saveVO.setOccurredTime(LocalDateTime.now());
        saveVO.setContentData(buildRecordContent(actionConfig, renderedNotifications));
        saveVO.setHandledStatus(AlarmRecordHandledStatusEnum.PENDING.getValue());
        saveVO.setRemark("Alarm triggered and notification sent.");
        saveVO.setCreatedBy(resolveAlarmRecordCreatedBy(ruleAlarmDetailsResultVO));
        saveVO.setCreatedOrgId(resolveAlarmRecordCreatedOrgId(ruleAlarmDetailsResultVO, policyContext));
        return saveVO;
    }

    private Long resolveAlarmRecordCreatedBy(RuleAlarmDetailsResultVO ruleAlarmDetailsResultVO) {
        return Optional.ofNullable(ruleAlarmDetailsResultVO)
                .map(RuleAlarmDetailsResultVO::getCreatedBy)
                .orElse(null);
    }

    private String resolveAlarmRecordAppId(RuleAlarmDetailsResultVO ruleAlarmDetailsResultVO,
                                           PolicyContext policyContext) {
        String alarmAppId = Optional.ofNullable(ruleAlarmDetailsResultVO)
                .map(RuleAlarmDetailsResultVO::getAppId)
                .orElse(null);
        if (StrUtil.isNotBlank(alarmAppId)) {
            return alarmAppId;
        }
        return Optional.ofNullable(policyContext)
                .map(PolicyContext::getRulePolicyDTO)
                .map(RulePolicyDTO::getAppId)
                .orElse(null);
    }

    private Long resolveAlarmRecordCreatedOrgId(RuleAlarmDetailsResultVO ruleAlarmDetailsResultVO,
                                                PolicyContext policyContext) {
        Long alarmCreatedOrgId = Optional.ofNullable(ruleAlarmDetailsResultVO)
                .map(RuleAlarmDetailsResultVO::getCreatedOrgId)
                .orElse(null);
        if (alarmCreatedOrgId != null) {
            return alarmCreatedOrgId;
        }
        return Optional.ofNullable(policyContext)
                .map(PolicyContext::getRulePolicyDTO)
                .map(RulePolicyDTO::getCreatedOrgId)
                .orElse(null);
    }

    private String buildRecordContent(RuleAlarmActionConfigDTO actionConfig,
                                      List<RuleAlarmRenderedNotificationDTO> renderedNotifications) {
        if (CollUtil.isNotEmpty(renderedNotifications)) {
            RuleAlarmRenderedNotificationDTO first = renderedNotifications.get(0);
            return StrUtil.blankToDefault(first.getContent(), first.getTitle());
        }
        return StrUtil.blankToDefault(actionConfig.getContentData(), "Alarm triggered.");
    }

    private static class AlarmNotificationDispatch {
        private final RuleAlarmRenderedNotificationDTO rendered;
        private final RuleAlarmChannelDetailsResultVO channel;

        private AlarmNotificationDispatch(RuleAlarmRenderedNotificationDTO rendered,
                                          RuleAlarmChannelDetailsResultVO channel) {
            this.rendered = rendered;
            this.channel = channel;
        }

        private RuleAlarmRenderedNotificationDTO getRendered() {
            return rendered;
        }

        private RuleAlarmChannelDetailsResultVO getChannel() {
            return channel;
        }
    }

    private List<Kv> parseChannelConfig(String channelConfig, AlarmChannelTypeEnum alarmChannelTypeEnum) {
        if (StrUtil.isBlank(channelConfig)) {
            log.error("Channel config is empty: {}", channelConfig);
            return new ArrayList<>();
        }
        List<Kv> cofigList = new ArrayList<>();
        switch (alarmChannelTypeEnum) {
            case DING_TALK:
                Optional<DingTalkMessageParamDTO> dingTalkMessageParam = parseConfig(channelConfig, DingTalkMessageParamDTO.class);
                if (dingTalkMessageParam.isPresent()) {
                    cofigList.add(Kv.builder().key("msgType").value("MARKDOWN").build());
                    cofigList.add(Kv.builder().key("secret").value(dingTalkMessageParam.get().getSecret()).build());
                    cofigList.add(Kv.builder().key("token").value(dingTalkMessageParam.get().getToken()).build());
                } else {
                    log.error("Failed to parse DingTalkMessageParamDTO from channel config, channelConfig: {} ", channelConfig);
                }
                break;
            case ENTERPRISE_WECHAT:
                Optional<WeChatWorkMessageParamDTO> weChatWorkMessageParam = parseConfig(channelConfig, WeChatWorkMessageParamDTO.class);
                if (weChatWorkMessageParam.isPresent()) {
                    cofigList.add(Kv.builder().key("msgType").value("MARKDOWN").build());
                    cofigList.add(Kv.builder().key("token").value(weChatWorkMessageParam.get().getToken()).build());
                } else {
                    log.error("Failed to parse WeChatWorkMessageParamDTO from channel config, channelConfig: {} ", channelConfig);
                }
                break;
            case FS:
                Optional<FeishuMessageParamDTO> feishuMessageParam = parseConfig(channelConfig, FeishuMessageParamDTO.class);
                if (feishuMessageParam.isPresent()) {
                    cofigList.add(Kv.builder().key("msgType").value("TEXT").build());
                    cofigList.add(Kv.builder().key("appSecret").value(feishuMessageParam.get().getAppSecret()).build());
                    cofigList.add(Kv.builder().key("appId").value(feishuMessageParam.get().getAppId()).build());
                    cofigList.add(Kv.builder().key("token").value(feishuMessageParam.get().getToken()).build());
                } else {
                    log.error("Failed to parse FeishuMessageParamDTO from channel config, channelConfig: {} ", channelConfig);
                }
                break;
            default:
                log.error("Unsupported alarm channel type: {}", alarmChannelTypeEnum);
        }
        return cofigList;
    }

    private <T> Optional<T> parseConfig(String config, Class<T> clazz) {
        try {
            return Optional.ofNullable(JSON.parseObject(config, clazz));
        } catch (Exception e) {
            log.error("Failed to parse config to {}: {}", clazz.getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }

}
