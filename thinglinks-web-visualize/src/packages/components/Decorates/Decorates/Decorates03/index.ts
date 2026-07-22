import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const Decorates03Config: ConfigType = {
  key: 'Decorates03',
  chartKey: 'VDecorates03',
  conKey: 'VCDecorates03',
  title: 'project.component_decorate_03',
  category: ChatCategoryEnum.DECORATE,
  get categoryName() { return getChatCategoryEnumName().DECORATE },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'decorates03.png'
}
