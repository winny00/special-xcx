<script lang="ts" setup>
import type { ISpecialTeacher } from '@/api/types/special'
import { getTeacherList } from '@/api/special'

definePage({
  style: {
    navigationBarTitleText: '老师档案',
  },
})

const teachers = ref<ISpecialTeacher[]>([])
const loading = ref(false)
const finished = ref(false)
const initialLoading = ref(true)
const pageNum = ref(1)
const keyword = ref('')

async function loadMore() {
  if (loading.value || finished.value)
    return
  loading.value = true
  try {
    const res = await getTeacherList({
      pageNum: pageNum.value,
      pageSize: 10,
      name: keyword.value || undefined,
    })
    const rows = res.rows || []
    teachers.value.push(...rows)
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
  teachers.value = []
  pageNum.value = 1
  finished.value = false
  loading.value = false
  initialLoading.value = true
  loadMore()
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/teacher/detail?id=${id}` })
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
    <view class="p-3">
      <wd-search v-model="keyword" placeholder="搜索老师姓名" @search="search" @clear="search" />
    </view>

    <fg-skeleton-block v-if="initialLoading" />
    <fg-empty-state
      v-else-if="teachers.length === 0 && !loading"
      title="暂无老师档案"
      description="通过审核的老师会显示在这里"
      action-text="返回首页"
      @action="goHome"
    />
    <view v-else class="flex flex-col gap-3 p-3">
      <view
        v-for="item in teachers"
        :key="String(item.id)"
        class="fg-surface-card fg-tap-active flex overflow-hidden"
        @click="goDetail(item.id)"
      >
        <image
          v-if="item.avatarUrl"
          :src="item.avatarUrl"
          mode="aspectFill"
          class="shrink-0"
          style="width: 88px; height: 88px"
        />
        <view
          v-else
          class="flex shrink-0 items-center justify-center text-xl font-medium text-white"
          style="width: 88px; height: 88px; background-color: #2A8F9A"
        >
          {{ (item.name || '师').slice(0, 1) }}
        </view>
        <view class="min-w-0 flex-1 px-3 py-3">
          <view class="truncate text-base font-medium text-[#1C2B28]">
            {{ item.name }}
          </view>
          <view v-if="item.title" class="mt-1 text-sm text-muted">
            {{ item.title }}
          </view>
          <view v-if="item.specialties" class="mt-2 truncate text-xs text-[#1B7F6B]">
            {{ item.specialties }}
          </view>
        </view>
      </view>
    </view>
    <view v-if="loading && !initialLoading" class="py-4 text-center text-sm text-muted">
      加载中...
    </view>
  </view>
</template>
