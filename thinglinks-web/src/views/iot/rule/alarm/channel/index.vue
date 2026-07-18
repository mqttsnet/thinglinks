<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable @register="registerTable" @switch-change="getSwitchChange" :switchFlag="switchFlag">
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="pageWithChannelTypeLabel"
          :title="title"
          :searchData="searchData"
          nameField="channelName"
          :nameFallback="t('iot.link.engine.channel.table.title')"
          :fields="cardFields"
          statusField="status"
          :statusResolver="resolveChannelStatus"
          :extraActions="cardExtraActions"
          @input="getSwitchChange"
          @extra-action="handleCardExtraAction"
        >
          <template #headerExtra>
            <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
              {{ t('common.title.add') }}
            </a-button>
          </template>
          <template #cardImage>
            <AlarmChannelSvg />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          disabled
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView"
          >{{ t('iot.link.device.device.switchView') }}
        </a-button>
      </template>
      <template #channelType="{ record }">
        {{ resolveAlarmChannelTypeLabel(record?.channelType) }}
      </template>
      <template #status="{ record }">
        {{ getDictLabel('RULE_ALARM_CHANNEL_STATUS', record?.status, '') }}
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                tooltip: t('common.title.edit'),
                icon: 'ant-design:edit-outlined',
                onClick: handleEdit.bind(null, record),
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                popConfirm: {
                  title: t('common.tips.confirmDelete'),
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
            :stopButtonPropagation="true"
          />
        </template>
      </template>
    </BasicTable>
    <EditModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardAction } from '/@/components/BusinessCardList';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { page, remove, deleteSingle } from '../../../../../api/iot/rule/alarm/channel';
  import {
    columns,
    resolveAlarmChannelTypeLabel,
    searchFormSchema,
    cardFields as buildCardFields,
  } from './channel.data';
  import EditModal from './Edit.vue';
  import { useDict } from '/@/components/Dict';
  import { AlarmChannelSvg } from '/@/components/iot/svg';
  const { getDictLabel } = useDict();

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '渠道管理',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      BusinessCardList,
      EditModal,
      AlarmChannelSvg,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const cardListRef = ref<any>(null);
      const cardFields = buildCardFields();
      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('common.title.edit'),
          icon: 'ant-design:edit-outlined',
          event: 'edit',
        },
        {
          tooltip: t('common.title.delete'),
          icon: 'ant-design:delete-outlined',
          color: 'error',
          event: 'delete',
        },
      ];

      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('iot.link.engine.channel.table.title'),
        api: pageWithChannelTypeLabel,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'ChannelSearch',
          labelWidth: 96,
          schemas: searchFormSchema(),
          compact: true,
          autoSubmitOnEnter: true,
          resetButtonOptions: {
            preIcon: 'ant-design:rest-outlined',
          },
          submitButtonOptions: {
            preIcon: 'ant-design:search-outlined',
          },
        },
        beforeFetch: handleFetchParams,
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        rowKey: 'id',
        rowSelection: {
          type: 'checkbox',
          columnWidth: 40,
        },
        actionColumn: {
          width: 200,
          title: t('common.column.action'),
          dataIndex: 'action',
        },
      });

      // 弹出新增页面
      function handleAdd() {
        openModal(true, {
          type: ActionEnum.ADD,
        });
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.EDIT,
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
      }

      // 删除单条数据
      const handleDeleteSingle = async (id: string) => {
        await deleteSingle(id);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      };

      async function batchDelete(ids: string[]) {
        await remove(ids);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      }

      // 点击单行删除
      function handleDelete(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (record?.id) {
          handleDeleteSingle(record.id);
        }
      }

      function handleCardDelete(record: Recordable) {
        if (!record?.id) return;
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: () => handleDeleteSingle(record.id),
        });
      }

      function handleCardExtraAction(payload: { event: string; record: Recordable }) {
        const event = new Event('synthetic');
        if (payload.event === 'edit') {
          handleEdit(payload.record, event);
          return;
        }
        if (payload.event === 'delete') {
          handleCardDelete(payload.record);
        }
      }

      function resolveChannelStatus(record: Recordable) {
        const label = getDictLabel('RULE_ALARM_CHANNEL_STATUS', record?.status, '-');
        return {
          label,
          cls: Number(record?.status) === 1 ? 'online' : 'offline',
        };
      }

      async function pageWithChannelTypeLabel(...args: any[]) {
        const result = await page(...args);
        const records = result?.records || result?.data?.records || [];
        if (Array.isArray(records)) {
          records.forEach((record) => {
            record.channelTypeLabel = resolveAlarmChannelTypeLabel(record?.channelType);
          });
        }
        return result;
      }

      // 点击批量删除
      function handleBatchDelete() {
        const ids = getSelectRowKeys();
        if (!ids || ids.length <= 0) {
          createMessage.warning(t('common.tips.pleaseSelectTheData'));
          return;
        }
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            try {
              await batchDelete(ids);
            } catch (e) {}
          },
        });
      }
      // 切换视图 卡片&&列表
      const switchFlag = ref<boolean>(true);
      function switchView() {
        console.log('switchView');
        switchFlag.value = !switchFlag.value;
      }
      function getSwitchChange(e) {
        switchFlag.value = e;
      }
      watch(switchFlag, (newValue) => {
        if (newValue === false) {
          reload();
        }
      });
      return {
        t,
        registerTable,
        registerModal,
        handleAdd,
        handleEdit,
        handleDelete,
        handleBatchDelete,
        handleSuccess,
        getDictLabel,
        switchView,
        getSwitchChange,
        switchFlag,
        cardFields,
        cardExtraActions,
        cardListRef,
        handleCardExtraAction,
        resolveChannelStatus,
        page: pageWithChannelTypeLabel,
        pageWithChannelTypeLabel,
        resolveAlarmChannelTypeLabel,
      };
    },
  });
</script>
