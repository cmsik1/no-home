import { createContext, useContext } from 'react'

const AuthContext = createContext(null)

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
