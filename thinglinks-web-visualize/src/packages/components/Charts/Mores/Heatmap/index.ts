import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const HeatmapConfig: ConfigType = {
  key: 'Heatmap',
  chartKey: 'VHeatmap',
  conKey: 'VCHeatmap',
  title: 'project.component_heatmap',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'heatmap.png'
}
