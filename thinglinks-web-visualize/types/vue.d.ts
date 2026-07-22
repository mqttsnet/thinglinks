import 'vue'
import '@vue/runtime-core'

declare module 'vue' {
  interface ComponentCustomProperties {
    $t: (key: string, ...args: unknown[]) => string
  }
}

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $t: (key: string, ...args: unknown[]) => string
  }
}

export {}
