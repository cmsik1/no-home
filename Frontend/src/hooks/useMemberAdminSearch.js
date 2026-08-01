import { useCallback, useState } from 'react'
import { memberService } from '../services/memberService'

export function useMemberAdminSearch({ setMemberLoading, setMemberMessage, setMemberError }) {
  const [memberSearchKeyword, setMemberSearchKeyword] = useState('')
  const [memberSearchResults, setMemberSearchResults] = useState([])

  const resetMemberSearch = useCallback(() => {
    setMemberSearchKeyword('')
    setMemberSearchResults([])
  }, [])

  const searchMembers = useCallback(async (isNoticeAdmin) => {
    if (!isNoticeAdmin) {
      setMemberError('관리자만 회원 검색을 사용할 수 있습니다.')
      return
    }
    const keyword = memberSearchKeyword.trim()
    if (!keyword) {
      setMemberError('검색어를 입력해 주세요.')
      return
    }
    setMemberLoading(true)
    setMemberError('')
    setMemberMessage('')
    try {
      const members = await memberService.searchMembers(keyword)
      const count = Array.isArray(members) ? members.length : 0
      setMemberSearchResults(Array.isArray(members) ? members : [])
      setMemberMessage(`${count.toLocaleString()}명의 회원을 찾았습니다.`)
    } catch (exception) {
      setMemberError(exception instanceof Error ? exception.message : '회원 검색에 실패했습니다.')
    } finally {
      setMemberLoading(false)
    }
  }, [memberSearchKeyword, setMemberError, setMemberLoading, setMemberMessage])

  return { memberSearchKeyword, setMemberSearchKeyword, memberSearchResults, setMemberSearchResults, resetMemberSearch, searchMembers }
}
