<script lang="ts" setup>
withDefaults(defineProps<{
  title?: string
  description?: string
  actionText?: string
}>(), {
  title: '暂无内容',
  description: '',
  actionText: '',
})

const emit = defineEmits<{
  action: []
}>()
</script>

<template>
  <view class="flex flex-col items-center px-8 py-16 text-center">
    <view class="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-[#E7F4F0] text-[#1B7F6B]">
      <text class="i-carbon-document-blank text-xl" />
    </view>
    <view class="text-base font-medium text-[#1C2B28]">
      {{ title }}
    </view>
    <view v-if="description" class="mt-1 text-sm leading-relaxed text-muted">
      {{ description }}
    </view>
    <view
      v-if="actionText"
      class="mt-4 inline-flex min-h-11 items-center rounded-full bg-[#1B7F6B] px-5 text-sm text-white active:opacity-80"
      hover-class="opacity-80"
      @click="emit('action')"
    >
      {{ actionText }}
    </view>
  </view>
</template>
