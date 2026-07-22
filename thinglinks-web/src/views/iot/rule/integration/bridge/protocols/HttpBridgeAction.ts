import type { FormSchema } from '/@/components/Form';
import type { HttpActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto } from './i18n';

const tk = tProto('http');

export const httpBridgeAction: BridgeOutboundActionModule<HttpActionConfigDto> = {
  type: 'HTTP',

  actionFields(): FormSchema[] {
    return [
      {
        label: tk('headers.label'),
        field: 'ac_http_headers',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"X-Source":"thinglinks"}' },
      },
      {
        label: tk('queryParams.label'),
        field: 'ac_http_queryParams',
        component: 'InputTextArea',
        componentProps: { rows: 2, placeholder: '{"src":"iot"}' },
      },
      {
        label: tk('bodyWrap.label'),
        field: 'ac_http_bodyWrap',
        component: 'Switch',
        defaultValue: false,
        helpMessage: 'false=直接发 payload；true=包一层 {data: payload}',
      },
    ];
  },

  assembleAction(values) {
    return {
      headers: values.ac_http_headers,
      queryParams: values.ac_http_queryParams,
      bodyWrap: values.ac_http_bodyWrap,
    };
  },

  flattenAction(dto) {
    return {
      ac_http_headers: dto?.headers,
      ac_http_queryParams: dto?.queryParams,
      ac_http_bodyWrap: dto?.bodyWrap ?? false,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_http_headers: preset.headers,
          ac_http_queryParams: preset.queryParams,
          ac_http_bodyWrap: preset.bodyWrap ?? false,
        }
      : {};
  },
};
