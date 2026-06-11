import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.clickhouse.${key}`);

/**
 * ClickHouse 协议模块（列存 OLAP，写实时大宽表）。
 *
 * @author mqttsnet
 */
export const clickHouseProtocol: ProtocolModule = {
  type: 'CLICKHOUSE',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('jdbcUrl.label'),
        field: 'jdbcUrl',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('jdbcUrl.placeholder') },
        helpMessage: tk('jdbcUrl.help'),
      },
      {
        label: tk('database.label'),
        field: 'database',
        component: 'Input',
        componentProps: { placeholder: tk('database.placeholder') },
      },
      {
        label: tk('table.label'),
        field: 'table',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('table.placeholder') },
      },
      {
        label: tk('columnMapping.label'),
        field: 'columnMapping',
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 4, placeholder: '{ "ts": "${timestamp}", "device_id": "${deviceIdentification}", "payload": "${payload}" }' },
        helpMessage: 'JSON 格式：{表字段: 占位符}',
      },
      {
        label: tk('useAsyncInsert.label'),
        field: 'useAsyncInsert',
        component: 'Switch',
        defaultValue: true,
        helpMessage: tk('useAsyncInsert.help'),
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      { label: tk('username.label'), field: 'username', component: 'Input', defaultValue: 'default' },
      {
        label: tk('password.label'),
        field: 'password',
        component: 'InputPassword',
        helpMessage: tk('password.help'),
      },
    ];
  },

  examplePresets(): ProtocolPreset[] {
    return [
      {
        key: 'local',
        label: tk('presets.local_label'),
        description: tk('presets.local_description'),
        connection: {
          jdbcUrl: 'jdbc:clickhouse://127.0.0.1:8123/iot',
          database: 'iot',
          table: 'device_data_wide',
          columnMapping:
            '{"ts":"${timestamp}","device_id":"${deviceIdentification}","payload":"${payload}"}',
          useAsyncInsert: true,
        },
        credential: { username: 'default' },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 1,
      defaultRetryBackoffMs: 1000,
      defaultTimeoutMs: 5000,
    };
  },
};
