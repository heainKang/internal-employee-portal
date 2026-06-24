import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  { path: '/me', component: () => import('../views/MyProfileView.vue') },
  { path: '/admin/employees', component: () => import('../views/admin/EmployeeListView.vue'), meta: { admin: true } },
  { path: '/admin/employees/create', component: () => import('../views/admin/EmployeeCreateView.vue'), meta: { admin: true } },
  { path: '/admin/employees/:id', component: () => import('../views/admin/EmployeeDetailView.vue'), meta: { admin: true } },
  { path: '/admin/background-checks', component: () => import('../views/admin/BackgroundCheckView.vue'), meta: { admin: true } },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLoggedIn) return '/login'
  if (to.meta.admin && !auth.isAdmin) return '/me'
  if (to.path === '/login' && auth.isLoggedIn) {
    return auth.isAdmin ? '/admin/employees' : '/me'
  }
})

export default router
