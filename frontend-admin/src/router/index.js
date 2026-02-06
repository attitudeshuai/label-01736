import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'paper-reading',
        name: 'PaperReading',
        component: () => import('@/views/PaperReading.vue'),
        meta: { title: '论文阅读' }
      },
      {
        path: 'achievement',
        name: 'Achievement',
        component: () => import('@/views/Achievement.vue'),
        meta: { title: '成果登记' }
      },
      {
        path: 'weekly-report',
        name: 'WeeklyReport',
        component: () => import('@/views/WeeklyReport.vue'),
        meta: { title: '周报管理' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/User.vue'),
        meta: { title: '用户管理', roles: [1] }
      },
      {
        path: 'operation-log',
        name: 'OperationLog',
        component: () => import('@/views/OperationLog.vue'),
        meta: { title: '操作日志', roles: [1] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth === false) {
    next()
    return
  }
  
  if (!userStore.token) {
    next('/login')
    return
  }
  
  if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
