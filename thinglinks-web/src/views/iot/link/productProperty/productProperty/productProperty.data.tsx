import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum, FileBizTypeEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { Tag, Tooltip, message } from 'ant-design-vue';
import CopyableText from '/@/components/CopyableText';
// dataTypeValidator: 物模型 datatype 校验/提示统一工具,跟设备调试参数填写共用
// 本文件目前只用到 buildHelpMessage(动态 tooltip) + TD_NCHAR_MAX(上限常量);
// buildRules / validateByDatatype 留给后续命令参数 / 设备调试场景调用
import { buildHelpMessage, TD_NCHAR_MAX, thingModelCodeRules } from '/@/utils/iot/dataTypeValidator';

const { t } = useI18n();

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.productProperty.productProperty.serviceId'),
      dataIndex: 'serviceId',
    },
    {
      title: t('iot.link.productProperty.productProperty.propertyCode'),
      dataIndex: 'propertyCode',
      width: 150,
      ellipsis: true,
      customRender: ({ record }) => {
        return <CopyableText text={record.propertyCode || ''} />;
      },
    },
    {
      title: t('iot.link.productProperty.productProperty.propertyName'),
      dataIndex: 'propertyName',
      width: 120,
      ellipsis: true,
      customRender: ({ record }) => {
        return (
          <Tooltip placement="topLeft" title={record.propertyName}>
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {record.propertyName}
            </span>
          </Tooltip>
        );
      },
    },
    {
      title: t('iot.link.productProperty.productProperty.datatype'),
      dataIndex: 'datatype',
      width: 100,
    },
    {
      title: t('iot.link.productProperty.productProperty.description'),
      dataIndex: 'description',
      width: 150,
      ellipsis: true,
      customRender: ({ record }) => {
        return (
          <Tooltip placement="topLeft" title={record.description}>
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {record.description}
            </span>
          </Tooltip>
        );
      },
    },
    {
      title: t('iot.link.productProperty.productProperty.enumlist'),
      dataIndex: 'enumlist',
      width: 100,
    },
    {
      title: t('iot.link.productProperty.productProperty.max'),
      dataIndex: 'max',
      width: 80,
    },
    {
      title: t('iot.link.productProperty.productProperty.maxlength'),
      dataIndex: 'maxlength',
      width: 100,
    },
    {
      title: t('iot.link.productProperty.productProperty.method'),
      dataIndex: 'method',
      width: 100,
    },
    {
      title: t('iot.link.productProperty.productProperty.min'),
      dataIndex: 'min',
      width: 80,
    },
    {
      title: t('iot.link.productProperty.productProperty.required'),
      dataIndex: 'required',
      width: 90,
    },
    {
      title: t('iot.link.productProperty.productProperty.step'),
      dataIndex: 'step',
      width: 80,
    },
    {
      title: t('iot.link.productProperty.productProperty.unit'),
      dataIndex: 'unit',
      width: 70,
    },
    {
      title: t('iot.link.productProperty.productProperty.icon'),
      dataIndex: 'icon',
      width: 80,
    },
    {
      title: t('iot.link.productProperty.productProperty.remark'),
      dataIndex: 'remark',
      width: 120,
      ellipsis: true,
      customRender: ({ record }) => {
        return (
          <Tooltip placement="topLeft" title={record.remark}>
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {record.remark}
            </span>
          </Tooltip>
        );
      },
    },
    {
      title: t('iot.link.productProperty.productProperty.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
    {
      title: t('thinglinks.common.updatedTime'),
      dataIndex: 'updatedTime',
      sorter: true,
      width: 180,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.productProperty.productProperty.propertyCode'),
      field: 'propertyCode',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productProperty.productProperty.propertyName'),
      field: 'propertyName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productProperty.productProperty.description'),
      field: 'description',
      component: 'Input',
      colProps: { span: 6 },
    }
  ];
};

