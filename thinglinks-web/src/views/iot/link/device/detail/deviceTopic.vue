<template>
  <div class="topic-panel">
    <div class="topic-toolbar">
      <div class="toolbar-title">
        <div class="title-icon"><NodeIndexOutlined /></div>
        <div>
          <div class="title-text">{{ Tt('title') }}</div>
          <div class="title-sub">{{ Tt('subtitle') }}</div>
        </div>
      </div>

      <div class="toolbar-actions">
        <AInput
          v-model:value="filters.topic"
          allow-clear
          size="small"
          class="topic-search"
          :placeholder="Tt('topicPh')"
          @press-enter="handleSearch"
        >
          <template #prefix>
            <SearchOutlined />
          </template>
        </AInput>
        <AButton size="small" type="primary" @click="handleSearch">
          <template #icon><SearchOutlined /></template>
          {{ Tt('search') }}
        </AButton>
        <AButton size="small" :loading="loading" @click="loadTopics">
          <template #icon><ReloadOutlined /></template>
          {{ Tt('refresh') }}
        </AButton>
      </div>
    </div>

    <div class="topic-filters">
      <ASelect
        v-model:value="filters.topicType"
        allow-clear
        size="small"
        class="filter-select"
        :placeholder="t('iot.link.productTopic.productTopic.topicType')"
        :options="topicTypeOptions"
        @change="handleSearch"
      />
      <ASelect
        v-model:value="filters.functionType"
        allow-clear
        size="small"
        class="filter-select"
        :placeholder="t('iot.link.productTopic.productTopic.functionType')"
        :options="functionTypeOptions"
        @change="handleSearch"
      />
      <ASelect
        v-model:value="filters.publisher"
        allow-clear
        size="small"
        class="filter-select"
        :placeholder="t('iot.link.productTopic.productTopic.publisher')"
        :options="publisherOptions"
        @change="handleSearch"
      />
      <ASelect
        v-model:value="filters.subscriber"
        allow-clear
        size="small"
        class="filter-select"
        :placeholder="t('iot.link.productTopic.productTopic.subscriber')"
        :options="subscriberOptions"
        @change="handleSearch"
      />
      <AButton size="small" @click="handleReset">{{ Tt('reset') }}</AButton>
    </div>

    <div class="topic-summary">
      <div v-for="item in summaryCards" :key="item.key" class="summary-item" :class="item.key">
        <div class="summary-icon">
          <NodeIndexOutlined v-if="item.key === 'total'" />
          <ProfileOutlined v-else-if="item.key === 'visible'" />
          <SwapOutlined v-else-if="item.key === 'publishable'" />
          <InboxOutlined v-else />
        </div>
        <div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <ASpin :spinning="loading">
      <div v-if="records.length === 0" class="topic-empty">
        <AEmpty :description="hasFilter ? Tt('emptyFiltered') : Tt('empty')">
          <template #image>
            <NodeIndexOutlined class="empty-icon" />
          </template>
        </AEmpty>
      </div>

      <div v-else class="topic-list">
        <div v-for="record in records" :key="record.id || record.topic" class="topic-row">
          <div class="row-main">
            <div class="row-head">
              <div class="tag-line">
                <ATag :color="topicTypeColor(record)">{{ topicTypeText(record) }}</ATag>
                <ATag color="green">{{ functionTypeText(record) }}</ATag>
                <span class="row-time">{{ record.updatedTime || record.createdTime || '-' }}</span>
              </div>
              <AButton type="link" size="small" @click="copyTopic(record.topic || '')">
                <template #icon><CopyOutlined /></template>
                {{ Tt('copy') }}
              </AButton>
            </div>

            <ATooltip :title="record.topic" placement="topLeft">
              <div class="topic-path">{{ record.topic || '-' }}</div>
            </ATooltip>

            <div class="topic-meta">
              <span>
                {{ t('iot.link.productTopic.productTopic.publisher') }}:
                <b>{{ publisherText(record) }}</b>
              </span>
              <span>
                {{ t('iot.link.productTopic.productTopic.subscriber') }}:
                <b>{{ subscriberText(record) }}</b>
              </span>
              <span v-if="record.remark">
                {{ t('iot.link.productTopic.productTopic.remark') }}:
                <b>{{ record.remark }}</b>
              </span>
            </div>
          </div>
        </div>
      </div>
    </ASpin>

    <div v-if="pagination.total > 0" class="topic-pagination">
      <APagination
        :current="pagination.current"
        :page-size="pagination.size"
        :total="pagination.total"
        show-size-changer
        show-quick-jumper
        size="small"
        :show-total="(total) => Tt('totalWithCount', { total })"
        @change="handlePageChange"
        @show-size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, reactive, ref, watch } from 'vue';
  import {
    Button as AButton,
    Empty as AEmpty,
    Input as AInput,
    Pagination as APagination,
    Select as ASelect,
    Spin as ASpin,
    Tag as ATag,
    Tooltip as ATooltip,
  } from 'ant-design-vue';
  import {
    CopyOutlined,
    InboxOutlined,
    NodeIndexOutlined,
    ProfileOutlined,
    ReloadOutlined,
    SearchOutlined,
    SwapOutlined,
  } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  import { DictEnum } from '/@/enums/commonEnum';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { page } from '/@/api/iot/link/productTopic/productTopic';
  import type { ProductTopicResultVO } from '/@/api/iot/link/productTopic/model/productTopicModel';
  import { handleCopyTextV2, handleFetchParams } from '/@/utils/thinglinks/common';
  import { replaceTopicDynamicParams } from '/@/utils/thinglinks/iot/topic';

  defineOptions({ name: 'DeviceTopic' });

  interface TopicFilters {
    topic?: string;
    topicType?: string | number;
    functionType?: string | number;
    publisher?: string | number;
    subscriber?: string | number;
  }

  const props = defineProps({
    deviceIdentification: {
      type: String,
      default: '',
    },
    deviceSdkVersion: {
      type: String,
      default: '',
    },
    nodeType: {
      type: [String, Number],
      default: '',
    },
    productIdentification: {
      type: String,
      default: '',
    },
    appId: {
      type: String,
      default: '',
    },
    userName: {
      type: String,
      default: '',
    },
  });

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { getDictLabel, getDictList } = useDict();
  const Tt = (key: string, params?: Recordable) =>
    t(`iot.link.device.device.deviceTopic.${key}`, params);

  const loading = ref(false);
  const records = ref<ProductTopicResultVO[]>([]);
  const filters = reactive<TopicFilters>({});
  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0,
  });

  const hasFilter = computed(() =>
    [filters.topic, filters.topicType, filters.functionType, filters.publisher, filters.subscriber]
      .map((item) => String(item ?? '').trim())
      .some(Boolean),
  );

  const topicTypeOptions = computed(() => dictOptions(DictEnum.LINK_PRODUCT_TOPIC_TYPE));
  const functionTypeOptions = computed(() =>
    dictOptions(DictEnum.LINK_PRODUCT_TOPIC_FUNCTION_TYPE),
  );
  const publisherOptions = computed(() => dictOptions(DictEnum.LINK_PRODUCT_TOPIC_PUBLISHER));
  const subscriberOptions = computed(() => dictOptions(DictEnum.LINK_PRODUCT_TOPIC_SUBSCRIBER));

  const summaryCards = computed(() => {
    const publishable = records.value.filter(
      (record) => !!(record.publisher || record.echoMap?.publisher),
    ).length;
    const subscribable = records.value.filter(
      (record) => !!(record.subscriber || record.echoMap?.subscriber),
    ).length;
    return [
      { key: 'total', label: Tt('total'), value: pagination.total },
      { key: 'visible', label: Tt('visible'), value: records.value.length },
      { key: 'publishable', label: Tt('publishable'), value: publishable },
      { key: 'subscribable', label: Tt('subscribable'), value: subscribable },
    ];
  });

  watch(
    () => [
      props.productIdentification,
      props.deviceIdentification,
      props.deviceSdkVersion,
      props.appId,
      props.userName,
    ],
    () => {
      pagination.current = 1;
      loadTopics();
    },
  );

  onMounted(loadTopics);

  async function loadTopics(): Promise<void> {
    if (!props.productIdentification) {
      records.value = [];
      pagination.total = 0;
      return;
    }
    loading.value = true;
    try {
      const result = await page(
        handleFetchParams({
          current: pagination.current,
          size: pagination.size,
          productIdentification: props.productIdentification,
          ...cleanFilters(filters),
        }) as any,
      );
      const rows = result?.records || [];
      records.value = rows.map((item) => ({
        ...item,
        topic: replaceTopicDynamicParams(item.topic || '', {
          appId: props.appId,
          userName: props.userName,
          deviceIdentification: props.deviceIdentification,
          deviceSdkVersion: props.deviceSdkVersion,
          productIdentification: props.productIdentification,
        }),
      }));
      pagination.total = Number(result?.total || 0);
    } catch (e: any) {
      records.value = [];
      pagination.total = 0;
      createMessage.error(e?.message || Tt('loadFailed'));
    } finally {
      loading.value = false;
    }
  }

  function cleanFilters(source: TopicFilters): TopicFilters {
    return Object.entries(source).reduce((model, [key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        model[key] = value;
      }
      return model;
    }, {} as Recordable);
  }

  function dictOptions(type: DictEnum): { label: string; value: string | number }[] {
    return getDictList(type).map((item) => ({
      label: item.name,
      value: item.key,
    }));
  }

  function dictText(type: DictEnum, value?: string | number, fallback = '-'): string {
    const echoValue = value ?? '';
    return getDictLabel(type, String(echoValue), fallback);
  }

  function topicTypeText(record: ProductTopicResultVO): string {
    return (
      record.echoMap?.topicType || dictText(DictEnum.LINK_PRODUCT_TOPIC_TYPE, record.topicType)
    );
  }

  function functionTypeText(record: ProductTopicResultVO): string {
    return (
      record.echoMap?.functionType ||
      dictText(DictEnum.LINK_PRODUCT_TOPIC_FUNCTION_TYPE, record.functionType)
    );
  }

  function publisherText(record: ProductTopicResultVO): string {
    return (
      record.echoMap?.publisher || dictText(DictEnum.LINK_PRODUCT_TOPIC_PUBLISHER, record.publisher)
    );
  }

  function subscriberText(record: ProductTopicResultVO): string {
    return (
      record.echoMap?.subscriber ||
      dictText(DictEnum.LINK_PRODUCT_TOPIC_SUBSCRIBER, record.subscriber)
    );
  }

  function topicTypeColor(record: ProductTopicResultVO): string {
    return Number(record.topicType) === 1 ? 'purple' : 'blue';
  }

  function handleSearch(): void {
    pagination.current = 1;
    loadTopics();
  }

  function handleReset(): void {
    Object.assign(filters, {
      topic: undefined,
      topicType: undefined,
      functionType: undefined,
      publisher: undefined,
      subscriber: undefined,
    });
    handleSearch();
  }

  function handlePageChange(pageNo: number, pageSize: number): void {
    pagination.current = pageNo;
    pagination.size = pageSize;
    loadTopics();
  }

  function handleSizeChange(_pageNo: number, pageSize: number): void {
    pagination.current = 1;
    pagination.size = pageSize;
    loadTopics();
  }

  function copyTopic(text: string): void {
    handleCopyTextV2(text);
  }
