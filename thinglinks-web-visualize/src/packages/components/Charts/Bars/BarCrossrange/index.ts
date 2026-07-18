import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const BarCrossrangeConfig: ConfigType = {
  key: 'BarCrossrange',
  chartKey: 'VBarCrossrange',
  conKey: 'VCBarCrossrange',
  title: 'project.component_bar_crossrange',
  category: ChatCategoryEnum.BAR,
  get categoryName() { return getChatCategoryEnumName().BAR },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.ECHARTS,
  image: 'bar_y.png'
}
