<template>
  <div ref="wrapRef" :class="getWrapperClass">
    <BasicForm
      ref="formRef"
      submitOnReset
      v-bind="getFormProps"
      v-if="getBindValues.useSearchForm"
      :tableAction="tableAction"
      @register="registerForm"
      @submit="handleSubmit"
      @advanced-change="redoHeight"
    >
      <template #[replaceFormSlotKey(item)]="data" v-for="item in getFormSlotKeys">
        <slot :name="item" v-bind="data || {}"></slot>
      </template>
    </BasicForm>
    <CardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isDevice"
    />
    <ProductCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isProduct"
    />
    <linkAgeCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isLinkAge"
    />
    <ChainedCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isChained"
      :switchFlag="switchFlag"
    />
    <ChannelCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isChannel"
    />
    <RuleGroovyScriptCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isRuleGroovyScript"
    />
    <AlarmListCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isAlarmList"
    />
    <AlarmRecordCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isAlarmRecord"
    />
    <PluginInstanceCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isPluginInstance"
    />
    <PluginInfoCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isPluginInfo"
    />
    <DeviceAclRuleCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isDeviceAclRule"
    />
    <CaCertLicenseCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isCaCertLicense"
    />
    <!-- 新增物联网卡渠道的卡片列表 -->
    <IotChannelCardList
      :title="title"
      @input="setSwitchFlag"
      v-if="switchFlag && isIotChannelCard"
      :searchData="searchData"
    />
    <IotSimCardList
      :title="title"
      @input="setSwitchFlag"
      v-if="switchFlag && isIotSimCard"
      :searchData="searchData"
    />
    <VideoNodeCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isVideoNode"
    />
    <VideoStreamPullCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isVideoStreamPull"
    />
    <VideoStreamPushCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isVideoStreamPush"
    />
    <VideoDeviceInfoCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isVideoDeviceInfo"
    />
    <VideoDeviceChannelCardList
      @input="setSwitchFlag"
      :title="title"
      :searchData="searchData"
      v-if="switchFlag && isVideoDeviceChannel"
    />
    <!-- 通用卡片视图插槽：使用 BusinessCardList 时通过此 slot 传入，无需新增 boolean prop -->
    <slot
      v-if="switchFlag && $slots.cardView"
      name="cardView"
      :searchData="searchData"
      :title="title"
      :switchView="setSwitchFlag"
    ></slot>
    <Table
      ref="tableElRef"
      v-bind="getBindValues"
      :rowClassName="getRowClassName"
      v-show="getEmptyDataIsShowTable"
      @change="handleTableChange"
      v-if="!switchFlag"
    >
      <template #[item]="data" v-for="item in Object.keys($slots)" :key="item">
        <slot :name="item" v-bind="data || {}"></slot>
      </template>
      <template #headerCell="{ column }">
        <HeaderCell :column="column" />
      </template>
      <!-- 增加对antdv3.x兼容 -->
      <template #bodyCell="data">
        <slot name="bodyCell" v-bind="data || {}"></slot>
      </template>
      <template #[`header-${column.dataIndex}`] v-for="(column, index) in columns" :key="index">
        <HeaderCell :column="column" />
      </template>
    </Table>
    <CopyModal @register="registerModalCode" />
  </div>
