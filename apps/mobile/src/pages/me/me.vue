<script lang="ts" setup>
import type { IMobileProfile } from '@/api/me'
import { getMyProfile } from '@/api/me'
import { storeToRefs } from 'pinia'
import { LOGIN_PAGE } from '@/router/config'
import { useUserStore } from '@/store'
import { useTokenStore } from '@/store/token'

definePage({
  style: {
    navigationBarTitleText: '我的',
  },
})

const userStore = useUserStore()
const tokenStore = useTokenStore()
const { userInfo } = storeToRefs(userStore)

const profile = ref<IMobileProfile | null>(null)

const displayName = computed(() => {
  if (!tokenStore.hasLogin)
    return '未登录'
  return profile.value?.nickname || userInfo.value.nickname || userInfo.value.username || '已登录用户'
})

const displayHint = computed(() => {
  if (!tokenStore.hasLogin)
    return '登录后可预约咨询、查看个人信息'
  const parts = []
  if (profile.value?.roleName)
    parts.push(profile.value.roleName)
  if (profile.value?.phone)
    parts.push(profile.value.phone)
  if (parts.length)
    return parts.join(' · ')
  return userInfo.value.username ? `账号 ${userInfo.value.username}` : '欢迎回来'
})

const avatarUrl = computed(() => profile.value?.avatar || userInfo.value.avatar)

async function loadProfile() {
  if (!tokenStore.hasLogin) {
    profile.value = null
    return
  }
  try {
    profile.value = await getMyProfile()
  }
  catch (e) {
    console.error('加载资料失败', e)
  }
}

function handleLogin() {
  uni.navigateTo({ url: LOGIN_PAGE })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        useTokenStore().logout()
        profile.value = null
        uni.showToast({ title: '退出登录成功', icon: 'success' })
      }
    },
  })
}

function goAppointments() {
  if (!tokenStore.hasLogin) {
    handleLogin()
    return
  }
  uni.navigateTo({ url: '/pages/me/appointments' })
}

function goAbout() {
  uni.navigateTo({ url: '/pages/about/about' })
}

onShow(() => {
  loadProfile()
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <fg-profile-card
      :display-name="displayName"
      :hint="displayHint"
      :logged-in="tokenStore.hasLogin"
      :avatar-url="avatarUrl"
      @login="handleLogin"
    />

    <view class="menu-card fg-surface-card mx-3 -mt-2 overflow-hidden">
      <view class="menu-item fg-tap-active" @click="goAppointments">
        <view class="flex items-center">
          <text class="i-carbon-calendar menu-icon" />
          <text class="text-base text-[#1C2B28]">我的预约</text>
        </view>
        <text class="i-carbon-chevron-right text-muted" />
      </view>
      <view class="menu-divider" />
      <view class="menu-item fg-tap-active" @click="goAbout">
        <view class="flex items-center">
          <text class="i-carbon-information menu-icon" />
          <text class="text-base text-[#1C2B28]">关于</text>
        </view>
        <text class="i-carbon-chevron-right text-muted" />
      </view>
    </view>

    <view v-if="tokenStore.hasLogin" class="mx-3 mt-6">
      <view class="logout-btn fg-surface-card fg-tap-active" @click="handleLogout">
        退出登录
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.menu-card {
  margin-top: -8px;
}
.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 52px;
  padding: 0 16px;
}
.menu-icon {
  margin-right: 12px;
  color: var(--color-primary);
  font-size: 18px;
}
.menu-divider {
  height: 1px;
  margin: 0 16px;
  background: var(--color-canvas);
}
.logout-btn {
  padding: 14px;
  text-align: center;
  font-size: 14px;
  color: #c45656;
}
</style>
