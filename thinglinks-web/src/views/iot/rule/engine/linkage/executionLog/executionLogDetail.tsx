import { BasicColumn, FormSchema } from '/@/components/Table';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { DictEnum } from '/@/enums/commonEnum';
import { useI18n } from '/@/hooks/web/useI18n';
import { useDict } from '/@/components/Dict';
import { Tag } from 'ant-design-vue';
const { t } = useI18n();
const { getDictLabel } = useDict();

function formatDuration(record: Recordable) {
  const extendLatency = parseExtendLatency(record?.extendParams);
  if (extendLatency != null) {
    return formatLatency(extendLatency);
  }
  if (record?.totalLatencyMs != null) {
    return formatLatency(record.totalLatencyMs);
  }
  const start = Date.parse(record?.startTime || '');
  const end = Date.parse(record?.endTime || '');
  const diff = end - start;
  return Number.isFinite(diff) && diff >= 0 ? formatLatency(diff) : '-';
}

function parseExtendLatency(extendParams?: string) {
  if (!extendParams) return undefined;
  try {
    const parsed = JSON.parse(extendParams);
    const latency = Number(parsed?.latencyMs);
    return Number.isFinite(latency) && latency >= 0 ? latency : undefined;
  } catch {
    return undefined;
  }
}

function formatLatency(value?: number | string) {
  const next = Number(value);
  if (!Number.isFinite(next) || next < 0) return '-';
  return next === 0 ? '< 1 ms' : `${next} ms`;
}

function getStatusColor(status) {
  const value = Number(status);
  if (value === 2) return 'success';
  if (value === 1) return 'warning';
  return 'error';
}

export function columns(): BasicColumn[] {
  return [
    {
      dataIndex: 'id',
      title: t('iot.link.engine.executionLog.id'),
      width: 180,
    },
    {
      dataIndex: 'ruleName',
      title: t('iot.link.engine.executionLog.ruleName'),
      width: 180,
      ellipsis: true,
    },
    {
      dataIndex: 'ruleIdentification',
      title: t('iot.link.engine.executionLog.ruleIdentification'),
      width: 180,
      ellipsis: true,
    },
    {
      dataIndex: 'status',
      title: t('iot.link.engine.executionLog.status'),
      width: 100,
      customRender: ({ record }) => {
        return (
          <Tag color={getStatusColor(record.status)}>
            {getDictLabel('RULE_EXECUTION_LOG_STATUS', record.status, '-')}
          </Tag>
        );
      },
    },
    {
      dataIndex: 'duration',
      title: t('iot.link.engine.executionLog.duration'),
      width: 110,
      customRender: ({ record }) => formatDuration(record),
    },
    {
      dataIndex: 'startTime',
      title: t('iot.link.engine.executionLog.startTime'),
      width: 180,
    },
    {
      dataIndex: 'endTime',
      title: t('iot.link.engine.executionLog.endTime'),
      width: 180,
    },
    {
      dataIndex: 'remark',
      title: t('iot.link.engine.executionLog.remark'),
      width: 220,
      ellipsis: true,
    },
  ];
}
export function searchFormSchema(): FormSchema[] {
  return [
    {
      label: t('iot.link.engine.executionLog.id'),
      field: 'id',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.engine.executionLog.status'),
      field: 'status',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_EXECUTION_LOG_STATUS),
      },
      colProps: { span: 6 },
    },
    {
      field: 'startTimeRange',
      label: t('iot.link.engine.executionLog.startTime'),
      component: 'RangePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
      },
      colProps: { span: 8 },
    },
  ];
}
