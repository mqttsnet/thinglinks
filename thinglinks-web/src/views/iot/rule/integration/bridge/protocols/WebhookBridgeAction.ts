import type { FormSchema } from '/@/components/Form';
import type { WebhookActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto } from './i18n';

const tk = tProto('webhook');

export const webhookBridgeAction: BridgeOutboundActionModule<WebhookActionConfigDto> = {
  type: 'WEBHOOK',

  actionFields(): FormSchema[] {
    return [
      {
        label: tk('headers.label'),
        field: 'ac_webhook_headers',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"Content-Type":"application/json"}' },
      },
    ];
  },

  assembleAction(values) {
    return {
      headers: values.ac_webhook_headers,
    };
  },

  flattenAction(dto) {
    return {
      ac_webhook_headers: dto?.headers,
    };
  },

  presetToFlat(preset) {
    return preset ? { ac_webhook_headers: preset.headers } : {};
  },
};
