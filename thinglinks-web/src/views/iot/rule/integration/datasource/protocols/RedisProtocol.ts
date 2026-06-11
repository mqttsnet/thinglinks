import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.redis.${key}`);

/**
 * Redis 协议模块（支持 STANDALONE / SENTINEL / CLUSTER 三种部署模式）。
 *
 * @author mqttsnet
 */
export const redisProtocol: ProtocolModule = {
  type: 'REDIS',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('mode.label'),
        field: 'mode',
        component: 'Select',
        required: true,
        defaultValue: 'STANDALONE',
        componentProps: {
          options: [
            { label: tk('mode.opt_STANDALONE'), value: 'STANDALONE' },
            { label: tk('mode.opt_SENTINEL'), value: 'SENTINEL' },
            { label: tk('mode.opt_CLUSTER'), value: 'CLUSTER' },
          ],
        },
        helpMessage: tk('mode.help'),
      },
      {
        label: tk('host.label'),
        field: 'host',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('host.placeholder') },
        ifShow: ({ values }) => values.mode === 'STANDALONE',
      },
      {
        label: tk('port.label'),
        field: 'port',
        component: 'InputNumber',
        defaultValue: 6379,
        componentProps: { style: 'width:100%', min: 1, max: 65535 },
        ifShow: ({ values }) => values.mode === 'STANDALONE',
      },
      {
        label: tk('sentinels.label'),
        field: 'sentinels',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('sentinels.placeholder') },
        helpMessage: tk('sentinels.help'),
        ifShow: ({ values }) => values.mode === 'SENTINEL',
      },
      {
        label: tk('masterName.label'),
        field: 'masterName',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('masterName.placeholder') },
        ifShow: ({ values }) => values.mode === 'SENTINEL',
      },
      {
        label: tk('clusterNodes.label'),
        field: 'clusterNodes',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('clusterNodes.placeholder') },
        ifShow: ({ values }) => values.mode === 'CLUSTER',
      },
      {
        label: tk('db.label'),
        field: 'db',
        component: 'InputNumber',
        defaultValue: 0,
        componentProps: { style: 'width:100%', min: 0, max: 15 },
        helpMessage: tk('db.help'),
        ifShow: ({ values }) => values.mode !== 'CLUSTER',
      },
      {
        label: tk('command.label'),
        field: 'command',
        component: 'Select',
        required: true,
        defaultValue: 'LPUSH',
        componentProps: {
          options: [
            { label: tk('command.opt_LPUSH'), value: 'LPUSH' },
            { label: tk('command.opt_RPUSH'), value: 'RPUSH' },
            { label: tk('command.opt_XADD'), value: 'XADD' },
            { label: tk('command.opt_PUBLISH'), value: 'PUBLISH' },
            { label: tk('command.opt_SET'), value: 'SET' },
          ],
        },
        helpMessage: tk('command.help'),
      },
      {
        label: tk('keyTemplate.label'),
        field: 'keyTemplate',
        component: 'Input',
        required: true,
        componentProps: { placeholder: 'iot:${tenantId}:${productId}:${deviceId}' },
        helpMessage: 'Key 模板，支持占位符 ${tenantId} ${productId} ${deviceId}',
      },
      {
        label: tk('ttlSeconds.label'),
        field: 'ttlSeconds',
        component: 'InputNumber',
        defaultValue: 0,
        componentProps: { style: 'width:100%', min: 0 },
        helpMessage: tk('ttlSeconds.help'),
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
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
        key: 'local_standalone',
        label: tk('presets.local_standalone_label'),
        description: tk('presets.local_standalone_description'),
        connection: {
          mode: 'STANDALONE',
          host: '127.0.0.1',
          port: 6379,
          db: 0,
          command: 'LPUSH',
          keyTemplate: 'iot:test:${deviceId}',
        },
      },
      {
        key: 'aliyun_cluster',
        label: tk('presets.aliyun_cluster_label'),
        description: tk('presets.aliyun_cluster_description'),
        connection: {
          mode: 'CLUSTER',
          clusterNodes: 'r-xxx.redis.rds.aliyuncs.com:7000,r-yyy.redis.rds.aliyuncs.com:7000',
          command: 'XADD',
          keyTemplate: 'iot:${tenantId}:${productId}:${deviceId}',
          ttlSeconds: 86400,
        },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 0,
      defaultRetryMaxTimes: 2,
      defaultRetryBackoffMs: 500,
      defaultTimeoutMs: 2000,
      defaultRateLimitQps: 0,
    };
  },

  validate(values): string | null {
    if (values.mode === 'SENTINEL' && (!values.sentinels || !values.masterName)) {
      return tk('validate.sentinel_required');
    }
    if (values.mode === 'CLUSTER' && !values.clusterNodes) {
      return tk('validate.cluster_required');
    }
    return null;
  },
};
