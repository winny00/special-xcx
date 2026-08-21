<script lang="ts" setup>
import type { ISpecialResource } from '@/api/types/special'
import { RESOURCE_TYPE_MAP } from '@/api/types/special'
import { getResourceDetail } from '@/api/special'

definePage({
  style: {
    navigationBarTitleText: '资源详情',
  },
})

const TYPE_BG: Record<string, string> = {
  course: '#1B7F6B',
  tool: '#3D7EA6',
  teacher: '#2A8F9A',
  org: '#5B8A6A',
  assessment: '#6B7F8A',
}

const resource = ref<ISpecialResource | null>(null)
const loading = ref(true)
const resourceId = ref('')

const typeLabel = computed(() => {
  if (!resource.value)
    return ''
  return RESOURCE_TYPE_MAP[resource.value.resourceType] || resource.value.resourceType
})

const heroBg = computed(() => {
  if (!resource.value)
    return '#1B7F6B'
  return TYPE_BG[resource.value.resourceType] || '#1B7F6B'
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
    <fg-skeleton-block v-if="loading" :rows="1" />
    <fg-empty-state
      v-else-if="!resource"
      title="暂无该资源"
      description="资源可能已下架，返回首页看看其他推荐"
      action-text="返回上一页"
      @action="goBack"
    />
    <template v-else>
      <image
        v-if="resource.coverUrl"
        :src="resource.coverUrl"
        mode="aspectFill"
        class="detail-cover w-full"
      />
      <view class="detail-hero" :style="{ backgroundColor: heroBg }">
        <view class="detail-hero__tag">
          {{ typeLabel }}
        </view>
        <view class="detail-hero__title">
          {{ resource.title }}
        </view>
      </view>

      <view
        v-if="resource.providerName || resource.contactPhone"
        class="section-card fg-surface-card fg-tap-active mx-3 mt-3 flex items-center px-4 py-3"
        @click="callProvider"
      >
        <view class="provider-icon">
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
        <view v-if="resource.contactPhone" class="call-link">
          <text class="i-carbon-phone mr-1" />
          <text>拨打</text>
        </view>
      </view>

      <view class="section-card fg-surface-card mx-3 mt-3 p-4">
        <view class="section-title">
          资源介绍
        </view>
        <view v-if="resource.summary" class="mb-3 text-base leading-relaxed text-[#1C2B28]">
          {{ resource.summary }}
        </view>
        <view v-if="resource.content" class="text-sm leading-relaxed text-muted">
          {{ resource.content }}
        </view>
        <view v-if="!resource.summary && !resource.content" class="text-sm text-muted">
          暂无详细介绍
        </view>
        <view v-if="resource.category || resource.region" class="mt-3 flex flex-wrap gap-2">
          <text v-if="resource.category" class="meta-tag">{{ resource.category }}</text>
          <text v-if="resource.region" class="meta-tag">{{ resource.region }}</text>
        </view>
      </view>

      <view class="section-card fg-surface-card mx-3 mt-3 mb-24 flex items-center justify-between px-4 py-3">
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

      <fg-sticky-cta
        :show-secondary="!!resource.contactPhone"
        secondary-text="电话联系"
        @primary="goAppointment"
        @secondary="callProvider"
      />
    </template>
  </view>
</template>

<style scoped lang="scss">
.detail-page {
  padding-bottom: calc(env(safe-area-inset-bottom) + 88px);
}
.detail-cover {
  height: 180px;
}
.detail-hero {
  padding: 24px 16px 28px;
  color: #fff;
}
.detail-hero__tag {
  display: inline-flex;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  font-size: 12px;
}
.detail-hero__title {
  margin-top: 12px;
  font-size: 22px;
  font-weight: 600;
  line-height: 1.4;
}
.provider-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  margin-right: 12px;
  border-radius: 50%;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 20px;
}
.call-link {
  display: flex;
  align-items: center;
  min-height: 44px;
  margin-left: 8px;
  font-size: 14px;
  color: var(--color-primary);
}
.section-title {
  margin-bottom: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-ink);
}
.meta-tag {
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--color-canvas);
  font-size: 12px;
  color: var(--color-muted);
}
</style>