// 编辑页字段
export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.productProperty.productProperty.serviceId'),
      field: 'serviceId',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.productProperty.productProperty.propertyCode'),
      field: 'propertyCode',
      component: 'Input',
      rules: thingModelCodeRules(),
      componentProps: {
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.propertyName'),
      field: 'propertyName',
      component: 'Input',
      rules: [{ required: true }],
      componentProps: {
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.datatype'),
      field: 'datatype',
      component: 'ApiSelect',
      // 动态 helpMessage:用户选了 datatype 后立即给出该类型的范围/格式提示
      // 用 function 形式确保 datatype 变化时 BasicForm 重新计算 tooltip 文案
      helpMessage: ({ values }) => buildHelpMessage(values?.datatype),
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_PROPERTY_DATA_TYPE),
      },
      rules: [{ required: true }],
    },
    {
      label: t('iot.link.productProperty.productProperty.icon'),
      field: 'icon',
      component: 'Upload',
      // colProps: { span: 22 },
      componentProps: ({ schema, tableAction, formActionType, formModel }) => {
        return {
          isDef: false,
          maxSize: 2048,
          multiple: false,
          maxNumber: 1,
          accept: ['.png','.jpg'],
          uploadParams: { bizType: FileBizTypeEnum.BASE_LINK_PRODUCT_ICON },
          resultField: 'id',
          onChange: async (file) => {
            console.log(111, file);

            await formActionType.setFieldsValue({ icon: [file[0]] });
            await formActionType.validateFields(['icon']);
          },
        };
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.description'),
      field: 'description',
      component: 'InputTextArea',
      colProps: {
        span: 22,
      },
      componentProps: {
        rows: 3,
        placeholder: t('common.inputText'),
      },
    },
    // 枚举值(仅 string)── 逗号分隔,如 "RED,GREEN,BLUE"
    {
      label: t('iot.link.productProperty.productProperty.enumlist'),
      field: 'enumlist',
      component: 'Input',
      ifShow: ({ values }) => values.datatype === 'string',
      helpMessage: t('iot.link.productProperty.productProperty.enumlistHelp'),
      componentProps: {
        placeholder: 'RED,GREEN,BLUE',
      },
    },
    // 数值约束(仅 int / decimal)── min/max/step 三件套用 InputNumber + 跨字段校验
    {
      label: t('iot.link.productProperty.productProperty.min'),
      field: 'min',
      component: 'InputNumber',
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      // 动态 helpMessage:按当前 datatype 给出 INT/DOUBLE 推荐范围,跟后端 TdDataTypeEnum 对齐
      helpMessage: ({ values }) => buildHelpMessage(values?.datatype),
      componentProps: ({ formModel }) => ({
        placeholder: t('common.inputText'),
        style: { width: '100%' },
        precision: formModel.datatype === 'int' ? 0 : undefined,
      }),
    },
    {
      label: t('iot.link.productProperty.productProperty.max'),
      field: 'max',
      component: 'InputNumber',
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      helpMessage: t('iot.link.productProperty.productProperty.maxGreaterThanMin'),
      componentProps: ({ formModel }) => ({
        placeholder: t('common.inputText'),
        style: { width: '100%' },
        precision: formModel.datatype === 'int' ? 0 : undefined,
      }),
      // 注:max > min 跨字段校验在 customFormSchemaRules 中实现(BasicForm validator 拿到 formModel)
    },
    {
      label: t('iot.link.productProperty.productProperty.step'),
      field: 'step',
      component: 'InputNumber',
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      helpMessage: t('iot.link.productProperty.productProperty.help.step'),
      componentProps: ({ formModel }) => ({
        placeholder: t('common.inputText'),
        style: { width: '100%' },
        precision: formModel.datatype === 'int' ? 0 : undefined,
        min: 0,
      }),
    },
    // 字符串约束(仅 string / DateTime / jsonObject)── maxlength 对变长类型有意义,且为必填
    {
      label: t('iot.link.productProperty.productProperty.maxlength'),
      field: 'maxlength',
      component: 'InputNumber',
      ifShow: ({ values }) =>
        ['string', 'DateTime', 'jsonObject'].includes(values.datatype),
      // 时序库 NCHAR 单字段上限 16374 字节,超出后端 DDL 会失败 → 表单端就 cap
      helpMessage: t('iot.link.productProperty.productProperty.help.string', {
        max: TD_NCHAR_MAX,
      }),
      // dynamicRules 与 ifShow 联动:仅变长类型时必填,其它类型被隐藏不参与校验
      dynamicRules: ({ values }) => {
        if (!['string', 'DateTime', 'jsonObject'].includes(values?.datatype)) return [];
        return [{ required: true, message: t('common.inputText') as string }];
      },
      componentProps: {
        placeholder: t('common.inputText'),
        style: { width: '100%' },
        min: 1,
        max: TD_NCHAR_MAX,
        precision: 0,
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.method'),
      field: 'method',
      component: 'ApiRadioGroup',
      rules: [{ required: true }],
      defaultValue: 'RWE',
      componentProps: {
        ...dictComponentProps(DictEnum.ACCESS_MODE),
        isBtn: true,
        isSolid: true,
      },
    },

    {
      label: t('iot.link.productProperty.productProperty.required'),
      field: 'required',
      defaultValue: '1',
      component: 'ApiRadioGroup',
      rules: [{ required: true }],
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_REQUIRED),
        isBtn: true,
        isSolid: true,
      },
    },
    // 单位(仅 int / decimal)── 数值才有单位语义
    {
      label: t('iot.link.productProperty.productProperty.unit'),
      field: 'unit',
      component: 'Input',
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      componentProps: {
        placeholder: t('iot.link.productProperty.productProperty.unitHelp'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: {
        span: 22,
      },
      componentProps: {
        rows: 3,
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.createdOrgId'),
      field: 'createdOrgId',
      component: 'Input',
      show: false,
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
