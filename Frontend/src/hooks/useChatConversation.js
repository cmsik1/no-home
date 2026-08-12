import { useEffect, useRef, useState } from 'react'
import { capabilities } from '../houseSearchParams'
import { parseAssistantResponse } from '../services/agentClient'
import { PROGRESS_STAGES, clampToMaxLength, getConversationId, messageLength } from '../utils/chatPanel'

const WELCOME_MESSAGE = '안녕하세요. 서울 아파트 실거래가를 자연어로 검색해 보세요. 예: "강남구 2024년 5월 매매 검색"'

/**
 * 채팅 메시지와 진행 표시를 관리하고 현재 검색 문맥을 AI assistant API에 전달한다.
 * 답변은 즉시 메시지로 표시하고, 명령 응답은 useAgentCommands로 넘겨 실제 화면 동작을 수행한다.
 */
export function useChatConversation({ loggedIn, currentFilters, currentPage, totalPages, agentResult, onAgentCommand }) {
  const [input, setInputState] = useState('')
  const [loading, setLoading] = useState(false)
  const [loadingStatus, setLoadingStatus] = useState('')
  const [messages, setMessages] = useState([{ role: 'assistant', text: WELCOME_MESSAGE }])
  const progressTimersRef = useRef([])
  const scrollRef = useRef(null)
  const lastAgentSeqRef = useRef(null)

  function scrollToBottom() {
    window.requestAnimationFrame(() => {
      if (scrollRef.current) scrollRef.current.scrollTop = scrollRef.current.scrollHeight
    })
  }
  function clearProgress() {
    progressTimersRef.current.forEach(clearTimeout)
    progressTimersRef.current = []
    setLoadingStatus('')
  }
  function startProgress() {
    clearProgress()
    setLoadingStatus(PROGRESS_STAGES[0].text)
    progressTimersRef.current = PROGRESS_STAGES.slice(1).map((stage) => setTimeout(() => {
      setLoadingStatus(stage.text)
      scrollToBottom()
    }, stage.delayMs))
  }
  function appendAssistantMessage(text) {
    setMessages((previous) => [...previous, { role: 'assistant', text }])
    scrollToBottom()
  }

  useEffect(() => () => {
    progressTimersRef.current.forEach(clearTimeout)
  }, [])
  useEffect(() => {
    if (!agentResult || agentResult.seq === lastAgentSeqRef.current) return
    lastAgentSeqRef.current = agentResult.seq
    clearProgress()
    setLoading(false)
    if (agentResult.text) appendAssistantMessage(agentResult.text)
  }, [agentResult])

  async function requestAssistant(message) {
    const response = await fetch('/api/ai/assistant', {
      method: 'POST',
      headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ message, conversationId: getConversationId(), capabilities: capabilities(), currentFilters, currentPage, totalPages }),
    })
    const body = await response.json().catch(() => null)
    return parseAssistantResponse({ status: response.status, ok: response.ok, body, retryAfter: response.headers.get('Retry-After') })
  }

  async function send() {
    const message = input.trim()
    if (!message || loading) return
    if (!loggedIn) {
      appendAssistantMessage('로그인 후 사용할 수 있습니다.')
      return
    }
    setMessages((previous) => [...previous, { role: 'user', text: message }])
    setInputState('')
    setLoading(true)
    startProgress()
    scrollToBottom()
    try {
      const result = await requestAssistant(message)
      if (result.kind === 'command') {
        clearProgress()
        setLoadingStatus('요청을 처리하고 있습니다.')
        onAgentCommand?.(result.command)
        return
      }
      clearProgress()
      setLoading(false)
      appendAssistantMessage(result.text)
    } catch (error) {
      clearProgress()
      setLoading(false)
      appendAssistantMessage(error instanceof Error ? error.message : '오류가 발생했습니다.')
    }
  }

  return {
    input,
    setInput: (value) => setInputState(clampToMaxLength(value)),
    inputLength: messageLength(input),
    loading,
    loadingStatus,
    messages,
    scrollRef,
    send,
  }
}
