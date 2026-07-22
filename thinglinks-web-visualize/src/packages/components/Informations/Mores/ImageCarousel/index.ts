import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const ImageCarouselConfig: ConfigType = {
  key: 'ImageCarousel',
  chartKey: 'VImageCarousel',
  conKey: 'VCImageCarousel',
  title: 'project.component_image_carousel',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.INFORMATIONS,
  chartFrame: ChartFrameEnum.NAIVE_UI,
  image: 'photo_carousel.png'
}
