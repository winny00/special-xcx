<script lang="ts" setup>
import { register, sendSmsCode } from '@/api/login'
import { LOGIN_PAGE } from '@/router/config'

definePage({
  style: {
    navigationBarTitleText: '注册',
  },
})

const phone = ref('')
const code = ref('')
const password = ref('')
const countdown = ref(0)
const submitting = ref(false)
const sending = ref(false)
let timer: ReturnType<typeof setInterval> | undefined

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
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

function goLogin() {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: LOGIN_PAGE })
    },
  })
}

async function doRegister() {
  const phoneNumber = phone.value.trim()
  const smsCode = code.value.trim()
  const pwd = password.value.trim()
  if (!isPhone(phoneNumber)) {
    toast('请输入正确的手机号')
    return
  }
  if (!smsCode) {
    toast('请输入验证码')
    return
  }
  if (!pwd) {
    toast('请输入密码')
    return
  }
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    await register({
      username: phoneNumber,
      password: pwd,
      code: smsCode,
    })
    uni.showToast({ title: '注册成功', icon: 'success' })
    uni.redirectTo({ url: LOGIN_PAGE })
  }
  catch (error) {
    console.log('注册失败', error)
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
        注册家长账号
      </view>
      <view class="mt-2 text-sm leading-relaxed text-muted">
        用手机号开通账号，方便预约课程与老师
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
      <view class="mb-4">
        <text class="mb-2 block text-sm text-muted">验证码</text>
        <view class="flex items-center gap-2">
          <input
            v-model="code"
            class="h-11 min-w-0 flex-1 rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
            type="number"
            maxlength="6"
            placeholder="请输入验证码"
            confirm-type="next"
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
      <view class="mb-6">
        <text class="mb-2 block text-sm text-muted">密码</text>
        <input
          v-model="password"
          class="h-11 w-full rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
          password
          placeholder="请输入密码"
          confirm-type="done"
        >
      </view>
      <wd-button block type="primary" :disabled="submitting" @click="doRegister">
        注册
      </wd-button>
      <view
        class="mt-2 flex h-11 items-center justify-center"
        @click="goLogin"
      >
        <text class="text-base text-[#1B7F6B]">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>
