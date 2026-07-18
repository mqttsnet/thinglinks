import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const TableScrollBoardConfig: ConfigType = {
  key: 'TableScrollBoard',
  chartKey: 'VTableScrollBoard',
  conKey: 'VCTableScrollBoard',
  title: 'project.component_table_scroll_board',
  category: ChatCategoryEnum.TABLE,
  get categoryName() { return getChatCategoryEnumName().TABLE },
  package: PackagesCategoryEnum.TABLES,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'table_scrollboard.png'
}
