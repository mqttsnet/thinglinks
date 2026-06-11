import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.pulsar.${key}`);

/**
 * Apache Pulsar 协议模块（Kafka 替代品，多租户原生 + tiered storage）。
 *
 * @author mqttsnet
 */
export const pulsarProtocol: ProtocolModule = {
  type: 'PULSAR',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('serviceUrl.label'),
        field: 'serviceUrl',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('serviceUrl.placeholder') },
        helpMessage: tk('serviceUrl.help'),
      },
      {
        label: tk('topic.label'),
        field: 'topic',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('topic.placeholder') },
        helpMessage: tk('topic.help'),
      },
      {
        label: tk('producerName.label'),
        field: 'producerName',
        component: 'Input',
        componentProps: { placeholder: tk('producerName.placeholder') },
        helpMessage: tk('producerName.help'),
      },
      {
        label: tk('sendMode.label'),
        field: 'sendMode',
        component: 'Select',
        defaultValue: 'ASYNC',
        componentProps: {
          options: [
            { label: tk('sendMode.opt_ASYNC'), value: 'ASYNC' },
            { label: tk('sendMode.opt_SYNC'), value: 'SYNC' },
          ],
        },
      },
      {
        label: tk('compressionType.label'),
        field: 'compressionType',
        component: 'Select',
        defaultValue: 'LZ4',
        componentProps: {
          options: [
            { label: tk('compressionType.opt_NONE'), value: 'NONE' },
            { label: tk('compressionType.opt_LZ4'), value: 'LZ4' },
            { label: tk('compressionType.opt_ZLIB'), value: 'ZLIB' },
            { label: tk('compressionType.opt_ZSTD'), value: 'ZSTD' },
            { label: tk('compressionType.opt_SNAPPY'), value: 'SNAPPY' },
          ],
        },
      },
      {
        label: tk('enableBatching.label'),
        field: 'enableBatching',
        component: 'Switch',
        defaultValue: true,
        helpMessage: tk('enableBatching.help'),
      },
      {
        label: tk('schemaType.label'),
        field: 'schemaType',
        component: 'Select',
        defaultValue: 'BYTES',
        componentProps: {
          options: [
            { label: tk('schemaType.opt_BYTES'), value: 'BYTES' },
            { label: tk('schemaType.opt_STRING'), value: 'STRING' },
            { label: tk('schemaType.opt_JSON'), value: 'JSON' },
            { label: tk('schemaType.opt_AVRO'), value: 'AVRO' },
          ],
        },
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      {
        label: tk('authToken.label'),
        field: 'authToken',
        component: 'InputPassword',
        helpMessage: tk('authToken.help'),
      },
      {
        label: tk('tlsCert.label'),
        field: 'tlsCert',
        component: 'InputTextArea',
        componentProps: { rows: 3, placeholder: tk('tlsCert.placeholder') },
        helpMessage: tk('tlsCert.help'),
      },
      {
        label: tk('tlsKey.label'),
        field: 'tlsKey',
        component: 'InputTextArea',
        componentProps: { rows: 3, placeholder: tk('tlsKey.placeholder') },
        helpMessage: tk('tlsKey.help'),
      },
    ];
  },

  examplePresets(): ProtocolPreset[] {
    return [
      {
        key: 'local_no_auth',
        label: tk('presets.local_no_auth_label'),
        description: tk('presets.local_no_auth_description'),
        connection: {
          serviceUrl: 'pulsar://127.0.0.1:6650',
          topic: 'persistent://public/default/iot-test',
          sendMode: 'ASYNC',
          compressionType: 'LZ4',
          enableBatching: true,
          schemaType: 'BYTES',
        },
      },
      {
        key: 'jwt_prod',
        label: tk('presets.jwt_prod_label'),
        description: tk('presets.jwt_prod_description'),
        connection: {
          serviceUrl: 'pulsar+ssl://pulsar.example.com:6651',
          topic: 'persistent://iot-prod/default/device-data',
          sendMode: 'ASYNC',
          compressionType: 'ZSTD',
          enableBatching: true,
        },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 0,
      defaultRetryBackoffMs: 1000,
      defaultTimeoutMs: 5000,
    };
  },
};
