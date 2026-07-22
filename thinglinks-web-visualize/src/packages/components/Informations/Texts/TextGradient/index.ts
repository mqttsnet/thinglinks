import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const TextGradientConfig: ConfigType = {
  key: 'TextGradient',
  chartKey: 'VTextGradient',
  conKey: 'VCTextGradient',
  title: 'project.component_text_gradient',
  category: ChatCategoryEnum.TEXT,
  get categoryName() { return getChatCategoryEnumName().TEXT },
  package: PackagesCategoryEnum.INFORMATIONS,
  chartFrame: ChartFrameEnum.NAIVE_UI,
  image: 'text_gradient.png'
}
