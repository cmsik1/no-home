import { useCallback, useState } from 'react'
import { memberService } from '../services/memberService'
import { useMemberAdminSearch } from './useMemberAdminSearch'

const EMPTY_LOGIN = { email: '', password: '' }
const EMPTY_SIGNUP = { email: '', password: '', name: '', phone: '' }
const EMPTY_PASSWORD_RESET = { email: '', name: '', phone: '', newPassword: '' }

export function useMemberAccount({ setActivePage, onLogoutCleanup, onLoginSuccess } = {}) {
  const [member, setMember] = useState(null)
  const [accountMode, setAccountMode] = useState('login')
  const [memberLoading, setMemberLoading] = useState(false)
  const [memberMessage, setMemberMessage] = useState('')
  const [memberError, setMemberError] = useState('')
  const [loginForm, setLoginForm] = useState(EMPTY_LOGIN)
  const [signupForm, setSignupForm] = useState(EMPTY_SIGNUP)
  const [passwordResetForm, setPasswordResetForm] = useState(EMPTY_PASSWORD_RESET)
  const [profileForm, setProfileForm] = useState({ name: '', phone: '' })
  const [profileEditing, setProfileEditing] = useState(false)
  const [deleteConfirm, setDeleteConfirm] = useState('')
  const adminSearch = useMemberAdminSearch({ setMemberLoading, setMemberMessage, setMemberError })
  const { resetMemberSearch } = adminSearch

  const clearFeedback = useCallback(() => {
    setMemberError('')
    setMemberMessage('')
  }, [])

  const reportError = useCallback((exception, fallback) => {
    setMemberError(exception instanceof Error ? exception.message : fallback)
  }, [])

  const setCurrentMember = useCallback((nextMember) => {
    setMember(nextMember)
    setProfileForm({ name: nextMember?.name || '', phone: nextMember?.phone || '' })
    if (!nextMember) {
      resetMemberSearch()
      setActivePage?.((page) => page === 'member-search' ? 'search' : page)
    }
  }, [resetMemberSearch, setActivePage])

  const loadCurrentMember = useCallback(async ({ silent = true } = {}) => {
    setMemberLoading(true)
    if (!silent) clearFeedback()
    try {
      setCurrentMember(await memberService.getCurrentMember())
    } catch (exception) {
      if (exception?.status === 401) {
        setCurrentMember(null)
        if (!silent) setMemberMessage('로그인이 필요합니다.')
      } else if (!silent) {
        reportError(exception, '회원 정보를 불러오지 못했습니다.')
      }
    } finally {
      setMemberLoading(false)
    }
  }, [clearFeedback, reportError, setCurrentMember])

  async function signupMember() {
    setMemberLoading(true)
    clearFeedback()
    try {
      const createdMember = await memberService.signup(signupForm)
      setLoginForm({ email: createdMember.email || signupForm.email, password: '' })
      setSignupForm(EMPTY_SIGNUP)
      setAccountMode('login')
      setMemberMessage('회원가입이 완료되었습니다. 새 계정으로 로그인해 주세요.')
    } catch (exception) {
      reportError(exception, '회원가입에 실패했습니다.')
    } finally {
      setMemberLoading(false)
    }
  }

  async function loginMember() {
    setMemberLoading(true)
    clearFeedback()
    try {
      const loggedInMember = await memberService.login(loginForm)
      setCurrentMember(loggedInMember)
      setLoginForm((previous) => ({ ...previous, password: '' }))
      setAccountMode('profile')
      setMemberMessage('로그인되었습니다.')
      await onLoginSuccess?.()
    } catch (exception) {
      reportError(exception, '로그인에 실패했습니다.')
    } finally {
      setMemberLoading(false)
    }
  }

  async function logoutMember() {
    setMemberLoading(true)
    clearFeedback()
    try {
      await memberService.logout()
    } catch {
      // Expired server sessions must not block local cleanup.
    } finally {
      setCurrentMember(null)
      onLogoutCleanup?.()
      setProfileEditing(false)
      setAccountMode('login')
      setMemberMessage('로그아웃되었습니다.')
      setMemberLoading(false)
    }
  }

  async function resetPassword() {
    setMemberLoading(true)
    clearFeedback()
    try {
      await memberService.resetPassword(passwordResetForm)
      setLoginForm({ email: passwordResetForm.email, password: '' })
      setPasswordResetForm(EMPTY_PASSWORD_RESET)
      setAccountMode('login')
      setMemberMessage('비밀번호가 변경되었습니다. 새 비밀번호로 로그인해 주세요.')
    } catch (exception) {
      reportError(exception, '비밀번호 변경에 실패했습니다.')
    } finally {
      setMemberLoading(false)
    }
  }

  async function updateMember() {
    setMemberLoading(true)
    clearFeedback()
    try {
      setCurrentMember(await memberService.updateCurrentMember(profileForm))
      setProfileEditing(false)
      setMemberMessage('회원 정보가 수정되었습니다.')
    } catch (exception) {
      reportError(exception, '회원 정보 수정에 실패했습니다.')
    } finally {
      setMemberLoading(false)
    }
  }

  async function deleteMember() {
    if (deleteConfirm !== 'DELETE') {
      setMemberError('회원 계정을 삭제하려면 확인란에 DELETE를 입력해 주세요.')
      return
    }
    setMemberLoading(true)
    clearFeedback()
    try {
      await memberService.deleteCurrentMember()
      setCurrentMember(null)
      setDeleteConfirm('')
      setAccountMode('login')
      setMemberMessage('회원 계정이 삭제되었습니다.')
    } catch (exception) {
      reportError(exception, '회원 탈퇴에 실패했습니다.')
    } finally {
      setMemberLoading(false)
    }
  }

  return {
    member, setCurrentMember, accountMode, setAccountMode, memberLoading, memberMessage, setMemberMessage,
    memberError, setMemberError, loginForm, setLoginForm, signupForm, setSignupForm, passwordResetForm,
    setPasswordResetForm, profileForm, setProfileForm, profileEditing, setProfileEditing,
    memberSearchKeyword: adminSearch.memberSearchKeyword, setMemberSearchKeyword: adminSearch.setMemberSearchKeyword,
    memberSearchResults: adminSearch.memberSearchResults, setMemberSearchResults: adminSearch.setMemberSearchResults,
    deleteConfirm, setDeleteConfirm, loadCurrentMember, signupMember, loginMember, logoutMember, resetPassword,
    updateMember, deleteMember, searchMembers: adminSearch.searchMembers,
  }
}
