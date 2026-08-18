const pending = {
  keyword: '',
  resourceType: '',
  has: false,
}

export function setPendingResourceFilter(filter: { keyword?: string, resourceType?: string } = {}) {
  pending.keyword = filter.keyword || ''
  pending.resourceType = filter.resourceType || ''
  pending.has = true
}

export function consumePendingResourceFilter() {
  if (!pending.has)
    return null
  pending.has = false
  return {
    keyword: pending.keyword,
    resourceType: pending.resourceType,
  }
}

export function openResourceList(filter: { keyword?: string, resourceType?: string } = {}) {
  setPendingResourceFilter(filter)
  uni.switchTab({ url: '/pages/resource/list' })
}
