<script lang="ts" setup>
import type { ISpecialOrganization } from '@/api/types/special'
import { getOrganizationList } from '@/api/special'

definePage({
  style: {
    navigationBarTitleText: '机构学校',
  },
})

const organizations = ref<ISpecialOrganization[]>([])
const loading = ref(false)
const finished = ref(false)
const initialLoading = ref(true)
const pageNum = ref(1)
const keyword = ref('')
const orgType = ref('')

const typeTabs = [
  { label: '全部', value: '' },
  { label: '学校', value: 'school' },
  { label: '康复中心', value: 'rehab' },
  { label: '培训机构', value: 'training' },
]

function orgTypeLabel(type?: string) {
  if (type === 'school')
    return '学校'
  if (type === 'rehab')
    return '康复中心'
  if (type === 'training')
    return '培训机构'
  return '机构'
}

async function loadMore() {
  if (loading.value || finished.value)
    return
  loading.value = true
  try {
    const res = await getOrganizationList({
      pageNum: pageNum.value,
      pageSize: 10,
      name: keyword.value || undefined,
      orgType: orgType.value || undefined,
    })
    const rows = res.rows || []
    organizations.value.push(...rows)
    if (rows.length < 10)
      finished.value = true
    else
      pageNum.value++
  }
  catch (e) {
    console.error(e)
  }
  finally {
    loading.value = false
    initialLoading.value = false
  }
}

function search() {
  organizations.value = []
  pageNum.value = 1
  finished.value = false
  loading.value = false
  initialLoading.value = true
  loadMore()
}

function selectType(value: string) {
  orgType.value = value
  search()
}

function goHome() {
  uni.switchTab({ url: '/pages/index/index' })
}

onLoad(() => {
  loadMore()
})

onReachBottom(() => {
  loadMore()
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <fg-filter-bar
      :pills="typeTabs"
      :active-value="orgType"
      @select="selectType"
    >
      <wd-search v-model="keyword" placeholder="搜索机构、学校..." @search="search" @clear="search" />
    </fg-filter-bar>

    <fg-skeleton-block v-if="initialLoading" />

    <fg-empty-state
      v-else-if="organizations.length === 0 && !loading"
      title="暂无机构信息"
      description="换个关键词或类型再试试"
      action-text="返回首页"
      @action="goHome"
    />

    <view v-else class="flex flex-col gap-3 p-3">
      <fg-org-card
        v-for="org in organizations"
        :key="String(org.id)"
        :org="org"
        :type-label="orgTypeLabel(org.orgType)"
      />
    </view>

    <view v-if="loading && !initialLoading" class="py-4 text-center text-sm text-muted">
      加载中...
    </view>
    <view v-else-if="finished && organizations.length > 0" class="py-4 text-center text-sm text-muted">
      没有更多了
    </view>
  </view>
</template>
