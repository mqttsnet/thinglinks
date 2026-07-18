import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const ScatterLogarithmicRegressionConfig: ConfigType = {
  key: 'ScatterLogarithmicRegression',
  chartKey: 'VScatterLogarithmicRegression',
  conKey: 'VCScatterLogarithmicRegression',
  title: 'project.component_scatter_logarithmic_regression',
  category: ChatCategoryEnum.SCATTER,
  get categoryName() { return getChatCategoryEnumName().SCATTER },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.ECHARTS,
  image: 'scatter-logarithmic-regression.png'
}
