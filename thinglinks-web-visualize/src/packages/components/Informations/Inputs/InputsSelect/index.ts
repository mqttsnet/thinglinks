import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const InputsSelectConfig: ConfigType = {
  key: 'InputsSelect',
  chartKey: 'VInputsSelect',
  conKey: 'VCInputsSelect',
  title: 'project.component_inputs_select',
  category: ChatCategoryEnum.INPUTS,
  get categoryName() { return getChatCategoryEnumName().INPUTS },
  package: PackagesCategoryEnum.INFORMATIONS,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'inputs_select.png'
}
