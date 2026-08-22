<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ISpecialOrganization } from '@/api/types/special'

const props = defineProps<{
  org: ISpecialOrganization
  typeLabel: string
}>()

const coverFailed = ref(false)
watch(() => props.org.coverUrl, () => {
  coverFailed.value = false
})
const showCover = computed(() => Boolean(props.org.coverUrl) && !coverFailed.value)
const initialChar = computed(() => (props.org.name || '机').slice(0, 1))
</script>

<template>
  <view class="fg-org-card fg-surface-card fg-tap-active flex overflow-hidden">
    <image
      v-if="showCover"
      :src="org.coverUrl"
      mode="aspectFill"
      class="cover-image shrink-0"
      style="width: 88px; height: 88px"
      @error="coverFailed = true"
    />
    <view
      v-else
      class="cover-fallback"
      style="width: 88px; height: 88px; background-color: #1B7F6B"
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
.cover-image,
.cover-fallback {
  width: 88px;
  height: 88px;
  flex-shrink: 0;
}
.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 500;
  color: #fff;
}
.type-tag {
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  font-size: 12px;
  color: var(--color-primary);
}
</style>
