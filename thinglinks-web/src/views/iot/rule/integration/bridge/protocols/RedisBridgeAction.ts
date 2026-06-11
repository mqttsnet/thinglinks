import type { FormSchema } from '/@/components/Form';
import type { RedisActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tEnum, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('redis');

export const redisBridgeAction: BridgeOutboundActionModule<RedisActionConfigDto> = {
  type: 'REDIS',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('command.label')),
        field: 'ac_redis_command',
        component: 'Select',
        componentProps: {
          placeholder: dsDefaultPlaceholder(),
          allowClear: true,
          options: [
            { label: tEnum('LPUSH'), value: 'LPUSH' },
            { label: tEnum('RPUSH'), value: 'RPUSH' },
            { label: tEnum('XADD'), value: 'XADD' },
            { label: tEnum('PUBLISH'), value: 'PUBLISH' },
            { label: tEnum('SET'), value: 'SET' },
          ],
        },
      },
      {
        label: tWithOverride(tk('keyTemplate.label')),
        field: 'ac_redis_keyTemplate',
        component: 'Input',
        componentProps: { placeholder: dsDefaultPlaceholder() },
      },
      {
        label: tWithOverride(tk('ttlSeconds.label')),
        field: 'ac_redis_ttlSeconds',
        component: 'InputNumber',
        componentProps: { style: 'width:100%', min: 0 },
      },
    ];
  },

  assembleAction(values) {
    return {
      command: values.ac_redis_command,
      keyTemplate: values.ac_redis_keyTemplate,
      ttlSeconds: values.ac_redis_ttlSeconds,
    };
  },

  flattenAction(dto) {
    return {
      ac_redis_command: dto?.command,
      ac_redis_keyTemplate: dto?.keyTemplate,
      ac_redis_ttlSeconds: dto?.ttlSeconds,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_redis_command: preset.command,
          ac_redis_keyTemplate: preset.keyTemplate,
          ac_redis_ttlSeconds: preset.ttlSeconds,
        }
      : {};
  },
};
