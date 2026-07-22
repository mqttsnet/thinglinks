import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.influxdb.${key}`);

/**
 * InfluxDB 协议模块（v2 API + Line Protocol；OSS 时序主流）。
 *
 * @author mqttsnet
 */
export const influxDbProtocol: ProtocolModule = {
  type: 'INFLUXDB',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('apiVersion.label'),
        field: 'apiVersion',
        component: 'Select',
        defaultValue: 'V2',
        componentProps: {
          options: [
            { label: tk('apiVersion.opt_V2'), value: 'V2' },
            { label: tk('apiVersion.opt_V1'), value: 'V1' },
          ],
        },
        helpMessage: tk('apiVersion.help'),
      },
      {
        label: tk('url.label'),
        field: 'url',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('url.placeholder') },
      },
      {
        label: tk('org.label'),
        field: 'org',
        component: 'Input',
        componentProps: { placeholder: tk('org.placeholder') },
        helpMessage: tk('org.help'),
        ifShow: ({ values }) => values.apiVersion !== 'V1',
      },
      {
        label: tk('bucket.label'),
        field: 'bucket',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('bucket.placeholder') },
        helpMessage: tk('bucket.help'),
      },
      {
        label: tk('measurement.label'),
        field: 'measurement',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('measurement.placeholder') },
        helpMessage: '写入 measurement 名；支持模板 ${productId}',
      },
      {
        label: tk('tagsMapping.label'),
        field: 'tagsMapping',
        component: 'InputTextArea',
        componentProps: { rows: 3, placeholder: '{ "productId": "${productId}", "deviceId": "${deviceIdentification}" }' },
        helpMessage: tk('tagsMapping.help'),
      },
      {
        label: tk('fieldsMapping.label'),
        field: 'fieldsMapping',
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 3, placeholder: '{ "value": "${payload}" }' },
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      {
        label: tk('token.label'),
        field: 'token',
        component: 'InputPassword',
        helpMessage: tk('token.help'),
      },
      {
        label: tk('username.label'),
        field: 'username',
        component: 'Input',
        helpMessage: tk('username.help'),
      },
      {
        label: tk('password.label'),
        field: 'password',
        component: 'InputPassword',
        helpMessage: tk('password.help'),
      },
    ];
  },

  examplePresets(): ProtocolPreset[] {
    return [
      {
        key: 'v2_local',
        label: tk('presets.v2_local_label'),
        description: tk('presets.v2_local_description'),
        connection: {
          apiVersion: 'V2',
          url: 'http://127.0.0.1:8086',
          org: 'iot-org',
          bucket: 'device_metrics',
          measurement: 'device_data',
          tagsMapping: '{"productId":"${productId}","deviceId":"${deviceIdentification}"}',
          fieldsMapping: '{"value":"${payload}"}',
        },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 2,
      defaultRetryBackoffMs: 1000,
      defaultTimeoutMs: 3000,
    };
  },

  validate(values): string | null {
    if (values.apiVersion === 'V1' && !values.username) {
      return tk('validate.v1_username_required');
    }
    if (values.apiVersion === 'V2' && !values.org) {
      return tk('validate.v2_org_required');
    }
    return null;
  },
};
