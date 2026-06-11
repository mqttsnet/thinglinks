export default {
  table: { title: 'ACL Rules' },
  id: 'ID',
  ruleName: 'Rule Name',
  deviceIdentification: 'Device Identification',
  actionType: 'Action Type',
  priority: 'Priority',
  topicPattern: 'MQTT Topic Pattern',
  ipWhitelist: 'IP Whitelist',
  decision: 'Decision',
  enabled: 'Enabled',
  remark: 'Remark',
  createdTime: 'Created Time',
  createdBy: 'Created By',
  updatedTime: 'Updated Time',
  updatedBy: 'Updated By',
  createdOrgId: 'Created Org ID',
  productIdentification: 'Product Identification',
  ruleLevel: 'Rule Level',
  allow: 'Allow',
  deny: 'Deny',

  /** Card view */
  card: {
    nameFallback: 'Unnamed Rule',
  },

  /** Status */
  status: {
    enabled: 'Enabled',
    disabled: 'Disabled',
  },

  /** Detail page */
  detail: {
    title: 'ACL Rule Detail',
    tabBasic: 'Basic Info',
    tabAudit: 'Audit Info',
    deviceLevelHint: 'Product-level rule, applies to all devices under this product',
    priorityLevel: {
      high: 'High Priority',
      normal: 'Normal Priority',
      low: 'Low Priority',
    },
  },

  /** Edit form sections */
  section: {
    base: 'Basic Information',
    scope: 'Scope Matching',
    decision: 'Permission Decision',
  },

  /** Action buttons */
  action: {
    pickTopic: 'Pick Topic',
    pickProduct: 'Pick Product',
    pickDevice: 'Pick Device',
    copy: 'Copy Rule',
  },

  /** Dialog titles */
  dialog: {
    pickTopic: 'Select MQTT Topic',
    pickProduct: 'Select Product',
    pickDevice: 'Select Device',
  },

  /** Placeholders */
  placeholder: {
    ruleName: 'e.g. Workshop temperature sensor subscription',
    priority: '0-1000, smaller = higher priority',
    productIdentification: 'Select a product',
    deviceIdentification: 'Required when rule level is Device',
    topicPattern: 'e.g. device/+/data or home/room/#',
    ipWhitelist: 'Press Enter to add IP, supports CIDR (e.g. 192.168.1.0/24)',
    searchTopic: 'Search topic',
  },

  /** Tips */
  tips: {
    pickProductFirst: 'Please pick a product first before selecting topic',
    ipWhitelistFormat: 'Single IP / CIDR; separate by Enter or comma',
    noTopicData: 'No topics under this product, please configure in Product Management',
    deviceCount: '{n} devices under this product. Pick from list or type to filter',
  },

  helpMessage: {
    ruleName: [
      'Note: Give the ACL rules an easily recognizable name',
      'Example: Subscription rights for workshop temperature sensors',
      'Note: It is recommended to use business-related names, such as [Region]+[Device Type]+[Permission Type]',
    ],
    ruleLevel: [
      'Explanation: The scope of application of the control rules',
      'Optional value: 0- Product level: Applied to all devices under this product (device identification left blank)',
      '1- Device Level: Only for specific devices (device identification must be filled in)',
      'Note: Product-level rules are general, device-level rules are for special cases',
    ],
    productIdentification: [
      'Note: The unique identification of the product to which the rule belongs',
      'Format requirement: Consistent with the identification in the product management module',
      'Example: "PROD_TEMP_2023"',
    ],
    deviceIdentification: [
      'Note: Required only for "Device-level" rules, applies to this specific device',
      'Pick from registered devices under the product, or type to filter (also accepts unregistered IDs)',
      'Leave blank for Product-level rules',
    ],
    priority: [
      'Note: When multiple rules conflict, the one with the smaller value has a higher priority',
      'Value range: 0-1000 (default: 500)',
      'Suggestion:',
      'The default rule of the system is set to 1000',
      'The key rule is set to 0-100',
      'The general rule is set at 101-500',
    ],
    actionType: [
      'Description: The type of MQTT operation to which the control rule applies',
      'Optional value',
      '0- All: includes publish/subscribe/unsubscribe',
      "1- Publish: Control the device's permission to publish messages",
      "2- Subscribe: Control the device's subscription topic permissions",
      '3- Unsubscribe: Control the unsubscribe permission',
    ],
    topicPattern: [
      'Description: MQTT topic filtering rules that support wildcards',
      'Example:',
      'Exact matching: device/001/temperature',
      'Single-layer matching: factory/+/status',
      'Multi-layer matching: home/room/#',
    ],
    ipWhitelist: [
      'Description: List of allowed IP addresses (CIDR format supported)',
      'Format:',
      'Multiple IPs are separated by commas',
      'Support IP segments: 192.168.1.0/24',
    ],
    decision: [
      'Explanation: Define whether the rule allows or denies access',
      'Optional value:',
      '0- Reject: Block matching requests',
      '1- Allow: Allow matching requests',
      'Note: The priority of the rejection rule should be higher than that of the permission rule',
    ],
  },

  /** Tester entry */
  testerEntry: {
    button: 'Test Rule',
    tooltip: 'Pick a real product / device, simulate a device upload and inspect the decision',
  },
};
