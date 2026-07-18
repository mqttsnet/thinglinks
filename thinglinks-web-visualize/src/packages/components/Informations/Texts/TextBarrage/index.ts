import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const TextBarrageConfig: ConfigType = {
  key: 'TextBarrage',
  chartKey: 'VTextBarrage',
  conKey: 'VCTextBarrage',
  title: 'project.component_text_barrage',
  category: ChatCategoryEnum.TEXT,
  get categoryName() { return getChatCategoryEnumName().TEXT },
  package: PackagesCategoryEnum.INFORMATIONS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'text_barrage.png'
}
