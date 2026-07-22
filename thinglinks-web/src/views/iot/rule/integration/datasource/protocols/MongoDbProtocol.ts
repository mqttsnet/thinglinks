import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.mongodb.${key}`);

/**
 * MongoDB 协议模块（mongodb-driver-sync；存原始 JSON / 复杂业务对象）。
 *
 * @author mqttsnet
 */
export const mongoDbProtocol: ProtocolModule = {
  type: 'MONGODB',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('uri.label'),
        field: 'uri',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('uri.placeholder') },
        helpMessage: tk('uri.help'),
      },
      {
        label: tk('database.label'),
        field: 'database',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('database.placeholder') },
      },
      {
        label: tk('collection.label'),
        field: 'collection',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('collection.placeholder') },
        helpMessage: '集合名；支持模板 ${productId}',
      },
      {
        label: tk('writeMode.label'),
        field: 'writeMode',
        component: 'Select',
        defaultValue: 'INSERT',
        componentProps: {
          options: [
            { label: tk('writeMode.opt_INSERT'), value: 'INSERT' },
            { label: tk('writeMode.opt_UPSERT'), value: 'UPSERT' },
          ],
        },
      },
      {
        label: tk('upsertKey.label'),
        field: 'upsertKey',
        component: 'Input',
        componentProps: { placeholder: tk('upsertKey.placeholder') },
        helpMessage: tk('upsertKey.help'),
        ifShow: ({ values }) => values.writeMode === 'UPSERT',
      },
      {
        label: tk('writeConcern.label'),
        field: 'writeConcern',
        component: 'Select',
        defaultValue: 'ACKNOWLEDGED',
        componentProps: {
          options: [
            { label: tk('writeConcern.opt_UNACKNOWLEDGED'), value: 'UNACKNOWLEDGED' },
            { label: tk('writeConcern.opt_ACKNOWLEDGED'), value: 'ACKNOWLEDGED' },
            { label: tk('writeConcern.opt_MAJORITY'), value: 'MAJORITY' },
          ],
        },
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      { label: tk('username.label'), field: 'username', component: 'Input', helpMessage: tk('username.help') },
      { label: tk('password.label'), field: 'password', component: 'InputPassword', helpMessage: tk('password.help') },
      { label: tk('authDatabase.label'), field: 'authDatabase', component: 'Input', defaultValue: 'admin' },
    ];
  },

  examplePresets(): ProtocolPreset[] {
    return [
      {
        key: 'local',
        label: tk('presets.local_label'),
        description: tk('presets.local_description'),
        connection: {
          uri: 'mongodb://127.0.0.1:27017',
          database: 'iot',
          collection: 'device_data',
          writeMode: 'INSERT',
          writeConcern: 'ACKNOWLEDGED',
        },
      },
      {
        key: 'upsert',
        label: tk('presets.upsert_label'),
        description: tk('presets.upsert_description'),
        connection: {
          uri: 'mongodb://127.0.0.1:27017',
          database: 'iot',
          collection: 'device_latest',
          writeMode: 'UPSERT',
          upsertKey: 'deviceId',
          writeConcern: 'MAJORITY',
        },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 2,
      defaultRetryBackoffMs: 500,
      defaultTimeoutMs: 3000,
    };
  },
};
