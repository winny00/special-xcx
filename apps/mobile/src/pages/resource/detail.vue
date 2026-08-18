<script lang="ts" setup>
import type { ISpecialResource } from '@/api/types/special'
import { RESOURCE_TYPE_MAP } from '@/api/types/special'
import { getResourceDetail } from '@/api/special'

definePage({
  style: {
    navigationBarTitleText: '资源详情',
  },
})

const resource = ref<ISpecialResource | null>(null)
const loading = ref(true)
const resourceId = ref('')

const typeLabel = computed(() => {
  if (!resource.value)
    return ''
  return RESOURCE_TYPE_MAP[resource.value.resourceType] || resource.value.resourceType
})

const priceText = computed(() => {
  if (!resource.value)
    return '免费咨询'
  return resource.value.price && Number(resource.value.price) > 0 ? `¥${resource.value.price}` : '免费咨询'
})

async function loadDetail() {
  if (!resourceId.value) {
    loading.value = false
    resource.value = null
    return
  }
  loading.value = true
  try {
    resource.value = await getResourceDetail(resourceId.value)
  }
  catch (e) {
    console.error(e)
    resource.value = null
  }
  finally {
    loading.value = false
  }
}

function goAppointment() {
  if (!resource.value)
    return
  uni.navigateTo({
    url: `/pages/resource/appointment?resourceId=${resource.value.id}&title=${encodeURIComponent(resource.value.title)}`,
  })
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
    return
  }
  uni.switchTab({ url: '/pages/index/index' })
}

function callProvider() {
  if (!resource.value?.contactPhone)
    return
  uni.makePhoneCall({
    phoneNumber: resource.value.contactPhone,
  })
}

onLoad((query) => {
  resourceId.value = String(query?.id || '')
  loadDetail()
})
</script>

<template>
  <view class="detail-page min-h-screen bg-[#F4F7F6]">
    <view v-if="loading" class="py-20 text-center text-sm text-muted">
      加载中...
    </view>
    <fg-empty-state
      v-else-if="!resource"
      title="暂无该资源"
      description="资源可能已下架，返回首页看看其他推荐"
      action-text="返回上一页"
      @action="goBack"
    />
    <template v-else>
      <view class="bg-white px-4 py-5">
        <view class="text-xl font-semibold text-[#1C2B28]">
          {{ resource.title }}
        </view>
        <view class="mt-3 flex flex-wrap gap-2">
          <text class="rounded-full bg-[#E7F4F0] px-2.5 py-0.5 text-xs text-[#1B7F6B]">
            {{ typeLabel }}
          </text>
          <text v-if="resource.category" class="rounded-full bg-[#F4F7F6] px-2.5 py-0.5 text-xs text-muted">
            {{ resource.category }}
          </text>
          <text v-if="resource.region" class="rounded-full bg-[#F4F7F6] px-2.5 py-0.5 text-xs text-muted">
            {{ resource.region }}
          </text>
        </view>
      </view>

      <view
        v-if="resource.providerName || resource.contactPhone"
        class="mx-3 mt-3 flex items-center rounded-[12px] bg-white px-4 py-3"
        @click="callProvider"
      >
        <view class="mr-3 flex h-11 w-11 items-center justify-center rounded-full bg-[#E7F4F0] text-[#1B7F6B]">
          <text class="i-carbon-building" />
        </view>
        <view class="min-w-0 flex-1">
          <view class="truncate text-sm font-medium text-[#1C2B28]">
            {{ resource.providerName || '平台资源' }}
          </view>
          <view v-if="resource.contactPhone" class="mt-0.5 text-xs text-[#1B7F6B]">
            {{ resource.contactPhone }}
          </view>
        </view>
        <view v-if="resource.contactPhone" class="ml-2 flex min-h-11 items-center text-sm text-[#1B7F6B]">
          <text class="i-carbon-phone mr-1" />
          <text>拨打</text>
        </view>
      </view>

      <view class="mx-3 mt-3 rounded-[12px] bg-white p-4">
        <view class="mb-2 text-sm font-medium text-[#1C2B28]">
          资源介绍
        </view>
        <view v-if="resource.summary" class="mb-3 text-sm leading-relaxed text-[#1C2B28]">
          {{ resource.summary }}
        </view>
        <view v-if="resource.content" class="text-sm leading-relaxed text-muted">
          {{ resource.content }}
        </view>
        <view v-if="!resource.summary && !resource.content" class="text-sm text-muted">
          暂无详细介绍
        </view>
      </view>

      <view class="mx-3 mt-3 mb-24 flex items-center justify-between rounded-[12px] bg-white px-4 py-3">
        <view>
          <view class="text-xs text-muted">
            参考价格
          </view>
          <view class="mt-0.5 text-lg font-semibold text-[#1B7F6B]">
            {{ priceText }}
          </view>
        </view>
        <view class="text-xs text-muted">
          {{ resource.viewCount || 0 }} 次浏览
        </view>
      </view>

      <view class="fixed bottom-0 left-0 right-0 border-t border-[#E7F4F0] bg-white px-4 py-3 pb-safe">
        <wd-button block type="primary" @click="goAppointment">
          预约咨询
        </wd-button>
      </view>
    </template>
  </view>
</template>

<style scoped>
.detail-page {
  padding-bottom: calc(env(safe-area-inset-bottom) + 88px);
}
</style>
