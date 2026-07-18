import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const Decorates05Config: ConfigType = {
  key: 'Decorates05',
  chartKey: 'VDecorates05',
  conKey: 'VCDecorates05',
  title: 'project.component_decorate_05',
  category: ChatCategoryEnum.DECORATE,
  get categoryName() { return getChatCategoryEnumName().DECORATE },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'decorates05.png'
}
