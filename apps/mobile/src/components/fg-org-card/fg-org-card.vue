<script setup lang="ts">
import type { ISpecialOrganization } from '@/api/types/special'

const props = defineProps<{
  org: ISpecialOrganization
  typeLabel: string
}>()

const initialChar = computed(() => (props.org.name || '机').slice(0, 1))
</script>

<template>
  <view class="fg-org-card fg-surface-card fg-tap-active flex overflow-hidden">
    <image
      v-if="org.coverUrl"
      :src="org.coverUrl"
      mode="aspectFill"
      class="h-full w-64px shrink-0"
      style="min-height: 88px"
    />
    <view
      v-else
      class="flex w-64px shrink-0 items-center justify-center text-xl font-medium text-white"
      style="min-height: 88px; background-color: #1B7F6B"
    >
      {{ initialChar }}
    </view>
    <view class="min-w-0 flex-1 p-4">
      <view class="flex items-start justify-between gap-2">
        <view class="min-w-0 flex-1 text-base font-medium text-[#1C2B28]">
          {{ org.name }}
        </view>
        <text class="type-tag shrink-0">
          {{ typeLabel }}
        </text>
      </view>
      <view v-if="org.region || org.address" class="mt-2 flex items-start text-sm text-muted">
        <text class="i-carbon-location mr-1 mt-0.5 text-[#1B7F6B]" />
        <text>{{ [org.region, org.address].filter(Boolean).join(' · ') }}</text>
      </view>
      <view v-if="org.description" class="mt-2 line-clamp-2 text-sm leading-relaxed text-muted">
        {{ org.description }}
      </view>
      <view v-if="org.contactPhone" class="mt-3 flex items-center text-sm text-[#1B7F6B]">
        <text class="i-carbon-phone mr-1" />
        <text>{{ org.contactName }} {{ org.contactPhone }}</text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.type-tag {
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  font-size: 12px;
  color: var(--color-primary);
}
</style>
