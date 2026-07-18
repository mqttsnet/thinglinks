import { ConfigType, PackagesCategoryEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const PipelineVConfig: ConfigType = {
  key: 'PipelineV',
  chartKey: 'VPipelineV',
  conKey: 'VCPipelineV',
  title: 'project.component_pipeline_v',
  category: ChatCategoryEnum.MORE,
  get categoryName() { return getChatCategoryEnumName().MORE },
  package: PackagesCategoryEnum.DECORATES,
  image: 'Pipeline_V.png'
}
