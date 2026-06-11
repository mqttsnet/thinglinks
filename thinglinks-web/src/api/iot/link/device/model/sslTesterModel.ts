/** SSL 证书认证测试请求 */
export interface DeviceSslTestQuery {
  /** 客户端证书 Base64 (必填) */
  clientCertBase64: string;
  /** 目标 CA 序列号;留空则按 clientIdentifier 反查设备绑定 CA */
  caSerialNumber?: string;
  /** 客户端标识(用于反查 + 审计) */
  clientIdentifier?: string;
}

/** SSL 测试分步状态 */
export type SslTestStepStatus = 'PASS' | 'FAIL' | 'SKIP';

/** SSL 测试分步枚举(与后端 DeviceSslTestStepEnum 一致) */
export type SslTestStepKey =
  | 'PARSE_CLIENT_CERT'
  | 'VALIDITY_CHECK'
  | 'FIND_CA'
  | 'CA_STATE_CHECK'
  | 'ISSUER_MATCH'
  | 'SIGNATURE_VERIFY';

/** 单步结果 */
export interface DeviceSslTestStepVO {
  step: SslTestStepKey;
  name: string;
  status: SslTestStepStatus;
  detail?: Record<string, any>;
  reason?: string;
  costMs?: number;
}

/** 整体响应 */
export interface DeviceSslTestResultVO {
  success: boolean;
  steps: DeviceSslTestStepVO[];
  summary: string;
  totalCostMs?: number;
}
