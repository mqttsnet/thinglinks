import type { FormSchema } from '/@/components/Form';
import type { MysqlActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tEnum, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('mysql');

export const mysqlBridgeAction: BridgeOutboundActionModule<MysqlActionConfigDto> = {
  type: 'MYSQL',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('columnMapping.label')),
        field: 'ac_mysql_columnMapping',
        component: 'InputTextArea',
        componentProps: {
          rows: 4,
          placeholder: '{"device_id":"${deviceIdentification}","ts":"${timestamp}","payload":"${payload}"}',
        },
        helpMessage: tk('columnMapping.help'),
      },
      {
        label: tWithOverride(tk('onDuplicate.label')),
        field: 'ac_mysql_onDuplicate',
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
      columnMapping: values.ac_mysql_columnMapping,
      onDuplicate: values.ac_mysql_onDuplicate,
    };
  },

  flattenAction(dto) {
    return {
      ac_mysql_columnMapping: dto?.columnMapping,
      ac_mysql_onDuplicate: dto?.onDuplicate,
    };
  },

  presetToFlat(preset) {
    return preset
      ? { ac_mysql_columnMapping: preset.columnMapping, ac_mysql_onDuplicate: preset.onDuplicate }
      : {};
  },
};
