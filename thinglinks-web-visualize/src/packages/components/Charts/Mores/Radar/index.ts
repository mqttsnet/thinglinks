import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const RadarConfig: ConfigType = {
  key: 'Radar',
  chartKey: 'VRadar',
  conKey: 'VCRadar',
  title: 'project.component_radar',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'radar.png'
}
