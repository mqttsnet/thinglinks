<template>
  <div style="background-color: #fff; padding: 6px">
    <div class="card-header">
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
          :xxl="8"
        >
          <div
            :class="{
              'product-item': true,
              normal: record.enable,
              error: !record.enable,
            }"
            @click="selectItem(record, $event)"
          >
            <div class="product-info">
              <div class="status">{{ record.enable ? t('thinglinks.common.enable') :  t('thinglinks.common.disable') }}</div>
              <a-tooltip placement="top" :title="record.name">
                <div class="title">{{ record.name }}</div>
              </a-tooltip>
              <div class="props">
                <div class="prop">
                  <div class="label">{{ t('iot.rule.groovy.ruleGroovyScript.appId') }}</div>
                  <div class="value">{{ record.appId }}</div>
                </div>
                <div class="prop">
                  <div class="label">{{ t('iot.rule.groovy.ruleGroovyScript.namespace') }}</div>
                  <div class="value">{{
                    getDictLabel('RULE_GROOVY_SCRIPT_NAMESPACE_TYPE', record.namespace + '', '')
                  }}</div>
                </div>
              </div>
              <div class="btns">
                <div
                  class="btn btn_chain"
                  v-hasAnyPermission="['rule:groovy:ruleGroovyScript:delete']"
                >
                  <a-tooltip placement="top" :title="t('common.title.delete')">
                    <img
                      src="../../../../../../../../assets/images/iot/link/delete-y.png"
                      alt=""
                      @click="handleDelete(record)"
                    />
                  </a-tooltip>
                </div>
                <div
                  class="btn btn_chain"
                  v-hasAnyPermission="['rule:groovy:ruleGroovyScript:view']"
                >
                  <a-tooltip placement="top" :title="t('common.title.view')">
                    <img
                      src="../../../../../../../../assets/images/iot/link/go-details-2.png"
                      alt=""
                      @click="handleView(record, $event)"
                    />
                  </a-tooltip>
                </div>
                <div
                  class="btn btn_chain"
                  v-hasAnyPermission="['rule:groovy:ruleGroovyScript:edit']"
                >
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
                src="../../../../../../../../assets/images/iot/rule/groovy/scriptCard.png"
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
  import { page, deleteSingle } from '/@/api/iot/rule/groovy/ruleGroovyScript';
  // import { columns, searchFormSchema } from './ruleGroovyScript.data';
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
  import EditModal from '/@/views/iot/rule/groovy/ruleGroovyScript/Edit.vue';
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
    },
    setup(props, { emit }) {
      const { t } = useI18n();
      const [registerModal, { openModal }] = useModal();
      const { push } = useRouter();
      const current = ref(1);
      const size = ref(20);
      const total = ref(0);
      const { createConfirm, notification } = useMessage();
      let model = reactive({});
      // 设备列表
      let deviceList = ref<Array<deviceItem>>([]);
      // 设备列表加载状态
      const cardLoading = ref<boolean>(false);
      const getRuleGroovyScriptList = () => {
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
          model = newValue;
          getRuleGroovyScriptList();
        },
        { immediate: true, deep: true },
      );
      // 切换视图 卡片&&列表
      const switchFlag = ref<boolean>(true);
      function switchView() {
        switchFlag.value = !switchFlag.value;
        getRuleGroovyScriptList();
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

      // 点击设备
      const deviceId = ref<string>('');
      function selectItem(record: Recordable, e: Event) {
        e?.stopPropagation();
        deviceId.value = record.id;
      }

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        push({
          name: '规则脚本详情',
          params: {
            id: record.id,
          },
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        getRuleGroovyScriptList();
      }

      function change(page: number, pageSize: number) {
        console.log(page, pageSize, size.value);
        getRuleGroovyScriptList();
      }
      // 删除
      function handleDelete(record) {
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            try {
              await deleteSingle(record.id);
              notification.success({
                message: '提示',
                description: t('common.tips.deleteSuccess'),
              });
              getRuleGroovyScriptList();
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
        //       getRuleGroovyScriptList()
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
        deviceId,
        Icon4,
        Icon5,
        current,
        total,
        size,
        getRuleGroovyScriptList,
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
<style lang="less" scoped>
  @import '../../../cardCommon.less';
</style>
