import { ref } from 'vue'
import { ChartEnum, PreviewEnum } from '@/enums/pageEnum'
import { routerTurnByNameWithQuery } from '@/utils'
import { Chartype } from '../../../index.d'
export const useModalDataInit = () => {
  const modalShow = ref<boolean>(false)
  const modalData = ref<Chartype | null>(null)

  // 关闭 modal
  const closeModal = () => {
    modalShow.value = false
    modalData.value = null
  }

  // 缩放处理
  const resizeHandle = (cardData: Chartype) => {
    if (!cardData) return
    modalShow.value = true
    modalData.value = cardData
  }

  // 编辑处理
  const editHandle = (cardData: Chartype) => {
    if (!cardData) return
    const identification = cardData.projectIdentification || cardData.templateIdentification
    if (!identification) return
    routerTurnByNameWithQuery(
      ChartEnum.CHART_HOME_NAME,
      {
        identification: identification,
        isMyProject: cardData.projectIdentification ? 1 : 0
      },
      { id: '' }, // 路由参数占位符
      undefined,
      true
    )
  }

  // 预览处理
  const previewHandle = (cardData: Chartype) => {
    if (!cardData) return
    const identification = cardData.projectIdentification || cardData.templateIdentification
    if (!identification) return
    routerTurnByNameWithQuery(
      PreviewEnum.CHART_PREVIEW_NAME,
      {
        identification: identification,
        isMyProject: cardData.projectIdentification ? 1 : 0
      },
      { }, // 路由参数
      undefined,
      true
    )
  }

  return {
    modalData,
    modalShow,
    closeModal,
    resizeHandle,
    editHandle,
    previewHandle
  }
}
