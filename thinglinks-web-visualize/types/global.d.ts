interface Window {
  $loading: any
  $message: any
  $dialog: any
  // 语言
  $t: any
  $vue: any
  // 键盘按键记录
  $KeyboardActive?: { [T: string]: boolean }
  onKeySpacePressHold?: Function

  // 编辑 JSON 的存储对象
  opener: any
}

declare const __THINGLINKS_PRODUCT_INFO__: {
  productCode: string
  productName: string
  componentCode: string
  componentName: string
  componentVersion: string
  editionCode: 'community' | 'commercial' | 'enterprise'
  mqNamespace: string
  licenseModel: string
  licenseFile: string
  editionNames: Record<'zh-CN' | 'en-US' | 'ja', string>
}

declare type Recordable<T = any> = Record<string, T>
