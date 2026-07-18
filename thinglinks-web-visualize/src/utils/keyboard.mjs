/**
 * 将新旧浏览器的按键字段归一为画布关心的按键。
 */
export const getTrackedKeyboardKey = event => {
  if (
    event.key === 'Control' ||
    event.key === 'Meta' ||
    event.code === 'ControlLeft' ||
    event.code === 'ControlRight' ||
    event.code === 'MetaLeft' ||
    event.code === 'MetaRight' ||
    event.keyCode === 17 ||
    event.keyCode === 91 ||
    event.keyCode === 93
  ) {
    return 'ctrl'
  }
  if (event.key === ' ' || event.code === 'Space' || event.keyCode === 32) {
    return 'space'
  }
  return undefined
}

/**
 * 输入框、选择框和可编辑内容中的空格必须保留默认输入语义。
 */
export const isEditableKeyboardTarget = target => {
  if (!target || typeof target !== 'object') return false
  if (target.isContentEditable) return true

  const tagName = target.tagName?.toLowerCase()
  if (tagName && ['input', 'textarea', 'select', 'option'].includes(tagName)) return true

  return Boolean(
    target.closest?.('input, textarea, select, option, [contenteditable]:not([contenteditable="false"])')
  )
}

/**
 * 每个按键只更新自己的状态，避免 Ctrl 和 Space 按下/抬起顺序不同时互相覆盖。
 */
export const updateKeyboardHoldState = (state, key, pressed) => ({
  ...state,
  [key]: pressed
})

export const resetKeyboardHoldState = () => ({ ctrl: false, space: false })

/**
 * 仅中键，或按住空格时的鼠标左键可启动画布平移。
 */
export const shouldStartCanvasPan = (button, spaceHeld, isPrimaryPointer, editableTarget) => {
  if (!isPrimaryPointer || editableTarget) return false
  return button === 1 || (button === 0 && spaceHeld)
}
