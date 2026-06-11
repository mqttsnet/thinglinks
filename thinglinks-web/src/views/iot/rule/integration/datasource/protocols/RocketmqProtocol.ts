import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.rocketmq.${key}`);

/**
 * RocketMQ 协议模块（自建 Apache 5.x + 阿里云 通过 access-channel 切换）。
 *
 * @author mqttsnet
 */
export const rocketmqProtocol: ProtocolModule = {
  type: 'ROCKETMQ',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('nameServer.label'),
        field: 'nameServer',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('nameServer.placeholder') },
        helpMessage: tk('nameServer.help'),
      },
      {
        label: tk('topic.label'),
        field: 'topic',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('topic.placeholder') },
        helpMessage: tk('topic.help'),
      },
      {
        label: tk('tag.label'),
        field: 'tag',
        component: 'Input',
        defaultValue: '*',
        componentProps: { placeholder: tk('tag.placeholder') },
        helpMessage: tk('tag.help'),
      },
      {
        label: tk('producerGroup.label'),
        field: 'producerGroup',
        component: 'Input',
        componentProps: { placeholder: tk('producerGroup.placeholder') },
        helpMessage: tk('producerGroup.help'),
      },
      {
        label: tk('accessChannel.label'),
        field: 'accessChannel',
        component: 'Select',
        defaultValue: 'LOCAL',
        componentProps: {
          options: [
            { label: tk('accessChannel.opt_LOCAL'), value: 'LOCAL' },
            { label: tk('accessChannel.opt_CLOUD'), value: 'CLOUD' },
          ],
        },
        helpMessage: tk('accessChannel.help'),
      },
      {
        label: tk('namespace.label'),
        field: 'namespace',
        component: 'Input',
        componentProps: { placeholder: tk('namespace.placeholder') },
        helpMessage: tk('namespace.help'),
        ifShow: ({ values }) => values.accessChannel === 'CLOUD',
      },
      {
        label: tk('vipChannelEnabled.label'),
        field: 'vipChannelEnabled',
        component: 'Switch',
        defaultValue: false,
        componentProps: {
          checkedChildren: tk('vipChannelEnabled.on'),
          unCheckedChildren: tk('vipChannelEnabled.off'),
        },
        helpMessage: tk('vipChannelEnabled.help'),
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      {
        label: tk('accessKey.label'),
        field: 'accessKey',
        component: 'Input',
        componentProps: { placeholder: tk('accessKey.placeholder') },
        helpMessage: tk('accessKey.help'),
      },
      {
        label: tk('secretKey.label'),
        field: 'secretKey',
        component: 'InputPassword',
        helpMessage: tk('secretKey.help'),
      },
    ];
  },

  examplePresets(): ProtocolPreset[] {
    return [
      {
        key: 'apache_local',
        label: tk('presets.apache_local_label'),
        description: tk('presets.apache_local_description'),
        connection: {
          nameServer: '127.0.0.1:9876',
          topic: 'iot-test',
          tag: '*',
          accessChannel: 'LOCAL',
          vipChannelEnabled: false,
        },
      },
      {
        key: 'aliyun',
        label: tk('presets.aliyun_label'),
        description: tk('presets.aliyun_description'),
        connection: {
          nameServer: 'http://MQ_INST_xxx.mq-internet-access.mq.aliyuncs.com:80',
          topic: 'iot-prod',
          tag: '*',
          accessChannel: 'CLOUD',
          namespace: 'MQ_INST_xxx',
          vipChannelEnabled: false,
        },
        credential: {
          accessKey: 'LTAI_xxx',
        },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 0,
      defaultRetryBackoffMs: 1000,
      defaultTimeoutMs: 5000,
    };
  },

  validate(values): string | null {
    if (values.accessChannel === 'CLOUD' && !values.namespace) {
      return tk('validate.cloud_namespace_required');
    }
    return null;
  },
};
