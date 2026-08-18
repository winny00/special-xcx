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
const pageNum = ref(1)
const finished = ref(false)
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
  }
}

function search() {
  organizations.value = []
  pageNum.value = 1
  finished.value = false
  loading.value = false
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
    <view class="sticky top-0 z-10 bg-white px-4 py-3">
      <wd-search v-model="keyword" placeholder="搜索机构、学校..." @search="search" @clear="search" />
      <scroll-view scroll-x class="mt-3 whitespace-nowrap">
        <view
          v-for="tab in typeTabs"
          :key="tab.value"
          class="mr-2 inline-flex min-h-11 items-center rounded-full px-3 text-sm"
          :class="orgType === tab.value ? 'bg-[#1B7F6B] text-white' : 'bg-[#F4F7F6] text-muted'"
          @click="selectType(tab.value)"
        >
          {{ tab.label }}
        </view>
      </scroll-view>
    </view>

    <fg-empty-state
      v-if="organizations.length === 0 && !loading"
      title="暂无机构信息"
      description="换个关键词或类型再试试"
      action-text="返回首页"
      @action="goHome"
    />

    <view v-else class="p-3">
      <view
        v-for="org in organizations"
        :key="org.id"
        class="mb-3 rounded-[12px] bg-white p-4"
      >
        <view class="flex items-start justify-between gap-2">
          <view class="min-w-0 flex-1 text-base font-medium text-[#1C2B28]">
            {{ org.name }}
          </view>
          <text class="shrink-0 rounded-full bg-[#E7F4F0] px-2 py-0.5 text-xs text-[#1B7F6B]">
            {{ orgTypeLabel(org.orgType) }}
          </text>
        </view>
        <view v-if="org.region || org.address" class="mt-2 flex items-start text-sm text-muted">
          <text class="i-carbon-location mr-1 mt-0.5 text-[#1B7F6B]" />
          <text>{{ [org.region, org.address].filter(Boolean).join(' · ') }}</text>
        </view>
        <view v-if="org.description" class="mt-2 line-clamp-2 text-sm text-muted">
          {{ org.description }}
        </view>
        <view v-if="org.contactPhone" class="mt-2 flex items-center text-sm text-[#1B7F6B]">
          <text class="i-carbon-phone mr-1" />
          <text>{{ org.contactName }} {{ org.contactPhone }}</text>
        </view>
      </view>
    </view>

    <view v-if="loading" class="py-4 text-center text-sm text-muted">
      加载中...
    </view>
    <view v-else-if="finished && organizations.length > 0" class="py-4 text-center text-sm text-muted">
      没有更多了
    </view>
  </view>
</template>
