<script lang="ts" setup>
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

const displayName = computed(() => {
  if (!tokenStore.hasLogin)
    return '未登录'
  return userInfo.value.nickname || userInfo.value.username || '已登录用户'
})

const displayHint = computed(() => {
  if (!tokenStore.hasLogin)
    return '登录后可预约咨询、查看个人信息'
  return userInfo.value.username ? `账号 ${userInfo.value.username}` : '欢迎回来'
})

async function handleLogin() {
  // #ifdef MP-WEIXIN
  await tokenStore.wxLogin()
  // #endif
  // #ifndef MP-WEIXIN
  uni.navigateTo({
    url: LOGIN_PAGE,
  })
  // #endif
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        useTokenStore().logout()
        uni.showToast({
          title: '退出登录成功',
          icon: 'success',
        })
      }
    },
  })
}

function goAppointments() {
  uni.showToast({ title: '我的预约即将上线', icon: 'none' })
}

function goAbout() {
  uni.navigateTo({ url: '/pages/about/about' })
}
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <view class="flex items-center bg-white px-4 py-6">
      <view class="flex h-14 w-14 items-center justify-center rounded-full bg-[#E7F4F0] text-lg font-medium text-[#1B7F6B]">
        {{ displayName.slice(0, 1) }}
      </view>
      <view class="ml-3 min-w-0 flex-1">
        <view class="truncate text-lg font-semibold text-[#1C2B28]">
          {{ displayName }}
        </view>
        <view class="mt-1 truncate text-xs text-muted">
          {{ displayHint }}
        </view>
      </view>
      <view
        v-if="!tokenStore.hasLogin"
        class="inline-flex min-h-11 items-center rounded-full bg-[#1B7F6B] px-4 text-sm text-white"
        @click="handleLogin"
      >
        登录
      </view>
    </view>

    <view class="mx-3 mt-3 overflow-hidden rounded-[12px] bg-white">
      <view class="flex items-center justify-between px-4 py-4" @click="goAppointments">
        <view class="flex items-center">
          <text class="i-carbon-calendar mr-3 text-[#1B7F6B]" />
          <text class="text-sm text-[#1C2B28]">我的预约</text>
        </view>
        <text class="i-carbon-chevron-right text-muted" />
      </view>
      <view class="mx-4 h-px bg-[#F4F7F6]" />
      <view class="flex items-center justify-between px-4 py-4" @click="goAbout">
        <view class="flex items-center">
          <text class="i-carbon-information mr-3 text-[#1B7F6B]" />
          <text class="text-sm text-[#1C2B28]">关于</text>
        </view>
        <text class="i-carbon-chevron-right text-muted" />
      </view>
    </view>

    <view v-if="tokenStore.hasLogin" class="mx-3 mt-6">
      <view
        class="rounded-[12px] bg-white py-3 text-center text-sm text-[#C45656]"
        @click="handleLogout"
      >
        退出登录
      </view>
    </view>
  </view>
</template>
