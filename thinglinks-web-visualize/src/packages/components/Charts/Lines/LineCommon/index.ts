import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const LineCommonConfig: ConfigType = {
  key: 'LineCommon',
  chartKey: 'VLineCommon',
  conKey: 'VCLineCommon',
  title: 'project.component_line_common',
  category: ChatCategoryEnum.LINE,
  get categoryName() { return getChatCategoryEnumName().LINE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.ECHARTS,
  image: 'line.png'
}
