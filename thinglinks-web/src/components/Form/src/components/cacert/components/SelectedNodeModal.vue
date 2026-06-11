<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('iot.link.device.device.selectCertSerialNumber')"
    :maskClosable="false"
    :destroyOnClose="true"
    :keyboard="true"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :width="1200"
  >
    <div class="node-card-list">
      <!-- 搜索 + 仅"已颁发"过滤切换 -->
      <div class="filter-bar">
        <a-input-search
          v-model:value="keyword"
          :placeholder="t('iot.link.device.device.cert.searchPlaceholder')"
          allow-clear
          style="max-width: 320px"
          @search="handleSearch"
          @keydown.enter="handleSearch"
        />
        <a-checkbox v-model:checked="onlyIssued" @change="handleSearch">
          {{ t('iot.link.device.device.cert.onlyIssuedTip') }}
        </a-checkbox>
      </div>

      <a-spin :spinning="loading">
        <a-row v-if="dataList.length > 0" :gutter="[24, 12]">
          <a-col
            v-for="record in dataList"
            :key="record.id"
            :lg="12"
            :md="12"
            :sm="24"
            :xl="8"
            :xs="24"
            :xxl="6"
          >
            <div
              class="card_wrap"
              :class="{
                active: selectNode.serialNumber === record.serialNumber,
                disabled: record.state === 2,
              }"
              @click="handleSelectNode(record)"
            >
              <div class="left_info">
                <div class="cert-name" :title="record.certName">
                  {{ record.certName || t('iot.link.device.device.cert.unnamed') }}
                </div>

                <div class="item">
                  <span class="label">{{
                    t('iot.link.operationMaintenance.cacert.caCertLicense.serialNumber')
                  }}</span>
                  <span class="content" :title="record.serialNumber">
                    {{ record.serialNumber }}
                  </span>
                </div>

                <div class="item">
                  <span class="label">{{
                    t('iot.link.operationMaintenance.cacert.caCertLicense.algorithm')
                  }}</span>
                  <a-tag color="blue">
                    {{ getDictLabel(DictEnum.LINK_CA_CERT_ALGORITHM, record.algorithm, '—') }}
                  </a-tag>
                </div>

                <div class="item">
                  <span class="label">{{ t('iot.link.device.device.cert.validity') }}</span>
                  <span class="content small">{{ formatValidity(record) }}</span>
                </div>

                <div class="item">
                  <span class="label">{{ t('iot.link.device.device.cert.daysLeft') }}</span>
                  <a-tag :color="daysLeftColor(record.notAfter)">
                    {{ formatDaysLeft(record.notAfter) }}
                  </a-tag>
                </div>
              </div>

              <img
                class="right_img"
                src="/@/assets/images/iot/link/operationMaintenance/cacert/caCertLicense/ca-cert-license.jpg"
              />

              <!-- 状态徽章 (使用 LINK_CA_CERT_STATUS 字典) -->
              <div class="status-badge" :class="stateClass(record.state)">
                {{ getDictLabel(DictEnum.LINK_CA_CERT_STATUS, record.state, '—') }}
              </div>
            </div>
          </a-col>
        </a-row>

        <a-empty v-else :description="t('iot.link.device.device.cert.empty')" />

        <div class="card_pagination">
          <a-pagination
            v-model:current="current"
            v-model:pageSize="pageSize"
            :page-size-options="pageSizeOptions"
            :show-total="(total) => t('component.table.total', { total })"
            :total="total"
            show-quick-jumper
            show-size-changer
            size="small"
            @change="handleChangePagination"
          />
        </div>
      </a-spin>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, watch } from 'vue';
  import dayjs from 'dayjs';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { DictEnum } from '/@/enums/commonEnum';
  import { useDict } from '/@/components/Dict';
  import { page } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';
  import { useMessage } from '/@/hooks/web/useMessage';

  const props = defineProps<{ value?: string }>();
  const emits = defineEmits(['success', 'updateSelectNode']);

  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const { createMessage } = useMessage();
  const [registerModal, { closeModal }] = useModalInner();

  const selectNode = reactive<any>({});
  const current = ref(1);
  const pageSize = ref(20);
  const total = ref(0);
  const dataList = ref<any[]>([]);
  const loading = ref(false);
  const pageSizeOptions = ref(['10', '20', '30', '40', '50']);

  // 用户控件
  const keyword = ref('');
  const onlyIssued = ref(true);

  const buildModel = () => {
    const model: Record<string, any> = {};
    if (keyword.value) {
      // 后端的 certName 是 like, serialNumber 是 equal,
      // 为了一框两查走 certName like; 序列号需精确则走过滤兜底
      model.certName = keyword.value;
    }
    if (onlyIssued.value) {
      model.state = 1;
    }
    return model;
  };

  const getList = async (cur: number, size: number) => {
    loading.value = true;
    try {
      const res = await page({ current: cur, size, model: buildModel(), extra: {} });
      total.value = res.total;
      dataList.value = res.records || [];
    } finally {
      loading.value = false;
    }
  };

  const handleSelectNode = (record: any) => {
    if (record.state === 2) {
      createMessage.warning(t('iot.link.device.device.cert.cannotSelectRevoked'));
      return;
    }
    Object.assign(selectNode, record);
  };

  const handleSubmit = () => {
    if (!selectNode.serialNumber) {
      createMessage.warning(t('iot.link.device.device.cert.pleaseSelect'));
      return;
    }
    emits('success', selectNode);
    closeModal();
  };

  const handleCancel = () => {
    closeModal();
  };

  const handleSearch = () => {
    current.value = 1;
    getList(1, pageSize.value);
  };

  const handleChangePagination = (p: number, s: number) => {
    current.value = p;
    pageSize.value = s;
  };

  // ===== 工具方法 =====
  const stateClass = (state: number) => {
    switch (state) {
      case 1:
        return 'normal'; // 已颁发
      case 2:
        return 'error'; // 已吊销
      default:
        return 'pending'; // 待颁发
    }
  };

  const formatValidity = (record: any) => {
    if (!record.notBefore && !record.notAfter) return '—';
    const start = record.notBefore ? dayjs(record.notBefore).format('YYYY-MM-DD') : '?';
    const end = record.notAfter ? dayjs(record.notAfter).format('YYYY-MM-DD') : '?';
    return `${start} ~ ${end}`;
  };

  const computeDaysLeft = (notAfter?: string): number | null => {
    if (!notAfter) return null;
    const diff = dayjs(notAfter).diff(dayjs(), 'day');
    return diff;
  };

  const formatDaysLeft = (notAfter?: string) => {
    const d = computeDaysLeft(notAfter);
    if (d === null) return '—';
    if (d < 0) return t('iot.link.device.device.cert.expired');
    return t('iot.link.device.device.cert.daysRemaining', { days: d });
  };

  const daysLeftColor = (notAfter?: string) => {
    const d = computeDaysLeft(notAfter);
    if (d === null) return 'default';
    if (d < 0) return 'error';
    if (d <= 30) return 'warning';
    return 'success';
  };

  // 分页参数变化 → 刷新
  watch([current, pageSize], async ([c, s]) => {
    await getList(c, s);
  });

  // 首次渲染 + 外部 value 变化 (回显)
  watch(
    () => props.value,
    async (newValue) => {
      // 无论 value 是否存在，都先拉一次列表给用户选择
      if (dataList.value.length === 0) {
        await getList(current.value, pageSize.value);
      }
      if (newValue) {
        const hit = dataList.value.find((it) => it.serialNumber === newValue);
        if (hit) {
          Object.assign(selectNode, hit);
          emits('updateSelectNode', hit);
        }
      } else {
        // 清空回显
        Object.keys(selectNode).forEach((k) => delete selectNode[k]);
        emits('updateSelectNode', {});
      }
    },
    { immediate: true },
  );
