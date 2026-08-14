import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { AuthProvider } from '../../context/AuthContext.jsx'
import { AppHeader } from './AppHeader.jsx'

describe('AppHeader deployment policy', () => {
  it('does not expose member search when the deployment blocks it', () => {
    render(
      <AuthProvider value={{ accountSummary: 'Admin', canSearchMembers: false, member: { memberId: 1 }, memberLoading: false }}>
        <AppHeader activePage="search" onSearch={vi.fn()} onNotice={vi.fn()} onMemberSearch={vi.fn()} onAccount={vi.fn()} onLogout={vi.fn()} />
      </AuthProvider>,
    )

    expect(screen.queryByRole('button', { name: '회원 검색' })).not.toBeInTheDocument()
  })
})
