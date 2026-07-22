import type {
  ProductSnapshotVO,
  ProductSnapshotServiceVO,
  ProductSnapshotPropertyVO,
  ProductSnapshotCommandVO,
} from '/@/api/iot/link/productVersion/model/productVersionModel';

/**
 * 快照层级节点 —— 与 {@code ProductVersionDiffNode} 同构,
 * 让 {@link ProductSnapshotViewer} 复用 diff viewer 的层级展开 / 搜索 / 过滤范式。
 *
 * <p>每个节点 = 一行可折叠的层级,字段(标量值)放 {@code fields},
 * 子层级(服务 → 属性 / 命令 → 参数)放 {@code children}。
 * 没有 changeType / before / after —— 快照只看当前态。</p>
 */
export interface ProductSnapshotNode {
  level: 'PRODUCT' | 'SERVICE' | 'PROPERTY' | 'COMMAND' | 'COMMAND_PARAM';
  /** COMMAND_PARAM 区分请求 / 响应,父级用于分组渲染。 */
  paramKind?: 'REQUEST' | 'RESPONSE';
  /** 节点编码(产品标识 / 服务编码 / 属性编码 / 命令编码 / 参数编码)。 */
  code?: string;
  /** 节点名称(产品名 / 服务名 / 属性名 / 命令名 / 参数名)。 */
  name?: string;
  /** 标量字段列表(field=英文名, label=i18n 后的中文名, value=展示值)。 */
  fields?: ProductSnapshotField[];
  /** 子节点。 */
  children?: ProductSnapshotNode[];
}

export interface ProductSnapshotField {
  field: string;
  label?: string;
  value: any;
}

/** 跳过 null / undefined / 空串。 */
function isPresent(v: any): boolean {
  return v !== null && v !== undefined && v !== '';
}

/** i18n key 前缀(viewer 内部统一从 versionList.preview.field 拿 label)。 */
const FIELD_KEY = 'iot.link.product.versionList.preview.field';

interface T {
  (key: string, args?: Recordable): string;
}

/** 取字段 i18n,缺则原样返回 field 名。 */
function L(t: T, field: string): string {
  const key = `${FIELD_KEY}.${field}`;
  const labeled = t(key);
  return labeled === key ? field : labeled;
}

/** 把布尔型 required / 必填类字段做"是 / 否"展示。 */
function fmtRequired(v: any): string {
  if (v === 1 || v === true || v === '1') return '是';
  return '否';
}

/** 把数据库存的 enum json 字符串美化(失败时原样回退)。 */
function fmtEnumList(v: any): any {
  if (typeof v !== 'string') return v;
  try {
    const parsed = JSON.parse(v);
    if (Array.isArray(parsed)) return parsed.join(', ');
    return parsed;
  } catch {
    return v;
  }
}

/** 产品基础信息字段映射。 */
function productFields(s: ProductSnapshotVO, t: T): ProductSnapshotField[] {
  const candidates: Array<{ field: string; raw: any }> = [
    { field: 'productName', raw: s.productName },
    { field: 'appId', raw: s.appId },
    { field: 'productType', raw: s.productType },
    { field: 'manufacturerId', raw: s.manufacturerId },
    { field: 'manufacturerName', raw: s.manufacturerName },
    { field: 'model', raw: s.model },
    { field: 'dataFormat', raw: s.dataFormat },
    { field: 'deviceType', raw: s.deviceType },
    { field: 'protocolType', raw: s.protocolType },
    { field: 'activeVersionNo', raw: s.activeVersionNo },
    { field: 'remark', raw: s.remark },
  ];
  return candidates
    .filter((x) => isPresent(x.raw))
    .map((x) => ({ field: x.field, label: L(t, x.field), value: x.raw }));
}

/** 服务状态值 → i18n 文案(0 启用 / 1 停用,与后端 ProductServiceStatusEnum 对齐;未知值原样展示)。 */
function fmtServiceStatus(v: any, t: T): any {
  if (v === 0 || v === '0') return t('iot.link.product.versionList.preview.serviceStatus.enabled');
  if (v === 1 || v === '1') return t('iot.link.product.versionList.preview.serviceStatus.disabled');
  return v;
}

/** 服务字段映射(不含 properties / commands,它们走 children)。 */
function serviceFields(s: ProductSnapshotServiceVO, t: T): ProductSnapshotField[] {
  const raw = s as any;
  const candidates: Array<{ field: string; raw: any; transform?: (v: any) => any }> = [
    { field: 'serviceCode', raw: raw.serviceCode },
    { field: 'serviceName', raw: raw.serviceName },
    { field: 'serviceType', raw: raw.serviceType },
    // 后端快照字段为 serviceStatus(非 status);transform 把 0/1 转成"启用/停用"。
    { field: 'status', raw: raw.serviceStatus, transform: (v) => fmtServiceStatus(v, t) },
    { field: 'description', raw: raw.description },
  ];
  return candidates
    .filter((x) => isPresent(x.raw))
    .map((x) => ({
      field: x.field,
      label: L(t, x.field),
      value: x.transform ? x.transform(x.raw) : x.raw,
    }));
}

