import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const WaterPoloConfig: ConfigType = {
  key: 'WaterPolo',
  chartKey: 'VWaterPolo',
  conKey: 'VCWaterPolo',
  title: 'project.component_water_polo',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'water_WaterPolo.png'
}
