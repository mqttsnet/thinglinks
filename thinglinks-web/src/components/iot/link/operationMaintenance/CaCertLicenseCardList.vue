<template>
  <div style="background-color: #fff; padding: 6px">
    <div class="card-header">
      <span>{{ title }}</span>
      <div>
        <a-button preIcon="ant-design:arrow-up-outlined" type="primary" @click="handleImport">
          {{ t('iot.link.operationMaintenance.cacert.caCertLicense.importTitle') }}
        </a-button>
        <!-- <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">
          {{ t('common.title.add') }}
        </a-button> -->
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">{{
          t('common.switchView')
        }}</a-button>
      </div>
    </div>
    <div v-if="cardLoading" class="loading">
      <a-spin />
    </div>
    <div style="padding: 0 23px">
      <a-row v-if="caCertLicenseList.length" :gutter="[24, 12]">
        <a-col
          v-for="record in caCertLicenseList"
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
              normal: record.enabled,
              error: !record.enabled,
            }"
          >
            <div class="product-info">
              <div class="status">{{ record.enabled ? t('iot.link.device.device.enable') : t('thinglinks.common.disable') }} </div>
              <a-tooltip placement="top" :title="record.certName">
                <div class="title">{{ record.certName }}</div>
              </a-tooltip>
              <div class="props">
                <div class="flex">
                  <div class="prop">
                    <div class="label">{{
                      t('iot.link.operationMaintenance.cacert.caCertLicense.serialNumber')
                    }}</div>
                    <div class="value" style="max-width: 220px" :title="record.serialNumber">
                      {{ record.serialNumber }}
                    </div>
                  </div>
                </div>
                <div class="prop">
                  <div class="label">{{
                    t('iot.link.operationMaintenance.cacert.caCertLicense.expirationDate')
                  }}</div>
                  <div class="value">{{ record.notBefore + '-' + record.notAfter }}</div>
                </div>
              </div>
              <div class="btns" style="bottom: 10px">
                <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.delete')">
                    <img
                      alt=""
                      src="../../../../../../../../assets/images/iot/link/device/delete-y.png"
                      @click="handleDelete(record)"
                    />
                  </a-tooltip>
                </div>
                <!-- <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.edit')">
                    <img
                      alt=""
                      src="../../../../../../../../assets/images/iot/link/device/edit-y.png"
                      @click="handleEdit(record)"
                    />
                  </a-tooltip>
                </div> -->
              </div>
            </div>
            <div class="product-img">
              <img
                src="/@/assets/images/iot/link/operationMaintenance/cacert/caCertLicense/ca-cert-license.jpg"
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
  <ImportModal @register="registerModal" @success="handleSuccess" />
  <!-- <EditModal @register="registerModal" @success="handleSuccess" /> -->
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, watch } from 'vue';
  // util
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { useModal } from '/@/components/Modal';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  // api
  import { page, remove } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';
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
  import { useRouter } from 'vue-router';
  import EditModal from '/@/views/iot/link/operationMaintenance/accessControl/deviceAclRule/Edit.vue';
  import ImportModal from '/@/views/iot/link/operationMaintenance/cacert/caCertLicense/ImportModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDict } from '/@/components/Dict';

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
  export default defineComponent({
    name: 'CardList',
    components: {
      ACard: Card,
      ARow: Row,
      ACol: Col,
      AButton: Button,
      ASpin: Spin,
      EditModal,
      ImportModal,
      ADivider: Divider,
      ATooltip: Tooltip,
      APagination: Pagination,
      AUpload: Upload,
      AEmpty: Empty,
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
      const [registerModal, { openModal }] = useModal();
      const { push } = useRouter();
      const current = ref(1);
      const size = ref(20);
      const total = ref(0);
      const { createMessage, createConfirm } = useMessage();
      let model = reactive({});
      // ac许可证书列表
      let caCertLicenseList = ref<Array<deviceItem>>([]);
      // ac许可证书列表加载状态
      const cardLoading = ref<boolean>(false);
      const getList = () => {
        cardLoading.value = true;
        page({
          current: current.value,
          size: size.value,
          ...handleFetchParams(model),
        }).then((res: any) => {
          total.value = res.total;
          caCertLicenseList.value = res.records;
          cardLoading.value = false;
        });
      };
      watch(
        () => props.searchData,
        (newValue, oldValue) => {
          model = newValue;
          getList();
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
        openModal(true, {
          type: ActionEnum.ADD,
        });
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable) {
        // e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.EDIT,
        });
      }

      // 弹出导入页面
      function handleImport() {
        openModal(true);
      }

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        push({
          name: 'CA许可证证书详情',
          params: { id: record.id },
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        getList();
      }

      function change(page: number, pageSize: number) {
        console.log(page, pageSize, size.value);
        getList();
      }

      // 删除
      function handleDelete(record) {
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async (e) => {
            try {
              await remove([record.id]);
              getList();
            } catch (e) {}
          },
        });
      }

      const pageSizeOptions = ref<string[]>(['10', '20', '30', '40', '50']);

      return {
        t,
        caCertLicenseList,
        cardLoading,
        switchFlag,
        switchView,
        registerModal,
        handleAdd,
        handleEdit,
        handleSuccess,
        handleView,
        handleImport,
        current,
        total,
        size,
        getList,
        change,
        pageSizeOptions,
        handleDelete,
        getDictLabel,
      };
    },
  });
</script>
<style scoped>
  @import '../../../cardCommon.less';
</style>
