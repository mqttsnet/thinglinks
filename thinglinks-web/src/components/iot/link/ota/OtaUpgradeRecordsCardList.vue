<template>
  <div style="background-color: #fff; padding: 6px">
    <div class="card-header">
      <span>{{ title }}</span>
      <div>
        <a-button preIcon="ant-design:swap-outlined" @click="selectAll">
          {{
            selectDeviceList.length === deviceList.length
              ? t('iot.link.ota.otaUpgradeRecords.selectNone')
              : t('iot.link.ota.otaUpgradeRecords.selectAll')
          }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="batchUpgrade(false)">
          {{ t('iot.link.ota.otaUpgradeRecords.batchUpgrade') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="addDeviceUpgrade(false)">
          {{ t('iot.link.ota.otaUpgradeRecords.addDeviceUpgrade') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">{{
          t('common.switchView')
        }}</a-button>
      </div>
    </div>
    <div v-if="cardLoading" class="loading">
      <a-spin />
    </div>
    <div style="padding: 0 23px">
      <a-row v-if="deviceList.length" :gutter="[24, 12]">
        <a-col
          v-for="record in deviceList"
          :key="record.id"
          :lg="12"
          :md="12"
          :sm="24"
          :xl="8"
          :xs="24"
          :xxl="6"
        >
          <div
            :class="{
              'product-item': true,
              normal: record.upgradeStatus == 0 || record.upgradeStatus == 2,
              warning: record.upgradeStatus == 1,
              error: record.upgradeStatus == 3,
              'selected-device': selectDeviceList.some((i) => i.id === record.id),
            }"
            @click="selectItem(record, $event)"
          >
            <div class="product-info">
              <div class="status">
                <template
                  v-if="
                    getDictLabel(
                      'LINK_OTA_TASK_RECORD_APP_CONFIRM_STATUS',
                      record.appConfirmationStatus + '',
                      '',
                    )
                  "
                >
                  {{
                    getDictLabel(
                      'LINK_OTA_TASK_RECORD_APP_CONFIRM_STATUS',
                      record.appConfirmationStatus + '',
                      '',
                    )
                  }}
                  |
                </template>
                <template
                  v-if="
                    getDictLabel(
                      'LINK_OTA_TASK_RECORD_COMMAND_SEND_STATUS',
                      record.commandSendStatus + '',
                      '',
                    )
                  "
                >
                  {{
                    getDictLabel(
                      'LINK_OTA_TASK_RECORD_COMMAND_SEND_STATUS',
                      record.commandSendStatus + '',
                      '',
                    )
                  }}
                  |
                </template>
                {{ getDictLabel('LINK_OTA_TASK_RECORD_STATUS', record.upgradeStatus + '', '') }}
              </div>
              <a-tooltip
                placement="top"
                :title="`${t('iot.link.ota.otaUpgradeRecords.deviceIdentification')}：${record.deviceIdentification}`"
              >
                <div class="title">
                  {{ t('iot.link.ota.otaUpgradeRecords.deviceIdentification') }}：{{
                    record.deviceIdentification
                  }}
                </div>
              </a-tooltip>
              <div class="props">
                <div class="prop">
                  <div class="label">{{ t('iot.link.ota.otaUpgradeRecords.progress') }}</div>
                  <div class="value">{{ record.progress }}%</div>
                </div>
                <div class="prop">
                  <div class="label">{{ t('iot.link.ota.otaUpgradeRecords.createdTime') }}</div>
                  <div class="value">{{ record.createdTime }}</div>
                </div>
              </div>
              <div class="btns">
                <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.delete')">
                    <img
                      alt=""
                      src="/@/assets/images/iot/link/device/delete-y.png"
                      @click.stop="handleDelete(record)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn">
                  <a-tooltip placement="top" :title="t('iot.link.ota.otaUpgradeRecords.reUpgrade')">
                    <ToTopOutlined style="color: #1a66ff" @click.stop="batchUpgrade(record)" />
                  </a-tooltip>
                </div>
                <div class="btn" style="height: 15px">
                  <a-tooltip placement="top" :title="t('common.title.details')">
                    <img
                      alt=""
                      src="/@/assets/images/iot/link/detail-icon2.png"
                      @click.stop="handleView(record, $event)"
                      v-hasAnyPermission="[
                        'link:otaUpgradeTasks:detail:view',
                        'link:otaUpgradeTasks:detail:records',
                      ]"
                    />
                  </a-tooltip>
                </div>
              </div>
            </div>
            <div class="product-img">
              <img
                v-if="record?.upgradeStatus === 1"
                src="/@/assets/images/iot/link/upgradingCount.png"
                @click.stop="handleView(record, $event)"
              />
              <img
                v-else-if="record?.upgradeStatus === 2"
                src="/@/assets/images/iot/link/upgradeSuccessCount.png"
                @click.stop="handleView(record, $event)"
              />
              <img
                v-else-if="record?.upgradeStatus === 3"
                src="/@/assets/images/iot/link/upgradeFailureCount.png"
                @click.stop="handleView(record, $event)"
              />
              <img
                v-else-if="record?.upgradeStatus === 0"
                src="/@/assets/images/iot/link/pendingUpgradeCount.png"
                @click.stop="handleView(record, $event)"
              />
            </div>
          </div>
        </a-col>
      </a-row>
      <a-empty v-else />
      <div class="tr">
        <a-pagination
          v-model:current="current"
          v-model:pageSize="size"
          :page-size-options="pageSizeOptions"
          :show-total="(total) => t('component.table.total', { total })"
          :total="total"
          show-quick-jumper
          show-size-changer
          size="small"
          @change="change"
        />
      </div>
    </div>
  </div>
  <EditModal @register="registerModal" @success="handleSuccess" />
  <AddOtaUpgradeDevice @register="registerModalAddDevice" />
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, watch } from 'vue';
  // util
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useModal } from '/@/components/Modal';
  import { useLoading } from '/@/components/Loading';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  // api
  import { page, remove } from '/@/api/iot/link/ota/otaUpgradeRecords';
  import { upgradeAgain } from '/@/api/iot/link/ota/otaUpgradeTasks';
  // components
  import {
    Card,
    Row,
    Col,
    Button,
    Spin,
    Divider,
    Tooltip,
    Pagination,
    Upload,
    Empty,
  } from 'ant-design-vue';
  import { useRouter, useRoute } from 'vue-router';
  import EditModal from '/@/views/iot/link/ota/otaUpgradeRecords/Edit.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDict } from '/@/components/Dict';
  import { ToTopOutlined } from '@ant-design/icons-vue';
  import AddOtaUpgradeDevice from '/@/views/iot/link/ota/otaUpgradeRecords/components/AddOtaUpgradeDevice.vue';
  import { usePermission } from '/@/hooks/web/usePermission';

  const { getDictLabel } = useDict();

  interface deviceItem {
    id: string;
    productName: string;
    productType: string;
    protocolType: string;
    productIdentification: string;
    productStatus: number;
    echoMap: any;
    customInfo: any;
    status: any;
    packageName: any;
    packageType: any;
    version: any;
    taskName: any;
    taskStatus: any;
    scheduledTime: any;
    upgradeStatus: any;
    appId: any;
    progress: any;
    taskId: any;
  }

  import Icon4 from '/@/assets/images/iot/link/device/Icon4.png';
  import Icon5 from '/@/assets/images/iot/link/device/Icon5.png';

  export default defineComponent({
    name: 'CardList',
    components: {
      ACard: Card,
      ARow: Row,
      ACol: Col,
      AButton: Button,
      ASpin: Spin,
      EditModal,
      ADivider: Divider,
      ATooltip: Tooltip,
      APagination: Pagination,
      AUpload: Upload,
      AEmpty: Empty,
      ToTopOutlined,
      AddOtaUpgradeDevice,
    },
    props: {
      title: {
        type: String,
        default: '列表',
      },
      searchData: {
        type: Object,
        default: {},
      },
      productIdentification: {
        type: String,
        default: '',
      },
    },
    setup(props, { emit }) {
      const { t } = useI18n();
      const [registerModal] = useModal();
      const { push } = useRouter();
      const route = useRoute();
      const { hasAnyPermission } = usePermission();

      const [openFullLoading, closeFullLoading] = useLoading({
        tip: t('common.loadingText'),
      });

      const current = ref(1);
      const size = ref(20);
      const total = ref(0);
      const { createMessage, createConfirm } = useMessage();
      let model = reactive({});
      // 设备列表
      let deviceList = ref<Array<deviceItem>>([]);
      // 设备列表加载状态
      const cardLoading = ref<boolean>(false);
      const getDecviceList = () => {
        cardLoading.value = true;
        page({
          current: current.value,
          size: size.value,
          ...handleFetchParams({ ...model, taskId: route.params.id as string }),
        }).then((res: any) => {
          total.value = res.total;
          deviceList.value = res.records;
          cardLoading.value = false;
        });
      };
      watch(
        () => props.searchData,
        (newValue) => {
          model = newValue;
          getDecviceList();
        },
        { immediate: true, deep: true },
      );
      // 切换视图 卡片&&列表
      const switchFlag = ref<boolean>(true);

      function switchView() {
        switchFlag.value = !switchFlag.value;
        emit('input', switchFlag.value);
      }

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        push({
          name: 'OTA升级任务记录详情',
          params: { id: record.id },
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        getDecviceList();
      }

      function change() {
        getDecviceList();
      }

      // 删除
      function handleDelete(record) {
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            try {
              await remove([record.id]);
              getDecviceList();
            } catch (e) {}
          },
        });
      }

      const pageSizeOptions = ref<string[]>(['10', '20', '30', '40', '50']);

      // 批量操作相关逻辑
      const selectDeviceList = ref<string>([]);
      function selectItem(record: Recordable, e: Event) {
        e?.stopPropagation();
        const isSelected = selectDeviceList.value?.find((i) => i.id === record.id);
        if (isSelected) {
          selectDeviceList.value = selectDeviceList.value.filter((i) => i.id !== record.id);
        } else {
          selectDeviceList.value.push(record);
        }
      }
      const selectAll = () => {
        if (selectDeviceList.value.length === deviceList.value.length) {
          selectDeviceList.value = [];
        } else {
          selectDeviceList.value = deviceList.value;
        }
      };
      const batchUpgrade = async (record: Recordable) => {
        const selectList = record ? [record] : selectDeviceList.value;
        if (!selectList.length) {
          return createMessage.warning(t('iot.link.ota.otaUpgradeRecords.tips.select'));
        }

        openFullLoading();
        try {
          const upgradeTaskId = route.params.id;
          const deviceIdentificationList = selectList.map((i) => i.deviceIdentification);

          const data = await upgradeAgain({
            deviceIdentificationList,
            upgradeTaskId,
          });

          if (data) {
            createMessage.success(data);
          }
        } catch (err) {
          console.log(err);
        }
        closeFullLoading();
      };

      const [registerModalAddDevice, { openModal: openModalAddDevice }] = useModal();
      const addDeviceUpgrade = () => {
        openModalAddDevice(true, {
          productIdentification: props.productIdentification,
          upgradeTaskId: route.params.id,
        });
      };

      return {
        t,
        deviceList,
        cardLoading,
        switchFlag,
        switchView,
        registerModal,
        handleSuccess,
        handleView,
        Icon4,
        Icon5,
        current,
        total,
        size,
        getDecviceList,
        change,
        pageSizeOptions,
        handleDelete,
        getDictLabel,
        selectDeviceList,
        selectItem,
        selectAll,
        batchUpgrade,
        registerModalAddDevice,
        addDeviceUpgrade,
      };
    },
  });
</script>
<style scoped>
  @import '../../../Table/src/types/components/cardCommon.less';

  .title {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    /* 设置为2行 */
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .product-item {
    position: relative;
  }

  .selected-device {
    border: 3px solid #1a66ff;
  }

  .selected-device::before {
    opacity: 1;
    border: 10px solid #1a66ff;
    border-block-end: 10px solid transparent;
    border-inline-end: 10px solid transparent;
    border-radius: 4px 0 0 0;
    position: absolute;
    inset-block-start: 2px;
    inset-inline-start: 2px;
    width: 0;
    height: 0;
    transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
    content: '';
  }

  :deep(.status) {
    max-width: none !important;
    top: 13px !important;
  }
</style>
