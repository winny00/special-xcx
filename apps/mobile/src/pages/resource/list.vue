<script lang="ts" setup>
import type { ISpecialResource } from '@/api/types/special'
import { RESOURCE_TYPE_MAP } from '@/api/types/special'
import { getResourceList } from '@/api/special'
import { consumePendingResourceFilter } from '@/utils/resource-nav'

definePage({
  style: {
    navigationBarTitleText: '资源库',
  },
})

const resources = ref<ISpecialResource[]>([])
const loading = ref(false)
const finished = ref(false)
const initialLoading = ref(true)
const pageNum = ref(1)
const pageSize = 10
const keyword = ref('')
const resourceType = ref('')

const typePills = [
  { label: '全部', value: '' },
  ...Object.entries(RESOURCE_TYPE_MAP)
    .filter(([key]) => key !== 'org')
    .map(([value, label]) => ({ label, value })),
]

async function loadMore() {
  if (loading.value || finished.value)
    return
  loading.value = true
  try {
    const res = await getResourceList({
      pageNum: pageNum.value,
      pageSize,
      title: keyword.value || undefined,
      resourceType: resourceType.value || undefined,
    })
    const rows = res.rows || []
    resources.value.push(...rows)
    if (rows.length < pageSize)
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
  resources.value = []
  pageNum.value = 1
  finished.value = false
  loading.value = false
  initialLoading.value = true
  loadMore()
}

function selectType(value: string) {
  resourceType.value = value
  search()
}

function goDetail(id: string | number) {
  uni.navigateTo({ url: `/pages/resource/detail?id=${id}` })
}

function goHome() {
  uni.switchTab({ url: '/pages/index/index' })
}

onShow(() => {
  const pending = consumePendingResourceFilter()
  if (pending) {
    keyword.value = pending.keyword
    resourceType.value = pending.resourceType
    search()
    return
  }
  if (resources.value.length === 0 && !loading.value && !finished.value)
    loadMore()
})

onReachBottom(() => {
  loadMore()
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <fg-filter-bar
      :pills="typePills"
      :active-value="resourceType"
      @select="selectType"
    >
      <wd-search v-model="keyword" placeholder="搜索课程、老师..." @search="search" @clear="search" />
    </fg-filter-bar>

    <fg-skeleton-block v-if="initialLoading" />

    <fg-empty-state
      v-else-if="resources.length === 0 && !loading"
      title="暂无匹配资源"
      description="换个关键词试试，或先看看首页推荐"
      action-text="返回首页"
      @action="goHome"
    />

    <view v-else class="flex flex-col gap-3 p-3">
      <view v-for="item in resources" :key="String(item.id)" @click="goDetail(item.id)">
        <fg-resource-card :item="item" />
      </view>
    </view>

    <view v-if="loading && !initialLoading" class="py-4 text-center text-sm text-muted">
      加载中...
    </view>
    <view v-else-if="finished && resources.length > 0" class="py-4 text-center text-sm text-muted">
      没有更多了
    </view>
  </view>
</template>
