import type { FormSchema } from '/@/components/Form';
import type { InfluxDbActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('influxdb');

export const influxDbBridgeAction: BridgeOutboundActionModule<InfluxDbActionConfigDto> = {
  type: 'INFLUXDB',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('measurementOverride.label')),
        field: 'ac_influxdb_measurementOverride',
        component: 'Input',
        componentProps: { placeholder: dsDefaultPlaceholder() },
      },
      {
        label: tWithOverride(tk('tagsMapping.label')),
        field: 'ac_influxdb_tagsMapping',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"productId":"${productId}"}' },
      },
      {
        label: tWithOverride(tk('fieldsMapping.label')),
        field: 'ac_influxdb_fieldsMapping',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"value":"${payload}"}' },
      },
    ];
  },

  assembleAction(values) {
    return {
      measurementOverride: values.ac_influxdb_measurementOverride,
      tagsMapping: values.ac_influxdb_tagsMapping,
      fieldsMapping: values.ac_influxdb_fieldsMapping,
    };
  },

  flattenAction(dto) {
    return {
      ac_influxdb_measurementOverride: dto?.measurementOverride,
      ac_influxdb_tagsMapping: dto?.tagsMapping,
      ac_influxdb_fieldsMapping: dto?.fieldsMapping,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_influxdb_measurementOverride: preset.measurementOverride,
          ac_influxdb_tagsMapping: preset.tagsMapping,
          ac_influxdb_fieldsMapping: preset.fieldsMapping,
        }
      : {};
  },
};
