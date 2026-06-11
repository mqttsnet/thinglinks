export default {
  table: { title: '告警通知订阅列表' },
  pageTitle: '告警订阅',

  // 字段
  subscriptionName: '订阅名称',
  channelType: '渠道类型',
  messageTemplate: '消息模板',
  eventType: '事件类型',
  eventTypes: '事件类型',
  priorityFilter: '级别过滤',
  priorityFilterAll: '全部',
  receiverScope: '接收范围',
  atAll: '＠全部',
  status: '状态',
  createdTime: '创建时间',

  // 事件类型映射
  event: {
    ALARM: '告警',
    DEVICE_ONLINE: '设备上线',
    DEVICE_OFFLINE: '设备离线',
    STREAM_CLOSE: '流断开',
  },

  // 接收范围
  scope: {
    SELF: '数据创建人',
    ORG: '创建人组织',
    CUSTOM: '自定义人员',
  },

  // 通用
  yes: '是',
  no: '否',
  enabled: '启用',
  disabled: '禁用',

  // Edit
  editTitleAdd: '新增订阅',
  editTitleEdit: '编辑订阅',

  // 表单字段
  messageTemplateCode: '消息模板编码',
  messageTemplateCodeHelp: '关联 base 消息模板 ExtendMsgTemplate.code',
  priorityFilterHelp: '逗号分隔, 如: 1,2,3,4 (空=全部)',
  recipientIds: '接收人ID',
  recipientIdsHelp: '用户ID, 逗号分隔 (限本部门)',
  atAllLabel: '＠所有人',
  jumpUrlTemplate: '跳转链接模板',
  jumpUrlTemplateHelp: '支持变量: [sys.domain], [bizId] 等',
  msgTemplate: '消息内容模板',
  msgTemplatePlaceholder: '支持 [变量] 占位符, 钉钉/企微支持 Markdown 格式',
  msgTemplateHelp: '留空则使用 base 消息模板默认内容',
  remark: '备注',

  // 校验规则
  rule: {
    subscriptionName: '请输入订阅名称',
    channelType: '请选择渠道类型',
    templateCode: '请输入消息模板编码',
    eventTypes: '请选择事件类型',
    token: '请输入Token',
    secret: '请输入Secret',
    appId: '请输入AppId',
    appSecret: '请输入AppSecret',
  },
};
