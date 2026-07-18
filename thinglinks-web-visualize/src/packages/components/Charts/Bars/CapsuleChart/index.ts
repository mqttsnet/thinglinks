import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const CapsuleChartConfig: ConfigType = {
  key: 'CapsuleChart',
  chartKey: 'VCapsuleChart',
  conKey: 'VCCapsuleChart',
  title: 'project.component_capsule_chart',
  category: ChatCategoryEnum.BAR,
  get categoryName() { return getChatCategoryEnumName().BAR },
  package: PackagesCategoryEnum.CHARTS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'capsule.png'
}
