import { defineStore } from 'pinia'
import type { RouteLocationNormalized } from 'vue-router'
import { ref } from 'vue'
import { tabFromRoute } from '@/utils/route-meta'
import { removeTab, upsertTab, type TabItem } from '@/utils/tab-nav'

export const useTabsStore = defineStore('tabs', () => {
  const tabs = ref<TabItem[]>([{ path: '/dashboard', title: '数据概览', closable: false }])
  const activePath = ref('/dashboard')

  function syncRoute(route: RouteLocationNormalized) {
    if (route.path === '/login')
      return
    const tab = tabFromRoute(route)
    tabs.value = upsertTab(tabs.value, tab)
    activePath.value = route.path
  }

  function closeTab(path: string) {
    const { tabs: nextTabs, nextPath } = removeTab(tabs.value, path)
    tabs.value = nextTabs
    return nextPath
  }

  function resetTabs() {
    tabs.value = [{ path: '/dashboard', title: '数据概览', closable: false }]
    activePath.value = '/dashboard'
  }

  return { tabs, activePath, syncRoute, closeTab, resetTabs }
})
