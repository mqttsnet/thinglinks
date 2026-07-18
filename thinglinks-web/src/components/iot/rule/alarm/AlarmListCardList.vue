<template>
  <div class="card-list">
    <div class="card-header" v-if="type !== 'select'">
      <span>{{ title }}</span>
      <div>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('common.switchView') }}
        </a-button>
      </div>
    </div>
    <div class="loading" v-if="cardLoading">
      <a-spin />
    </div>
    <div class="card-body">
      <a-row :gutter="[24, 12]" v-if="alarmList.length">
        <a-col
          v-for="record in alarmList"
          :key="record.id"
          :xs="type === 'select' ? 12 : 24"
          :sm="type === 'select' ? 12 : 24"
          :md="12"
          :lg="12"
          :xl="type === 'select' ? 12 : 8"
          :xxl="type === 'select' ? 8 : 6"
        >
          <div
            :class="[
              'product-item',
              'alarm-card',
              type === 'select' ? 'product-item--select' : '',
              getStatusClass(record.status),
              hasSelected === record?.alarmIdentification ? 'has_selected' : '',
            ]"
            @click="selectItem(record, $event)"
          >
            <div class="product-info">
              <div class="status">{{ statusLabel(record.status) }}</div>
              <a-tooltip placement="top" :title="record.alarmName || '-'">
                <div class="title o2">{{ record.alarmName || '-' }}</div>
              </a-tooltip>
              <div class="props">
                <div class="prop">
                  <div class="label">{{ t('iot.link.engine.alarm.alarmIdentification') }}</div>
                  <a-tooltip placement="top" :title="record.alarmIdentification || '-'">
                    <div class="value">{{ formatValue(record.alarmIdentification) }}</div>
                  </a-tooltip>
                </div>
                <div class="flex prop-row">
                  <div class="prop">
                    <div class="label">{{ t('iot.link.engine.alarm.level') }}</div>
                    <div class="value">{{ levelLabel(record.level) }}</div>
                  </div>
                  <div class="prop">
                    <div class="label">{{ t('iot.link.engine.alarm.alarmScene') }}</div>
                    <div class="value">{{ sceneLabel(record.alarmScene) }}</div>
                  </div>
                </div>
              </div>
              <div class="btns" v-if="type !== 'select'">
                <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.delete')">
                    <Icon
                      icon="ant-design:delete-outlined"
                      :size="16"
                      class="action-icon action-icon--danger"
                      @click.stop="handleDelete(record)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.copy')">
                    <Icon
                      icon="ant-design:copy-outlined"
                      :size="16"
                      class="action-icon"
                      @click.stop="handleCopy(record)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.edit')">
                    <Icon
                      icon="ant-design:edit-outlined"
                      :size="16"
                      class="action-icon"
                      @click.stop="handleEdit(record)"
                    />
                  </a-tooltip>
                </div>
              </div>
            </div>
            <div class="product-img alarm-card-img" @click="handleView(record, $event)">
              <AlarmRuleSvg class="alarm-card-svg" />
            </div>
          </div>
        </a-col>
      </a-row>
      <a-empty v-else />
      <div class="tr">
        <a-pagination
          @change="change"
          size="small"
          v-model:current="current"
          v-model:pageSize="size"
          :total="total"
          :show-total="(total) => t('component.table.total', { total })"
          show-size-changer
          show-quick-jumper
          :page-size-options="pageSizeOptions"
        />
      </div>
    </div>
  </div>
  <EditModal @register="registerModal" @success="handleSuccess" />
</template>