</template>
<script lang="ts">
  // enum
  import type {
    BasicTableProps,
    TableActionType,
    SizeType,
    ColumnChangeParam,
  } from './types/table';
  import { defineComponent, ref, reactive, computed, unref, toRaw, inject, watchEffect } from 'vue';
  import { Table } from 'ant-design-vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useModal } from '/@/components/Modal';
  import { PageWrapperFixedHeightKey } from '/@/components/Page';
  import HeaderCell from './components/HeaderCell.vue';
  import { InnerHandlers } from './types/table';

  import { usePagination } from './hooks/usePagination';
  import { useColumns } from './hooks/useColumns';
  import { useDataSource } from './hooks/useDataSource';
  import { useLoading } from './hooks/useLoading';
  import { useRowSelection } from './hooks/useRowSelection';
  import { useTableScroll } from './hooks/useTableScroll';
  import { useTableScrollTo } from './hooks/useScrollTo';
  import { useCustomRow } from './hooks/useCustomRow';
  import { useTableStyle } from './hooks/useTableStyle';
  import { useTableHeader } from './hooks/useTableHeader';
  import { useTableExpand } from './hooks/useTableExpand';
  import { createTableContext } from './hooks/useTableContext';
  import { useTableFooter } from './hooks/useTableFooter';
  import { useTableForm } from './hooks/useTableForm';
  import { useDesign } from '/@/hooks/web/useDesign';

  import { omit } from 'lodash-es';
  import { basicProps } from './props';
  import { isFunction } from '/@/utils/is';
  import { warn } from '/@/utils/log';
  // components
  import CardList from '/@/components/iot/link/device/CardList.vue';
  import ProductCardList from '/@/components/iot/link/product/ProductCardList.vue';
  import linkAgeCardList from '/@/components/iot/rule/engine/linkAgeCardList.vue';
  import ChainedCardList from '/@/components/iot/rule/engine/ChainedCardList.vue';
  import ChannelCardList from '/@/components/iot/rule/alarm/ChannelCardList.vue';
  import RuleGroovyScriptCardList from '/@/components/iot/rule/groovy/RuleGroovyScriptCardList.vue';
  import AlarmListCardList from '/@/components/iot/rule/alarm/AlarmListCardList.vue';
  import AlarmRecordCardList from '/@/components/iot/rule/alarm/AlarmRecordCardList.vue';
  import PluginInstanceCardList from '/@/components/iot/rule/plugin/PluginInstanceCardList.vue';
  import PluginInfoCardList from '/@/components/iot/rule/plugin/PluginInfoCardList.vue';
  import DeviceAclRuleCardList from '/@/components/iot/link/operationMaintenance/DeviceAclRuleCardList.vue';
  import CaCertLicenseCardList from '/@/components/iot/link/operationMaintenance/CaCertLicenseCardList.vue';
  // 新增物联网卡渠道卡片列表
  import IotChannelCardList from '/@/components/card/IotChannelCardList.vue';
  import IotSimCardList from '/@/components/card/IotSimCardList.vue';
  // 流媒体相关
  import VideoNodeCardList from '/@/components/video/media/server/VideoNodeCardList.vue';
  import VideoStreamPullCardList from '/@/components/video/media/proxy/VideoStreamPullCardList.vue';
  import VideoStreamPushCardList from '/@/components/video/media/push/VideoStreamPushCardList.vue';
  import VideoDeviceInfoCardList from '/@/components/video/device/info/VideoDeviceInfoCardList.vue';
  import VideoDeviceChannelCardList from '/@/components/video/device/channel/VideoDeviceChannelCardList.vue';
  // 通用弹窗组件信息
  import CopyModal from '/@/components/CopyModal/index.vue';
  export default defineComponent({
    components: {
      Table,
      BasicForm,
      HeaderCell,
      CopyModal,
      CardList,
      ProductCardList,
      linkAgeCardList,
      ChainedCardList,
      ChannelCardList,
      RuleGroovyScriptCardList,
      AlarmListCardList,
      AlarmRecordCardList,
      PluginInstanceCardList,
      PluginInfoCardList,
      DeviceAclRuleCardList,
      CaCertLicenseCardList,
      // 物联卡渠道列表
      IotChannelCardList,
      IotSimCardList,
      VideoNodeCardList,
      VideoStreamPullCardList,
      VideoStreamPushCardList,
      VideoDeviceInfoCardList,
      VideoDeviceChannelCardList,
    },
    props: basicProps,
    emits: [
      'fetch-success',
      'fetch-error',
      'selection-change',
      'register',
      'row-click',
      'row-dbClick',
      'row-contextmenu',
      'row-mouseenter',
      'row-mouseleave',
      'edit-end',
      'edit-cancel',
      'edit-row-end',
      'edit-change',
      'expanded-rows-change',
      'change',
      'columns-change',
      'switch-change',
    ],
    setup(props, { attrs, emit, slots, expose }) {
      const tableElRef = ref(null);
      const tableData = ref<Recordable[]>([]);
      const wrapRef = ref(null);
      const formRef = ref(null);
      const innerPropsRef = ref<Partial<BasicTableProps>>();

      const { prefixCls } = useDesign('basic-table');
      const [registerForm, formActions] = useForm();
      const [registerModalCode, { openModal: openCode }] = useModal();

      const getProps = computed(() => {
        return { ...props, ...unref(innerPropsRef) } as BasicTableProps;
      });
      const isFixedHeightPage = inject(PageWrapperFixedHeightKey, false);
      watchEffect(() => {
        unref(isFixedHeightPage) &&
          props.canResize &&
          warn(
            "'canResize' of BasicTable may not work in PageWrapper with 'fixedHeight' (especially in hot updates)",
          );
      });

      const { getLoading, setLoading } = useLoading(getProps);
      const {
        getPaginationInfo,
        getPagination,
        setPagination,
        setShowPagination,
        getShowPagination,
      } = usePagination(getProps);

      const {
        getRowSelection,
        getRowSelectionRef,
        getSelectRows,
        setSelectedRows,
        clearSelectedRowKeys,
        getSelectRowKeys,
        deleteSelectRowByKey,
        setSelectedRowKeys,
      } = useRowSelection(getProps, tableData, emit);

      const {
        handleTableChange: onTableChange,
        getDataSourceRef,
        getDataSource,
        getRawDataSource,
        setTableData,
        updateTableDataRecord,
        deleteTableDataRecord,
        insertTableDataRecord,
        findTableDataRecord,
        fetch,
        getRowKey,
        reload,
        getAutoCreateKey,
        updateTableData,
      } = useDataSource(
        getProps,
        {
          tableData,
          getPaginationInfo,
          setLoading,
          setPagination,
          getFieldsValue: formActions.getFieldsValue,
          clearSelectedRowKeys,
        },
        emit,
      );
      // watch(props.switchFlag, async (val) => {
      //   await fetch();
      // });

      function handleTableChange(...args) {
        onTableChange.call(undefined, ...args);
        emit('change', ...args);
        // 解决通过useTable注册onChange时不起作用的问题
        const { onChange } = unref(getProps);
        onChange && isFunction(onChange) && onChange.call(undefined, ...args);
      }

      // 内部查看弹窗方法
      const openRemarkModal = (value: string, title: string) => {
        openCode(true, { value, title });
      };

      const {
        getViewColumns,
        getColumns,
        setCacheColumnsByField,
        setColumns,
        getColumnsRef,
        getCacheColumns,
      } = useColumns(getProps, getPaginationInfo, openRemarkModal);

      const { getScrollRef, redoHeight } = useTableScroll(
        getProps,
        tableElRef,
        getColumnsRef,
        getRowSelectionRef,
        getDataSourceRef,
        wrapRef,
        formRef,
      );

      const { scrollTo } = useTableScrollTo(tableElRef, getDataSourceRef);

      const { customRow } = useCustomRow(getProps, {
        setSelectedRowKeys,
        getSelectRowKeys,
        clearSelectedRowKeys,
        getAutoCreateKey,
        emit,
      });

      const { getRowClassName } = useTableStyle(getProps, prefixCls);

      const { getExpandOption, expandAll, expandRows, collapseAll } = useTableExpand(
        getProps,
        tableData,
        emit,
      );

      const handlers: InnerHandlers = {
        onColumnsChange: (data: ColumnChangeParam[]) => {
          emit('columns-change', data);
          // support useTable
          unref(getProps).onColumnsChange?.(data);
        },
      };

      const { getHeaderProps } = useTableHeader(getProps, slots, handlers);

      const { getFooterProps } = useTableFooter(
        getProps,
        getScrollRef,
        tableElRef,
        getDataSourceRef,
      );

      const { getFormProps, replaceFormSlotKey, getFormSlotKeys, handleSearchInfoChange } =
        useTableForm(getProps, slots, fetch, getLoading);

      const getBindValues = computed(() => {
        const dataSource = unref(getDataSourceRef);
        let propsData: Recordable = {
          ...attrs,
          customRow,
          ...unref(getProps),
          ...unref(getHeaderProps),
          scroll: unref(getScrollRef),
          loading: unref(getLoading),
          tableLayout: 'fixed',
          rowSelection: unref(getRowSelectionRef),
          rowKey: unref(getRowKey),
          columns: toRaw(unref(getViewColumns)),
          pagination: toRaw(unref(getPaginationInfo)),
          dataSource,
          footer: unref(getFooterProps),
          ...unref(getExpandOption),
        };
        // if (slots.expandedRowRender) {
        //   propsData = omit(propsData, 'scroll');
        // }

        propsData = omit(propsData, ['class', 'onChange']);
        return propsData;
      });

      const getWrapperClass = computed(() => {
        const values = unref(getBindValues);
        return [
          prefixCls,
          attrs.class,
          {
            [`${prefixCls}-form-container`]: values.useSearchForm,
            [`${prefixCls}--inset`]: values.inset,
          },
        ];
      });

      const getEmptyDataIsShowTable = computed(() => {
        const { emptyDataIsShowTable, useSearchForm } = unref(getProps);
        if (emptyDataIsShowTable || !useSearchForm) {
          return true;
        }
        return !!unref(getDataSourceRef).length;
      });
      let title = ref('');
      function setProps(props: Partial<BasicTableProps>) {
        innerPropsRef.value = { ...unref(innerPropsRef), ...props };
        title.value = props.title as string;
      }

      const tableAction: TableActionType = {
        reload,
        getSelectRows,
        setSelectedRows,
        clearSelectedRowKeys,
        getSelectRowKeys,
        deleteSelectRowByKey,
        setPagination,
        setTableData,
        updateTableDataRecord,
        deleteTableDataRecord,
        insertTableDataRecord,
        findTableDataRecord,
        redoHeight,
        setSelectedRowKeys,
        setColumns,
        setLoading,
        getDataSource,
        getRawDataSource,
        setProps,
        getRowSelection,
        getPaginationRef: getPagination,
        getColumns,
        getCacheColumns,
        emit,
        updateTableData,
        setShowPagination,
        getShowPagination,
        setCacheColumnsByField,
        expandAll,
        expandRows,
        collapseAll,
        scrollTo,
        getSize: () => {
          return unref(getBindValues).size as SizeType;
        },
      };
      createTableContext({ ...tableAction, wrapRef, getBindValues });

      expose(tableAction);

      emit('register', tableAction, formActions);
      async function setSwitchFlag(value) {
        console.log('到table了', value);
        emit('switch-change', value);
        const info = await formActions.getFieldsValue();
        // value 为 false 表示切换到 table 视图，需要触发 fetch
        handleSearchInfoChange(info, value);
      }

      let searchData = reactive({});
      function handleSubmit(info: any) {
        handleSearchInfoChange(info, props.switchFlag);
        // TODO 临时解决重复点击查询按钮，未更新卡片列表数据问题
        // 目前为searchData变化，则卡片列表重新获取数据，应调整为：点击查询，则触发。
        searchData = Object.assign(searchData, { ...info }, { timeStamp: new Date().getTime() });
        console.log('searchData:', searchData);
      }
      return {
        formRef,
        tableElRef,
        getBindValues,
        getLoading,
        registerForm,
        handleSubmit,
        handleSearchInfoChange,
        getEmptyDataIsShowTable,
        handleTableChange,
        getRowClassName,
        wrapRef,
        tableAction,
        redoHeight,
        getFormProps: getFormProps as any,
        replaceFormSlotKey,
        getFormSlotKeys,
        getWrapperClass,
        columns: getViewColumns,
        setSwitchFlag,
        title,
        searchData,
        registerModalCode,
      };
    },
  });
