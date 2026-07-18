import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const TreeMapConfig: ConfigType = {
  key: 'TreeMap',
  chartKey: 'VTreeMap',
  conKey: 'VCTreeMap',
  title: 'project.component_tree_map',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'tree_map.png'
}
