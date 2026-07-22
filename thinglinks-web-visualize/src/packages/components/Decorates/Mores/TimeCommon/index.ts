import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum,getChatCategoryEnumName} from '../../index.d'

export const TimeCommonConfig: ConfigType = {
  key: 'TimeCommon',
  chartKey: 'VTimeCommon',
  conKey: 'VCTimeCommon',
  title: 'project.component_time_common',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'time.png'
}
