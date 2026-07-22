import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const Border03Config: ConfigType = {
  key: 'Border03',
  chartKey: 'VBorder03',
  conKey: 'VCBorder03',
  title: 'project.component_border_03',
  category: ChatCategoryEnum.BORDER,
  get categoryName() { return getChatCategoryEnumName().BORDER },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'border03.png'
}
