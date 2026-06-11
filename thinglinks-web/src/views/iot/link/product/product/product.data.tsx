import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum, FileBizTypeEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { echoMapText } from '/@/utils/echo';
import type { CardField } from '/@/components/BusinessCardList';
import { SnapshotIdTag } from '/@/components/iot';

const { t } = useI18n();

/**
 * BusinessCardList 卡片字段配置 ── 与桥接规则 / 数据源 同款 flexy 风格。
 * 名称(nameField=productName)与徽章/状态由 BusinessCardList 单独渲染,本配置只覆盖信息行。
 */
export const cardFields = (): CardField[] => [
  {
    label: t('iot.link.product.product.productIdentification'),
    field: 'productIdentification',
    span: 24,
  },
  {
    label: t('iot.link.product.product.protocolType'),
    field: 'protocolType',
    span: 12,
  },
  {
    // 卡片视图为信息浏览态,版本序号按纯文本展示即可(复制 / Tag 装饰留在详情页)
    label: t('iot.link.product.product.activeVersionNo'),
    field: 'activeVersionNo',
    span: 12,
  },
  {
    label: t('iot.link.product.product.manufacturerName'),
    field: 'manufacturerName',
    span: 24,
  },
];
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.product.product.appId'),
      dataIndex: 'appId',
    },
    {
      title: t('iot.link.product.product.templateId'),
      dataIndex: 'templateId',
    },
    {
      title: t('iot.link.product.product.productName'),
      dataIndex: 'productName',
    },
    {
      title: t('iot.link.product.product.productIdentification'),
      dataIndex: 'productIdentification',
    },
    {
      title: t('iot.link.product.product.productType'),
      dataIndex: 'productType',
      slots: { customRender: 'productTypeColumn' },
    },
    {
      title: t('iot.link.product.product.manufacturerId'),
      dataIndex: 'manufacturerId',
    },
    {
      title: t('iot.link.product.product.manufacturerName'),
      dataIndex: 'manufacturerName',
    },
    {
      title: t('iot.link.product.product.model'),
      dataIndex: 'model',
    },
    {
      title: t('iot.link.product.product.dataFormat'),
      dataIndex: 'dataFormat',
    },
    {
      title: t('iot.link.product.product.deviceType'),
      dataIndex: 'deviceType',
    },
    {
      title: t('iot.link.product.product.protocolType'),
      dataIndex: 'protocolType',
    },
    {
      title: t('iot.link.product.product.productStatus'),
      dataIndex: 'productStatus',
      slots: { customRender: 'productStatus' },
    },
    {
      title: t('iot.link.product.product.activeVersionNo'),
      dataIndex: 'activeVersionNo',
      width: 240,
      customRender: ({ record }) => <SnapshotIdTag value={record.activeVersionNo} />,
    },
    {
      title: t('iot.link.product.product.icon'),
      dataIndex: 'icon',
    },
    {
      title: t('iot.link.product.product.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.link.product.product.createdOrgId'),
      dataIndex: 'createdOrgId',
      customRender: ({ record }) => echoMapText(record, 'createdOrgId'),
    },
    {
      title: t('iot.link.product.product.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
  ];
};

/**
 * 产品搜索面板 ── flexy 风格,与桥接规则对齐:
 *   - 第一行(默认展示):产品名 + 产品标识 + 产品类型 + 启用状态(4 个高频字段)
 *   - 第二行(点"展开"出现):协议类型 + 应用ID + 厂商 + 型号 + 创建时间(5 个次级字段)
 *
 * BasicTable formConfig 已开启 showAdvancedButton,字段按 schema 顺序排列,
 * 超过第一行的字段自动归入"展开"按钮。
 */
