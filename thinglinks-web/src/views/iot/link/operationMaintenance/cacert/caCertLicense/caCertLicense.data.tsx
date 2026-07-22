import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import type { CardField } from '/@/components/BusinessCardList';

const { t } = useI18n();

const tNs = 'iot.link.operationMaintenance.cacert.caCertLicense';

/**
 * 卡片视图字段配置 ── 仅展示用户关心的高频字段:
 * 序列号 / 算法 / 通用名 / 组织 / 有效期 / 创建时间.
 */
export const cardFields = (): CardField[] => [
  { label: t(`${tNs}.serialNumber`), field: 'serialNumber', span: 24 },
  {
    label: t(`${tNs}.algorithm`),
    field: 'algorithm',
    dictType: DictEnum.LINK_CA_CERT_ALGORITHM,
    span: 12,
  },
  { label: t(`${tNs}.commonName`), field: 'commonName', span: 12 },
  { label: t(`${tNs}.organization`), field: 'organization', span: 12 },
  { label: t(`${tNs}.notAfter`), field: 'notAfter', span: 12 },
  { label: t('thinglinks.common.createdTime'), field: 'createdTime', span: 24 },
];

/** 列表页字段(精简版,仅核心字段;详情页查看完整信息) */
export const columns = (): BasicColumn[] => [
  {
    title: t(`${tNs}.certName`),
    dataIndex: 'certName',
    slots: { customRender: 'certName' },
  },
  { title: t(`${tNs}.serialNumber`), dataIndex: 'serialNumber' },
  { title: t(`${tNs}.commonName`), dataIndex: 'commonName' },
  { title: t(`${tNs}.organization`), dataIndex: 'organization' },
  {
    title: t(`${tNs}.algorithm`),
    dataIndex: 'algorithm',
    slots: { customRender: 'algorithm' },
    width: 100,
  },
  {
    title: t(`${tNs}.state`),
    dataIndex: 'state',
    slots: { customRender: 'state' },
    width: 100,
  },
  { title: t(`${tNs}.notAfter`), dataIndex: 'notAfter', width: 170 },
  {
    title: t('thinglinks.common.createdTime'),
    dataIndex: 'createdTime',
    sorter: true,
    width: 170,
  },
];

/** 搜索表单 */
export const searchFormSchema = (): FormSchema[] => [
  {
    label: t(`${tNs}.certName`),
    field: 'certName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t(`${tNs}.serialNumber`),
    field: 'serialNumber',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t(`${tNs}.commonName`),
    field: 'commonName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t(`${tNs}.algorithm`),
    field: 'algorithm',
    component: 'ApiSelect',
    colProps: { span: 6 },
    componentProps: { ...dictComponentProps(DictEnum.LINK_CA_CERT_ALGORITHM) },
  },
  {
    label: t(`${tNs}.state`),
    field: 'state',
    component: 'ApiSelect',
    colProps: { span: 6 },
    componentProps: { ...dictComponentProps(DictEnum.LINK_CA_CERT_STATUS) },
  },
  {
    field: 'createTimeRange',
    label: t('thinglinks.common.createdTime'),
    component: 'RangePicker',
    colProps: { span: 6 },
  },
];

/**
 * 元信息编辑表单 ── CA 证书 PEM 解析的字段 (serialNumber / commonName / algorithm /
 * notBefore / notAfter 等) 全部不可改; 用户可改的仅 certName 显示名 + remark 备注.
 *
 * <p>backend {@code CaCertLicenseUpdateVO} 把 algorithm/state 标了 @NotNull,
 * 所以表单也透传(hidden), 避免后端校验报"必填"红.
 */
export const editMetadataSchema = (): FormSchema[] => [
  { field: 'id', label: 'ID', component: 'Input', show: false },
  // 后端 @NotNull 字段, 透传不展示
  { field: 'algorithm', label: 'algorithm', component: 'Input', show: false },
  { field: 'state', label: 'state', component: 'Input', show: false },
  // ===== 用户可改 =====
  {
    label: t(`${tNs}.certName`),
    field: 'certName',
    component: 'Input',
    required: true,
    componentProps: { maxlength: 50, showCount: true },
  },
  {
    label: t(`${tNs}.remark`),
    field: 'remark',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
      maxlength: 500,
      showCount: true,
      placeholder: t(`${tNs}.placeholder.remark`),
    },
  },
];

/** 导入证书表单 (PEM 导入) */
export const importSchema = (_type?: Ref<ActionEnum>): FormSchema[] => [
  {
    label: t(`${tNs}.certName`),
    field: 'certName',
    component: 'Input',
    required: true,
    componentProps: {
      maxlength: 100,
      placeholder: t(`${tNs}.placeholder.certName`),
    },
  },
  {
    label: t(`${tNs}.caCertPem`),
    field: 'caCertPem',
    component: 'InputTextArea',
    required: true,
    componentProps: {
      'auto-size': { minRows: 6, maxRows: 12 },
      placeholder: t(`${tNs}.placeholder.caCertPem`),
    },
  },
  {
    label: t(`${tNs}.remark`),
    field: 'remark',
    component: 'InputTextArea',
    componentProps: {
      rows: 2,
      maxlength: 500,
      showCount: true,
      placeholder: t(`${tNs}.placeholder.remark`),
    },
  },
];

export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => [];
