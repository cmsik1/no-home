// Framework-free helpers for the AI agent (실행) mode.
// Mirrors chatClient.js so the HTTP status mapping can be unit-tested with `node --test`.
// 질문 모드(chatClient)는 텍스트 답변을 받지만, 에이전트 모드는 구조화 명령(AgentCommand)을 받는다.

export { MAX_MESSAGE_LENGTH } from './chatClient.js'

function retryHint(retryAfter) {
  const seconds = Number.parseInt(retryAfter ?? '', 10)
  return Number.isFinite(seconds) && seconds > 0
    ? ` ${seconds}초 후 다시 시도할 수 있어요.`
    : ''
}

// Map a unified /api/ai/assistant response into a chat message.
// The backend returns AssistantResponse { type:'answer'|'command', answer, command, notice }.
// Returns { kind:'answer', text } | { kind:'command', command, notice } | { kind:'error', text }
// so ChatWidget can branch the same way for both a text answer and a page-action command.
export function parseAssistantResponse({ status, ok, body, retryAfter } = {}) {
  if (status === 401) {
    return { kind: 'error', text: '로그인이 필요합니다.' }
  }
  if (status === 429) {
    const base = body?.message || '요청이 너무 많습니다.'
    return { kind: 'error', text: `${base}${retryHint(retryAfter)}` }
  }
  if (!ok || body?.success === false) {
    return { kind: 'error', text: body?.message || `요청 실패 (${status})` }
  }
  const data = body?.data
  if (!data || typeof data !== 'object') {
    return { kind: 'error', text: '응답을 해석하지 못했습니다.' }
  }
  if (data.type === 'command') {
    const command = data.command
    if (!command || typeof command !== 'object') {
      return { kind: 'error', text: '명령을 해석하지 못했습니다.' }
    }
    return { kind: 'command', command, notice: data.notice ?? null }
  }
  // 기본은 텍스트 답변. answer가 비면 안내 문구로 대체.
  return { kind: 'answer', text: data.answer || '응답을 받지 못했습니다.' }
}
