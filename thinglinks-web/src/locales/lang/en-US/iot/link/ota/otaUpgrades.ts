export default {
  table: { title: 'OtaUpgrades list' },
  detailTitle: 'OTA Resource Details',
  detailAsideTip: 'The package type determines how a device upgrades and verifies',
  group: {
    basic: 'Basic info',
    product: 'Product & target version',
    package: 'Upgrade package',
    extra: 'Description & extras',
    timeline: 'Timeline',
  },
  id: 'Id',
  appId: 'AppId',
  packageName: 'PackageName',
  packageType: 'PackageType',
  productIdentification: 'ProductIdentification',
  version: 'Version',
  productVersionNo: 'Target product version',
  productVersionNoRequired: 'Please select the target product version',
  upgradePackage: 'Upgrade Package',
  fileLocation: 'FileLocation',
  signMethod: 'Signature method',
  status: 'Status',
  description: 'Description',
  customInfo: 'CustomInfo',
  remark: 'Remark',
  createdBy: 'CreatedBy',
  createdTime: 'CreatedTime',
  updatedBy: 'UpdatedBy',
  updatedTime: 'UpdatedTime',
  createdOrgId: 'CreatedOrgId',
  helpMessage: {
    version:
      'Please enter the version number (format: x.y.z[- pre-release tag][+ build metadata]), ' +
      'for example: 1.0.0, 1.0.0-alpha, 1.0.0+20200101',
    customInfo:
      'Custom information has no restrictions on format or content and will be sent to the device ' +
      'in the upgrade notification after creating an upgrade task based on this upgrade package',
    productVersionNo:
      'After a device upgrades via this package, or reports the matching firmware / software version, its bound product ' +
      'version auto-switches to the shadow version chosen here. Publish a shadow version under the product first.',
  },
  versionRule: 'The version number format is incorrect',
  placeholder: {
    customInfoPlaceholder: 'Please enter custom information to push to the device',
    descriptionPlaceholder: 'Please enter upgrade package description',
  },
  codeEditorDefine: {
    title: 'Assist in writing',
    helpMessage: 'Assist in writing custom parameters',
    placeholder: 'Please enter custom parameters in json format',
    description: 'Please enter the correct json format',
  },
  triggerRule: {
    title: 'Select a product',
    description1: 'This product has been selected. There is no need to select it again',
    description2: 'Please select the product first',
  },
};
