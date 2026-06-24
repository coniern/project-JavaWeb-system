import { defineStore } from 'pinia'

export const useSessionStore = defineStore('session', {
  state: () => ({
    token: '',
    user: null,
    initialized: false,
    sidebarCollapsed: false
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    displayName: (state) => state.user?.nickname || state.user?.username || '未登录'
  },
  actions: {
    hydrate() {
      this.token = localStorage.getItem('token') || ''
      const rawUser = localStorage.getItem('user')
      this.user = rawUser ? JSON.parse(rawUser) : null
      this.initialized = true
    },
    setSession(payload) {
      this.token = payload.token
      this.user = payload.user
      localStorage.setItem('token', payload.token)
      localStorage.setItem('user', JSON.stringify(payload.user))
    },
    updateUser(user) {
      this.user = user
      localStorage.setItem('user', JSON.stringify(user))
    },
    clearSession() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    }
  }
})
