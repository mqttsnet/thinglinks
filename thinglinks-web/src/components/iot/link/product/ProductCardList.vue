<template>
  <div style="background-color: #fff; padding: 6px">
    <div class="card-header" v-if="!isSelect">
      <span>{{ title }}</span>
      <div>
        <a-button
          v-hasPermission="['link:product:product:add']"
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button
          v-hasPermission="['link:product:product:quick']"
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="onQuick"
        >
          {{ t('iot.link.device.device.quick') }}
        </a-button>
        <a-button
          v-hasPermission="['link:product:product:import']"
          type="primary"
          preIcon="ant-design:download-outlined"
          @click="handleImport"
          >{{ t('common.title.import') }}</a-button
        >
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">{{
          t('common.switchView')
        }}</a-button>
      </div>
    </div>
    <div v-if="isSelect">
      <BasicForm @register="register" @submit="handleSearch" submitOnReset />
    </div>
    <div class="loading" v-if="cardLoading">
      <a-spin />
    </div>
    <div style="padding: 0 23px">
      <a-row :gutter="[24, 12]" v-if="deviceList.length">
        <a-col
          v-for="record in deviceList"
          :key="record.id"
          :xs="isSelect ? 12 : 24"
          :sm="isSelect ? 12 : 24"
          :md="12"
          :lg="12"
          :xl="isSelect ? 12 : 8"
          :xxl="isSelect ? 12 : 6"
        >
          <div
            :class="{
              'product-item': true,
              'product-item--select': isSelect,
              normal: record.productStatus == 0,
              error: record.productStatus == 1,
            }"
            @click="selectItem(record, $event)"
          >
            <div class="product-info">
              <div class="status">{{
                getDictLabel('LINK_PRODUCT_STATUS', record.productStatus + '', '')
              }}</div>
              <a-tooltip placement="top" :title="record.productName">
                <div class="title o2">{{ record.productName }}</div>
              </a-tooltip>
              <div class="props">
                <div class="flex" style="justify-content: space-between">
                  <div class="prop">
                    <div class="label">{{ t('iot.link.product.product.productType') }}</div>
                    <div class="value">{{
                      getDictLabel('LINK_PRODUCT_TYPE', record.productType, '')
                    }}</div>
                  </div>
                  <div class="prop">
                    <div class="label">{{ t('iot.link.product.product.protocolType') }}</div>
                    <div class="value">{{ record.protocolType }}</div>
                  </div>
                </div>
                <div class="prop">
                  <div class="label">{{ t('iot.link.product.product.productIdentification') }}</div>
                  <div class="value">{{ record.productIdentification }}</div>
                </div>
              </div>
              <div class="btns" v-if="!isSelect">
                <div class="btn" v-hasPermission="['link:product:product:delete']">
                  <a-tooltip placement="top" :title="t('common.title.delete')">
                    <Icon
                      icon="ant-design:delete-outlined"
                      :size="16"
                      class="action-icon action-icon--danger"
                      @click.stop="handleDelete(record)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn" v-hasPermission="['link:product:product:copy']">
                  <a-tooltip placement="top" :title="t('common.title.copy')">
                    <Icon
                      icon="ant-design:copy-outlined"
                      :size="16"
                      class="action-icon"
                      @click.stop="handleCopy(record, $event)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn" v-hasPermission="['link:product:product:export']">
                  <a-tooltip placement="top" :title="t('common.title.export')">
                    <Icon
                      icon="ant-design:export-outlined"
                      :size="16"
                      class="action-icon"
                      @click.stop="handleExport(record)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn" v-hasPermission="['link:product:product:edit']">
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
            <div class="product-img" @click="handleView(record, $event)">
              <!-- 优先用产品自定义 icon,无则按 productType 回退到内联 SVG(与产品列表一致) -->
              <ThumbUrl
                v-if="record?.icon"
                :fileId="record.icon"
                :imageStyle="{ 'max-width': '90px', 'max-height': '90px' }"
              />
              <component v-else :is="getProductTypeSvg(record?.productType)" />
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

    <EditModal @register="registerModal" @success="handleSuccess" />
    <ImportModal @register="importModal" @reload="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, watch, toRefs, reactive } from 'vue';
  // util
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { useModal } from '/@/components/Modal';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { downloadFile } from '/@/utils/file/download.ts';
  // api
  import { page, deleteSingle, exportJson } from '/@/api/iot/link/product/product';
  // components
  import { Row, Col, Button, Spin, Tooltip, Pagination } from 'ant-design-vue';
  import { useRouter } from 'vue-router';
  import EditModal from '/@/views/iot/link/product/product/Edit.vue';
  import ImportModal from '/@/views/iot/link/product/product/ImportModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { useDict } from '/@/components/Dict';
  import { getProductTypeSvg } from '/@/components/iot/svg';
  import ThumbUrl from '/@/components/Upload/src/ThumbUrl.vue';
  import { BasicForm, useForm } from '/@/components/Form';
  import { productSearchSchema } from '/@/components/Table/src/types/components/PublicSearchSchema';
  import { cleanInput } from '/@/utils';
  import { Icon } from '/@/components/Icon';

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
  export default defineComponent({
    name: 'CardList',
    components: {
      ARow: Row,
      ACol: Col,
      AButton: Button,
      ASpin: Spin,
      EditModal,
      ImportModal,
      ATooltip: Tooltip,
      APagination: Pagination,
      BasicForm,
      ThumbUrl,
      Icon,
    },
    props: {
      title: {
        type: String,
        default: '列表',
      },
      isSelect: {
        type: Boolean,
        default: false,
      },
      searchData: {
        type: Object,
        default: () => ({}),
      },
      productIdentification: {
        type: String,
        default: '',
      },
    },
    setup(props, { emit }) {
      const { t } = useI18n();
      const { hasPermission } = usePermission();
      const [registerModal, { openModal }] = useModal();
      const [importModal, { openModal: openImportModal }] = useModal();
      const { push } = useRouter();
      const current = ref(1);
      const size = ref(20);
      const total = ref(0);
      const { createMessage, createConfirm } = useMessage();
      const [register, { getFieldsValue }] = useForm({
        labelWidth: 120,
        schemas: productSearchSchema(),
        showAdvancedButton: true,
      });

      // 设备列表
      let deviceList = ref<Array<deviceItem>>([]);
      let model = reactive({});
      let state = reactive({
        productIdentification: props.productIdentification,
      });
      // 设备列表加载状态
      const cardLoading = ref<boolean>(false);
      const getDecviceList = (searchValue = {}) => {
        cardLoading.value = true;
        page({
          current: current.value,
          size: size.value,
          ...handleFetchParams({ ...searchValue, ...model }),
        }).then((res: any) => {
          total.value = res.total;
          deviceList.value = res.records;
          cardLoading.value = false;
        });
      };
      function onQuick(e: Event) {
        e?.stopPropagation();
        push({
          name: '快捷生成',
        });
      }
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

      const selectProductCard = (column) => {
        emit('selectProductCard', column);
      };
      // 点击产品
      function selectItem(record: Recordable, e: Event) {
        e?.stopPropagation();
        state.productIdentification = record.productIdentification;
        // 如果选择，选中改项
        if (props.isSelect) {
          selectProductCard(record);
        }
      }

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        state.productIdentification = record.productIdentification;
        // 如果选择，选中改项
        if (props.isSelect) {
          selectProductCard(record);
        } else {
          if (!hasPermission('link:product:product:view')) {
            createMessage.warning(t('sys.api.operationFailed'));
            return;
          }
          push({
            name: '产品详情',
            params: { id: record.id },
          });
        }
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
              await deleteSingle(record.id);
              createMessage.success(t('common.tips.deleteSuccess'));
              getDecviceList();
            } catch (e) {}
          },
        });
      }

      // 修改设备状态
      function handleStatus(_record, _status: number) {
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

      // 导出JSON
      async function handleExport(record: Recordable) {
        try {
          const response = await exportJson(record.productIdentification);
          downloadFile(response);
          createMessage.success(t('common.tips.exportSuccess'));
        } catch (error) {
          console.error('导出失败:', error);
          createMessage.error(t('common.tips.exportFail'));
        }
      }

      const pageSizeOptions = ref<string[]>(['10', '20', '30', '40', '50']);

      const handleImport = () => {
        openImportModal(true);
      };

      /**
       * 单独调用卡片页面时顶部筛选form方法
       */
      const handleSearch = async () => {
        const values = await getFieldsValue();
        getDecviceList(cleanInput(values));
        console.log('handleSearch', values);
      };
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
        getProductTypeSvg,
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
        handleExport,
        onQuick,
        register,
        handleSearch,
        importModal,
        handleImport,
        ...toRefs(state),
      };
    },
  });
</script>
<style scoped lang="less">
  @import '../../../Table/src/types/components/cardCommon.less';

  /* 产品图标:容器给定 90×90;内联 SVG 是子组件根节点,需 :deep 穿透 scope 才能命中并撑满 */
  .product-img {
    width: 90px;
    height: 90px;
  }

  .product-img :deep(svg) {
    display: block;
    width: 100%;
    height: 100%;
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
