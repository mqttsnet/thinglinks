export default {
  table: { title: 'OtaUpgradeTasks list' },
  upgradeId: 'UpgradeId',
  taskName: 'TaskName',
  productIdentification: 'Product Identification for Upgrade Package',
  taskStatus: 'TaskStatus',
  scheduledTime: 'Scheduled Time',
  upgradeMode: 'Upgrade Mode',
  upgradeModeTips: {
    staticUpgrade: 'Static Upgrade: A one-time upgrade task with a short lifecycle;',
    dynamicUpgrade:
      'Dynamic Upgrade: A long-term effective upgrade rule with a long lifecycle that will continuously ' +
      'maintain the scope of devices to be upgraded, including devices that have already reported version ' +
      'numbers and newly activated devices;',
  },
  upgradeScope: 'Upgrade Scope',
  directUpgrade: 'Direct Upgrade',
  directUpgradeTips: {
    selectDirectUpgradeDevice: 'Please select devices for direct upgrade',
  },
  groupUpgrade: 'Group Upgrade',
  groupId: 'Group ID',
  areaUpgrade: 'Area Upgrade',
  areaCode: 'Area Code',
  areaUpgradeTips: {
    selectAreaRequired: 'Please select province and city',
    areaIdsRepeat: 'City information cannot be duplicated',
  },
  maxRetryCount: 'Maximum Retry Count',
  maxRetryCountTips: {
    rangeValidation: 'Maximum retry count must be >= 1 and <= 5',
  },
  retryIntervalMinutes: 'Retry Interval (Minutes)',
  retryIntervalMinutesRequired: 'Retry interval minutes cannot be empty',
  retryIntervalMinutesRangeValidation: 'Retry interval minutes must be >= 1',
  appConfirmationRequired: 'APP Confirmation Required',
  appConfirmationRequiredTips:
    'If yes is selected, devices will only receive upgrade notifications or be able to actively request upgrade tasks after upgrade confirmation through the open API.', // eslint-disable-line
  upgradeRate: 'Upgrade Rate',
  upgradeRateRequired: 'Upgrade rate cannot be empty',
  upgradeRateRangeValidation: 'Upgrade rate must be between 10-1000',
  deviceUpgradeTimeout: 'Device Upgrade Timeout',
  deviceUpgradeTimeoutPlaceholder: 'Leave blank for no timeout by default',
  deviceUpgradeTimeoutTips:
    'Configure time range from 5~1440 minutes (24 hours), leave blank for no timeout by default',
  deviceUpgradeTimeoutRangeValidation: 'Device upgrade timeout must be between 5-1440 minutes',
  scheduledStartTime: 'Scheduled Start Time',
  scheduledStartTimeRequired: 'Scheduled start time cannot be empty',
  scheduledStartTimeAfterTenMinutes:
    'Scheduled start time must be greater than current time + 10 minutes',
  scheduledEndTime: 'Scheduled End Time',
  scheduledEndTimeRequired: 'Scheduled end time cannot be empty',
  scheduledEndTimeAfterStartTime: 'Scheduled end time must be after the start time',
  scheduledEndTimeAfterTenMinutes:
    'Scheduled end time must be greater than current time + 10 minutes',
  sourceVersions: 'Source Versions to Upgrade',
  description: 'Description',
  remark: 'Remark',
  createdBy: 'CreatedBy',
  createdTime: 'CreatedTime',
  updatedBy: 'UpdatedBy',
  updatedTime: 'UpdatedTime',
  createdOrgId: 'CreatedOrgId',
  id: 'Id',
  confirmText:
    'After creation, upgrade scope and related information cannot be edited. Are you sure you want to create?',
  upgradeTaskDetail: 'Upgrade Task Detail',
  upgradePackageDetail: 'Upgrade Package Detail',
  taskDetail: 'Task Detail',
  taskBasicInfo: 'Basic info',
  fetchTaskDetailFail: 'Failed to fetch upgrade task detail',
  taskId: 'Task ID',
  upgradeRecords: 'Upgrade Records',
};
