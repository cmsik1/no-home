export function SignupForm({ form, setForm, memberLoading, signupMember, setAccountMode }) {
  return (
    <form className="account-form" onSubmit={(event) => { event.preventDefault(); signupMember() }}>
      <label><span>Email</span><input value={form.email} type="email" autoComplete="email" required onChange={(event) => setForm({ ...form, email: event.target.value.trim() })} /></label>
      <label><span>Password</span><input value={form.password} type="password" autoComplete="new-password" required onChange={(event) => setForm({ ...form, password: event.target.value })} /></label>
      <label><span>Name</span><input value={form.name} type="text" autoComplete="name" required onChange={(event) => setForm({ ...form, name: event.target.value.trim() })} /></label>
      <label><span>Phone</span><input value={form.phone} type="tel" onChange={(event) => setForm({ ...form, phone: event.target.value.trim() })} /></label>
      <div className="actions"><button className="primary-button" type="submit" disabled={memberLoading}>Sign Up</button><button className="secondary-button" type="button" onClick={() => setAccountMode('login')}>Back to Login</button></div>
    </form>
  )
}
