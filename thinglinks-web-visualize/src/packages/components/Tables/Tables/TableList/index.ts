import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const TableListConfig: ConfigType = {
  key: 'TableList',
  chartKey: 'VTableList',
  conKey: 'VCTableList',
  title: 'project.component_table_list',
  category: ChatCategoryEnum.TABLE,
  get categoryName() { return getChatCategoryEnumName().TABLE },
  package: PackagesCategoryEnum.TABLES,
  chartFrame: ChartFrameEnum.COMMON,
  image: 'tables_list.png'
}
