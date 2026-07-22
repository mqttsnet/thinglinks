import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.iotdb.${key}`);

/**
 * Apache IoTDB 协议模块（jdbc 模式；Apache 顶级时序）。
 *
 * @author mqttsnet
 */
export const ioTDbProtocol: ProtocolModule = {
  type: 'IOTDB',

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
        defaultValue: 6667,
        componentProps: { style: 'width:100%', min: 1, max: 65535 },
      },
      {
        label: tk('storageGroup.label'),
        field: 'storageGroup',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('storageGroup.placeholder') },
        helpMessage: tk('storageGroup.help'),
      },
      {
        label: tk('timeseriesTemplate.label'),
        field: 'timeseriesTemplate',
        component: 'Input',
        required: true,
        componentProps: { placeholder: 'root.iot.${routingKey}' },
        helpMessage: tk('timeseriesTemplate.help'),
      },
      {
        label: tk('columnMapping.label'),
        field: 'columnMapping',
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 3, placeholder: '{ "value": "${payload}" }' },
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
        defaultValue: 'root',
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
          port: 6667,
          storageGroup: 'root.iot',
          timeseriesTemplate: 'root.iot.${routingKey}',
          columnMapping: '{"value":"${payload}"}',
        },
        credential: { username: 'root', password: 'root' },
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
