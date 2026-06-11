<template>
  <BasicTable @register="registerTable">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'action'">
        <div class="table-operation-device">
          <TableAction
            :actions="[
              {
                tooltip: t('common.title.view'),
                icon: 'ant-design:search-outlined',
                onClick: handleView.bind(null, record),
              },
            ]"
            :stopButtonPropagation="true"
          />
        </div>
      </template>
    </template>
  </BasicTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useI18n } from '/@/hooks/web/useI18n';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useRouter } from 'vue-router';
import { handleFetchParams } from '/@/utils/thinglinks/common';
import { page } from '/@/api/iot/link/device/device';
import { columns, searchFormSchema } from '/@/views/iot/link/device/subDevice/action.data';

export default defineComponent({
  name: 'SubDeviceList',
  components: {
    BasicTable,
    TableAction,
  },
  props: {
    deviceIdentification: {
      type: String,
      default: '',
    },
  },
  setup(props) {
    const { t } = useI18n();
    const router = useRouter();

    const [registerTable] = useTable({
      title: '',
      api: page,
      columns: columns(),
      formConfig: {
        name: 'SubDeviceSearch',
        labelWidth: 120,
        schemas: searchFormSchema(),
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
      searchInfo: {
        gatewayId: props.deviceIdentification,
      },
    });

    function handleView(record: Recordable) {
      // 路由 :id 段语义是 deviceIdentification（业务唯一标识，非主键 id）
      router.push({
        name: '设备详情',
        params: { id: record.deviceIdentification },
      });
    }

    return {
      t,
      registerTable,
      handleView,
    };
  },
});
</script>