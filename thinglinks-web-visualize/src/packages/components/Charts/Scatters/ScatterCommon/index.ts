import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const ScatterCommonConfig: ConfigType = {
  key: 'ScatterCommon',
  chartKey: 'VScatterCommon',
  conKey: 'VCScatterCommon',
  title: 'project.component_scatter_common',
  category: ChatCategoryEnum.SCATTER,
  get categoryName() { return getChatCategoryEnumName().SCATTER },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.ECHARTS,
  image: 'scatter-multi.png'
}
