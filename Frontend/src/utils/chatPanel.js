export const MAX_MESSAGE_LENGTH = 500

export const PROGRESS_STAGES = [
  { delayMs: 0, text: '질문을 전송했어요. 답변을 기다리고 있어요.' },
  { delayMs: 3000, text: '실거래가 데이터를 확인하고 있어요.' },
  { delayMs: 12000, text: '답변이 평소보다 오래 걸리고 있어요. 조금만 더 기다려 주세요.' },
]

const CONVERSATION_ID_KEY = 'no-home.ai.conversation-id'
const PANEL_SIZE_KEY = 'no-home.ai.panel-size'
export const DEFAULT_PANEL_WIDTH = 340
export const DEFAULT_PANEL_HEIGHT = 480
const MIN_PANEL_WIDTH = 300
const MIN_PANEL_HEIGHT = 360

export function getConversationId(storage = globalThis.sessionStorage) {
  try {
    const existing = storage?.getItem(CONVERSATION_ID_KEY)
    if (existing) return existing
    const created = globalThis.crypto?.randomUUID?.()
      ?? `s-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`
    storage?.setItem(CONVERSATION_ID_KEY, created)
    return created
  } catch {
    return null
  }
}

export function clampPanelSize(width, height, maxWidth = Infinity, maxHeight = Infinity) {
  const safeWidth = Number.isFinite(width) ? width : DEFAULT_PANEL_WIDTH
  const safeHeight = Number.isFinite(height) ? height : DEFAULT_PANEL_HEIGHT
  return {
    width: Math.round(Math.max(MIN_PANEL_WIDTH, Math.min(safeWidth, Math.max(MIN_PANEL_WIDTH, maxWidth)))),
    height: Math.round(Math.max(MIN_PANEL_HEIGHT, Math.min(safeHeight, Math.max(MIN_PANEL_HEIGHT, maxHeight)))),
  }
}

export function loadPanelSize(storage = globalThis.localStorage) {
  try {
    const raw = storage?.getItem(PANEL_SIZE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (parsed && Number.isFinite(parsed.width) && Number.isFinite(parsed.height)) {
        return clampPanelSize(parsed.width, parsed.height)
      }
    }
  } catch {
    // Storage is optional; private browsing can deny access.
  }
  return { width: DEFAULT_PANEL_WIDTH, height: DEFAULT_PANEL_HEIGHT }
}

export function savePanelSize(width, height, storage = globalThis.localStorage) {
  try {
    storage?.setItem(PANEL_SIZE_KEY, JSON.stringify({ width, height }))
  } catch {
    // Resizing remains usable even when persistence is unavailable.
  }
}

export function messageLength(text) {
  return Array.from(text ?? '').length
}

export function clampToMaxLength(text, max = MAX_MESSAGE_LENGTH) {
  const chars = Array.from(text ?? '')
  return chars.length <= max ? chars.join('') : chars.slice(0, max).join('')
}
