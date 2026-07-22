import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const LineGradientsConfig: ConfigType = {
  key: 'LineGradients',
  chartKey: 'VLineGradients',
  conKey: 'VCLineGradients',
  title: 'project.component_line_gradients',
  category: ChatCategoryEnum.LINE,
  get categoryName() { return getChatCategoryEnumName().LINE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.ECHARTS,
  image: 'line_gradient.png'
}
