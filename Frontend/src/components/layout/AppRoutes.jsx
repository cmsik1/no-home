import ChatWidget from '../chat/ChatWidget.jsx'
import { AccountPage, MemberSearchPage, NoticePage, SearchPage } from '../../pages'
import {
  buildAccountPageProps,
  buildMemberSearchPageProps,
  buildNoticePageProps,
  buildSearchPageProps,
} from './routeProps'

export function AppRoutes({ controller }) {
  const { activePage, search, memberAccount, agent, isNoticeAdmin } = controller

  return (
    <>
      {activePage === 'search' && <SearchPage {...buildSearchPageProps(controller)} />}
      {activePage === 'account' && <AccountPage {...buildAccountPageProps(controller)} />}
      {activePage === 'member-search' && isNoticeAdmin && <MemberSearchPage {...buildMemberSearchPageProps(controller)} />}
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
