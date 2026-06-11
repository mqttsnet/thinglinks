<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
          {{ t('iot.link.productTopic.productTopic.DefineTopic') }}
        </a-button>
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
                tooltip: t('component.aclTopicMatcherTester.title'),
                icon: 'ant-design:experiment-outlined',
                onClick: handleTest.bind(null, record),
                ifShow: !!record.topic,
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
    <!-- Topic 匹配测试器(IoT 通用组件) -->
    <AclTopicMatcherTesterModal @register="registerTesterModal" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { page, remove } from '/@/api/iot/link/productTopic/productTopic';
  import { columns, searchFormSchema } from './customizeTopic.data';
  import EditModal from './Edit.vue';
  import {
    AclTopicMatcherTesterModal,
    type TestableAclRule,
  } from '/@/components/iot/AclTopicMatcherTester';

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '产品Topic',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      AclTopicMatcherTesterModal,
    },
    props: {
      topicType: {
        type: String,
        default: '',
      },
      productIdentification: {
        type: String,
        default: '',
      },
    },
    setup(props) {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const [registerTesterModal, { openModal: openTesterModal }] = useModal();

      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('iot.link.productTopic.productTopic.table.title'),
        api: page,
        columns: columns(),
        formConfig: {
          name: 'ProductTopicSearch',
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
          topicType: props.topicType,
          productIdentification: props.productIdentification,
        },
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
          productIdentification: props.productIdentification,
        });
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.EDIT,
          productIdentification: props.productIdentification,
        });
      }

      /**
       * 弹出"topic 测试器" ── 把当前行的 topic 模式作为规则传入,
       * 用户可在 modal 内选真实设备替换占位符,验证具体设备 topic 是否能命中。
       * 此 topic 没有 ACL decision 概念,仅做模式匹配测试,所以不传 decision 字段。
       */
      function handleTest(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (!record?.topic) return;
        const rule: TestableAclRule = {
          ruleName: record.topic,
          topicPattern: record.topic,
          enabled: true,
        };
        openTesterModal(true, {
          rule,
          presetProductIdentification: props.productIdentification,
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        reload();
      }

      async function batchDelete(ids: string[]) {
        await remove(ids);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      }

      // 点击单行删除
      function handleDelete(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (record?.id) {
          batchDelete([record.id]);
        }
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

      return {
        t,
        registerTable,
        registerModal,
        registerTesterModal,
        handleAdd,
        handleEdit,
        handleTest,
        handleDelete,
        handleBatchDelete,
        handleSuccess,
      };
    },
  });
</script>
../../../../../api/iot/link/productTopic/productTopic
