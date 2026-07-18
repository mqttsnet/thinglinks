import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const Decorates06Config: ConfigType = {
  key: 'Decorates06',
  chartKey: 'VDecorates06',
  conKey: 'VCDecorates06',
  title: 'project.component_decorate_06',
  category: ChatCategoryEnum.DECORATE,
  get categoryName() { return getChatCategoryEnumName().DECORATE },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'decorates06.png'
}
