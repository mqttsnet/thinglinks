import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.kafka.${key}`);

/**
 * Kafka 协议模块。字段名严格对齐后端 {@code KafkaConnectionDto} / {@code KafkaCredentialDto}。
 *
 * @author mqttsnet
 */
export const kafkaProtocol: ProtocolModule = {
  type: 'KAFKA',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('bootstrapServers.label'),
        field: 'bootstrapServers',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('bootstrapServers.placeholder') },
        helpMessage: tk('bootstrapServers.help'),
      },
      {
        label: tk('topic.label'),
        field: 'topic',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('topic.placeholder') },
        helpMessage: '数据投递到的 topic 名称。支持模板变量 ${tenantId} ${productId}',
      },
      {
        label: tk('clientId.label'),
        field: 'clientId',
        component: 'Input',
        componentProps: { placeholder: 'thinglinks-bridge-${dsId}' },
        helpMessage: tk('clientId.help'),
      },
      {
        label: tk('partitionStrategy.label'),
        field: 'partitionStrategy',
        component: 'Select',
        defaultValue: 'hash',
        componentProps: {
          options: [
            { label: tk('partitionStrategy.opt_hash'), value: 'hash' },
            { label: tk('partitionStrategy.opt_roundRobin'), value: 'roundRobin' },
            { label: tk('partitionStrategy.opt_sticky'), value: 'sticky' },
            { label: tk('partitionStrategy.opt_manual'), value: 'manual' },
          ],
        },
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
        label: tk('saslMechanism.label'),
        field: 'saslMechanism',
        component: 'Select',
        defaultValue: 'SCRAM-SHA-256',
        componentProps: {
          options: [
            { label: t('iot.rule.integration.protocols.common.saslMechanism_PLAIN'), value: 'PLAIN' },
            { label: t('iot.rule.integration.protocols.common.saslMechanism_SCRAM_256'), value: 'SCRAM-SHA-256' },
            { label: t('iot.rule.integration.protocols.common.saslMechanism_SCRAM_512'), value: 'SCRAM-SHA-512' },
          ],
        },
      },
      {
        label: tk('saslUsername.label'),
        field: 'saslUsername',
        component: 'Input',
        componentProps: { placeholder: tk('saslUsername.placeholder') },
        helpMessage: tk('saslUsername.help'),
      },
      {
        label: tk('saslPassword.label'),
        field: 'saslPassword',
        component: 'InputPassword',
        helpMessage: tk('saslPassword.help'),
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
          bootstrapServers: '127.0.0.1:9092',
          topic: 'iot-test',
          partitionStrategy: 'hash',
          useTls: false,
        },
      },
      {
        key: 'aliyun_sasl',
        label: tk('presets.aliyun_sasl_label'),
        description: tk('presets.aliyun_sasl_description'),
        connection: {
          bootstrapServers: 'alikafka-xxx.aliyuncs.com:9093',
          topic: 'iot-prod',
          partitionStrategy: 'hash',
          useTls: true,
        },
        credential: {
          saslMechanism: 'PLAIN',
          saslUsername: 'alikafka_user',
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
      defaultRateLimitQps: 0,
    };
  },
};
