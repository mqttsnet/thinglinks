import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.mqtt.${key}`);

/**
 * MQTT 协议模块（外部 MQTT broker 互联）。
 *
 * @author mqttsnet
 */
export const mqttProtocol: ProtocolModule = {
  type: 'MQTT',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('broker.label'),
        field: 'broker',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('broker.placeholder') },
        helpMessage: tk('broker.help'),
      },
      {
        label: tk('clientId.label'),
        field: 'clientId',
        component: 'Input',
        componentProps: { placeholder: 'thinglinks-bridge-${dsId}' },
        helpMessage: tk('clientId.help'),
      },
      {
        label: tk('topicTemplate.label'),
        field: 'topicTemplate',
        component: 'Input',
        required: true,
        componentProps: { placeholder: 'out/${productId}/${deviceId}' },
        helpMessage: tk('topicTemplate.help'),
      },
      {
        label: tk('qos.label'),
        field: 'qos',
        component: 'Select',
        defaultValue: 1,
        componentProps: {
          options: [
            { label: tk('qos.opt_0'), value: 0 },
            { label: tk('qos.opt_1'), value: 1 },
            { label: tk('qos.opt_2'), value: 2 },
          ],
        },
        helpMessage: tk('qos.help'),
      },
      {
        label: tk('retained.label'),
        field: 'retained',
        component: 'Switch',
        defaultValue: false,
        helpMessage: tk('retained.help'),
      },
      {
        label: tk('username.label'),
        field: 'username',
        component: 'Input',
        helpMessage: tk('username.help'),
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      {
        label: tk('password.label'),
        field: 'password',
        component: 'InputPassword',
        helpMessage: tk('password.help'),
      },
      {
        label: tk('caCert.label'),
        field: 'caCert',
        component: 'InputTextArea',
        componentProps: { rows: 4, placeholder: tk('caCert.placeholder') },
        helpMessage: tk('caCert.help'),
      },
    ];
  },

  examplePresets(): ProtocolPreset[] {
    return [
      {
        key: 'tcp_no_auth',
        label: tk('presets.tcp_no_auth_label'),
        description: tk('presets.tcp_no_auth_description'),
        connection: {
          broker: 'tcp://broker.example.com:1883',
          topicTemplate: 'out/${productId}/${deviceId}',
          qos: 1,
          retained: false,
        },
      },
      {
        key: 'ssl_with_auth',
        label: tk('presets.ssl_with_auth_label'),
        description: tk('presets.ssl_with_auth_description'),
        connection: {
          broker: 'ssl://broker.example.com:8883',
          topicTemplate: 'out/${productId}/${deviceId}',
          qos: 1,
          retained: false,
          username: 'iot_bridge',
        },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 2,
      defaultRetryBackoffMs: 1000,
      defaultTimeoutMs: 3000,
    };
  },
};
