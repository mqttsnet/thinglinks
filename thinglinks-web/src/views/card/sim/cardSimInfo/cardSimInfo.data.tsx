import { Ref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { yesNoComponentProps } from '/@/utils/thinglinks/common';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
// import type { VideoMediaServerPageQuery } from '/@/api/video/media/model/serverModel';
import { useDict } from '/@/components/Dict';
import { echoMapText } from '/@/utils/echo';
const { getDictLabel } = useDict();

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('card.sim.cardSimInfo.imsi'),
      dataIndex: 'imsi',
    },
    {
      title: t('card.sim.cardSimInfo.iccid'),
      dataIndex: 'iccid',
    },
    {
      title: t('card.sim.cardSimInfo.cardNumber'),
      dataIndex: 'cardNumber',
    },
    {
      title: t('card.sim.cardSimInfo.cardType'),
      dataIndex: 'cardType',
      slots: { customRender: 'cardType' },
      customRender: ({ record }) => {
        return getDictLabel('CARD_SIMINFO_CARD_TYPE', record.cardType, '');
      },
    },
    {
      title: t('card.sim.cardSimInfo.channelId'),
      dataIndex: 'channelId',
    },
    {
      title: t('card.sim.cardSimInfo.flowsUsed'),
      dataIndex: 'flowsUsed',
    },
    {
      title: t('card.sim.cardSimInfo.flowsTotal'),
      dataIndex: 'flowsTotal',
    },
    {
      title: t('card.sim.cardSimInfo.flowsRest'),
      dataIndex: 'flowsRest',
    },
    {
      title: t('card.sim.cardSimInfo.virtualFlowsUsed'),
      dataIndex: 'virtualFlowsUsed',
    },
    {
      title: t('card.sim.cardSimInfo.virtualFlowsRest'),
      dataIndex: 'virtualFlowsRest',
    },
    {
      title: t('card.sim.cardSimInfo.virtualFlowsTotal'),
      dataIndex: 'virtualFlowsTotal',
    },
    {
      title: t('card.sim.cardSimInfo.smsFlag'),
      dataIndex: 'smsFlag',
      slots: { customRender: 'smsFlag' },
    },
    {
      title: t('card.sim.cardSimInfo.gprsFlag'),
      dataIndex: 'gprsFlag',
      slots: { customRender: 'gprsFlag' },
    },
    {
      title: t('card.sim.cardSimInfo.openTime'),
      dataIndex: 'openTime',
    },
    {
      title: t('card.sim.cardSimInfo.lastOpenTime'),
      dataIndex: 'lastOpenTime',
    },
    {
      title: t('card.sim.cardSimInfo.startTime'),
      dataIndex: 'startTime',
    },
    {
      title: t('card.sim.cardSimInfo.endTime'),
      dataIndex: 'endTime',
    },
    {
      title: t('card.sim.cardSimInfo.flowsEndTime'),
      dataIndex: 'flowsEndTime',
    },
    {
      title: t('card.sim.cardSimInfo.carrierType'),
      dataIndex: 'carrierType',
      slots: { customRender: 'carrierType' },
    },
    {
      title: t('card.sim.cardSimInfo.smsCount'),
      dataIndex: 'smsCount',
    },
    {
      title: t('card.sim.cardSimInfo.status'),
      dataIndex: 'status',
      slots: { customRender: 'status' },
    },
    {
      title: t('card.sim.cardSimInfo.useType'),
      dataIndex: 'useType',
      slots: { customRender: 'useType' },
    },
    {
      title: t('card.sim.cardSimInfo.apnName'),
      dataIndex: 'apnName',
    },
    {
      title: t('card.sim.cardSimInfo.ipAddress'),
      dataIndex: 'ipAddress',
    },
    {
      title: t('card.sim.cardSimInfo.gainTime'),
      dataIndex: 'gainTime',
    },
    {
      title: t('card.sim.cardSimInfo.onlineFlag'),
      dataIndex: 'onlineFlag',
      slots: { customRender: 'onlineFlag' },
    },
    {
      title: t('card.sim.cardSimInfo.stopCardType'),
      dataIndex: 'stopCardType',
      slots: { customRender: 'stopCardType' },
    },
    {
      title: t('card.sim.cardSimInfo.monthlyWarning'),
      dataIndex: 'monthlyWarning',
    },
    {
      title: t('card.sim.cardSimInfo.imei'),
      dataIndex: 'imei',
    },
    {
      title: t('card.sim.cardSimInfo.limitFlow'),
      dataIndex: 'limitFlow',
    },
    {
      title: t('card.sim.cardSimInfo.limitFlag'),
      dataIndex: 'limitFlag',
      slots: { customRender: 'limitFlag' },
    },
    {
      title: t('card.sim.cardSimInfo.limitStatus'),
      dataIndex: 'limitStatus',
      slots: { customRender: 'limitStatus' },
    },
    {
      title: t('card.sim.cardSimInfo.offerId'),
      dataIndex: 'offerId',
    },
    {
      title: t('card.sim.cardSimInfo.searchableStatus'),
      dataIndex: 'searchableStatus',
      slots: { customRender: 'searchableStatus' },
    },
    {
      title: t('card.sim.cardSimInfo.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('card.sim.cardSimInfo.createdOrgId'),
      dataIndex: 'createdOrgId',
      customRender: ({ record }) => echoMapText(record, 'createdOrgId'),
    },
    {
      title: t('thinglinks.common.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('card.sim.cardSimInfo.iccid'),
      field: 'iccid',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('card.sim.cardSimInfo.cardNumber'),
      field: 'cardNumber',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('card.sim.cardSimInfo.cardType'),
      field: 'cardType',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_CARD_TYPE),
      },
    },
    {
      label: t('card.sim.cardSimInfo.carrierType'),
      field: 'carrierType',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_CARRIER_TYPE),
      },
    },
    {
      label: t('card.sim.cardSimInfo.status'),
      field: 'status',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_STATUS),
      },
    },
    {
      field: 'createTimeRange',
      label: t('thinglinks.common.createdTime'),
      component: 'RangePicker',
      colProps: { span: 6 },
    },
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
      label: t('card.sim.cardSimInfo.imsi'),
      field: 'imsi',
      component: 'Input',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.iccid'),
      field: 'iccid',
      component: 'Input',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.cardNumber'),
      field: 'cardNumber',
      component: 'Input',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.cardType'),
      field: 'cardType',
      component: 'ApiSelect',
      defaultValue: 0,
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_CARD_TYPE),
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.channelId'),
      field: 'channelId',
      component: 'ApiCardoSelectNodeCard',
      colProps: { span: 24 },
      required: true,
      componentProps: ({ formModel, formActionType }) => {
        return {
          type: 'iotSimCard',
          onSelect: (value) => {
            const { validateFields } = formActionType;
            formModel.channelId = value?.id;
            validateFields(['channelId']);
          },
          value: formModel.channelId,
        };
      },
    },
    {
      label: t('card.sim.cardSimInfo.flowsUsed'),
      field: 'flowsUsed',
      component: 'Input',
      defaultValue: '0.00',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.flowsTotal'),
      field: 'flowsTotal',
      component: 'Input',
      defaultValue: '0.00',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.flowsRest'),
      field: 'flowsRest',
      component: 'Input',
      defaultValue: '0.00',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.virtualFlowsUsed'),
      field: 'virtualFlowsUsed',
      component: 'Input',
      defaultValue: '0.00',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.virtualFlowsRest'),
      field: 'virtualFlowsRest',
      component: 'Input',
      defaultValue: '0.00',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.virtualFlowsTotal'),
      field: 'virtualFlowsTotal',
      component: 'Input',
      defaultValue: '0.00',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.smsFlag'),
      field: 'smsFlag',
      component: 'RadioGroup',
      defaultValue: 0,
      componentProps: {
        options: [
          { label: t('card.sim.cardSimInfo.selectYes'), value: 1 },
          { label: t('card.sim.cardSimInfo.selectNo'), value: 0 },
        ],
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.gprsFlag'),
      field: 'gprsFlag',
      component: 'ApiSelect',
      defaultValue: 0,
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_GPRSFLAG),
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.openTime'),
      field: 'openTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.lastOpenTime'),
      field: 'lastOpenTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.startTime'),
      field: 'startTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.endTime'),
      field: 'endTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.flowsEndTime'),
      field: 'flowsEndTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.carrierType'),
      field: 'carrierType',
      component: 'ApiSelect',
      defaultValue: 1,
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_CARRIER_TYPE),
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.smsCount'),
      field: 'smsCount',
      component: 'Input',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.status'),
      field: 'status',
      component: 'ApiSelect',
      defaultValue: 1,
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_STATUS),
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.useType'),
      field: 'useType',
      component: 'ApiSelect',
      defaultValue: 1,
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_USETYPE),
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.apnName'),
      field: 'apnName',
      component: 'Input',
      defaultValue: 'CMIOT',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.ipAddress'),
      field: 'ipAddress',
      component: 'Input',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.gainTime'),
      field: 'gainTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.onlineFlag'),
      field: 'onlineFlag',
      component: 'ApiSelect',
      defaultValue: 0,
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_ONLINE_FLAG),
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.stopCardType'),
      field: 'stopCardType',
      component: 'ApiSelect',
      defaultValue: 1,
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_STOP_CARD_TYPE),
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.monthlyWarning'),
      field: 'monthlyWarning',
      component: 'Input',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.imei'),
      field: 'imei',
      component: 'Input',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.limitFlow'),
      field: 'limitFlow',
      component: 'Input',
      defaultValue: '0.00',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.limitFlag'),
      field: 'limitFlag',
      component: 'ApiSelect',
      defaultValue: 0,
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_LIMIT_FLAG),
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.limitStatus'),
      field: 'limitStatus',
      component: 'ApiSelect',
      defaultValue: 0,
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_SIMINFO_LIMIT_STATUS),
      },
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.offerId'),
      field: 'offerId',
      component: 'Input',
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.searchableStatus'),
      field: 'searchableStatus',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: t('card.sim.cardSimInfo.selectYes'), value: 1 },
          { label: t('card.sim.cardSimInfo.selectNo'), value: 0 },
        ],
      },
      defaultValue: 0,
      required: true,
    },
    {
      label: t('card.sim.cardSimInfo.remark'),
      field: 'remark',
      component: 'InputTextArea',
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
