<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('iot.link.engine.linkage.triggerAlarm')"
    :maskClosable="false"
    :destroyOnClose="true"
    :keyboard="true"
    :showOkBtn="false"
    :showCancelBtn="false"
    :height="680"
    width="1280px"
    @cancel="handleCancel"
  >
    <div class="alarm-template-modal">
      <a-steps :current="current" size="small" class="template-steps">
        <a-step v-for="item in steps" :key="item.key" :title="item.title" />
      </a-steps>

      <div v-show="current === 0" class="select-step">
        <AlarmListCardList
          type="select"
          @handle-select="handleSelect"
          :selectedValue="currentSelectedAlarmIdentification"
        />
      </div>

      <div v-show="current === 1" class="template-step">
        <div class="side-panel">
          <div class="selected-alarm">
            <div class="panel-label">{{ t('iot.link.engine.linkage.selectedAlarmRule') }}</div>
            <div class="alarm-name">{{ selectedAlarm?.alarmName || '-' }}</div>
            <div class="alarm-meta">
              <span>{{ selectedAlarm?.alarmScene || '-' }}</span>
              <span>{{ currentSelectedAlarmIdentification || '-' }}</span>
            </div>
          </div>

          <div class="recipient-box">
            <div class="panel-title">{{ t('iot.link.engine.linkage.recipients') }}</div>
            <div class="recipient-input">
              <a-select v-model:value="recipientType" class="recipient-type">
                <a-select-option value="PHONE">
                  {{ t('iot.link.engine.linkage.phone') }}
                </a-select-option>
                <a-select-option value="EMPLOYEE">{{
                  t('iot.link.engine.linkage.employee')
                }}</a-select-option>
                <a-select-option value="ALL">
                  {{ t('iot.link.engine.linkage.atAll') }}
                </a-select-option>
              </a-select>
              <a-input
                v-if="recipientType !== 'ALL'"
                v-model:value="recipientInput"
                :placeholder="recipientPlaceholder"
                @press-enter="addRecipient"
              />
              <a-button type="primary" @click="addRecipient">
                {{ t('iot.link.engine.linkage.add') }}
              </a-button>
            </div>
            <div class="recipient-tags">
              <a-tag
                v-for="item in recipients"
                :key="`${item.type}:${item.value}`"
                closable
                @close.prevent="removeRecipient(item)"
              >
                {{ recipientLabel(item) }}
              </a-tag>
              <span v-if="!recipients.length" class="muted">{{
                t('iot.link.engine.linkage.noRecipient')
              }}</span>
            </div>
          </div>

          <div class="channel-list">
            <div class="panel-title">{{ t('iot.link.engine.linkage.channelTemplate') }}</div>
            <button
              v-for="channel in channelOptions"
              :key="channel.channelType"
              type="button"
              :class="['channel-item', activeChannelType === channel.channelType ? 'active' : '']"
              @click="switchChannel(channel.channelType)"
            >
              <span>
                <strong>{{ channelLabel(channel.channelType) }}</strong>
                <em>{{ formatLabel(getTemplate(channel.channelType).format) }}</em>
              </span>
              <a-switch
                size="small"
                :checked="getTemplate(channel.channelType).enabled === true"
                @click.stop
                @change="(checked: boolean) => toggleChannel(channel.channelType, checked)"
              />
            </button>
          </div>
        </div>

        <div class="editor-panel">
          <div class="editor-header">
            <div>
              <div class="panel-title">{{ channelLabel(activeChannelType) }}</div>
              <div class="muted">{{ t('iot.link.engine.linkage.channelTemplateTip') }}</div>
            </div>
            <div class="editor-actions">
              <a-select
                v-model:value="selectedTemplateId"
                size="small"
                class="template-select"
                :placeholder="t('iot.link.engine.linkage.templatePreset')"
                @change="(value) => applyPreset(String(value))"
              >
                <a-select-option
                  v-for="preset in templatePresets"
                  :key="preset.id"
                  :value="preset.id"
                >
                  {{ t(preset.nameKey) }}
                </a-select-option>
              </a-select>
              <a-button :loading="previewLoading" @click="handlePreview">
                {{ t('iot.link.engine.linkage.preview') }}
              </a-button>
            </div>
          </div>

          <div class="template-presets">
            <button
              v-for="preset in templatePresets"
              :key="preset.id"
              type="button"
              :class="['preset-item', selectedTemplateId === preset.id ? 'active' : '']"
              @click="applyPreset(preset.id)"
            >
              <span>{{ t(preset.nameKey) }}</span>
              <em>{{ t(preset.descriptionKey) }}</em>
            </button>
          </div>

          <div v-if="activePresetDocUrl" class="format-note">
            {{ t('iot.link.engine.linkage.officialFormat') }}
            <a :href="activePresetDocUrl" target="_blank" rel="noopener noreferrer">
              {{ t('iot.link.engine.linkage.officialReference') }}
            </a>
          </div>

          <div class="field-group">
            <label>{{ t('iot.link.engine.linkage.templateTitle') }}</label>
            <a-input
              v-model:value="activeTemplate.titleTemplate"
              @focus="activeField = 'titleTemplate'"
            />
          </div>

          <div class="field-group">
            <label>{{ t('iot.link.engine.linkage.templateContent') }}</label>
            <a-textarea
              v-model:value="activeTemplate.contentTemplate"
              :auto-size="{ minRows: 9, maxRows: 14 }"
              @focus="activeField = 'contentTemplate'"
            />
          </div>

          <div class="field-group" v-if="activeChannelType === CHANNEL_TYPE.SITE_MESSAGE">
            <label>{{ t('iot.link.engine.linkage.noticeUrl') }}</label>
            <a-input
              v-model:value="activeTemplate.urlTemplate"
              @focus="activeField = 'urlTemplate'"
            />
          </div>

          <div class="field-inline" v-if="activeChannelType !== CHANNEL_TYPE.SITE_MESSAGE">
            <a-checkbox v-model:checked="activeTemplate.atAll">
              {{ t('iot.link.engine.linkage.robotAtAll') }}
            </a-checkbox>
          </div>

          <div class="preview-panel">
            <div class="panel-title">{{ t('iot.link.engine.linkage.previewResult') }}</div>
            <div v-if="currentPreview" class="preview-content">
              <div class="preview-title">{{ currentPreview.title || '-' }}</div>
              <pre>{{ currentPreview.content || '-' }}</pre>
              <div v-if="currentPreview.url" class="preview-url">{{ currentPreview.url }}</div>
            </div>
            <a-empty v-else :description="t('iot.link.engine.linkage.noPreview')" />
          </div>
        </div>

        <div class="variable-panel">
          <div class="panel-title">{{ t('iot.link.engine.linkage.variables') }}</div>
          <div class="muted variable-tip">{{ t('iot.link.engine.linkage.variableTip') }}</div>
          <a-spin :spinning="variablesLoading">
            <div class="variable-group" v-for="group in variableGroups" :key="group.groupCode">
              <div class="group-title">{{ variableGroupName(group) }}</div>
              <button
                v-for="variable in group.variables"
                :key="variable.key"
                type="button"
                class="variable-item"
                :title="variableDescription(group.groupCode, variable)"
                @click="insertVariable(variable.placeholder)"
              >
                <span>{{ variableLabel(group.groupCode, variable) }}</span>
                <code>{{ variable.placeholder }}</code>
              </button>
            </div>
          </a-spin>
        </div>
      </div>
    </div>

    <template #footer>
      <a-button @click="handleCancel"> {{ t('common.cancelText') }} </a-button>
      <a-button v-show="current > 0" @click="prev">
        {{ t('iot.link.engine.linkage.prev') }}
      </a-button>
      <a-button v-show="current < steps.length - 1" type="primary" @click="next">
        {{ t('iot.link.engine.linkage.next') }}
      </a-button>
      <a-button v-show="current === steps.length - 1" type="primary" @click="handleSubmit">
        {{ t('common.okText') }}
      </a-button>
    </template>
  </BasicModal>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { i18n } from '/@/locales/setupI18n';
  import AlarmListCardList from '../../../../../../../components/iot/rule/alarm/AlarmListCardList.vue';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { getRuleAlarmDetails, page as pageAlarm } from '/@/api/iot/rule/alarm/alarm';
  import {
    getNotificationVariables,
    previewNotification,
  } from '/@/api/iot/rule/engine/linkage/notification';
  import {
    NOTIFICATION_CHANNEL_TYPE as CHANNEL_TYPE,
    applyNotificationTemplatePreset,
    createNotificationChannelTemplate,
    getDefaultNotificationTemplatePreset,
    getNotificationTemplatePreset,
    getNotificationTemplatePresets,
  } from './notificationTemplates';
  import { FALLBACK_NOTIFICATION_VARIABLE_GROUPS } from './notificationTemplates/variables';
  import {
    getNotificationValidationErrorKey,
    getNotificationVariableLocaleKeys,
  } from './notificationTemplates/notificationConfig.mjs';
  import type {
    RuleAlarmChannelTemplate,
    RuleAlarmRecipient,
    RuleAlarmRenderedNotification,
    RuleNotificationVariable,
    RuleNotificationVariableGroup,
  } from '/@/api/iot/rule/engine/linkage/notification';

  const DEFAULT_CHANNELS = [
    CHANNEL_TYPE.DING_TALK,
    CHANNEL_TYPE.ENTERPRISE_WECHAT,
    CHANNEL_TYPE.FEISHU,
    CHANNEL_TYPE.SITE_MESSAGE,
  ];

  const emit = defineEmits(['saveTriggerAlarm']);
  const { t } = useI18n();
  const { createMessage } = useMessage();

  function resolvePresetMessage(key: string) {
    return key ? i18n.global.tm(key) : '';
  }

  const currentType = ref<ActionEnum>(ActionEnum.ADD);
  const current = ref(0);
  const actionIndex = ref(0);
  const actionItem = ref<Recordable>({});
  const currentSelectedAlarmIdentification = ref('');
  const selectedAlarm = ref<Recordable>({});
  const recipients = ref<RuleAlarmRecipient[]>([]);
  const recipientType = ref<RuleAlarmRecipient['type']>('PHONE');
  const recipientInput = ref('');
  const channelTemplates = ref<RuleAlarmChannelTemplate[]>([]);
  const activeChannelType = ref<number>(CHANNEL_TYPE.DING_TALK);
  const selectedTemplateId = ref('');
  const activeField = ref<'titleTemplate' | 'contentTemplate' | 'urlTemplate'>('contentTemplate');
  const variableGroups = ref<RuleNotificationVariableGroup[]>([]);
  const variablesLoading = ref(false);
  const previewLoading = ref(false);
  const previewList = ref<RuleAlarmRenderedNotification[]>([]);

  const steps = computed(() => [
    { key: '0', title: t('iot.link.engine.linkage.selectAlarmRule') },
    { key: '1', title: t('iot.link.engine.linkage.editNotification') },
  ]);

  const recipientPlaceholder = computed(() =>
    recipientType.value === 'EMPLOYEE'
      ? t('iot.link.engine.linkage.employeePlaceholder')
      : t('iot.link.engine.linkage.phonePlaceholder'),
  );

  const channelOptions = computed(() => {
    const detailChannels =
      selectedAlarm.value?.ruleAlarmChannelDetailsResultVOList ||
      selectedAlarm.value?.alarmChannelResultVOS ||
      selectedAlarm.value?.ruleAlarmChannelDetailsList ||
      [];
    const fromDetails = detailChannels
      .map((item) => Number(item?.channelType))
      .filter((item) => !Number.isNaN(item));
    const fromTemplates = channelTemplates.value.map((item) => Number(item.channelType));
    const source = fromDetails.length
      ? fromDetails
      : fromTemplates.length
      ? fromTemplates
      : DEFAULT_CHANNELS;
    const channelTypes = Array.from(new Set(source));
    return channelTypes.map((channelType) => ({ channelType }));
  });

  const activeTemplate = computed(() => getTemplate(activeChannelType.value));

  const templatePresets = computed(() => getNotificationTemplatePresets(activeChannelType.value));

  const activePresetDocUrl = computed(() => {
    const preset =
      getNotificationTemplatePreset(selectedTemplateId.value) ||
      getNotificationTemplatePreset(activeTemplate.value.templateId);
    return preset?.docUrl || '';
  });

  const currentPreview = computed(() =>
    previewList.value.find((item) => Number(item.channelType) === Number(activeChannelType.value)),
  );

  const [registerModal, { closeModal }] = useModalInner(async (data) => {
    resetHandle(false);
    actionIndex.value = data.actionIndex;
    actionItem.value = data.actionItem || {};
    currentType.value = data?.type;
    if (currentType.value === ActionEnum.EDIT) {
      hydrateFromAction(actionItem.value?.actionContent || {});
    }
  });

  function hydrateFromAction(actionContent: Recordable) {
    currentSelectedAlarmIdentification.value = actionContent?.alarmIdentification || '';
    recipients.value = normalizeRecipients(actionContent);
    channelTemplates.value = normalizeTemplates(actionContent);
    if (channelTemplates.value.length) {
      activeChannelType.value = Number(channelTemplates.value[0].channelType);
    }
    syncSelectedTemplateId();
    if (currentSelectedAlarmIdentification.value) {
      selectedAlarm.value = {
        alarmIdentification: currentSelectedAlarmIdentification.value,
        alarmName: actionContent?.alarmName,
      };
    }
  }

  function normalizeRecipients(actionContent: Recordable): RuleAlarmRecipient[] {
    if (Array.isArray(actionContent?.recipients)) {
      return actionContent.recipients;
    }
    return String(actionContent?.atPhone || '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
      .map((phone) => ({ type: 'PHONE', value: phone, label: phone }));
  }

  function normalizeTemplates(actionContent: Recordable): RuleAlarmChannelTemplate[] {
    if (Array.isArray(actionContent?.channelTemplates) && actionContent.channelTemplates.length) {
      return actionContent.channelTemplates.map((item) => ({
        ...createNotificationChannelTemplate(
          Number(item.channelType),
          resolvePresetMessage,
          item.templateId,
        ),
        ...item,
        channelType: Number(item.channelType),
        enabled: item.enabled !== false,
      }));
    }
    const legacyContent = actionContent?.contentData || '';
    return DEFAULT_CHANNELS.map((channelType) => {
      const template = createNotificationChannelTemplate(channelType, resolvePresetMessage);
      return {
        ...template,
        contentTemplate: legacyContent || template.contentTemplate,
        enabled: channelType === CHANNEL_TYPE.DING_TALK && !!legacyContent,
      };
    });
  }

  async function next() {
    if (!currentSelectedAlarmIdentification.value) {
      createMessage.error(t('iot.link.engine.linkage.selectAlarmRuleTip'));
      return;
    }
    current.value++;
    await loadSelectedAlarmDetails();
    await loadVariables();
    ensureChannelTemplates();
    await handlePreview(true);
  }

  function prev() {
    current.value--;
  }

  async function loadSelectedAlarmDetails() {
    let id = selectedAlarm.value?.id;
    if (!id && currentSelectedAlarmIdentification.value) {
      const pageResult = await pageAlarm({
        model: { alarmIdentification: currentSelectedAlarmIdentification.value },
        size: 1,
        current: 1,
        extra: {},
      });
      id = pageResult?.records?.[0]?.id;
    }
    if (!id) return;
    try {
      selectedAlarm.value = await getRuleAlarmDetails(String(id));
    } catch {
      // 列表记录已经足够完成配置，详情失败时不阻断用户继续编辑。
    }
  }

  async function loadVariables() {
    if (variableGroups.value.length) return;
    variablesLoading.value = true;
    try {
      const remoteGroups = await getNotificationVariables();
      variableGroups.value = remoteGroups?.length
        ? remoteGroups
        : FALLBACK_NOTIFICATION_VARIABLE_GROUPS;
    } catch {
      variableGroups.value = FALLBACK_NOTIFICATION_VARIABLE_GROUPS;
    } finally {
      variablesLoading.value = false;
    }
  }

  function variableGroupName(group: RuleNotificationVariableGroup) {
    const localeKey = getNotificationVariableLocaleKeys(group.groupCode)?.groupName;
    return translateVariableMetadata(localeKey, group.groupName);
  }

  function variableLabel(groupCode: string, variable: RuleNotificationVariable) {
    const localeKey = getNotificationVariableLocaleKeys(groupCode, variable.key)?.label;
    return translateVariableMetadata(localeKey, variable.label);
  }

  function variableDescription(groupCode: string, variable: RuleNotificationVariable) {
    const localeKey = getNotificationVariableLocaleKeys(groupCode, variable.key)?.description;
    return translateVariableMetadata(localeKey, variable.description);
  }

  function translateVariableMetadata(localeKey?: string, fallback = '') {
    if (!localeKey) return fallback;
    const fullKey = `iot.link.engine.linkage.${localeKey}`;
    const translated = t(fullKey);
    return translated === fullKey ? fallback : translated;
  }

  function ensureChannelTemplates() {
    const availableChannelTypes = channelOptions.value.map((item) => Number(item.channelType));
    if (availableChannelTypes.length) {
      channelTemplates.value = channelTemplates.value.filter((item) =>
        availableChannelTypes.includes(Number(item.channelType)),
      );
    }
    const existing = new Set(channelTemplates.value.map((item) => Number(item.channelType)));
    channelOptions.value.forEach((channel) => {
      if (!existing.has(channel.channelType)) {
        channelTemplates.value.push(defaultTemplate(channel.channelType));
      }
    });
    if (!channelOptions.value.some((item) => item.channelType === activeChannelType.value)) {
      activeChannelType.value = channelOptions.value[0]?.channelType || CHANNEL_TYPE.DING_TALK;
    }
    syncSelectedTemplateId();
  }

  function handleSelect(alarmIdentification: string, record: Recordable) {
    currentSelectedAlarmIdentification.value = alarmIdentification;
    selectedAlarm.value = record || { alarmIdentification };
  }

  function addRecipient() {
    const value = recipientType.value === 'ALL' ? 'all' : recipientInput.value.trim();
    if (!value) {
      createMessage.error(t('iot.link.engine.linkage.recipientRequired'));
      return;
    }
    if (recipientType.value === 'PHONE' && !/^1[3-9]\d{9}$/.test(value)) {
      createMessage.error(t('iot.link.engine.linkage.phoneInvalid'));
      return;
    }
    const nextRecipient: RuleAlarmRecipient = {
      type: recipientType.value,
      value,
      label: recipientType.value === 'ALL' ? t('iot.link.engine.linkage.atAll') : value,
    };
    const key = `${nextRecipient.type}:${nextRecipient.value}`;
    if (!recipients.value.some((item) => `${item.type}:${item.value}` === key)) {
      recipients.value.push(nextRecipient);
    }
    recipientInput.value = '';
  }

  function removeRecipient(item: RuleAlarmRecipient) {
    recipients.value = recipients.value.filter(
      (recipient) => `${recipient.type}:${recipient.value}` !== `${item.type}:${item.value}`,
    );
  }

  function recipientLabel(item: RuleAlarmRecipient) {
    const prefix =
      item.type === 'EMPLOYEE'
        ? t('iot.link.engine.linkage.employee')
        : item.type === 'ALL'
        ? t('iot.link.engine.linkage.atAll')
        : t('iot.link.engine.linkage.phone');
    return item.type === 'ALL' ? prefix : `${prefix}: ${item.label || item.value}`;
  }

  function switchChannel(channelType: number) {
    activeChannelType.value = channelType;
    activeField.value = 'contentTemplate';
    if (channelType === CHANNEL_TYPE.SITE_MESSAGE) {
      recipientType.value = 'EMPLOYEE';
    }
    getTemplate(channelType);
    syncSelectedTemplateId();
  }

  function toggleChannel(channelType: number, checked: boolean) {
    getTemplate(channelType).enabled = checked;
    if (checked && channelType === CHANNEL_TYPE.SITE_MESSAGE) {
      recipientType.value = 'EMPLOYEE';
    }
  }

  function getTemplate(channelType: number): RuleAlarmChannelTemplate {
    let template = channelTemplates.value.find(
      (item) => Number(item.channelType) === Number(channelType),
    );
    if (!template) {
      template = createNotificationChannelTemplate(channelType, resolvePresetMessage);
      channelTemplates.value.push(template);
    }
    return template;
  }

  function defaultTemplate(channelType: number): RuleAlarmChannelTemplate {
    return createNotificationChannelTemplate(channelType, resolvePresetMessage);
  }

  function syncSelectedTemplateId() {
    const template = getTemplate(activeChannelType.value);
    selectedTemplateId.value =
      template.templateId || getDefaultNotificationTemplatePreset(activeChannelType.value).id;
  }

  function applyPreset(presetId: string) {
    const preset = getNotificationTemplatePreset(presetId);
    if (!preset) return;
    const template = applyNotificationTemplatePreset(
      getTemplate(activeChannelType.value),
      preset,
      resolvePresetMessage,
    );
    const index = channelTemplates.value.findIndex(
      (item) => Number(item.channelType) === Number(activeChannelType.value),
    );
    if (index >= 0) {
      channelTemplates.value.splice(index, 1, template);
    } else {
      channelTemplates.value.push(template);
    }
    selectedTemplateId.value = preset.id;
  }

  function insertVariable(placeholder: string) {
    const template = activeTemplate.value;
    const field = activeField.value;
    template[field] = `${template[field] || ''}${placeholder}`;
  }

  async function handlePreview(silent = false) {
    previewLoading.value = true;
    try {
      const result = await previewNotification({
        alarmIdentification: currentSelectedAlarmIdentification.value,
        recipients: recipients.value,
        channelTemplates: channelTemplates.value.filter((item) => item.enabled === true),
      });
      previewList.value = result?.channels || [];
    } catch {
      previewList.value = [];
      if (!silent) {
        createMessage.warning(t('iot.link.engine.linkage.previewUnavailable'));
      }
    } finally {
      previewLoading.value = false;
    }
  }

  function validateBeforeSubmit() {
    const errorKey = getNotificationValidationErrorKey(channelTemplates.value, recipients.value);
    if (errorKey) {
      createMessage.error(t(`iot.link.engine.linkage.${errorKey}`));
      return false;
    }
    return true;
  }

  function handleSubmit() {
    if (!currentSelectedAlarmIdentification.value) {
      createMessage.error(t('iot.link.engine.linkage.selectAlarmRuleTip'));
      return;
    }
    if (!validateBeforeSubmit()) return;
    const phoneRecipients = recipients.value
      .filter((item) => item.type === 'PHONE' || item.type === 'ALL')
      .map((item) => item.value)
      .join(',');
    const availableChannelTypes = channelOptions.value.map((item) => Number(item.channelType));
    const payloadTemplates = channelTemplates.value
      .filter((item) => availableChannelTypes.includes(Number(item.channelType)))
      .map((item) => ({
        ...item,
        channelType: Number(item.channelType),
        enabled: item.enabled === true,
      }));
    const enabledTemplates = payloadTemplates.filter((item) => item.enabled === true);
    emit('saveTriggerAlarm', {
      actionIndex: actionIndex.value,
      version: 2,
      alarmIdentification: currentSelectedAlarmIdentification.value,
      alarmName: selectedAlarm.value?.alarmName,
      recipients: recipients.value,
      atPhone: phoneRecipients,
      contentData: enabledTemplates[0]?.contentTemplate || '',
      channelTemplates: payloadTemplates,
    });
    closeModal();
    resetHandle();
  }

  function handleCancel() {
    closeModal();
    resetHandle();
  }

  function resetHandle(resetModal = true) {
    current.value = 0;
    currentType.value = ActionEnum.ADD;
    currentSelectedAlarmIdentification.value = '';
    selectedAlarm.value = {};
    recipients.value = [];
    recipientType.value = 'PHONE';
    recipientInput.value = '';
    channelTemplates.value = [];
    activeChannelType.value = CHANNEL_TYPE.DING_TALK;
    selectedTemplateId.value = '';
    activeField.value = 'contentTemplate';
    previewList.value = [];
    if (resetModal) {
      actionIndex.value = 0;
      actionItem.value = {};
    }
  }

  function channelLabel(channelType: number) {
    const labels: Record<number, string> = {
      [CHANNEL_TYPE.DING_TALK]: t('iot.link.engine.linkage.dingTalk'),
      [CHANNEL_TYPE.ENTERPRISE_WECHAT]: t('iot.link.engine.linkage.enterpriseWechat'),
      [CHANNEL_TYPE.FEISHU]: t('iot.link.engine.linkage.feishu'),
      [CHANNEL_TYPE.SITE_MESSAGE]: t('iot.link.engine.linkage.siteMessage'),
    };
    return labels[channelType] || t('iot.link.engine.linkage.unknownChannel');
  }

  function formatLabel(format?: string) {
    return format || '-';
  }
</script>

<style scoped lang="less">
  .alarm-template-modal {
    height: 680px;
    display: flex;
    flex-direction: column;
  }

  .template-steps {
    flex: 0 0 auto;
    padding: 2px 4px 14px;
  }

  .select-step {
    flex: 1;
    min-height: 0;
    overflow: auto;
  }

  .template-step {
    flex: 1;
    min-height: 0;
    display: grid;
    grid-template-columns: 300px minmax(420px, 1fr) 300px;
    gap: 16px;
    overflow: hidden;
  }

  .side-panel,
  .editor-panel,
  .variable-panel {
    min-height: 0;
    overflow: auto;
    border: 1px solid @border-color-base;
    border-radius: 8px;
    background: @component-background;
  }

  .side-panel,
  .variable-panel {
    padding: 14px;
  }

  .editor-panel {
    padding: 16px;
  }

  .panel-title {
    color: @text-color;
    font-size: 15px;
    font-weight: 600;
  }

  .panel-label,
  .muted {
    color: @text-color-secondary;
    font-size: 12px;
  }

  .selected-alarm {
    padding: 12px;
    border: 1px solid fade(@primary-color, 22%);
    border-radius: 8px;
    background: fade(@primary-color, 6%);
  }

  .alarm-name {
    margin-top: 6px;
    color: @heading-color;
    font-size: 17px;
    font-weight: 700;
    line-height: 1.35;
  }

  .alarm-meta {
    display: flex;
    flex-direction: column;
    gap: 2px;
    margin-top: 8px;
    color: @text-color-secondary;
    font-size: 12px;
  }

  .recipient-box,
  .channel-list {
    margin-top: 16px;
  }

  .recipient-input {
    display: grid;
    grid-template-columns: 92px minmax(0, 1fr) 58px;
    gap: 8px;
    margin-top: 10px;
  }

  .recipient-type {
    width: 92px;
  }

  .recipient-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    min-height: 32px;
    margin-top: 10px;
  }

  .channel-item {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 10px;
    padding: 10px 12px;
    border: 1px solid @border-color-base;
    border-radius: 8px;
    background: @component-background;
    color: @text-color;
    cursor: pointer;
    text-align: left;
    transition: border-color 0.2s ease, background-color 0.2s ease;

    strong,
    em {
      display: block;
      font-style: normal;
    }

    em {
      margin-top: 3px;
      color: @text-color-secondary;
      font-size: 12px;
    }

    &.active {
      border-color: @primary-color;
      background: fade(@primary-color, 8%);
    }
  }

  .editor-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 16px;
  }

  .editor-actions {
    flex: 0 0 auto;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .template-select {
    width: 142px;
  }

  .template-presets {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
    margin-bottom: 12px;
  }

  .preset-item {
    min-height: 56px;
    padding: 9px 10px;
    border: 1px solid @border-color-base;
    border-radius: 8px;
    background: @component-background;
    color: @text-color;
    cursor: pointer;
    text-align: left;
    transition: border-color 0.2s ease, background-color 0.2s ease;

    span,
    em {
      display: block;
      font-style: normal;
      line-height: 1.35;
    }

    span {
      font-weight: 600;
    }

    em {
      margin-top: 4px;
      color: @text-color-secondary;
      font-size: 12px;
    }

    &.active,
    &:hover {
      border-color: @primary-color;
      background: fade(@primary-color, 6%);
    }
  }

  .format-note {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 14px;
    color: @text-color-secondary;
    font-size: 12px;
  }

  .field-group {
    margin-bottom: 14px;

    label {
      display: block;
      margin-bottom: 6px;
      color: @text-color;
      font-weight: 600;
    }
  }

  .field-inline {
    margin-bottom: 14px;
  }

  .preview-panel {
    margin-top: 8px;
    padding-top: 14px;
    border-top: 1px dashed @border-color-base;
  }

  .preview-content {
    margin-top: 10px;
    padding: 12px;
    border-radius: 8px;
    background: @background-color-light;
  }

  .preview-title {
    color: @heading-color;
    font-weight: 700;
  }

  .preview-content pre {
    margin: 10px 0 0;
    white-space: pre-wrap;
    word-break: break-word;
    color: @text-color;
    font-family: inherit;
  }

  .preview-url {
    margin-top: 10px;
    color: @primary-color;
    word-break: break-all;
  }

  .variable-tip {
    margin-top: 4px;
    margin-bottom: 12px;
  }

  .variable-group + .variable-group {
    margin-top: 14px;
  }

  .group-title {
    margin-bottom: 8px;
    color: @text-color-secondary;
    font-weight: 600;
  }

  .variable-item {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-bottom: 8px;
    padding: 8px 10px;
    border: 1px solid @border-color-base;
    border-radius: 7px;
    background: @component-background;
    color: @text-color;
    cursor: pointer;
    text-align: left;

    &:hover {
      border-color: @primary-color;
    }

    code {
      color: @text-color-secondary;
      font-size: 12px;
      word-break: break-all;
    }
  }
</style>