</script>

<style lang="less" scoped>
  .node-card-list {
    background-color: #fff;
    padding: 22px;

    .filter-bar {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 16px;
    }

    .card_wrap {
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: relative;
      background-image: url('../../../../../../assets/images/iot/link/blue-bg.png');
      border: 1px solid #e8e8e8;
      padding: 10px 12px;
      border-radius: 8px;
      background-color: #fff;
      background-repeat: no-repeat;
      background-position: center center;
      background-size: 104% 104%;
      transition: all 0.3s;
      min-height: 200px;
      cursor: pointer;

      &:hover:not(.disabled) {
        border-color: #1a66ff;
        transform: scale(1.01);
        box-shadow: 0 4px 12px rgba(0, 26, 51, 0.08);
      }

      &.active {
        border: 1px solid #1a66ff;
        box-shadow: 0 4px 16px rgba(26, 102, 255, 0.18);
      }

      &.disabled {
        opacity: 0.55;
        cursor: not-allowed;
      }

      .left_info {
        flex: 1;
        z-index: 10;
        display: flex;
        flex-direction: column;
        gap: 4px;

        .cert-name {
          font-size: 15px;
          font-weight: 600;
          color: #1f2933;
          max-width: 230px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-bottom: 6px;
        }

        .item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;

          .label {
            color: #999;
            min-width: 60px;
          }

          .content {
            color: #333;
            max-width: 220px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;

            &.small {
              font-size: 12px;
            }
          }
        }
      }

      .right_img {
        width: 160px;
        height: 160px;
        margin-top: 18px;
        cursor: pointer;
        position: absolute;
        top: 0;
        right: 0;
        pointer-events: none;
      }

      .status-badge {
        position: absolute;
        top: 0;
        right: 0;
        border-radius: 0 8px 0 4px;
        padding: 2px 10px;
        font-size: 12px;

        &.normal {
          background-color: #e6fff5;
          color: #43cf7c;
        }

        &.error {
          background-color: #fad7d9;
          color: #d43030;
        }

        &.pending {
          background-color: #fff7e6;
          color: #fa8c16;
        }
      }
    }

    .card_pagination {
      display: flex;
      justify-content: flex-end;
      margin-top: 16px;
    }
  }
</style>
