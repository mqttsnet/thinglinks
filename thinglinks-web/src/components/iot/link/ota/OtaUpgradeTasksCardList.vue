<template>
  <div style="background-color: #fff; padding: 6px">
    <div class="card-header">
      <span>{{ title }}</span>
      <div>
        <a-button
          preIcon="ant-design:plus-outlined"
          type="primary"
          @click="handleAdd"
          v-hasAnyPermission="['basic:link:otaUpgradeTasks:add']"
        >
          {{ t('common.title.add') }}
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
              normal: record.taskStatus == 1,
              error: record.taskStatus == 0,
            }"
            @click="selectItem(record, $event)"
          >
            <div class="product-info">
              <div class="status">
                <template v-if="getDictLabel('LINK_OTA_TASK_STATUS', record.taskStatus + '', '')">
                  {{ getDictLabel('LINK_OTA_TASK_STATUS', record.taskStatus + '', '') }} |
                </template>
                {{ record.echoMap.upgradeMethod }}
              </div>
              <a-tooltip placement="top" :title="record.taskName">
                <div class="title">{{ record.taskName }}</div>
              </a-tooltip>
              <div class="props">
                <div class="prop">
                  <div class="label">{{ t('iot.link.ota.otaUpgradeRecords.taskId') }}</div>
                  <div class="value">{{ record.id }}</div>
                </div>
              </div>
              <div class="props">
                <div class="prop">
                  <div class="label">
                    {{ t('iot.link.ota.otaUpgradeTasks.scheduledStartTime') }}
                  </div>
                  <a-tooltip :title="record.scheduledStartTime">
                    <div class="value">{{ record.scheduledStartTime }}</div>
                  </a-tooltip>
                </div>
                <div class="prop">
                  <div class="label">
                    {{ t('iot.link.ota.otaUpgradeTasks.scheduledEndTime') }}
                  </div>
                  <a-tooltip :title="record.scheduledEndTime">
                    <div class="value">{{ record.scheduledEndTime }}</div>
                  </a-tooltip>
                </div>
              </div>
              <div class="btns">
                <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.delete')">
                    <img
                      alt=""
                      src="/@/assets/images/iot/link/device/delete-y.png"
                      @click="handleDelete(record)"
                      v-hasAnyPermission="['basic:link:otaUpgradeTasks:delete']"
                    />
                  </a-tooltip>
                </div>
                <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.copy')">
                    <img
                      alt=""
                      src="/@/assets/images/iot/link/device/copy-y.png"
                      @click="handleCopy(record)"
                      v-hasAnyPermission="['basic:link:otaUpgradeTasks:copy']"
                    />
                  </a-tooltip>
                </div>
                <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.edit')">
                    <img
                      alt=""
                      src="/@/assets/images/iot/link/device/edit-y.png"
                      @click="handleEdit(record)"
                      v-hasAnyPermission="['basic:link:otaUpgradeTasks:edit']"
                    />
                  </a-tooltip>
                </div>
                <div class="btn" style="height: 15px">
                  <a-tooltip placement="top" :title="t('common.title.details')">
                    <img
                      alt=""
                      src="/@/assets/images/iot/link/detail-icon2.png"
                      @click="handleView(record, $event)"
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
                v-if="record?.taskStatus === 1"
                src="/@/assets/images/iot/link/tasking.png"
                @click="handleView(record, $event)"
              />
              <img
                v-else-if="record?.taskStatus === 2"
                src="/@/assets/images/iot/link/tasksuccess.png"
                @click="handleView(record, $event)"
              />
              <img
                v-else-if="[3, 4].includes(record?.taskStatus)"
                src="/@/assets/images/iot/link/taskfail.png"
                @click="handleView(record, $event)"
              />
              <img
                v-else-if="record?.taskStatus === 0"
                src="/@/assets/images/iot/link/taskpending.png"
                @click="handleView(record, $event)"
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
  <EditModal @register="registerDrawer" @success="handleSuccess" />
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, watch } from 'vue';
  // util
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { useDrawer } from '/@/components/Drawer';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  // api
  import { page, deleteSingle } from '/@/api/iot/link/ota/otaUpgradeTasks';
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
  import { FileTextOutlined } from '@ant-design/icons-vue';
  import { useRouter } from 'vue-router';
  import EditModal from '/@/views/iot/link/ota/otaUpgradeTasks/Edit.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDict } from '/@/components/Dict';
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
      FileTextOutlined,
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
    },
    setup(props, { emit }) {
      const { t } = useI18n();
      const [registerDrawer, { openDrawer }] = useDrawer();
      const { push } = useRouter();
      const { createConfirm } = useMessage();
      const { hasAnyPermission } = usePermission();
      const current = ref(1);
      const size = ref(20);
      const total = ref(0);
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
          ...handleFetchParams(model),
        }).then((res: any) => {
          total.value = res.total;
          deviceList.value = res.records;
          cardLoading.value = false;
        });
      };
      // getDecviceList();

      watch(
        () => props.searchData,
        (newValue, oldValue) => {
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

      // 弹出新增页面
      function handleAdd() {
        openDrawer(true, {
          type: ActionEnum.ADD,
        });
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable) {
        // e?.stopPropagation();
        openDrawer(true, {
          record,
          type: ActionEnum.EDIT,
        });
      }

      // 点击设备
      const deviceId = ref<string>('');

      function selectItem(record: Recordable, e: Event) {
        e?.stopPropagation();
        deviceId.value = record.id;
        console.log(record, 'record');
      }

      // 弹出查看页面
      function handleView(record: Recordable) {
        if (
          !hasAnyPermission([
            'link:otaUpgradeTasks:detail:view',
            'link:otaUpgradeTasks:detail:records',
          ])
        )
          return;
        push(`/link/otaUpgradeTasks/${record.id}`);
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        getDecviceList();
      }

      function change(page: number, pageSize: number) {
        console.log(page, pageSize, size.value);
        getDecviceList();
      }

      // 删除
      function handleDelete(record) {
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async (e) => {
            try {
              await deleteSingle(record.id);
              getDecviceList();
            } catch (e) {}
          },
        });
      }

      // 修改设备状态
      function handleStatus(record, status: number) {
        // createConfirm({
        //   iconType: 'warning',
        //   content: status==1?t('common.tips.confirmEnable'):t('common.tips.confirmDeactivate'),
        //   onOk: async () => {
        //     try {
        //       await statusChange(record.id,{status: status})
        //       getDecviceList()
        //     } catch (e) { }
        //   },
        // });
      }

      // 弹出复制页面
      function handleCopy(record: Recordable, e: Event) {
        e?.stopPropagation();
        openDrawer(true, {
          record,
          type: ActionEnum.COPY,
        });
      }

      const pageSizeOptions = ref<string[]>(['10', '20', '30', '40', '50']);

      return {
        t,
        deviceList,
        cardLoading,
        switchFlag,
        switchView,
        registerDrawer,
        handleAdd,
        handleEdit,
        handleSuccess,
        handleView,
        selectItem,
        deviceId,
        Icon4,
        Icon5,
        current,
        total,
        size,
        getDecviceList,
        change,
        pageSizeOptions,
        handleDelete,
        handleStatus,
        getDictLabel,
        handleCopy,
      };
    },
  });
</script>
<style scoped lang="less">
  @import '../../../Table/src/types/components/cardCommon.less';

  .product-item {
    min-height: 220px;
    max-height: 250px;
  }
</style>
