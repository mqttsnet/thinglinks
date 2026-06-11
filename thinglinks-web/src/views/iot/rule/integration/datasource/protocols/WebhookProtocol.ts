import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.webhook.${key}`);

/**
 * WebHook 协议模块（HTTP + HMAC 签名 + 时间戳防重放）。
 *
 * @author mqttsnet
 */
export const webhookProtocol: ProtocolModule = {
  type: 'WEBHOOK',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('url.label'),
        field: 'url',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('url.placeholder') },
        helpMessage: tk('url.help'),
      },
      {
        label: tk('contentType.label'),
        field: 'contentType',
        component: 'Input',
        defaultValue: 'application/json',
      },
      {
        label: tk('signMethod.label'),
        field: 'signMethod',
        component: 'Select',
        defaultValue: 'HMAC_SHA256',
        componentProps: {
          options: [
            { label: tk('signMethod.opt_HMAC_SHA1'), value: 'HMAC_SHA1' },
            { label: tk('signMethod.opt_HMAC_SHA256'), value: 'HMAC_SHA256' },
            { label: tk('signMethod.opt_HMAC_SHA512'), value: 'HMAC_SHA512' },
          ],
        },
        helpMessage: tk('signMethod.help'),
      },
      {
        label: tk('signHeaderName.label'),
        field: 'signHeaderName',
        component: 'Input',
        defaultValue: 'X-Signature',
        helpMessage: tk('signHeaderName.help'),
      },
      {
        label: tk('timestampHeaderName.label'),
        field: 'timestampHeaderName',
        component: 'Input',
        defaultValue: 'X-Timestamp',
        helpMessage: tk('timestampHeaderName.help'),
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      {
        label: tk('secretKey.label'),
        field: 'secretKey',
        component: 'InputPassword',
        required: true,
        componentProps: { placeholder: tk('secretKey.placeholder') },
        helpMessage: tk('secretKey.help'),
      },
    ];
  },

  examplePresets(): ProtocolPreset[] {
    return [
      {
        key: 'hmac_sha256',
        label: tk('presets.hmac_sha256_label'),
        description: tk('presets.hmac_sha256_description'),
        connection: {
          url: 'https://webhook.example.com/iot',
          contentType: 'application/json',
          signMethod: 'HMAC_SHA256',
          signHeaderName: 'X-Signature',
          timestampHeaderName: 'X-Timestamp',
        },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 5,
      defaultRetryBackoffMs: 2000,
      defaultTimeoutMs: 5000,
      defaultRateLimitQps: 50,
    };
  },
};
