import { test } from 'node:test'
import assert from 'node:assert/strict'
import {
  MAX_MESSAGE_LENGTH,
  PROGRESS_STAGES,
  messageLength,
  clampToMaxLength,
  getConversationId,
  clampPanelSize,
  loadPanelSize,
  savePanelSize,
  DEFAULT_PANEL_WIDTH,
  DEFAULT_PANEL_HEIGHT,
} from './chatClient.js'

function fakeStorage() {
  const map = new Map()
  return {
    getItem: (k) => (map.has(k) ? map.get(k) : null),
    setItem: (k, v) => map.set(k, String(v)),
  }
}

test('getConversationId returns a stable id within one storage (session)', () => {
  const storage = fakeStorage()
  const first = getConversationId(storage)
  const second = getConversationId(storage)
  assert.ok(first, 'an id is produced')
  assert.equal(first, second, 'same session returns the same id')
})

test('getConversationId yields a fresh id for a new storage (closed/reopened session)', () => {
  const a = getConversationId(fakeStorage())
  const b = getConversationId(fakeStorage())
  assert.notEqual(a, b, 'a brand new session gets a different id')
})

test('getConversationId tolerates unavailable storage (private mode)', () => {
  const throwing = {
    getItem: () => {
      throw new Error('blocked')
    },
    setItem: () => {
      throw new Error('blocked')
    },
  }
  assert.equal(getConversationId(throwing), null)
})

test('messageLength counts Unicode code points, not UTF-16 units', () => {
  assert.equal(messageLength('hello'), 5)
  assert.equal(messageLength('안녕'), 2)
  assert.equal(messageLength('😀'), 1) // surrogate pair -> 1
  assert.equal(messageLength('a😀b'), 3)
  assert.equal(messageLength(''), 0)
  assert.equal(messageLength(null), 0)
})

test('clampToMaxLength truncates by code point without splitting surrogate pairs', () => {
  assert.equal(clampToMaxLength('abc', 5), 'abc')
  assert.equal(clampToMaxLength('abcdef', 3), 'abc')
  // 3 emojis, limit 2 -> exactly 2 emojis, no broken surrogate
  assert.equal(clampToMaxLength('😀😀😀', 2), '😀😀')
  assert.equal(messageLength(clampToMaxLength('😀😀😀', 2)), 2)
  assert.equal(clampToMaxLength(null, 5), '')
})

test('PROGRESS_STAGES has an immediate first stage and later delays', () => {
  assert.equal(PROGRESS_STAGES[0].delayMs, 0)
  assert.ok(PROGRESS_STAGES.length >= 2)
  assert.ok(PROGRESS_STAGES.slice(1).every((s) => s.delayMs > 0))
})

test('MAX_MESSAGE_LENGTH is 500', () => {
  assert.equal(MAX_MESSAGE_LENGTH, 500)
})

test('clampPanelSize keeps a size within min/max bounds', () => {
  assert.deepEqual(clampPanelSize(400, 500, 2000, 2000), { width: 400, height: 500 })
})

test('clampPanelSize enforces the minimum size', () => {
  assert.deepEqual(clampPanelSize(100, 100, 2000, 2000), { width: 300, height: 360 })
})

test('clampPanelSize enforces the maximum (viewport) size', () => {
  assert.deepEqual(clampPanelSize(5000, 5000, 800, 600), { width: 800, height: 600 })
})

test('clampPanelSize falls back to defaults for non-finite input', () => {
  assert.deepEqual(clampPanelSize(NaN, undefined), {
    width: DEFAULT_PANEL_WIDTH,
    height: DEFAULT_PANEL_HEIGHT,
  })
})

test('loadPanelSize restores a saved size and falls back to defaults', () => {
  const storage = fakeStorage()
  assert.deepEqual(loadPanelSize(storage), { width: DEFAULT_PANEL_WIDTH, height: DEFAULT_PANEL_HEIGHT })
  storage.setItem('no-home.ai.panel-size', JSON.stringify({ width: 420, height: 600 }))
  assert.deepEqual(loadPanelSize(storage), { width: 420, height: 600 })
})

test('loadPanelSize ignores corrupt data and clamps restored size', () => {
  const storage = fakeStorage()
  storage.setItem('no-home.ai.panel-size', '{ not json')
  assert.deepEqual(loadPanelSize(storage), { width: DEFAULT_PANEL_WIDTH, height: DEFAULT_PANEL_HEIGHT })
  storage.setItem('no-home.ai.panel-size', JSON.stringify({ width: 50, height: 50 }))
  assert.deepEqual(loadPanelSize(storage), { width: 300, height: 360 })
})

test('savePanelSize writes the size and is tolerant of unavailable storage', () => {
  const storage = fakeStorage()
  savePanelSize(420, 600, storage)
  assert.deepEqual(JSON.parse(storage.getItem('no-home.ai.panel-size')), { width: 420, height: 600 })
  const throwing = { setItem: () => { throw new Error('blocked') } }
  assert.doesNotThrow(() => savePanelSize(420, 600, throwing))
})
