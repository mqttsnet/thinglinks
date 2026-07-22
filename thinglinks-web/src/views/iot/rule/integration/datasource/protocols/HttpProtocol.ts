import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.http.${key}`);

/**
 * HTTP 协议模块（POST/PUT/PATCH 推送 JSON / 表单）。
 *
 * @author mqttsnet
 */
export const httpProtocol: ProtocolModule = {
  type: 'HTTP',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('url.label'),
        field: 'url',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('url.placeholder') },
        helpMessage: '目标接口 URL；支持模板 ${tenantId} ${productId}',
      },
      {
        label: tk('method.label'),
        field: 'method',
        component: 'Select',
        defaultValue: 'POST',
        componentProps: {
          options: [
            { label: 'POST', value: 'POST' },
            { label: 'PUT', value: 'PUT' },
            { label: 'PATCH', value: 'PATCH' },
          ],
        },
      },
      {
        label: tk('contentType.label'),
        field: 'contentType',
        component: 'Input',
        defaultValue: 'application/json',
        componentProps: { placeholder: tk('contentType.placeholder') },
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      {
        label: tk('bearerToken.label'),
        field: 'bearerToken',
        component: 'InputPassword',
        helpMessage: tk('bearerToken.help'),
      },
      {
        label: tk('basicUsername.label'),
        field: 'basicUsername',
        component: 'Input',
        helpMessage: tk('basicUsername.help'),
      },
      {
        label: tk('basicPassword.label'),
        field: 'basicPassword',
        component: 'InputPassword',
        helpMessage: tk('basicPassword.help'),
      },
    ];
  },

  examplePresets(): ProtocolPreset[] {
    return [
      {
        key: 'simple_post',
        label: tk('presets.simple_post_label'),
        description: tk('presets.simple_post_description'),
        connection: {
          url: 'https://api.example.com/iot',
          method: 'POST',
          contentType: 'application/json',
        },
      },
      {
        key: 'bearer_auth',
        label: tk('presets.bearer_auth_label'),
        description: tk('presets.bearer_auth_description'),
        connection: {
          url: 'https://api.example.com/iot',
          method: 'POST',
          contentType: 'application/json',
        },
        credential: { bearerToken: 'eyJxxx...' },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 3,
      defaultRetryBackoffMs: 1000,
      defaultTimeoutMs: 3000,
      defaultRateLimitQps: 100,
    };
  },
};
