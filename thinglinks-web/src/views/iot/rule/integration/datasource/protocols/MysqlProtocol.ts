import type { FormSchema } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';

const { t } = useI18n();
const tk = (key: string) => t(`iot.rule.integration.protocols.mysql.${key}`);

/**
 * MySQL 协议模块。
 *
 * <p>⚠ <b>字段位置修复</b>：{@code username} 落在 {@link #connectionFields()}（旧 schema 在
 * credentialFields），与后端 {@code MysqlConnectionDto.username} 1:1 对齐。
 *
 * @author mqttsnet
 */
export const mysqlProtocol: ProtocolModule = {
  type: 'MYSQL',

  connectionFields(): FormSchema[] {
    return [
      {
        label: tk('jdbcUrl.label'),
        field: 'jdbcUrl',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('jdbcUrl.placeholder') },
        helpMessage: tk('jdbcUrl.help'),
      },
      {
        label: tk('username.label'),
        field: 'username',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('username.placeholder') },
        helpMessage: tk('username.help'),
      },
      {
        label: tk('table.label'),
        field: 'table',
        component: 'Input',
        required: true,
        componentProps: { placeholder: tk('table.placeholder') },
        helpMessage: '目标表名；支持模板 ${productId}',
      },
      {
        label: tk('columnMapping.label'),
        field: 'columnMapping',
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 4, placeholder: '{ "device_id": "${deviceIdentification}", "ts": "${timestamp}", "payload": "${payload}" }' },
        helpMessage: 'JSON 格式：{表字段: 占位符}；占位符可用 envelope 任意字段',
      },
      {
        label: tk('onDuplicate.label'),
        field: 'onDuplicate',
        component: 'Select',
        defaultValue: 'INSERT',
        componentProps: {
          options: [
            { label: tk('onDuplicate.opt_INSERT'), value: 'INSERT' },
            { label: tk('onDuplicate.opt_UPDATE'), value: 'UPDATE' },
            { label: tk('onDuplicate.opt_IGNORE'), value: 'IGNORE' },
          ],
        },
        helpMessage: tk('onDuplicate.help'),
      },
    ];
  },

  credentialFields(): FormSchema[] {
    return [
      {
        label: tk('password.label'),
        field: 'password',
        component: 'InputPassword',
        required: true,
        helpMessage: tk('password.help'),
      },
    ];
  },

  examplePresets(): ProtocolPreset[] {
    return [
      {
        key: 'local',
        label: tk('presets.local_label'),
        description: tk('presets.local_description'),
        connection: {
          jdbcUrl: 'jdbc:mysql://127.0.0.1:3306/iot_dev?useSSL=false&serverTimezone=UTC',
          username: 'root',
          table: 'iot_data',
          columnMapping:
            '{"device_id":"${deviceIdentification}","ts":"${timestamp}","payload":"${payload}"}',
          onDuplicate: 'INSERT',
        },
      },
      {
        key: 'aliyun_rds',
        label: tk('presets.aliyun_rds_label'),
        description: tk('presets.aliyun_rds_description'),
        connection: {
          jdbcUrl: 'jdbc:mysql://rm-xxx.mysql.rds.aliyuncs.com:3306/iot_prod?useSSL=true',
          username: 'iot_writer',
          table: 'iot_data',
          columnMapping:
            '{"device_id":"${deviceIdentification}","ts":"${timestamp}","payload":"${payload}"}',
          onDuplicate: 'UPDATE',
        },
      },
    ];
  },

  recommendedDefaults(): ProtocolDefaultPolicy {
    return {
      defaultQos: 1,
      defaultRetryMaxTimes: 1,
      defaultRetryBackoffMs: 1000,
      defaultTimeoutMs: 5000,
      defaultRateLimitQps: 0,
    };
  },
};
