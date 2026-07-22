export default {
  table: { title: 'CaCertLicense list' },
  id: 'Id',
  certName: 'CertName',
  serialNumber: 'SerialNumber',
  commonName: 'CommonName',
  organization: 'Organization',
  organizationalUnit: 'OrganizationalUnit',
  countryName: 'CountryName',
  provinceName: 'ProvinceName',
  localityName: 'LocalityName',
  email: 'Email',
  licenseBase64: 'LicenseBase64',
  businessLicenseFileid: 'BusinessLicenseFileid',
  authorizationCertFileid: 'AuthorizationCertFileid',
  algorithm: 'Algorithm',
  param1: 'Param1',
  param2: 'Param2',
  extendParams: 'ExtendParams',
  notBefore: 'NotBefore',
  notAfter: 'NotAfter',
  revokeTime: 'RevokeTime',
  expirationDate: 'ExpirationDate',
  state: 'State',
  remark: 'Remark',
  createdTime: 'CreatedTime',
  createdBy: 'CreatedBy',
  updatedTime: 'UpdatedTime',
  updatedBy: 'UpdatedBy',
  createdOrgId: 'CreatedOrgId',
  detailTitle: 'CA License Certificate Detail',
  importTitle: 'Import CA Certificate',
  caCertPem: 'CA certificate PEM',
  thumbprint: 'Thumbprint (SHA-256)',
  validityRemaining: '{days} days remaining',
  expired: 'Expired',
  boundDeviceCount: 'Bound devices',
  onlineDeviceCount: 'Online',
  switchView: 'Switch view',
  contentTip: 'PEM content of this CA certificate, copy for client-side verification',

  card: {
    nameFallback: 'Unnamed certificate',
  },

  status: {
    pending: 'Pending',
    issued: 'Issued',
    revoked: 'Revoked',
    other: 'Unavailable',
  },

  placeholder: {
    certName: 'e.g. Aliyun IoT CA',
    caCertPem: 'Paste CA certificate PEM content, starting with -----BEGIN CERTIFICATE-----',
    remark: 'Optional notes (purpose, associated app, etc.)',
  },

  action: {
    issue: 'Issue',
    revoke: 'Revoke',
    downloadPack: 'Download Pack',
    testSsl: 'SSL Test',
    viewImpact: 'View Impact',
  },

  import: {
    tip:
      'Paste a standard PEM CA root certificate. The system will auto-extract cert name, ' +
      'serial number, validity period, signature algorithm, etc.',
    confirm: 'Import',
    success: 'Certificate imported successfully',
    failed: 'Failed to import certificate',
  },

  edit: {
    title: 'Edit Certificate Info',
    tip: 'PEM-derived fields (serial number, CN, algorithm, etc.) are read-only. You can only update the display name and remark.',
    success: 'Saved successfully',
    failed: 'Failed to save',
    noId: 'Select a certificate to edit',
    backList: 'Back to list',
  },

  detail: {
    refresh: 'Refresh',
    loadFailed: 'Failed to load certificate detail',
  },

  impact: {
    title: 'Revoke Impact',
    warning:
      'This will immediately affect {count} devices ({online} online). Affected devices will fail SSL authentication.',
    confirm: 'I understand, revoke now',
    boundDevices: 'Bound devices (top 50)',
    deviceIdentification: 'Device ID',
    deviceName: 'Device Name',
    online: 'Status',
    connectStatusOnline: 'Online',
    connectStatusOffline: 'Offline',
    revokeReason: 'Revoke reason',
    revokeReasonPh: 'Recommended for audit trail',
    revokeSuccess: 'Certificate revoked successfully',
    revokeFailed: 'Failed to revoke certificate',
    loadFailed: 'Failed to load impact data',
    top50Tip: 'Total {count} bound devices, showing first 50',
  },

  downloadPack: {
    tip: 'Issue a client certificate signed by this CA and download as a ZIP package',
    caInfo: 'Signed by',
    notAfter: 'Client certificate expiry',
    notAfterPh: 'Pick expiry date (1 year recommended)',
    confirm: 'Issue & Download',
    success: 'Client pack download started',
    failed: 'Issue or download failed',
  },

  audit: {
    tabTitle: 'Audit Timeline',
    type: 'Action',
    detail: 'Detail',
    operator: 'Operator',
    time: 'Time',
    empty: 'No audit records yet',
  },

  tabs: {
    basic: 'Basic',
    content: 'Content',
    devices: 'Devices',
    audit: 'Audit',
  },
};
