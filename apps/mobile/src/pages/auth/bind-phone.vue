<script lang="ts" setup>
import { bindMyPhone } from '@/api/me'
import { sendSmsCode } from '@/api/login'
import { LOGIN_PAGE } from '@/router/config'
import { useUserStore } from '@/store'
import { useTokenStore } from '@/store/token'
import { syncCachedRole } from '@/utils/current-role'

definePage({
  style: {
    navigationBarTitleText: '绑定手机号',
  },
})

const tokenStore = useTokenStore()
const userStore = useUserStore()
const phone = ref('')
const code = ref('')
const countdown = ref(0)
const submitting = ref(false)
const sending = ref(false)
const redirect = ref('')
let timer: ReturnType<typeof setInterval> | undefined

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

onLoad((query) => {
  const raw = typeof query?.redirect === 'string' ? query.redirect : ''
  redirect.value = raw ? decodeURIComponent(raw) : ''
})

function toast(title: string) {
  uni.showToast({ title, icon: 'none' })
}

function isPhone(value: string) {
  return /^\d{11}$/.test(value)
}

function startCountdown() {
  countdown.value = 60
  if (timer) {
    clearInterval(timer)
  }
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0 && timer) {
      clearInterval(timer)
      timer = undefined
    }
  }, 1000)
}

async function sendCode() {
  const phoneNumber = phone.value.trim()
  if (!isPhone(phoneNumber)) {
    toast('请输入正确的手机号')
    return
  }
  if (countdown.value > 0 || sending.value) {
    return
  }
  sending.value = true
  try {
    await sendSmsCode(phoneNumber)
    toast('验证码已发送')
    startCountdown()
  }
  catch (error) {
    console.log('发送验证码失败', error)
  }
  finally {
    sending.value = false
  }
}

function goAfterBind() {
  const url = redirect.value
  if (url.startsWith('/pages/') && !url.includes('://')) {
    const path = url.split('?')[0]
    if (
      path === '/pages/index/index'
      || path === '/pages/resource/list'
      || path === '/pages/organization/list'
      || path === '/pages/me/me'
    ) {
      uni.switchTab({ url: path })
      return
    }
    uni.redirectTo({ url })
    return
  }
  uni.navigateBack({
    fail: () => {
      uni.switchTab({ url: '/pages/index/index' })
    },
  })
}

async function doBind() {
  if (!tokenStore.hasLogin) {
    uni.navigateTo({ url: LOGIN_PAGE })
    return
  }
  const phoneNumber = phone.value.trim()
  const smsCode = code.value.trim()
  if (!isPhone(phoneNumber)) {
    toast('请输入正确的手机号')
    return
  }
  if (!smsCode) {
    toast('请输入验证码')
    return
  }
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    const vo = await bindMyPhone({ phone: phoneNumber, smsCode })
    tokenStore.setTokenInfo({
      token: vo.access_token || '',
      expiresIn: Number(vo.expire_in) || 7200,
    })
    const info = await userStore.fetchUserInfo()
    syncCachedRole(info.roles ?? [])
    uni.showToast({ title: '绑定成功', icon: 'success' })
    goAfterBind()
  }
  catch (error) {
    console.log('绑定手机号失败', error)
  }
  finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6]">
    <view class="fg-hero-gradient px-6 pb-10 pt-10">
      <view class="text-2xl font-semibold text-[#1B7F6B]">
        绑定手机号
      </view>
      <view class="mt-2 text-sm leading-relaxed text-muted">
        绑定后可预约课程、查看老师资料
      </view>
    </view>

    <view class="mx-4 -mt-6 fg-surface-card px-5 py-6">
      <view class="mb-4">
        <text class="mb-2 block text-sm text-muted">手机号</text>
        <input
          v-model="phone"
          class="h-11 w-full rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
          type="number"
          maxlength="11"
          placeholder="请输入手机号"
          confirm-type="next"
        >
      </view>
      <view class="mb-6">
        <text class="mb-2 block text-sm text-muted">验证码</text>
        <view class="flex items-center gap-2">
          <input
            v-model="code"
            class="h-11 min-w-0 flex-1 rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
            type="number"
            maxlength="6"
            placeholder="请输入验证码"
            confirm-type="done"
          >
          <wd-button
            type="primary"
            size="small"
            :disabled="countdown > 0 || sending"
            custom-class="!min-h-11 !px-3"
            @click="sendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </wd-button>
        </view>
      </view>
      <wd-button block type="primary" :disabled="submitting" @click="doBind">
        绑定
      </wd-button>
    </view>
  </view>
</template>
