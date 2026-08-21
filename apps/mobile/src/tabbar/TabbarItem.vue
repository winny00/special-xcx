<script setup lang="ts">
import type { CustomTabBarItem } from './types'
import { computed } from 'vue'
import { getI18nText } from './i18n'
import { tabbarStore } from './store'

const props = defineProps<{
  item: CustomTabBarItem
  index: number
  isBulge?: boolean
}>()

const isActive = computed(() => tabbarStore.curIdx === props.index)

function getImageByIndex(index: number, item: CustomTabBarItem) {
  if (!item.iconActive) {
    console.warn('image 模式下，需要配置 iconActive (高亮时的图片），否则无法切换高亮图片')
    return item.icon
  }
  return tabbarStore.curIdx === index ? item.iconActive : item.icon
}
</script>

<template>
  <view class="flex flex-col items-center justify-center">
    <template v-if="item.iconType === 'uiLib'">
      <!-- TODO: 以下内容请根据选择的UI库自行替换 -->
    </template>
    <template v-if="item.iconType === 'unocss' || item.iconType === 'iconfont'">
      <view
        class="tab-icon-shell"
        :class="[
          isBulge ? 'tab-icon-shell--bulge' : '',
          isActive && !isBulge ? 'tab-icon-shell--active' : '',
        ]"
      >
        <view
          :class="[
            item.icon,
            isBulge ? 'text-80px' : (item.iconType === 'iconfont' ? 'tab-iconfont' : 'text-20px'),
          ]"
        />
      </view>
    </template>
    <template v-if="item.iconType === 'image'">
      <image :src="getImageByIndex(index, item)" mode="scaleToFill" :class="isBulge ? 'h-80px w-80px' : 'h-24px w-24px'" />
    </template>
    <view
      v-if="!isBulge"
      class="tab-label"
      :class="isActive ? 'tab-label--active' : 'text-muted'"
    >
      {{ getI18nText(item.text) }}
    </view>
    <!-- 角标显示 -->
    <view v-if="item.badge">
      <template v-if="item.badge === 'dot'">
        <view class="absolute right-0 top-0 h-2 w-2 rounded-full bg-#f56c6c" />
      </template>
      <template v-else>
        <view class="absolute top-0 box-border h-5 min-w-5 center rounded-full bg-#f56c6c px-1 text-center text-xs text-white -right-3">
          {{ item.badge > 99 ? '99+' : item.badge }}
        </view>
      </template>
    </view>
  </view>
</template>

<style scoped lang="scss">
.tab-icon-shell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  transition: background-color 0.2s ease, transform 0.2s ease;
}
.tab-icon-shell--active {
  background-color: #e7f4f0;
}
.tab-icon-shell--bulge {
  width: auto;
  height: auto;
  border-radius: 0;
  background: transparent;
}
.tab-iconfont {
  font-size: 22px;
  line-height: 1;
  transition: font-size 0.2s ease, color 0.2s ease;
}
.tab-icon-shell--active .tab-iconfont {
  font-size: 24px;
}
.tab-label {
  margin-top: 2px;
  font-size: 11px;
  line-height: 1.2;
  transition: color 0.2s ease;
}
.tab-label--active {
  color: var(--wot-color-theme, #1b7f6b);
  font-weight: 500;
}
@media (prefers-reduced-motion: reduce) {
  .tab-icon-shell,
  .tab-iconfont,
  .tab-label {
    transition: none;
  }
}
</style>
