import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    meta: { title: '登录', requiresAuth: false },
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        meta: { title: '仪表盘' },
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'projects',
        name: 'Projects',
        meta: { title: '项目管理' },
        component: () => import('@/views/Projects.vue')
      },
      {
        path: 'tasks',
        name: 'Tasks',
        meta: { title: '任务管理' },
        component: () => import('@/views/Tasks.vue')
      },
      {
        path: 'users',
        name: 'Users',
        meta: { title: '用户管理' },
        component: () => import('@/views/Users.vue')
      },
      {
        path: 'ai',
        name: 'AiAssistant',
        meta: { title: 'AI 助手' },
        component: () => import('@/views/AiAssistant.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  document.title = `${to.meta?.title || '项目管理系统'} · Project OS`

  if (to.meta.requiresAuth === false) {
    if (token && to.path === '/login') {
      next('/dashboard')
      return
    }
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router
