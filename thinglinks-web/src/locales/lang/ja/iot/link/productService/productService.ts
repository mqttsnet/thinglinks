export default {
  table: {
    title: '製品模型サービスリスト',
  },
  id: 'サービスid',
  productId: '製品ID',
  serviceCode: 'サービスコード',
  serviceName: 'サービス名称',
  serviceType: 'サービスタイプ',
  serviceStatus: 'ステータス',
  description: '説明情報',
  remark: '备考',
  createdTime: '作成時間',
  createdBy: '作成者',
  updatedTime: '最后更新時間',
  updatedBy: '最后更新者',
  createdOrgId: '作成者組織',
  serviceList: 'サービス一覧',
  attributeList: 'プロパティ一覧',
  commandList: 'コマンド一覧',
  emptyService: 'サービスがありません、+ で追加してください',
  pickServiceHint: '左側からサービスを選択してください',
  helpMessage: {
    serviceCode:
      '支持英文小写、数字及下划线，すべて小写命名，禁止出现英文大写，多个单词用下划线，分隔长度[2,50]',
    description: '文本説明，不影响实际功能，可設定为空字符串””',
  },
};
