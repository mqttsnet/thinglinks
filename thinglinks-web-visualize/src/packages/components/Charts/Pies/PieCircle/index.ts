import image from '@/assets/images/chart/charts/pie-circle.png'
import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const PieCircleConfig: ConfigType = {
  key: 'PieCircle',
  chartKey: 'VPieCircle',
  conKey: 'VCPieCircle',
  title: 'project.component_pie_circle',
  category: ChatCategoryEnum.PIE,
  get categoryName() { return getChatCategoryEnumName().PIE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.ECHARTS,
  image: 'pie-circle.png'
}
