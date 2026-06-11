<template>
  <div style="background-color: #fff; padding: 6px">
    <div v-if="loading" class="loading">
      <a-spin />
    </div>
    <div class="card-header">
      <span class="title">{{ title }}</span>
      <div class="btn_group">
        <a-button
          preIcon="ant-design:plus-outlined"
          type="primary"
          @click="handleAdd"
          v-hasAnyPermission="['rule:plugin:pluginInstance:add']"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">{{
          t('common.switchView')
        }}</a-button>
      </div>
    </div>
    <div style="padding: 0 23px">
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
            style="padding: 10px 0"
            class="normal"
            :class="{
              'product-item': true,
            }"
          >
            <div class="product-info">
              <a-tooltip placement="top" :title="record.instanceName || '未命名'">
                <div
                  style="
                    font-weight: bold;
                    font-size: 16px;
                    max-width: 350px;
                    text-overflow: ellipsis;
                    overflow: hidden;
                    white-space: nowrap;
                  "
                  >{{ record.instanceName || '未命名' }}</div
                >
              </a-tooltip>

              <div class="props">
                <div class="prop" style="margin-bottom: 5px">
                  <div class="label">{{ t('iot.rule.plugin.pluginInstance.machineIp') }}</div>
                  <div class="content" :title="record.machineIp">
                    {{ record.machineIp }}
                  </div>
                </div>
                <div class="prop" style="margin-bottom: 5px">
                  <div class="label">{{
                    t('iot.rule.plugin.pluginInstance.instanceIdentification')
                  }}</div>
                  <div
                    style="
                      min-width: 200px;
                      text-overflow: ellipsis;
                      overflow: hidden;
                      white-space: nowrap;
                    "
                    class="content"
                    :title="record.instanceIdentification"
                    >{{ record.instanceIdentification }}</div
                  >
                </div>
                <div class="prop" style="margin-bottom: 5px">
                  <div class="label">{{ t('iot.rule.plugin.pluginInstance.createdTime') }}</div>
                  <div class="content" :title="record.createdTime">{{ record.createdTime }}</div>
                </div>
              </div>
              <div class="btns">
                <div class="btn" v-hasAnyPermission="['rule:plugin:pluginInstance:delete']">
                  <a-tooltip placement="top" :title="t('common.title.delete')">
                    <img
                      alt=""
                      src="../../../../../../../../assets/images/iot/link/device/delete-y.png"
                      @click="handleDelete(record)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn" v-hasAnyPermission="['rule:plugin:pluginInstance:edit']">
                  <a-tooltip placement="top" :title="t('common.title.edit')">
                    <img
                      alt=""
                      src="../../../../../../../../assets/images/iot/link/device/edit-y.png"
                      @click="handleEdit(record)"
                    />
                  </a-tooltip>
                </div>
              </div>
            </div>
            <div class="product-img">
              <img
                src="../../../../../../../../assets/images/iot/rule/plugin/instance.png"
                alt=""
                @click="handleView(record)"
            /></div>
          </div>
        </a-col>
      </a-row>
      <a-empty v-else />
      <div class="tr">
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
    </div>
  </div>
  <EditModal @register="registerModal" @success="handleSuccess" />
</template>
<script setup lang="ts">
  import { ref, reactive, watch, watchEffect } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { useDict } from '/@/components/Dict';
  import { useRouter } from 'vue-router';
  import { page, remove } from '/@/api/iot/rule/plugin/pluginInstance';
  import EditModal from '/@/views/iot/rule/plugin/pluginInstance/Edit.vue';
  import type { PluginInstanceUpdateVO } from '/@/api/iot/rule/plugin/model/pluginInstanceModel';

  const props = defineProps({ title: String, searchData: { type: Object, default: () => {} } });
  const emit = defineEmits(['input']);

  const { t } = useI18n();
  const { push } = useRouter();
  const { getDictLabel } = useDict();
  const { notification, createConfirm } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const pageSizeOptions = ref(['10', '20', '30', '40', '50']);

  const current = ref<number>(1);
  const pageSize = ref<number>(20);
  const total = ref<number>(0);
  const dataList = ref<PluginInstanceUpdateVO[]>([]);
  const model = reactive<PluginInstanceUpdateVO>({});
  const loading = ref<boolean>(false);

  // 获取列表
  const getList = async (current: number, size: number) => {
    loading.value = true;
    const res = await page({
      current,
      size,
      ...handleFetchParams(model.value ?? {}),
    });
    total.value = res.total;
    dataList.value = res.records;
    loading.value = false;
  };

  const handleChangePagination = (page: number, size: number) => {
    current.value = page;
    pageSize.value = size;
  };

  const handleAdd = () => {
    openModal(true, {
      type: ActionEnum.ADD,
    });
  };

  const handleDelete = async (record) => {
    createConfirm({
      iconType: 'warning',
      content: t('common.tips.confirmDelete'),
      onOk: async (e) => {
        try {
          await remove([record.id]);
          notification.success({
            message: t('common.tips.tips'),
            description: t('common.tips.deleteSuccess'),
          });
          getList(current.value, pageSize.value);
        } catch (e) {
          throw new Error(e);
        }
      },
    });
  };

  function handleEdit(record: PluginInstanceUpdateVO) {
    openModal(true, {
      record,
      type: ActionEnum.EDIT,
    });
  }

  function handleView(record, e: MouseEvent) {
    e?.stopPropagation();
    push({
      name: '插件实例详情',
      params: {
        id: record.id,
      },
    });
  }

  const handleSuccess = () => {
    getList(current.value, pageSize.value);
  };

  watch(
    () => props.searchData,
    () => {
      model.value = props.searchData;
      current.value = 1;
      pageSize.value = 20;
      getList(current.value, pageSize.value);
    },
    { deep: true, immediate: true },
  );

  const switchView = () => {
    emit('input', false);
  };
</script>
<style lang="less" scoped>
  @import '../../../cardCommon.less';
</style>