</script>
<style lang="less">
  @border-color: #cecece4d;

  @prefix-cls: ~'@{namespace}-basic-table';

  [data-theme='dark'] {
    .ant-table-tbody > tr:hover.ant-table-row-selected > td,
    .ant-table-tbody > tr.ant-table-row-selected td {
      background-color: #262626;
    }
  }

  .@{prefix-cls} {
    max-width: 100%;
    height: 100%;

    &-row__striped {
      td {
        background-color: @app-content-background;
      }
    }

    &-form-container {
      padding: 16px;

      .ant-form {
        width: 100%;
        padding: 12px 10px 6px;
        margin-bottom: 16px;
        background-color: @component-background;
        border-radius: 2px;
      }
    }

    .ant-tag {
      margin-right: 0;
    }

    .ant-table-wrapper {
      padding: 6px;
      background-color: @component-background;
      border-radius: 2px;

      .ant-table-title {
        min-height: 40px;
        padding: 0 0 8px !important;
      }

      .ant-table.ant-table-bordered .ant-table-title {
        border: none !important;
      }
    }

    .ant-table {
      width: 100%;
      overflow-x: hidden;

      &-title {
        display: flex;
        padding: 8px 6px;
        border-bottom: none;
        justify-content: space-between;
        align-items: center;
      }

      //.ant-table-tbody > tr.ant-table-row-selected td {
      //background-color: fade(@primary-color, 8%) !important;
      //}
    }

    .ant-pagination {
      margin: 10px 0 0;
    }

    .ant-table-footer {
      padding: 0;

      .ant-table-wrapper {
        padding: 0;
      }

      table {
        border: none !important;
      }

      .ant-table-body {
        overflow-x: hidden !important;
        //  overflow-y: scroll !important;
      }

      td {
        padding: 12px 8px;
      }
    }

    &--inset {
      .ant-table-wrapper {
        padding: 0;
      }
    }
  }
</style>
