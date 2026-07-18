import { ConfigType, PackagesCategoryEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const PipelineHConfig: ConfigType = {
  key: 'PipelineH',
  chartKey: 'VPipelineH',
  conKey: 'VCPipelineH',
  title: 'project.component_pipeline_h',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.DECORATES,
  image: 'Pipeline_H.png'
}
