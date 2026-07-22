import type { FormSchema } from '/@/components/Form';
import type { RabbitmqActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tWithOverride } from './i18n';

const tk = tProto('rabbitmq');

export const rabbitmqBridgeAction: BridgeOutboundActionModule<RabbitmqActionConfigDto> = {
  type: 'RABBITMQ',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('routingKey.label')),
        field: 'ac_rabbitmq_routingKey',
        component: 'Input',
        componentProps: { placeholder: 'device.${productId}.${actionType}' },
      },
      {
        label: tk('properties.label'),
        field: 'ac_rabbitmq_properties',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"deliveryMode":2}' },
        helpMessage: tk('properties.help'),
      },
    ];
  },

  assembleAction(values) {
    return {
      routingKey: values.ac_rabbitmq_routingKey,
      properties: values.ac_rabbitmq_properties,
    };
  },

  flattenAction(dto) {
    return {
      ac_rabbitmq_routingKey: dto?.routingKey,
      ac_rabbitmq_properties: dto?.properties,
    };
  },

  presetToFlat(preset) {
    return preset
      ? { ac_rabbitmq_routingKey: preset.routingKey, ac_rabbitmq_properties: preset.properties }
      : {};
  },
};
