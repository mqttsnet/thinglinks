import { Ref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { DictEnum } from '/@/enums/commonEnum';
import { useDict } from '/@/components/Dict';
import type { CardField } from '/@/components/BusinessCardList';
import type {
  AlarmRecordResultVO,
  RuleAlarmChannelDetailsResultVO,
} from '/@/api/iot/rule/alarm/model/recordModel';
const { getDictLabel } = useDict();

const { t } = useI18n();
const searchColProps = { xs: 24, sm: 12, md: 8, lg: 6, xl: 6, xxl: 4 };
const searchRangeColProps = { xs: 24, sm: 24, md: 12, lg: 8, xl: 8 };

function formatDisplayValue(value?: string | number | null) {
  return value === undefined || value === null || value === '' ? '-' : String(value);
}

function getRuleInfo(record: Recordable) {
  return record?.ruleAlarmDetailsResultVO || {};
}

function getChannelList(record: Recordable): RuleAlarmChannelDetailsResultVO[] {
  return getRuleInfo(record)?.ruleAlarmChannelDetailsResultVOList || [];
}

function getConfiguredChannelCount(record: Recordable) {
  const ids = String(getRuleInfo(record)?.alarmChannelIds || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
  return ids.length;
}

export function normalizeAlarmRecordContent(value?: unknown) {
  if (value === undefined || value === null || value === '') return '-';
  const text = String(value).trim();
  if (!text) return '-';
  try {
    return JSON.stringify(JSON.parse(text), null, 2);
  } catch {
    return text.replace(/\r?\n{3,}/g, '\n\n');
  }
}

export function resolveAlarmRecordTitle(record: Recordable) {
  return formatDisplayValue(
    getRuleInfo(record)?.alarmName || record?.alarmName || record?.alarmIdentification,
  );
}

export function resolveAlarmRecordLevelText(record: Recordable) {
  const level = getRuleInfo(record)?.level ?? record?.alarmLevel ?? record?.level;
  return getDictLabel('RULE_ALARM_LEVEL', level, '-');
}

export function resolveAlarmRecordHandledStatusText(record: Recordable) {
  return getDictLabel('RULE_ALARM_RECORD_HANDLED_STATUE', record?.handledStatus, '-');
}

export function resolveAlarmRecordChannelSummary(record: Recordable) {
  const names = getChannelList(record)
    .map((item) => item.channelName)
    .filter(Boolean);
  if (names.length) return names.join('、');

  const count = getConfiguredChannelCount(record);
  if (count > 0) return `${count}${t('iot.link.engine.alarmRecord.channelUnit')}`;

  return '-';
}

export function decorateAlarmRecord(record: AlarmRecordResultVO) {
  return {
    ...record,
    alarmRecordTitle: resolveAlarmRecordTitle(record),
    alarmLevelText: resolveAlarmRecordLevelText(record),
    handledStatusText: resolveAlarmRecordHandledStatusText(record),
    channelSummary: resolveAlarmRecordChannelSummary(record),
    occurredTimeText: formatDisplayValue(record?.occurredTime),
    contentPreview: normalizeAlarmRecordContent(record?.contentData),
  };
}

export function decorateAlarmRecordPageResult<T extends { records?: AlarmRecordResultVO[] }>(
  res: T,
) {
  return {
    ...res,
    records: (res.records || []).map((record) => decorateAlarmRecord(record)),
  };
}

export const cardFields = (): CardField[] => [
  {
    label: t('iot.link.engine.alarmRecord.alarmIdentification'),
    field: 'alarmIdentification',
  },
  {
    label: t('iot.link.engine.alarmRecord.handledStatus'),
    field: 'handledStatusText',
    span: 12,
  },
  {
    label: t('iot.link.engine.alarmRecord.alarmLevel'),
    field: 'alarmLevelText',
    span: 12,
  },
  {
    label: t('iot.link.engine.alarmRecord.notificationChannels'),
    field: 'channelSummary',
    multiline: true,
  },
  {
    label: t('iot.link.engine.alarmRecord.occurredTime'),
    field: 'occurredTimeText',
    multiline: true,
  },
];

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.engine.alarmRecord.alarmName'),
      dataIndex: 'alarmName',
      customRender: ({ record }) => resolveAlarmRecordTitle(record),
    },
    {
      title: t('iot.link.engine.alarmRecord.alarmIdentification'),
      dataIndex: 'alarmIdentification',
    },
    {
      title: t('iot.link.engine.alarmRecord.alarmLevel'),
      dataIndex: 'alarmLevel',
      customRender: ({ record }) => resolveAlarmRecordLevelText(record),
    },
    {
      title: t('iot.link.engine.alarmRecord.notificationChannels'),
      dataIndex: 'channelSummary',
      customRender: ({ record }) => resolveAlarmRecordChannelSummary(record),
      width: 180,
    },
    {
      title: t('iot.link.engine.alarmRecord.contentData'),
      dataIndex: 'contentData',
      customRender: ({ record }) => normalizeAlarmRecordContent(record?.contentData),
      width: 260,
    },
    {
      title: t('iot.link.engine.alarmRecord.occurredTime'),
      dataIndex: 'occurredTime',
    },
    {
      title: t('iot.link.engine.alarmRecord.handledStatus'),
      dataIndex: ['handledStatus'],
      customRender: ({ record }) => {
        return getDictLabel('RULE_ALARM_RECORD_HANDLED_STATUE', record.handledStatus, '');
      },
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.engine.alarmRecord.alarmIdentification'),
      field: 'alarmIdentification',
      component: 'Input',
      colProps: searchColProps,
    },
    {
      label: t('iot.link.engine.alarmRecord.handledStatus'),
      field: 'handledStatus',
      component: 'ApiSelect',
      colProps: searchColProps,
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_ALARM_RECORD_HANDLED_STATUE),
      },
    },
    {
      field: 'createTimeRange',
      label: t('thinglinks.common.createdTime'),
      component: 'RangePicker',
      colProps: searchRangeColProps,
    },
  ];
};
// 查看页字段
export const viewFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.engine.alarmRecord.contentData'),
      field: 'contentData',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
    },
    {
      label: t('iot.link.engine.alarmRecord.handledTime'),
      field: 'handledTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      colProps: { span: 12 },
    },
    {
      label: t('iot.link.engine.alarmRecord.resolvedTime'),
      field: 'resolvedTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      colProps: { span: 12 },
    },
    {
      label: t('iot.link.engine.alarmRecord.handlingNotes'),
      field: 'handlingNotes',
      component: 'InputTextArea',
      show: true,
      componentProps: {
        disabled: false,
        allowClear: true,
        ...dictComponentProps(DictEnum.RULE_ALARM_LEVEL),
      },
    },
    {
      label: t('iot.link.engine.alarmRecord.resolutionNotes'),
      field: 'resolutionNotes',
      component: 'InputTextArea',
      show: true,
      componentProps: {
        disabled: false,
        allowClear: true,
        ...dictComponentProps(DictEnum.RULE_ALARM_LEVEL),
      },
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
      field: 'handledStatus',
      label: 'handledStatus',
      component: 'Input',
      show: false,
      componentProps: {
        disabled: false,
        allowClear: true,
        ...dictComponentProps(DictEnum.RULE_ALARM_RECORD_HANDLED_STATUE),
      },
    },
    {
      label: t('iot.link.engine.alarmRecord.handlingNotes'),
      field: 'handleNotes',
      component: 'InputTextArea',
      show: true,
      componentProps: {
        disabled: false,
        allowClear: true,
        ...dictComponentProps(DictEnum.RULE_ALARM_LEVEL),
      },
    },
  ];
};
export const editresolutionFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.engine.alarmRecord.resolutionNotes'),
      field: 'handleNotes',
      component: 'InputTextArea',
      show: true,
      componentProps: {
        disabled: false,
        allowClear: true,
        ...dictComponentProps(DictEnum.RULE_ALARM_LEVEL),
      },
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
