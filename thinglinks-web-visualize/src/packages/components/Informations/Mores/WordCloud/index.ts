import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const WordCloudConfig: ConfigType = {
  key: 'WordCloud',
  chartKey: 'VWordCloud',
  conKey: 'VCWordCloud',
  title: 'project.component_word_cloud',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.INFORMATIONS,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'words_cloud.png'
}
