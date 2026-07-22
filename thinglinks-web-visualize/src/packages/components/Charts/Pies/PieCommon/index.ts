import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const PieCommonConfig: ConfigType = {
  key: 'PieCommon',
  chartKey: 'VPieCommon',
  conKey: 'VCPieCommon',
  title: 'project.component_pie_common',
  category: ChatCategoryEnum.PIE,
  get categoryName() { return getChatCategoryEnumName().PIE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.ECHARTS,
  image: 'pie.png'
}
