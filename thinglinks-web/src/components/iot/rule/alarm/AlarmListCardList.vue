<template>
  <div style="background-color: #fff; padding: 6px">
    <div class="card-header" v-if="type !== 'select'">
      <span>{{ title }}</span>
      <div>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">{{
          t('common.switchView')
        }}</a-button>
      </div>
    </div>
    <div class="loading" v-if="cardLoading">
      <a-spin />
    </div>
    <div style="padding: 0 23px">
      <a-row :gutter="[24, 12]" v-if="deviceList.length > 0">
        <a-col
          v-for="record in deviceList"
          :key="record.id"
          :xs="24"
          :sm="24"
          :md="12"
          :lg="12"
          :xl="8"
          :xxl="6"
        >
          <div
            :class="{
              'product-item': true,
              normal: record.status == 1,
              error: record.status == 0,
              has_selected: hasSelected === record?.alarmIdentification,
            }"
            @click="selectItem(record, $event)"
          >
            <div class="product-info">
              <a-tooltip placement="top" :title="record.alarmName || '暂无名称'">
                <div class="title o2">{{ record.alarmName || '暂无名称' }}</div>
              </a-tooltip>
              <div class="status">{{
                getDictLabel('RULE_ALARM_STATUS', record.status + '', '')
              }}</div>
              <div class="props">
                <div class="prop">
                  <div class="label">{{ t('iot.link.engine.alarm.alarmIdentification') }}</div>
                  <div class="value">{{ record.alarmIdentification }}</div>
                </div>
                <div class="prop">
                  <div class="label">{{ t('iot.link.engine.alarm.level') }}</div>
                  <div class="value">{{
                    getDictLabel('RULE_ALARM_LEVEL', record.level + '', '')
                  }}</div>
                </div>
              </div>
              <div class="btns" v-if="type !== 'select'">
                <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.delete')">
                    <img
                      src="../../../../../../../../assets/images/iot/link/delete-y.png"
                      alt=""
                      @click="handleDelete(record)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn">
                  <img
                    src="../../../../../../../../assets/images/iot/link/device/copy-n.png"
                    alt=""
                    @click="handleCopy(record)"
                  />
                </div>
                <div class="btn">
                  <a-tooltip placement="top" :title="t('common.title.edit')">
                    <img
                      src="../../../../../../../../assets/images/iot/link/device/edit-y.png"
                      alt=""
                      @click="handleEdit(record)"
                    />
                  </a-tooltip>
                </div>
              </div>
            </div>
            <div class="product-img">
              <img
                @click="handleView(record, $event)"
                src="../../../../../../../../assets/images/iot/rule/alarm/alarm.png"
              />
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
  import { defineComponent, ref, reactive, watch } from 'vue';
  // util
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { useModal } from '/@/components/Modal';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  // api
  import { page, remove } from '../../../../../../../../api/iot/rule/alarm/alarm';
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
  import EditModal from '/@/views/iot/rule/alarm/list/Edit.vue';
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
      // 兼容选择功能
      type: {
        type: String,
        default: '',
      },
      selectedValue: {
        type: String,
        default: '',
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
      // 设备列表
      let deviceList = ref<Array<deviceItem>>([]);
      // 设备列表加载状态
      const cardLoading = ref<boolean>(false);
      // 已选择项
      const hasSelected = ref<string>('');
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
      watch(
        () => props.searchData,
        (newValue, oldValue) => {
          console.log('searchData变化了', newValue, oldValue);
          model = newValue;
          getDecviceList();
        },
        { immediate: true, deep: true },
      );
      watch(
        () => props.selectedValue,
        (val) => {
          hasSelected.value = val;
        },
        { immediate: true },
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
      // 弹出复制页面
      function handleCopy(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.COPY,
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

      function selectItem(record: Recordable, e: Event) {
        e?.stopPropagation();
        hasSelected.value = record.alarmIdentification;
        emit('handleSelect', record.alarmIdentification);
        console.log(record, 'record');
      }

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        if (props.type === 'select') return;

        // e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.VIEW,
        });
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
          onOk: async () => {
            try {
              await remove([record.id]);
              createMessage.success(t('common.tips.deleteSuccess'));
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
        openModal(true, {
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
        registerModal,
        handleAdd,
        handleEdit,
        handleSuccess,
        handleView,
        selectItem,
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
        hasSelected,
      };
    },
  });
</script>
<style scoped lang="less">
  @import '../../../cardCommon.less';

  .has_selected {
    border: 1px solid #1a66ff;
  }
</style>
