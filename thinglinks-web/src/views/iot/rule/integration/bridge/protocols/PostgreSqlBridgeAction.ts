import type { FormSchema } from '/@/components/Form';
import type { PostgreSqlActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tEnum, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('postgresql');

export const postgreSqlBridgeAction: BridgeOutboundActionModule<PostgreSqlActionConfigDto> = {
  type: 'POSTGRESQL',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('tableOverride.label')),
        field: 'ac_postgresql_tableOverride',
        component: 'Input',
        componentProps: { placeholder: tk('tableOverride.placeholder') },
      },
      {
        label: tWithOverride(tk('columnMapping.label')),
        field: 'ac_postgresql_columnMapping',
        component: 'InputTextArea',
        componentProps: {
          rows: 3,
          placeholder: '{"device_id":"${deviceIdentification}","payload":"${payload}"}',
        },
      },
      {
        label: tWithOverride(tk('onConflict.label')),
        field: 'ac_postgresql_onConflictOverride',
        component: 'Select',
        componentProps: {
          placeholder: dsDefaultPlaceholder(),
          allowClear: true,
          options: [
            { label: tEnum('INSERT'), value: 'INSERT' },
            { label: tEnum('UPSERT'), value: 'UPSERT' },
            { label: tEnum('IGNORE'), value: 'IGNORE' },
          ],
        },
      },
    ];
  },

  assembleAction(values) {
    return {
      tableOverride: values.ac_postgresql_tableOverride,
      columnMapping: values.ac_postgresql_columnMapping,
      onConflictOverride: values.ac_postgresql_onConflictOverride,
    };
  },

  flattenAction(dto) {
    return {
      ac_postgresql_tableOverride: dto?.tableOverride,
      ac_postgresql_columnMapping: dto?.columnMapping,
      ac_postgresql_onConflictOverride: dto?.onConflictOverride,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_postgresql_tableOverride: preset.tableOverride,
          ac_postgresql_columnMapping: preset.columnMapping,
          ac_postgresql_onConflictOverride: preset.onConflictOverride,
        }
      : {};
  },
};
