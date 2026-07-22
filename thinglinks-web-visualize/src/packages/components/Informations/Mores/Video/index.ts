import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const VideoConfig: ConfigType = {
  key: 'Video',
  chartKey: 'VVideo',
  conKey: 'VCVideo',
  title: 'project.component_video',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.INFORMATIONS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'video.png'
}
