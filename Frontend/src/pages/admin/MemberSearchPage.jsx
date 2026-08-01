export function MemberSearchPage({ memberLoading, memberMessage, memberError, keyword, setKeyword, results, onSearch, onClear, onBack }) {
  return (
    <main className="notice-page account-page" aria-label="회원 검색 화면">
      <section className="notice-page-inner account-page-inner">
        <div className="notice-page-heading">
          <div><p className="section-kicker">Admin</p><h2>회원 검색</h2></div>
          <button className="back-button compact-button" type="button" onClick={onBack}>검색으로</button>
        </div>
        {memberMessage && <p className="account-message">{memberMessage}</p>}
        {memberError && <p className="account-message is-error">{memberError}</p>}
        <form className="account-form" onSubmit={(event) => { event.preventDefault(); onSearch() }}>
          <label><span>검색어</span><input value={keyword} type="search" placeholder="이메일, 이름, 전화번호" required onChange={(event) => setKeyword(event.target.value)} /></label>
          <div className="actions"><button className="primary-button" type="submit" disabled={memberLoading}>검색</button><button className="secondary-button" type="button" disabled={memberLoading} onClick={onClear}>초기화</button></div>
        </form>
        {results.length > 0 && (
          <dl className="detail-list member-search-results">
            {results.map((searchedMember) => <div key={searchedMember.memberId}><dt>{searchedMember.name}</dt><dd>{searchedMember.email} · {searchedMember.phone || '-'}</dd></div>)}
          </dl>
        )}
      </section>
    </main>
  )
}
