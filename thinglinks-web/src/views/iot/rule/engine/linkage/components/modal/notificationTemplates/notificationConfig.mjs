const SITE_MESSAGE_CHANNEL_TYPE = 3;

const VARIABLE_GROUP_LOCALE_KEY_BY_CODE = Object.freeze({
  rule: 'rule',
  alarm: 'alarm',
  device: 'device',
  trigger: 'trigger',
  link: 'link',
});

const VARIABLE_LOCALE_KEY_BY_CODE = Object.freeze({
  'rule.name': 'ruleName',
  'rule.identification': 'ruleIdentification',
  'rule.executionId': 'ruleExecutionId',
  'alarm.name': 'alarmName',
  'alarm.identification': 'alarmIdentification',
  'alarm.scene': 'alarmScene',
  'alarm.level': 'alarmLevel',
  'product.name': 'productName',
  'product.identification': 'productIdentification',
  'device.name': 'deviceName',
  'device.identification': 'deviceIdentification',
  'trigger.time': 'triggerTime',
  'trigger.actionType': 'triggerActionType',
  'trigger.payloadKind': 'triggerPayloadKind',
  'trigger.rawMessage': 'triggerRawMessage',
  'link.ruleUrl': 'linkRuleUrl',
  'link.executionLogUrl': 'linkExecutionLogUrl',
});

function hasRecipient(recipients, type) {
  return recipients.some(
    (recipient) => recipient?.type === type && String(recipient?.value || '').trim().length > 0,
  );
}

function getEffectiveRecipients(template, globalRecipients) {
  return Array.isArray(template?.recipients) && template.recipients.length
    ? template.recipients
    : globalRecipients;
}

/**
 * 返回通知配置校验对应的多语言键；配置有效时返回 null。
 */
export function getNotificationValidationErrorKey(channelTemplates = [], recipients = []) {
  const enabledTemplates = channelTemplates.filter((template) => template?.enabled === true);
  if (!enabledTemplates.length) return 'channelRequired';

  for (const template of enabledTemplates) {
    const effectiveRecipients = getEffectiveRecipients(template, recipients);
    if (Number(template?.channelType) === SITE_MESSAGE_CHANNEL_TYPE) {
      if (!hasRecipient(effectiveRecipients, 'EMPLOYEE')) return 'siteRecipientRequired';
      continue;
    }
    const hasRobotRecipient =
      hasRecipient(effectiveRecipients, 'PHONE') || hasRecipient(effectiveRecipients, 'ALL');
    if (!template?.atAll && !hasRobotRecipient) return 'robotRecipientRequired';
  }

  const hasIncompleteTemplate = enabledTemplates.some(
    (template) =>
      !String(template?.titleTemplate || '').trim() ||
      !String(template?.contentTemplate || '').trim(),
  );
  return hasIncompleteTemplate ? 'templateRequired' : null;
}

/**
 * 根据后端稳定编码返回前端多语言键；未知编码不转换，继续显示后端文案。
 */
export function getNotificationVariableLocaleKeys(groupCode, variableKey) {
  const groupLocaleKey = VARIABLE_GROUP_LOCALE_KEY_BY_CODE[groupCode];
  const variableLocaleKey = VARIABLE_LOCALE_KEY_BY_CODE[variableKey];
  return {
    groupName: groupLocaleKey ? `notificationVariableGroups.${groupLocaleKey}` : undefined,
    label: variableLocaleKey ? `notificationVariables.${variableLocaleKey}.label` : undefined,
    description: variableLocaleKey
      ? `notificationVariables.${variableLocaleKey}.description`
      : undefined,
  };
}

/**
 * 读取当前语言的原始消息，保留通知变量占位符，不交给 i18n 插值编译器处理。
 */
export function resolveNotificationTemplateMessages(preset, resolveMessage) {
  const titleTemplate = resolveMessage(preset?.titleTemplateKey);
  const contentTemplate = resolveMessage(preset?.contentTemplateKey);
  return {
    titleTemplate: typeof titleTemplate === 'string' ? titleTemplate : '',
    contentTemplate: typeof contentTemplate === 'string' ? contentTemplate : '',
  };
}
