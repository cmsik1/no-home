import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { LoginForm } from './LoginForm.jsx'

describe('LoginForm', () => {
  it('updates credentials and submits through the account hook contract', async () => {
    const user = userEvent.setup()
    const loginMember = vi.fn()
    let form = { email: '', password: '' }
    const setForm = vi.fn((next) => {
      form = next
      rerender(<LoginForm form={form} setForm={setForm} memberLoading={false} loginMember={loginMember} setAccountMode={vi.fn()} />)
    })
    const { rerender } = render(
      <LoginForm form={form} setForm={setForm} memberLoading={false} loginMember={loginMember} setAccountMode={vi.fn()} />,
    )

    await user.type(screen.getByLabelText('Email'), 'user@example.com')
    await user.type(screen.getByLabelText('Password'), 'secret')
    await user.click(screen.getByRole('button', { name: 'Login' }))

    expect(form).toEqual({ email: 'user@example.com', password: 'secret' })
    expect(loginMember).toHaveBeenCalledOnce()
  })

  it('moves to signup mode and disables submit while loading', async () => {
    const user = userEvent.setup()
    const setAccountMode = vi.fn()
    render(
      <LoginForm
        form={{ email: '', password: '' }}
        setForm={vi.fn()}
        memberLoading
        loginMember={vi.fn()}
        setAccountMode={setAccountMode}
      />,
    )

    expect(screen.getByRole('button', { name: 'Login' })).toBeDisabled()
    await user.click(screen.getByRole('button', { name: 'Create Account' }))
    expect(setAccountMode).toHaveBeenCalledWith('signup')
  })
})
