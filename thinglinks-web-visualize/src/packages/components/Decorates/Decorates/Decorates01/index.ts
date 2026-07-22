import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum,getChatCategoryEnumName} from '../../index.d'

export const Decorates01Config: ConfigType = {
  key: 'Decorates01',
  chartKey: 'VDecorates01',
  conKey: 'VCDecorates01',
  title: 'project.component_decorate_01',
  category: ChatCategoryEnum.DECORATE,
  get categoryName() { return getChatCategoryEnumName().DECORATE },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'decorates01.png'
}
