import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { AccountTabs } from './AccountTabs.jsx'

describe('AccountTabs deployment policy', () => {
  it('hides password reset when the public deployment disables it', () => {
    render(<AccountTabs accountMode="login" setAccountMode={vi.fn()} passwordResetEnabled={false} />)

    expect(screen.getByRole('button', { name: 'Login' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Reset Password' })).not.toBeInTheDocument()
  })
})
