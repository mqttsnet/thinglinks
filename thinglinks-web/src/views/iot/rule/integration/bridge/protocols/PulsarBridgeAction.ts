import type { FormSchema } from '/@/components/Form';
import type { PulsarActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tEnum, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('pulsar');

export const pulsarBridgeAction: BridgeOutboundActionModule<PulsarActionConfigDto> = {
  type: 'PULSAR',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('topicOverride.label')),
        field: 'ac_pulsar_topicOverride',
        component: 'Input',
        componentProps: { placeholder: dsDefaultPlaceholder() },
      },
      {
        label: tk('messageKey.label'),
        field: 'ac_pulsar_messageKey',
        component: 'Input',
        componentProps: { placeholder: '${deviceIdentification}' },
        helpMessage: tk('messageKey.help'),
      },
      {
        label: tk('messageProperties.label'),
        field: 'ac_pulsar_messageProperties',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"productId":"${productId}"}' },
      },
      {
        label: tWithOverride(tk('sendModeOverride.label')),
        field: 'ac_pulsar_sendModeOverride',
        component: 'Select',
        componentProps: {
          placeholder: dsDefaultPlaceholder(),
          allowClear: true,
          options: [
            { label: tEnum('ASYNC'), value: 'ASYNC' },
            { label: tEnum('SYNC'), value: 'SYNC' },
          ],
        },
      },
    ];
  },

  assembleAction(values) {
    return {
      topicOverride: values.ac_pulsar_topicOverride,
      messageKey: values.ac_pulsar_messageKey,
      messageProperties: values.ac_pulsar_messageProperties,
      sendModeOverride: values.ac_pulsar_sendModeOverride,
    };
  },

  flattenAction(dto) {
    return {
      ac_pulsar_topicOverride: dto?.topicOverride,
      ac_pulsar_messageKey: dto?.messageKey,
      ac_pulsar_messageProperties: dto?.messageProperties,
      ac_pulsar_sendModeOverride: dto?.sendModeOverride,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_pulsar_topicOverride: preset.topicOverride,
          ac_pulsar_messageKey: preset.messageKey,
          ac_pulsar_messageProperties: preset.messageProperties,
          ac_pulsar_sendModeOverride: preset.sendModeOverride,
        }
      : {};
  },
};
