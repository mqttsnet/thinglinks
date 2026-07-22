import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const LineGradientSingleConfig: ConfigType = {
  key: 'LineGradientSingle',
  chartKey: 'VLineGradientSingle',
  conKey: 'VCLineGradientSingle',
  title: 'project.component_line_gradient_single',
  category: ChatCategoryEnum.LINE,
  get categoryName() { return getChatCategoryEnumName().LINE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.ECHARTS,
  image: 'line_gradient_single.png'
}
