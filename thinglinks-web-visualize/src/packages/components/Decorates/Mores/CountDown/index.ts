import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const CountDownConfig: ConfigType = {
  key: 'CountDown',
  chartKey: 'VCountDown',
  conKey: 'VCCountDown',
  title: 'project.component_count_down',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'countdown.png'
}
