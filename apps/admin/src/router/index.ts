import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/api/request'

const RESOURCE_TYPES = ['course', 'tool', 'teacher', 'assessment'] as const

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
      redirect: '/dashboard',
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/Index.vue'),
      meta: { title: '数据概览', breadcrumb: ['数据概览'] },
    },
    {
      path: '/resource',
      redirect: '/resource/course',
    },
    {
      path: '/resource/:type',
      name: 'ResourceByType',
      component: () => import('@/views/resource/Index.vue'),
      beforeEnter: (to) => {
        const type = to.params.type as string
        if (!RESOURCE_TYPES.includes(type as typeof RESOURCE_TYPES[number])) {
          return '/resource/course'
        }
      },
      meta: { title: '资源管理' },
    },
    {
      path: '/organization',
      name: 'Organization',
      component: () => import('@/views/organization/Index.vue'),
      meta: { title: '机构管理', breadcrumb: ['机构管理'] },
    },
    {
      path: '/article',
      name: 'Article',
      component: () => import('@/views/article/Index.vue'),
      meta: { title: '资讯管理', breadcrumb: ['资讯管理'] },
    },
    {
      path: '/appointment',
      name: 'Appointment',
      component: () => import('@/views/appointment/Index.vue'),
      meta: { title: '预约管理', breadcrumb: ['预约管理'] },
    },
    {
      path: '/audit',
      name: 'Audit',
      component: () => import('@/views/audit/Index.vue'),
      meta: { title: '审核中心', breadcrumb: ['审核中心'] },
    },
  ],
})

router.beforeEach((to) => {
  const token = getToken()
  if (!to.meta.public && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && token) {
    return '/dashboard'
  }
})

export { RESOURCE_TYPES }
export default router
