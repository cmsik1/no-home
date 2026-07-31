// Framework-free helpers for the AI chat widget.
// Extracted from ChatWidget.vue so the bug-prone parts (HTTP status mapping,
// Unicode-safe length) can be unit-tested with `node --test`.

export const MAX_MESSAGE_LENGTH = 500

// Progress messages shown while waiting for an answer.
// delayMs is relative to send time; the first stage (0) is shown immediately.
export const PROGRESS_STAGES = [
  { delayMs: 0, text: '질문을 전송했어요. 답변을 기다리고 있어요…' },
  { delayMs: 3000, text: '실거래가 데이터를 확인하고 있어요…' },
  { delayMs: 12000, text: '답변이 평소보다 오래 걸리고 있어요. 조금만 더 기다려주세요…' },
]

// 단기 대화기억용 세션 conversationId. sessionStorage에 보관하므로 탭/세션을 닫으면
// 사라지고(다음 세션은 새 id) 새 대화로 초기화된다 — 백엔드 InMemory 휘발 정책과 일치.
// 질문/에이전트 모드가 같은 id를 공유해 한 세션 안에서 맥락이 이어진다.
const CONVERSATION_ID_KEY = 'no-home.ai.conversation-id'

export function getConversationId(storage = globalThis.sessionStorage) {
  try {
    const existing = storage?.getItem(CONVERSATION_ID_KEY)
    if (existing) {
      return existing
    }
    const created =
      globalThis.crypto?.randomUUID?.() ??
      `s-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`
    storage?.setItem(CONVERSATION_ID_KEY, created)
    return created
  } catch {
    // sessionStorage 접근 불가(프라이빗 모드 등) → id 없이 진행(서버가 memberId로 fallback).
    return null
  }
}

// 챗 패널 드래그 리사이즈. 창 크기는 프라이버시 무관한 UI 선호이므로 대화기억(sessionStorage 휘발)과 달리
// localStorage에 영구 저장한다(브라우저를 닫았다 열어도 유지).
const PANEL_SIZE_KEY = 'no-home.ai.panel-size'
export const DEFAULT_PANEL_WIDTH = 340
export const DEFAULT_PANEL_HEIGHT = 480
const MIN_PANEL_WIDTH = 300
const MIN_PANEL_HEIGHT = 360

// 패널 크기를 [최소, 최대] 범위로 clamp한다(화면 밖으로 커지지 않게). 순수 함수.
// maxW/maxH는 호출부에서 window.innerWidth-40 등을 넘긴다(없으면 상한 없음).
export function clampPanelSize(width, height, maxW = Infinity, maxH = Infinity) {
  const w = Number.isFinite(width) ? width : DEFAULT_PANEL_WIDTH
  const h = Number.isFinite(height) ? height : DEFAULT_PANEL_HEIGHT
  return {
    width: Math.round(Math.max(MIN_PANEL_WIDTH, Math.min(w, Math.max(MIN_PANEL_WIDTH, maxW)))),
    height: Math.round(Math.max(MIN_PANEL_HEIGHT, Math.min(h, Math.max(MIN_PANEL_HEIGHT, maxH)))),
  }
}

// 저장된 패널 크기 복원(없거나 접근 불가 시 기본값). graceful.
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
    // 손상/접근 불가 → 기본값
  }
  return { width: DEFAULT_PANEL_WIDTH, height: DEFAULT_PANEL_HEIGHT }
}

export function savePanelSize(width, height, storage = globalThis.localStorage) {
  try {
    storage?.setItem(PANEL_SIZE_KEY, JSON.stringify({ width, height }))
  } catch {
    // 저장 실패 무시(프라이빗 모드 등)
  }
}

// Count by Unicode code points (not UTF-16 units), matching the backend's
// code-point based limit. Avoids the HTML `maxlength` (UTF-16) mismatch where
// emoji/surrogate pairs are counted as 2.
export function messageLength(text) {
  return Array.from(text ?? '').length
}

// Truncate to at most `max` code points without splitting a surrogate pair.
export function clampToMaxLength(text, max = MAX_MESSAGE_LENGTH) {
  const value = text ?? ''
  const chars = Array.from(value)
  return chars.length <= max ? value : chars.slice(0, max).join('')
}

