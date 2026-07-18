import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const SankeyConfig: ConfigType = {
  key: 'Sankey',
  chartKey: 'VSankey',
  conKey: 'VCSankey',
  title: 'project.component_sankey',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'sankey.png'
}
