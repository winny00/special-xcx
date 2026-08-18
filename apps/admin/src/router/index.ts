import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/api/request'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      redirect: '/resource',
    },
    {
      path: '/resource',
      name: 'Resource',
      component: () => import('@/views/resource/Index.vue'),
    },
    {
      path: '/organization',
      name: 'Organization',
      component: () => import('@/views/organization/Index.vue'),
    },
    {
      path: '/appointment',
      name: 'Appointment',
      component: () => import('@/views/appointment/Index.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const token = getToken()
  if (!to.meta.public && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && token) {
    return '/resource'
  }
})

export default router
