import { ConfigType, PackagesCategoryEnum, ChartFrameEnum } from '@/packages/index.d'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../../index.d'

export const MapBaseConfig: ConfigType = {
    key: 'MapBase',
    chartKey: 'VMapBase',
    conKey: 'VCMapBase',
    title: 'project.component_map_base',
    category: ChatCategoryEnum.MAP,
    get categoryName() { return getChatCategoryEnumName().MAP },
    package: PackagesCategoryEnum.CHARTS,
    chartFrame: ChartFrameEnum.COMMON,
    image: 'map.png'
}