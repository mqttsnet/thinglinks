import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.dm.${key}`);

/**
 * 达梦数据库 DM 协议模块（国产 OLTP）。
 *
 * @author mqttsnet
 */
export const dmProtocol: ProtocolModule = {
  type: 'DM',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('jdbcUrl.label'),
        field: 'jdbcUrl',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('jdbcUrl.placeholder') },
      },
      {
        label: tk('username.label'),
        field: 'username',
        component: 'Input',
        required: true,
        defaultValue: 'SYSDBA',
        componentProps: { placeholder: tk('username.placeholder') },
      },
      {
        label: tk('schemaName.label'),
        field: 'schemaName',
        component: 'Input',
        helpMessage: tk('schemaName.help'),
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
        componentProps: { rows: 4, placeholder: '{ "device_id": "${deviceIdentification}", "ts": "${timestamp}", "payload": "${payload}" }' },
      },
      {
        label: tk('onDuplicate.label'),
        field: 'onDuplicate',
        component: 'Select',
        defaultValue: 'INSERT',
        componentProps: {
          options: [
            { label: tk('onDuplicate.opt_INSERT'), value: 'INSERT' },
            { label: tk('onDuplicate.opt_UPDATE'), value: 'UPDATE' },
            { label: tk('onDuplicate.opt_IGNORE'), value: 'IGNORE' },
          ],
        },
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      {
        label: tk('password.label'),
        field: 'password',
        component: 'InputPassword',
        required: true,
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
          jdbcUrl: 'jdbc:dm://127.0.0.1:5236/IOT',
          username: 'SYSDBA',
          schemaName: 'IOT',
          table: 'iot_data',
          columnMapping:
            '{"device_id":"${deviceIdentification}","ts":"${timestamp}","payload":"${payload}"}',
          onDuplicate: 'INSERT',
        },
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
