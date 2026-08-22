import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/api/request'
import { adminHomePath, canOpenAdminPath, isTeacherOnly, readStoredRoles } from '@/utils/admin-access'

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
      path: '/parent',
      name: 'Parent',
      component: () => import('@/views/parent/Index.vue'),
      meta: { title: '家长管理', breadcrumb: ['家长管理'] },
    },
    {
      path: '/account',
      name: 'Account',
      component: () => import('@/views/account/Index.vue'),
      meta: { title: '用户角色', breadcrumb: ['用户角色'] },
    },
    {
      path: '/teacher/me',
      name: 'TeacherMe',
      component: () => import('@/views/teacher/Me.vue'),
      meta: { title: '我的资料', breadcrumb: ['我的资料'] },
    },
    {
      path: '/teacher',
      name: 'Teacher',
      component: () => import('@/views/teacher/Index.vue'),
      meta: { title: '老师档案', breadcrumb: ['老师档案'] },
    },
    {
      path: '/audit',
      name: 'Audit',
      component: () => import('@/views/audit/Index.vue'),
      meta: { title: '审核中心', breadcrumb: ['审核中心'] },
    },
  ],
})

router.beforeEach(async (to) => {
  const token = getToken()
  if (!to.meta.public && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!token) {
    return
  }
  let roles = readStoredRoles()
  if (roles.length === 0) {
    const { useAuthStore } = await import('@/store/auth')
    try {
      await useAuthStore().fetchProfile()
      roles = [...useAuthStore().roles]
    }
    catch {
      roles = readStoredRoles()
    }
  }
  if (to.path === '/login') {
    return adminHomePath(roles)
  }
  if (roles.length === 0) {
    return
  }
  if (isTeacherOnly(roles) && to.path === '/teacher') {
    return '/teacher/me'
  }
  if (!canOpenAdminPath(to.path, roles)) {
    return adminHomePath(roles)
  }
})

export { RESOURCE_TYPES }
export default router
