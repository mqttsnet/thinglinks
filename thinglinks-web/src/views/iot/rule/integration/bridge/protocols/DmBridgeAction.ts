import type { FormSchema } from '/@/components/Form';
import type { DmActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tEnum, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('dm');

/**
 * 达梦 DM 出站动作配置模块（国产 OLTP；MySQL 风格语法兼容）。
 *
 * @author mqttsnet
 */
export const dmBridgeAction: BridgeOutboundActionModule<DmActionConfigDto> = {
  type: 'DM',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('tableOverride.label')),
        field: 'ac_dm_tableOverride',
        component: 'Input',
        componentProps: { placeholder: tk('tableOverride.placeholder') },
      },
      {
        label: tWithOverride(tk('columnMapping.label')),
        field: 'ac_dm_columnMapping',
        component: 'InputTextArea',
        componentProps: {
          rows: 3,
          placeholder: '{"device_id":"${deviceIdentification}","payload":"${payload}"}',
        },
      },
      {
        label: tWithOverride(tk('onDuplicate.label')),
        field: 'ac_dm_onDuplicateOverride',
        component: 'Select',
        componentProps: {
          placeholder: dsDefaultPlaceholder(),
          allowClear: true,
          options: [
            { label: tEnum('INSERT'), value: 'INSERT' },
            { label: tEnum('UPDATE'), value: 'UPDATE' },
            { label: tEnum('IGNORE'), value: 'IGNORE' },
          ],
        },
      },
    ];
  },

  assembleAction(values) {
    return {
      tableOverride: values.ac_dm_tableOverride,
      columnMapping: values.ac_dm_columnMapping,
      onDuplicateOverride: values.ac_dm_onDuplicateOverride,
    };
  },

  flattenAction(dto) {
    return {
      ac_dm_tableOverride: dto?.tableOverride,
      ac_dm_columnMapping: dto?.columnMapping,
      ac_dm_onDuplicateOverride: dto?.onDuplicateOverride,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_dm_tableOverride: preset.tableOverride,
          ac_dm_columnMapping: preset.columnMapping,
          ac_dm_onDuplicateOverride: preset.onDuplicateOverride,
        }
      : {};
  },
};