</script>

<style lang="less" scoped>
  .topic-panel {
    display: flex;
    flex-direction: column;
    gap: 14px;
    min-height: 420px;
  }

  .topic-toolbar {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    padding: 14px 16px;
    border: 1px solid #e5eaef;
    border-radius: 8px;
    background: #fff;
  }

  .toolbar-title {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 240px;
  }

  .title-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    flex-shrink: 0;
    border-radius: 8px;
    color: #5d87ff;
    background: #ecf2ff;
    font-size: 17px;
  }

  .title-text {
    color: #2a3547;
    font-size: 16px;
    font-weight: 700;
    line-height: 1.4;
  }

  .title-sub {
    margin-top: 2px;
    color: #5a6a85;
    font-size: 12px;
  }

  .toolbar-actions,
  .topic-filters {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    flex-wrap: wrap;
    gap: 8px;
    min-width: 0;
  }

  .topic-search {
    width: min(420px, 42vw);
    border-color: #dfe5ef;
    border-radius: 8px;
    background: #fff;
    box-shadow: none;

    &:hover,
    &:focus,
    &:focus-within {
      border-color: #5d87ff;
      box-shadow: 0 0 0 3px rgba(93, 135, 255, 0.12);
    }

    :deep(.ant-input) {
      color: #2a3547;
      font-size: 13px;
    }

    :deep(.anticon) {
      color: #8c97a5;
    }
  }

  .topic-filters {
    justify-content: flex-start;
    padding: 12px 16px;
    border: 1px solid #edf2f7;
    border-radius: 8px;
    background: #fff;
  }

  .filter-select {
    width: 188px;

    :deep(.ant-select-selector) {
      border-color: #dfe5ef !important;
      border-radius: 8px !important;
      box-shadow: none !important;
    }

    &:hover :deep(.ant-select-selector),
    &.ant-select-focused :deep(.ant-select-selector) {
      border-color: #5d87ff !important;
      box-shadow: 0 0 0 3px rgba(93, 135, 255, 0.12) !important;
    }
  }

  .topic-summary {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
  }

  .summary-item {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
    padding: 14px;
    border: 1px solid #edf2f7;
    border-radius: 8px;
    background: #fff;
  }

  .summary-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    flex-shrink: 0;
    border-radius: 8px;
    color: #5d87ff;
    background: #ecf2ff;
  }

  .summary-item.visible .summary-icon {
    color: #49beff;
    background: #e8f7ff;
  }

  .summary-item.publishable .summary-icon {
    color: #13deb9;
    background: #e6fffa;
  }

  .summary-item.subscribable .summary-icon {
    color: #ffae1f;
    background: #fff8e6;
  }

  .summary-value {
    max-width: 180px;
    overflow: hidden;
    color: #2a3547;
    font-size: 16px;
    font-weight: 700;
    line-height: 1.25;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .summary-label {
    margin-top: 2px;
    color: #5a6a85;
    font-size: 12px;
  }

  .topic-empty {
    padding: 48px 0;
    border: 1px dashed #dfe5ef;
    border-radius: 8px;
    background: #fff;
  }

  .empty-icon {
    color: #b8c2d1;
    font-size: 44px;
  }

  .topic-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .topic-row {
    padding: 14px;
    border: 1px solid #edf2f7;
    border-radius: 8px;
    background: #fff;
    transition: border-color 0.18s ease, background 0.18s ease;

    &:hover {
      border-color: #d8e2f0;
      background: #fbfcfe;
    }
  }

  .row-main {
    min-width: 0;
  }

  .row-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .tag-line {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    min-width: 0;
  }

  .row-time {
    color: #8c97a5;
    font-size: 12px;
  }

  .topic-path {
    margin-top: 10px;
    overflow: hidden;
    color: #2a3547;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 14px;
    font-weight: 700;
    line-height: 1.45;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .topic-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 16px;
    margin-top: 8px;
    color: #5a6a85;
    font-size: 12px;

    b {
      color: #2a3547;
      font-weight: 600;
    }
  }

  .topic-pagination {
    display: flex;
    justify-content: flex-end;
    padding-top: 2px;
  }

  @media (max-width: 1180px) {
    .topic-toolbar {
      flex-direction: column;
    }

    .toolbar-actions {
      justify-content: flex-start;
      width: 100%;
    }

    .topic-search {
      width: min(100%, 480px);
    }
  }

  @media (max-width: 768px) {
    .topic-summary {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .filter-select {
      width: calc(50% - 4px);
    }

    .row-head {
      align-items: flex-start;
      flex-direction: column;
    }
  }

  @media (max-width: 560px) {
    .topic-summary {
      grid-template-columns: 1fr;
    }

    .toolbar-actions,
    .topic-filters {
      align-items: stretch;
      flex-direction: column;
    }

    .topic-search,
    .filter-select {
      width: 100%;
    }
  }
</style>
