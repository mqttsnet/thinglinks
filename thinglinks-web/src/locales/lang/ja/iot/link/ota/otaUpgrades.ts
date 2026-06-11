export default {
  table: {
    title: 'OTAアップグレード包リスト',
  },
  id: '主键',
  appId: 'アプリケーションシーン',
  packageName: '包名称',
  packageType: 'アップグレード包タイプ',
  productIdentification: '製品标识',
  version: 'アップグレード包バージョン号',
  upgradePackage: 'アップグレードパッケージ',
  fileLocation: 'アップグレード包的位置',
  signMethod: '署名方法',
  status: 'ステータス',
  description: 'アップグレード包功能説明',
  customInfo: '自定义情報',
  remark: '説明',
  createdBy: '作成者',
  createdTime: '作成時間',
  updatedBy: '更新人',
  updatedTime: '更新時間',
  createdOrgId: '作成者組織',
  helpMessage: {
    version:
      '请输入バージョン号（格式：x.y.z[-预リリース标签][+构建元データ]），例如：1.0.0、1.0.0-alpha、1.0.0+20200101',
    customInfo:
      'カスタム情報は形式や内容に制限はありません。このアップグレードパッケージに基づいてアップグレードタスクを作成した後、アップグレード通知としてデバイスに送信されます',
  },
  versionRule: 'バージョン号格式エラー',
  placeholder: {
    customInfoPlaceholder: 'デバイスにプッシュするカスタム情報を入力してください',
    descriptionPlaceholder: 'アップグレードパッケージの説明を入力してください',
  },
  codeEditorDefine: {
    title: '辅助编写',
    helpMessage: '辅助编写自定义パラメータ',
    placeholder: '请输入自定义パラメータjson格式',
    description: '请输入正确的json格式',
  },
  triggerRule: {
    title: '选择製品',
    description1: '已选择该製品，无需重新选择',
    description2: '请先选择製品',
  },
};
