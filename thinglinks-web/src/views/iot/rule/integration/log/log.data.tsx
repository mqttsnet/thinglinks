import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { router } from '/@/router';

const { t } = useI18n();

/**
 * 从当前路由 query 取默认值（支持其它页面跳来时按 ruleId / deviceId 过滤）。
 * 找不到时返回 undefined，避免占位空字符串污染查询。
 */
function queryDefault(key: string): string | undefined {
  try {
    const v = router.currentRoute.value?.query?.[key];
    if (typeof v === 'string' && v.length > 0) return v;
  } catch {
    /* ignore */
  }
  return undefined;
}

export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.rule.integration.log.status'),
      dataIndex: 'status',
      slots: { customRender: 'status' },
      width: 110,
    },
    {
      title: t('iot.rule.integration.log.bridgeRuleId'),
      dataIndex: 'bridgeRuleId',
      slots: { customRender: 'ruleName' },
    },
    {
      title: t('iot.rule.integration.log.triggerSource'),
      dataIndex: 'triggerSource',
      slots: { customRender: 'triggerSource' },
      width: 120,
    },
    { title: t('iot.rule.integration.log.deviceIdentification'), dataIndex: 'deviceIdentification' },
    { title: t('iot.rule.integration.log.actionType'), dataIndex: 'actionType', width: 100 },
    { title: t('iot.rule.integration.log.stepCount'), dataIndex: 'stepCount', width: 90 },
    { title: t('iot.rule.integration.log.totalLatencyMs'), dataIndex: 'totalLatencyMs', width: 110 },
    {
      title: t('iot.rule.integration.log.startTime'),
      dataIndex: 'startTime',
      sorter: true,
      width: 180,
    },
    { title: t('iot.rule.integration.log.traceId'), dataIndex: 'traceId', width: 200 },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.rule.integration.log.traceId'),
      field: 'traceId',
      component: 'Input',
      colProps: { span: 8 },
    },
    {
      label: t('iot.rule.integration.log.bridgeRuleId'),
      field: 'bridgeRuleId',
      component: 'Input',
      colProps: { span: 6 },
      defaultValue: queryDefault('bridgeRuleId'),
    },
    {
      label: t('iot.rule.integration.log.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      colProps: { span: 6 },
      defaultValue: queryDefault('deviceIdentification'),
    },
    {
      label: t('iot.rule.integration.log.status'),
      field: 'status',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: { ...dictComponentProps(DictEnum.BRIDGE_MESSAGE_STATUS) },
    },
    {
      label: t('iot.rule.integration.log.triggerSource'),
      field: 'triggerSource',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: { ...dictComponentProps(DictEnum.BRIDGE_TRIGGER_SOURCE) },
    },
    {
      label: t('iot.rule.integration.log.startTime'),
      field: 'startTimeBegin',
      component: 'DatePicker',
      colProps: { span: 6 },
      componentProps: { showTime: true, format: 'YYYY-MM-DD HH:mm:ss', valueFormat: 'YYYY-MM-DD HH:mm:ss' },
    },
    {
      label: '至',
      field: 'startTimeEnd',
      component: 'DatePicker',
      colProps: { span: 6 },
      componentProps: { showTime: true, format: 'YYYY-MM-DD HH:mm:ss', valueFormat: 'YYYY-MM-DD HH:mm:ss' },
    },
  ];
};
