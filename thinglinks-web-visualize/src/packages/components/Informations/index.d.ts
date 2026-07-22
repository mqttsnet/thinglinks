export enum ChatCategoryEnum {
  TEXT = 'Texts',
  TITLE = 'Titles',
  INPUTS = 'Inputs',
  MORE = 'Mores'
}

// 获取分类名称（支持多语言）
export const getChatCategoryEnumName = () => {
  const t = window['$t'] || ((key: string) => key)
  return {
    TEXT: t('project.category_text'),
    TITLE: t('project.category_title'),
    INPUTS: t('project.category_inputs'),
    MORE: t('project.category_more')
  }
}
