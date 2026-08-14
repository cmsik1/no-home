import { useAuthContext } from '../../context/AuthContext.jsx'

export function AppHeader({ activePage, onSearch, onNotice, onMemberSearch, onAccount, onLogout }) {
  const { accountSummary, canSearchMembers, member, memberLoading } = useAuthContext()

  return (
    <header className="top-bar">
      <div className="brand">
        <button className="brand-mark" type="button" aria-label="검색 화면" onClick={onSearch}>
          <img src="/nohome-logo.png" alt="" />
        </button>
        <div>
          <p className="app-kicker">SSAFY Home</p>
          <h1>NoHome 실거래가 검색</h1>
        </div>
      </div>
      <nav className="top-nav" aria-label="주요 화면">
        <button className={`nav-tab icon-tab${activePage === 'notice' ? ' is-active' : ''}`} type="button" aria-label="공지사항" title="공지사항" onClick={onNotice}>공지</button>
        {canSearchMembers && <button className={`nav-tab${activePage === 'member-search' ? ' is-active' : ''}`} type="button" onClick={onMemberSearch}>회원 검색</button>}
      </nav>
      <div className="account-actions" aria-label="회원 메뉴">
        <span className="account-summary">{accountSummary}</span>
        {member ? (
          <>
            <button className="account-button" type="button" onClick={() => onAccount('profile')}>내 정보</button>
            <button className="secondary-button compact-button" type="button" disabled={memberLoading} onClick={onLogout}>로그아웃</button>
          </>
        ) : (
          <button className="account-button" type="button" onClick={() => onAccount('login')}>로그인</button>
        )}
      </div>
    </header>
  )
}
