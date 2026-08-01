import { requestJson } from './apiClient'

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
