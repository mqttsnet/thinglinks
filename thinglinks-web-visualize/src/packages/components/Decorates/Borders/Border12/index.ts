import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const Border12Config: ConfigType = {
  key: 'Border12',
  chartKey: 'VBorder12',
  conKey: 'VCBorder12',
  title: 'project.component_border_12',
  category: ChatCategoryEnum.BORDER,
  get categoryName() { return getChatCategoryEnumName().BORDER },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'border12.png'
}
