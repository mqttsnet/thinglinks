import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.rabbitmq.${key}`);

/**
 * RabbitMQ 协议模块。
 *
 * <p>⚠ <b>字段名修复</b>：使用 {@code exchangeName}（不是旧的 {@code exchange}），
 * 与后端 {@code RabbitmqConnectionDto.exchangeName} 1:1 对齐。
 *
 * @author mqttsnet
 */
export const rabbitmqProtocol: ProtocolModule = {
  type: 'RABBITMQ',

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
        defaultValue: 5672,
        componentProps: { style: 'width:100%', min: 1, max: 65535 },
        helpMessage: tk('port.help'),
      },
      {
        label: tk('virtualHost.label'),
        field: 'virtualHost',
        component: 'Input',
        defaultValue: '/',
        helpMessage: tk('virtualHost.help'),
      },
      {
        label: tk('exchangeName.label'),
        field: 'exchangeName',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('exchangeName.placeholder') },
        helpMessage: tk('exchangeName.help'),
      },
      {
        label: tk('exchangeType.label'),
        field: 'exchangeType',
        component: 'Select',
        defaultValue: 'topic',
        componentProps: {
          options: [
            { label: tk('exchangeType.opt_direct'), value: 'direct' },
            { label: tk('exchangeType.opt_topic'), value: 'topic' },
            { label: tk('exchangeType.opt_fanout'), value: 'fanout' },
            { label: tk('exchangeType.opt_headers'), value: 'headers' },
          ],
        },
      },
      {
        label: tk('routingKey.label'),
        field: 'routingKey',
        component: 'Input',
        required: true,
        componentProps: { placeholder: 'device.${productId}.${actionType}' },
        helpMessage: tk('routingKey.help'),
      },
      {
        label: tk('useTls.label'),
        field: 'useTls',
        component: 'Switch',
        defaultValue: false,
        helpMessage: tk('useTls.help'),
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      {
        label: tk('username.label'),
        field: 'username',
        component: 'Input',
        defaultValue: 'guest',
        componentProps: { placeholder: tk('username.placeholder') },
      },
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
        key: 'local_topic',
        label: tk('presets.local_topic_label'),
        description: tk('presets.local_topic_description'),
        connection: {
          host: '127.0.0.1',
          port: 5672,
          virtualHost: '/',
          exchangeName: 'iot.events',
          exchangeType: 'topic',
          routingKey: 'device.${productId}.${actionType}',
          useTls: false,
        },
        credential: { username: 'guest' },
      },
      {
        key: 'fanout',
        label: tk('presets.fanout_label'),
        description: tk('presets.fanout_description'),
        connection: {
          host: '127.0.0.1',
          port: 5672,
          virtualHost: '/',
          exchangeName: 'iot.broadcast',
          exchangeType: 'fanout',
          routingKey: '',
          useTls: false,
        },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 2,
      defaultRetryBackoffMs: 1000,
      defaultTimeoutMs: 5000,
    };
  },
};
