export default {
  table: { title: 'ACL规则' },
  id: '主键ID',
  ruleName: '规则名称',
  deviceIdentification: '设备标识',
  actionType: '动作类型',
  priority: '规则优先级',
  topicPattern: 'MQTT主题模式',
  ipWhitelist: 'IP白名单',
  decision: '决策',
  enabled: '是否启用',
  remark: '备注',
  createdTime: '创建时间',
  createdBy: '创建人',
  updatedTime: '最后修改时间',
  updatedBy: '最后修改人',
  createdOrgId: '创建人组织',
  productIdentification: '产品标识',
  ruleLevel: '规则级别',
  allow: '允许',
  deny: '拒绝',

  /** 卡片视图 */
  card: {
    nameFallback: '未命名规则',
  },

  /** 启用 / 禁用 状态文案 */
  status: {
    enabled: '已启用',
    disabled: '已禁用',
  },

  /** 详情页 */
  detail: {
    title: 'ACL规则详情',
    tabBasic: '基本信息',
    tabAudit: '审计信息',
    deviceLevelHint: '产品级规则,作用于该产品下所有设备',
    priorityLevel: {
      high: '高优先级',
      normal: '普通优先级',
      low: '低优先级',
    },
  },

  /** 编辑页 ── 分组 */
  section: {
    base: '基础信息',
    scope: '范围匹配',
    decision: '权限决策',
  },

  /** 操作按钮 */
  action: {
    pickTopic: '选择主题',
    pickProduct: '选择产品',
    pickDevice: '选择设备',
    copy: '复制规则',
  },

  /** 弹窗标题 */
  dialog: {
    pickTopic: '选择 MQTT 主题',
    pickProduct: '选择产品',
    pickDevice: '选择设备',
  },

  /** 输入框 placeholder */
  placeholder: {
    ruleName: '如:车间温度传感器订阅权限',
    priority: '0-1000,数值越小优先级越高',
    productIdentification: '请选择产品',
    deviceIdentification: '请输入设备标识(规则级别为设备级时必填)',
    topicPattern: '如:device/+/data 或 home/room/#',
    ipWhitelist: '回车添加 IP,支持 CIDR(如 192.168.1.0/24)',
    searchTopic: '搜索 topic',
  },

  /** 提示信息 */
  tips: {
    pickProductFirst: '请先选择产品后再选择 topic',
    ipWhitelistFormat: '支持单 IP / CIDR 段;按回车或逗号分隔多条',
    noTopicData: '该产品下暂无 topic,请先到产品管理中配置',
    deviceCount: '该产品下共 {n} 个设备,可下拉选择或直接输入',
  },

  /** 帮助说明(rule level / 字段说明等) */
  helpMessage: {
    ruleName: [
      '说明:给 ACL 规则起一个易于识别的名称',
      '示例:车间温度传感器订阅权限',
      '注意:建议使用业务相关命名,如 [区域]+[设备类型]+[权限类型]',
    ],
    ruleLevel: [
      '说明:控制规则的适用范围',
      '可选值:0-产品级:应用于该产品下所有设备(设备标识留空)',
      '1-设备级:仅针对特定设备(需填写设备标识)',
      '注意:产品级规则一般用于通用约束,设备级规则用于特殊处理',
    ],
    productIdentification: [
      '说明:规则所属的产品唯一标识',
      '格式要求:与产品管理模块中的标识一致',
      '示例:"PROD_TEMP_2023"',
    ],
    deviceIdentification: [
      '说明:仅"设备级"规则需要填写,表示规则只对此特定设备生效',
      '可下拉选择该产品下已注册的设备,或直接输入设备标识(应对尚未注册的设备)',
      '产品级规则请将此字段留空',
    ],
    priority: [
      '说明:当多条规则冲突时,数值小的优先级更高',
      '取值范围:0-1000(默认500)',
      '建议:',
      '系统默认规则设为 1000',
      '关键规则设为 0-100',
      '普通规则设为 101-500',
    ],
    actionType: [
      '说明:控制规则适用的 MQTT 操作类型',
      '可选值',
      '0-全部:包含发布/订阅/取消订阅',
      '1-发布:控制设备发布消息权限',
      '2-订阅:控制设备订阅主题权限',
      '3-取消订阅:控制取消订阅权限',
    ],
    topicPattern: [
      '说明:支持通配符的 MQTT 主题过滤规则',
      '示例:',
      '精确匹配:device/001/temperature',
      '单层匹配:factory/+/status',
      '多层匹配:home/room/#',
    ],
    ipWhitelist: [
      '说明:允许访问的 IP 地址列表(支持 CIDR 格式)',
      '格式:',
      '多个 IP 用英文逗号分隔',
      '支持 IP 段:192.168.1.0/24',
    ],
    decision: [
      '说明:定义规则是允许还是拒绝访问',
      '可选值:',
      '0-拒绝:阻断匹配的请求',
      '1-允许:放行匹配的请求',
      '注意:拒绝规则的优先级应高于允许规则',
    ],
  },

  /** 测试规则按钮 */
  testerEntry: {
    button: '规则测试',
    tooltip: '基于此规则,选真实产品 / 设备模拟设备实际上报,查看是否命中以及决策结果',
  },
};
