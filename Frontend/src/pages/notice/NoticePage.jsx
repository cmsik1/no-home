export function NoticePage({ notices, noticeLoading, noticeMessage, noticeError, isNoticeAdmin, noticeEditingId, noticeForm, setNoticeForm, saveNotice, resetNoticeForm, editNotice, deleteNotice, displayNoticeDate }) {
  return (
    <main className="notice-page" aria-label="공지사항 화면">
      <section className="notice-page-inner">
        <div className="notice-page-heading">
          <div><p className="section-kicker">Notice</p><h2>공지사항</h2></div>
          <span className="result-count">{notices.length.toLocaleString()}건</span>
        </div>
        {noticeMessage && <p className="account-message">{noticeMessage}</p>}
        {noticeError && <p className="account-message is-error">{noticeError}</p>}
        {isNoticeAdmin && (
          <form className="notice-form" onSubmit={(event) => { event.preventDefault(); saveNotice() }}>
            <label><span>제목</span><input value={noticeForm.title} type="text" maxLength="200" required onChange={(event) => setNoticeForm({ ...noticeForm, title: event.target.value.trim() })} /></label>
            <label><span>내용</span><textarea value={noticeForm.content} rows="5" required onChange={(event) => setNoticeForm({ ...noticeForm, content: event.target.value.trim() })}></textarea></label>
            <div className="actions"><button className="primary-button" type="submit" disabled={noticeLoading}>{noticeEditingId ? '수정' : '등록'}</button><button className="secondary-button" type="button" disabled={noticeLoading} onClick={resetNoticeForm}>취소</button></div>
          </form>
        )}
        {noticeLoading && notices.length === 0 ? (
          <div className="state-box loading-state"><strong>공지사항을 불러오는 중입니다.</strong></div>
        ) : notices.length === 0 ? (
          <div className="state-box"><strong>등록된 공지사항이 없습니다.</strong></div>
        ) : (
          <ul className="notice-list">
            {notices.map((notice) => (
              <li key={notice.noticeId}>
                <article className="notice-item">
                  <div className="notice-item-header"><strong>{notice.title}</strong><span>{displayNoticeDate(notice)}</span></div>
                  <p>{notice.content}</p>
                  {isNoticeAdmin && notice.editable && (
                    <div className="notice-actions">
                      <button className="secondary-button compact-button" type="button" disabled={noticeLoading} onClick={() => editNotice(notice)}>수정</button>
                      <button className="danger-button compact-button" type="button" disabled={noticeLoading} onClick={() => deleteNotice(notice)}>삭제</button>
                    </div>
                  )}
                </article>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  )
}
