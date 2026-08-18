<script lang="ts" setup>
import type { ISpecialResource } from '@/api/types/special'
import { RESOURCE_CATEGORIES } from '@/api/types/special'
import { getResourceList } from '@/api/special'
import { useCapsuleNav } from '@/hooks/useCapsuleNav'
import { openResourceList } from '@/utils/resource-nav'

definePage({
  type: 'home',
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '特教资源平台',
  },
})

const { headerPaddingStyle, capsuleRowStyle } = useCapsuleNav()

const loading = ref(true)
const resources = ref<ISpecialResource[]>([])
const activeCategory = ref('')
const keyword = ref('')

const shortcuts = [
  { label: '课程', icon: 'i-carbon-education', action: 'course' },
  { label: '老师', icon: 'i-carbon-user', action: 'teacher' },
  { label: '找机构', icon: 'i-carbon-building', action: 'org' },
  { label: '预约咨询', icon: 'i-carbon-calendar', action: 'appoint' },
]

async function loadResources() {
  loading.value = true
  try {
    const res = await getResourceList({
      pageNum: 1,
      pageSize: 10,
      category: activeCategory.value || undefined,
    })
    resources.value = res.rows || []
  }
  catch (e) {
    console.error('加载资源失败', e)
    resources.value = []
  }
  finally {
    loading.value = false
  }
}

function selectCategory(cat: string) {
  activeCategory.value = activeCategory.value === cat ? '' : cat
  loadResources()
}

function goResourceList(query: Record<string, string> = {}) {
  openResourceList({
    keyword: query.keyword,
    resourceType: query.resourceType,
  })
}

function goSearch() {
  goResourceList({ keyword: keyword.value.trim() })
}

function goShortcut(action: string) {
  if (action === 'org') {
    uni.switchTab({ url: '/pages/organization/list' })
    return
  }
  if (action === 'appoint') {
    goResourceList()
    return
  }
  goResourceList({ resourceType: action })
}

function goDetail(id: string | number) {
  uni.navigateTo({ url: `/pages/resource/detail?id=${id}` })
}

onLoad(() => {
  loadResources()
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <view class="bg-[#F4F7F6] px-4 pb-3" :style="headerPaddingStyle">
      <view class="flex items-center text-lg font-semibold text-[#1C2B28]" :style="capsuleRowStyle">
        你好，需要什么帮助？
      </view>
      <view class="mt-3 flex min-h-11 items-center rounded-full bg-white px-4 active:opacity-80" @click="goSearch">
        <text class="i-carbon-search mr-2 text-muted" />
        <input
          v-model="keyword"
          class="h-6 flex-1 text-sm text-[#1C2B28]"
          placeholder="搜索课程、老师、机构"
          confirm-type="search"
          @confirm="goSearch"
          @click.stop
        >
      </view>
    </view>

    <view class="mx-3 mt-2 flex rounded-[12px] bg-white px-2 py-4">
      <view
        v-for="item in shortcuts"
        :key="item.action"
        class="flex min-h-11 flex-1 flex-col items-center active:opacity-80"
        @click="goShortcut(item.action)"
      >
        <view class="mb-1.5 flex h-11 w-11 items-center justify-center rounded-full bg-[#E7F4F0] text-[#1B7F6B]">
          <text :class="item.icon" />
        </view>
        <text class="text-xs text-[#1C2B28]">{{ item.label }}</text>
      </view>
    </view>

    <view class="mt-4 px-3">
      <scroll-view scroll-x class="whitespace-nowrap">
        <view
          class="mr-2 inline-flex min-h-11 items-center rounded-full px-3 text-sm"
          :class="activeCategory === '' ? 'bg-[#1B7F6B] text-white' : 'bg-white text-muted'"
          @click="selectCategory('')"
        >
          全部领域
        </view>
        <view
          v-for="cat in RESOURCE_CATEGORIES"
          :key="cat"
          class="mr-2 inline-flex min-h-11 items-center rounded-full px-3 text-sm"
          :class="activeCategory === cat ? 'bg-[#1B7F6B] text-white' : 'bg-white text-muted'"
          @click="selectCategory(cat)"
        >
          {{ cat }}
        </view>
      </scroll-view>
    </view>

    <view class="mx-3 mt-4">
      <view class="mb-3 flex items-center justify-between">
        <text class="text-base font-semibold text-[#1C2B28]">推荐资源</text>
        <text class="inline-flex min-h-11 items-center text-sm text-[#1B7F6B] active:opacity-80" @click="goResourceList()">查看更多</text>
      </view>

      <view v-if="loading" class="py-10 text-center text-sm text-muted">
        加载中...
      </view>
      <fg-empty-state
        v-else-if="resources.length === 0"
        title="暂无推荐资源"
        description="可以先浏览全部资源，或换一个干预领域试试"
        action-text="浏览全部资源"
        @action="goResourceList()"
      />
      <view v-else class="flex flex-col gap-3">
        <view v-for="item in resources" :key="String(item.id)" @click="goDetail(item.id)">
          <fg-resource-card :item="item" />
        </view>
      </view>
    </view>
  </view>
</template>
