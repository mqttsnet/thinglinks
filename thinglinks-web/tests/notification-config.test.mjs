import assert from 'node:assert/strict';
import test from 'node:test';

import {
  getNotificationValidationErrorKey,
  getNotificationVariableLocaleKeys,
  resolveNotificationTemplateMessages,
} from '../src/views/iot/rule/engine/linkage/components/modal/notificationTemplates/notificationConfig.mjs';

const robotTemplate = {
  channelType: 0,
  enabled: true,
  atAll: false,
  titleTemplate: 'Alarm',
  contentTemplate: 'Device offline',
};

const siteMessageTemplate = {
  channelType: 3,
  enabled: true,
  titleTemplate: 'Alarm',
  contentTemplate: 'Device offline',
};

test('至少启用一个通知渠道', () => {
  assert.equal(getNotificationValidationErrorKey([], []), 'channelRequired');
});

test('机器人渠道需要手机号接收人或开启全员提醒', () => {
  assert.equal(
    getNotificationValidationErrorKey([robotTemplate], [{ type: 'EMPLOYEE', value: '1001' }]),
    'robotRecipientRequired',
  );
  assert.equal(getNotificationValidationErrorKey([{ ...robotTemplate, atAll: true }], []), null);
});

test('渠道级接收人覆盖全局接收人', () => {
  assert.equal(
    getNotificationValidationErrorKey(
      [
        {
          ...robotTemplate,
          recipients: [{ type: 'PHONE', value: '13800000000' }],
        },
      ],
      [],
    ),
    null,
  );
  assert.equal(
    getNotificationValidationErrorKey(
      [
        {
          ...robotTemplate,
          recipients: [{ type: 'EMPLOYEE', value: '1001' }],
        },
      ],
      [{ type: 'PHONE', value: '13800000000' }],
    ),
    'robotRecipientRequired',
  );
});

test('站内信渠道必须配置员工接收人', () => {
  assert.equal(
    getNotificationValidationErrorKey(
      [siteMessageTemplate],
      [{ type: 'PHONE', value: '13800000000' }],
    ),
    'siteRecipientRequired',
  );
  assert.equal(
    getNotificationValidationErrorKey([siteMessageTemplate], [{ type: 'EMPLOYEE', value: '1001' }]),
    null,
  );
  assert.equal(
    getNotificationValidationErrorKey(
      [
        {
          ...siteMessageTemplate,
          recipients: [{ type: 'EMPLOYEE', value: '1001' }],
        },
      ],
      [],
    ),
    null,
  );
  assert.equal(
    getNotificationValidationErrorKey(
      [
        {
          ...siteMessageTemplate,
          recipients: [{ type: 'PHONE', value: '13800000000' }],
        },
      ],
      [{ type: 'EMPLOYEE', value: '1001' }],
    ),
    'siteRecipientRequired',
  );
});

test('未启用的站内信渠道不要求员工接收人', () => {
  assert.equal(
    getNotificationValidationErrorKey(
      [robotTemplate, { ...siteMessageTemplate, enabled: false }],
      [{ type: 'PHONE', value: '13800000000' }],
    ),
    null,
  );
});

test('启用渠道的标题和正文不能是空白字符', () => {
  assert.equal(
    getNotificationValidationErrorKey(
      [{ ...robotTemplate, titleTemplate: '   ' }],
      [{ type: 'PHONE', value: '13800000000' }],
    ),
    'templateRequired',
  );
});

test('已知通知变量使用稳定的多语言键，未知变量保留服务端文案', () => {
  assert.deepEqual(getNotificationVariableLocaleKeys('rule', 'rule.name'), {
    groupName: 'notificationVariableGroups.rule',
    label: 'notificationVariables.ruleName.label',
    description: 'notificationVariables.ruleName.description',
  });
  assert.deepEqual(getNotificationVariableLocaleKeys('custom', 'custom.value'), {
    groupName: undefined,
    label: undefined,
    description: undefined,
  });
});

test('通知预设通过当前语言的原始消息解析标题和正文', () => {
  const messages = {
    'preset.title': 'Alert: ${alarm.name}',
    'preset.content': 'Device: ${device.name}',
  };
  assert.deepEqual(
    resolveNotificationTemplateMessages(
      {
        titleTemplateKey: 'preset.title',
        contentTemplateKey: 'preset.content',
      },
      (key) => messages[key],
    ),
    {
      titleTemplate: 'Alert: ${alarm.name}',
      contentTemplate: 'Device: ${device.name}',
    },
  );
});
