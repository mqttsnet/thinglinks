import type { FormSchema } from '/@/components/Form';
import type { KingBaseActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tEnum, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('kingbase');

/**
 * 人大金仓 KingBase 出站动作配置模块（PG 兼容生态；国产 OLTP）。
 *
 * @author mqttsnet
 */
export const kingBaseBridgeAction: BridgeOutboundActionModule<KingBaseActionConfigDto> = {
  type: 'KINGBASE',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('tableOverride.label')),
        field: 'ac_kingbase_tableOverride',
        component: 'Input',
        componentProps: { placeholder: tk('tableOverride.placeholder') },
      },
      {
        label: tWithOverride(tk('columnMapping.label')),
        field: 'ac_kingbase_columnMapping',
        component: 'InputTextArea',
        componentProps: {
          rows: 3,
          placeholder: '{"device_id":"${deviceIdentification}","payload":"${payload}"}',
        },
      },
      {
        label: tWithOverride(tk('onConflict.label')),
        field: 'ac_kingbase_onConflictOverride',
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
      tableOverride: values.ac_kingbase_tableOverride,
      columnMapping: values.ac_kingbase_columnMapping,
      onConflictOverride: values.ac_kingbase_onConflictOverride,
    };
  },

  flattenAction(dto) {
    return {
      ac_kingbase_tableOverride: dto?.tableOverride,
      ac_kingbase_columnMapping: dto?.columnMapping,
      ac_kingbase_onConflictOverride: dto?.onConflictOverride,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_kingbase_tableOverride: preset.tableOverride,
          ac_kingbase_columnMapping: preset.columnMapping,
          ac_kingbase_onConflictOverride: preset.onConflictOverride,
        }
      : {};
  },
};
