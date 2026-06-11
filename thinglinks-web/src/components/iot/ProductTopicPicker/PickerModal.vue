<template>
  <BasicModal
    @register="registerModal"
    :title="t('component.productTopicPicker.modalTitle')"
    :maskClosable="false"
    :width="900"
    @ok="handleConfirm"
  >
    <!-- 顶部产品 + 模式切换 -->
    <div class="picker-header">
      <a-tag color="blue" class="product-tag">
        <DatabaseOutlined />
        {{ productIdentification }}
      </a-tag>
      <a-radio-group v-model:value="mode" button-style="solid" size="small">
        <a-radio-button value="basic">
          <UnorderedListOutlined />
          {{ t('component.productTopicPicker.modeBasic') }}
        </a-radio-button>
        <a-radio-button value="custom">
          <EditOutlined />
          {{ t('component.productTopicPicker.modeCustom') }}
        </a-radio-button>
      </a-radio-group>
    </div>

    <!-- ============================== ① 基础 Topic 模式 ============================== -->
    <div v-if="mode === 'basic'" class="picker-body">
      <a-input
        v-model:value="keyword"
        :placeholder="t('component.productTopicPicker.placeholderSearch')"
        allow-clear
        size="small"
        style="width: 280px; margin-bottom: 8px"
        @change="onSearch"
      >
        <template #prefix>
          <SearchOutlined />
        </template>
      </a-input>
      <a-table
        :columns="basicColumns"
        :loading="loading"
        :row-selection="{
          type: 'radio',
          columnWidth: 50,
          onChange: onSelectChange,
          selectedRowKeys: selectedRowKeys,
        }"
        :data-source="data"
        :pagination="false"
        :scroll="{ y: 360 }"
        rowKey="topic"
        size="small"
        @row-click="onRowClick"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'topic'">
            <code class="topic-mono">{{ record.topic }}</code>
          </template>
        </template>
        <template #emptyText>
          <a-empty
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
            :description="t('component.productTopicPicker.noBasicTopic')"
          />
        </template>
      </a-table>
      <div class="pagination-row">
        <a-pagination
          v-model:current="current"
          v-model:pageSize="size"
          :total="total"
          size="small"
          :show-total="(t) => totalLabel(t)"
          show-size-changer
          show-quick-jumper
          :page-size-options="['10', '20', '30', '50']"
          @change="loadData"
        />
      </div>
    </div>

    <!-- ============================== ② 自定义 Topic 模式 ============================== -->
    <div v-else class="picker-body">
      <div class="custom-help">
        <InfoCircleOutlined />
        <span>{{ t('component.productTopicPicker.customHelp') }}</span>
      </div>

      <a-input
        v-model:value="customTopic"
        :placeholder="t('component.productTopicPicker.placeholderCustom')"
        size="large"
        class="custom-input"
        allow-clear
      >
        <template #prefix>
          <FunctionOutlined />
        </template>
      </a-input>

      <!-- 通配符语法提示 -->
      <a-card class="syntax-card" :bordered="false" size="small">
        <template #title>
          <span class="syntax-title">
            <ThunderboltOutlined />
            {{ t('component.productTopicPicker.syntaxTitle') }}
          </span>
        </template>
        <a-descriptions :column="1" size="small" :colon="false">
          <a-descriptions-item :label="t('component.productTopicPicker.syntaxExact')">
            <code class="topic-mono">device/001/temperature</code>
            <span class="syntax-desc">
              {{ t('component.productTopicPicker.syntaxExactDesc') }}
            </span>
          </a-descriptions-item>
          <a-descriptions-item :label="t('component.productTopicPicker.syntaxSingle')">
            <code class="topic-mono">factory/+/status</code>
            <span class="syntax-desc">
              {{ t('component.productTopicPicker.syntaxSingleDesc') }}
            </span>
          </a-descriptions-item>
          <a-descriptions-item :label="t('component.productTopicPicker.syntaxMulti')">
            <code class="topic-mono">home/room/#</code>
            <span class="syntax-desc">
              {{ t('component.productTopicPicker.syntaxMultiDesc') }}
            </span>
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 快捷模板按钮 -->
      <div class="template-row">
        <span class="template-label">{{ t('component.productTopicPicker.quickTemplates') }}:</span>
        <a-tag
          v-for="tpl in quickTemplates"
          :key="tpl"
          color="blue"
          class="template-tag"
          @click="customTopic = tpl"
        >
          {{ tpl }}
        </a-tag>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, toRefs, computed } from 'vue';
  import { Empty } from 'ant-design-vue';
  import {
    DatabaseOutlined,
    UnorderedListOutlined,
    EditOutlined,
    SearchOutlined,
    InfoCircleOutlined,
    FunctionOutlined,
    ThunderboltOutlined,
  } from '@ant-design/icons-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDebounceFn } from '@vueuse/core';
  import { page as productTopicPage } from '/@/api/iot/link/productTopic/productTopic';
  import type { ProductTopicPickerMode, ProductTopicRecord } from './types';

  type Key = string | number;

  defineOptions({ name: 'ProductTopicPickerModal' });

  const emit = defineEmits<{
    (e: 'success', topic: string, mode: ProductTopicPickerMode): void;
    (e: 'register', ...args: any[]): void;
  }>();

  const { t } = useI18n();
  const { createMessage } = useMessage();

  // ============================== 状态 ==============================
  const productIdentification = ref('');
  const mode = ref<ProductTopicPickerMode>('basic');

  // 基础 topic 表
  const current = ref(1);
  const size = ref(10);
  const total = ref(0);
  const keyword = ref('');
  const loading = ref(false);

  const state = reactive<{
    selectedRowKeys: Key[];
    data: ProductTopicRecord[];
  }>({
    selectedRowKeys: [],
    data: [],
  });

  // 自定义 topic
  const customTopic = ref('');

  // 列定义
  const basicColumns = computed(() => [
    {
      title: t('component.productTopicPicker.col.functionType'),
      dataIndex: ['echoMap', 'functionType'],
      width: 120,
    },
    {
      title: t('component.productTopicPicker.col.topic'),
      dataIndex: 'topic',
      ellipsis: true,
    },
    {
      title: t('component.productTopicPicker.col.publisher'),
      dataIndex: ['echoMap', 'publisher'],
      width: 110,
    },
    {
      title: t('component.productTopicPicker.col.subscriber'),
      dataIndex: ['echoMap', 'subscriber'],
      width: 110,
    },
    {
      title: t('component.productTopicPicker.col.remark'),
      dataIndex: 'remark',
      ellipsis: true,
    },
  ]);

  // 快捷模板
  const quickTemplates = computed(() => [
    'device/+/data',
    'device/+/cmd',
    'home/room/#',
    '$thing/up/property/+',
    '$thing/down/control/+',
  ]);

  // ============================== 数据加载 ==============================
  async function loadData() {
    if (!productIdentification.value) return;
    loading.value = true;
    try {
      const model: Record<string, any> = {
        productIdentification: productIdentification.value,
      };
      if (keyword.value) model.topic = keyword.value;
      const res: any = await productTopicPage({
        model,
        current: current.value,
        size: size.value,
      } as any);
      state.data = res?.records ?? [];
      total.value = res?.total ?? 0;
    } finally {
      loading.value = false;
    }
  }

  const onSearch = useDebounceFn(() => {
    current.value = 1;
    loadData();
  }, 350);

  function totalLabel(n: number) {
    return t('component.table.total', { total: n });
  }

  // ============================== 表格行交互 ==============================
  function onSelectChange(keys: Key[]) {
    state.selectedRowKeys = keys;
  }

  /** 双击行也算选中(快捷选择) */
  function onRowClick(record: ProductTopicRecord) {
    state.selectedRowKeys = [record.topic];
  }

  // ============================== Modal 注册 ==============================
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    productIdentification.value = data?.productIdentification || '';

    // 重置状态
    state.data = [];
    state.selectedRowKeys = [];
    keyword.value = '';
    current.value = 1;
    customTopic.value = '';

    // 默认按调用方意图选中模式;否则:basic
    mode.value = data?.defaultMode || 'basic';

    // 已有值回填(如编辑场景):
    // - 包含 + / # → 自定义模式 + 填入 customTopic
    // - 否则若可在基础 topic 列表中找到 → 基础模式 + 选中该行
    // - 否则 → 自定义模式
    const initialValue: string = data?.value || '';
    if (initialValue) {
      if (/[+#]/.test(initialValue)) {
        mode.value = 'custom';
        customTopic.value = initialValue;
      } else {
        // 走基础模式;loadData 后查不到的话再降级
        mode.value = 'basic';
        await loadData();
        if (state.data.some((r) => r.topic === initialValue)) {
          state.selectedRowKeys = [initialValue];
        } else {
          mode.value = 'custom';
          customTopic.value = initialValue;
        }
        return;
      }
    }

    if (mode.value === 'basic') {
      await loadData();
    }
  });

  // ============================== 确认 ==============================
  function handleConfirm() {
    if (mode.value === 'basic') {
      if (state.selectedRowKeys.length === 0) {
        createMessage.warning(t('component.productTopicPicker.warnPickBasic'));
        return;
      }
      emit('success', String(state.selectedRowKeys[0]), 'basic');
    } else {
      const v = (customTopic.value || '').trim();
      if (!v) {
        createMessage.warning(t('component.productTopicPicker.warnInputCustom'));
        return;
      }
      emit('success', v, 'custom');
    }
    closeModal();
  }

  // expose to template
  const { selectedRowKeys, data } = toRefs(state);
