import type { RuleNotificationVariableGroup } from '/@/api/iot/rule/engine/linkage/notification';

const consoleOrigin = typeof window === 'undefined' ? '' : window.location.origin;
const sampleRuleUrl = `${consoleOrigin}/#/engine/linkage/save?id=691884008387597318&type=handleView`;

export const FALLBACK_NOTIFICATION_VARIABLE_GROUPS: RuleNotificationVariableGroup[] = [
  {
    groupCode: 'rule',
    groupName: '规则',
    variables: [
      {
        key: 'rule.name',
        placeholder: '${rule.name}',
        label: '规则名称',
        description: '当前场景联动规则名称',
        sample: '设备离线告警联动',
      },
      {
        key: 'rule.identification',
        placeholder: '${rule.identification}',
        label: '规则标识',
        description: '当前场景联动规则标识',
        sample: '691884008387597318',
      },
      {
        key: 'rule.executionId',
        placeholder: '${rule.executionId}',
        label: '执行流水号',
        description: '本次规则执行流水号',
        sample: '764352343040583682',
      },
    ],
  },
  {
    groupCode: 'alarm',
    groupName: '告警',
    variables: [
      {
        key: 'alarm.name',
        placeholder: '${alarm.name}',
        label: '告警名称',
        description: '所选告警规则名称',
        sample: '设备离线告警',
      },
      {
        key: 'alarm.identification',
        placeholder: '${alarm.identification}',
        label: '告警标识',
        description: '所选告警规则标识',
        sample: 'ALARM_DEVICE_OFFLINE',
      },
      {
        key: 'alarm.scene',
        placeholder: '${alarm.scene}',
        label: '告警场景',
        description: '告警规则所属场景',
        sample: '设备状态',
      },
      {
        key: 'alarm.level',
        placeholder: '${alarm.level}',
        label: '告警等级',
        description: '告警等级编码',
        sample: '2',
      },
    ],
  },
  {
    groupCode: 'device',
    groupName: '设备',
    variables: [
      {
        key: 'product.name',
        placeholder: '${product.name}',
        label: '产品名称',
        description: '触发设备所属产品名称',
        sample: '全志F135水晶球',
      },
      {
        key: 'product.identification',
        placeholder: '${product.identification}',
        label: '产品标识',
        description: '触发设备所属产品标识',
        sample: '2992007290322944',
      },
      {
        key: 'device.name',
        placeholder: '${device.name}',
        label: '设备名称',
        description: '触发设备名称',
        sample: '测试普通设备01',
      },
      {
        key: 'device.identification',
        placeholder: '${device.identification}',
        label: '设备标识',
        description: '触发设备标识',
        sample: '3752151419551745',
      },
    ],
  },
  {
    groupCode: 'trigger',
    groupName: '触发事件',
    variables: [
      {
        key: 'trigger.time',
        placeholder: '${trigger.time}',
        label: '触发时间',
        description: '设备事件触发时间',
        sample: '2026-07-05 01:45:46',
      },
      {
        key: 'trigger.actionType',
        placeholder: '${trigger.actionType}',
        label: '设备动作',
        description: 'MQS 设备动作类型',
        sample: 'DISCONNECT',
      },
      {
        key: 'trigger.payloadKind',
        placeholder: '${trigger.payloadKind}',
        label: '报文形态',
        description: 'RAW 或 THING_MODEL',
        sample: 'THING_MODEL',
      },
      {
        key: 'trigger.rawMessage',
        placeholder: '${trigger.rawMessage}',
        label: '原始报文',
        description: '触发规则的原始报文',
        sample: '{"status":"offline"}',
      },
    ],
  },
  {
    groupCode: 'link',
    groupName: '链接',
    variables: [
      {
        key: 'link.ruleUrl',
        placeholder: '${link.ruleUrl}',
        label: '规则详情链接',
        description: '场景联动规则详情地址',
        sample: sampleRuleUrl,
      },
      {
        key: 'link.executionLogUrl',
        placeholder: '${link.executionLogUrl}',
        label: '执行日志链接',
        description: '规则执行日志地址',
        sample: sampleRuleUrl,
      },
    ],
  },
];
