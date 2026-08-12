import { useEffect, useRef, useState } from 'react'
import { useChatConversation } from '../../hooks/useChatConversation'
import { useResizableChatPanel } from '../../hooks/useResizableChatPanel'
import { ChatPanel } from './ChatPanel.jsx'

/** 대화 상태와 패널 크기 상태를 결합하고, 열린 동안에만 AI 패널을 화면에 유지한다. */
export default function ChatWidget({ loggedIn = false, currentFilters = {}, currentPage = 1, totalPages = 1, agentResult = null, onAgentCommand }) {
  const [open, setOpen] = useState(false)
  const inputRef = useRef(null)
  const conversation = useChatConversation({ loggedIn, currentFilters, currentPage, totalPages, agentResult, onAgentCommand })
  const panel = useResizableChatPanel()

  useEffect(() => {
    if (open) window.requestAnimationFrame(() => inputRef.current?.focus())
  }, [open])

  return (
    <aside className={`chat-widget${open ? ' is-open' : ''}`} aria-label="AI 도우미">
      {open && <ChatPanel
        loggedIn={loggedIn}
        panelStyle={panel.panelStyle}
        messages={conversation.messages}
        loading={conversation.loading}
        loadingStatus={conversation.loadingStatus}
        input={conversation.input}
        inputLength={conversation.inputLength}
        inputRef={inputRef}
        scrollRef={conversation.scrollRef}
        onClose={() => setOpen(false)}
        onInputChange={conversation.setInput}
        onSend={conversation.send}
        onStartResize={panel.startResize}
      />}
      <button className="chat-toggle" type="button" aria-expanded={open} onClick={() => setOpen((value) => !value)}>AI</button>
    </aside>
  )
}
