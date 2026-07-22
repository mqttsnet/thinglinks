import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const TextCommonConfig: ConfigType = {
  key: 'TextCommon',
  chartKey: 'VTextCommon',
  conKey: 'VCTextCommon',
  title: 'project.component_text_common',
  category: ChatCategoryEnum.TEXT,
  get categoryName() { return getChatCategoryEnumName().TEXT },
  package: PackagesCategoryEnum.INFORMATIONS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'text_static.png'
}
