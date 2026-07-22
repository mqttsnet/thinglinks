import { LangEnum } from '@/enums/styleEnum'

export const productInfo = __THINGLINKS_PRODUCT_INFO__

export function getProductName(locale: LangEnum): string {
  if (locale === LangEnum.ZH) return productInfo.productNames['zh-CN']
  if (locale === LangEnum.JA) return productInfo.productNames.ja
  return productInfo.productNames['en-US']
}

export function getProductEditionName(locale: LangEnum): string {
  if (locale === LangEnum.ZH) return productInfo.editionNames['zh-CN']
  if (locale === LangEnum.JA) return productInfo.editionNames.ja
  return productInfo.editionNames['en-US']
}
