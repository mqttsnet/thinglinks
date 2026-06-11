import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.postgresql.${key}`);

/**
 * PostgreSQL 协议模块（通用 OLTP 关系库；jdbc + HikariCP）。
 *
 * @author mqttsnet
 */
export const postgreSqlProtocol: ProtocolModule = {
  type: 'POSTGRESQL',

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
          jdbcUrl: 'jdbc:postgresql://127.0.0.1:5432/iot_dev',
          username: 'postgres',
          schemaName: 'public',
          table: 'iot_data',
          columnMapping:
            '{"device_id":"${deviceIdentification}","ts":"${timestamp}","payload":"${payload}"}',
          onConflict: 'INSERT',
        },
      },
      {
        key: 'upsert',
        label: tk('presets.upsert_label'),
        description: tk('presets.upsert_description'),
        connection: {
          jdbcUrl: 'jdbc:postgresql://127.0.0.1:5432/iot_prod',
          username: 'iot_writer',
          schemaName: 'public',
          table: 'iot_data',
          columnMapping:
            '{"device_id":"${deviceIdentification}","ts":"${timestamp}","payload":"${payload}"}',
          onConflict: 'UPSERT',
          conflictKeys: 'device_id,ts',
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
