export interface KeyboardHoldState {
  ctrl: boolean
  space: boolean
}

export type TrackedKeyboardKey = keyof KeyboardHoldState

interface KeyboardEventLike {
  code?: string
  key?: string
  keyCode?: number
}

interface EditableTargetLike {
  tagName?: string
  isContentEditable?: boolean
  closest?: (selector: string) => unknown
}

export function getTrackedKeyboardKey(event: KeyboardEventLike): TrackedKeyboardKey | undefined

export function isEditableKeyboardTarget(
  target: EventTarget | EditableTargetLike | null
): boolean

export function updateKeyboardHoldState(
  state: KeyboardHoldState,
  key: TrackedKeyboardKey,
  pressed: boolean
): KeyboardHoldState

export function resetKeyboardHoldState(): KeyboardHoldState

export function shouldStartCanvasPan(
  button: number,
  spaceHeld: boolean,
  isPrimaryPointer: boolean,
  editableTarget: boolean
): boolean
