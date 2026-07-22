import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.kingbase.${key}`);

/**
 * 人大金仓 KingBase 协议模块（PG 兼容生态；国产 OLTP）。
 *
 * @author mqttsnet
 */
export const kingBaseProtocol: ProtocolModule = {
  type: 'KINGBASE',

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
        defaultValue: 'system',
        componentProps: { placeholder: tk('username.placeholder') },
      },
      {
        label: tk('schemaName.label'),
        field: 'schemaName',
        component: 'Input',
        defaultValue: 'public',
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
        label: tk('onConflict.label'),
        field: 'onConflict',
        component: 'Select',
        defaultValue: 'INSERT',
        componentProps: {
          options: [
            { label: tk('onConflict.opt_INSERT'), value: 'INSERT' },
            { label: tk('onConflict.opt_UPSERT'), value: 'UPSERT' },
            { label: tk('onConflict.opt_IGNORE'), value: 'IGNORE' },
          ],
        },
      },
      {
        label: tk('conflictKeys.label'),
        field: 'conflictKeys',
        component: 'Input',
        componentProps: { placeholder: tk('conflictKeys.placeholder') },
        helpMessage: tk('conflictKeys.help'),
        ifShow: ({ values }) => values.onConflict === 'UPSERT',
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
          jdbcUrl: 'jdbc:kingbase8://127.0.0.1:54321/iot',
          username: 'system',
          schemaName: 'public',
          table: 'iot_data',
          columnMapping:
            '{"device_id":"${deviceIdentification}","ts":"${timestamp}","payload":"${payload}"}',
          onConflict: 'INSERT',
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
