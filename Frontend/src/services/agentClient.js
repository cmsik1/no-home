export { MAX_MESSAGE_LENGTH } from '../utils/chatPanel.js'

function retryHint(retryAfter) {
  const seconds = Number.parseInt(retryAfter ?? '', 10)
  return Number.isFinite(seconds) && seconds > 0 ? ` ${seconds}초 후 다시 시도해 주세요.` : ''
}

export function parseAssistantResponse({ status, ok, body, retryAfter } = {}) {
  if (status === 401) return { kind: 'error', text: '로그인이 필요합니다.' }
  if (status === 429) {
    return { kind: 'error', text: `${body?.message || '요청이 너무 많습니다.'}${retryHint(retryAfter)}` }
  }
  if (!ok || body?.success === false) {
    return { kind: 'error', text: body?.message || `요청에 실패했습니다. (${status})` }
  }
  const data = body?.data
  if (!data || typeof data !== 'object') return { kind: 'error', text: '응답을 해석하지 못했습니다.' }
  if (data.type === 'command') {
    if (!data.command || typeof data.command !== 'object') {
      return { kind: 'error', text: '명령을 해석하지 못했습니다.' }
    }
    return { kind: 'command', command: data.command, notice: data.notice ?? null }
  }
  return { kind: 'answer', text: data.answer || '응답을 받지 못했습니다.' }
}