<script lang="ts">
  import { defineComponent, ref, watch } from 'vue';
  import { Row, Col, Button, Spin, Tooltip, Pagination, Empty } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { useModal } from '/@/components/Modal';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { page, remove } from '/@/api/iot/rule/alarm/alarm';
  import type { AlarmResultVO } from '/@/api/iot/rule/alarm/model/alarmModel';
  import EditModal from '/@/views/iot/rule/alarm/list/Edit.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDict } from '/@/components/Dict';
  import { AlarmRuleSvg } from '/@/components/iot/svg';
  import { Icon } from '/@/components/Icon';

  const { getDictLabel } = useDict();

  export default defineComponent({
    name: 'AlarmListCardList',
    components: {
      ARow: Row,
      ACol: Col,
      AButton: Button,
      ASpin: Spin,
      ATooltip: Tooltip,
      APagination: Pagination,
      AEmpty: Empty,
      EditModal,
      AlarmRuleSvg,
      Icon,
    },
    props: {
      title: {
        type: String,
        default: '列表',
      },
      searchData: {
        type: Object,
        default: () => ({}),
      },
      type: {
        type: String,
        default: '',
      },
      selectedValue: {
        type: String,
        default: '',
      },
    },
    emits: ['input', 'handleSelect'],
    setup(props, { emit }) {
      const { t } = useI18n();
      const [registerModal, { openModal }] = useModal();
      const { createMessage, createConfirm } = useMessage();
      const current = ref(1);
      const size = ref(20);
      const total = ref(0);
      const alarmList = ref<AlarmResultVO[]>([]);
      const model = ref<Recordable>({});
      const cardLoading = ref(false);
      const hasSelected = ref('');
      const pageSizeOptions = ref<string[]>(['10', '20', '30', '40', '50']);

      const formatValue = (value?: string | number | null) =>
        value === undefined || value === null || value === '' ? '-' : String(value);
      const statusLabel = (value?: number) =>
        getDictLabel('RULE_ALARM_STATUS', formatValue(value), '-');
      const levelLabel = (value?: number) =>
        getDictLabel('RULE_ALARM_LEVEL', formatValue(value), '-');
      const sceneLabel = (value?: string) =>
        getDictLabel('RULE_ALARM_SCENE', formatValue(value), '-');
      const getStatusClass = (value?: number) => (Number(value) === 1 ? 'normal' : 'error');

      const getAlarmList = () => {
        cardLoading.value = true;
        page({
          current: current.value,
          size: size.value,
          ...handleFetchParams(model.value),
        })
          .then((res: any) => {
            total.value = res?.total || 0;
            alarmList.value = res?.records || [];
          })
          .finally(() => {
            cardLoading.value = false;
          });
      };

      watch(
        () => props.searchData,
        (newValue) => {
          model.value = { ...(newValue || {}) };
          current.value = 1;
          getAlarmList();
        },
        { immediate: true, deep: true },
      );

      watch(
        () => props.selectedValue,
        (val) => {
          hasSelected.value = val || '';
        },
        { immediate: true },
      );

      function switchView() {
        emit('input', false);
      }

      function handleAdd() {
        openModal(true, { type: ActionEnum.ADD });
      }

      function handleCopy(record: Recordable) {
        openModal(true, { record, type: ActionEnum.COPY });
      }

      function handleEdit(record: Recordable) {
        openModal(true, { record, type: ActionEnum.EDIT });
      }

      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (props.type === 'select') return;
        openModal(true, { record, type: ActionEnum.VIEW });
      }

      function selectItem(record: Recordable, e: Event) {
        e?.stopPropagation();
        hasSelected.value = record.alarmIdentification;
        emit('handleSelect', record.alarmIdentification, record);
      }

      function handleSuccess() {
        getAlarmList();
      }

      function change() {
        getAlarmList();
      }

      function handleDelete(record: Recordable) {
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            await remove([record.id]);
            createMessage.success(t('common.tips.deleteSuccess'));
            getAlarmList();
          },
        });
      }

      return {
        t,
        alarmList,
        cardLoading,
        registerModal,
        handleAdd,
        handleCopy,
        handleEdit,
        handleView,
        handleSuccess,
        selectItem,
        switchView,
        current,
        total,
        size,
        change,
        pageSizeOptions,
        handleDelete,
        hasSelected,
        statusLabel,
        levelLabel,
        sceneLabel,
        getStatusClass,
        formatValue,
      };
    },
  });
</script>

<style scoped lang="less">
  @import '../../../Table/src/types/components/cardCommon.less';

  .card-list {
    background-color: #fff;
    padding: 6px;
  }

  .card-body {
    padding: 0 23px;
  }

  .alarm-card {
    cursor: pointer;

    &.has_selected {
      border: 2px solid @primary-color;
      box-shadow: 0 0 8px 0 fade(@primary-color, 20%);
    }

    .prop-row {
      gap: 12px;
      justify-content: space-between;
    }
  }

  .alarm-card-img {
    width: 104px;
    height: 104px;

    .alarm-card-svg {
      width: 104px;
      height: 104px;
      display: block;
    }
  }

  .action-icon {
    color: @primary-color;
    cursor: pointer;
    transition: color 0.2s ease;

    &:hover {
      color: @primary-color;
    }

    &--danger {
      color: @button-error-color;

      &:hover {
        color: @button-error-hover-color;
      }
    }
  }
</style>