export const searchFormSchema = (): FormSchema[] => {
  return [
    // ────────── 第一行:高频字段(4 个 × span 6) ──────────
    {
      label: t('iot.link.product.product.productName'),
      field: 'productName',
      component: 'Input',
      colProps: { span: 6 },
      componentProps: { allowClear: true },
    },
    {
      label: t('iot.link.product.product.productIdentification'),
      field: 'productIdentification',
      component: 'Input',
      colProps: { span: 6 },
      componentProps: { allowClear: true },
    },
    {
      label: t('iot.link.product.product.productType'),
      field: 'productType',
      colProps: { span: 6 },
      component: 'ApiSelect',
      componentProps: { allowClear: true, ...dictComponentProps(DictEnum.LINK_PRODUCT_TYPE) },
    },
    {
      label: t('iot.link.product.product.productStatus'),
      field: 'productStatus',
      colProps: { span: 6 },
      component: 'ApiSelect',
      componentProps: { allowClear: true, ...dictComponentProps(DictEnum.LINK_PRODUCT_STATUS) },
    },
    // ────────── 第二行:次级字段(展开后显示) ──────────
    {
      label: t('iot.link.product.product.protocolType'),
      field: 'protocolType',
      colProps: { span: 6 },
      component: 'ApiSelect',
      componentProps: {
        allowClear: true,
        ...dictComponentProps(DictEnum.LINK_PRODUCT_PROTOCOL_TYPE),
      },
    },
    {
      label: t('iot.link.device.device.appId'),
      field: 'appId',
      colProps: { span: 6 },
      component: 'ApiSelect',
      componentProps: {
        allowClear: true,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('iot.link.product.product.manufacturerName'),
      field: 'manufacturerName',
      component: 'Input',
      colProps: { span: 6 },
      componentProps: { allowClear: true },
    },
    {
      label: t('iot.link.product.product.model'),
      field: 'model',
      component: 'Input',
      colProps: { span: 6 },
      componentProps: { allowClear: true },
    },
    {
      field: 'createTimeRange',
      label: t('common.createdTime'),
      component: 'RangePicker',
      colProps: { span: 6 },
      componentProps: { allowClear: true, style: { width: '100%' } },
    },
  ];
};

// 编辑页字段
export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    // ────────── 隐藏字段 ──────────
    { field: 'id', label: 'ID', component: 'Input', show: false },
    { label: t('iot.link.product.product.templateId'), field: 'id', component: 'Input', show: false },
    {
      label: t('iot.link.product.product.productIdentification'),
      field: 'productIdentification',
      component: 'Input',
      show: false,
    },
    { label: t('iot.link.product.product.createdOrgId'), field: 'createdOrgId', component: 'Input', show: false },

    // ════════════ 基础信息 ════════════
    {
      field: 'divider-basic',
      component: 'Divider',
      label: t('iot.link.product.product.group.basic'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.link.product.product.appId'),
      field: 'appId',
      component: 'ApiSelect',
      rules: [{ required: true }],
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
      helpMessage: t('iot.link.product.product.appId'),
    },
    {
      label: t('iot.link.product.product.productName'),
      field: 'productName',
      component: 'Input',
      rules: [{ required: true }],
      helpMessage: t('iot.link.product.product.helpMessage.productName'),
    },
    {
      label: t('iot.link.product.product.productType'),
      field: 'productType',
      defaultValue: '1',
      component: 'ApiSelect',
      rules: [{ required: true }],
      componentProps: {
        disabled: _type.value === ActionEnum.EDIT,
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_PRODUCT_TYPE),
      },
      helpMessage: t('iot.link.product.product.helpMessage.productType'),
    },
    {
      label: t('iot.link.product.product.productStatus'),
      field: 'productStatus',
      defaultValue: '0',
      component: 'ApiSelect',
      rules: [{ required: true }],
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_PRODUCT_STATUS),
      },
      helpMessage: t('iot.link.product.product.productStatus'),
    },
    {
      label: t('iot.link.product.product.protocolType'),
      field: 'protocolType',
      component: 'ApiSelect',
      rules: [{ required: true }],
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_PROTOCOL_TYPE),
      },
      helpMessage: t('iot.link.product.product.helpMessage.protocolType'),
    },
    {
      label: t('iot.link.product.product.dataFormat'),
      field: 'dataFormat',
      component: 'ApiSelect',
      defaultValue: 'JSON',
      rules: [{ required: true }],
      componentProps: {
        disabled: true,
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_PRODUCT_DATA_FORMAT),
      },
      helpMessage: t('iot.link.product.product.helpMessage.dataFormat'),
    },

    // ════════════ 厂商与型号 ════════════
    {
      field: 'divider-manufacturer',
      component: 'Divider',
      label: t('iot.link.product.product.group.manufacturer'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.link.product.product.manufacturerId'),
      field: 'manufacturerId',
      component: 'Input',
      rules: [{ required: true }],
      helpMessage: t('iot.link.product.product.helpMessage.manufacturerId'),
    },
    {
      label: t('iot.link.product.product.manufacturerName'),
      field: 'manufacturerName',
      component: 'Input',
      rules: [{ required: true }],
      helpMessage: t('iot.link.product.product.helpMessage.manufacturerName'),
    },
    {
      label: t('iot.link.product.product.model'),
      field: 'model',
      component: 'Input',
      rules: [{ required: true }],
      helpMessage: t('iot.link.product.product.helpMessage.model'),
    },
    {
      label: t('iot.link.product.product.deviceType'),
      field: 'deviceType',
      component: 'Input',
      rules: [{ required: true }],
      helpMessage: t('iot.link.product.product.helpMessage.deviceType'),
    },

    // ════════════ 图标与描述 ════════════
    {
      field: 'divider-appearance',
      component: 'Divider',
      label: t('iot.link.product.product.group.appearance'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.link.product.product.icon'),
      field: 'icon',
      component: 'Upload',
      componentProps: ({ schema, tableAction, formActionType, formModel }) => {
        return {
          isDef: false,
          multiple: false,
          maxNumber: 1,
          accept: ['.png'],
          uploadParams: { bizType: FileBizTypeEnum.BASE_LINK_PRODUCT_ICON },
          resultField: 'id',
          onChange: async (file) => {
            await formActionType.setFieldsValue({ icon: [file[0]] });
            await formActionType.validateFields(['icon']);
          },
        };
      },
    },
    {
      label: t('iot.link.product.product.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 24 },
      componentProps: { rows: 3 },
      helpMessage: t('iot.link.product.product.remark'),
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
