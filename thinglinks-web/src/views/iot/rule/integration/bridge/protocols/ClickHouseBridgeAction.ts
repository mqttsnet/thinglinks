import type { FormSchema } from '/@/components/Form';
import type { ClickHouseActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('clickhouse');

export const clickHouseBridgeAction: BridgeOutboundActionModule<ClickHouseActionConfigDto> = {
  type: 'CLICKHOUSE',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('tableOverride.label')),
        field: 'ac_clickhouse_tableOverride',
        component: 'Input',
        componentProps: { placeholder: dsDefaultPlaceholder() },
      },
      {
        label: tWithOverride(tk('columnMapping.label')),
        field: 'ac_clickhouse_columnMapping',
        component: 'InputTextArea',
        componentProps: { rows: 3, placeholder: '{"ts":"${timestamp}","payload":"${payload}"}' },
      },
    ];
  },

  assembleAction(values) {
    return {
      tableOverride: values.ac_clickhouse_tableOverride,
      columnMapping: values.ac_clickhouse_columnMapping,
    };
  },

  flattenAction(dto) {
    return {
      ac_clickhouse_tableOverride: dto?.tableOverride,
      ac_clickhouse_columnMapping: dto?.columnMapping,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_clickhouse_tableOverride: preset.tableOverride,
          ac_clickhouse_columnMapping: preset.columnMapping,
        }
      : {};
  },
};
