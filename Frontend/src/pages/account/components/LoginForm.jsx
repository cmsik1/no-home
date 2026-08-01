export function LoginForm({ form, setForm, memberLoading, loginMember, setAccountMode }) {
  return (
    <form className="account-form" onSubmit={(event) => { event.preventDefault(); loginMember() }}>
      <label><span>Email</span><input value={form.email} type="email" autoComplete="email" required onChange={(event) => setForm({ ...form, email: event.target.value.trim() })} /></label>
      <label><span>Password</span><input value={form.password} type="password" autoComplete="current-password" required onChange={(event) => setForm({ ...form, password: event.target.value })} /></label>
      <div className="actions"><button className="primary-button" type="submit" disabled={memberLoading}>Login</button><button className="secondary-button" type="button" onClick={() => setAccountMode('signup')}>Create Account</button></div>
    </form>
  )
}
