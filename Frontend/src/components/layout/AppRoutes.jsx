import ChatWidget from '../chat/ChatWidget.jsx'
import { AccountPage, MemberSearchPage, NoticePage, SearchPage } from '../../pages'
import {
  buildAccountPageProps,
  buildMemberSearchPageProps,
  buildNoticePageProps,
  buildSearchPageProps,
} from './routeProps'

/** 현재 페이지 상태에 맞는 화면을 선택하고 검색 화면에서만 AI 대화 문맥을 연결한다. */
export function AppRoutes({ controller }) {
  const { activePage, search, memberAccount, agent, canSearchMembers } = controller

  return (
    <>
      {activePage === 'search' && <SearchPage {...buildSearchPageProps(controller)} />}
      {activePage === 'account' && <AccountPage {...buildAccountPageProps(controller)} />}
      {activePage === 'member-search' && canSearchMembers && <MemberSearchPage {...buildMemberSearchPageProps(controller)} />}
      {activePage === 'notice' && <NoticePage {...buildNoticePageProps(controller)} />}

      {activePage === 'search' && (
        <ChatWidget
          loggedIn={Boolean(memberAccount.member)}
          currentFilters={search.filters}
          currentPage={search.searchPage}
          totalPages={search.totalPages}
          agentResult={agent.agentResult}
          onAgentCommand={agent.handleAgentCommand}
        />
      )}
    </>
  )
}
