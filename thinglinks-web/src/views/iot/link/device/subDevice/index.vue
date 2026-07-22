<template>
  <PageWrapper dense contentFullHeight>
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
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { useRouter } from 'vue-router';
  import { useGo } from '/@/hooks/web/usePage';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { page } from '/@/api/iot/link/device/device';
  import { columns, searchFormSchema } from './action.data';

  // icon
  import { SearchOutlined, CopyOutlined } from '@ant-design/icons-vue';
  import { Card, Row, Col } from 'ant-design-vue';
  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '设备动作',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      SearchOutlined,
      CopyOutlined,
      ACard: Card,
      ARow: Row,
      ACol: Col,
    },
    props: {
      deviceIdentification: {
        type: String,
        default: '',
      },
    },
    setup(_) {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const { replace } = useRouter();
      const go = useGo();

      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: '',
        api: page,
        columns: columns(),
        formConfig: {
          name: 'DeviceSearch',
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
          gatewayId: _.deviceIdentification,
          nodeType: 2,
        },
        defSort: {
          sort: 'createdTime',
          order: 'descend',
        },
        actionColumn: {
          width: 100,
          title: t('common.column.action'),
          dataIndex: 'action',
        },
      });

      // 新增或编辑成功回调
      function handleSuccess() {
        reload();
      }
      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        // 路由 :id 段语义是 deviceIdentification（业务唯一标识，非主键 id）
        replace({
          name: '设备详情',
          params: { id: record.deviceIdentification },
        });
      }
      return {
        t,
        registerTable,
        registerModal,
        handleSuccess,
        handleView,
      };
    },
  });
</script>
<style lang="less" scoped>
  .thinglinks-basic-table {
    padding: 0 16px;
  }

  :deep(.ant-form) {
    margin-bottom: 0;
    padding: 0;
  }

  :deep(.ant-table-body) {
    min-height: 250px;
  }

  :deep(.ant-table-expanded-row-fixed) {
    min-height: 250px;
  }</style
>../../../../../api/iot/link/deviceAction/deviceAction
