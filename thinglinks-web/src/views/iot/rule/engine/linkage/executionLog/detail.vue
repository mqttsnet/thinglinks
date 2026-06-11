<template>
  <BasicDrawer
    v-bind="$attrs"
    width="80%"
    @register="register"
    :title="t('common.title.details')"
    :maskClosable="false"
  >
    <BasicTable @register="registerTable">
      <template #status="{ record }">
        <a-tag :color="record.status == 2 ? 'success' : record.status == 1 ? 'warning' : 'error'">{{
          getDictLabel('RULE_EXECUTION_LOG_STATUS', record.status, '')
        }}</a-tag>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                tooltip: t('common.title.view'),
                icon: 'ant-design:switcher-outlined',
                onClick: handleView.bind(null, record),
              },
            ]"
            :stopButtonPropagation="true"
          />
        </template>
      </template>
    </BasicTable>
    <BasicModal
      @register="registerModel"
      :title="
        t('iot.link.engine.executionLog.conditionLog') +
        '、' +
        t('iot.link.engine.executionLog.actionLog')
      "
      :maskClosable="false"
      :keyboard="true"
    >
      <a-collapse v-model:activeKey="activeKey">
        <a-collapse-panel key="1" :header="t('iot.link.engine.executionLog.conditionLog')">
          <BasicTable
            :columns="conditionColumns()"
            :dataSource="ruleExecutionLogDetail.conditionExecutionLogDetailsResultVOList"
            :maxHeight="200"
            :canResize="true"
            :loading="loading"
            :striped="true"
            :bordered="true"
            :pagination="false"
          >
            <template #conditionType="{ record }">
              {{ getDictLabel('RULE_EVENT_TYPE', record?.conditionType, '') }}
            </template>
            <template #evaluationResult="{ record }">
              <a-tag :color="record.evaluationResult == true ? 'success' : 'error'">{{
                record.evaluationResult == true
                  ? t('iot.link.engine.executionLog.established')
                  : t('iot.link.engine.executionLog.notEstablished')
              }}</a-tag>
            </template>
            <template #remark="{ record }">
              <a-tooltip placement="top">
                <template #title>
                  <span
                    >{{ record.remark }}
                    <CopyOutlined @click="copyText(record.remark)" />
                  </span>
                </template>
                <p class="conent">{{ record.remark }}</p>
              </a-tooltip>
            </template>
            <template #extendParams="{ record }">
              <a-tooltip placement="top">
                <template #title>
                  <span
                    >{{ record.extendParams }}
                    <CopyOutlined @click="copyText(record.extendParams)" />
                  </span>
                </template>
                <p class="conent">{{ record.extendParams }}</p>
              </a-tooltip>
            </template>
          </BasicTable>
        </a-collapse-panel>
        <a-collapse-panel key="2" :header="t('iot.link.engine.executionLog.actionLog')">
          <BasicTable
            :columns="actionColumns()"
            :dataSource="ruleExecutionLogDetail.actionExecutionLogDetailsResultVOList"
            :maxHeight="200"
            :canResize="true"
            :loading="loading"
            :striped="true"
            :bordered="true"
            :pagination="false"
          >
            <template #actionType="{ record }">
              {{ getDictLabel('RULE_ACTION_TYPE', record?.actionType, '') }}
            </template>
            <template #result="{ record }">
              <a-tag :color="record.result == true ? 'success' : 'error'">{{
                record.result == true
                  ? t('iot.link.engine.executionLog.established')
                  : t('iot.link.engine.executionLog.notEstablished')
              }}</a-tag>
            </template>
            <template #actionContent="{ record }">
              <a-tooltip placement="top">
                <template #title>
                  <span
                    >{{ record.actionContent }}
                    <CopyOutlined @click="copyText(record.actionContent)" />
                  </span>
                </template>
                <p class="conent">{{ record.remark }}</p>
              </a-tooltip>
            </template>
            <template #remark="{ record }">
              <a-tooltip placement="top">
                <template #title>
                  <span
                    >{{ record.remark }}
                    <CopyOutlined @click="copyText(record.remark)" />
                  </span>
                </template>
                <p class="conent">{{ record.remark }}</p>
              </a-tooltip>
            </template>
            <template #extendParams="{ record }">
              <a-tooltip placement="top">
                <template #title>
                  <span
                    >{{ record.extendParams }}
                    <CopyOutlined @click="copyText(record.extendParams)" />
                  </span>
                </template>
                <p class="conent">{{ record.extendParams }}</p>
              </a-tooltip>
            </template>
          </BasicTable>
        </a-collapse-panel>
      </a-collapse>
    </BasicModal>
  </BasicDrawer>
</template>
<script lang="ts" setup>
  // util
  import { ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { CopyOutlined } from '@ant-design/icons-vue';
  // api
  import { page, getRuleExecutionLogDetails } from '/@/api/iot/rule/engine/linkage/executionLog';
  // components
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { BasicModal, useModal } from '/@/components/Modal';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { columns, searchFormSchema, conditionColumns, actionColumns } from './executionLogDetail';
  import { useDict } from '/@/components/Dict';
  const { getDictLabel } = useDict();
  const { t } = useI18n();
  const loading = ref(true);
  const activeKey = ref(['1']);
  const propertyMetadata = ref({});
  const [register] = useDrawerInner((data) => {
    // 基本信息赋值
    propertyMetadata.value = data;
    reload();
  });

  // 表格
  const [registerTable, { reload }] = useTable({
    title: t('iot.link.engine.executionLog.table.title'),
    api: page,
    immediate: false,
    columns: columns(),
    formConfig: {
      name: 'ProductSearch',
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
    beforeFetch: (params) => {
      params.model = {
        ruleIdentification: propertyMetadata.value.ruleIdentification, // 设置默认值
      };
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    rowKey: 'id',
    rowSelection: {
      type: 'checkbox',
      columnWidth: 40,
    },
    actionColumn: {
      width: 100,
      title: t('common.column.action'),
      dataIndex: 'action',
    },
  });
  const [registerModel, { openModal }] = useModal();
  // 弹出查看页面
  const ruleExecutionLogDetail = ref({});
  const handleView = async (record: Recordable, e: Event) => {
    e?.stopPropagation();
    openModal(true);
    loading.value = true;
    try {
      const res = await getRuleExecutionLogDetails(record.id);
      ruleExecutionLogDetail.value = res;
      loading.value = false;
    } catch (error) {
      loading.value = false;
    }
  };
  const { createMessage } = useMessage();
  const copyText = (text) => {
    let result = copyTextToClipboard(text);
    if (result) {
      createMessage.success(t('common.tips.copySuccess'));
    } else {
      createMessage.warning(t('common.tips.copyFail'));
    }
  };
</script>
<style lang="less" scoped>
  .mar-l-10 {
    margin-left: 10px;
  }

  .conent {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    padding-top: 15px;
  }
</style>
