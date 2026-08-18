<script lang="ts" setup>
import { useTokenStore } from '@/store/token'

definePage({
  style: {
    navigationBarTitleText: '登录',
  },
})

const tokenStore = useTokenStore()
const username = ref('')
const password = ref('')

function toast(title: string) {
  uni.showToast({ title, icon: 'none' })
}

async function doLogin() {
  if (tokenStore.hasLogin) {
    uni.navigateBack()
    return
  }
  const name = username.value.trim()
  const pwd = password.value.trim()
  if (!name) {
    toast('请输入账号')
    return
  }
  if (!pwd) {
    toast('请输入密码')
    return
  }
  try {
    await tokenStore.login({
      username: name,
      password: pwd,
    })
    uni.navigateBack()
  }
  catch (error) {
    console.log('登录失败', error)
  }
}

async function doWxLogin() {
  try {
    await tokenStore.wxLogin()
    uni.navigateBack()
  }
  catch (error) {
    console.log('微信登录失败', error)
  }
}
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6]">
    <view class="bg-[#E7F4F0] px-6 pb-8 pt-10">
      <view class="text-2xl font-semibold text-[#1B7F6B]">
        特教资源平台
      </view>
      <view class="mt-2 text-sm leading-relaxed text-muted">
        安心对接特教资源
      </view>
    </view>

    <view class="mx-4 -mt-4 rounded-[12px] bg-white px-5 py-6">
      <view class="mb-4">
        <text class="mb-2 block text-sm text-muted">账号</text>
        <input
          v-model="username"
          class="h-11 w-full rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
          placeholder="请输入账号，如 admin"
          confirm-type="next"
        >
      </view>
      <view class="mb-6">
        <text class="mb-2 block text-sm text-muted">密码</text>
        <input
          v-model="password"
          class="h-11 w-full rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
          password
          placeholder="请输入密码，如 admin123"
          confirm-type="done"
        >
      </view>
      <wd-button block type="primary" @click="doLogin">
        登录
      </wd-button>
      <!-- #ifdef MP-WEIXIN -->
      <view class="mt-3">
        <wd-button block plain @click="doWxLogin">
          微信一键登录
        </wd-button>
      </view>
      <!-- #endif -->
    </view>
  </view>
</template>
