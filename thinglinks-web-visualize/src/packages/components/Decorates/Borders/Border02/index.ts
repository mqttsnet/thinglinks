import { ConfigType, PackagesCategoryEnum, ChartFrameEnum} from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const Border02Config: ConfigType = {
  key: 'Border02',
  chartKey: 'VBorder02',
  conKey: 'VCBorder02',
  title: 'project.component_border_02',
  category: ChatCategoryEnum.BORDER,
  get categoryName() { return getChatCategoryEnumName().BORDER },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'border02.png'
}
