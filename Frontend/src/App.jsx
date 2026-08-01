import { AppHeader } from './components/layout/AppHeader.jsx'
import { AppRoutes } from './components/layout/AppRoutes.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import { useAppController } from './hooks/useAppController'

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