</script>

<style lang="less" scoped>
  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px dashed #f0f0f0;

    .product-tag {
      font-family: Menlo, Monaco, monospace;
      font-size: 13px;
      padding: 4px 10px;
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }
  }

  .picker-body {
    min-height: 380px;
  }

  .topic-mono {
    font-family: Menlo, Monaco, 'Courier New', monospace;
    font-size: 12px;
    background: #f5f5f5;
    padding: 2px 8px;
    border-radius: 3px;
    color: #d4380d;
  }

  .pagination-row {
    margin-top: 12px;
    text-align: right;
  }

  // ============================== 自定义模式 ==============================
  .custom-help {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    margin-bottom: 12px;
    background: #e6f4ff;
    border-radius: 4px;
    color: #0958d9;
    font-size: 13px;

    :deep(svg) {
      color: #1677ff;
    }
  }

  .custom-input {
    margin-bottom: 12px;

    :deep(.ant-input) {
      font-family: Menlo, Monaco, 'Courier New', monospace;
    }
  }

  .syntax-card {
    background: #fafafa;
    margin-bottom: 12px;

    :deep(.ant-card-head) {
      min-height: 40px;
      padding: 0 12px;
    }

    :deep(.ant-card-body) {
      padding: 8px 12px;
    }

    .syntax-title {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 600;
      color: #1f2937;

      :deep(svg) {
        color: #faad14;
      }
    }

    .syntax-desc {
      margin-left: 8px;
      color: #8c8c8c;
      font-size: 12px;
    }
  }

  .template-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;

    .template-label {
      font-size: 12px;
      color: #595959;
      margin-right: 4px;
    }

    .template-tag {
      cursor: pointer;
      font-family: Menlo, Monaco, monospace;
      font-size: 12px;
      transition: all 0.2s;

      &:hover {
        background: #1677ff;
        color: #fff;
        border-color: #1677ff;
      }
    }
  }
</style>
