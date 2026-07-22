import type { LocaleType } from '/#/config';

export const productInfo = __THINGLINKS_PRODUCT_INFO__;

export function getProductEditionName(locale: LocaleType): string {
  if (locale === 'zh-CN' || locale === 'en-US' || locale === 'ja') {
    return productInfo.editionNames[locale];
  }
  return productInfo.editionNames['en-US'];
}
