<template>
  <PageWrapper dense contentFullHeight>
    <div class="alarm-detail">
      <ACard class="detail-card summary-card" :bordered="false" :loading="detailLoading">
        <div class="summary-main">
          <div class="summary-icon">
            <AlarmRecordSvg />
          </div>
          <div class="summary-title">
            <div class="summary-title__line">
              <ATag :color="statusMeta.color">{{ statusMeta.label }}</ATag>
              <span>{{ t('iot.link.engine.alarmRecord.recordId') }}</span>
            </div>
            <div class="summary-id">{{ formatValue(alarmRecordDetail.id) }}</div>
            <div class="summary-sub">
              <span>{{ t('iot.link.engine.alarmRecord.alarmIdentification') }}:</span>
              <span>{{ formatValue(alarmRecordDetail.alarmIdentification) }}</span>
            </div>
          </div>
          <div class="summary-action">
            <AButton
              type="primary"
              :disabled="alarmRecordDetail.handledStatus === 2"
              @click="handleEdit"
            >
              <template #icon><EditOutlined /></template>
              {{ actionText }}
            </AButton>
          </div>
        </div>
        <div class="summary-grid">
          <div class="summary-item">
            <span>{{ t('iot.link.engine.alarmRecord.appId') }}</span>
            <strong>{{ formatValue(alarmRecordDetail.appId) }}</strong>
          </div>
          <div class="summary-item">
            <span>{{ t('iot.link.engine.alarmRecord.occurredTime') }}</span>
            <strong>{{ formatValue(alarmRecordDetail.occurredTime) }}</strong>
          </div>
          <div class="summary-item">
            <span>{{ t('iot.link.engine.alarmRecord.handledTime') }}</span>
            <strong>{{ formatValue(alarmRecordDetail.handledTime) }}</strong>
          </div>
          <div class="summary-item">
            <span>{{ t('iot.link.engine.alarmRecord.resolvedTime') }}</span>
            <strong>{{ formatValue(alarmRecordDetail.resolvedTime) }}</strong>
          </div>
        </div>
      </ACard>

      <ACard class="detail-card" :bordered="false">
        <ATabs v-model:activeKey="activeKey">
          <ATabPane key="record" :tab="t('iot.link.engine.alarmRecord.alarmRecord')">
            <div class="info-grid">
              <div class="info-tile info-tile--wide">
                <span>{{ t('iot.link.engine.alarmRecord.alarmRule') }}</span>
                <strong>
                  {{ formatValue(ruleInfo?.alarmName || alarmRecordDetail.alarmName) }}
                </strong>
              </div>
              <div class="info-tile">
                <span>{{ t('iot.link.engine.alarmRecord.alarmLevel') }}</span>
                <strong>{{ alarmLevelText }}</strong>
              </div>
              <div class="info-tile info-tile--wide">
                <span>{{ t('iot.link.engine.alarmRecord.notificationChannels') }}</span>
                <strong>{{ channelSummary }}</strong>
              </div>
              <div class="info-tile">
                <span>{{ t('iot.link.engine.alarmRecord.handledStatus') }}</span>
                <strong>
                  <ATag :color="statusMeta.color">{{ statusMeta.label }}</ATag>
                </strong>
              </div>
              <div class="info-tile">
                <span>{{ t('iot.link.engine.alarmRecord.remark') }}</span>
                <strong>{{ formatValue(alarmRecordDetail.remark) }}</strong>
              </div>
              <div class="info-tile info-tile--wide">
                <span>{{ t('iot.link.engine.alarmRecord.handlingNotes') }}</span>
                <strong>{{ formatValue(alarmRecordDetail.handlingNotes) }}</strong>
              </div>
              <div class="info-tile info-tile--wide">
                <span>{{ t('iot.link.engine.alarmRecord.resolutionNotes') }}</span>
                <strong>{{ formatValue(alarmRecordDetail.resolutionNotes) }}</strong>
              </div>
            </div>
            <div class="timeline-block">
              <div class="section-title">{{ t('iot.link.engine.alarmRecord.timeline') }}</div>
              <div class="timeline">
                <div class="timeline-item active">
                  <span></span>
                  <div>
                    <strong>{{ t('iot.link.engine.alarmRecord.occurredTime') }}</strong>
                    <p>{{ formatValue(alarmRecordDetail.occurredTime) }}</p>
                  </div>
                </div>
                <div class="timeline-item" :class="{ active: !!alarmRecordDetail.handledTime }">
                  <span></span>
                  <div>
                    <strong>{{ t('iot.link.engine.alarmRecord.handledTime') }}</strong>
                    <p>{{ formatValue(alarmRecordDetail.handledTime) }}</p>
                  </div>
                </div>
                <div class="timeline-item" :class="{ active: !!alarmRecordDetail.resolvedTime }">
                  <span></span>
                  <div>
                    <strong>{{ t('iot.link.engine.alarmRecord.resolvedTime') }}</strong>
                    <p>{{ formatValue(alarmRecordDetail.resolvedTime) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </ATabPane>

          <ATabPane key="rule" :tab="t('iot.link.engine.alarmRecord.ruleInfo')">
            <AEmpty v-if="!ruleInfo" :description="t('iot.link.engine.alarmRecord.noRuleInfo')" />
            <div v-else class="info-grid">
              <div class="info-tile info-tile--wide">
                <span>{{ t('iot.link.engine.alarmRecord.alarmName') }}</span>
                <strong>{{ formatValue(ruleInfo.alarmName) }}</strong>
              </div>
              <div class="info-tile">
                <span>{{ t('iot.link.engine.alarmRecord.appId') }}</span>
                <strong>{{ formatValue(ruleInfo.appId) }}</strong>
              </div>
              <div class="info-tile">
                <span>{{ t('iot.link.engine.alarmRecord.alarmIdentification') }}</span>
                <strong>{{ formatValue(ruleInfo.alarmIdentification) }}</strong>
              </div>
              <div class="info-tile">
                <span>{{ t('iot.link.engine.alarmRecord.alarmScene') }}</span>
                <strong>{{ dictLabel('RULE_ALARM_SCENE', ruleInfo.alarmScene) }}</strong>
              </div>
              <div class="info-tile">
                <span>{{ t('iot.link.engine.alarmRecord.alarmLevel') }}</span>
                <strong>{{ dictLabel('RULE_ALARM_LEVEL', ruleInfo.level) }}</strong>
              </div>
              <div class="info-tile">
                <span>{{ t('iot.link.engine.alarmRecord.status') }}</span>
                <strong>{{ dictLabel('RULE_ALARM_STATUS', ruleInfo.status) }}</strong>
              </div>
              <div class="info-tile">
                <span>{{ t('iot.link.engine.alarmRecord.notificationChannels') }}</span>
                <strong>{{ channelSummary }}</strong>
              </div>
              <div class="info-tile info-tile--wide">
                <span>{{ t('iot.link.engine.alarmRecord.remark') }}</span>
                <strong>{{ formatValue(ruleInfo.remark) }}</strong>
              </div>
            </div>
          </ATabPane>

          <ATabPane key="channel" :tab="t('iot.link.engine.alarmRecord.channelInfo')">
            <AEmpty
              v-if="!channelList.length"
              :description="t('iot.link.engine.alarmRecord.noChannelInfo')"
            />
            <ARow v-else :gutter="[16, 16]">
              <ACol v-for="item in channelList" :key="item.id" :xs="24" :md="12" :xl="8">
                <div class="channel-card">
                  <div class="channel-card__header">
                    <strong>{{ formatValue(item.channelName) }}</strong>
                    <ATag :color="Number(item.status) === 1 ? 'success' : 'default'">
                      {{ dictLabel('RULE_ALARM_CHANNEL_STATUS', item.status) }}
                    </ATag>
                  </div>
                  <div class="channel-card__row">
                    <span>{{ t('iot.link.engine.channel.channelType') }}</span>
                    <b>{{ resolveAlarmChannelTypeLabel(item.channelType) }}</b>
                  </div>
                  <div class="channel-card__row">
                    <span>{{ t('iot.link.engine.channel.createdTime') }}</span>
                    <b>{{ formatValue(item.createdTime) }}</b>
                  </div>
                  <div class="channel-card__row">
                    <span>{{ t('iot.link.engine.channel.remark') }}</span>
                    <b>{{ formatValue(item.remark) }}</b>
                  </div>
                </div>
              </ACol>
            </ARow>
          </ATabPane>

          <ATabPane key="content" :tab="t('iot.link.engine.alarmRecord.contentInfo')">
            <div class="content-toolbar">
              <span>{{ t('iot.link.engine.alarmRecord.contentData') }}</span>
              <AButton size="small" preIcon="ant-design:copy-outlined" @click="copyContent">
                {{ t('iot.link.engine.alarmRecord.copyContent') }}
              </AButton>
            </div>
            <pre v-if="contentText" class="content-box">{{ contentText }}</pre>
            <AEmpty v-else :description="t('iot.link.engine.alarmRecord.noContent')" />
          </ATabPane>
        </ATabs>
      </ACard>
    </div>
    <EditModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import {
    Button as AButton,
    Card as ACard,
    Col as ACol,
    Empty as AEmpty,
    Row as ARow,
    Tabs as ATabs,
    Tag as ATag,
  } from 'ant-design-vue';
  import { EditOutlined } from '@ant-design/icons-vue';
  import { PageWrapper } from '/@/components/Page';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import { detail } from '/@/api/iot/rule/alarm/record';
  import type { AlarmRecordResultVO } from '/@/api/iot/rule/alarm/model/recordModel';
  import { AlarmRecordSvg } from '/@/components/iot/svg';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common.tsx';
  import EditModal from './Edit.vue';
  import { useModal } from '/@/components/Modal';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { resolveAlarmChannelTypeLabel } from '../channel/channel.data';

  defineOptions({ name: '告警记录详情' });

  const ATabPane = ATabs.TabPane;

  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const route = useRoute();
  const activeKey = ref('record');
  const detailLoading = ref(false);
  const alarmRecordDetail = ref<AlarmRecordResultVO>({});
  const [registerModal, { openModal }] = useModal();

  const recordId = computed(() => String(route.params.id || ''));
  const ruleInfo = computed(() => alarmRecordDetail.value.ruleAlarmDetailsResultVO);
  const channelList = computed(() => ruleInfo.value?.ruleAlarmChannelDetailsResultVOList || []);
  const channelSummary = computed(() => {
    const names = channelList.value
      .map((item) => item.channelName || resolveAlarmChannelTypeLabel(item.channelType))
      .filter(Boolean);
    if (names.length) return names.join('、');

    const configuredCount = String(ruleInfo.value?.alarmChannelIds || '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean).length;
    if (configuredCount > 0) {
      return `${configuredCount}${t('iot.link.engine.alarmRecord.channelUnit')}`;
    }
    return '-';
  });
  const alarmLevelText = computed(() => dictLabel('RULE_ALARM_LEVEL', ruleInfo.value?.level));
  const statusMeta = computed(() => {
    const status = Number(alarmRecordDetail.value.handledStatus);
    if (status === 2) {
      return {
        color: 'success',
        label: dictLabel('RULE_ALARM_RECORD_HANDLED_STATUE', status),
      };
    }
    if (status === 1) {
      return {
        color: 'warning',
        label: dictLabel('RULE_ALARM_RECORD_HANDLED_STATUE', status),
      };
    }
    return {
      color: 'error',
      label: dictLabel('RULE_ALARM_RECORD_HANDLED_STATUE', status),
    };
  });
  const actionText = computed(() => {
    const status = Number(alarmRecordDetail.value.handledStatus);
    if (status === 0) return t('common.title.handle');
    if (status === 1) return t('common.title.goResolved');
    return t('common.title.resolved');
  });
  const contentText = computed(() => {
    const value = alarmRecordDetail.value.contentData;
    if (!value) return '';
    const text = String(value).trim();
    if (!text) return '';
    try {
      return JSON.stringify(JSON.parse(text), null, 2);
    } catch {
      return text;
    }
  });

  function formatValue(value?: string | number | null) {
    return value === undefined || value === null || value === '' ? '-' : String(value);
  }

  function dictLabel(dictType: string, value?: string | number | null) {
    return getDictLabel(dictType, formatValue(value), '-');
  }

  async function load() {
    if (!recordId.value) return;
    detailLoading.value = true;
    try {
      alarmRecordDetail.value = (await detail(recordId.value)) || {};
    } finally {
      detailLoading.value = false;
    }
  }

  function handleEdit(e: Event) {
    e?.stopPropagation();
    if (alarmRecordDetail.value.handledStatus === 2) return;
    openModal(true, {
      record: alarmRecordDetail.value,
      type: ActionEnum.EDIT,
    });
  }

  function handleSuccess() {
    load();
  }

  function copyContent() {
    handleCopyTextV2(contentText.value || '');
  }

  onMounted(load);
</script>

<style scoped lang="less">
  .alarm-detail {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .detail-card {
    border-radius: 8px;
  }

  .summary-main {
    display: flex;
    align-items: center;
    gap: 18px;
  }

  .summary-icon {
    width: 86px;
    height: 86px;
    border-radius: 8px;
    background: #f5f8ff;
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 0 0 auto;

    svg {
      width: 74px;
      height: 74px;
    }
  }

  .summary-title {
    min-width: 0;
    flex: 1;

    &__line {
      display: flex;
      align-items: center;
      gap: 8px;
      color: @text-color-secondary;
      font-size: 13px;
    }
  }

  .summary-id {
    margin-top: 8px;
    font-size: 22px;
    line-height: 28px;
    font-weight: 700;
    color: @text-color-base;
    word-break: break-all;
  }

  .summary-sub {
    margin-top: 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    color: @text-color-secondary;
  }

  .summary-action {
    flex: 0 0 auto;
  }

  .summary-grid {
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px dashed @border-color-base;
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
  }

  .summary-item {
    min-width: 0;
    padding: 10px 12px;
    border-radius: 8px;
    background: @background-color-light;

    span {
      display: block;
      color: @text-color-secondary;
      font-size: 12px;
    }

    strong {
      display: block;
      margin-top: 6px;
      font-size: 14px;
      color: @text-color-base;
      word-break: break-all;
    }
  }

  .timeline-block {
    margin-top: 18px;
  }

  .section-title {
    margin-bottom: 12px;
    font-size: 15px;
    font-weight: 600;
    color: @text-color-base;
  }

  .info-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .info-tile {
    min-width: 0;
    padding: 14px;
    border-radius: 8px;
    border: 1px solid @border-color-base;
    background: @component-background;

    &--wide {
      grid-column: span 2;
    }

    span {
      display: block;
      color: @text-color-secondary;
      font-size: 12px;
      line-height: 18px;
    }

    strong {
      display: block;
      margin-top: 8px;
      color: @text-color-base;
      font-size: 14px;
      font-weight: 600;
      line-height: 20px;
      word-break: break-word;
    }
  }

  .timeline {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .timeline-item {
    display: flex;
    gap: 10px;
    padding: 12px;
    border-radius: 8px;
    background: @background-color-light;
    color: @text-color-secondary;

    > span {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      margin-top: 5px;
      background: @border-color-base;
      flex: 0 0 auto;
    }

    &.active > span {
      background: @primary-color;
    }

    strong {
      color: @text-color-base;
    }

    p {
      margin: 4px 0 0;
    }
  }

  .channel-card {
    height: 100%;
    border-radius: 8px;
    border: 1px solid @border-color-base;
    padding: 14px;
    background: #fff;

    &__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      margin-bottom: 12px;

      strong {
        min-width: 0;
        font-size: 15px;
        word-break: break-all;
      }
    }

    &__row {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      font-size: 13px;

      & + .channel-card__row {
        margin-top: 8px;
      }

      span {
        color: @text-color-secondary;
      }

      b {
        font-weight: 600;
        color: @text-color-base;
        text-align: right;
        word-break: break-all;
      }
    }
  }

  .content-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    font-weight: 600;
  }

  .content-box {
    min-height: 260px;
    max-height: 520px;
    margin: 0;
    padding: 14px;
    overflow: auto;
    border-radius: 8px;
    border: 1px solid @border-color-base;
    background: #f7f9fc;
    color: @text-color-base;
    white-space: pre-wrap;
    word-break: break-word;
  }

  @media (max-width: 900px) {
    .summary-main {
      align-items: flex-start;
    }

    .info-grid,
    .summary-grid,
    .timeline {
      grid-template-columns: 1fr;
    }

    .info-tile--wide {
      grid-column: auto;
    }
  }
</style>
