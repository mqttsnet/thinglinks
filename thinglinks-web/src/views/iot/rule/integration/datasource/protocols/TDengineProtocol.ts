import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.tdengine.${key}`);

/**
 * TDengine 协议模块（IoT 时序首选；JDBC-RESTful 模式跨平台部署友好）。
 *
 * @author mqttsnet
 */
export const tdengineProtocol: ProtocolModule = {
  type: 'TDENGINE',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('host.label'),
        field: 'host',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('host.placeholder') },
      },
      {
        label: tk('port.label'),
        field: 'port',
        component: 'InputNumber',
        defaultValue: 6041,
        componentProps: { style: 'width:100%', min: 1, max: 65535 },
        helpMessage: tk('port.help'),
      },
      {
        label: tk('database.label'),
        field: 'database',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('database.placeholder') },
      },
      {
        label: tk('superTable.label'),
        field: 'superTable',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('superTable.placeholder') },
        helpMessage: tk('superTable.help'),
      },
      {
        label: tk('childTableTemplate.label'),
        field: 'childTableTemplate',
        component: 'Input',
        componentProps: { placeholder: 'd_${deviceIdentification}' },
        helpMessage: tk('childTableTemplate.help'),
      },
      {
        label: tk('tagsMapping.label'),
        field: 'tagsMapping',
        component: 'InputTextArea',
        componentProps: { rows: 3, placeholder: '{ "productId": "${productId}", "deviceId": "${deviceIdentification}" }' },
        helpMessage: tk('tagsMapping.help'),
      },
      {
        label: tk('columnMapping.label'),
        field: 'columnMapping',
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 3, placeholder: '{ "ts": "${timestamp}", "value": "${payload}" }' },
        helpMessage: tk('columnMapping.help'),
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      { label: tk('username.label'), field: 'username', component: 'Input', defaultValue: 'root' },
      {
        label: tk('password.label'),
        field: 'password',
        component: 'InputPassword',
        defaultValue: 'taosdata',
        helpMessage: tk('password.help'),
      },
    ];
  },

  examplePresets(): ProtocolPreset[] {
    return [
      {
        key: 'local_default',
        label: tk('presets.local_default_label'),
        description: tk('presets.local_default_description'),
        connection: {
          host: '127.0.0.1',
          port: 6041,
          database: 'iot',
          superTable: 'device_data',
          childTableTemplate: 'd_${deviceIdentification}',
          tagsMapping: '{"productId":"${productId}","deviceId":"${deviceIdentification}"}',
          columnMapping: '{"ts":"${timestamp}","value":"${payload}"}',
        },
        credential: { username: 'root', password: 'taosdata' },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 2,
      defaultRetryBackoffMs: 500,
      defaultTimeoutMs: 5000,
    };
  },
};
