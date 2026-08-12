import { requestJson } from './apiClient'

/** 회원·인증 화면이 endpoint 문자열이나 공통 응답 형식을 알지 않도록 API 호출을 한곳에 모은다. */
export const memberService = {
  getCurrentMember() {
    return requestJson('/api/members/me')
  },
  signup(form) {
    return requestJson('/api/members', { method: 'POST', body: JSON.stringify(form) })
  },
  login(form) {
    return requestJson('/api/auth/login', { method: 'POST', body: JSON.stringify(form) })
  },
  logout() {
    return requestJson('/api/auth/logout', { method: 'POST' })
  },
  resetPassword(form) {
    return requestJson('/api/auth/password-reset', { method: 'POST', body: JSON.stringify(form) })
  },
  updateCurrentMember(form) {
    return requestJson('/api/members/me', { method: 'PUT', body: JSON.stringify(form) })
  },
  deleteCurrentMember() {
    return requestJson('/api/members/me', { method: 'DELETE' })
  },
  searchMembers(keyword) {
    return requestJson(`/api/members/search?${new URLSearchParams({ keyword })}`)
  },
}
