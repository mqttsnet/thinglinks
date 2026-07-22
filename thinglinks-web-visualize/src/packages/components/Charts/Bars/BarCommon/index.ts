import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const BarCommonConfig: ConfigType = {
  key: 'BarCommon',
  chartKey: 'VBarCommon',
  conKey: 'VCBarCommon',
  title: 'project.component_bar_common',
  category: ChatCategoryEnum.BAR,
  get categoryName() { return getChatCategoryEnumName().BAR },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.ECHARTS,
  image: 'bar_x.png'
}
