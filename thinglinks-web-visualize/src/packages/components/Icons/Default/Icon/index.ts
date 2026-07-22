import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const IconConfig: ConfigType = {
  key: 'Icon',
  chartKey: 'VIcon',
  conKey: 'VCIcon',
  title: 'project.component_icon',
  category: ChatCategoryEnum.DEFAULT,
  get categoryName() { return getChatCategoryEnumName().DEFAULT },
  package: PackagesCategoryEnum.ICONS,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'icon.png'
}
