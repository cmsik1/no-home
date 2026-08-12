import { createContext, useContext } from 'react'

const AuthContext = createContext(null)

/** 현재 회원과 권한 파생 값을 깊은 화면 컴포넌트에 전달하는 읽기 전용 Context 경계다. */
export function AuthProvider({ value, children }) {
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuthContext() {
  const value = useContext(AuthContext)
  if (!value) {
    throw new Error('useAuthContext must be used inside AuthProvider')
  }
  return value
}
