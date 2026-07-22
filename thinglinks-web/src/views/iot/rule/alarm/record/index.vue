<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable @register="registerTable" @switch-change="getSwitchChange" :switchFlag="switchFlag">
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="alarmRecordPage"
          :title="title"
          :searchData="searchData"
          nameField="alarmRecordTitle"
          :nameFallback="t('iot.link.engine.alarmRecord.table.title')"
          :fields="cardFields"
          statusField="handledStatus"
          :statusResolver="resolveRecordStatus"
          :extraActions="cardExtraActions"
          @input="getSwitchChange"
          @extra-action="handleCardExtraAction"
        >
          <template #cardImage>
            <AlarmRecordSvg />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView"
          >{{ t('iot.link.device.device.switchView') }}
        </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                tooltip: t('common.title.view'),
                icon: 'ant-design:search-outlined',
                auth: 'rule:alarm:record:detail',
                onClick: handleView.bind(null, record),
              },
              // {
              //   tooltip: t(record.handledStatus == 0 ? 'common.title.handle' : 'common.title.goResolved'),
              //   icon: 'ant-design:edit-outlined',
              //   disabled: record.handledStatus == 2 ? true : false,
              //   onClick: handleEdit.bind(null, record),
              // },
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
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardAction } from '/@/components/BusinessCardList';
  import { useRouter } from 'vue-router';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { page, remove, deleteSingle } from '../../../../../api/iot/rule/alarm/record';
  import {
    columns,
    searchFormSchema,
    cardFields as buildCardFields,
    decorateAlarmRecordPageResult,
  } from './alarmRecord.data';
  import { useDict } from '/@/components/Dict';
  import { AlarmRecordSvg } from '/@/components/iot/svg';
  const { getDictLabel } = useDict();

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '告警记录',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      BusinessCardList,
      AlarmRecordSvg,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const { replace } = useRouter();
      const cardListRef = ref<any>(null);
      const cardFields = buildCardFields();
      const alarmRecordPage = async (...args: Parameters<typeof page>) => {
        const res = await page(...args);
        return decorateAlarmRecordPageResult(res);
      };
      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('common.title.view'),
          icon: 'ant-design:search-outlined',
          event: 'view',
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
        title: t('iot.link.engine.alarmRecord.table.title'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'AlarmRecordSearch',
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

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        replace({
          name: '告警记录详情',
          params: { id: record.id },
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
      }

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
        if (payload.event === 'view') {
          handleView(payload.record, event);
          return;
        }
        if (payload.event === 'delete') {
          handleCardDelete(payload.record);
        }
      }

      function resolveRecordStatus(record: Recordable) {
        const status = Number(record?.handledStatus);
        const label = getDictLabel('RULE_ALARM_RECORD_HANDLED_STATUE', record?.handledStatus, '-');
        if (status === 2) {
          return { label, cls: 'online' };
        }
        if (status === 1) {
          return { label, cls: 'offline' };
        }
        return { label, cls: 'danger' };
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
        handleView,
        handleDelete,
        handleBatchDelete,
        handleSuccess,
        switchView,
        getSwitchChange,
        switchFlag,
        cardFields,
        cardExtraActions,
        cardListRef,
        handleCardExtraAction,
        resolveRecordStatus,
        alarmRecordPage,
      };
    },
  });
</script>
