import type { FormSchema } from '/@/components/Form';
import type { RocketmqActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tWithOverride } from './i18n';

const tk = tProto('rocketmq');

export const rocketmqBridgeAction: BridgeOutboundActionModule<RocketmqActionConfigDto> = {
  type: 'ROCKETMQ',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('tag.label')),
        field: 'ac_rocketmq_tag',
        component: 'Input',
        componentProps: { placeholder: '${actionType}' },
      },
      {
        label: tk('hashKey.label'),
        field: 'ac_rocketmq_hashKey',
        component: 'Input',
        componentProps: { placeholder: '${deviceIdentification}' },
        helpMessage: tk('hashKey.help'),
      },
    ];
  },

  assembleAction(values) {
    return {
      tag: values.ac_rocketmq_tag,
      hashKey: values.ac_rocketmq_hashKey,
    };
  },

  flattenAction(dto) {
    return {
      ac_rocketmq_tag: dto?.tag,
      ac_rocketmq_hashKey: dto?.hashKey,
    };
  },

  presetToFlat(preset) {
    return preset ? { ac_rocketmq_tag: preset.tag, ac_rocketmq_hashKey: preset.hashKey } : {};
  },
};
