<template>
  <div class="bg-white m-4 mr-2 overflow-hidden">
    <Description
      :title="t('iot.link.group.deviceGroup.deviceGroupDetail.groupDevice')"
      :column="2"
      :data="baseInfo"
      :schema="baseInfoField"
    />
    <BasicTable @register="registerTable">
      <template #toolbar>
        <div class="table-custom-action">
          <!-- <a-button
            class="add-btn"
            type="primary"
            preIcon="ant-design:plus-outlined"
            @click="handleAdd"
            v-hasAnyPermission="['link:group:deviceGroupRel:add']"
          >
            {{ t('common.title.add') }}
          </a-button> -->
          <a-input-search
            class="device-search"
            v-model:value="keyword"
            enter-button
            :placeholder="t('iot.link.group.deviceGroup.search.devicePlaceholder')"
            @search="reload"
          />
        </div>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            v-if="!record.editable"
            :actions="[
              {
                auth: RoleEnum.LINK_DEVICE_DEVICE_VIEW,
                label: t('common.title.view'),
                onClick: handleView.bind(null, record),
              },
              {
                auth: RoleEnum.LINK_GROUP_DEVICEGROUPREL_EDIT,
                label: t('common.title.edit'),
                onClick: handleEdit.bind(null, record),
              },
              {
                auth: RoleEnum.LINK_GROUP_DEVICEGROUPREL_DELETE,
                label: t('common.title.delete'),
                color: 'error',
                onClick: handleDelete.bind(null, record),
              },
            ]"
          />
          <div v-else class="flex gap-2">
            <a-button type="primary" size="small" @click="handleSave(record)">
              {{ t('common.saveText') }}
            </a-button>
            <a-button size="small" @click="handleCancel(record)">
              {{ t('common.cancelText') }}
            </a-button>
          </div>
        </template>
      </template>
    </BasicTable>
    <!-- <BaseSelectDevice
      @register="registerModal"
      modalTitle="选择产品和设备"
      :hasProductStep="true"
      :hasDeviceStep="true"
      @success="handleComplete"
    /> -->
  </div>
</template>
<script lang="ts" setup>
  // util、hooks
  import { computed, ref, defineExpose } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { useModal } from '/@/components/Modal';
  // enum
  import { RoleEnum } from '/@/enums/roleEnum';
  // components
  import { BasicTable, TableAction, useTable } from '/@/components/Table';
  import { Description } from '/@/components/Description/index';
  // data
  import { getGroupBaseInfoField, getGroupDeviceBaseTableColumn } from './deviceGroupDetail.data';
  // api
  import { detail } from '/@/api/iot/link/group/deviceGroup';
  import { page, remove, update } from '/@/api/iot/link/group/deviceGroupRel';

  const { t } = useI18n();
  const { createMessage, createConfirm, notification } = useMessage();
  const { push } = useRouter();
  // 分组信息
  const currentGroup = ref({});
  // 基本信息字段
  const baseInfoField = computed(() => {
    return getGroupBaseInfoField();
  });
  // 基本信息
  const baseInfo = ref({});
  // 设备列表字段信息
  const baseTableColumn = computed(() => {
    return getGroupDeviceBaseTableColumn();
  });

  const keyword = ref('');
  const handleFetchParams = (params) => {
    const model = {
      groupId: currentGroup.value.id,
    };
    if (keyword.value) {
      model.deviceIdentification = keyword.value;
    }
    return {
      ...params,
      model,
    };
  };
  // table信息
  const [registerTable, { reload, getDataSource }] = useTable({
    title: t('iot.link.group.deviceGroup.deviceGroupDetail.linkDevice'),
    api: page,
    columns: baseTableColumn.value,
    beforeFetch: handleFetchParams,
    immediate: false,
    showTableSetting: true,
    bordered: true,
    rowKey: 'id',
    actionColumn: {
      width: 140,
      title: t('common.column.action'),
      dataIndex: 'action',
    },
  });

  // 加载数据
  async function fetchDetail(group) {
    const { id } = group;
    if (!id) {
      return createMessage.warning(t('iot.link.group.deviceGroup.deviceGroupDetail.queryFail'));
    }
    currentGroup.value = group;
    baseInfo.value = await detail(id);
    reload();
  }

  const handleView = (record) => {
    push({
      name: '设备详情',
      params: { id: record.deviceId },
    });
  };

  const handleEdit = (record) => {
    record.onEdit?.(true);
  };

  const handleSave = async (record) => {
    const valid = await record.onValid?.();
    if (!valid) {
      return false;
    }
    try {
      await update({
        id: record.id,
        groupId: currentGroup.value.id,
        remark: record.editValueRefs.remark,
      });
      notification.success({ message: t('common.tips.updateSuccess') });
      // 第二个参数 true 表示提交编辑数据到表格
      record.onEdit?.(false, true);
      return true;
    } catch (e) {
      return false;
    }
  };

  const handleCancel = (record) => {
    record.onEdit?.(false, false);
  };

  const handleDelete = (record) => {
    createConfirm({
      iconType: 'warning',
      content: t('common.tips.confirmDelete'),
      onOk: async () => {
        try {
          await remove([record.id]);
          notification.success({ message: t('common.tips.deleteSuccess') });
          reload();
        } catch (e) {}
      },
    });
  };

  const [registerModal, { openModal }] = useModal();

  const handleAdd = () => {
    console.log('add');
    openModal(true);
  };
  const handleComplete = (data) => {
    // deviceIdentification
    const selectedDeviceIds = data.selectedDevices.map((item) => item.id);
  };

  defineExpose({
    fetchDetail,
  });
</script>
<style lang="less" scoped>
  .table-custom-action {
    width: 85%;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .add-btn {
      margin-left: 12px;
    }

    .device-search {
      width: 30%;
      min-width: 250px;
    }
  }
</style>
