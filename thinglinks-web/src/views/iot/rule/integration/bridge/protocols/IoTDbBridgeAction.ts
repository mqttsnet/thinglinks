import type { FormSchema } from '/@/components/Form';
import type { IoTDbActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tWithOverride } from './i18n';

const tk = tProto('iotdb');

export const ioTDbBridgeAction: BridgeOutboundActionModule<IoTDbActionConfigDto> = {
  type: 'IOTDB',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('timeseriesTemplateOverride.label')),
        field: 'ac_iotdb_timeseriesTemplateOverride',
        component: 'Input',
        componentProps: { placeholder: 'root.iot.${routingKey}' },
      },
      {
        label: tWithOverride(tk('columnMapping.label')),
        field: 'ac_iotdb_columnMapping',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"value":"${payload}"}' },
      },
    ];
  },

  assembleAction(values) {
    return {
      timeseriesTemplateOverride: values.ac_iotdb_timeseriesTemplateOverride,
      columnMapping: values.ac_iotdb_columnMapping,
    };
  },

  flattenAction(dto) {
    return {
      ac_iotdb_timeseriesTemplateOverride: dto?.timeseriesTemplateOverride,
      ac_iotdb_columnMapping: dto?.columnMapping,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_iotdb_timeseriesTemplateOverride: preset.timeseriesTemplateOverride,
          ac_iotdb_columnMapping: preset.columnMapping,
        }
      : {};
  },
};
