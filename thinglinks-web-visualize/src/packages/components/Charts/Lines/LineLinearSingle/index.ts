import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const LineLinearSingleConfig: ConfigType = {
  key: 'LineLinearSingle',
  chartKey: 'VLineLinearSingle',
  conKey: 'VCLineLinearSingle',
  title: 'project.component_line_linear_single',
  category: ChatCategoryEnum.LINE,
  get categoryName() { return getChatCategoryEnumName().LINE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.ECHARTS,
  image: 'line_linear_single.png'
}
