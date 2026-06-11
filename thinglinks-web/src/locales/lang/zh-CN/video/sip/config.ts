export default {
  // 页面
  pageTitle: 'SIP 接入配置',

  // 字段
  configName: '配置名称',
  sipId: 'SIP 服务器编号',
  sipDomain: 'SIP 域',
  sipServerAddress: 'SIP 服务器地址',
  bindIp: '绑定 IP',
  isDefault: '默认',
  status: '状态',
  createdTime: '创建时间',
  sipPassword: '认证密码',
  registerInterval: '注册有效期(秒)',
  isDefaultLabel: '是否默认',
  remark: '备注',

  // 状态/默认
  enabled: '启用',
  disabled: '禁用',
  yes: '是',
  no: '否',
  defaultTag: '★ 默认',

  // 帮助
  sipIdHelp:
    '本平台对外暴露的「SIP 服务器编号」，20 位数字。设备端 GB28181/SIP 配置里的「SIP 服务器编号」必须填写此值（前 10 位为行政区划码，与下方 SIP 域一致）。⚠️ 此值不能与任何接入设备的「设备编号」相同，否则 INVITE 200 OK 之后设备会立即 BYE 拆 dialog，前端表现为播放一直转圈。常见摄像头出厂默认会把「SIP 服务器编号」和「设备编号」设成同一个值，请务必把设备的「设备编号」改成另一个 20 位国标编号。',
  sipDomainHelp: 'SIP 服务器编号的前 10 位（行政区划码）',
  sipPasswordHelp: '设备注册的默认密码，AES 加密存储；与设备端「SIP 认证密码」保持一致',
  sipServerAddressHelp: '设备端「SIP 服务器 IP / 地址」填写的值；可以是域名或 IP，集群部署时建议填 Nginx VIP / 域名',
  bindIpHelp: '多网卡隔离场景下指定监听的网卡 IP，逗号分隔多个；留空表示不限制',
  registerIntervalHelp: '留空使用全局默认值',
  isDefaultHelp: '默认配置用于出站 SIP 消息的 From header（向设备 INVITE / BYE 等）',

  // 操作
  setDefault: '设为默认',
  setDefaultConfirm: '设为默认配置？',
  refreshCache: '刷新缓存',
  setDefaultSuccess: '已设为默认',
  cacheRefreshed: '缓存已刷新',

  // 校验
  rule: {
    configName: '请输入配置名称',
    sipId: '请输入 SIP 服务器编号',
    sipIdLen: 'SIP 服务器编号必须为 20 位',
    sipIdPattern: 'SIP 服务器编号必须为 20 位数字',
    sipDomain: '请输入 SIP 域',
    sipPassword: '请输入认证密码',
  },
};
