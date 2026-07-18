import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName} from '../../index.d'

export const InputsTabConfig: ConfigType = {
  key: 'InputsTab',
  chartKey: 'VInputsTab',
  conKey: 'VCInputsTab',
  title: 'project.component_inputs_tab',
  category: ChatCategoryEnum.INPUTS,
  get categoryName() { return getChatCategoryEnumName().INPUTS },
  package: PackagesCategoryEnum.INFORMATIONS,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'inputs_tab.png'
}