/** 属性字段映射。 */
function propertyFields(p: ProductSnapshotPropertyVO, t: T): ProductSnapshotField[] {
  const raw = p as any;
  const candidates: Array<{ field: string; raw: any; transform?: (v: any) => any }> = [
    { field: 'propertyCode', raw: raw.propertyCode },
    { field: 'propertyName', raw: raw.propertyName },
    { field: 'datatype', raw: raw.datatype },
    { field: 'unit', raw: raw.unit },
    { field: 'required', raw: raw.required, transform: fmtRequired },
    { field: 'accessMode', raw: raw.accessMode },
    { field: 'min', raw: raw.min ?? raw.minValue },
    { field: 'max', raw: raw.max ?? raw.maxValue },
    { field: 'maxLength', raw: raw.maxLength },
    { field: 'step', raw: raw.step },
    { field: 'defaultValue', raw: raw.defaultValue },
    { field: 'enumList', raw: raw.enumList, transform: fmtEnumList },
    { field: 'description', raw: raw.description },
  ];
  return candidates
    .filter((x) => isPresent(x.raw))
    .map((x) => ({
      field: x.field,
      label: L(t, x.field),
      value: x.transform ? x.transform(x.raw) : x.raw,
    }));
}

/** 命令字段映射(请求 / 响应参数走 children)。 */
function commandFields(c: ProductSnapshotCommandVO, t: T): ProductSnapshotField[] {
  const raw = c as any;
  const candidates: Array<{ field: string; raw: any }> = [
    { field: 'commandCode', raw: raw.commandCode },
    { field: 'commandName', raw: raw.commandName },
    { field: 'description', raw: raw.description },
  ];
  return candidates
    .filter((x) => isPresent(x.raw))
    .map((x) => ({ field: x.field, label: L(t, x.field), value: x.raw }));
}

/** 命令参数字段映射(请求 / 响应共用)。 */
function paramFields(p: any, t: T): ProductSnapshotField[] {
  const candidates: Array<{ field: string; raw: any; transform?: (v: any) => any }> = [
    { field: 'parameterCode', raw: p.parameterCode },
    { field: 'parameterName', raw: p.parameterName },
    { field: 'datatype', raw: p.datatype },
    { field: 'unit', raw: p.unit },
    { field: 'required', raw: p.required, transform: fmtRequired },
    { field: 'min', raw: p.min ?? p.minValue },
    { field: 'max', raw: p.max ?? p.maxValue },
    { field: 'maxLength', raw: p.maxLength },
    { field: 'enumList', raw: p.enumList, transform: fmtEnumList },
    { field: 'description', raw: p.description },
  ];
  return candidates
    .filter((x) => isPresent(x.raw))
    .map((x) => ({
      field: x.field,
      label: L(t, x.field),
      value: x.transform ? x.transform(x.raw) : x.raw,
    }));
}

/**
 * 把 {@link ProductSnapshotVO} 展平成 viewer 需要的层级节点数组。
 *
 * <p>产品层为顶层节点,服务为二级,属性 / 命令为三级,命令参数为四级。
 * 层级清晰 → viewer 可按层级过滤 / 搜索 / 展开 / 收起。</p>
 *
 * @param snapshot 后端版本快照
 * @param t 国际化函数(versionList.preview.field.* 字段名翻译)
 */
export function snapshotToNodes(
  snapshot: ProductSnapshotVO | null | undefined,
  t: T,
): ProductSnapshotNode[] {
  if (!snapshot) return [];

  // 顶层 = 产品节点;服务作为子节点
  const productNode: ProductSnapshotNode = {
    level: 'PRODUCT',
    code: snapshot.productIdentification,
    name: snapshot.productName,
    fields: productFields(snapshot, t),
    children: (snapshot.services || []).map((svc) => serviceToNode(svc, t)),
  };
  return [productNode];
}

function serviceToNode(svc: ProductSnapshotServiceVO, t: T): ProductSnapshotNode {
  const propNodes: ProductSnapshotNode[] = (svc.properties || []).map((p) => ({
    level: 'PROPERTY',
    code: (p as any).propertyCode,
    name: (p as any).propertyName,
    fields: propertyFields(p, t),
  }));
  const cmdNodes: ProductSnapshotNode[] = (svc.commands || []).map((c) => commandToNode(c, t));
  return {
    level: 'SERVICE',
    code: (svc as any).serviceCode,
    name: (svc as any).serviceName,
    fields: serviceFields(svc, t),
    children: [...propNodes, ...cmdNodes],
  };
}

function commandToNode(cmd: ProductSnapshotCommandVO, t: T): ProductSnapshotNode {
  const reqs = ((cmd as any).requests || []).map(
    (p: any): ProductSnapshotNode => ({
      level: 'COMMAND_PARAM',
      paramKind: 'REQUEST',
      code: p.parameterCode,
      name: p.parameterName,
      fields: paramFields(p, t),
    }),
  );
  const resps = ((cmd as any).responses || []).map(
    (p: any): ProductSnapshotNode => ({
      level: 'COMMAND_PARAM',
      paramKind: 'RESPONSE',
      code: p.parameterCode,
      name: p.parameterName,
      fields: paramFields(p, t),
    }),
  );
  return {
    level: 'COMMAND',
    code: (cmd as any).commandCode,
    name: (cmd as any).commandName,
    fields: commandFields(cmd, t),
    children: [...reqs, ...resps],
  };
}
