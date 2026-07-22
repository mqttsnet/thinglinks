import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum,getChatCategoryEnumName} from '../../index.d'

export const Decorates02Config: ConfigType = {
  key: 'Decorates02',
  chartKey: 'VDecorates02',
  conKey: 'VCDecorates02',
  title: 'project.component_decorate_02',
  category: ChatCategoryEnum.DECORATE,
  get categoryName() { return getChatCategoryEnumName().DECORATE },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'decorates02.png'
}
