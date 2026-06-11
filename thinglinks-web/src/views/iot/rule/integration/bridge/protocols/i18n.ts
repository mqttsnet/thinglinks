/**
 * 桥接协议 i18n helper（17 个 BridgeAction.ts 共用）。
 *
 * <p>统一从 {@code iot.rule.integration.bridge.actionForm.*} 命名空间取文案，
 * 避免每个协议文件重复定义 tk / tEnum / tSuffix。
 *
 * @author mqttsnet
 */

import { useI18n } from '/@/hooks/web/useI18n';

const { t } = useI18n();

/** 协议特异字段命名空间（actionForm.proto.{type}.{key}） */
export const tProto = (proto: string) => (key: string) =>
  t(`iot.rule.integration.bridge.actionForm.proto.${proto}.${key}`);

/** 共用枚举值（INSERT/UPDATE/UPSERT/IGNORE/SYNC/ASYNC/...） */
export const tEnum = (key: string) =>
  t(`iot.rule.integration.bridge.actionForm.enums.${key}`);

/** actionForm 顶层文案（dsDefaultPlaceholder / overrideSuffix） */
export const tForm = (key: string) =>
  t(`iot.rule.integration.bridge.actionForm.${key}`);

/** label + "（覆盖）" 后缀，反复出现的便捷函数 */
export const tWithOverride = (label: string): string =>
  label + tForm('overrideSuffix');

/** "不填 = 沿用数据源默认" 占位符（反复出现） */
export const dsDefaultPlaceholder = (): string => tForm('dsDefaultPlaceholder');
