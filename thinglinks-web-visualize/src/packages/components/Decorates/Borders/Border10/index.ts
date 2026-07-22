import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const Border10Config: ConfigType = {
  key: 'Border10',
  chartKey: 'VBorder10',
  conKey: 'VCBorder10',
  title: 'project.component_border_10',
  category: ChatCategoryEnum.BORDER,
  get categoryName() { return getChatCategoryEnumName().BORDER },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'border10.png'
}
