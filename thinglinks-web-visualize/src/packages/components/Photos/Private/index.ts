import { ChartFrameEnum, ConfigType, PackagesCategoryEnum } from '@/packages/index.d'
import { ImageConfig } from '@/packages/components/Informations/Mores/Image/index'
import { ChatCategoryEnum, getChatCategoryEnumName } from '../index.d'
import { setLocalStorage, getLocalStorage, goDialog } from '@/utils'
import { StorageEnum } from '@/enums/storageEnum'
import { FileTypeEnum } from '@/enums/fileTypeEnum'
import { backgroundImageSize } from '@/settings/designSetting'
import { usePackagesStore } from '@/store/modules/packagesStore/packagesStore'

const StoreKey = StorageEnum.GO_USER_MEDIA_PHOTOS

/**
 * 上传完成事件类型
 */
type UploadCompletedEventType = {
  fileName: string
  url: string
}

const storedUserPhotos: ConfigType[] = getLocalStorage(StoreKey) || []
const userPhotosList: ConfigType[] = storedUserPhotos.map(photo => ({
  ...photo,
  get categoryName() { return getChatCategoryEnumName().PRIVATE }
}))

const uploadFile = (callback: Function | null = null) => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.png,.jpg,.jpeg,.gif' // 这里只允许部分图片类型
  input.onchange = async () => {
    if (!input.files || !input.files.length) return
    const file = input.files[0]
    const { name, size, type } = file
    if (size > 1024 * 1024 * backgroundImageSize) {
      window['$message'].warning(window['$t']('project.photo_size_exceeded', { size: backgroundImageSize }))
      return false
    }
    if (type !== FileTypeEnum.PNG && type !== FileTypeEnum.JPEG && type !== FileTypeEnum.GIF) {
      window['$message'].warning(window['$t']('project.photo_format_error'))
      return false
    }
    const reader = new FileReader()
    reader.onload = () => {
      const eventObj: UploadCompletedEventType = { fileName: name, url: reader.result as string }
      callback && callback(eventObj)
    }
    reader.readAsDataURL(file)
  }
  input.click()
}

const addConfig = {
  ...ImageConfig,
  category: ChatCategoryEnum.PRIVATE,
  get categoryName() { return getChatCategoryEnumName().PRIVATE },
  package: PackagesCategoryEnum.PHOTOS,
  chartFrame: ChartFrameEnum.STATIC,
  title: '点击上传图片', // 此字段在组件注册时使用，多语在显示时处理
  image: 'upload.png',
  redirectComponent: `${ImageConfig.package}/${ImageConfig.category}/${ImageConfig.key}`, // 跳转组件路径规则：packageName/categoryName/componentKey
  disabled: true,
  configEvents: {
    // 点击上传事件
    addHandle: (photoConfig: ConfigType) => {
      goDialog({
        message: window['$t']('project.photo_upload_message', { size: backgroundImageSize }),
        transformOrigin: 'center',
        onPositiveCallback: () => {
          uploadFile((e: UploadCompletedEventType) => {
            // 和上传组件一样配置，更换标题，图片，预设数据
            const packagesStore = usePackagesStore()
            const newPhoto = {
              ...ImageConfig,
              category: ChatCategoryEnum.PRIVATE,
              get categoryName() { return getChatCategoryEnumName().PRIVATE },
              package: PackagesCategoryEnum.PHOTOS,
              chartFrame: ChartFrameEnum.STATIC,
              title: e.fileName,
              image: e.url,
              dataset: e.url,
              redirectComponent: `${ImageConfig.package}/${ImageConfig.category}/${ImageConfig.key}` // 跳转组件路径规则：packageName/categoryName/componentKey
            }
            userPhotosList.unshift(newPhoto)
            // 存储在本地数据中
            setLocalStorage(StoreKey, userPhotosList)
            // 插入到上传按钮前的位置
            packagesStore.addPhotos(newPhoto, 1)
          })
        }
      })
    }
  }
}

export default [addConfig, ...userPhotosList]
