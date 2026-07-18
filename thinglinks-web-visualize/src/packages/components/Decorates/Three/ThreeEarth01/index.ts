import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const ThreeEarth01Config: ConfigType = {
  key: 'ThreeEarth01',
  chartKey: 'VThreeEarth01',
  conKey: 'VCThreeEarth01',
  title: 'project.component_three_earth_01',
  category: ChatCategoryEnum.THREE,
  get categoryName() { return getChatCategoryEnumName().THREE },
  package: PackagesCategoryEnum.DECORATES,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'threeEarth01.png'
}
