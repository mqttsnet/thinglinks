import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const Decorates04Config: ConfigType = {
  key: 'Decorates04',
  chartKey: 'VDecorates04',
  conKey: 'VCDecorates04',
  title: 'project.component_decorate_04',
  category: ChatCategoryEnum.DECORATE,
  get categoryName() { return getChatCategoryEnumName().DECORATE },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'decorates04.png'
}
