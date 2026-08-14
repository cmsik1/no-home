import { AccountTabs } from './components/AccountTabs.jsx'
import { LoginForm } from './components/LoginForm.jsx'
import { PasswordResetForm } from './components/PasswordResetForm.jsx'
import { ProfilePanel } from './components/ProfilePanel.jsx'
import { SignupForm } from './components/SignupForm.jsx'
import { PASSWORD_RESET_ENABLED } from '../../config/appConfig'

/** 현재 회원 유무와 accountMode에 따라 가입·로그인·재설정·프로필 흐름 중 하나를 표시한다. */
export function AccountPage(props) {
  const {
    member, accountMode, setAccountMode, memberLoading, memberMessage, memberError,
    loginForm, setLoginForm, signupForm, setSignupForm, passwordResetForm, setPasswordResetForm,
    profileForm, setProfileForm, profileEditing, setProfileEditing, deleteConfirm, setDeleteConfirm,
    onClose, loginMember, signupMember, resetPassword, logoutMember, updateMember, deleteMember,
  } = props
  const visibleAccountMode = accountMode === 'password-reset' && !PASSWORD_RESET_ENABLED ? 'login' : accountMode

  return (
    <main className="notice-page account-page" aria-label="Account page">
      <section className="notice-page-inner account-page-inner">
        <div className="notice-page-heading">
          <div><p className="section-kicker">Account</p><h2>Member Account</h2></div>
          <button className="back-button compact-button" type="button" onClick={onClose}>Back to Search</button>
        </div>

        {!member && <AccountTabs accountMode={visibleAccountMode} setAccountMode={setAccountMode} passwordResetEnabled={PASSWORD_RESET_ENABLED} />}
        {memberMessage && <p className="account-message">{memberMessage}</p>}
        {memberError && <p className="account-message is-error">{memberError}</p>}

        {visibleAccountMode === 'login' && !member && (
          <LoginForm
            form={loginForm}
            setForm={setLoginForm}
            memberLoading={memberLoading}
            loginMember={loginMember}
            setAccountMode={setAccountMode}
          />
        )}

        {visibleAccountMode === 'password-reset' && PASSWORD_RESET_ENABLED && !member && (
          <PasswordResetForm
            form={passwordResetForm}
            setForm={setPasswordResetForm}
            memberLoading={memberLoading}
            resetPassword={resetPassword}
            setAccountMode={setAccountMode}
          />
        )}

        {visibleAccountMode === 'signup' && !member && (
          <SignupForm
            form={signupForm}
            setForm={setSignupForm}
            memberLoading={memberLoading}
            signupMember={signupMember}
            setAccountMode={setAccountMode}
          />
        )}

        {member && (
          <ProfilePanel
            member={member}
            memberLoading={memberLoading}
            profileForm={profileForm}
            setProfileForm={setProfileForm}
            profileEditing={profileEditing}
            setProfileEditing={setProfileEditing}
            logoutMember={logoutMember}
            updateMember={updateMember}
            deleteConfirm={deleteConfirm}
            setDeleteConfirm={setDeleteConfirm}
            deleteMember={deleteMember}
          />
        )}
      </section>
    </main>
  )
}
