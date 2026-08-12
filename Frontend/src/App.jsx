import { AppHeader } from './components/layout/AppHeader.jsx'
import { AppRoutes } from './components/layout/AppRoutes.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import { useAppController } from './hooks/useAppController'

/**
 * 애플리케이션의 최상위 조립점이다. useAppController가 만든 도메인별 상태와 동작을
 * 인증 Context, 공통 헤더와 현재 페이지에 전달하며 직접 비즈니스 로직은 수행하지 않는다.
 */
export default function App() {
  const controller = useAppController()

  return (
    <AuthProvider value={{
      member: controller.memberAccount.member,
      memberLoading: controller.memberAccount.memberLoading,
      isNoticeAdmin: controller.isNoticeAdmin,
      accountSummary: controller.accountSummary,
    }}>
      <div className="app-shell">
        <AppHeader
          activePage={controller.activePage}
          onSearch={() => controller.setActivePage('search')}
          onNotice={controller.openNoticePage}
          onMemberSearch={controller.openMemberSearchPage}
          onAccount={controller.openAccountPanel}
          onLogout={controller.logoutMember}
        />
        <AppRoutes controller={controller} />
      </div>
    </AuthProvider>
  )
}
