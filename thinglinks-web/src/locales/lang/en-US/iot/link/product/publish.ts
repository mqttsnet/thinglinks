export default {
  modalTitle: 'Publish Product Version',
  currentVersion: 'Current version',
  hint: 'A new immutable snapshot will be generated from the current thing model. Device data routes by the chosen strategy.',

  // Step titles
  stepStrategy: 'Choose release strategy',
  stepConfig: 'Strategy configuration',
  stepRemark: 'Publish remark',

  // Strategies
  strategyLabel: {
    0: 'Full release',
    1: 'Canary release',
    2: 'Shadow release',
  },
  strategyDesc: {
    0: 'Roll out to all devices at once',
    1: 'Stage via device whitelist',
    2: 'Side-by-side verification, no impact on production',
  },

  // FULL / SHADOW tip cards
  fullTipTitle: 'Full rollout',
  fullTipDesc:
    'The new version will take effect immediately on every online device of this product. Please confirm the blast radius.',
  shadowTipTitle: 'Shadow mode',
  shadowTipDesc:
    'The new version only performs shadow writes; real device traffic still runs the current version. Ideal for pre-release verification.',

  // Canary config
  canaryConfig: 'Canary configuration',
  canarySourceGroup: 'By group',
  canarySourceManual: 'By device identifier',
  canarySourcePercent: 'By percent',
  canarySourceGroupTip:
    'Pick a device group; the system will use every device under it as the canary whitelist.',
  canarySourceManualTip: 'Paste device identifiers, separated by newline or comma.',
  canarySourcePercentTip:
    'Pick a percentage of devices via consistent hashing (same device + same percent always yields the same hit, no flapping on rollouts).',

  // Group panel
  groupSelectPlaceholder: 'Select a device group',
  groupDevicePreview: 'Will roll out to {count} device(s) in this group',
  groupEmpty: 'This group has no devices, please pick another one',
  groupLoadFailed: 'Failed to load devices for the selected group',

  // Manual panel
  deviceWhitelist: 'Device whitelist',
  deviceWhitelistTip: 'Separate device identifiers by newline or comma',

  // Summary
  canarySummary: '{count} device(s) selected',
  canaryPercentSummary: 'Will hit {percent}% of the product devices via consistent hashing',
  canaryPercentQuick: 'Quick',
  canaryEmpty: 'No canary devices selected yet',

  // Remark
  publishRemark: 'Publish remark',
  publishRemarkPlaceholder: 'Optional, describe what is changed',

  // Validation / feedback
  noStrategy: 'Please choose a release strategy first',
  noCanaryDevice: 'Canary release needs at least 1 device',
  noCanaryPercent: 'Canary percent must be between 1% and 99%',
  success: 'Published',
  failed: 'Publish failed',
};
