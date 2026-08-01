import { AccountTabs } from './components/AccountTabs.jsx'
import { LoginForm } from './components/LoginForm.jsx'
import { PasswordResetForm } from './components/PasswordResetForm.jsx'
import { ProfilePanel } from './components/ProfilePanel.jsx'
import { SignupForm } from './components/SignupForm.jsx'

export function AccountPage(props) {
  const {
    member, accountMode, setAccountMode, memberLoading, memberMessage, memberError,
    loginForm, setLoginForm, signupForm, setSignupForm, passwordResetForm, setPasswordResetForm,
    profileForm, setProfileForm, profileEditing, setProfileEditing, deleteConfirm, setDeleteConfirm,
    onClose, loginMember, signupMember, resetPassword, logoutMember, updateMember, deleteMember,
  } = props

  return (
    <main className="notice-page account-page" aria-label="Account page">
      <section className="notice-page-inner account-page-inner">
        <div className="notice-page-heading">
          <div><p className="section-kicker">Account</p><h2>Member Account</h2></div>
          <button className="back-button compact-button" type="button" onClick={onClose}>Back to Search</button>
        </div>

        {!member && <AccountTabs accountMode={accountMode} setAccountMode={setAccountMode} />}
        {memberMessage && <p className="account-message">{memberMessage}</p>}
        {memberError && <p className="account-message is-error">{memberError}</p>}

        {accountMode === 'login' && !member && (
          <LoginForm
            form={loginForm}
            setForm={setLoginForm}
            memberLoading={memberLoading}
            loginMember={loginMember}
            setAccountMode={setAccountMode}
          />
        )}

        {accountMode === 'password-reset' && !member && (
          <PasswordResetForm
            form={passwordResetForm}
            setForm={setPasswordResetForm}
            memberLoading={memberLoading}
            resetPassword={resetPassword}
            setAccountMode={setAccountMode}
          />
        )}

        {accountMode === 'signup' && !member && (
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
