export interface TabItem {
  path: string
  title: string
  closable: boolean
}

const DASHBOARD_PATH = '/dashboard'

export function upsertTab(tabs: TabItem[], tab: TabItem): TabItem[] {
  const exists = tabs.some(t => t.path === tab.path)
  if (exists)
    return tabs
  return [...tabs, tab]
}

export function removeTab(tabs: TabItem[], path: string): { tabs: TabItem[]; nextPath: string } {
  const index = tabs.findIndex(t => t.path === path)
  if (index === -1)
    return { tabs, nextPath: DASHBOARD_PATH }
  const next = tabs.filter(t => t.path !== path)
  if (next.length === 0)
    return { tabs: [{ path: DASHBOARD_PATH, title: '数据概览', closable: false }], nextPath: DASHBOARD_PATH }
  const nextPath = next[Math.min(index, next.length - 1)]?.path ?? DASHBOARD_PATH
  return { tabs: next, nextPath }
}
