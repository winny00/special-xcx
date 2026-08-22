import type { RouteLocationNormalized } from 'vue-router'
import type { TabItem } from './tab-nav'

const RESOURCE_TITLES: Record<string, { title: string; breadcrumb: string[] }> = {
  course: { title: '课程管理', breadcrumb: ['资源对接', '课程管理'] },
  tool: { title: '工具管理', breadcrumb: ['资源对接', '工具管理'] },
  teacher: { title: '老师资源', breadcrumb: ['资源对接', '老师资源'] },
  assessment: { title: '评估管理', breadcrumb: ['资源对接', '评估管理'] },
}

const SKIP_TAB_PATHS = new Set(['/', '/login', '/resource'])

export function shouldRecordTab(route: RouteLocationNormalized): boolean {
  if (SKIP_TAB_PATHS.has(route.path))
    return false
  if (route.path.startsWith('/resource/'))
    return true
  const meta = route.meta as { title?: string }
  return Boolean(meta.title)
}

export function resolveRouteMeta(route: RouteLocationNormalized) {
  if (route.path.startsWith('/resource/')) {
    const type = route.params.type as string
    return RESOURCE_TITLES[type] ?? { title: '资源管理', breadcrumb: ['资源对接', '资源管理'] }
  }
  const meta = route.meta as { title?: string; breadcrumb?: string[] }
  return {
    title: meta.title ?? '特教管理后台',
    breadcrumb: meta.breadcrumb ?? [meta.title ?? '页面'],
  }
}

export function tabFromRoute(route: RouteLocationNormalized): TabItem {
  const { title } = resolveRouteMeta(route)
  return {
    path: route.path,
    title,
    closable: route.path !== '/dashboard',
  }
}
