package com.mqttsnet.thinglinks.service.alarm.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.dto.alarm.channel.dingtalk.DingTalkMessageParamDTO;
import com.mqttsnet.thinglinks.dto.alarm.channel.fs.FeishuMessageParamDTO;
import com.mqttsnet.thinglinks.dto.alarm.channel.site.SiteMessageParamDTO;
import com.mqttsnet.thinglinks.dto.alarm.channel.wechat.WeChatWorkMessageParamDTO;
import com.mqttsnet.thinglinks.entity.alarm.RuleAlarmChannel;
import com.mqttsnet.thinglinks.enumeration.alarm.AlarmChannelTypeEnum;
import com.mqttsnet.thinglinks.manager.alarm.RuleAlarmChannelManager;
import com.mqttsnet.thinglinks.msg.enumeration.NoticeRemindModeEnum;
import com.mqttsnet.thinglinks.service.alarm.RuleAlarmChannelService;
import com.mqttsnet.thinglinks.vo.query.alarm.RuleAlarmChannelPageQuery;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmChannelDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmChannelResultVO;
import com.mqttsnet.thinglinks.vo.save.alarm.RuleAlarmChannelSaveVO;
import com.mqttsnet.thinglinks.vo.update.alarm.RuleAlarmChannelUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 业务实现类
 * 告警规则渠道表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-09-09 21:14:58
 * @create [2023-09-09 21:14:58] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class RuleAlarmChannelServiceImpl extends SuperServiceImpl<RuleAlarmChannelManager, Long, RuleAlarmChannel> implements RuleAlarmChannelService {


    /**
     * Save the alarm channel.
     *
     * @param saveVO Parameters for saving.
     * @return {@link RuleAlarmChannelSaveVO} Entity.
     */
    @Override
    public RuleAlarmChannelSaveVO saveAlarmChannel(RuleAlarmChannelSaveVO saveVO) {
        log.info("Saving alarm channel. channelType={}, status={}", saveVO.getChannelType(), saveVO.getStatus());

        // Validate the parameters.
        checkedAlarmChannelSaveVO(saveVO);

        // Build the parameters.
        RuleAlarmChannel alarmChannel = builderAlarmChannelSaveVO(saveVO);

        // Persist the alarm channel.
        superManager.save(alarmChannel);

        return BeanPlusUtil.toBeanIgnoreError(alarmChannel, RuleAlarmChannelSaveVO.class);
    }

    /**
     * Validate the parameters for addition.
     *
     * @param saveVO Parameters for saving.
     */
    private void checkedAlarmChannelSaveVO(RuleAlarmChannelSaveVO saveVO) {
        ArgumentAssert.notBlank(saveVO.getChannelName(), "Alarm channel name cannot be blank.");
        AlarmChannelTypeEnum channelType = validateChannelType(saveVO.getChannelType());
        saveVO.setChannelConfig(normalizeAndValidateChannelConfig(channelType, saveVO.getChannelConfig()));
    }

    /**
     * Construct save parameters.
     *
     * @param saveVO Parameters for saving.
     * @return {@link RuleAlarmChannel} Alarm channel entity.
     */
    private RuleAlarmChannel builderAlarmChannelSaveVO(RuleAlarmChannelSaveVO saveVO) {
        return BeanPlusUtil.copyProperties(saveVO, RuleAlarmChannel.class);
    }

    /**
     * Update the alarm channel.
     *
     * @param updateVO Parameters for updating.
     * @return {@link RuleAlarmChannelUpdateVO} Update result.
     */
    @Override
    public RuleAlarmChannelUpdateVO updateAlarmChannel(RuleAlarmChannelUpdateVO updateVO) {
        log.info("Updating alarm channel. id={}, channelType={}, status={}",
                updateVO.getId(), updateVO.getChannelType(), updateVO.getStatus());

        // Validate the parameters.
        checkedAlarmChannelUpdateVO(updateVO);

        // Construct the parameters.
        RuleAlarmChannel alarmChannel = builderAlarmChannelUpdateVO(updateVO);

        // Update the alarm channel in database.
        superManager.updateById(alarmChannel);

        return updateVO;
    }

    /**
     * Delete the alarm channel.
     *
     * @param id ID.
     * @return {@link Boolean} Boolean value. true: successful, false: failed.
     */
    @Override
    public Boolean deleteAlarmChannel(Long id) {
        ArgumentAssert.notNull(id, "id cannot be null");

        RuleAlarmChannel alarmChannel = superManager.getById(id);

        if (null == alarmChannel) {
            throw BizException.wrap("The alarm channel does not exist");
        }

        // TODO Validate if the alarm channel is in use, e.g., if it has associated rules or records.
        // if (isAlarmChannelInUse(id)) {
        //     throw BizException.wrap("The alarm channel is currently in use and cannot be deleted");
        // }

        return superManager.removeById(id);
    }

    /**
     * Retrieve the details of the alarm channel.
     *
     * @param id ID.
     * @return {@link RuleAlarmChannelDetailsResultVO} Entity.
     */
    @Override
    public RuleAlarmChannelDetailsResultVO getAlarmChannelDetails(Long id) {
        if (id == null) {
            throw BizException.wrap("Alarm channel ID cannot be null");
        }

        RuleAlarmChannel alarmChannel = superManager.getById(id);
        if (alarmChannel == null) {
            throw new BizException("Alarm channel does not exist");
        }

        return BeanPlusUtil.toBeanIgnoreError(alarmChannel, RuleAlarmChannelDetailsResultVO.class);
    }

    /**
     * Retrieve a list of rule alarm channel VO.
     *
     * @param query Search parameters for rule alarm channels.
     * @return {@link List<RuleAlarmChannelResultVO>} List of rule alarm channel VO.
     */
    @Override
    public List<RuleAlarmChannelResultVO> getRuleAlarmChannelResultVOList(RuleAlarmChannelPageQuery query) {
        List<RuleAlarmChannel> ruleAlarmChannelList = superManager.getRuleAlarmChannelList(query);
        return BeanPlusUtil.toBeanList(ruleAlarmChannelList, RuleAlarmChannelResultVO.class);
    }

    /**
     * Validate the parameters for updating.
     *
     * @param updateVO Parameters for updating.
     */
    private void checkedAlarmChannelUpdateVO(RuleAlarmChannelUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "id cannot be null");

        RuleAlarmChannel existingChannel = superManager.getById(updateVO.getId());
        if (null == existingChannel) {
            throw BizException.wrap("Alarm channel does not exist");
        }

        ArgumentAssert.notBlank(updateVO.getChannelName(), "Alarm channel name cannot be blank.");
        AlarmChannelTypeEnum channelType = validateChannelType(updateVO.getChannelType());
        updateVO.setChannelConfig(normalizeAndValidateChannelConfig(channelType, updateVO.getChannelConfig()));
    }

    private AlarmChannelTypeEnum validateChannelType(Integer channelType) {
        return AlarmChannelTypeEnum.fromValue(channelType)
                .orElseThrow(() -> BizException.wrap("Unsupported alarm channel type: " + channelType));
    }

    private String normalizeAndValidateChannelConfig(AlarmChannelTypeEnum channelType, String channelConfig) {
        if (AlarmChannelTypeEnum.SITE_MESSAGE.equals(channelType)) {
            SiteMessageParamDTO config = parseChannelConfig(StrUtil.blankToDefault(channelConfig, "{}"),
                    SiteMessageParamDTO.class, channelType);
            SiteMessageParamDTO normalized = SiteMessageParamDTO.builder()
                    .remindMode(normalizeRemindMode(config.getRemindMode()))
                    .target(StrUtil.blankToDefault(config.getTarget(), "01"))
                    .autoRead(config.getAutoRead() == null ? false : config.getAutoRead())
                    .url(config.getUrl())
                    .recipientList(normalizeRecipientList(config.getRecipientList()))
                    .build();
            return JSON.toJSONString(normalized);
        }
        ArgumentAssert.notBlank(channelConfig, "Alarm channel config cannot be blank.");
        switch (channelType) {
            case DING_TALK:
                DingTalkMessageParamDTO dingTalk = parseChannelConfig(channelConfig, DingTalkMessageParamDTO.class, channelType);
                ArgumentAssert.notBlank(dingTalk.getToken(), "DingTalk alarm channel token cannot be blank.");
                ArgumentAssert.notBlank(dingTalk.getSecret(), "DingTalk alarm channel secret cannot be blank.");
                break;
            case ENTERPRISE_WECHAT:
                WeChatWorkMessageParamDTO weChat = parseChannelConfig(channelConfig, WeChatWorkMessageParamDTO.class, channelType);
                ArgumentAssert.notBlank(weChat.getToken(), "Enterprise WeChat alarm channel token cannot be blank.");
                break;
            case FS:
                FeishuMessageParamDTO feishu = parseChannelConfig(channelConfig, FeishuMessageParamDTO.class, channelType);
                ArgumentAssert.notBlank(feishu.getToken(), "Feishu alarm channel token cannot be blank.");
                ArgumentAssert.notBlank(feishu.getAppId(), "Feishu alarm channel appId cannot be blank.");
                ArgumentAssert.notBlank(feishu.getAppSecret(), "Feishu alarm channel appSecret cannot be blank.");
                break;
            default:
                throw BizException.wrap("Unsupported alarm channel type: " + channelType);
        }
        return channelConfig;
    }

    private <T> T parseChannelConfig(String channelConfig, Class<T> clazz, AlarmChannelTypeEnum channelType) {
        try {
            T config = JSON.parseObject(channelConfig, clazz);
            if (config == null) {
                throw BizException.wrap("Alarm channel config cannot be blank.");
            }
            return config;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw BizException.wrap("Invalid " + channelType.getDesc() + " alarm channel config.");
        }
    }

    private String normalizeRemindMode(String remindMode) {
        if (Arrays.stream(NoticeRemindModeEnum.values()).anyMatch(item -> item.getCode().equals(remindMode))) {
            return remindMode;
        }
        return NoticeRemindModeEnum.EARLY_WARNING.getCode();
    }

    private List<String> normalizeRecipientList(List<String> recipientList) {
        if (recipientList == null) {
            return List.of();
        }
        return recipientList.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .toList();
    }

    /**
     * Construct update parameters.
     *
     * @param updateVO Parameters for updating.
     * @return {@link RuleAlarmChannel} Alarm channel entity.
     */
    private RuleAlarmChannel builderAlarmChannelUpdateVO(RuleAlarmChannelUpdateVO updateVO) {
        return BeanPlusUtil.copyProperties(updateVO, RuleAlarmChannel.class);
    }

}
