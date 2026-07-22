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
    1: 'Stage via group / specific devices',
    2: 'Auto-switch after OTA upgrade',
  },

  // FULL / SHADOW tip cards
  fullTipTitle: 'Full rollout',
  fullTipDesc:
    'The new version will take effect immediately on every online device of this product. Please confirm the blast radius.',
  shadowTipTitle: 'Shadow version: works with OTA upgrade',
  shadowTipDesc:
    'A shadow publish only pre-builds the new version’s thing-model table; it does not change ' +
    'any device’s current version. After a device upgrades to the matching firmware/software via OTA, ' +
    'it auto-switches its binding to this shadow version — stage the new model first, devices ' +
    'migrate in one by one with the firmware rollout.',
  // Shadow version usage flow (publish -> link OTA -> auto take-effect)
  shadowFlowTitle: 'How to use a shadow version',
  shadowFlowStep1:
    'Publish: creates the shadow version and pre-builds its data table; no device is touched, existing devices keep running.',
  shadowFlowStep2:
    'Link OTA: in OTA Resources, set the target product version of the matching firmware/software package to this shadow version.',
  shadowFlowStep3:
    'Auto take-effect: after a device upgrades successfully via OTA (or reports the matching ' +
    'firmware/software version), it auto-switches its binding to this shadow version and data lands in the new table.',

  // Strategy execution result (publish record / version list report)
  result: {
    fullSwitched: 'device(s) switched (full)',
    canaryHit: 'device(s) rebound · target {target}',
    shadowPreBuilt: 'super table(s) pre-built · no rebind',
    ofTotalLabel: 'of all devices at publish',
    sourceGroup: 'from {n} group(s)',
    sourceManual: 'from device list',
    sourcePercent: '{percent}% by ratio',
    expandGroups: 'Show {n} group(s)',
  },

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
  groupMultiPreview: 'Selected {groups} group(s), {count} unique device(s)',
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

  // Canary impact (blast radius before publish; hit -> new version, rest + new devices -> stable)
  canaryImpactTitle: 'Canary impact',
  canaryImpactHitDevices: '{count} hit device(s) upgrade to the new version',
  canaryImpactHitPercent: '~{percent}% of devices upgrade to the new version',
  canaryImpactRest: 'The rest stay on the current stable version {version}',
  canaryImpactNewDevice:
    'Devices created or imported during canary auto-bind to the stable version (not the canary cohort)',

  // Manual whitelist parse feedback
  manualParsed: '{count} device(s) recognized',
  manualDedupedSuffix: ', {dup} duplicate(s) removed',

  // Remark
  publishRemark: 'Publish remark',
  publishRemarkPlaceholder: 'Optional, describe what is changed',

  // Max fallback retries
  maxRetryLabel: 'Max fallback retries',
  maxRetryHint: 'Auto-retry limit if async execution fails (default 3, max 10)',

  // Validation / feedback
  noStrategy: 'Please choose a release strategy first',
  noCanaryDevice: 'Canary release needs at least 1 device',
  noCanaryPercent: 'Canary percent must be between 1% and 99%',
  success: 'Published',
  failed: 'Publish failed',
};
