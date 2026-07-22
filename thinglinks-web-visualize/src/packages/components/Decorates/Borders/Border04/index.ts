import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const Border04Config: ConfigType = {
  key: 'Border04',
  chartKey: 'VBorder04',
  conKey: 'VCBorder04',
  title: 'project.component_border_04',
  category: ChatCategoryEnum.BORDER,
  get categoryName() { return getChatCategoryEnumName().BORDER },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'border04.png'
}
