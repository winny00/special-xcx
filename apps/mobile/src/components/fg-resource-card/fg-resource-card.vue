<script lang="ts" setup>
import type { ISpecialResource } from '@/api/types/special'
import { RESOURCE_TYPE_MAP } from '@/api/types/special'

const props = defineProps<{
  item: ISpecialResource
}>()

const TYPE_CHAR: Record<string, string> = {
  course: '课',
  tool: '工',
  teacher: '师',
  org: '机',
  assessment: '评',
}

const TYPE_BG: Record<string, string> = {
  course: '#1B7F6B',
  tool: '#3D7EA6',
  teacher: '#2A8F9A',
  org: '#5B8A6A',
  assessment: '#6B7F8A',
}

const char = computed(() => TYPE_CHAR[props.item.resourceType] || '资')
const bg = computed(() => TYPE_BG[props.item.resourceType] || '#1B7F6B')
const typeLabel = computed(() => RESOURCE_TYPE_MAP[props.item.resourceType] || props.item.resourceType)
const priceText = computed(() => (props.item.price && props.item.price > 0) ? `¥${props.item.price}` : '免费')
</script>

<template>
  <view class="flex overflow-hidden rounded-[12px] bg-white">
    <view
      class="flex w-64px shrink-0 items-center justify-center text-xl font-medium text-white"
      :style="{ backgroundColor: bg, minHeight: '88px' }"
    >
      {{ char }}
    </view>
    <view class="min-w-0 flex-1 px-3 py-3">
      <view class="truncate text-base font-medium text-[#1C2B28]">
        {{ item.title }}
      </view>
      <view class="mt-1 flex flex-wrap items-center gap-1">
        <text class="rounded-full bg-[#E7F4F0] px-2 py-0.5 text-xs text-[#1B7F6B]">
          {{ typeLabel }}
        </text>
        <text v-if="item.category" class="rounded-full bg-[#F4F7F6] px-2 py-0.5 text-xs text-muted">
          {{ item.category }}
        </text>
      </view>
      <view class="mt-2 flex items-center justify-between text-xs text-muted">
        <text class="truncate">{{ item.providerName || item.region || '平台资源' }}</text>
        <text class="ml-2 shrink-0 font-medium text-[#1B7F6B]">{{ priceText }}</text>
      </view>
    </view>
  </view>
</template>
