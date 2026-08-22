<script setup lang="ts">
defineProps<{
  displayName: string
  hint: string
  loggedIn: boolean
  avatarUrl?: string
  roleLabel?: string
}>()

const emit = defineEmits<{
  login: []
}>()
</script>

<template>
  <view class="fg-profile-card fg-hero-gradient px-4 pb-6 pt-8">
    <view class="fg-profile-card__body fg-surface-card">
      <view class="flex items-center px-4 py-5">
        <image
          v-if="loggedIn && avatarUrl"
          :src="avatarUrl"
          mode="aspectFill"
          class="avatar avatar--image"
        />
        <view v-else class="avatar">
          {{ displayName.slice(0, 1) }}
        </view>
        <view class="ml-3 min-w-0 flex-1">
          <view class="flex min-h-11 items-center gap-2">
            <view class="truncate text-lg font-semibold text-[#1C2B28]">
              {{ displayName }}
            </view>
            <view
              v-if="loggedIn && roleLabel"
              class="role-tag"
            >
              {{ roleLabel }}
            </view>
          </view>
          <view class="mt-1 truncate text-sm text-muted">
            {{ hint }}
          </view>
        </view>
        <view
          v-if="!loggedIn"
          class="login-btn fg-tap-active"
          @click="emit('login')"
        >
          去登录
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.fg-profile-card__body {
  overflow: hidden;
}
.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--color-primary-soft);
  font-size: 20px;
  font-weight: 600;
  color: var(--color-primary);
}
.avatar--image {
  background: var(--color-canvas);
}
.login-btn {
  display: inline-flex;
  align-items: center;
  min-height: 44px;
  padding: 0 16px;
  border-radius: 999px;
  background: var(--color-primary);
  font-size: 14px;
  color: #fff;
}
.role-tag {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  font-size: 12px;
  line-height: 1;
  color: var(--color-primary);
}
</style>
