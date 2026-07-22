interface Window {
  $loading: any
  $message: any
  $dialog: any
  // 语言
  $t: any
  $vue: any
  MonacoEnvironment: {
    getWorker: (workerId: string, label: string) => Worker
  }
  // 键盘按键记录
  $KeyboardActive?: { ctrl: boolean; space: boolean }
  onKeySpacePressHold?: (isHold: boolean) => void

  // 编辑 JSON 的存储对象
  opener: any
}

declare const __THINGLINKS_PRODUCT_INFO__: {
  productCode: string
  productName: string
  productNames: Record<'zh-CN' | 'en-US' | 'ja', string>
  componentCode: string
  componentName: string
  componentVersion: string
  editionCode: 'community' | 'commercial' | 'enterprise'
  mqNamespace: string
  publicSiteUrl: string
  licenseModel: string
  licenseFile: string
  editionNames: Record<'zh-CN' | 'en-US' | 'ja', string>
}

declare type Recordable<T = any> = Record<string, T>
declare type Nullable<T> = T | null
