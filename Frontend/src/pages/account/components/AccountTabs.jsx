const ACCOUNT_MODES = [
  ['login', 'Login'],
  ['signup', 'Sign Up'],
  ['password-reset', 'Reset Password'],
]

export function AccountTabs({ accountMode, setAccountMode }) {
  return (
    <div className="account-tabs" role="tablist" aria-label="Account mode">
      {ACCOUNT_MODES.map(([mode, label]) => (
        <button
          key={mode}
          className={`secondary-button compact-button${accountMode === mode ? ' is-active' : ''}`}
          type="button"
          onClick={() => setAccountMode(mode)}
        >
          {label}
        </button>
      ))}
    </div>
  )
}
