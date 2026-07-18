import assert from 'node:assert/strict'
import test from 'node:test'

import {
  getTrackedKeyboardKey,
  isEditableKeyboardTarget,
  resetKeyboardHoldState,
  shouldStartCanvasPan,
  updateKeyboardHoldState
} from '../src/utils/keyboard.mjs'

test('输入控件和可编辑区域保留空格输入语义', () => {
  assert.equal(isEditableKeyboardTarget({ tagName: 'INPUT' }), true)
  assert.equal(isEditableKeyboardTarget({ tagName: 'textarea' }), true)
  assert.equal(isEditableKeyboardTarget({ isContentEditable: true }), true)
  assert.equal(isEditableKeyboardTarget({ closest: () => ({}) }), true)
  assert.equal(isEditableKeyboardTarget({ tagName: 'DIV', closest: () => null }), false)
})

test('Ctrl 后按 Space，抬起 Ctrl 不会错误结束空格平移状态', () => {
  let state = resetKeyboardHoldState()
  state = updateKeyboardHoldState(state, 'ctrl', true)
  state = updateKeyboardHoldState(state, 'space', true)
  state = updateKeyboardHoldState(state, 'ctrl', false)

  assert.deepEqual(state, { ctrl: false, space: true })
  assert.deepEqual(updateKeyboardHoldState(state, 'space', false), resetKeyboardHoldState())
})

test('Space 后按 Ctrl，抬起 Space 仅结束空格平移状态', () => {
  let state = resetKeyboardHoldState()
  state = updateKeyboardHoldState(state, 'space', true)
  state = updateKeyboardHoldState(state, 'ctrl', true)
  state = updateKeyboardHoldState(state, 'space', false)

  assert.deepEqual(state, { ctrl: true, space: false })
  assert.deepEqual(resetKeyboardHoldState(), { ctrl: false, space: false })
})

test('窗口失去焦点时同时释放 Ctrl 和 Space', () => {
  let state = { ctrl: true, space: true }
  state = resetKeyboardHoldState()

  assert.deepEqual(state, { ctrl: false, space: false })
})

test('按键识别同时兼容 code、key 和旧 keyCode', () => {
  assert.equal(getTrackedKeyboardKey({ code: 'Space' }), 'space')
  assert.equal(getTrackedKeyboardKey({ key: ' ' }), 'space')
  assert.equal(getTrackedKeyboardKey({ keyCode: 32 }), 'space')
  assert.equal(getTrackedKeyboardKey({ code: 'ControlLeft' }), 'ctrl')
  assert.equal(getTrackedKeyboardKey({ code: 'MetaLeft' }), 'ctrl')
  assert.equal(getTrackedKeyboardKey({ keyCode: 65 }), undefined)
})

test('画布平移仅接受中键或 Space+左键，并过滤非主指针与编辑区', () => {
  assert.equal(shouldStartCanvasPan(1, false, true, false), true)
  assert.equal(shouldStartCanvasPan(0, true, true, false), true)
  assert.equal(shouldStartCanvasPan(0, false, true, false), false)
  assert.equal(shouldStartCanvasPan(2, true, true, false), false)
  assert.equal(shouldStartCanvasPan(1, false, false, false), false)
  assert.equal(shouldStartCanvasPan(1, false, true, true), false)
})
