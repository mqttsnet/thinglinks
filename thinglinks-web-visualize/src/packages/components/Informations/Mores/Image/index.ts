import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const ImageConfig: ConfigType = {
  key: 'Image',
  chartKey: 'VImage',
  conKey: 'VCImage',
  title: 'project.component_image',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.INFORMATIONS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'photo.png'
}
