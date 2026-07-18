import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const FlipperNumberConfig: ConfigType = {
  key: 'FlipperNumber',
  chartKey: 'VFlipperNumber',
  conKey: 'VCFlipperNumber',
  title: 'project.component_flipper_number',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'flipper-number.png'
}
