import type { FormSchema } from '/@/components/Form';
import type { KafkaActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto } from './i18n';

const tk = tProto('kafka');

/**
 * Kafka 出站动作模块（桥接规则的 Kafka 子段；落 action_config_json.kafka）。
 *
 * @author mqttsnet
 */
export const kafkaBridgeAction: BridgeOutboundActionModule<KafkaActionConfigDto> = {
  type: 'KAFKA',

  actionFields(): FormSchema[] {
    return [
      {
        label: tk('partitionKey.label'),
        field: 'ac_kafka_partitionKey',
        component: 'Input',
        componentProps: { placeholder: '${deviceIdentification}' },
        helpMessage: tk('partitionKey.help'),
      },
      {
        label: tk('headers.label'),
        field: 'ac_kafka_headers',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"X-Source":"thinglinks","X-Tenant":"${tenantId}"}' },
        helpMessage: tk('headers.help'),
      },
    ];
  },

  assembleAction(values) {
    return {
      partitionKey: values.ac_kafka_partitionKey,
      headers: values.ac_kafka_headers,
    };
  },

  flattenAction(dto) {
    return {
      ac_kafka_partitionKey: dto?.partitionKey,
      ac_kafka_headers: dto?.headers,
    };
  },

  presetToFlat(preset) {
    return preset
      ? { ac_kafka_partitionKey: preset.partitionKey, ac_kafka_headers: preset.headers }
      : {};
  },
};
