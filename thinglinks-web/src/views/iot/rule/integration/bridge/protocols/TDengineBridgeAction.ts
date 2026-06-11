import type { FormSchema } from '/@/components/Form';
import type { TDengineActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('tdengine');

export const tdengineBridgeAction: BridgeOutboundActionModule<TDengineActionConfigDto> = {
  type: 'TDENGINE',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('superTableOverride.label')),
        field: 'ac_tdengine_superTableOverride',
        component: 'Input',
        componentProps: { placeholder: dsDefaultPlaceholder() },
      },
      {
        label: tWithOverride(tk('childTableTemplate.label')),
        field: 'ac_tdengine_childTableTemplate',
        component: 'Input',
        componentProps: { placeholder: 'd_${deviceIdentification}' },
      },
      {
        label: tWithOverride(tk('tagsMapping.label')),
        field: 'ac_tdengine_tagsMapping',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"productId":"${productId}"}' },
      },
      {
        label: tWithOverride(tk('columnMapping.label')),
        field: 'ac_tdengine_columnMapping',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"ts":"${timestamp}","value":"${payload}"}' },
      },
    ];
  },

  assembleAction(values) {
    return {
      superTableOverride: values.ac_tdengine_superTableOverride,
      childTableTemplate: values.ac_tdengine_childTableTemplate,
      tagsMapping: values.ac_tdengine_tagsMapping,
      columnMapping: values.ac_tdengine_columnMapping,
    };
  },

  flattenAction(dto) {
    return {
      ac_tdengine_superTableOverride: dto?.superTableOverride,
      ac_tdengine_childTableTemplate: dto?.childTableTemplate,
      ac_tdengine_tagsMapping: dto?.tagsMapping,
      ac_tdengine_columnMapping: dto?.columnMapping,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_tdengine_superTableOverride: preset.superTableOverride,
          ac_tdengine_childTableTemplate: preset.childTableTemplate,
          ac_tdengine_tagsMapping: preset.tagsMapping,
          ac_tdengine_columnMapping: preset.columnMapping,
        }
      : {};
  },
};
