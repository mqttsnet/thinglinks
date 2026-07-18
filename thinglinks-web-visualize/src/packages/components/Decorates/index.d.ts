export enum ChatCategoryEnum {
  BORDER = 'Borders',
  DECORATE = 'Decorates',
  THREE = 'Three',
  MORE = 'Mores'
}

// 获取分类名称（支持多语言）
export const getChatCategoryEnumName = () => {
  const t = window['$t'] || ((key: string) => key)
  return {
    BORDER: t('project.category_border'),
    DECORATE: t('project.category_decorate'),
    THREE: t('project.category_three'),
    MORE: t('project.category_more')
  }
}
