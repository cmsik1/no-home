export function ChatMessages({ messages, loading, loadingStatus, scrollRef }) {
  return (
    <div ref={scrollRef} className="chat-messages" aria-live="polite">
      {messages.map((message, index) => (
        <div key={`${message.role}-${index}`} className={`chat-message is-${message.role}`}>
          {message.text}
        </div>
      ))}
      {loading && loadingStatus && (
        <div className="chat-message is-assistant is-loading">
          {loadingStatus}
          <span className="loading-dots" aria-hidden="true"><i></i><i></i><i></i></span>
        </div>
      )}
    </div>
  )
}
