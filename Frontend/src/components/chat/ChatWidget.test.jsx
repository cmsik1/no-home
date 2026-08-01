import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { render } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import ChatWidget from './ChatWidget.jsx'

describe('ChatWidget', () => {
  it('opens the assistant panel with readable guidance', async () => {
    const user = userEvent.setup()
    render(<ChatWidget />)

    await user.click(screen.getByRole('button', { name: 'AI' }))

    expect(screen.getByRole('complementary', { name: 'AI 도우미' })).toBeInTheDocument()
    expect(screen.getByText('로그인 후 사용할 수 있어요.')).toBeInTheDocument()
    await waitFor(() => {
      expect(screen.getByPlaceholderText('예: 강남구 2024년 5월 매매 검색')).toHaveFocus()
    })
  })

  it('explains that login is required without calling the API', async () => {
    const user = userEvent.setup()
    render(<ChatWidget />)
    await user.click(screen.getByRole('button', { name: 'AI' }))
    await user.type(screen.getByRole('textbox'), '강남구 검색')
    await user.click(screen.getByRole('button', { name: '전송' }))

    expect(screen.getByText('로그인 후 사용할 수 있습니다.')).toBeInTheDocument()
  })
})
