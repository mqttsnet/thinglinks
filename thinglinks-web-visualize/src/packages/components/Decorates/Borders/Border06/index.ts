import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const Border06Config: ConfigType = {
  key: 'Border06',
  chartKey: 'VBorder06',
  conKey: 'VCBorder06',
  title: 'project.component_border_06',
  category: ChatCategoryEnum.BORDER,
  get categoryName() { return getChatCategoryEnumName().BORDER },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'border06.png'
}
