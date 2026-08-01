import { ChatInput } from './ChatInput.jsx'
import { ChatMessages } from './ChatMessages.jsx'

export function ChatPanel({ loggedIn, panelStyle, messages, loading, loadingStatus, input, inputLength, inputRef, scrollRef, onClose, onInputChange, onSend, onStartResize }) {
  return (
    <section className="chat-panel" style={panelStyle}>
      <button className="chat-resize-handle" type="button" aria-label="채팅창 크기 조절" onMouseDown={onStartResize} onTouchStart={onStartResize} />
      <header className="chat-header">
        <div>
          <strong>AI 도우미</strong>
          <span>{loggedIn ? '검색 조건 변경과 페이지 이동을 도와드려요.' : '로그인 후 사용할 수 있어요.'}</span>
        </div>
        <button type="button" className="chat-close" aria-label="닫기" onClick={onClose}>x</button>
      </header>
      <ChatMessages messages={messages} loading={loading} loadingStatus={loadingStatus} scrollRef={scrollRef} />
      <ChatInput inputRef={inputRef} input={input} inputLength={inputLength} loading={loading} onInputChange={onInputChange} onSend={onSend} />
    </section>
  )
}
