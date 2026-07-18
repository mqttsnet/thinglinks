import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const InputsDateConfig: ConfigType = {
  key: 'InputsDate',
  chartKey: 'VInputsDate',
  conKey: 'VCInputsDate',
  title: 'project.component_inputs_date',
  category: ChatCategoryEnum.INPUTS,
  get categoryName() { return getChatCategoryEnumName().INPUTS },
  package: PackagesCategoryEnum.INFORMATIONS,
  chartFrame: ChartFrameEnum.STATIC,
  image: 'inputs_date.png'
}
