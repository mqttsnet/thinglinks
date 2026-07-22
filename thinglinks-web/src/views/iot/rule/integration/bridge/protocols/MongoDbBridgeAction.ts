import type { FormSchema } from '/@/components/Form';
import type { MongoDbActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tEnum, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('mongodb');

export const mongoDbBridgeAction: BridgeOutboundActionModule<MongoDbActionConfigDto> = {
  type: 'MONGODB',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('collectionOverride.label')),
        field: 'ac_mongodb_collectionOverride',
        component: 'Input',
        componentProps: { placeholder: dsDefaultPlaceholder() },
      },
      {
        label: tWithOverride(tk('writeModeOverride.label')),
        field: 'ac_mongodb_writeModeOverride',
        component: 'Select',
        componentProps: {
          placeholder: dsDefaultPlaceholder(),
          allowClear: true,
          options: [
            { label: tEnum('INSERT'), value: 'INSERT' },
            { label: tEnum('UPSERT'), value: 'UPSERT' },
          ],
        },
      },
    ];
  },

  assembleAction(values) {
    return {
      collectionOverride: values.ac_mongodb_collectionOverride,
      writeModeOverride: values.ac_mongodb_writeModeOverride,
    };
  },

  flattenAction(dto) {
    return {
      ac_mongodb_collectionOverride: dto?.collectionOverride,
      ac_mongodb_writeModeOverride: dto?.writeModeOverride,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_mongodb_collectionOverride: preset.collectionOverride,
          ac_mongodb_writeModeOverride: preset.writeModeOverride,
        }
      : {};
  },
};
