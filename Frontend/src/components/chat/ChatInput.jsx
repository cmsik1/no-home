import { MAX_MESSAGE_LENGTH } from '../../utils/chatPanel'

export function ChatInput({ inputRef, input, inputLength, loading, onInputChange, onSend }) {
  return (
    <footer className="chat-input-row">
      <textarea
        ref={inputRef}
        value={input}
        maxLength={MAX_MESSAGE_LENGTH * 2}
        placeholder="예: 강남구 2024년 5월 매매 검색"
        onChange={(event) => onInputChange(event.target.value)}
        onKeyDown={(event) => {
          if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault()
            onSend()
          }
        }}
      />
      <div className="chat-input-meta">
        <span>{inputLength}/{MAX_MESSAGE_LENGTH}</span>
        <button className="primary-button compact-button" type="button" disabled={loading || !input.trim()} onClick={onSend}>전송</button>
      </div>
    </footer>
  )
}
