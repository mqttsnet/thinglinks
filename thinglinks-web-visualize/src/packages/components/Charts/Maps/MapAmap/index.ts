import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const MapAmapConfig: ConfigType = {
  key: 'MapAmap',
  chartKey: 'VMapAmap',
  conKey: 'VCMapAmap',
  title: 'project.component_map_amap',
  category: ChatCategoryEnum.MAP,
  get categoryName() { return getChatCategoryEnumName().MAP },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'map_amap.png'
}
